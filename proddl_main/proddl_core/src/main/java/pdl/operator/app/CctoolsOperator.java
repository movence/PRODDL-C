/*
 * Copyright J. Craig Venter Institute, 2011
 *
 * The creation of this program was supported by the U.S. National
 * Science Foundation grant 1048199 and the Microsoft allocation
 * in the MS Azure cloud.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pdl.operator.app;

import pdl.cloud.model.DynamicData;
import pdl.cloud.storage.TableOperator;
import pdl.common.Configuration;
import pdl.common.StaticValues;
import pdl.common.ToolPool;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 8/15/11
 * Time: 9:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class CctoolsOperator extends AbstractApplicationOperator {
    private static final String KEY_DYNAMIC_DATA_CATALOGSERVERADDRESS = "CatalogServerAddress";
    private static final String KEY_DYNAMIC_DATA_CATALOGSERVERPORT = "CatalogServerPort";

    private Configuration conf;

    private List<Process> processes;

    private String cctoolsBinPath;
    private String cygwinBinPath;

    private String catalogServerAddress;
    private String catalogServerPort;

    private boolean isEnvironmentVarialbeSet = false;

    public CctoolsOperator(String storagePath, String packageName, String flagFile, String param) {
        super(storagePath, packageName, flagFile, param);
        setPaths();
    }

    private void setPaths() {
        try {
            conf = Configuration.getInstance();

            cctoolsBinPath = packagePath + File.separator + "bin";
            cygwinBinPath = storagePath + conf.getProperty("CYGWIN_NAME") + File.separator + "bin";

            processes = new ArrayList<Process>();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (processes != null && processes.size() > 0) {
                        for (Process currProcess : processes)
                            currProcess.destroy();
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean start(String param) {
        return true;
    }

    private ProcessBuilder buildProcessBuilder(String... args) {
        if (cygwinBinPath == null || cctoolsBinPath == null)
            this.setPaths();

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(new File(cctoolsBinPath));
        pb.redirectErrorStream(true);

        if (!isEnvironmentVarialbeSet) {
            Map<String, String> env = pb.environment();
            for (String key : env.keySet()) {
                if (key.toLowerCase().equals("path")) {
                    env.put(key, cygwinBinPath + File.pathSeparator + cctoolsBinPath + File.pathSeparator + env.get(key));
                    isEnvironmentVarialbeSet = true;
                    break;
                }
            }

            //If no Path variable found in environment, add it
            if(!isEnvironmentVarialbeSet) {
                env.put("path", cygwinBinPath + File.pathSeparator + cctoolsBinPath);
                isEnvironmentVarialbeSet = true;
            }
        }
        return pb;
    }

    public boolean startMakeflow(String taskName, String taskFileName, String taskDirectory) {
        boolean rtnVal = false;

        try {
            validateCatalogServerInformation();

            if (taskFileName != null && !taskFileName.isEmpty() && taskDirectory != null && !taskDirectory.isEmpty()) {
                if (ToolPool.isDirectoryExist(taskDirectory)) {
                    File currFile = new File(taskDirectory + File.separator + taskFileName);
                    if (currFile.exists() || currFile.canRead()) {
                        ProcessBuilder pb = this.buildProcessBuilder(
                                cctoolsBinPath + File.separator + "makeflow",
                                "-T", "wq",
                                "-C", catalogServerAddress + ":" + catalogServerPort, "-a", //catalog server and advertise makeflow to catalog server
                                "-p", "-1", //random port
                                "-N", taskName,
                                currFile.getPath());

                        Process process = pb.start();
                        processes.add(process);

                        BufferedReader ireader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        //storageOperator.enqueue( StaticValues.QUEUE_JOBQUEUE_NAME, taskName );
                        while ((line = ireader.readLine()) != null) {
                            System.out.println("MAKEFLOW OUTPUT: " + line);
                        }

                        System.out.printf("Makeflow process for job(%s) has been completed.%n", taskName);

                        rtnVal = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnVal;
    }

    public boolean startWorkQ() {
        boolean rtnVal = false;
        try {
            validateCatalogServerInformation();

            //String taskName = storageOperator.dequeue( StaticValues.QUEUE_JOBQUEUE_NAME, true );

            ProcessBuilder pb = this.buildProcessBuilder(
                    cctoolsBinPath + File.separator + "work_queue_worker",
                    "-C", catalogServerAddress + ":" + catalogServerPort, "-a", "-s");
            //"-N", taskName );

            Process process = pb.start();
            processes.add(process);

            BufferedReader ireader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = ireader.readLine()) != null) {
                System.out.println("WORKQ OUTPUT: " + line);
            }

            System.out.println("Makeflow process for has been completed.");

            rtnVal = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnVal;
    }

    public boolean startCatalogServer(String catalogServerAddress, String catalogServerPort) {
        boolean rtnVal = false;

        try {
            this.catalogServerAddress = catalogServerAddress;
            this.catalogServerPort = catalogServerPort;

            this.updateCatalogServerInfo(KEY_DYNAMIC_DATA_CATALOGSERVERADDRESS, catalogServerAddress);
            this.updateCatalogServerInfo(KEY_DYNAMIC_DATA_CATALOGSERVERPORT, catalogServerPort);

            ProcessBuilder pb = this.buildProcessBuilder(cctoolsBinPath + File.separator + "catalog_server", "-p", catalogServerPort);

            Process process = pb.start();
            processes.add(process);
            /*BufferedReader ireader = new BufferedReader( new InputStreamReader( catalogServerProcess.getInputStream() ) );
            String line;
            while ( (line = ireader.readLine ()) != null ) {
                if( line.contains( "catalog_server" ) && line.contains( "starting" ) ) {
                    rtnVal = true;
                    break;
                }
            }*/

            rtnVal = true;

            System.out.println("CctoolsOperator : STARTING CATALOG_SERVER DONE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnVal;
    }

    private void validateCatalogServerInformation() {
        this.getCatalogServerAddress();
        this.getCatalogServerPort();
    }

    public String getCatalogServerAddress() {
        try {
            if (this.catalogServerAddress == null || this.catalogServerAddress.isEmpty())
                this.catalogServerAddress = this.getCatalogServerInfo(KEY_DYNAMIC_DATA_CATALOGSERVERADDRESS);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this.catalogServerAddress;
    }

    public String getCatalogServerPort() {
        try {
            if (this.catalogServerPort == null || this.catalogServerPort.isEmpty())
                this.catalogServerPort = this.getCatalogServerInfo(KEY_DYNAMIC_DATA_CATALOGSERVERPORT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this.catalogServerPort;
    }

    public void updateCatalogServerInfo(String key, String value) {
        try {
            DynamicData catalogServerInfo = new DynamicData("catalogserver_info");
            catalogServerInfo.setDataKey(key);
            catalogServerInfo.setDataValue(value);

            TableOperator tableOperator = new TableOperator(conf);
            tableOperator.insertSingleEntity(conf.getStringProperty("TABLE_NAME_DYNAMIC_DATA"), catalogServerInfo);
            System.out.printf(
                    "Update catalogServerPort('%s':'%s') information to '%s'%n",
                    key, value, conf.getStringProperty("TABLE_NAME_DYNAMIC_DATA"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCatalogServerInfo(String key) {
        String rtnVal = null;

        try {
            TableOperator tableOperator = new TableOperator(conf);

            DynamicData catalogServerInfo = (DynamicData) tableOperator.queryEntityBySearchKey(
                    conf.getStringProperty("TABLE_NAME_DYNAMIC_DATA"),
                    StaticValues.COLUMN_DYNAMIC_DATA_KEY,
                    key,
                    DynamicData.class
            );

            if (catalogServerInfo != null)
                rtnVal = catalogServerInfo.getDataValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnVal;
    }

}

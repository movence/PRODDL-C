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

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final String LOOKUP_KEY_CATALOGSERVER_INFO = "$catalogServer";
    private static final String LOOKUP_KEY_TASK_NAME = "$taskName";

    private Configuration conf;

    private List<Process> processes;

    private String cctoolsBinPath;
    private String cygwinBinPath;

    private String catalogServerAddress;
    private String catalogServerPort;

    private final static String[] makeflowArgs =
        {
            "-T", "wq", //batch system
            "-p", "-1", //random port
            "-C", LOOKUP_KEY_CATALOGSERVER_INFO, "-a", //catalog server and advertise makeflow to catalog server
            "-N", LOOKUP_KEY_TASK_NAME //project name
        };

    private final static String[] workerArgs = {
        "-s", "-t", "21600", //max idle time
        "-C", LOOKUP_KEY_CATALOGSERVER_INFO, "-a", //catalog server and advertise makeflow to catalog server
        //"-N", taskName
    };

    public CctoolsOperator(String storagePath, String packageName, String flagFile, String param) {
        super(storagePath, packageName, flagFile, param);
        setPaths();
    }

    private void setPaths() {
        try {
            conf = Configuration.getInstance();

            cctoolsBinPath = ToolPool.buildFilePath(packagePath, "bin");
            cygwinBinPath = ToolPool.buildFilePath(storagePath, conf.getStringProperty("CYGWIN_NAME"), "bin");

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

    private ProcessBuilder buildProcessBuilder(List<String> args) {
        boolean isEnvironmentVarialbeSet = false;

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

    public boolean startMakeflow(boolean isClean, String taskName, String taskFileName, String taskDirectory) {
        boolean rtnVal = false;

        try {
            this.isCatalogServerInfoAvailable();

            if (taskFileName != null && !taskFileName.isEmpty() && taskDirectory != null && !taskDirectory.isEmpty()) {
                if (ToolPool.isDirectoryExist(taskDirectory)) {
                    File currFile = new File(ToolPool.buildFilePath(taskDirectory,taskFileName));
                    if (currFile.exists() || currFile.canRead()) {

                        List<String> processArgs = new ArrayList<String>();
                        processArgs.add(ToolPool.buildFilePath(cctoolsBinPath, "makeflow"));

                        if(isClean)
                            processArgs.add("-c"); //clean up log files and all targets
                        else {
                            processArgs.addAll(Arrays.asList(makeflowArgs));
                            processArgs.set(processArgs.indexOf(LOOKUP_KEY_CATALOGSERVER_INFO), catalogServerAddress + ":" + catalogServerPort);
                            processArgs.set(processArgs.indexOf(LOOKUP_KEY_TASK_NAME), taskName);
                        }
                        processArgs.add(currFile.getPath());

                        ProcessBuilder pb = this.buildProcessBuilder(processArgs);
                        pb.directory(new File(taskDirectory));
                        Process process = pb.start();
                        processes.add(process);

                        boolean mfSucceded = true;
                        BufferedReader ireader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        //storageOperator.enqueue( StaticValues.QUEUE_JOBQUEUE_NAME, taskName );
                        while ((line = ireader.readLine()) != null) {
                            if(line.toLowerCase().contains("workflow failed")) //makeflow fails
                                mfSucceded = false;
                            System.out.println("MAKEFLOW OUTPUT: " + line);
                        }

                        System.out.printf("Makeflow process for job(%s) has been completed.%n", taskName);

                        rtnVal = mfSucceded;
                    } else
                        throw new Exception("CCTOOLS-startMakeflow(): Makeflow(task) file does not exist!");

                } else
                    throw new Exception("CCTOOLS-startMakeflow(): Task directory does not exist!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnVal;
    }

    public boolean startWorkQ() {
        boolean rtnVal = false;
        try {
            this.isCatalogServerInfoAvailable();
            //String taskName = storageOperator.dequeue( StaticValues.QUEUE_JOBQUEUE_NAME, true );

            List<String> processArgs = new ArrayList<String>();
            processArgs.add(ToolPool.buildFilePath(cctoolsBinPath, "work_queue_worker"));
            processArgs.addAll(Arrays.asList(workerArgs));
            processArgs.set(processArgs.indexOf(LOOKUP_KEY_CATALOGSERVER_INFO), catalogServerAddress + ":" + catalogServerPort);

            ProcessBuilder pb = this.buildProcessBuilder(processArgs);
            Process process = pb.start();
            processes.add(process);

            LogStreamReader lsr = new LogStreamReader(process.getInputStream());
            Thread thread = new Thread(lsr, "LogStreamReader");
            thread.start();

            process.waitFor();
            System.out.println("worker process has been completed.");

            rtnVal = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnVal;
    }

    public boolean startCatalogServer(String catalogServerAddress, String catalogServerPort) {
        boolean rtnVal = false;

        try {
            this.setCatalogServerInfo(catalogServerAddress, catalogServerPort);

            List<String> processArgs = new ArrayList<String>();
            processArgs.add(ToolPool.buildFilePath(cctoolsBinPath, "catalog_server"));
            processArgs.add("-p");
            processArgs.add(catalogServerPort);

            ProcessBuilder pb = this.buildProcessBuilder(processArgs);
            Process process = pb.start();
            processes.add(process);

            rtnVal = true;

            System.out.println("CctoolsOperator : STARTING CATALOG_SERVER DONE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnVal;
    }

    private void setCatalogServerInfo(String address, String port) {
        this.catalogServerAddress = address;
        this.catalogServerPort = port;

        this.updateCatalogServerInfo(KEY_DYNAMIC_DATA_CATALOGSERVERADDRESS, address);
        this.updateCatalogServerInfo(KEY_DYNAMIC_DATA_CATALOGSERVERPORT, port);
    }

    public boolean isCatalogServerInfoAvailable() {
        try {
            DynamicData info;
            if (this.catalogServerAddress == null || this.catalogServerAddress.isEmpty()) {
                info = this.getCatalogServerInfo(KEY_DYNAMIC_DATA_CATALOGSERVERADDRESS);
                if(info !=null)
                    this.catalogServerAddress = info.getDataValue();
            }
            if (this.catalogServerPort == null || this.catalogServerPort.isEmpty()) {
                info = this.getCatalogServerInfo(KEY_DYNAMIC_DATA_CATALOGSERVERPORT);
                if(info !=null)
                    this.catalogServerPort = info.getDataValue();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this.catalogServerAddress!=null && this.catalogServerPort!=null;
    }

    public void updateCatalogServerInfo(String key, String value) {
        try {
            TableOperator tableOperator = new TableOperator(conf);
            String tableName = conf.getStringProperty("TABLE_NAME_DYNAMIC_DATA");

            DynamicData catalogServerInfo = this.getCatalogServerInfo(key);
            if(catalogServerInfo!=null)
                tableOperator.deleteEntity(tableName, catalogServerInfo);

            catalogServerInfo = new DynamicData("catalogserver_info");
            catalogServerInfo.setDataKey(key);
            catalogServerInfo.setDataValue(value);

            tableOperator.insertSingleEntity(tableName, catalogServerInfo);
            System.out.printf("Update catalogServerPort('%s':'%s') information to '%s'%n", key, value, tableName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DynamicData getCatalogServerInfo(String key) {
        DynamicData catalogServerInfo = null;

        try {
            TableOperator tableOperator = new TableOperator(conf);

            catalogServerInfo = (DynamicData) tableOperator.queryEntityBySearchKey(
                    conf.getStringProperty("TABLE_NAME_DYNAMIC_DATA"),
                    StaticValues.COLUMN_DYNAMIC_DATA_KEY,
                    key,
                    DynamicData.class
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
        return catalogServerInfo;
    }

    public class LogStreamReader implements Runnable {
        private BufferedReader reader;
        public LogStreamReader(InputStream is) {
            this.reader = new BufferedReader(new InputStreamReader(is));
        }

        public void run() {
            try {
                String line = reader.readLine();
                while (line != null) {
                    System.out.println(line);
                    line = reader.readLine();
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

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
    private static final String LOOKUP_KEY_PORT = "$port";
    private static final String LOOKUP_KEY_DEBUG_LOG = "$debug";

    private static final String ENV_MAKEFLOW_LOW_PORT = "TCP_LOW_PORT";
    private static final String ENV_MAKEFLOW_MAX_PORT = "TCP_MAX_PORT";

    private static final String GENERIC_TASK_NAME = "proddl";

    private Configuration conf;

    private List<Process> processes;

    private String cctoolsBinPath;
    private String cygwinBinPath;

    private String catalogServerAddress;
    private String catalogServerPort;

    private final static String[] makeflowArgs = {
            "-T", "wq", //batch system
            "-p", LOOKUP_KEY_PORT, //random port
            "-C", LOOKUP_KEY_CATALOGSERVER_INFO, "-a", //catalog server and advertise makeflow to catalog server
            "-N", LOOKUP_KEY_TASK_NAME, //LOOKUP_KEY_TASK_NAME, //project name
            "-r", "3", //retry
            "-L", "final.log"
            /*"-d", "all", //debugging sub-system
            "-o", LOOKUP_KEY_DEBUG_LOG //debugging log*/
    };

    private final static String[] workerArgs = {
            "-t", "21600", //max idle time
            "-C", LOOKUP_KEY_CATALOGSERVER_INFO, "-a", //catalog server and advertise makeflow to catalog server
            "-z", "1024", //available disk space in MB
            "-i", "3", //initial back-off time to connection failure to master
            "-N", LOOKUP_KEY_TASK_NAME
    };

    public CctoolsOperator(String storagePath, String packageName, String flagFile) {
        super(storagePath, packageName, flagFile, null);
        setPaths();
    }

    private void setPaths() {
        try {
            conf = Configuration.getInstance();

            cctoolsBinPath = ToolPool.buildFilePath(toolPath, "bin");
            cygwinBinPath = ToolPool.buildFilePath(storagePath, "cygwin", "bin");

            //create /tmp directory under cctools
            File tmpDir = new File(ToolPool.buildFilePath(toolPath, "tmp"));
            if(!tmpDir.exists() || !tmpDir.isDirectory())
                tmpDir.mkdir();

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

    public ProcessBuilder buildProcessBuilder(String taskDir, List<String> args, boolean setPortRange) {
        boolean isEnvironmentVarialbeSet = false;

        if (cygwinBinPath == null || cctoolsBinPath == null)
            this.setPaths();

        ProcessBuilder pb = new ProcessBuilder(args);
        if(taskDir==null)
            pb.directory(new File(cctoolsBinPath));
        else
            pb.directory(new File(taskDir));
        pb.redirectErrorStream(true);

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
        }

        env.put(ENV_MAKEFLOW_LOW_PORT, "9001");
        env.put(ENV_MAKEFLOW_MAX_PORT, "40000");

        return pb;
    }

    public boolean startMakeflow(boolean isClean, String taskName, String taskFileName, String taskDirectory) {
        boolean rtnVal = false;
        Process process = null;

        try {
            this.isCatalogServerInfoAvailable();

            if (taskFileName != null && !taskFileName.isEmpty() && taskDirectory != null && !taskDirectory.isEmpty()) {
                if (ToolPool.isDirectoryExist(taskDirectory)) {
                    File currFile = new File(ToolPool.buildFilePath(taskDirectory,taskFileName));
                    if (currFile.exists() || currFile.canRead()) {

                        List<String> processArgs = new ArrayList<String>();
                        processArgs.add(cctoolsBinPath+"makeflow");

                        if(isClean)
                            processArgs.add("-c"); //clean up log files and all targets
                        else {
                            processArgs.addAll(Arrays.asList(makeflowArgs));
                            processArgs.set(processArgs.indexOf(LOOKUP_KEY_CATALOGSERVER_INFO), catalogServerAddress + ":" + catalogServerPort);
                            //processArgs.set(processArgs.indexOf(LOOKUP_KEY_TASK_NAME), taskName);
                            processArgs.set(processArgs.indexOf(LOOKUP_KEY_TASK_NAME), GENERIC_TASK_NAME);
                            processArgs.set(processArgs.indexOf(LOOKUP_KEY_PORT), "0"/*String.valueOf(makeflowPort++)*/);
                            //processArgs.set(processArgs.indexOf(LOOKUP_KEY_DEBUG_LOG), "mf_debug.log");
                        }
                        processArgs.add(currFile.getPath());

                        ProcessBuilder pb = this.buildProcessBuilder(taskDirectory, processArgs, true);
                        process = pb.start();
                        processes.add(process);

                        LogStreamReader errorGobbler = new LogStreamReader(process.getErrorStream(), "ERROR-[Makeflow]");
                        LogStreamReader outputGobbler = new LogStreamReader(process.getInputStream(), "OUTPUT-[Makeflow]");
                        errorGobbler.start();
                        outputGobbler.start();

                        rtnVal = process.waitFor()==0;
                        System.out.printf("Makeflow process for job(%s) has been completed.%n", taskName);
                    } else
                        throw new Exception("CCTOOLS-startMakeflow(): Makeflow(task) file does not exist!");
                } else
                    throw new Exception("CCTOOLS-startMakeflow(): Task directory does not exist!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(processes!=null && processes.contains(process))
                processes.remove(process);
            if(process!=null)
                process.destroy();
        }
        return rtnVal;
    }

    public boolean startWorkQ() {
        boolean rtnVal = false;
        Process process = null;

        try {
            this.isCatalogServerInfoAvailable();
            //String taskName = storageOperator.dequeue( StaticValues.QUEUE_JOBQUEUE_NAME, true );

            List<String> processArgs = new ArrayList<String>();
            processArgs.add(cctoolsBinPath+"work_queue_worker");
            processArgs.addAll(Arrays.asList(workerArgs));
            processArgs.set(processArgs.indexOf(LOOKUP_KEY_CATALOGSERVER_INFO), catalogServerAddress + ":" + catalogServerPort);
            processArgs.set(processArgs.indexOf(LOOKUP_KEY_TASK_NAME), GENERIC_TASK_NAME);

            ProcessBuilder pb = this.buildProcessBuilder(null, processArgs, false);
            process = pb.start();
            processes.add(process);

            LogStreamReader errorGobbler = new LogStreamReader(process.getErrorStream(), "ERROR-[Worker]");
            LogStreamReader outputGobbler = new LogStreamReader(process.getInputStream(), "OUTPUT-[Worker]");
            errorGobbler.start();
            outputGobbler.start();

            rtnVal = process.waitFor()==0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(processes!=null && processes.contains(process))
                processes.remove(process);
            if(process!=null)
                process.destroy();
        }
        return rtnVal;
    }

    public boolean startCatalogServer(String catalogServerAddress, String catalogServerPort) {
        Process process = null;
        boolean rtnVal = false;

        try {
            this.setCatalogServerInfo(catalogServerAddress, catalogServerPort);

            List<String> processArgs = new ArrayList<String>();
            processArgs.add(cctoolsBinPath+"catalog_server");
            processArgs.add("-p");
            processArgs.add(catalogServerPort);

            ProcessBuilder pb = this.buildProcessBuilder(null, processArgs, false);
            process = pb.start();
            /*process = Runtime.getRuntime().exec(cctoolsBinPath+"catalog_server" + " -p " + catalogServerPort);
            processes.add(process);*/

            /*LogStreamReader errorGobbler = new LogStreamReader(process.getErrorStream(), "CS ERROR");
            LogStreamReader outputGobbler = new LogStreamReader(process.getInputStream(), "CS OUTPUT");
            errorGobbler.start();
            outputGobbler.start();*/

            System.out.println("CctoolsOperator : STARTING CATALOG_SERVER DONE");
            rtnVal = true;
        } catch (Exception e) {
            e.printStackTrace();
            if(processes!=null && processes.contains(process))
                processes.remove(process);

            if(process!=null)
                process.destroy();
        } finally {
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

            DynamicData catalogServerInfo = this.getCatalogServerInfo(key);
            if(catalogServerInfo!=null)
                tableOperator.deleteEntity(ToolPool.buildTableName(StaticValues.TABLE_NAME_DYNAMIC_DATA), catalogServerInfo);

            catalogServerInfo = new DynamicData("catalogserver_info");
            catalogServerInfo.setDataKey(key);
            catalogServerInfo.setDataValue(value);

            tableOperator.insertSingleEntity(ToolPool.buildTableName(StaticValues.TABLE_NAME_DYNAMIC_DATA), catalogServerInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DynamicData getCatalogServerInfo(String key) {
        DynamicData catalogServerInfo = null;

        try {
            TableOperator tableOperator = new TableOperator(conf);

            catalogServerInfo = (DynamicData) tableOperator.queryEntityBySearchKey(
                    ToolPool.buildTableName(StaticValues.TABLE_NAME_DYNAMIC_DATA),
                    StaticValues.COLUMN_DYNAMIC_DATA_KEY,
                    key,
                    DynamicData.class
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
        return catalogServerInfo;
    }

    public boolean startBash(String taskFileName, String taskDirectory) {
        boolean rtnVal = false;
        Process process = null;
        try {
            File currFile = new File(ToolPool.buildFilePath(taskDirectory,taskFileName));
            if (currFile.exists() || currFile.canRead()) {

                List<String> processArgs = new ArrayList<String>();
                processArgs.add(ToolPool.buildFilePath(cygwinBinPath, "bash"));
                processArgs.add(currFile.getPath());

                ProcessBuilder pb = this.buildProcessBuilder(taskDirectory, processArgs, false);
                process = pb.start();
                processes.add(process);

                LogStreamReader errorGobbler = new LogStreamReader(process.getErrorStream(), "ERROR-[Bash]");
                LogStreamReader outputGobbler = new LogStreamReader(process.getInputStream(), "OUTPUT-[Bash]");
                errorGobbler.start();
                outputGobbler.start();

                rtnVal = process.waitFor()==0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(processes!=null && processes.contains(process))
                processes.remove(process);
        }
        return rtnVal;
    }

    public boolean startExe(String taskFileName, String taskDirectory) {
        boolean rtnVal = false;
        try {
            File currFile = new File(ToolPool.buildFilePath(taskDirectory,taskFileName));
            if (currFile.exists() || currFile.canRead()) {
                Process process = Runtime.getRuntime().exec(taskFileName);

                LogStreamReader errorGobbler = new LogStreamReader(process.getErrorStream(), "ERROR-[Exe]");
                LogStreamReader outputGobbler = new LogStreamReader(process.getInputStream(), "OUTPUT-[Exe]");
                errorGobbler.start();
                outputGobbler.start();

                rtnVal = process.waitFor()==0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return rtnVal;
    }

    class LogStreamReader extends Thread {
        InputStream is;
        String type;

        LogStreamReader(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line=null;
                while ( (line = br.readLine()) != null)
                    System.out.println(type+" "+line);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}

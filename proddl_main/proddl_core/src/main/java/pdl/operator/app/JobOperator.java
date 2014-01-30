/*
 * Copyright J. Craig Venter Institute, 2014
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package pdl.operator.app;

import pdl.utils.Configuration;
import pdl.utils.StaticValues;
import pdl.utils.ToolPool;

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
public class JobOperator {
    private Configuration conf = null;

    private static List<Process> processes;

    public JobOperator() {
        this.init();
    }

    public void init() {
        try {
            conf = Configuration.getInstance();

            //add shutdown hook to any running processes
            processes = new ArrayList<Process>();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (processes.size() > 0) {
                        for (Process currProcess : processes)
                            currProcess.destroy();
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ProcessBuilder buildProcessBuilder(String taskDir, List<String> args, String envPath) {
        ProcessBuilder pb = new ProcessBuilder(args);
        if(taskDir != null) {
            pb.directory(new File(taskDir));
        }
        pb.redirectErrorStream(true);

        Map<String, String> env = pb.environment();
        boolean isPathSet = false;

        if(envPath != null && !envPath.isEmpty()) {
            for (String key : env.keySet()) {
                if (key.toLowerCase().equals("path")) {
                    env.put(key, envPath + File.pathSeparator + env.get(key));
                    isPathSet = true;
                    break;
                }
            }

            if(!isPathSet) {
                env.put("PATH", envPath);
            }
        }

        return pb;
    }

    public boolean startMakeflow(boolean isClean, String taskName, String taskFileName, String taskDirectory) {
        boolean rtnVal = false;
        Process process = null;

        try {
            if (taskFileName != null && !taskFileName.isEmpty() && taskDirectory != null && !taskDirectory.isEmpty()) {
                if (ToolPool.isDirectoryExist(taskDirectory)) {
                    File currFile = new File(ToolPool.buildFilePath(taskDirectory, taskFileName));
                    if (currFile.exists() || currFile.canRead()) {

                        List<String> processArgs = new ArrayList<String>();
                        processArgs.add("makeflow");

                        if(isClean)
                            processArgs.add("-c"); //clean up log files and all targets
                        else {
                            if(conf.hasProperty(StaticValues.CONFIG_KEY_MAKEFLOW_ARGS)) {
                                String makeflowArgs = conf.getStringProperty(StaticValues.CONFIG_KEY_MAKEFLOW_ARGS);
                                String[] makeflowArgsArr = makeflowArgs.split(" ");
                                processArgs.addAll(Arrays.asList(makeflowArgsArr));
                            }

                            if(conf.hasProperty(StaticValues.CONFIG_KEY_START_CATALOG_SERVER)
                                    && "true".equals(conf.getStringProperty(StaticValues.CONFIG_KEY_START_CATALOG_SERVER).toLowerCase())) {
                                processArgs.add("-C");
                                processArgs.add("127.0.0.1" + ":" + conf.getStringProperty(StaticValues.CONFIG_KEY_CATALOG_SERVER_PORT));
                                processArgs.add("-a"); //advertise to catalog server
                            }

                            //add task name
                            processArgs.add("-N");
                            processArgs.add("proddl");
                        }
                        processArgs.add(currFile.getPath());

                        //if there is configuration value of path to cctools
                        String cctoolsPath = null;
                        if(conf.hasProperty(StaticValues.CONFIG_KEY_CCTOOLS_PATH)) {
                            cctoolsPath = conf.getStringProperty(StaticValues.CONFIG_KEY_CCTOOLS_PATH) + "bin";
                        }

                        ProcessBuilder pb = this.buildProcessBuilder(taskDirectory, processArgs, cctoolsPath);
                        process = pb.start();
                        processes.add(process);

                        LogStreamReader errorGobbler = new LogStreamReader(process.getErrorStream(), "ERROR-[Makeflow]");
                        LogStreamReader outputGobbler = new LogStreamReader(process.getInputStream(), "OUTPUT-[Makeflow]");
                        errorGobbler.start();
                        outputGobbler.start();

                        rtnVal = process.waitFor()==0;
                        System.out.printf("makeflow process for job(%s) completed.%n", taskName);
                    } else
                        throw new Exception("CCTOOLS-startMakeflow(): makeflow(task) file not found!");
                } else
                    throw new Exception("CCTOOLS-startMakeflow(): task directory not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(processes != null && processes.contains(process)) {
                processes.remove(process);
            }
            if(process != null) {
                process.destroy();
            }
        }
        return rtnVal;
    }

    public boolean startCatalogServer() {
        Process process = null;
        boolean rtnVal = false;

        try {
            String catalogServerPort = conf.getStringProperty(StaticValues.CONFIG_KEY_CATALOG_SERVER_PORT);

            if(catalogServerPort == null || catalogServerPort.isEmpty()) {
                catalogServerPort = "8999";
                conf.setProperty(StaticValues.CONFIG_KEY_CATALOG_SERVER_PORT, catalogServerPort);
            }

            List<String> processArgs = new ArrayList<String>();
            processArgs.add("catalog_server");
            processArgs.add("-p");
            processArgs.add(catalogServerPort);

            ProcessBuilder pb = this.buildProcessBuilder(null, processArgs, null);
            process = pb.start();
            processes.add(process);

            rtnVal = true;
        } catch (Exception e) {
            e.printStackTrace();
            if(processes != null && processes.contains(process)) {
                processes.remove(process);
            }
            if(process != null) {
                process.destroy();
            }
        } finally {
        }
        return rtnVal;
    }


    public boolean startBash(String taskFileName, String taskDirectory) {
        boolean rtnVal = false;
        Process process = null;
        try {
            File currFile = new File(ToolPool.buildFilePath(taskDirectory,taskFileName));
            if (currFile.exists() || currFile.canRead()) {

                List<String> processArgs = new ArrayList<String>();
                processArgs.add("bash");
                processArgs.add(currFile.getPath());

                ProcessBuilder pb = this.buildProcessBuilder(taskDirectory, processArgs, null);
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
            if(processes != null && processes.contains(process)) {
                processes.remove(process);
            }
            if(process != null) {
                process.destroy();
            }
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

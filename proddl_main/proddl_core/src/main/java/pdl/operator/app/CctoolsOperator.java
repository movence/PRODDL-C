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
public class CctoolsOperator {
    private static final String GENERIC_TASK_NAME = "proddl";

    private Configuration conf = null;

    private List<Process> processes;

    private String storagePath = null;
    private String cctoolsPath = null;
    private boolean useCatalogServer = false;

    private String catalogServerAddress = "127.0.0.1";
    private String catalogServerPort;

    public CctoolsOperator() {
        this(null);
    }

    public CctoolsOperator(String storagePath) {
        this.init(storagePath);
    }

    public void init(String storagePath) {
        try {
            conf = Configuration.getInstance();

            if(storagePath == null) {
                this.storagePath = conf.getStringProperty(StaticValues.CONFIG_KEY_STORAGE_PATH);
            } else {
                this.storagePath = storagePath;
            }

            if(conf.hasProperty(StaticValues.CONFIG_KEY_CCTOOLS_PATH)) {
                this.cctoolsPath = conf.getStringProperty(StaticValues.CONFIG_KEY_CCTOOLS_PATH) + "bin";
            }

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

    public ProcessBuilder buildProcessBuilder(String taskDir, List<String> args) {
        ProcessBuilder pb = new ProcessBuilder(args);
        if(taskDir != null) {
            pb.directory(new File(taskDir));
        }
        pb.redirectErrorStream(true);

        Map<String, String> env = pb.environment();
        boolean isPathSet = false;

        if(cctoolsPath != null) {
            for (String key : env.keySet()) {
                if (key.toLowerCase().equals("path")) {
                    env.put(key, cctoolsPath + File.pathSeparator + env.get(key));
                    isPathSet = true;
                    break;
                }
            }

            if(!isPathSet) {
                env.put("PATH", cctoolsPath);
            }
        }

        return pb;
    }

    public boolean startMakeflow(boolean isClean, String taskName, String taskFileName, String taskDirectory) {
        boolean rtnVal = false;
        Process process = null;

        try {
            //this.isCatalogServerInfoAvailable(); - catalog server information are already available by ServiceOperatorHelper

            if (taskFileName != null && !taskFileName.isEmpty() && taskDirectory != null && !taskDirectory.isEmpty()) {
                if (ToolPool.isDirectoryExist(taskDirectory)) {
                    File currFile = new File(ToolPool.buildFilePath(taskDirectory,taskFileName));
                    if (currFile.exists() || currFile.canRead()) {

                        List<String> processArgs = new ArrayList<String>();
                        processArgs.add("bash");
                        processArgs.add("-c");
                        processArgs.add("makeflow");

                        if(isClean)
                            processArgs.add("-c"); //clean up log files and all targets
                        else {
                            if(conf.hasProperty(StaticValues.CONFIG_KEY_MAKEFLOW_ARGS)) {
                                String makeflowArgs = conf.getStringProperty(StaticValues.CONFIG_KEY_MAKEFLOW_ARGS);
                                String[] makeflowArgsArr = makeflowArgs.split(" ");
                                processArgs.addAll(Arrays.asList(makeflowArgsArr));
                            }

                            if(this.useCatalogServer) {
                                processArgs.add("-C");
                                processArgs.add(catalogServerAddress + ":" + catalogServerPort);
                                processArgs.add("-a"); //advertise to catalog server
                            }

                            //add task name
                            processArgs.add("-N");
                            processArgs.add(GENERIC_TASK_NAME);
                        }
                        processArgs.add(currFile.getPath());

                        ProcessBuilder pb = this.buildProcessBuilder(taskDirectory, processArgs);
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

    public boolean startCatalogServer() {
        Process process = null;
        boolean rtnVal = false;

        try {
            this.catalogServerPort = conf.getStringProperty(StaticValues.CONFIG_KEY_CATALOG_SERVER_PORT);

            List<String> processArgs = new ArrayList<String>();
            processArgs.add("bash");
            processArgs.add("-c");
            processArgs.add("catalog_server");

            if(this.catalogServerPort != null && !this.catalogServerPort.isEmpty()) {
                processArgs.add("-p");
                processArgs.add(this.catalogServerPort);
            }

            ProcessBuilder pb = this.buildProcessBuilder(null, processArgs);
            process = pb.start();
            processes.add(process);

            this.useCatalogServer = true;
            rtnVal = true;
        } catch (Exception e) {
            e.printStackTrace();
            if(processes!=null && processes.contains(process))
                processes.remove(process);

            if(process!=null) {
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

                ProcessBuilder pb = this.buildProcessBuilder(taskDirectory, processArgs);
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

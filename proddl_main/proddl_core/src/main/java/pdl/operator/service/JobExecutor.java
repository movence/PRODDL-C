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

package pdl.operator.service;

import org.apache.commons.io.FileUtils;
import pdl.cloud.model.FileInfo;
import pdl.cloud.model.JobDetail;
import pdl.common.Configuration;
import pdl.common.FileTool;
import pdl.common.StaticValues;
import pdl.common.ToolPool;
import pdl.operator.app.CctoolsOperator;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/27/12
 * Time: 10:19 AM
 */
public class JobExecutor extends Thread {
    private CctoolsOperator cctoolsOperator;
    private JobDetail currJob;

    public JobExecutor(ThreadGroup group, JobDetail currJob, CctoolsOperator operator) {
        super(group, currJob.getJobUUID() + "_job");
        this.currJob=currJob;
        this.cctoolsOperator = operator;
    }

    public JobExecutor(JobDetail currJob, CctoolsOperator operator) {
        this(new ThreadGroup("worker"), currJob, operator);
    }

    @Override
    public String toString() {
        /**
         * This method overrides toString() in order to provide job ID to RejectedJobExecutorHandler
         * in case this thread gets rejected by Asynchronous Queue
         */
        return currJob.getJobUUID();
    }

    public void run() {
        if (currJob != null && currJob.getJobUUID() != null) {
            boolean jobExecuted = executeJob();
            if (!jobExecuted) {
                System.err.printf("Job Execution Failed for UUID: '%s'%n", this.toString());
            }
        }
    }

    private boolean executeJob() {
        boolean rtnVal = false;

        try {
            if (currJob != null) {
                String jobID = currJob.getJobUUID();

                System.out.printf(
                        "Job Executor('%s') is processing a job - UUID: '%s' JobName: '%s'%n",
                        Thread.currentThread().getName(), jobID, currJob.getJobName()
                );

                String workDirPath = createJobDirectoryIfNotExist(jobID);
                if (workDirPath != null) {
                    if(currJob.getInput()!=null)
                        this.createInputJsonFile(workDirPath);

                    String jobName = currJob.getJobName();
                    if(jobName.equals(StaticValues.SPECIAL_EXECUTION_JOB))
                        this.genericJobExecution(workDirPath);
                    else
                        //executes universal job script then waits until its execution finishes
                        //TODO This part should be replaced to python execution code
                        this.tempExecuteJob(workDirPath);
                    rtnVal = true;
                } else
                    throw new Exception("Creating task area failed!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return rtnVal;
    }

    private String createJobDirectoryIfNotExist(String jobUUID) throws Exception {
        String jobDir = null;
        String storagePath = Configuration.getInstance().getStringProperty(StaticValues.CONFIG_KEY_DATASTORE_PATH);
        boolean jobAreaExist = false;

        File jobArea = new File(ToolPool.buildFilePath(storagePath + StaticValues.DIRECTORY_TASK_AREA));
        if (!ToolPool.isDirectoryExist(jobArea.getPath()))
            jobAreaExist = jobArea.mkdir();
        else
            jobAreaExist = true;

        if (jobAreaExist) {
            File currJobDirectory = new File(ToolPool.buildFilePath(jobArea.getPath(),jobUUID));
            if (!ToolPool.isDirectoryExist(currJobDirectory.getPath())) {
                if (currJobDirectory.mkdir())
                    jobDir = currJobDirectory.getPath();
                else
                    throw new Exception(String.format("Job Executor: Failed to create job(%s) directory", jobUUID));
            } else
                jobDir = currJobDirectory.getPath();
        } else
            throw new Exception(String.format("Job Executor: There is no storage area at '%s'", jobArea.getPath()));

        return ToolPool.buildFilePath(jobDir, null);
    }

    private boolean createInputJsonFile(String workDir) throws Exception {
        boolean rtnVal;

        try {
            String jsonInputPath = ToolPool.buildFilePath(workDir, StaticValues.FILE_JOB_INPUT);
            FileUtils.writeStringToFile(new File(jsonInputPath), currJob.getInput());

            rtnVal = ToolPool.canReadFile(jsonInputPath);
        } catch(Exception ex) {
            throw ex;
        }

        return rtnVal;
    }

    private void genericJobExecution(String workDir) throws Exception {
        JobHandler jobHandler = new JobHandler();

        try {
            if(currJob.getInput()!=null) {
                Map<String, Object> inputInMap = ToolPool.jsonStringToMap(currJob.getInput());
                String interpreter = null;

                Object mapObject = inputInMap.get("interpreter");
                if(mapObject!=null)
                    interpreter = (String)mapObject;

                mapObject = inputInMap.get("script");
                if(mapObject!=null) {
                    String scriptID = (String)mapObject;
                    if(scriptID.trim().isEmpty())
                        throw new Exception("script is not given!");
                    currJob.setScriptFileUUID(scriptID);
                }
                mapObject = inputInMap.get("input");
                if(mapObject!=null) {
                    String inputID = (String)mapObject;
                    if(!inputID.trim().isEmpty())
                        currJob.setInputFileUUID(inputID);
                }

                String fileExtension = interpreter==null||interpreter.isEmpty()?"exe":"sh";
                String scriptFile = this.validateJobFiles(workDir, fileExtension);

                jobHandler.updateJobStatus(currJob.getJobUUID(), StaticValues.JOB_STATUS_RUNNING, null, null);

                boolean executed = false;
                if(interpreter!=null) {
                    if(interpreter.equals("makeflow"))
                        executed = cctoolsOperator.startMakeflow(false, currJob.getJobUUID(), scriptFile, workDir);
                    else if(interpreter.equals("bash"))
                        executed = cctoolsOperator.startBash(scriptFile, workDir);
                } else {

                }

                boolean outputUploaded = false;
                if(executed) {
                    FileTool fileTool = new FileTool();
                    //task output
                    String outputFileId = null;
                    String outputFilePath = ToolPool.buildFilePath(workDir, "output" + StaticValues.FILE_EXTENSION);
                    if(ToolPool.canReadFile(outputFilePath))
                        outputFileId = fileTool.createFile(null, new FileInputStream(outputFilePath), currJob.getUserId());
                    //log file
                    String logFilePath = ToolPool.buildFilePath(workDir, "final.log");
                    String logFileId = null;
                    if(ToolPool.canReadFile(logFilePath))
                        logFileId = fileTool.createFile(null, new FileInputStream(ToolPool.buildFilePath(workDir, "final.log")), currJob.getUserId());

                    //if(outputFileId!=null && !outputFileId.isEmpty())
                    jobHandler.updateJobStatus(currJob.getJobUUID(), StaticValues.JOB_STATUS_COMPLETED, outputFileId, logFileId);
                } else { //error while executing makeflow
                    throw new Exception("Job Execution Failed");
                }
            } else
                throw new Exception("input data is empty.");
        } catch (Exception ex) {
            jobHandler.updateJobStatus(currJob.getJobUUID(), StaticValues.JOB_STATUS_FAILED, null, null);
            ex.printStackTrace();
        }
    }

    /*
    *Temporary methods for big test
    */
    //TODO remove this test methods
    private void tempExecuteJob(String workDir) {
        //update job status and result information
        JobHandler jobHandler = new JobHandler();

        try {
            String mfFile = null;

            if(currJob.getInput()!=null) {
                Map<String, Object> inputInMap = ToolPool.jsonStringToMap(currJob.getInput());

                if(currJob.getInputFileUUID()==null || currJob.getScriptFileUUID()==null) {
                    String scriptID = (String)inputInMap.get("script");
                    if(scriptID==null || scriptID.isEmpty())
                        throw new Exception("script is not given!");
                    currJob.setScriptFileUUID(scriptID);

                    String inputID = (String)inputInMap.get("input");
                    if(inputID!=null && inputID.isEmpty())
                        currJob.setInputFileUUID(inputID);
                }

                mfFile = validateJobFiles(workDir, "sh");
            }
            jobHandler.updateJobStatus(currJob.getJobUUID(), StaticValues.JOB_STATUS_RUNNING, null, null);

            boolean executed = cctoolsOperator.startMakeflow(false, currJob.getJobUUID(), mfFile, workDir);
            if(executed) {
                String outputFilePath = ToolPool.buildFilePath(workDir, "output" + StaticValues.FILE_EXTENSION);

                FileTool fileTool = new FileTool();
                String outputFileId = fileTool.createFile(null, new FileInputStream(outputFilePath), currJob.getUserId());
                //log file
                String logFileId = null;
                String logFilePath = ToolPool.buildFilePath(workDir, "final.log");
                if(ToolPool.canReadFile(logFilePath))
                    logFileId = fileTool.createFile(null, new FileInputStream(ToolPool.buildFilePath(workDir, "final.log")), currJob.getUserId());

                //if(outputFileId!=null && !outputFileId.isEmpty())
                jobHandler.updateJobStatus(currJob.getJobUUID(), StaticValues.JOB_STATUS_COMPLETED, outputFileId, logFileId);
            } else { //error while executing makeflow
                throw new Exception("Job Execution Failed");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            jobHandler.updateJobStatus(currJob.getJobUUID(), StaticValues.JOB_STATUS_FAILED, null, null);
        }
    }
    private String validateJobFiles(String workingDirectory, String fileExt) throws Exception {
        FileTool fileTool = new FileTool();
        String scriptFileName = "script." + fileExt;

        if(currJob.getScriptFileUUID()!=null) {
            FileInfo scriptFile = fileTool.getFileInfoById(currJob.getScriptFileUUID());
            if(scriptFile!=null) {
                if(!fileTool.copyFromDatastore(
                        ToolPool.buildFilePath(scriptFile.getPath(), scriptFile.getName()),
                        ToolPool.buildFilePath(workingDirectory, scriptFileName)))
                    throw new Exception("Copying script file failed.");
            } else {
                throw new Exception("script file record does not exit - " + currJob.getScriptFileUUID());
            }
        }

        if(currJob.getInputFileUUID()!=null) {
            FileInfo inputFile = fileTool.getFileInfoById(currJob.getInputFileUUID());
            if(inputFile!=null) {
                if(!fileTool.copyFromDatastore(
                        ToolPool.buildFilePath(inputFile.getPath(), inputFile.getName()),
                        ToolPool.buildFilePath(workingDirectory, "input" + StaticValues.FILE_EXTENSION)))
                    throw new Exception("Copying input file failed.");
            } else {
                throw new Exception("Input file record does not exit - " + currJob.getInputFileUUID());
            }
        }

        return scriptFileName;
    }
}

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
import pdl.cloud.management.CertificateManager;
import pdl.cloud.management.CloudInstanceManager;
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
 * Main job executor class that parses input data and processes job
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/27/12
 * Time: 10:19 AM
 */
public class JobExecutor extends Thread {
    private final String WORKER_INSTANCE_COUNT_KEY = "n_worker";  //for scale
    private final String CER_CERTIFICATE_KEY = "cer_fid"; // file UUID for cer
    private final String PFX_CERTIFICATE_KEY = "pfx_fid"; // file UUID for pfx
    private final String CERTIFICATE_PASSWORD_KEY = "c_password"; //certification password
    private CctoolsOperator cctoolsOperator;
    private JobDetail currJob;
    private JobHandler jobHandler;

    public JobExecutor(ThreadGroup group, JobDetail currJob, CctoolsOperator operator) {
        super(group, currJob.getJobUUID() + "_job");
        this.currJob=currJob;
        this.cctoolsOperator = operator;
        this.jobHandler = new JobHandler();
    }

    public JobExecutor(JobDetail currJob, CctoolsOperator operator) {
        this(new ThreadGroup("worker"), currJob, operator);
    }

    /**
     * returns id of current job so that the status can be updated in thread pool in case of failure
     * @return current job id
     */
    @Override
    public String toString() {
        /**
         * This method overrides toString() in order to provide job ID to RejectedJobExecutorHandler
         * in case this thread gets rejected by Asynchronous Queue
         */
        return currJob.getJobUUID();
    }

    /**
     * thread run
     */
    public void run() {
        if (currJob != null && currJob.getJobUUID() != null) {
            boolean jobExecuted = executeJob();
            if (!jobExecuted) {
                System.err.printf("Job Execution Failed for UUID: '%s'%n", this.toString());
            }
        }
    }

    /**
     * executes job according to its jobName with parameters(input)
     * @return boolean value of job execution
     */
    private boolean executeJob() {
        boolean rtnVal = false;

        try {
            if (currJob != null) {
                String jobID = currJob.getJobUUID();
                String jobName = currJob.getJobName();

                System.out.printf(
                        "Job Executor('%s') is processing a job - UUID: '%s' JobName: '%s'%n",
                        Thread.currentThread().getName(), jobID, jobName
                );

                if(jobName.equals("scale")) { //if(jobName.equals("scaleup") || jobName.equals("scaledown")) {
                    rtnVal = this.executeScaleJob();
                } else if(jobName.equals("cert")) {
                    rtnVal = this.executeCertificateJob();
                } else {
                    String workDirPath = createJobDirectoryIfNotExist(jobID);
                    if (workDirPath != null) {
                        if(currJob.getInput()!=null)
                            this.createInputJsonFile(workDirPath);

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
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return rtnVal;
    }

    /**
     * creates a directory for current job
     * @param jobUUID current job id
     * @return path to job directory
     * @throws Exception
     */
    private String createJobDirectoryIfNotExist(String jobUUID) throws Exception {
        String jobDir = null;
        String storagePath = Configuration.getInstance().getStringProperty(StaticValues.CONFIG_KEY_DATASTORE_PATH);
        boolean jobAreaExist = false;

        File jobArea = new File(ToolPool.buildDirPath(storagePath + StaticValues.DIRECTORY_TASK_AREA));
        if (!ToolPool.isDirectoryExist(jobArea.getPath()))
            jobAreaExist = jobArea.mkdir();
        else
            jobAreaExist = true;

        if (jobAreaExist) {
            File currJobDirectory = new File(ToolPool.buildDirPath(jobArea.getPath(),jobUUID));
            if (!ToolPool.isDirectoryExist(currJobDirectory.getPath())) {
                if (currJobDirectory.mkdir())
                    jobDir = currJobDirectory.getPath();
                else
                    throw new Exception(String.format("Job Executor: Failed to create job(%s) directory", jobUUID));
            } else
                jobDir = currJobDirectory.getPath();
        } else
            throw new Exception(String.format("Job Executor: There is no storage area at '%s'", jobArea.getPath()));

        return jobDir;
    }

    /**
     * writes input data to .json file in job directory
     * @param workDir job directory path
     * @return boolean value for creating json file
     * @throws Exception
     */
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

    /**
     * executes scale job - administrator only
     * @return boolean value for executing scale job
     * @throws Exception
     */
    private boolean executeScaleJob() throws Exception {
        boolean rtnVal = false;
        Map<String, Object> inputInMap = null;
        int workerCount = -1;

        if(currJob.getInput()!=null) {
            inputInMap = ToolPool.jsonStringToMap(currJob.getInput());
            if(inputInMap.containsKey(WORKER_INSTANCE_COUNT_KEY)) {
                String strCount = (String)inputInMap.get(WORKER_INSTANCE_COUNT_KEY);
                if(!strCount.equals("0"))
                    workerCount = Integer.parseInt(strCount);
            }
        }

        if(workerCount>0) {
            CloudInstanceManager instanceManager = new CloudInstanceManager();
            rtnVal = instanceManager.scaleService(workerCount);
        } else {
            rtnVal = false;
        }

        jobHandler.updateJobStatus(currJob.getJobUUID(), rtnVal?StaticValues.JOB_STATUS_COMPLETED:StaticValues.JOB_STATUS_FAILED);
        return rtnVal;
    }

    /**
     * executes certificate('cert') job - administrator only
     * @return boolean value for executing certificate job
     * @throws Exception
     */
    private boolean executeCertificateJob() throws Exception {
        boolean rtnVal = false;
        Map<String, Object> inputInMap = null;

        if(currJob.getInput()!=null) {
            inputInMap = ToolPool.jsonStringToMap(currJob.getInput());
            if(inputInMap.containsKey(CER_CERTIFICATE_KEY) && inputInMap.containsKey(PFX_CERTIFICATE_KEY) && inputInMap.containsKey(CERTIFICATE_PASSWORD_KEY)) {
                String cerFileId = (String)inputInMap.get(CER_CERTIFICATE_KEY);
                String pfxFileId = (String)inputInMap.get(PFX_CERTIFICATE_KEY);
                String certPass = (String)inputInMap.get(CERTIFICATE_PASSWORD_KEY);

                if(cerFileId!=null && pfxFileId!=null && certPass!=null) {
                    CertificateManager certManager = new CertificateManager();
                    rtnVal = certManager.execute(pfxFileId, cerFileId, certPass);
                }
            }
        }
        jobHandler.updateJobStatus(currJob.getJobUUID(), rtnVal?StaticValues.JOB_STATUS_COMPLETED:StaticValues.JOB_STATUS_FAILED);
        return rtnVal;
    }

    /**
     * executes general job('execute')
     * @param workDir current job directory path
     * @throws Exception
     */
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
                    String outputFilePath = ToolPool.buildFilePath(workDir, "output" + StaticValues.FILE_DAT_EXTENSION);
                    if(ToolPool.canReadFile(outputFilePath))
                        outputFileId = fileTool.createFile(null, new FileInputStream(outputFilePath), null, currJob.getUserId());
                    //log file
                    String logFilePath = ToolPool.buildFilePath(workDir, "final.log");
                    String logFileId = null;
                    if(ToolPool.canReadFile(logFilePath))
                        logFileId = fileTool.createFile(null, new FileInputStream(ToolPool.buildFilePath(workDir, "final.log")), null, currJob.getUserId());

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
                String outputFilePath = ToolPool.buildFilePath(workDir, "output" + StaticValues.FILE_DAT_EXTENSION);

                FileTool fileTool = new FileTool();
                String outputFileId = fileTool.createFile(null, new FileInputStream(outputFilePath), null, currJob.getUserId());
                //log file
                String logFileId = null;
                String logFilePath = ToolPool.buildFilePath(workDir, "final.log");
                if(ToolPool.canReadFile(logFilePath))
                    logFileId = fileTool.createFile(null, new FileInputStream(ToolPool.buildFilePath(workDir, "final.log")), null, currJob.getUserId());

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
                        ToolPool.buildFilePath(workingDirectory, "input" + StaticValues.FILE_DAT_EXTENSION)))
                    throw new Exception("Copying input file failed.");
            } else {
                throw new Exception("Input file record does not exit - " + currJob.getInputFileUUID());
            }
        }

        return scriptFileName;
    }
}

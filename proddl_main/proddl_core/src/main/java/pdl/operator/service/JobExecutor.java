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

    public void run() {
        try {
            if (currJob != null && currJob.getJobUUID() != null) {
                boolean jobExecuted = executeJob();
                if (!jobExecuted) {
                    throw new Exception(String.format("Job Execution Failed for UUID: '%s'%n", this.toString()));
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private boolean executeJob() throws Exception {
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
                    //executes universal job script then waits until its execution finishes
                    //TODO This part should be replaced to python execution code
                    this.tempExecuteJob(workDirPath);
                    rtnVal = true;
                } else
                    throw new Exception("Creating task area failed!");
            }
        } catch (Exception ex) {
            throw ex;
        }

        return rtnVal;
    }

    private String createJobDirectoryIfNotExist(String jobUUID) throws Exception {
        String jobDir = null;
        String storagePath = Configuration.getInstance().getStringProperty("STORAGE_PATH");
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

    private String validateJobFiles(String workingDirectory) throws Exception {
        FileTool fileTool = new FileTool();
        String makefileName = "make.makeflow";

        if(currJob.getMakeflowFileUUID()!=null) {
            FileInfo makeFile = fileTool.getFileInfoById(currJob.getMakeflowFileUUID());
            if(makeFile!=null) {
                String currMakeFilePath = ToolPool.buildFilePath(makeFile.getPath(), makeFile.getName());
                String newMakefilePath = ToolPool.buildFilePath(workingDirectory, makefileName);
                if(!fileTool.copyFromDatastore(currMakeFilePath, newMakefilePath))
                    throw new Exception("Copying Makefile failed.");
            } else {
                throw new Exception("Makefile does not exit in files table - " + currJob.getMakeflowFileUUID());
            }
        }

        if(currJob.getInputFileUUID()!=null) {
            FileInfo inputFile = fileTool.getFileInfoById(currJob.getInputFileUUID());
            if(inputFile!=null) {
                String currInputFilePath = ToolPool.buildFilePath(inputFile.getPath(), inputFile.getName());
                String newInputfilePath = ToolPool.buildFilePath(workingDirectory, "input" + StaticValues.FILE_EXTENSION);
                if(!fileTool.copyFromDatastore(currInputFilePath, newInputfilePath))
                    throw new Exception("Copying input file failed.");
            } else {
                throw new Exception("Input file does not exit in files table - " + currJob.getInputFileUUID());
            }
        }

        return makefileName;
    }

    @Override
    public String toString() {
        /**
         * This method overrides toString() in order to provide job ID to RejectedJobExecutorHandler
         * in case this thread gets rejected by Asynchronous Queue
         */
        return currJob.getJobUUID();
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

                if(currJob.getInputFileUUID()==null || currJob.getMakeflowFileUUID()==null) {
                    String makeflowFileID = (String)inputInMap.get("mfile");
                    String inputFileID = (String)inputInMap.get("ifile");
                    if(makeflowFileID==null || makeflowFileID.isEmpty() || inputFileID==null || inputFileID.isEmpty()) {
                        throw new Exception("makeflow or input file is NULL!");
                    }
                    currJob.setInputFileUUID(inputFileID);
                    currJob.setMakeflowFileUUID(makeflowFileID);
                }

                mfFile = validateJobFiles(workDir);
            }
            jobHandler.updateJobStatus(currJob.getJobUUID(), StaticValues.JOB_STATUS_RUNNING, null);

            boolean executed = cctoolsOperator.startMakeflow(currJob.getJobUUID(), mfFile, workDir);
            boolean outputUploaded = false;
            if(executed) {
                String outputFilePath = ToolPool.buildFilePath(workDir, "output" + StaticValues.FILE_EXTENSION);

                FileTool fileTool = new FileTool();
                String outputFileId = fileTool.createFile(null, new FileInputStream(outputFilePath),currJob.getUserId());

                if(outputFileId!=null && !outputFileId.isEmpty())
                    jobHandler.updateJobStatus(currJob.getJobUUID(), StaticValues.JOB_STATUS_COMPLETED, outputFileId);
            } else { //error while executing makeflow
                throw new Exception("Job Execution Failed");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            jobHandler.updateJobStatus(currJob.getJobUUID(), StaticValues.JOB_STATUS_FAILED, null);
        }
    }

    private String createMakeflowFile() throws Exception {
        String mfFilePath = null;
        return mfFilePath;
    }
}

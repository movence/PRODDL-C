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

import pdl.common.Configuration;
import pdl.common.StaticValues;
import pdl.common.ToolPool;
import pdl.operator.app.CctoolsOperator;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/27/12
 * Time: 10:19 AM
 */
public class JobExecutor extends Thread {
    private CctoolsOperator cctoolsOperator;
    private pdl.cloud.model.JobDetail currJob;

    public JobExecutor(ThreadGroup group, pdl.cloud.model.JobDetail currJob, CctoolsOperator operator) {
        super(group, currJob.getJobUUID() + "_job");
        this.cctoolsOperator = operator;
    }

    public JobExecutor(pdl.cloud.model.JobDetail currJob, CctoolsOperator operator) {
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
                    String fileName = validateJobFiles(workDirPath);
                    //executes universal job script then waits its execution finishes
                    cctoolsOperator.startMakeflow(jobID, fileName, workDirPath);
                }
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

        File jobArea = new File(storagePath + File.separator + StaticValues.DIRECTORY_TASK_AREA);
        if (!ToolPool.isDirectoryExist(jobArea.getPath()))
            jobAreaExist = jobArea.mkdir();
        else
            jobAreaExist = true;

        if (jobAreaExist) {
            File currJobDirectory = new File(jobArea.getPath() + File.separatorChar + jobUUID);
            if (!ToolPool.isDirectoryExist(currJobDirectory.getPath())) {
                if (currJobDirectory.mkdir())
                    jobDir = currJobDirectory.getPath();
                else
                    throw new Exception(String.format("Job Executor: Failed to create job(%s) directory", jobUUID));
            }
        } else
            throw new Exception(String.format("Job Executor: There is no storage area at '%s'", jobArea.getPath()));

        return jobDir;
    }

    private String validateJobFiles(String workingDirectory) throws Exception {
        pdl.cloud.StorageServices storageServices = new pdl.cloud.StorageServices();

        String inputFileName = storageServices.getFileNameById(currJob.getInputFileUUID());
        String inputFilePath = workingDirectory + File.separator + inputFileName;
        if (!ToolPool.canReadFile(inputFilePath))
            storageServices.downloadJobFileByName(inputFileName, inputFilePath);

        String makeflowFileName = storageServices.getFileNameById(currJob.getMakeflowFileUUID());
        String makeflowFilePath = workingDirectory + File.separator + makeflowFileName;
        if (!ToolPool.canReadFile(makeflowFilePath))
            storageServices.downloadJobFileByName(makeflowFileName, makeflowFilePath);

        return makeflowFileName;
    }

    @Override
    public String toString() {
        /**
         * This method overrides toString() in order to provide job ID to RejectedJobExecutorHandler
         * in case this thread gets rejected by Asynchronous Queue
         */
        return currJob.getJobUUID();
    }
}

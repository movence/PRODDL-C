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

package pdl.cloud.management;

import org.soyatec.windowsazure.table.ITableServiceEntity;
import pdl.cloud.model.JobDetail;
import pdl.cloud.storage.TableOperator;
import pdl.common.Configuration;
import pdl.common.QueryTool;
import pdl.common.StaticValues;
import pdl.common.ToolPool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 11/7/11
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobManager {

    private Configuration conf;
    private TableOperator tableOperator;
    private String jobDetailTableName;

    public JobManager() {
        conf = Configuration.getInstance();
        tableOperator = new TableOperator(conf);
        jobDetailTableName = ToolPool.buildTableName(StaticValues.TABLE_NAME_JOB_DETAIL);
    }

    /**
     * Prioritise jobs by its status and high-level sorting mechanism that has not been implemented at this time
     *
     * @throws Exception
     */
    private synchronized void reorderSubmittedJobs() throws Exception {
        try {
            List<ITableServiceEntity> jobs = getJobList(
                    QueryTool.getSingleConditionalStatement(StaticValues.COLUMN_JOB_DETAIL_STATUS, "eq", StaticValues.JOB_STATUS_SUBMITTED
                )
            );

            if(jobs!=null && jobs.size()>0) {
                ArrayList<JobDetail> prioritisedJobList = new ArrayList<JobDetail>();
                int scaleJobCount = 0;
                for (ITableServiceEntity job : jobs) {

                    JobDetail currJob = (JobDetail) job;
                    if(currJob.getJobName().contains("scale")) //set highest priority for scaling jobs
                        prioritisedJobList.add(scaleJobCount++, currJob);
                    else
                        prioritisedJobList.add(currJob);

                    //TODO needs more sophisticated reordering mechanism

                    //simply adds a job if priority list is empty
                    /*if (prioritisedJobList.size() == 0) {
                        prioritisedJobList.add(currJob);
                    } else {
                        //appends pending jobs without changing their orders
                        if (currJob.getStatus() == StaticValues.JOB_STATUS_SUBMITTED) {
                            int i;
                            for (i = 0; i < prioritisedJobList.size(); i++) {

                                JobDetail currentJob = prioritisedJobList.get(i);
                                if (currentJob.getStatus() == StaticValues.JOB_STATUS_SUBMITTED) {
                                    continue;
                                } else {
                                    break;
                                }
                            }
                            prioritisedJobList.add(i, currJob);
                        } else if (currJob.getStatus() == StaticValues.JOB_STATUS_RUNNING) {
                            prioritisedJobList.add(currJob);
                        }
                    }*/
                }

                for (int curr = 0; curr < prioritisedJobList.size(); curr++) {
                    JobDetail currentJob = prioritisedJobList.get(curr);
                    currentJob.setPriority(curr + 1);
                    //currentJob.setStatus(StaticValues.JOB_STATUS_SUBMITTED);
                }

                tableOperator.updateMultipleEntities(jobDetailTableName, prioritisedJobList);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * insert job information to job table
     *
     * @param jobDetail
     * @return boolean
     * @throws Exception
     */
    public boolean submitJob(JobDetail jobDetail) throws Exception {
        boolean rtnVal = false;

        try {
            jobDetail.setStatus(StaticValues.JOB_STATUS_SUBMITTED);
            jobDetail.setPriority(0);
            rtnVal = tableOperator.insertSingleEntity(jobDetailTableName, jobDetail);
            if(rtnVal)
                this.reorderSubmittedJobs();
            else
                throw new Exception("Adding job to Azure table failed.");

        } catch (Exception ex) {
            throw ex;
        }

        return rtnVal;
    }

    /**
     * Retrieves job by its UUID
     *
     * @param jobId
     * @return JobDetail corresponding to given jobUUID
     * @throws Exception
     */
    public JobDetail getJobByID(String jobId) throws Exception {
        JobDetail job = null;

        try {
            ITableServiceEntity retrievedJob = null;

            if (jobId != null && !jobId.isEmpty()) {
                retrievedJob = tableOperator.queryEntityBySearchKey(
                        jobDetailTableName,
                        StaticValues.COLUMN_ROW_KEY,
                        jobId,
                        JobDetail.class
                );
            }

            if (retrievedJob != null && retrievedJob.getClass() == JobDetail.class)
                job = (JobDetail) retrievedJob;
            else
                throw new Exception(String.format("Job (ID:'%s') does not exist.", jobId));

        } catch (Exception ex) {
            throw ex;
        }

        return job;
    }

    /**
     * Retrieves JobDetail Object by condition -
     * condition1: "submitted", condition2: priority == 1
     *
     * @return JobDetail
     * @throws Exception
     */
    public synchronized JobDetail getSingleSubmittedJob() throws Exception {
        JobDetail job = null;

        try {
            ITableServiceEntity retrievedJob = null;

            //gets job which has the highest priority
            retrievedJob = tableOperator.queryEntityByCondition(
                    jobDetailTableName,
                    QueryTool.mergeConditions(
                            QueryTool.getSingleConditionalStatement(
                                    StaticValues.COLUMN_JOB_DETAIL_STATUS,
                                    "eq",
                                    StaticValues.JOB_STATUS_SUBMITTED
                            ),
                            "and",
                            QueryTool.getSingleConditionalStatement(
                                    StaticValues.COLUMN_JOB_DETAIL_PRIORITY,
                                    "eq",
                                    1
                            )
                    ),
                    JobDetail.class
            );

            //if no job is found in previous step, grab a submitted job to run
            if (retrievedJob == null) {
                retrievedJob = tableOperator.queryEntityBySearchKey(
                        jobDetailTableName,
                        StaticValues.COLUMN_JOB_DETAIL_STATUS,
                        StaticValues.JOB_STATUS_SUBMITTED,
                        JobDetail.class
                );
            }

            if (retrievedJob != null && retrievedJob.getClass() == JobDetail.class) {
                job = (JobDetail) retrievedJob;
                this.updateJobStatus(job.getJobUUID(), StaticValues.JOB_STATUS_PENDING, null, null);
                this.reorderSubmittedJobs();
            }

        } catch (Exception ex) {
            throw ex;
        }

        return job;
    }

    /**
     * Updates job status of a job with given UUID (String) and status (Integer)
     *
     * @param jobId  UUID of a job
     * @param status integer value of job status (defined in StaticValues object)
     * @param resultFileName uid of result output file
     * @return boolean
     * @throws Exception
     */
    public boolean updateJobStatus(String jobId, int status, String resultFileName, String logFile) throws Exception {
        boolean rtnVal = false;

        try {
            JobDetail entity = getJobByID(jobId);

            if (entity != null && entity.getStatus() != status) {
                entity.setStatus(status);
                if (status != StaticValues.JOB_STATUS_SUBMITTED)
                    entity.setPriority(0);

                if(resultFileName!=null && !resultFileName.isEmpty())
                    entity.setResult(resultFileName);
                if(logFile!=null && !logFile.isEmpty())
                    entity.setLog(logFile);

                rtnVal = tableOperator.updateSingleEntity(jobDetailTableName, entity);
            }
        } catch (Exception ex) {
            throw ex;
        }

        return rtnVal;
    }

    public void updateMultipleJobStatus(int prevStatus, int newStatus) throws Exception {
        try {
            List<ITableServiceEntity> jobList = getJobList(QueryTool.getSingleConditionalStatement(StaticValues.COLUMN_JOB_DETAIL_STATUS, "eq", prevStatus));

            if(jobList!=null && jobList.size()>0) {
                for (ITableServiceEntity entity : jobList) {
                    JobDetail currJob = (JobDetail) entity;
                    updateJobStatus(currJob.getJobUUID(), newStatus, null, null);
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    public boolean updateWorkDirectory(String jobId, String path) throws Exception {
        boolean rtnVal = false;

        try {
            JobDetail entity = getJobByID(jobId);

            if (entity != null) {
                entity.setJobDirectory(path);
                rtnVal = tableOperator.updateSingleEntity(jobDetailTableName, entity);
            }

        } catch (Exception ex) {
            throw ex;
        }

        return rtnVal;
    }

    public List<ITableServiceEntity> getJobList(String condition) throws Exception {
        List<ITableServiceEntity> jobs;
        try {
            jobs = tableOperator.queryListByCondition(
                    jobDetailTableName,
                    condition,
                    JobDetail.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return jobs;
    }
}

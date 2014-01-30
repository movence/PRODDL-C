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

package pdl.cloud.management;

import pdl.cloud.model.JobDetail;
import pdl.cloud.storage.TableOperator;
import pdl.utils.QueryTool;
import pdl.utils.StaticValues;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 11/7/11
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobManager {
    private TableOperator tableOperator;
    private String jobDetailTableName;

    public JobManager() {
        tableOperator = TableOperator.getInstance(null);
        jobDetailTableName = StaticValues.TABLE_NAME_JOB_DETAIL;
    }

    /**
     * Prioritise jobs by its status and high-level sorting mechanism that has not been implemented at this time
     *
     * @throws Exception
     */
    private synchronized void reorderSubmittedJobs() throws Exception {
        try {
            List<JobDetail> jobs = getJobList(
                    QueryTool.getSingleConditionalStatement(StaticValues.COLUMN_JOB_DETAIL_STATUS, "=", StaticValues.JOB_STATUS_SUBMITTED)
            );

            if(jobs!=null && jobs.size()>0) {
                ArrayList<JobDetail> prioritisedJobList = new ArrayList<JobDetail>();
                int scaleJobCount = 0;
                for (JobDetail currJob : jobs) {
                    if(currJob.getJobName().contains("scale")) { //set highest priority for scaling jobs
                        prioritisedJobList.add(scaleJobCount++, currJob);
                    } else {
                        prioritisedJobList.add(currJob);
                    }

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
     * @param jobDetail job information
     * @return boolean boolean of job submission result
     * @throws Exception
     */
    public boolean submitJob(JobDetail jobDetail) throws Exception {
        boolean rtnVal = false;

        try {
            jobDetail.setStatus(StaticValues.JOB_STATUS_SUBMITTED);
            jobDetail.setPriority(0);
            rtnVal = tableOperator.insertEntity(jobDetailTableName, jobDetail);
            if(rtnVal)
                this.reorderSubmittedJobs();
            else
                throw new Exception("failed to add job.");

        } catch (Exception ex) {
            throw ex;
        }

        return rtnVal;
    }

    /**
     * Retrieves job by its uid
     *
     * @param jobId job uid
     * @return JobDetail job information
     * @throws Exception
     */
    public JobDetail getJobByID(String jobId) throws Exception {
        JobDetail job = null;

        try {
            Map<String, String> retrievedJob = null;

            if (jobId != null && !jobId.isEmpty()) {
                retrievedJob = tableOperator.queryEntityBySearchKey(jobDetailTableName, StaticValues.COLUMN_ROW_KEY, jobId);
            }

            if (retrievedJob != null) {
                job = new JobDetail();
                job.map(retrievedJob);
            } else {
                throw new Exception(String.format("job - '%s' not found", jobId));
            }

        } catch (Exception ex) {
            throw ex;
        }

        return job;
    }

    /**
     * Retrieves JobDetail Object by condition -
     * condition1: "submitted", condition2: priority == 1
     *
     * @return JobDetail job information
     * @throws Exception
     */
    public synchronized JobDetail getSingleSubmittedJob() throws Exception {
        JobDetail job = null;

        try {
            StringBuilder where = new StringBuilder();

            //gets job which has the highest priority
            where.append(
                    QueryTool.getSingleConditionalStatement(StaticValues.COLUMN_JOB_DETAIL_STATUS, "=", StaticValues.JOB_STATUS_SUBMITTED)
            );
            where.append(" and ");
            where.append(QueryTool.getSingleConditionalStatement(StaticValues.COLUMN_JOB_DETAIL_PRIORITY, "=", 1));

            Map<String, String> jobMap = tableOperator.queryEntity(jobDetailTableName, where.toString());
            if(jobMap != null) {
                job = new JobDetail();
                job.map(jobMap);
            }

            //if no job is found in previous step, grab a submitted job to run
            if (job == null) {
                jobMap = tableOperator.queryEntityBySearchKey(
                        jobDetailTableName,
                        StaticValues.COLUMN_JOB_DETAIL_STATUS,
                        StaticValues.JOB_STATUS_SUBMITTED
                );
                if(jobMap != null) {
                    job = new JobDetail();
                    job.map(jobMap);
                }
            }

            if (job != null && job.getClass() == JobDetail.class) {
                this.updateJobStatus(job.getUuid(), StaticValues.JOB_STATUS_PENDING);
                this.reorderSubmittedJobs();
            }

        } catch (Exception ex) {
            throw ex;
        }

        return job;
    }

    /**
     * Updates status of a job with given UUID (String) and status (Integer)
     *
     * @param jobId  job uid
     * @param status integer value of job status (defined in StaticValues object)
     * @param resultFileName uid of result output file
     * @return boolean boolean of status update result
     * @throws Exception
     */
    public boolean updateJob(String jobId, int status, String resultFileName, String logFile) throws Exception {
        boolean rtnVal = false;

        try {
            JobDetail job = this.getJobByID(jobId);

            if (job != null && job.getStatus() != status) {
                job.setStatus(status);
                if (status != StaticValues.JOB_STATUS_SUBMITTED)
                    job.setPriority(0);

                if(resultFileName!=null && !resultFileName.isEmpty())
                    job.setResult(resultFileName);
                if(logFile!=null && !logFile.isEmpty())
                    job.setLog(logFile);

                rtnVal = tableOperator.updateEntity(jobDetailTableName, job);
            }
        } catch (Exception ex) {
            throw ex;
        }

        return rtnVal;
    }

    public boolean updateJobStatus(String jobId, int status) {
        boolean rtnVal = false;
        try {
            rtnVal = this.updateJob(jobId, status, null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rtnVal;
    }

    /**
     * batch job status update
     * @param prevStatus integer of previous job status
     * @param newStatus integer of new job status
     * @throws Exception
     */
    public void updateMultipleJobStatus(int prevStatus, int newStatus) throws Exception {
        try {
            List<JobDetail> jobList = getJobList(
                    QueryTool.getSingleConditionalStatement(StaticValues.COLUMN_JOB_DETAIL_STATUS, "=", prevStatus)
            );

            if(jobList!=null && jobList.size()>0) {
                for (JobDetail currJob : jobList) {
                    //updateJobStatus(currJob.getJobUUID(), newStatus, null, null);
                    currJob.setStatus(newStatus);
                }
                tableOperator.updateMultipleEntities(jobDetailTableName, jobList);
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * update job table entity for working directory change
     * @param jobId job uid
     * @param path working directory for the job
     * @return boolean of update result
     * @throws Exception
     */
    public boolean updateWorkDirectory(String jobId, String path) throws Exception {
        boolean rtnVal = false;

        try {
            JobDetail entity = getJobByID(jobId);

            if (entity != null) {
                entity.setJobDirectory(path);
                rtnVal = tableOperator.updateEntity(jobDetailTableName, entity);
            }

        } catch (Exception ex) {
            throw ex;
        }

        return rtnVal;
    }

    /**
     * retrieves list of job that meets given sql condition
     * @param condition sql formatted (Azure style) condition - where clause
     * @return list of jobs
     * @throws Exception
     */
    public List<JobDetail> getJobList(String condition) throws Exception {
        List<JobDetail> jobs = null;
        try {
            List<Map<String, String>> list = tableOperator.queryList(jobDetailTableName, condition);
            if(list != null && list.size() > 0) {
                jobs = new ArrayList<JobDetail>();
                for(Map<String, String> entity : list) {
                    JobDetail job = new JobDetail();
                    job.map(entity);
                    jobs.add(job);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return jobs;
    }
}

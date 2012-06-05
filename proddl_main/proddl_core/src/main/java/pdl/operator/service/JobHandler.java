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

import pdl.cloud.management.JobManager;
import pdl.cloud.model.JobDetail;
import pdl.common.StaticValues;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/27/12
 * Time: 10:42 AM
 */
public class JobHandler {
    private JobManager jobManager;

    public JobHandler() {
        jobManager = new JobManager();
    }

    public synchronized JobDetail getSubmmittedJob() {
        JobDetail singleJob = null;

        try {
            singleJob = jobManager.getSingleSubmittedJob();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return singleJob;
    }

    public JobDetail getCurrentJob(String jobUUID) {
        JobDetail currJob = null;

        try {
            currJob = jobManager.getJobByID(jobUUID);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return currJob;
    }

    public boolean updateJobStatus(String jobUUID, int status, String resultFile, String logFile) {
        boolean rtnVal = false;
        try {
            rtnVal = jobManager.updateJobStatus(jobUUID, status, resultFile, logFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rtnVal;
    }

    public boolean updateWorkDirectory(String jobUUID, String path) {
        boolean rtnVal = false;

        try {
            rtnVal = jobManager.updateWorkDirectory(jobUUID, path);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return rtnVal;
    }

    public void rollbackAllRunningJobStatus() {
        try {
            jobManager.updateMultipleJobStatus(StaticValues.JOB_STATUS_PENDING, StaticValues.JOB_STATUS_SUBMITTED);
            jobManager.updateMultipleJobStatus(StaticValues.JOB_STATUS_RUNNING, StaticValues.JOB_STATUS_SUBMITTED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

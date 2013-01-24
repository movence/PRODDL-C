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
import pdl.common.StaticValues;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 2/2/12
 * Time: 3:21 PM
 */
public class RejectedJobExecutorHandler implements RejectedExecutionHandler {

    /**
     * update Job status of rejected Jobs
     *
     * @param runnable Job Executor thread
     * @param executor Thread executor service
     */
    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
        JobManager jobManager = new JobManager();
        jobManager.updateJobStatus(runnable.toString(), StaticValues.JOB_STATUS_SUBMITTED);
    }
}

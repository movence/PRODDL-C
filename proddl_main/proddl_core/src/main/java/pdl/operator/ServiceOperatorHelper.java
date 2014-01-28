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

package pdl.operator;

import pdl.cloud.management.JobManager;
import pdl.cloud.model.JobDetail;
import pdl.operator.app.JettyThreadedOperator;
import pdl.operator.service.JobExecutor;
import pdl.operator.service.JobExecutorThreadPool;
import pdl.operator.service.RejectedJobExecutorHandler;
import pdl.utils.Configuration;
import pdl.utils.StaticValues;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 12/13/11
 * Time: 2:31 PM
 */
public class ServiceOperatorHelper {

    private Configuration conf;

    public ServiceOperatorHelper() {
        conf = Configuration.getInstance();
    }

    /**
     * starts master or worker by flag
     */
    public void run() {
        try {
            this.runMaster();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * starts master
     *  -rolls back any pending or running job statues
     *  -starts catalog server
     *  -runs a job
     * @throws Exception
     */
    private void runMaster() throws Exception {
        String storagePath = conf.getStringProperty(StaticValues.CONFIG_KEY_STORAGE_PATH);
        String webServerPort = conf.getStringProperty(StaticValues.CONFIG_KEY_WEB_SERVER_PORT);
        //default web server port to 80
        if(webServerPort == null || webServerPort.isEmpty()) {
            webServerPort = "80";
        }

        JettyThreadedOperator jettyOperator = new JettyThreadedOperator(webServerPort, storagePath);
        jettyOperator.start();

        JobManager jobManager = new JobManager();

        //clear the "being processed" or "running" states from all jobs
        jobManager.updateMultipleJobStatus(StaticValues.JOB_STATUS_RUNNING, StaticValues.JOB_STATUS_SUBMITTED);
        jobManager.updateMultipleJobStatus(StaticValues.JOB_STATUS_PENDING, StaticValues.JOB_STATUS_SUBMITTED);

        //Job running threads pool
        final JobExecutorThreadPool threadExecutor = new JobExecutorThreadPool(
                StaticValues.CORE_NUMBER_JOB_EXECUTOR,
                StaticValues.MAX_NUMBER_JOB_EXECUTOR,
                StaticValues.MAX_KEEP_ALIVE_VALUE_JOB_EXECUTOR,
                StaticValues.MAX_KEEP_ALIVE_UNIT_JOB_EXECUTOR.equals("min") ? TimeUnit.MINUTES : TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                //new BlockingArrayQueue<Runnable>(5),
                new RejectedJobExecutorHandler()
        );

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                threadExecutor.shutdownNow();
            }
        });

        ThreadGroup threadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(), "worker");
        //checks available job indefinitely
        while (true) {
            JobDetail submittedJob = jobManager.getSingleSubmittedJob();
            if (submittedJob != null) {
                JobExecutor jobExecutor = new JobExecutor(threadGroup, submittedJob, null, jobManager);
                threadExecutor.execute(jobExecutor);
            }
        }
    }
}

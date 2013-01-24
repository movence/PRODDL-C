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

package pdl.operator;

import pdl.cloud.management.JobManager;
import pdl.cloud.model.JobDetail;
import pdl.common.Configuration;
import pdl.common.StaticValues;
import pdl.operator.app.CctoolsOperator;
import pdl.operator.app.JettyThreadedOperator;
import pdl.operator.app.ToolOperator;
import pdl.operator.service.*;

import java.io.File;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 12/13/11
 * Time: 2:31 PM
 */
public class ServiceOperatorHelper {

    private Configuration conf;
    private CctoolsOperator cctoolsOperator;

    private String storagePath;

    public ServiceOperatorHelper() {
        conf = Configuration.getInstance();
    }

    /**
     * starts master or worker by flag
     */
    public void run() {
        try {
            this.storagePath = conf.getStringProperty(StaticValues.CONFIG_KEY_STORAGE_PATH);

            this.runOperators();

            String isMaster = conf.getStringProperty(StaticValues.CONFIG_KEY_MASTER_INSTANCE);
            if (isMaster != null && isMaster.equals("true")) { //Master Instance
                String webServerPort = conf.getStringProperty(StaticValues.CONFIG_KEY_WEBSERVER_PORT);
                String internalAddress = conf.getStringProperty(StaticValues.CONFIG_KEY_INTERNAL_ADDR);
                String internalPort = conf.getStringProperty(StaticValues.CONFIG_KEY_INTERNAL_PORT);
                this.runMaster(webServerPort, internalAddress, internalPort);
            } else { //Job(WorkQ) runner
                this.runJobRunner();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * download 3rd party tools from blob storage area, then extracts into local storage space
     * @throws Exception
     */
    private void runOperators() throws Exception {
        ToolOperator pythonOperator = new ToolOperator(storagePath, "python", "python.exe", null);
        pythonOperator.run();

        ToolOperator cygwinOperator = new ToolOperator(storagePath, "cygwin", "cygwin.bat", null);
        cygwinOperator.run();

        cctoolsOperator = new CctoolsOperator(storagePath, "cctools-3.6.1", "bin" + File.separator + "makeflow.exe");
        cctoolsOperator.run();
    }

    /**
     * starts master
     *  -rolls back any pending or running job statues
     *  -starts catalog server
     *  -runs a job
     * @param jettyPort port number Jetty uses
     * @param masterAddress local IP address
     * @param catalogServerPort internal communication port between cctools instances
     * @throws Exception
     */
    private void runMaster(String jettyPort, String masterAddress, String catalogServerPort) throws Exception {
        JettyThreadedOperator jettyOperator = new JettyThreadedOperator(jettyPort);
        jettyOperator.start();

        JobManager jobManager = new JobManager();

        //clear the "being processed" or "running" states from all jobs
        jobManager.updateMultipleJobStatus(StaticValues.JOB_STATUS_RUNNING, StaticValues.JOB_STATUS_SUBMITTED);
        jobManager.updateMultipleJobStatus(StaticValues.JOB_STATUS_PENDING, StaticValues.JOB_STATUS_SUBMITTED);

        if (!cctoolsOperator.startCatalogServer(masterAddress, catalogServerPort)) {
            throw new Exception("starting catalog server failed - " + masterAddress + ":" + catalogServerPort);
        }

        //Adds processor time monitor to timer
        /*
        * Removed instance monitor for manual instance management through REST API
        * by hkim 6/26/12
        *
        ScheduledInstanceMonitor instanceMonitor = new ScheduledInstanceMonitor();
        Timer instanceMonitorTimer = new Timer();
        instanceMonitorTimer.scheduleAtFixedRate(instanceMonitor, StaticValue.WORKER_INSTANCE_MONITORING_INTERVAL, timeInterval);
        */

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
                JobExecutor jobExecutor = new JobExecutor(threadGroup, submittedJob, cctoolsOperator, jobManager);
                threadExecutor.execute(jobExecutor);
            }
        }
    }

    /**
     * starts worker
     * @throws Exception
     */
    private void runJobRunner() throws Exception {
        int maxWorkerCountPerNode = StaticValues.MAX_WORKER_INSTANCE_PER_NODE;

        //waits until master is available
        while (!cctoolsOperator.isCatalogServerInfoAvailable()) {
            Thread.sleep(10000);
        }

        cctoolsOperator.createTmpForWorker();

        final ThreadPoolExecutor workerPool = new ThreadPoolExecutor(
                maxWorkerCountPerNode,
                maxWorkerCountPerNode,
                StaticValues.MAX_KEEP_ALIVE_VALUE_JOB_EXECUTOR,
                StaticValues.MAX_KEEP_ALIVE_UNIT_JOB_EXECUTOR.equals("min") ? TimeUnit.MINUTES : TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                //new BlockingArrayQueue<Runnable>(5),
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
                        return;
                    }
                }
        );

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                workerPool.shutdownNow();
            }
        });

        for (int i = 0; i < maxWorkerCountPerNode; i++) {
            WorkerExecutor worker = new WorkerExecutor(cctoolsOperator);
            workerPool.execute(worker);
        }
    }
}

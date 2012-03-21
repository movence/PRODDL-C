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

import pdl.cloud.StorageServices;
import pdl.cloud.management.ScheduledInstanceMonitor;
import pdl.cloud.model.DynamicData;
import pdl.cloud.model.JobDetail;
import pdl.common.Configuration;
import pdl.common.StaticValues;
import pdl.operator.app.CctoolsOperator;
import pdl.operator.app.CygwinOperator;
import pdl.operator.app.JettyThreadedOperator;
import pdl.operator.app.PythonOperator;
import pdl.operator.service.JobExecutor;
import pdl.operator.service.JobExecutorThreadPool;
import pdl.operator.service.JobHandler;
import pdl.operator.service.RejectedJobExecutorHandler;

import java.io.File;
import java.util.Timer;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 12/13/11
 * Time: 2:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServiceOperatorHelper {

    private Configuration conf;
    private StorageServices storageServices;

    private PythonOperator pythonOperator;
    private CygwinOperator cygwinOperator;
    private CctoolsOperator cctoolsOperator;

    private String storagePath;

    public ServiceOperatorHelper() {
        conf = Configuration.getInstance();
        storageServices = new StorageServices();
    }

    /**
     * @param isMaster
     * @param storagePath
     * @param masterAddress
     * @param catalogServerPort
     * @param jettyPort
     */
    public void run(String isMaster, String storagePath, String masterAddress, String catalogServerPort, String jettyPort) {
        try {

            storagePath = storagePath.replace("/", File.separator);
            if (!storagePath.endsWith(File.separator))
                storagePath += File.separator;

            conf.setProperty("STORAGE_PATH", storagePath);
            this.storagePath = storagePath;

            DynamicData storageData = new DynamicData("storage_info");
            storageData.setDataKey(StaticValues.KEY_DYNAMIC_DATA_STORAGE_PATH);
            storageData.setDataValue(storagePath);
            storageServices.insertSingleEnttity(conf.getStringProperty("TABLE_NAME_DYNAMIC_DATA"), storageData);

            this.runOperators();

            if (isMaster.equals("true")) { //Master Instance
                this.runMaster(jettyPort, masterAddress, catalogServerPort);
            } else { //Job(WorkQ) runner
                this.runJobRunner();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void runOperators() throws Exception {
        pythonOperator = new PythonOperator(
                storagePath,
                conf.getStringProperty("PYTHON_NAME"),
                conf.getStringProperty("PYTHON_FLAG_FILE"),
                null
        );
        pythonOperator.run(storageServices);

        cygwinOperator = new CygwinOperator(
                storagePath,
                conf.getStringProperty("CYGWIN_NAME"),
                conf.getStringProperty("CYGWIN_FLAG_FILE"),
                null
        );
        cygwinOperator.run(storageServices);

        cctoolsOperator = new CctoolsOperator(
                storagePath,
                conf.getStringProperty("CCTOOLS_NAME"),
                "bin" + File.separator + conf.getProperty("CCTOOLS_FLAG_FILE"),
                null
        );
        cctoolsOperator.run(storageServices);
    }

    private void runMaster(String jettyPort, String masterAddress, String catalogServerPort) throws Exception {
        /*JettyOperator jettyOperator = new JettyOperator( storagePath, prop.getProperty( "JETTY_NAME" ) );
        if( jettyOperator.download( blobOperator, prop.getProperty( "JETTY_FLAG_FILE" ) ) ) {
            jettyOperator.start( jettyPort );
        }*/
        JettyThreadedOperator jettyOperator = new JettyThreadedOperator(jettyPort);
        jettyOperator.start();

        JobHandler jobHandler = new JobHandler();
        //clear the "being processed" state from all jobs
        jobHandler.rollbackAllRunningJobStatus();

        String tempCatalogServerAddress = cctoolsOperator.getCatalogServerAddress();
        if (tempCatalogServerAddress == null) {
            if (!cctoolsOperator.startCatalogServer(masterAddress, catalogServerPort)) {
                throw new Exception("Failed to start Catalog Server at " + masterAddress + ":" + catalogServerPort);
            }
        }

        //Adds processor time monitor to timer
        int timeInterval = 180000;
        ScheduledInstanceMonitor instanceMonitor = new ScheduledInstanceMonitor();
        Timer instanceMonitorTimer = new Timer();
        instanceMonitorTimer.scheduleAtFixedRate(instanceMonitor, timeInterval, timeInterval);

        //Job running threads pool
        final JobExecutorThreadPool threadExecutor = new JobExecutorThreadPool(
                conf.getIntegerProperty("CORE_NUMBER_JOB_EXECUTOR"),
                conf.getIntegerProperty("MAX_NUMBER_JOB_EXECUTOR"),
                conf.getIntegerProperty("MAX_KEEP_ALIVE_VALUE_JOB_EXECUTOR"),
                conf.getStringProperty("MAX_KEEP_ALIVE_UNIT_JOB_EXECUTOR").equals("min") ? TimeUnit.MINUTES : TimeUnit.SECONDS,
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
            JobDetail submittedJob = jobHandler.getSubmmittedJob();
            if (submittedJob != null) {
                JobExecutor jobExecutor = new JobExecutor(threadGroup, submittedJob, cctoolsOperator);
                threadExecutor.execute(jobExecutor);
            }
        }
    }

    private void runJobRunner() throws Exception {
        while (true) {

            while (cctoolsOperator.getCatalogServerAddress() == null) {
                Thread.sleep(10000);
            }

            cctoolsOperator.startWorkQ();
        }
    }
}

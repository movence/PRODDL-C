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
import pdl.cloud.model.JobDetail;
import pdl.common.Configuration;
import pdl.common.StaticValues;
import pdl.operator.app.*;
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
    private StorageServices storageServices;
    private CctoolsOperator cctoolsOperator;

    private String storagePath;

    public ServiceOperatorHelper() {
        conf = Configuration.getInstance();
        storageServices = new StorageServices();
    }

    public void run() {
        try {
            this.storagePath = conf.getStringProperty(StaticValues.CONFIG_KEY_STORAGE_PATH);

            this.runOperators();

            String isMaster = conf.getStringProperty(StaticValues.CONFIG_KEY_MASTER_INSTANCE);
            if (isMaster != null && isMaster.equals("true")) { //Master Instance
                /*
                 * Storage and datastore information are supplied with properties file by bootstrap
                 * by hkim 9/6/2012
                 *
                String dynamicTable = ToolPool.buildTableName(StaticValues.TABLE_NAME_DYNAMIC_DATA);

                //remove odl storage path data in dynamic table
                ITableServiceEntity oldPath = storageServices.queryEntityBySearchKey(
                        dynamicTable,
                        StaticValues.COLUMN_DYNAMIC_DATA_KEY,
                        StaticValues.CONFIG_KEY_STORAGE_PATH,
                        DynamicData.class);
                if (oldPath != null) {
                    storageServices.deleteEntity(dynamicTable, oldPath);
                }

                DynamicData storageData = new DynamicData("storage_info");
                storageData.setDataKey(StaticValues.CONFIG_KEY_STORAGE_PATH);
                storageData.setDataValue(storagePath);
                storageServices.insertSingleEnttity(dynamicTable, storageData);

                //file storage space, provided by c# bootstrap application
                String datastorePath = conf.getStringProperty(StaticValues.CONFIG_KEY_DATASTORE_PATH);
                if (datastorePath == null) {
                    storageData = (DynamicData) storageServices.queryEntityBySearchKey(
                            dynamicTable,
                            StaticValues.COLUMN_DYNAMIC_DATA_KEY, StaticValues.KEY_DYNAMIC_DATA_DRIVE_PATH,
                            DynamicData.class);

                    if (storageData == null)
                        datastorePath = storagePath;
                    else
                        datastorePath = storageData.getDataValue();

                    conf.setProperty(StaticValues.CONFIG_KEY_DATASTORE_PATH, ToolPool.buildFilePath(datastorePath, null));
                }
                */
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

    private void runOperators() throws Exception {
         //TODO need more dynamic way to handle 3rd party package
        AbstractApplicationOperator pythonOperator = new PythonOperator(storagePath, "python", "python.exe");
        pythonOperator.run(storageServices);

        AbstractApplicationOperator cygwinOperator = new CygwinOperator(storagePath, "cygwin", "cygwin.bat");
        cygwinOperator.run(storageServices);

        cctoolsOperator = new CctoolsOperator(storagePath, "cctools-3.5.1", "bin" + File.separator + "makeflow.exe");
        cctoolsOperator.run(storageServices);
    }

    private void runMaster(String jettyPort, String masterAddress, String catalogServerPort) throws Exception {
        JettyThreadedOperator jettyOperator = new JettyThreadedOperator(jettyPort);
        jettyOperator.start();

        JobHandler jobHandler = new JobHandler();
        //clear the "being processed" state from all jobs
        jobHandler.rollbackAllRunningJobStatus();

        if (!cctoolsOperator.startCatalogServer(masterAddress, catalogServerPort)) {
            throw new Exception("Failed to start Catalog Server at " + masterAddress + ":" + catalogServerPort);
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
            JobDetail submittedJob = jobHandler.getSubmmittedJob();
            if (submittedJob != null) {
                JobExecutor jobExecutor = new JobExecutor(threadGroup, submittedJob, cctoolsOperator);
                threadExecutor.execute(jobExecutor);
            }
        }
    }

    private void runJobRunner() throws Exception {
        int maxWorkerCount = StaticValues.MAX_WORKER_INSTANCE_PER_NODE;

        while (!cctoolsOperator.isCatalogServerInfoAvailable()) {
            Thread.sleep(10000);
        }

        final ThreadPoolExecutor workerPool = new ThreadPoolExecutor(
                maxWorkerCount,
                maxWorkerCount,
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

        for (int i = 0; i < maxWorkerCount; i++) {
            WorkerExecutor worker = new WorkerExecutor(cctoolsOperator);
            workerPool.execute(worker);
        }

        /*final List<WorkerExecutor> workers = Collections.synchronizedList(new ArrayList<WorkerExecutor>(maxWorkerCount));
        ArrayList<WorkerExecutor> completedWorkers = new ArrayList<WorkerExecutor>();
        boolean allWorkersAlive;
        while (true) {
            allWorkersAlive = true;
            if(workers.size() == maxWorkerCount) {
                for(int i=0;i<maxWorkerCount;i++) {
                    WorkerExecutor aWorker = workers.get(i);
                    if(!aWorker.isAlive()) {
                        allWorkersAlive = false;
                        completedWorkers.add(aWorker);
                    }
                }

                if(allWorkersAlive)
                    Thread.sleep(conf.getIntegerProperty("MAX_KEEP_ALIVE_VALUE_JOB_EXECUTOR") * 60 * 1000);
                else {
                    workers.removeAll(completedWorkers);
                    completedWorkers.clear();
                }
            }

            if(workers.size() < maxWorkerCount) {
                WorkerExecutor worker = new WorkerExecutor(cctoolsOperator);
                workers.add(worker);
                worker.start();
                //threadExecutor.execute(worker);
            }
        }*/
    }
}

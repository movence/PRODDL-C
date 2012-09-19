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

package pdl.common;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 7/20/11
 * Time: 1:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class StaticValues {
    //AZURE
    public static final String AZURE_BLOB_HOST_NAME = "http://blob.core.windows.net";
    public static final String AZURE_QUEUE_HOST_NAME = "http://queue.core.windows.net";
    public static final String AZURE_TABLE_HOST_NAME = "http://table.core.windows.net";

    //PROPERTIES
    public static final String CONFIG_FILENAME = "proddl.properties";
    public static final String CONFIG_KEY_SUBSCRIPTION_ID = "SubscriptionId";
    public static final String CONFIG_KEY_DEPLOYMENT_ID = "DeploymentId";
    public static final String CONFIG_KEY_DEPLOYMENT_NAME = "DeploymentName";
    public static final String CONFIG_KEY_WORKER_NAME = "CloudRoleWorkerName";

    public static final String CONFIG_KEY_CERTIFICATE_NAME = "CertificateName";
    public static final String CONFIG_KEY_CERT_PASSWORD = "CertificatePassword";
    public static final String CONFIG_KEY_CERT_ALIAS = "CertificateAlias";

    public static final String CONFIG_KEY_DATASTORE_PATH = "DataStorePath";
    public static final String CONFIG_KEY_STORAGE_PATH = "StoragePath";
    public static final String CONFIG_KEY_CSTORAGE_NAME = "StorageAccountName";
    public static final String CONFIG_KEY_CSTORAGE_PKEY = "StorageAccountPkey";
    public static final String CONFIG_KEY_MASTER_INSTANCE = "IsMaster";
    public static final String CONFIG_KEY_WEBSERVER_PORT = "WebserverPort";
    public static final String CONFIG_KEY_INTERNAL_ADDR = "InternalAddress";
    public static final String CONFIG_KEY_INTERNAL_PORT = "InternalPort";

    //STORAGE
    public static final String BLOB_CONTAINER_TOOLS = "tools";
    public static final String TABLE_NAME_JOB_DETAIL = "jobDetail";
    public static final String TABLE_NAME_DYNAMIC_DATA = "dynamicData";
    public static final String TABLE_NAME_FILES = "files";
    public static final String TABLE_NAME_USER = "user";

    public static final String COLUMN_ROW_KEY = "RowKey";
    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_DYNAMIC_DATA_KEY = "dataKey";
    public static final String COLUMN_JOB_DETAIL_PRIORITY = "priority";
    public static final String COLUMN_JOB_DETAIL_STATUS = "status";

    public static final String KEY_DYNAMIC_DATA_DRIVE_PATH="MasterDrivePath";

    public static final String DIRECTORY_TASK_AREA = "task";
    public static final String DIRECTORY_FILE_AREA = "datastore";

    //JOB STATUS
    public static final int JOB_STATUS_SUBMITTED = 1;
    public static final int JOB_STATUS_PENDING = 2;
    public static final int JOB_STATUS_RUNNING = 3;
    public static final int JOB_STATUS_COMPLETED = 4;
    public static final int JOB_STATUS_FAILED = 5;

    //FILE
    public static final int FILE_STATUS_RESERVED = 0;
    public static final int FILE_STATUS_COMMITTED = 1;
    public static final String FILE_ZIP_EXTENTION = ".zip";
    public static final String FILE_DAT_EXTENSION = ".dat";
    public static final String FILE_JOB_INPUT = "input.json";
    public static final int MAX_FILE_COUNT_PER_DIRECTORY = 500;

    //JOB EXECUTION
    public static final String[] ADMIN_ONLY_JOBS =
        {
            "execute",
            "adduser",
            "scaleup",
            "scaledown",
            "cert"
        };
    public static final String SPECIAL_EXECUTION_JOB = "execute";
    public static final int CORE_NUMBER_JOB_EXECUTOR = 1;
    public static final int MAX_NUMBER_JOB_EXECUTOR = 20;
    public static final String MAX_KEEP_ALIVE_UNIT_JOB_EXECUTOR = "min"; //Available units: min, sec
    public static final int MAX_KEEP_ALIVE_VALUE_JOB_EXECUTOR = 1;

    //CLOUD INSTANCE MANAGEMENT
    public static final int MAX_TOTAL_WORKER_INSTANCE = 96;
    public static final int MAX_WORKER_INSTANCE_PER_NODE = 1;
    public static final int WORKER_INSTANCE_MONITORING_INTERVAL = 3600000; //time interval for monitoring CPU usage on Worker instance in milliseconds
    public static final int MAXIMUM_AVERAGE_CPU_USAGE = 50;
    public static final int MINIMUM_AVERAGE_CPU_USAGE = 15;

    public static final String CERTIFICATE_NAME = "management";
    public static final String CERTIFICATE_ALIAS = "keyalias";

    //ERROR CODE
}

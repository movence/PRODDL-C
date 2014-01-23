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

package pdl.utils;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 7/20/11
 * Time: 1:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class StaticValues {

    //PROPERTIES
    public static final String CONFIG_FILENAME = "proddl.ini";
    public static final String CONFIG_KEY_STORAGE_PATH = "storage_path";
    public static final String CONFIG_KEY_WEB_SERVER_PORT = "web_server_port";
    public static final String CONFIG_KEY_START_CATALOG_SERVER = "start_wq_catalog_server";
    public static final String CONFIG_KEY_CATALOG_SERVER_PORT = "wq_catalog_server_port";
    public static final String CONFIG_KEY_START_WORKER_POOL = "start_wq_worker_pool";
    public static final String CONFIG_KEY_WORKER_POOL = "worker_pool";
    public static final String CONFIG_KEY_MAKEFLOW_ARGS = "makeflow_args";

    //STORAGE
    public static final String TABLE_NAME_JOB_DETAIL = "jobs";
    public static final String TABLE_NAME_INFOS = "infos";
    public static final String TABLE_NAME_FILES = "files";
    public static final String TABLE_NAME_USER = "users";

    public static final String COLUMN_ROW_KEY = "uuid";
    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_INFOS_KEY = "key";
    public static final String COLUMN_JOB_DETAIL_PRIORITY = "priority";
    public static final String COLUMN_JOB_DETAIL_STATUS = "status";

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
    public static final String FILE_EXTENSION_ZIP = ".zip";
    public static final String FILE_EXTENSION_DAT = ".dat";
    public static final String FILE_JOB_INPUT = "input.json";
    public static final int MAX_FILE_COUNT_PER_DIRECTORY = 500;

    //JOB EXECUTION
    public static final String SPECIAL_EXECUTION_JOB = "execute";
    public static final String[] ADMIN_ONLY_JOBS = {
        SPECIAL_EXECUTION_JOB,
        "adduser",
        "scale",
        "cert"
    };
    public static final int CORE_NUMBER_JOB_EXECUTOR = 1;
    public static final int MAX_NUMBER_JOB_EXECUTOR = 20;
    public static final String MAX_KEEP_ALIVE_UNIT_JOB_EXECUTOR = "min"; //Available units: min, sec
    public static final int MAX_KEEP_ALIVE_VALUE_JOB_EXECUTOR = 1;

    //ERROR CODE
}

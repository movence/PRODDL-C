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
    public static final String COLUMN_ROW_KEY = "RowKey";
    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_DYNAMIC_DATA_KEY = "dataKey";
    public static final String COLUMN_JOB_DETAIL_PRIORITY = "priority";
    public static final String COLUMN_JOB_DETAIL_STATUS = "status";
    public static final String COLUMN_FILE_INFO_NAME = "name";
    public static final String COLUMN_FILE_INFO_SUUID = "suuid";

    public static final String KEY_DYNAMIC_DATA_STORAGE_PATH="StoragePath";

    public static final String DIRECTORY_TASK_AREA = "task";
    public static final String DIRECTORY_FILE_UPLOAD_AREA = "uploads";

    public static final int JOB_STATUS_SUBMITTED = 1;
    public static final int JOB_STATUS_PENDING = 2;
    public static final int JOB_STATUS_RUNNING = 3;
    public static final int JOB_STATUS_COMPLETED = 4;
    public static final int JOB_STATUS_FAILED = 5;
}

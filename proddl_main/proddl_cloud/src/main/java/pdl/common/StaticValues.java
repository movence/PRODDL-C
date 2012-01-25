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
    public static final String CLOUD_ROLE_WORKER_NAME = "PRODDLJobRunner";

    public static final String TABLE_DYNAMIC_DATA_NAME = "dynamicData";
    public static final String COLUMN_DYNAMIC_DATA_NAME = "dataKey";

    public static final String TALBE_JOB_DETAIL_NAME = "jobDetail";

    public static final String QUEUE_JOBFILES_NAME = "jobfiles";
    public static final String QUEUE_JOBQUEUE_NAME = "jobqueue";

    public static final int JOB_STATUS_SUBMITTED = 0;
    public static final int JOB_STATUS_PENDING = 1;
    public static final int JOB_STATUS_RUNNING = 2;
    public static final int JOB_STATUS_FINISHED = 3;
    public static final int JOB_STATUS_FAILED = 4;
}

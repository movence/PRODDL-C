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

import pdl.cloud.model.JobDetail;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 3/26/12
 * Time: 11:18 AM
 *
 * This class runs jobs by
 * 1) parsing input json for files then download them to working directory
 * 2) running makeflow
 * 3) generating output file and uploading it to blob storage
 * This class is for big test and might not be used on production
 */
public class JobExecutorHelper {
    private String workDir;

    public JobExecutorHelper() {

    }

    public void runJob(String workDir) {
        JobHandler handler = new JobHandler();
        JobDetail job = handler.getSubmmittedJob();
        this.runJob(job, workDir);
    }

    public void runJob(JobDetail job, String workDir) {
    }
}

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

package pdl.services.management;

import org.soyatec.windowsazure.table.ITableServiceEntity;
import pdl.common.Configuration;
import pdl.common.StaticValues;
import pdl.services.model.JobDetail;
import pdl.services.storage.TableOperator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 11/7/11
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobProcessingManager {
    private Configuration conf;

    private TableOperator tableOperator;

    public JobProcessingManager() {
        try {
            conf = Configuration.getInstance();
            initializeTableOperator();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    public JobProcessingManager( Configuration conf ) {
        this.conf = conf;
        initializeTableOperator();
    }

    private void initializeTableOperator() {
        tableOperator = new TableOperator( conf );
    }

    private void reorderPendingJobs() throws Exception {
        try {
            List<ITableServiceEntity> jobs = tableOperator.queryListByCondition(
                    StaticValues.TALBE_JOB_DETAIL_NAME, "status ne " + StaticValues.JOB_STATUS_FINISHED, JobDetail.class );

            ArrayList<JobDetail> prioritisedJobList = new ArrayList<JobDetail>();
            for( ITableServiceEntity job : jobs ) {
                JobDetail singleJob = (JobDetail)job;

                if( prioritisedJobList.size() == 0 ) { //simply adds a job if priority list is empty
                    prioritisedJobList.add( singleJob );
                } else {
                    if( singleJob.getStatus() == StaticValues.JOB_STATUS_SUBMITTED ) { //appends pending jobs without changing their orders
                        int i;
                        for( i = 0; i < prioritisedJobList.size(); i++ ) {

                            JobDetail currentJob = prioritisedJobList.get( i );
                            if( currentJob.getStatus() == StaticValues.JOB_STATUS_SUBMITTED ) {
                                continue;
                            } else {
                                break;
                            }
                        }
                        prioritisedJobList.add( i, singleJob );
                    } else if( singleJob.getStatus() == StaticValues.JOB_STATUS_RUNNING ) {
                        prioritisedJobList.add( singleJob );
                    }
                }
            }

            for( int curr = 0; curr < prioritisedJobList.size(); curr++ ) {
                JobDetail currentJob = prioritisedJobList.get( curr );
                currentJob.setPriority( curr + 1 );
            }

            tableOperator.updateMultipleEntities( StaticValues.TALBE_JOB_DETAIL_NAME, prioritisedJobList );

        } catch ( Exception ex ) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public boolean addNewJob( JobDetail jobDetail ) throws Exception {
        boolean rtnVal = false;

        try {
            rtnVal = tableOperator.insertSingleEntity( StaticValues.TALBE_JOB_DETAIL_NAME, jobDetail );

            if( rtnVal )
                this.reorderPendingJobs();
            else
                throw new Exception( "Adding job to Azure table failed." );

        } catch ( Exception ex ) {
            throw ex;
        }

        return rtnVal;
    }

    public boolean updateJobStatus( String jobId, int status ) {
        boolean rtnVal = false;

        try {
           JobDetail entity = (JobDetail)tableOperator.queryEntityBySearchKey(
                   StaticValues.TALBE_JOB_DETAIL_NAME,
                   "RowKey",
                   jobId,
                   JobDetail.class );

            if( entity.getStatus() != status )
                entity.setStatus( status );


            rtnVal = tableOperator.updateSingleEntity( StaticValues.TALBE_JOB_DETAIL_NAME, entity );
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        return rtnVal;
    }
}

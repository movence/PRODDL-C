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

package pdl.web.service;

import org.codehaus.jackson.map.ObjectMapper;
import pdl.services.management.JobManager;
import pdl.services.model.JobDetail;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/17/12
 * Time: 2:43 PM
 */
public class JobReuqestHandler {

    public Map<String, Object> submitJob( String jobName, String inputInString, String userName ) {
        Map<String, Object> rtnJson = null;

        try {
            Map<String, Object> inputInMap = null;

            JobDetail jobDetail = new JobDetail( jobName );
            jobDetail.setJobName( jobName );
            jobDetail.setInput( inputInString );
            jobDetail.setUserId( userName );

            if( inputInString != null && inputInString.length() > 0 ) {
                ObjectMapper mapper = new ObjectMapper();
                inputInMap = mapper.readValue( inputInString, Map.class);
                for(Map.Entry<String, Object> entry : inputInMap.entrySet()) {
                    if( "inputFileId".equals( entry.getKey() ) )
                        jobDetail.setInputFileUUID((String)entry.getValue());
                    else if("makeFileId".equals(entry.getKey()))
                        jobDetail.setMakeflowFileUUID((String)entry.getValue());
                }
            }

            JobManager jobManager = new JobManager();
            jobManager.submitJob(jobDetail);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return rtnJson;
    }

    public String getJobInfo( String jobId ) {
        String rtnStr = null;

        try {
            rtnStr = "{\"t\":\"t\", \"s\":\"s\",\"w\":\"w\" }";
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return rtnStr;
    }

    public String getJobResult( String jobId ) {
        String rtnStr = null;

        try {
            rtnStr = this.getJobInfo( jobId );
            if( rtnStr != null ) {

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return rtnStr;
    }
}

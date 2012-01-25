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
import pdl.services.management.JobProcessingManager;
import pdl.services.model.JobDetail;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/17/12
 * Time: 2:43 PM
 */
public class JobReuqestHandler {

    public Map<String, Object> runJob( String jobName, String inputInString, String userName ) throws Exception {
        Map<String, Object> rtnJson = null;

        try {
            Map<String, Object> inputInMap = null;

            JobDetail jobDetail = new JobDetail( jobName );
            jobDetail.setJobName( jobName );
            jobDetail.setInput( inputInString );
            jobDetail.setUserId( userName );

            if( inputInString != null && inputInString.length() > 0 ) {
                ObjectMapper mapper = new ObjectMapper();
                inputInMap = mapper.readValue( inputInString, Map.class );
                for( String key : inputInMap.keySet() ) {
                    if( "fileId".equals( key ) )
                        jobDetail.setJobFileUUID( (String)inputInMap.get( key ) );
                }
            }

            JobProcessingManager jobManager = new JobProcessingManager();
            jobManager.addNewJob( jobDetail );

        } catch (Exception ex) {
            throw ex;
        }

        return rtnJson;
    }
}

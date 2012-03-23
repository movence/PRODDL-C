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
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.type.TypeReference;
import org.soyatec.windowsazure.table.ITableServiceEntity;
import pdl.cloud.management.JobManager;
import pdl.cloud.management.UserService;
import pdl.cloud.model.JobDetail;
import pdl.cloud.model.User;
import pdl.common.QueryTool;
import pdl.common.StaticValues;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/17/12
 * Time: 2:43 PM
 */
public class JobRequestHandler {
    JobManager manager;
    public JobRequestHandler() {
        manager = new JobManager();
    }

    public Map<String, Object> submitJob(String jobName, String inputInString, String userName) {
        Map<String, Object> rtnVal = new TreeMap<String, Object> ();

        try {
            String result;
            boolean succeed;

            Map<String, Object> inputInMap = null;
            ObjectMapper mapper = new ObjectMapper();
            if(inputInString!=null && !inputInString.isEmpty()) {
                TypeReference<TreeMap<String,Object>> typeRef = new TypeReference<TreeMap<String,Object>>() {};
                inputInMap = mapper.readValue(inputInString, typeRef);
            }

            //check user privilege for jobs
            UserService userService = new UserService();
            boolean isAdminUser = userService.isAdmin(userName);
            List<String> userAllowedJobs = new ArrayList<String>(Arrays.asList(StaticValues.USER_AVAILABLE_JOBS.split(",")));
            List<String> adminAllowedJobs = new ArrayList<String>(Arrays.asList(StaticValues.ADMIN_ONLY_JOBS.split(",")));
            adminAllowedJobs.addAll(userAllowedJobs);
            if(!userAllowedJobs.contains(jobName) || !(isAdminUser && adminAllowedJobs.contains(jobName))) {
                rtnVal.put("message", String.format("The Job('%s') is not available or you do not have permission.", jobName));
                throw new Exception("User requested a job that is not allowed to execute.");
            }

            //add user by admin
            if(jobName.equals("adduser")) {
                User user = new User();

                if(!inputInMap.containsKey("id") || !inputInMap.containsKey("password")) {
                    rtnVal.put("message", "'adduser' requires 'id' and 'password' in input data.");
                    throw new Exception();
                }

                String givenUserId = (String)inputInMap.get("id");
                //check for duplicated user id
                User dupUser = userService.getUserById(givenUserId);
                if(dupUser!=null) {
                    rtnVal.put("message", String.format("There is already existing user with given id '%s'", givenUserId));
                    throw new Exception();
                }

                user.setUserid(givenUserId);
                user.setUserpass((String)inputInMap.get("password"));
                user.setAdmin(inputInMap.containsKey("admin")?(Integer)inputInMap.get("admin"):0);

                user.setFirstName(inputInMap.containsKey("firstname")?(String)inputInMap.get("fristname"):null);
                user.setLastName(inputInMap.containsKey("lastname")?(String)inputInMap.get("lastname"):null);
                user.setEmail(inputInMap.containsKey("email")?(String)inputInMap.get("email"):null);
                user.setPhone(inputInMap.containsKey("phone")?(String)inputInMap.get("phone"):null);

                succeed = userService.loadUser(user);
                if(succeed) {
                    result = String.format("User '%s' has been added", user.getUserid());
                } else {
                    result = "Failed to add user.";
                }
            } else {
                JobDetail jobDetail = new JobDetail(jobName);
                jobDetail.setJobName(jobName);
                jobDetail.setUserId(userName);

                if (inputInMap!=null) {
                    inputInMap.put("jobName", jobName);
                    inputInMap.put("username", userName);

                    ObjectWriter writer = mapper.writer();
                    inputInString = writer.writeValueAsString(inputInMap);
                    jobDetail.setInput(inputInString);

                    for (Map.Entry<String, Object> entry : inputInMap.entrySet()) {
                        if ("inputFileId".equals(entry.getKey()))
                            jobDetail.setInputFileUUID((String) entry.getValue());
                        else if ("makeFileId".equals(entry.getKey()))
                            jobDetail.setMakeflowFileUUID((String) entry.getValue());
                    }
                }

                succeed = manager.submitJob(jobDetail);

                if(succeed) {
                    rtnVal.put("Job ID", jobDetail.getJobUUID());
                    rtnVal.put("Job Name", jobDetail.getJobName());
                    result = "Job submitted";
                } else {
                    result = "Failed to submit the job.";
                }
            }

            rtnVal.put("result", result);

        } catch (Exception ex) {
            ex.printStackTrace();
            rtnVal.put("error", "Failed to submit a job");
        }

        return rtnVal;
    }

    public Map<String , Object> getJobInfo(String jobId) {
        HashMap<String, Object> rtnVal = new HashMap<String, Object>();

        try {
            if(jobId==null || jobId.isEmpty())
                throw new Exception();

            JobDetail job = manager.getJobByID(jobId);

            Object jobInfo;
            if(job!=null) {
                Map<String, Object> jobInfoMap = new TreeMap<String, Object>();
                //jobInfoMap.put("Job ID", job.getJobUUID());
                jobInfoMap.put("Name", job.getJobName());
                jobInfoMap.put("Status", job.getStatusInString());
                jobInfoMap.put("User", job.getUserId());
                jobInfoMap.put("Input", job.getInput());

                if(job.getStatus()== StaticValues.JOB_STATUS_COMPLETED)
                    jobInfoMap.put("result", this.getJobResult(jobId));
                jobInfo = jobInfoMap;
            } else {
                jobInfo = String.format("There is no existing job with given ID(%s)", jobId);
            }
            rtnVal.put("Job Info", jobInfo);

        } catch (Exception ex) {
            ex.printStackTrace();
            rtnVal.put("error", String.format("Failed to get job information for ID(%s)", jobId));
        }
        return rtnVal;
    }

    public Map<String , String> getJobResult(String jobId) {
        HashMap<String, String> rtnVal = new HashMap<String, String>();

        try {
            JobDetail job = manager.getJobByID(jobId);

            String result="";
            if(job!=null) {
                if(job.getStatus()!=StaticValues.JOB_STATUS_COMPLETED)
                    result = String.format("Job has not been completed. Current Status is '%s'", job.getStatusInString());
                else
                    result = job.getResult();
            }
            rtnVal.put("Result", result);
        } catch (Exception ex) {
            ex.printStackTrace();
            rtnVal.put("error", String.format("Failed to get job result for ID(%s)", jobId));
        }

        return rtnVal;
    }

    public Map<String, Object> getJobsForUser(String userName) {
        Map<String, Object> rtnVal = new HashMap<String, Object>();
        try {
            List<String> jobIdWithStatus = new ArrayList<String>();

            List<ITableServiceEntity> jobs = manager.getJobList(QueryTool.getSingleConditionalStatement("userId", "eq", userName));
            if(jobs!=null && jobs.size()>0) {
                for(ITableServiceEntity entity : jobs) {
                    JobDetail job = (JobDetail)entity;
                    jobIdWithStatus.add(job.getJobUUID()+":"+job.getStatusInString());
                }
            }
            rtnVal.put("Jobs", jobIdWithStatus.isEmpty()?"There is no job for "+userName:jobIdWithStatus);

        } catch(Exception ex) {
            ex.printStackTrace();
            rtnVal.put("error", "Failed to retrieve Job list for " + userName);
        }

        return rtnVal;
    }
}

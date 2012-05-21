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

import org.soyatec.windowsazure.table.ITableServiceEntity;
import org.springframework.web.multipart.MultipartFile;
import pdl.cloud.management.CloudInstanceManager;
import pdl.cloud.management.JobManager;
import pdl.cloud.management.UserService;
import pdl.cloud.model.JobDetail;
import pdl.cloud.model.User;
import pdl.common.Configuration;
import pdl.common.QueryTool;
import pdl.common.StaticValues;
import pdl.common.ToolPool;
import pdl.web.service.common.FileService;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/17/12
 * Time: 2:43 PM
 */
public class JobRequestHandler {
    JobManager manager;
    UserService userService;
    FileService fileService;
    ArrayList<String> adminJobs;

    private static final String JOB_TYPE_SCALE_UP = "scaleup";

    public JobRequestHandler() {
        manager = new JobManager();
        userService = new UserService();
        fileService = new FileService();
        adminJobs = null;
    }

    public Map<String, Object> submitJob(String jobName, String inputInString, String userName) {
        Map<String, Object> rtnVal = new TreeMap<String, Object> ();
        String result = null;

        try {
            boolean succeed;
            UserService userService = new UserService();

            //check user privilege for jobs
            this.checkJobPrivilege(jobName, userName);

            if(jobName.equals("scaleup") || jobName.equals("scaledown")) {
                CloudInstanceManager instanceManager = new CloudInstanceManager();
                if(jobName.equals("scaleup"))
                    succeed = instanceManager.scaleUp();
                else
                    succeed = instanceManager.scaleDown();

                result = String.format("'%s' job has been %s.", jobName, succeed?"submitted":"failed");
            } else {
                Map<String, Object> inputInMap = ToolPool.jsonStringToMap(inputInString);

                //add user by admin
                if(jobName.equals("adduser")) {
                    succeed = this.addUser(inputInMap, userService);
                    if(succeed)
                        result = "User '%s' has been added.";
                    else
                        result = "Failed to add user.";
                } else if(jobName.equals("makeflow")) {

                } else {
                    JobDetail jobDetail = new JobDetail(jobName);
                    jobDetail.setJobName(jobName);
                    jobDetail.setUserId(userName);

                    if (inputInMap!=null) {
                        inputInMap.put("job", jobName);
                        inputInMap.put("username", userName);

                        jobDetail.setInput(ToolPool.jsonMapToString(inputInMap));
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
            }
            rtnVal.put("result", result);
        } catch (Exception ex) {
            rtnVal.put("message", result==null?ex.toString():result);
            rtnVal.put("error", "Failed to submit a job");
        }

        return rtnVal;
    }

    private boolean isUserAdmin(String userName) throws Exception {
        return userService.isAdmin(userName);
    }

    private void checkJobPrivilege(String jobName, String userName) throws Exception {
        if(adminJobs==null) {
            adminJobs = new ArrayList<String>(Arrays.asList(Configuration.getInstance().getStringProperty("ADMIN_ONLY_JOBS").split(",")));
        }

        boolean isAdminUser = this.isUserAdmin(userName);

        if(!isAdminUser && adminJobs.contains(jobName))
            throw new Exception(String.format("The Job('%s') is not available or you do not have permission.", jobName));

        /*List<String> userAllowedJobs = new ArrayList<String>(Arrays.asList(StaticValues.USER_AVAILABLE_JOBS.split(",")));
        List<String> adminAllowedJobs = new ArrayList<String>(Arrays.asList(StaticValues.ADMIN_ONLY_JOBS.split(",")));
        adminAllowedJobs.addAll(userAllowedJobs);
        if(!userAllowedJobs.contains(jobName) || !(isAdminUser && adminAllowedJobs.contains(jobName))) {
            rtnVal.put("message", String.format("The Job('%s') is not available or you do not have permission.", jobName));
            throw new Exception("User requested a job that is not allowed to execute.");
        }*/
    }

    private boolean addUser(Map<String, Object> inputInMap, UserService userService) throws Exception {
        User user = new User();

        if(!inputInMap.containsKey("id") || !inputInMap.containsKey("password"))
            throw new Exception("'adduser' requires 'id' and 'password' in input data.");

        String givenUserId = (String)inputInMap.get("id");
        //check for duplicated user id
        User dupUser = userService.getUserById(givenUserId);
        if(dupUser!=null)
            throw new Exception(String.format("There is already existing user with given id '%s'", givenUserId));

        user.setUserid(givenUserId);
        user.setUserpass((String)inputInMap.get("password"));
        user.setAdmin(inputInMap.containsKey("admin")?(Integer)inputInMap.get("admin"):0);

        user.setFirstName(inputInMap.containsKey("firstname")?(String)inputInMap.get("fristname"):null);
        user.setLastName(inputInMap.containsKey("lastname")?(String)inputInMap.get("lastname"):null);
        user.setEmail(inputInMap.containsKey("email")?(String)inputInMap.get("email"):null);
        user.setPhone(inputInMap.containsKey("phone")?(String)inputInMap.get("phone"):null);

        return userService.loadUser(user);
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
                jobInfoMap.put("name", job.getJobName());
                jobInfoMap.put("status", job.getStatusInString());
                jobInfoMap.put("user", job.getUserId());
                jobInfoMap.put("input", job.getInput());

                if(job.getStatus()== StaticValues.JOB_STATUS_COMPLETED) //get job result
                    jobInfoMap.put("result", job.getResult());

                jobInfo = jobInfoMap;
            } else {
                jobInfo = String.format("There is no existing job with given ID(%s)", jobId);
            }
            rtnVal.put("info", jobInfo);

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
            /*rtnVal.put("status", job.getStatusInString());*/

            String result="";
            if(job!=null) {
                switch(job.getStatus()) {
                    case StaticValues.JOB_STATUS_COMPLETED:
                        result = job.getResult();
                        break;
                    case StaticValues.JOB_STATUS_FAILED:
                        result = "Job Failed.";
                        break;
                    default:
                        result = String.format("Job has not been completed. Current Status is '%s'", job.getStatusInString());
                }
            }
            rtnVal.put("result", result);
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

    public Map<String, String> updateJob(String jobId, int status, String resultFileId, String userName) {
        Map<String, String> rtnVal = new HashMap<String, String>();
        try {
            if(!this.isUserAdmin(userName))
                throw new Exception("You need Admin privilege to update job status manually.");

            fileService.getFilePathById(resultFileId);

            boolean result = manager.updateJobStatus(jobId, status, resultFileId);
            rtnVal.put("result", ""+(result?1:0));

        } catch(Exception ex) {
            ex.printStackTrace();
            rtnVal.put("error", ex.toString());
        }

        return rtnVal;
    }


    /*
    *    File handlers
    */
    public Map<String, String> uploadFile(MultipartFile file, String type, String userName) {
        Map<String, String> rtnVal = new HashMap<String, String>();
        try {
            rtnVal = fileService.uploadFile(file, type, userName);
        } catch(Exception ex) {
            ex.printStackTrace();
            rtnVal.put("error", ex.toString());
        }
        return rtnVal;
    }

    public Map<String, String> downloadFile(String fileId, HttpServletResponse res, String userName) {
        Map<String, String> rtnVal = new HashMap<String, String>();
        try {
            rtnVal = fileService.downloadFile(fileId, res, userName);
        } catch(Exception ex) {
            ex.printStackTrace();
            rtnVal.put("error", ex.toString());
        }
        return rtnVal;
    }

    public Map<String, String> createFile(String userName) {
        Map<String, String> rtnVal = new HashMap<String, String>();
        try {
            rtnVal = fileService.createFile(userName);
        } catch(Exception ex) {
            ex.printStackTrace();
            rtnVal.put("error", ex.toString());
        }
        return rtnVal;
    }

    public Map<String, String> commitFile(String fileId, String userName) {
        Map<String, String> rtnVal = new HashMap<String, String>();
        try {
            rtnVal = fileService.commitFile(fileId, userName);
        } catch(Exception ex) {
            ex.printStackTrace();
            rtnVal.put("error", ex.toString());
        }
        return rtnVal;
    }

    public Map<String, String> deleteFile(String fileId, String userName) {
        Map<String, String> rtnVal = new HashMap<String, String>();
        try {
            rtnVal = fileService.deleteFile(fileId, userName);
        } catch(Exception ex) {
            ex.printStackTrace();
            rtnVal.put("error", ex.toString());
        }
        return rtnVal;
    }
}

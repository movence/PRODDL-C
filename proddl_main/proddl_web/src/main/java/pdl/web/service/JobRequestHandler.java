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

import org.springframework.web.multipart.MultipartFile;
import pdl.cloud.management.CloudInstanceManager;
import pdl.cloud.management.JobManager;
import pdl.cloud.management.UserService;
import pdl.cloud.model.FileInfo;
import pdl.cloud.model.JobDetail;
import pdl.cloud.model.User;
import pdl.common.*;
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
    JobManager jobManager;
    FileService fileService;
    FileTool fileTool;
    ArrayList<String> adminJobs;

    public JobRequestHandler() {
        jobManager = new JobManager();
        fileService = new FileService();
        fileTool = new FileTool();
        adminJobs = null;
    }

    /**
     * submit user requested job
     * @param jobName job identifier
     * @param inputInString input data in json format
     * @param userName user ID
     * @return json object
     */
    public Map<String, Object> submitJob(String jobName, String inputInString, String userName) {
        Map<String, Object> rtnVal = new TreeMap<String, Object> ();
        String result = null;

        try {
            boolean succeed;
            UserService userService = new UserService();

            //check user privilege for jobs
            this.checkJobPrivilege(jobName, userName);

            Map<String, Object> inputInMap = ToolPool.jsonStringToMap(inputInString);

            //add user only by admin
            if(jobName.equals("adduser")) {
                succeed = this.addUser(inputInMap, userService);
                if(succeed)
                    result = "user is added";
                else
                    result = "failed";
            } else { //regular job submission
                JobDetail jobDetail = new JobDetail(jobName);
                jobDetail.setJobName(jobName);
                jobDetail.setUserId(userName);
                jobDetail.setStatus(StaticValues.JOB_STATUS_SUBMITTED);

                if(inputInMap==null) {
                    inputInMap = new HashMap<String, Object>();
                }
                inputInMap.put("job", jobName);
                inputInMap.put("user", userName);
                jobDetail.setInput(ToolPool.jsonMapToString(inputInMap));

                succeed = jobManager.submitJob(jobDetail);
                if(succeed) {
                    rtnVal.put("id", jobDetail.getJobUUID());
                    rtnVal.put("name", jobDetail.getJobName());
                    result = "submitted";
                } else {
                    result = "failed";
                }
            }
            rtnVal.put("result", result);
        } catch (Exception ex) {
            //rtnVal.put("message", result==null?ex.toString():result);
            rtnVal.put("error", "job submission failed");
        }

        return rtnVal;
    }

    /**
     * check if user is admin for job privileges
     * @param userName user ID
     * @return boolean result
     * @throws Exception
     */
    private boolean isUserAdmin(String userName) throws Exception {
        UserService userService = new UserService();
        return userService.isAdmin(userName);
    }

    /**
     * check if user has corresponding job privilege
     * @param jobName job identifier
     * @param userName user ID
     * @throws Exception
     */
    private void checkJobPrivilege(String jobName, String userName) throws Exception {
        if(adminJobs==null) {
            adminJobs = new ArrayList<String>(Arrays.asList(StaticValues.ADMIN_ONLY_JOBS));
        }

        boolean isAdminUser = this.isUserAdmin(userName);

        if(!isAdminUser && adminJobs.contains(jobName))
            throw new Exception(String.format("access denied for %s", jobName));
    }

    /**
     * add new user - admin only
     * @param inputInMap input data in json format
     * @param userService user service object
     * @return boolean result
     * @throws Exception
     */
    private boolean addUser(Map<String, Object> inputInMap, UserService userService) throws Exception {
        User user = new User();

        if(!inputInMap.containsKey("id") || !inputInMap.containsKey("password"))
            throw new Exception("'adduser' requires 'id' and 'password'");

        String givenUserId = (String)inputInMap.get("id");
        //check for duplicated user id
        User dupUser = userService.getUserById(givenUserId);
        if(dupUser!=null)
            throw new Exception("user already exist");

        user.setUserId(givenUserId);
        user.setUserpass((String)inputInMap.get("password"));
        user.setAdmin(inputInMap.containsKey("admin")?Integer.parseInt(""+inputInMap.get("admin")):0);

        user.setFirstName(inputInMap.containsKey("firstname")?(String)inputInMap.get("firstname"):null);
        user.setLastName(inputInMap.containsKey("lastname")?(String)inputInMap.get("lastname"):null);
        user.setEmail(inputInMap.containsKey("email")?(String)inputInMap.get("email"):null);
        user.setPhone(inputInMap.containsKey("phone")?(String)inputInMap.get("phone"):null);

        return userService.loadUser(user);
    }

    /**
     * return current instance count of worker node
     * @return json object
     */
    public Map<String, Object> getInstanceCount() {
        HashMap<String, Object> rtnVal = new HashMap<String, Object>();
        CloudInstanceManager instanceManager = new CloudInstanceManager();
        Integer workers = instanceManager.getCurrentInstanceCount();
        rtnVal.put("c_c", workers);
        rtnVal.put("max", StaticValues.MAX_TOTAL_WORKER_INSTANCE);

        return rtnVal;
    }

    /**
     * get job information by UUID
     * @param jobId job UUID
     * @return json object
     */
    public Map<String , Object> getJobInfo(String jobId) {
        HashMap<String, Object> rtnVal = new HashMap<String, Object>();

        try {
            if(jobId==null || jobId.isEmpty())
                throw new Exception();

            JobDetail job = jobManager.getJobByID(jobId);

            Object jobInfo = null;
            if(job!=null) {
                Map<String, Object> jobInfoMap = new TreeMap<String, Object>();
                //jobInfoMap.put("Job ID", job.getJobUUID());
                jobInfoMap.put("name", job.getJobName());
                jobInfoMap.put("status", job.getStatusInString());
                jobInfoMap.put("user", job.getUserId());
                jobInfoMap.put("input", job.getInput());

                if(job.getStatus()== StaticValues.JOB_STATUS_COMPLETED) {//get job result
                    jobInfoMap.put("result", job.getResult());
                    jobInfoMap.put("log", job.getLog());
                }

                jobInfo = jobInfoMap;
            }
            rtnVal.put("info", jobInfo);

        } catch (Exception ex) {
            ex.printStackTrace();
            rtnVal.put("error", "failed to get job");
        }
        return rtnVal;
    }

    /**
     * get current status of a job
     * @param jobId job UUID
     * @return json object
     */
    public Map<String , String> getJobResult(String jobId) {
        HashMap<String, String> rtnVal = new HashMap<String, String>();

        try {
            JobDetail job = jobManager.getJobByID(jobId);
            /*rtnVal.put("status", job.getStatusInString());*/

            String result="";
            if(job!=null) {
                switch(job.getStatus()) {
                    case StaticValues.JOB_STATUS_COMPLETED:
                        result = job.getResult();
                        break;
                    case StaticValues.JOB_STATUS_FAILED:
                        result = "failed";
                        break;
                    default:
                        result = String.format("status-'%s'", job.getStatusInString());
                }
            }
            rtnVal.put("result", result);
        } catch (Exception ex) {
            ex.printStackTrace();
            rtnVal.put("error", String.format("failed to get job"));
        }

        return rtnVal;
    }

    /**
     * get list of jobs that belong to the user
     * @param userName user ID
     * @return json object
     */
    public Map<String, Object> getJobsForUser(String userName) {
        Map<String, Object> rtnVal = new HashMap<String, Object>();
        try {
            List<String> jobIdWithStatus = new ArrayList<String>();

            List<JobDetail> jobs = jobManager.getJobList(
                    QueryTool.getSingleConditionalStatement(StaticValues.COLUMN_USER_ID, "eq", userName)
            );
            if(jobs!=null && jobs.size()>0) {
                for(JobDetail job : jobs) {
                    jobIdWithStatus.add(job.getJobName()+":"+job.getJobUUID()+":"+job.getStatusInString());
                }
            }
            rtnVal.put("job", jobIdWithStatus);

        } catch(Exception ex) {
            ex.printStackTrace();
            rtnVal.put("error", "failed to get jobs");
        }

        return rtnVal;
    }

    /**
     * update job information
     * @param jobId job UUID
     * @param status job status
     * @param resultFileId result file (outcome) UUID
     * @param userName user ID
     * @return json object
     */
    public Map<String, String> updateJob(String jobId, int status, String resultFileId, String userName) {
        Map<String, String> rtnVal = new HashMap<String, String>();
        try {
            if(!this.isUserAdmin(userName))
                throw new Exception("access denied");

            fileService.getFilePathById(resultFileId);

            boolean result = jobManager.updateJobStatus(jobId, status, resultFileId, null);
            rtnVal.put("result", ""+(result?1:0));

        } catch(Exception ex) {
            ex.printStackTrace();
            rtnVal.put("error", ex.toString());
        }

        return rtnVal;
    }


    /*
    *    File handlers
    *
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

    public Map<String, Object> getFilesForUser(String userName) {
        Map<String, Object> rtnVal = new HashMap<String, Object>();
        try {
            List<String> fileInfoList = null;
            List<FileInfo> files = fileTool.getFileList(userName);
            if(files!=null && files.size()>0) {
                fileInfoList = new ArrayList<String>();
                for(FileInfo fileInfo : files) {
                    fileInfoList.add(fileInfo.getIuuid()+":"+fileInfo.getOriginalName()+":"+fileInfo.getStatusInString());
                }
            }
            rtnVal.put("file", fileInfoList);

        } catch(Exception ex) {
            ex.printStackTrace();
            rtnVal.put("error", "failed to get files");
        }

        return rtnVal;
    }
}

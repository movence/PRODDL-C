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

package pdl.web.controller.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pdl.common.StaticValues;
import pdl.web.service.JobRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/6/12
 * Time: 11:30 AM
 */
@Controller
@RequestMapping(value = "r")
public class RestMainController {
    JobRequestHandler handler;
    public RestMainController() {
        handler = new JobRequestHandler();
    }

    @RequestMapping(value = "role", method = RequestMethod.GET)
    public @ResponseBody Map<String, Object> getRoles(HttpServletRequest req, Principal principal) {
        Map<String, Object> rtnJson = new HashMap<String, Object>();
        rtnJson.put("hinder", req.isUserInRole("ROLE_ADMIN"));
        rtnJson.put("c_u", principal.getName());
        return rtnJson;
    }

    @RequestMapping(value = "instance", method = RequestMethod.GET)
    public @ResponseBody Map<String, Object> getInstanceCount(Principal principal) {
        Map<String, Object> rtnJson = handler.getInstanceCount();
        return rtnJson;
    }

    /**
     * Returns list of job ids for current user
     * @param principal user principal
     * @return list of jobs of current user in json format
     * @format curl <ip address>:<port>/pdl/r/joblist -u <user id>:<pass>
     */
    @RequestMapping(value = "joblist", method = RequestMethod.GET)
    public @ResponseBody Map<String, Object> getJobList(Principal principal) {
        Map rtnJson = handler.getJobsForUser(principal.getName());
        return rtnJson;
    }

    /**
     * submit a job
     * @param jobName job identifier
     * @param inputInString json formate input
     * @param principal user principal
     * @return job submission result in json format
     * @format curl <ip address>:<port>/pdl/r/job/<jobname> -d '{"key":"value"}' -u <user id>:<pass> -H "Content-Type: application/json" -X POST
     */
    @RequestMapping(value = "job/{name}", method = RequestMethod.POST)
    public @ResponseBody Map<String, Object> submitJob(
            @PathVariable("name") String jobName,
            //format '{"key":"value"}': {"interpreter":<name>, "script":<id>, "input":<id>, ...}
            @RequestBody() final String inputInString,
            Principal principal) {

        Map<String, Object> jobResult = handler.submitJob(jobName, inputInString, principal.getName());
        return jobResult;
    }

    /**
     * submit a job
     * @param jobName job identifier
     * @param principal user principal
     * @return job submission result in json format
     * @format curl <ip address>:<port>/pdl/r/job/<jobname> -u <user id>:<pass>
     */
    /*@RequestMapping(value = "job/{name}", method = RequestMethod.GET)
    public @ResponseBody Map<String, Object> jobRunner(@PathVariable("name") String jobName, Principal principal) {

        Map<String, Object> jobResult = handler.submitJob(jobName, null, principal.getName());
        return jobResult;
    }*/

    /**
     * queries job status with given job id
     * @param jobId jobUUID
     * @return job status in json format
     * @format curl <ip address>:<port>/pdl/r/job/?jid=<jobid> -u <user id>:<pass>
     */
    @RequestMapping(value = "job", method = RequestMethod.GET)
    public @ResponseBody Map<String, Object> getJobInfo(@RequestParam(value = "jid", defaultValue = "") String jobId) {

        Map<String, Object> jsonResult = handler.getJobInfo(jobId);
        return jsonResult;
    }

    /**
     * get job result
     * @param jobId job UUID
     * @return job result in json format
     * @format curl <ip address>:<port>/pdl/r/result/?jid=<jobid> -u <user id>:<pass>
     */
    @RequestMapping(value = "result", method = RequestMethod.GET)
    public @ResponseBody Map<String, String> getJobResult(@RequestParam(value = "jid", defaultValue = "") String jobId) {

        Map<String, String> jsonResult = handler.getJobResult(jobId);
        return jsonResult;
    }

    /**
     * update job status to completed
     * @param jobId job UUID
     * @return result in json format
     * @format curl <ip address>:<port>/pdl/r/status/complete?jid=<jobid>  -u <user id>:<pass> -X POST|GET
     */
    @RequestMapping(value = "status/complete", method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody Map<String, String> setJobComplete(
            @RequestParam(value = "jid", defaultValue = "") String jobId,
            @RequestParam(value = "result", defaultValue = "") String resultFileId,
            Principal principal) {
        Map<String, String> jsonResult = handler.updateJob(jobId, StaticValues.JOB_STATUS_COMPLETED, resultFileId, principal.getName());
        return jsonResult;
    }

    /**
     * update job status to failed
     * @param jobId job UUID
     * @return result in json format
     * @format curl <ip address>:<port>/pdl/r/status/complete?jid=<jobid>  -u <user id>:<pass> -X POST|GET
     */
    @RequestMapping(value = "status/fail", method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody Map<String, String> setJobFail(@RequestParam(value = "jid", defaultValue = "") String jobId, Principal principal) {
        Map<String, String> jsonResult = handler.updateJob(jobId, StaticValues.JOB_STATUS_FAILED, null, principal.getName());
        return jsonResult;
    }

    /**
     * kills a job
     * @param jobId job UUID
     * @return result in json format
     * @format curl <ip address>:<port>/pdl/r/kill/?jid=<jobid> -u <user id>:<pass> -X POST|GET
     */
    @RequestMapping(value = "kill", method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody Map<String, String> killJob(@RequestParam(value = "jid", defaultValue = "") String jobId) {

        Map<String, String> jsonResult = null;
        return jsonResult;
    }

    /**
     * File upload request handler (POST, PUT)
     * @param file MultipartFile data in form
     * @return file information in json format
     * @format curl <ip address>:<port>/pdl/r/file/upload -u <user id>:<pass> -F file=@<file> --keepalive-time <seconds> -X POST|PUT
     */
    @RequestMapping(value = "file/upload", method = {RequestMethod.POST, RequestMethod.PUT})
    public @ResponseBody Map<String, String> fileUpload(
            @RequestParam("file") MultipartFile file, @RequestParam(value = "type", defaultValue = "") String type, Principal principal) {
        //TODO allow admin to upload third-party application to tools container

        Map<String, String> rtnJson = handler.uploadFile(file, type, principal.getName());
        return rtnJson;
    }

    /**
     * download file by UUID
     * @return file information (UUID, absolute path) in json format
     * @format curl <ip address>:<port>/pdl/r/file/get/?id=<file id> -u <user id>:<pass> -o <filename> -X GET
     */
    @RequestMapping(value = "file/get", method = RequestMethod.GET)
    public void fileDownload(
            @RequestParam(value = "id", defaultValue = "") String fileId,
            Principal principal,
            HttpServletResponse res) {

        handler.downloadFile(fileId, res, principal.getName());
        return;
    }

    /**
     * Obtain new File UUID
     * @return file information (UUID, absolute path) in json format
     * @format curl <ip address>:<port>/pdl/r/file/new -u <user id>:<pass> -X POST|GET
     */
    @RequestMapping(value = "file/new", method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody Map<String, String> fileCreate(Principal principal) {

        Map<String, String> rtnJson = handler.createFile(principal.getName());
        return rtnJson;
    }

    /**
     * update file record as file operation finishes
     * @return file information (UUID, absolute path) in json format
     * @format curl <ip address>:<port>/pdl/r/file/commit/?id=<file id> -u <user id>:<pass> -X POST|GET
     */
    @RequestMapping(value = "file/commit", method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody Map<String, String> fileCommit(@RequestParam(value = "id", defaultValue = "") String fileId, Principal principal) {

        Map<String, String> rtnJson = handler.commitFile(fileId, principal.getName());
        return rtnJson;
    }

    /**
     * deletes a file
     * @param fileId   unique file identifier (rowKey of FileDetail table)
     * @return string message in json format
     * @format curl <ip address>:<port>/pdl/r/file/delete/?fileId=<fileId> -u <user id>:<pass>
     */
    @RequestMapping(value = "file/delete", method = RequestMethod.POST)
    public @ResponseBody Map<String, String> fileDelete(@RequestParam("id") String fileId, Principal principal) {

        Map<String, String> rtnJson = handler.deleteFile(fileId, principal.getName());
        return rtnJson;
    }

    /**
     * Returns list of files for current user
     * @param principal user principal
     * @return list of files that belong to current user in json format
     * @format curl <ip address>:<port>/pdl/r/filelist -u <user id>:<pass>
     */
    @RequestMapping(value = "filelist", method = RequestMethod.GET)
    public @ResponseBody Map<String, Object> getFileList(Principal principal) {
        Map rtnJson = handler.getFilesForUser(principal.getName());
        return rtnJson;
    }
}

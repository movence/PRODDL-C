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
import pdl.web.service.JobRequestHandler;
import pdl.web.service.common.FileService;

import java.security.Principal;
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

    /**
     * Returns list of job ids under current user
     * @param principal
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
     * @param jobName
     * @param inputInString json formate input
     * @param principal
     * @return job submission result in json format
     * @format curl <ip address>:<port>/pdl/r/job/<jobname> -d '{"key":"value"}' -u <user id>:<pass> -H "Content-Type: application/json"
     */
    @RequestMapping(value = "job/{name}", method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody Map<String, Object> jobRunner(
            @PathVariable("name") String jobName,
            @RequestBody final String inputInString, //format '{"key":"value"}': files{"mfile":"<fileID>" - makeflow file, "ifile":"<fileId>" - input file}
            Principal principal) {

        Map<String, Object> jobResult = handler.submitJob(jobName, inputInString, principal.getName());
        return jobResult;
    }

    /**
     * queries job status with given job id
     * @param jobId
     * @return job status in json format
     * @format curl <ip address>:<port>/pdl/r/job/?jid=<jobid> -u <user id>:<pass>
     */
    @RequestMapping(value = "job", method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody Map<String, Object> getJobInfo(@RequestParam(value = "jid", defaultValue = "") String jobId) {

        Map<String, Object> jsonResult = handler.getJobInfo(jobId);
        return jsonResult;
    }

    /**
     * get job result
     * @param jobId
     * @return job result in json format
     * @format curl <ip address>:<port>/pdl/r/job/result/?jid=<jobid> -u <user id>:<pass>
     */
    @RequestMapping(value = "job/result", method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody Map<String, String> getJobResult(@RequestParam(value = "jid", defaultValue = "") String jobId) {

        Map<String, String> jsonResult = handler.getJobResult(jobId);
        return jsonResult;
    }

    /**
     * kills a job
     * @param jobId
     * @return result in json format
     * @format curl <ip address>:<port>/pdl/r/job/kill/?jid=<jobid> -u <user id>:<pass> -X POST|GET
     */
    @RequestMapping(value = "job/kill", method = {RequestMethod.POST, RequestMethod.GET})
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

        FileService fileService = new FileService();
        Map<String, String> rtnJson = fileService.uploadFile(file, type, principal.getName());
        return rtnJson;
    }

    /**
     * Obtain new File UUID
     * @return file information (UUID, absolute path) in json format
     * @format curl <ip address>:<port>/pdl/r/file/new -u <user id>:<pass> -X POST|GET
     */
    @RequestMapping(value = "file/new", method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody Map<String, String> fileCreate(Principal principal) {
        FileService fileService = new FileService();
        Map<String, String> rtnJson = fileService.createFile(principal.getName());
        return rtnJson;
    }

    /**
     * Obtain new File UUID
     * @return file information (UUID, absolute path) in json format
     * @format curl <ip address>:<port>/pdl/r/file/commit/?id=<file id> -u <user id>:<pass> -X POST|GET
     */
    @RequestMapping(value = "file/commit", method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody Map<String, String> fileCommit(@RequestParam(value = "id", defaultValue = "") String fileId, Principal principal) {
        FileService fileService = new FileService();
        Map<String, String> rtnJson = fileService.commitFile(fileId, principal.getName());
        return rtnJson;
    }

    /**
     * deletes a file
     * @param fileId   unique file identifier (rowKey of FileDetail table)
     * @return string message in json format
     * @format curl <ip address>:<port>/pdl/r/file/delete/?fileId=<fileId> -u <user id>:<pass>
     */
    @RequestMapping(value = "file/delete", method = RequestMethod.POST)
    public @ResponseBody Map<String, String> fileDelete(@RequestParam("fileId") String fileId, Principal principal) {

        FileService fileService = new FileService();
        Map<String, String> rtnJson = fileService.deleteFile(fileId, principal.getName());
        return rtnJson;
    }
}

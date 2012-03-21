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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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

    /*$ curl -k -u 'key:secret_key' https://api.picloud.com/r/unique_id/square_func/
    {
        "label": "square_func",
        "output_encoding": "json",
        "version": "0.1",
        "uri": "https://api.picloud.com/r/unique_id/square_func",
        "signature": "square_func(x)"
        "description": "Returns square of a number"
    }
    $ curl -k -u 'key:secret_key' https://api.picloud.com/job/?jids=12
    {
      "info":
      {
        "12":
        {
          "status": "done",
          "exception": null,
          "runtime": 0.1,
          "stderr": "",
          "stdout": "Squaring 5"
         }
      },
      "version": "0.1"
    }
    $ curl -k -u 'key:secret_key' https://api.picloud.com/job/?jids=12&field=status&field=stdout
    {
      "info":
      {
        "12":
        {
          "status": "done",
          "stdout": "Squaring 5"
         }
      },
      "version": "0.1"
    }
    $ curl -k -u 'key:secret_key' https://api.picloud.com/job/result/?jid=12
    {
      "version": "0.1",
      "result": 25
    }
    {"version": "0.1", "error_code": 455, "retry": false, "error_msg": "Requested job is not done."}
    $ curl -k -u 'key:secret_key' -X POST https://api.picloud.com/job/delete/?jids=12
    $ curl -k -u 'key:secret_key' -X POST https://api.picloud.com/job/kill/?jids=12*/

    /**
     * This allows user to query available jobs through RestFul service
     *
     * @return available job information in json format
     */
    @RequestMapping(value = "joblist", method = RequestMethod.GET)
    public @ResponseBody Map<String, Object> getJobList(Principal principal) {

        Map rtnJson = handler.getJobsForUser(principal.getName());
        return rtnJson;
    }

    @RequestMapping(value = "job/{name}", method = {RequestMethod.POST, RequestMethod.GET}, headers = "Accept=application/json")
    public @ResponseBody Map<String, Object> jobRunner(
            @PathVariable("name") String jobName,
            @RequestBody final String inputInString, //format '{"key":"value"}'
            Principal principal) {

        Map<String, Object> jobResult = handler.submitJob(jobName, inputInString, principal.getName());
        return jobResult;
    }

    /**
     * @param jobId
     * @return
     */
    @RequestMapping(value = "job", method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody Map<String, Object> getJobInfo(@RequestParam(value = "jid", defaultValue = "") String jobId) {

        Map<String, Object> jsonResult = handler.getJobInfo(jobId);
        return jsonResult;
    }

    /**
     * @param jobId
     * @return
     */
    @RequestMapping(value = "job/result", method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody Map<String, String> getJobResult(@RequestParam(value = "jid", defaultValue = "") String jobId) {

        Map<String, String> jsonResult = handler.getJobResult(jobId);
        return jsonResult;
    }

    /**
     * @param jobId
     * @return
     */
    @RequestMapping(value = "job/kill", method = RequestMethod.POST)
    public @ResponseBody Map<String, String> killJob(@RequestParam(value = "jid", defaultValue = "") String jobId) {

        Map<String, String> jsonResult = null;
        return jsonResult;
    }

    /**
     * File upload request handler (POST, PUT)
     *
     * @param file MultipartFile data in form
     * @return file information in json format
     */
    @RequestMapping(value = "file/upload", method = {RequestMethod.POST, RequestMethod.PUT})
    public @ResponseBody Map<String, Object> fileUpload(
            @RequestParam("file") MultipartFile file, @RequestParam(value = "type", defaultValue = "") String type, Principal principal) {

        Map<String, Object> rtnJson = new TreeMap<String, Object>();
        String exceptionStr = "";

        try {
            FileService fileService = new FileService();
            if(type.isEmpty())
                type="blob";
            String fileUid = fileService.uploadFile(file, type, principal.getName());

            if (fileUid == null)
                throw new Exception();

            rtnJson.put("Result", "Succeed");
            rtnJson.put("Name", file.getOriginalFilename());
            rtnJson.put("AccessId", fileUid);
            rtnJson.put("Size", String.valueOf(file.getSize()));
            rtnJson.put("User", principal.getName());
            rtnJson.put("Contnet-Type", file.getContentType());
        } catch (Exception e) {
            rtnJson.put("error", "File upload failed for " + file.getOriginalFilename());
            rtnJson.put("message", exceptionStr);
        }

        return rtnJson;
    }

    /**
     * deletes request of cloud file
     *
     * @param fileName original file name
     * @param fileId   unique file identifier (rowKey of FileDetail table)
     * @return string message in json format
     * @format curl <ip address>:<port>/pdl/r/file/upload -u <user id>:<pass> -F file=@<filepath> -X POST
     */
    @RequestMapping(value = "file/delete", method = RequestMethod.POST)
    public @ResponseBody Map<String, String> fileDelete(@RequestParam("name") String fileName, @RequestParam("fileId") String fileId) {
        Map<String, String> rtnJson = new HashMap<String, String>();

        try {
            if ((fileName == null || fileName.trim().isEmpty()) && (fileId == null || fileId.trim().isEmpty()))
                throw new Exception();

            FileService fileService = new FileService();
            if (fileService.deleteFile(fileName, fileId))
                throw new Exception();

            rtnJson.put("message", "File has been deleted");
        } catch (Exception e) {
            rtnJson.put("error_msg", String.format("File deletion failed for %s (id:%s).", fileName, fileId));
        }

        return rtnJson;
    }
}

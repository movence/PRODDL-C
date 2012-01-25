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
import pdl.web.service.JobReuqestHandler;
import pdl.web.service.common.FileService;

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

    @RequestMapping(value = "job/{name}", method = RequestMethod.POST)
    public @ResponseBody Map<String, Object> jobRunner(
            @PathVariable("name") String jobName,
            @RequestBody final String inputInString,
            Principal principal ) {
        Map<String , Object> rtnJson = new HashMap<String, Object>();
        String exceptionStr = null;

        try {
            JobReuqestHandler jobHandler = new JobReuqestHandler();
            Map<String, Object> jobResult = jobHandler.runJob( jobName, inputInString, principal.getName() );

            rtnJson.put( "info", "Job Execution" );
            rtnJson.put( "jobName", jobName );
            rtnJson.put( "input", inputInString );
            rtnJson.put( "result", jobResult );
            rtnJson.put( "message", "Job has been submitted.");
        } catch ( Exception e ) {
            if( exceptionStr == null )
                exceptionStr = e.toString();
            rtnJson.put( "message", "Failed Job Execution." );
            rtnJson.put( "exception", exceptionStr );
        }

        return rtnJson;
    }

    /**
     *
     * @param jobId
     * @return
     */
    @RequestMapping(value = "job/result", method = RequestMethod.POST)
    public @ResponseBody Map<String, Object> jobRunner( @RequestParam(value = "jobId", defaultValue = "") String jobId ) {
        Map<String , Object> rtnJson = new HashMap<String, Object>();
        String exceptionStr = "";

        try {
            JobReuqestHandler jobHandler = new JobReuqestHandler();

            rtnJson.put( "info", "Job Result" );

        } catch ( Exception e ) {
            rtnJson.put( "message", "Failed Job Result Retrieval." );
            rtnJson.put( "exception", exceptionStr );
        }

        return rtnJson;
    }

    /**
     *
     * @param jobId
     * @return
     */
    @RequestMapping(value = "job/kill", method = RequestMethod.POST)
    public @ResponseBody Map<String, Object> killJob( @RequestParam(value = "jobId", defaultValue = "") String jobId ) {
        Map<String , Object> rtnJson = new HashMap<String, Object>();
        String exceptionStr = "";

        try {
            if( "".equals( jobId ) ) {
                exceptionStr = "JOB ID should be provided ([host]/pdll/r/job/kill?jobId=[job id to kill]).";
                throw new Exception();
            }

            JobReuqestHandler jobHandler = new JobReuqestHandler();

            rtnJson.put( "info", "Job Termination" );
            rtnJson.put( "job ID", jobId );
            rtnJson.put( "status", "done" );
        } catch ( Exception e ) {
            rtnJson.put( "message", "Failed Job Termination." );
            rtnJson.put( "exception", exceptionStr );
        }

        return rtnJson;
    }

    /**
     * File upload request handler (POST, PUT)
     * @param file MultipartFile data in form
     * @return file information in json format
     */
    @RequestMapping(value = "file/upload", method = {RequestMethod.POST, RequestMethod.PUT})
    public @ResponseBody Map<String, Object> fileUpload(
            @RequestParam("file") MultipartFile file, @RequestParam("type") String type, Principal principal ) {
        Map<String , Object> rtnJson = new HashMap<String, Object>();
        String exceptionStr = "";

        try {
            FileService fileService = new FileService();
            String fileUid = fileService.uploadFile( file, type );

            if( fileUid == null )
                throw new Exception();

            rtnJson.put( "info", "File Upload" );

            Map<String, String> fileJson = new HashMap<String, String>();
            fileJson.put( "name", file.getOriginalFilename() );
            fileJson.put( "accessId", fileUid );
            fileJson.put( "size", String.valueOf( file.getSize() ) );
            fileJson.put( "user",  principal.getName() );
            fileJson.put( "Contnet-Type", file.getContentType() );
            rtnJson.put( "file", fileJson );
            rtnJson.put( "status", "success" );
            rtnJson.put( "message", "File has been uploaded" );
        } catch ( Exception e ) {
            rtnJson.put( "message", "File upload failed for " + file.getOriginalFilename() );
            rtnJson.put( "exception", exceptionStr );
        }

        return rtnJson;
    }

    /**
     * deletes request of cloud file
     * @param fileName original file name
     * @param fileId unique file identifier (rowKey of FileDetail table)
     * @return string message in json format
     */
    @RequestMapping(value = "file/delete", method = RequestMethod.POST)
    public @ResponseBody Map<String, String> fileDelete(
            @RequestParam("name") String fileName, @RequestParam("fileId") String fileId ) {
        Map<String , String> rtnJson = new HashMap<String, String>();

        try {
            if( ( fileName == null || fileName.trim().isEmpty() ) && ( fileId == null || fileId.trim().isEmpty() ) )
                throw new Exception();

            FileService fileService = new FileService();
            if( fileService.deleteFile( fileName, fileId ) )
                throw new Exception();

            rtnJson.put( "message", "File has been deleted" );
        } catch ( Exception e ) {
            rtnJson.put( "message", String.format( "File deletion failed for {0} (id={1}).", fileName, fileId ) );
        }

        return rtnJson;
    }

    @RequestMapping(value = "jsontest", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, String> getJsonResult() {
        HashMap<String, String> rtnVal = new HashMap<String, String>();
        rtnVal.put("test", "pompom");
        return rtnVal;
    }
}

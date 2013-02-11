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

package pdl.web.service.common;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;
import pdl.cloud.model.FileInfo;
import pdl.common.FileTool;
import pdl.common.ToolPool;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/10/12
 * Time: 4:06 PM
 */
public class FileService {
    protected static Logger logger = Logger.getLogger("FileService");
    private FileTool fileTool;

    public FileService() {
        fileTool = new FileTool();
    }

    public Map<String, String> uploadFile(MultipartFile theFile, String type, String username) {
        Map<String, String> rtnJson = new TreeMap<String, String>();
        try {
            /*if(type.isEmpty())
                type="blob";*/

            String fileUid = null;
            if (theFile.getSize() > 0) {
                InputStream fileIn = theFile.getInputStream();
                fileUid = fileTool.createFile(type, fileIn, theFile.getOriginalFilename(), username);
            }

            if (fileUid == null)
                throw new Exception();

            rtnJson.put("id", fileUid);
        } catch (Exception ex) {
            rtnJson.put("error", "File upload failed for " + theFile.getOriginalFilename());
            rtnJson.put("message", ex.toString());
        }

        return rtnJson;
    }

    public String getFilePathById(String fileId) throws Exception {
        return fileTool.getFilePath(fileId);
    }

    public Map<String, String> downloadFile(String fileId, HttpServletResponse res, String username) {
        //TODO check user permission for downloading a file
        Map<String, String> rtnJson = new TreeMap<String, String>();
        try {
            String filePath = this.getFilePathById(fileId);
            if(filePath!=null && !filePath.isEmpty() && ToolPool.canReadFile(filePath)) {
                String fileName = fileId.concat(".json");
                res.setContentType("application/json");
                res.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                IOUtils.copy(new FileInputStream(filePath), res.getOutputStream());
                res.flushBuffer();
                rtnJson.put("file name", fileName);
            }
        } catch (Exception ex) {
            rtnJson.put("error", "Downloading file failed");
            rtnJson.put("message", ex.toString());
        }

        return rtnJson;
    }

    public Map<String, String> createFile(String userName) {
        Map<String, String> rtnJson = new TreeMap<String, String>();
        try {
            FileInfo fileInfo = fileTool.createFileRecord(null, userName);
            fileTool.insertFileRecord(fileInfo);
            rtnJson.put("id", fileInfo.getIuuid());
            rtnJson.put("path", fileInfo.getPath() + File.separator + fileInfo.getName());
        } catch (Exception ex) {
            rtnJson.put("error", "Creating file failed");
            rtnJson.put("message", ex.toString());
        }

        return rtnJson;
    }

    public Map<String, String> commitFile(String fileId, String userName) {
        Map<String, String> rtnJson = new TreeMap<String, String>();
        try {
            boolean committed = fileTool.commitFileRecord(fileId);
            if(!committed) {
                throw new Exception();
            }
            rtnJson.put("result", "file committed");
        } catch (Exception ex) {
            rtnJson.put("error", "Committing file failed");
        }

        return rtnJson;
    }

    public Map<String, String> deleteFile(String fileId, String username) {
        Map<String, String> rtnJson = new TreeMap<String, String>();
        try {
            if(fileId!=null && !fileId.isEmpty()) {
                boolean deleted = fileTool.delete(fileId, username);
                if(!deleted) {
                    throw new Exception();
                }
                rtnJson.put("result", "file deleted");
            }
        } catch (Exception ex) {
            rtnJson.put("error", "file deletion failed");
        }

        return rtnJson;
    }

    public List<FileInfo> getFileList(String userName) {
        List<FileInfo> rtnVal = null;
        try {
            rtnVal = fileTool.getFileList(userName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rtnVal;
    }
}

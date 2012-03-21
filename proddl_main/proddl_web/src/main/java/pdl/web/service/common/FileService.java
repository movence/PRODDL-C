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

import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;
import pdl.common.FileTool;

import java.io.InputStream;
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
            if(type.isEmpty())
                type="blob";

            String fileUid = null;
            if (theFile.getSize() > 0) {
                InputStream fileIn = theFile.getInputStream();
                fileUid = fileTool.createFile(type, fileIn, theFile.getOriginalFilename(), theFile.getContentType(), username);
            }

            if (fileUid == null)
                throw new Exception();

            rtnJson.put("Name", theFile.getOriginalFilename());
            rtnJson.put("AccessId", fileUid);
            rtnJson.put("Size", String.valueOf(theFile.getSize()));
            /*rtnJson.put("User", principal.getName());
            rtnJson.put("Result", "Succeed");
            rtnJson.put("Contnet-Type", file.getContentType());*/
        } catch (Exception ex) {
            rtnJson.put("error", "File upload failed for " + theFile.getOriginalFilename());
        }

        return rtnJson;
    }

    public Map<String, String> deleteFile(String fileId, String username) {
        Map<String, String> rtnJson = new TreeMap<String, String>();
        try {
            if(fileId!=null && !fileId.isEmpty()) {
                boolean deleted = fileTool.delete(fileId, username);
                if(deleted) {
                    rtnJson.put("result", String.format("File '%s' has been deleted", fileId));
                } else {
                    rtnJson.put("result", "File ID does not exist!");
                }

            }
        } catch (Exception ex) {
            rtnJson.put("error", String.format("File upload failed for ID '%s'", fileId));
        }

        return rtnJson;
    }
}

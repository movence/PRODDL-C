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

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/10/12
 * Time: 4:06 PM
 */
public class FileService {
    protected static Logger logger = Logger.getLogger("FileService");

    public String uploadFile(MultipartFile theFile, String type, String username) throws Exception {
        String fileUid = null;

        try {
            if (theFile.getSize() > 0) {
                InputStream fileIn = theFile.getInputStream();
                FileTool fileTool = new FileTool();
                fileUid = fileTool.createFile(type, fileIn, theFile.getOriginalFilename(), theFile.getContentType(), username);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

        return fileUid;
    }

    public boolean deleteFile(String fileName, String fileId) throws Exception {
        boolean result = false;

        try {


        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

        return result;
    }
}

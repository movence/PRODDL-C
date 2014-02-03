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

package pdl.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 12/13/11
 * Time: 2:34 PM
 */
public class ZipHandler {
    public ZipHandler() {
    }

    public boolean unZip(String filePath, String parentPath) throws Exception {
        boolean rtnVal = false;

        try {
            ZipFile zipFile = new ZipFile(filePath);
            Enumeration files = zipFile.entries();

            while (files.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) files.nextElement();
                InputStream eis = zipFile.getInputStream(entry);
                File f = new File(parentPath + entry.getName());
                if (entry.isDirectory()) {
                    f.mkdirs();
                    continue;
                } else {
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                }
                FileUtils.copyInputStreamToFile(eis, f);
                eis.close();
            }
            zipFile.close();
            rtnVal = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("unzip process failed.");
        }
        return rtnVal;
    }
}

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

package pdl.cloud;


import pdl.common.Configuration;
import pdl.common.StaticValues;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 2/13/12
 * Time: 3:16 PM
 */
public class StorageServices {
    private Configuration conf;

    private pdl.cloud.storage.TableOperator tableOperator;
    private pdl.cloud.storage.BlobOperator blobOperator;

    public StorageServices() {
        conf = Configuration.getInstance();
    }

    /*
     *TABLE OPERATIONS
     */
    private pdl.cloud.storage.TableOperator getTableOperator() {
        if (tableOperator == null)
            tableOperator = new pdl.cloud.storage.TableOperator(conf);
        return tableOperator;
    }

    public String getFileNameById(String name) {
        pdl.cloud.model.FileInfo currFile = (pdl.cloud.model.FileInfo) getTableOperator().queryEntityBySearchKey(
                conf.getStringProperty("TABLE_NAME_FILES"),
                StaticValues.COLUMN_FILE_INFO_NAME, name,
                pdl.cloud.model.FileInfo.class);
        return currFile.getName() + "." + currFile.getType();
    }


    /*
     *BLOB OPERATIONS
     */
    private pdl.cloud.storage.BlobOperator getBlobOperator() {
        if (blobOperator == null)
            blobOperator = new pdl.cloud.storage.BlobOperator(conf);
        return blobOperator;
    }


    public boolean downloadByName(String container, String name, String path) {
        return getBlobOperator().getBlob(container, name, path, false);
    }

    public boolean downloadJobFileByName(String name, String path) {
        return this.downloadByName(conf.getStringProperty("BLOB_CONTAINER_JOB_FILES"), name, path);
    }

    public boolean downloadToolsByName(String name, String path) {
        return this.downloadByName(conf.getStringProperty("BLOB_CONTAINER_TOOLS"), name, path);
    }
}

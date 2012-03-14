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


import pdl.cloud.model.FileInfo;
import pdl.cloud.storage.BlobOperator;
import pdl.cloud.storage.TableOperator;
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

    private TableOperator tableOperator;
    private BlobOperator blobOperator;

    public StorageServices() {
        conf = Configuration.getInstance();
    }

    /*
     *TABLE OPERATIONS
     */
    private TableOperator getTableOperator() {
        if (tableOperator == null)
            tableOperator = new TableOperator(conf);
        return tableOperator;
    }

    public String getFileNameById(String name) {
        FileInfo currFile = (FileInfo) getTableOperator().queryEntityBySearchKey(
                conf.getStringProperty("TABLE_NAME_FILES"),
                StaticValues.COLUMN_FILE_INFO_NAME, name,
                FileInfo.class);
        return currFile.getName() + "." + currFile.getType();
    }


    /*
     *BLOB OPERATIONS
     */
    private BlobOperator getBlobOperator() {
        if (blobOperator == null)
            blobOperator = new BlobOperator(conf);
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

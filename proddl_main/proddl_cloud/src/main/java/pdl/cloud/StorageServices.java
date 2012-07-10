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


import org.soyatec.windowsazure.table.ITableServiceEntity;
import pdl.cloud.model.FileInfo;
import pdl.cloud.storage.BlobOperator;
import pdl.cloud.storage.TableOperator;
import pdl.common.Configuration;
import pdl.common.StaticValues;
import pdl.common.ToolPool;

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
    private String tableName;

    public StorageServices() {
        conf = Configuration.getInstance();
        tableName = ToolPool.buildTableName(conf.getStringProperty("TABLE_NAME_FILES"));
    }

    /*
     *TABLE OPERATIONS
     */
    private TableOperator getTableOperator() {
        if (tableOperator == null)
            tableOperator = new TableOperator(conf);
        return tableOperator;
    }

    public boolean insertSingleEnttity(String tablename, ITableServiceEntity entity) {
        return getTableOperator().insertSingleEntity(tablename, entity);
    }

    public String getFileNameById(String fileId) {
        FileInfo currFile = (FileInfo) getTableOperator().queryEntityBySearchKey(
                tableName,
                StaticValues.COLUMN_ROW_KEY, fileId,
                FileInfo.class);
        return currFile==null?null:currFile.getName();
    }

    public ITableServiceEntity queryEntityBySearchKey(String tablename, String column, String key, Class model) {
        return getTableOperator().queryEntityBySearchKey(tablename, column, key, model);
    }

    public boolean updateEntity(String tablename, ITableServiceEntity entity) {
        return getTableOperator().updateSingleEntity(tablename, entity);
    }

    public void deleteEntity(String tablename, ITableServiceEntity entity) {
        getTableOperator().deleteEntity(tablename, entity);
    }


    /*
     *BLOB OPERATIONS
     */
    private BlobOperator getBlobOperator() {
        if (blobOperator == null)
            blobOperator = new BlobOperator(conf);
        return blobOperator;
    }

    public boolean uploadFileToBlob(FileInfo fileInfo, String filePath, boolean overwrite) throws Exception {
        boolean uploaded = getBlobOperator().uploadFileToBlob(fileInfo.getContainer(), fileInfo.getName(), filePath, fileInfo.getType(), overwrite);
        boolean inserted = false;
        if(uploaded) {
            inserted = this.insertSingleEnttity(tableName, fileInfo);
        }
        return inserted;
    }

    public boolean downloadByName(String container, String name, String path) {
        return getBlobOperator().getBlob(container, name, path, false);
    }

    public boolean downloadToolsByName(String name, String path) {
        return this.downloadByName(conf.getStringProperty("BLOB_CONTAINER_TOOLS"), name, path);
    }
    /*
     * Files are stored in Azure drive 7/6/12
    public boolean downloadJobFileByName(String name, String path) {
        return this.downloadByName(conf.getStringProperty("BLOB_CONTAINER_JOB_FILES"), name, path);
    }

    public boolean downloadUploadedFileByName(String name, String path) {
        return this.downloadByName(conf.getStringProperty("BLOB_CONTAINER_UPLOADS"), name, path);
    }

    public boolean downloadFilesByName(String name, String path) {
        return this.downloadByName(conf.getStringProperty("BLOB_CONTAINER_FILES"), name, path);
    }
    */
    public boolean deleteBlob(String containerName, String blobName) {
        return getBlobOperator().deleteBlob(containerName, blobName);
    }
}

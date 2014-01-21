/*
 * Copyright J. Craig Venter Institute, 2014
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package pdl.cloud;


import org.soyatec.windowsazure.table.ITableServiceEntity;
import pdl.cloud.model.AbstractModel;
import pdl.cloud.model.FileInfo;
import pdl.cloud.storage.BlobOperator;
import pdl.cloud.storage.TableOperator;
import pdl.utils.StaticValues;
import pdl.utils.ToolPool;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 2/13/12
 * Time: 3:16 PM
 */
public class StorageServices {
    private TableOperator tableOperator;
    private BlobOperator blobOperator;

    public StorageServices() {}

    /*
     *TABLE OPERATIONS
     */
    private TableOperator getTableOperator() {
        if (tableOperator == null)
            tableOperator = new TableOperator();
        return tableOperator;
    }

    public boolean insertSingleEnttity(String tableName, AbstractModel entity) {
        return getTableOperator().insertEntity(tableName, entity);
    }

    public String getFileNameById(String fileId) {
        Map<String, String> entity = getTableOperator().queryEntityBySearchKey(
                ToolPool.buildTableName(StaticValues.TABLE_NAME_FILES), StaticValues.COLUMN_ROW_KEY, fileId
        );
        return entity == null ? null : entity.get("name");
    }

    public ITableServiceEntity queryEntityBySearchKey(String tableName, String column, String key, Class model) {
        return getTableOperator().queryEntityBySearchKey(tableName, column, key, model);
    }

    public List<ITableServiceEntity> queryListBySearchKey(String tableName, String column, String key, Class model) {
        return getTableOperator().queryListBySearchKey(tableName, column, key, null, null, model);
    }

    public boolean updateEntity(String tableName, AbstractModel entity) {
        return getTableOperator().updateEntity(tableName, entity);
    }

    public void deleteEntity(String tableName, AbstractModel entity) {
        getTableOperator().deleteEntity(tableName, entity);
    }


    /*
     *BLOB OPERATIONS
     */
    private BlobOperator getBlobOperator() {
        if (blobOperator == null)
            blobOperator = new BlobOperator();
        return blobOperator;
    }

    public boolean uploadFileToBlob(FileInfo fileInfo, String filePath, boolean overwrite) throws Exception {
        boolean uploaded = getBlobOperator().uploadFileToBlob(fileInfo.getContainer(), fileInfo.getName(), filePath, fileInfo.getType(), overwrite);
        boolean inserted = false;
        if(uploaded) {
            inserted = this.insertSingleEnttity(ToolPool.buildTableName(StaticValues.TABLE_NAME_FILES), fileInfo);
        }
        return inserted;
    }

    public boolean downloadByName(String container, String name, String path) {
        return getBlobOperator().getBlob(container, name, path, false);
    }

    public boolean downloadToolsByName(String name, String path) {
        return this.downloadByName(StaticValues.BLOB_CONTAINER_TOOLS, name, path);
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

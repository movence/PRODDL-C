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

package pdl.common;

import pdl.cloud.StorageServices;
import pdl.cloud.model.DynamicData;
import pdl.cloud.model.FileInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 3/20/12
 * Time: 1:46 PM
 */
public class FileTool {
    Configuration conf;
    StorageServices services;
    String uploadDirectoryPath;

    public FileTool() {
        services = new StorageServices();
        this.getUploadDirectoryPath();
    }

    private void getUploadDirectoryPath() {
        if(conf==null)
            conf = Configuration.getInstance();

        String storagePath = conf.getStringProperty("STORAGE_PATH");

        if(storagePath==null) {
            DynamicData storageData = (DynamicData)services.queryEntityBySearchKey(conf.getStringProperty("TABLE_NAME_DYNAMIC_DATA"),
                    StaticValues.COLUMN_DYNAMIC_DATA_KEY, StaticValues.KEY_DYNAMIC_DATA_STORAGE_PATH, DynamicData.class);

            if(storageData==null)
                storagePath = System.getProperty("java.io.tmpdir");
            else
                storagePath = storageData.getDataValue();
            conf.setProperty("STORAGE_PATH", storagePath);
        }

        uploadDirectoryPath = storagePath + StaticValues.DIRECTORY_FILE_UPLOAD_AREA;
        File uploadDir = new File(uploadDirectoryPath);
        if(!uploadDir.exists())
            uploadDir.mkdir();
    }

    public String createFile(String type, InputStream fileIn, String fileName, String fileType, String username) throws Exception{
        String rtnVal = null;
        try {
            String newFilePath = uploadDirectoryPath + File.separator + fileName;
            FileOutputStream fileOut = new FileOutputStream(newFilePath);

            int readBytes = 0;
            int readBlockSize = 4 * 1024;
            byte[] buffer = new byte[readBlockSize];
            while ((readBytes = fileIn.read(buffer, 0, readBlockSize)) != -1) {
                fileOut.write(buffer, 0, readBytes);
            }

            fileOut.close();
            fileIn.close();

            rtnVal = newFilePath;

            //TODO It might need to allow files to be uploaded to other blob containers than jobFiles
            if (type!=null && type.equals("blob")) {
                boolean uploaded = services.uploadFileToBlob(conf.getStringProperty("BLOB_CONTAINER_UPLOADS"), fileName, newFilePath, fileType, false);
                boolean inserted = false;
                if(uploaded) {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setName(fileName);
                    fileInfo.setType(fileType);
                    fileInfo.setUserId(username);

                    inserted = services.insertSingleEnttity(conf.getStringProperty("TABLE_NAME_FILES"), fileInfo);
                    if(inserted)
                        rtnVal = fileInfo.getIuuid();

                }

                if(!uploaded || !inserted) {
                    File file = new File(newFilePath);
                    if(file.exists())
                        file.delete();
                    throw new Exception("FileTool:Failed to upload file to Blob storage.");
                }

            }
        } catch(Exception ex) {
            throw ex;
        }
        return rtnVal;
    }

    public boolean delete(String fileId, String username) throws Exception {
        boolean rtnVal = false;
        try {
            String filesTable = conf.getStringProperty("TABLE_NAME_FILES");
            FileInfo info = (FileInfo)services.queryEntityBySearchKey(
                    filesTable, StaticValues.COLUMN_ROW_KEY, fileId, FileInfo.class
            );

            if(info!=null) {
                if(!username.equals(info.getUserId()))
                    throw new Exception("The file belongs to another user");

                services.deleteEntity(filesTable, info);

                String filePath = uploadDirectoryPath + File.separator + info.getName();
                File file = new File(filePath);
                if(file.exists())
                    file.delete();

                rtnVal = services.deleteBlob(conf.getStringProperty("BLOB_CONTAINER_UPLOADS"), info.getName());
            }
        } catch(Exception ex) {
            throw ex;
        }

        return rtnVal;
    }
}

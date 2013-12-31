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

import org.apache.commons.io.FileUtils;
import org.soyatec.windowsazure.table.ITableServiceEntity;
import pdl.cloud.model.FileInfo;
import pdl.cloud.storage.BlobOperator;
import pdl.cloud.storage.TableOperator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 3/20/12
 * Time: 1:46 PM
 */
public class FileTool {
    Configuration conf;
    TableOperator tableOperator;
    String fileStoragePath;
    String fileTableName;

    public FileTool() {
        this.initialize();
    }

    public String getFileStoragePath() {
        return fileStoragePath;
    }

    /**
     * Initialize FileTool by getting configuration values
     */
    private void initialize() {
        if(conf==null)
            conf = Configuration.getInstance();
        tableOperator = new TableOperator(conf);

        fileTableName = ToolPool.buildTableName(StaticValues.TABLE_NAME_FILES);

        String storagePath = conf.getStringProperty(StaticValues.CONFIG_KEY_DATASTORE_PATH);

        fileStoragePath = ToolPool.buildDirPath(storagePath, StaticValues.DIRECTORY_FILE_AREA);
        File uploadDir = new File(fileStoragePath);
        if(!uploadDir.exists())
            uploadDir.mkdir();
    }

    /**
     * create file information object with reserved flag
     * @param originalFileName original file name
     * @param userName user ID
     * @return file information
     */
    public FileInfo createFileRecord(String originalFileName, String userName) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(fileInfo.getIuuid()+StaticValues.FILE_EXTENSION_DAT);
        fileInfo.setUserId(userName);
        fileInfo.setStatus(StaticValues.FILE_STATUS_RESERVED);
        fileInfo.setOriginalName(originalFileName);

        int hashedDirectory = Math.abs(fileInfo.getIuuid().hashCode()) % StaticValues.MAX_FILE_COUNT_PER_DIRECTORY;
        String dirPath = fileStoragePath + hashedDirectory;
        ToolPool.createDirectoryIfNotExist(dirPath);
        fileInfo.setPath(String.valueOf(hashedDirectory));

        return fileInfo;
    }

    /**
     * get file information by UUID
     * @param fileId UUID of file
     * @return file information
     * @throws Exception
     */
    public FileInfo getFileInfoById(String fileId) throws Exception{
        FileInfo fileInfo = null;

        ITableServiceEntity entity = tableOperator.queryEntityBySearchKey(fileTableName, StaticValues.COLUMN_ROW_KEY, fileId, FileInfo.class);
        if(entity!=null)
            fileInfo = (FileInfo)entity;

        return fileInfo;
    }

    /**
     * create actual file by inserting file information into file table, then write file stream into physical file
     * The file information get 'committed' status after its completion
     * @param type file type : blob or null
     * @param fileIn input stream of a file
     * @param fileName original file name
     * @param userName user ID
     * @return full path of file
     * @throws Exception
     */
    public String createFile(String type, InputStream fileIn, String fileName, String userName) throws Exception{
        String rtnVal = null;
        FileOutputStream fileOut = null;
        try {
            FileInfo fileInfo = this.createFileRecord(fileName, userName);

            String newFilePath = ToolPool.buildFilePath(fileStoragePath, fileInfo.getPath(), fileInfo.getName());
            fileOut = new FileOutputStream(newFilePath);

            int readBytes = 0;
            int readBlockSize = 4 * 1024 * 1024;
            byte[] buffer = new byte[readBlockSize];
            while ((readBytes = fileIn.read(buffer, 0, readBlockSize)) != -1) {
                fileOut.write(buffer, 0, readBytes);
            }

            fileOut.close();
            fileOut = null;
            fileIn.close();
            fileIn = null;

            if (type != null && !type.isEmpty()) {
                if(type.equals("blob")) {
                    fileInfo.setContainer("files");
                } else if(type.equals("tool")) {
                    fileInfo.setContainer("tools");
                }

                BlobOperator blobOperator = new BlobOperator(conf);
                boolean uploaded = blobOperator.uploadFileToBlob(fileInfo.getContainer(), fileInfo.getName(), newFilePath, fileInfo.getType(), false);
                //boolean uploaded = storageServices.uploadFileToBlob(fileInfo, newFilePath, false);

                if(!uploaded) {
                    File file = new File(newFilePath);
                    if(file.exists())
                        file.delete();
                    throw new Exception("FileTool:Failed to upload file to Blob storage.");
                }
            }

            fileInfo.setStatus(StaticValues.FILE_STATUS_COMMITTED);
            boolean recordInserted = this.insertFileRecord(fileInfo);
            if(recordInserted)
                rtnVal = fileInfo.getIuuid();
        } catch(Exception ex) {
            throw ex;
        } finally {
            if(fileIn!=null)
                fileIn.close();
            if(fileOut!=null)
                fileOut.close();
        }
        return rtnVal;
    }

    /**
     * insert file information into file table
     * @param fileinfo file information
     * @return boolean result
     * @throws Exception
     */
    public boolean insertFileRecord(FileInfo fileinfo) throws Exception {
        return tableOperator.insertEntity(fileTableName, fileinfo);
    }

    /**
     * update file status to 'committed' after confirming the exist of physical file
     * @param fileId file UUID
     * @return boolean result
     * @throws Exception
     */
    public boolean commitFileRecord(String fileId) throws Exception {
        boolean rtnVal = false;
        FileInfo fileInfo = getFileInfoById(fileId);
        if(fileInfo!=null) {
            fileInfo.setStatus(StaticValues.FILE_STATUS_COMMITTED);
            rtnVal = tableOperator.updateEntity(fileTableName, fileInfo);
        } else {
            throw new Exception("File does not exist.");
        }
        return rtnVal;
    }

    public boolean deleteFileRecord(String fileId) throws Exception {
        boolean rtnVal = false;
        FileInfo fileInfo = this.getFileInfoById(fileId);
        if(fileInfo!=null) {
            tableOperator.deleteEntity(fileTableName, fileInfo);
            rtnVal = true;
        }
        return rtnVal;
    }

    /**
     * copies a file from one location to the other
     * @param from full path of source
     * @param to full path of target
     * @return boolean result
     * @throws Exception
     */
    public boolean copy(String from, String to) throws Exception {
        boolean rtnVal = false;
        File fromFile = new File(from);
        if(fromFile.exists() && fromFile.canRead()) {
            FileUtils.copyFile(fromFile, new File(to));
            rtnVal = true;
        }
        return rtnVal;
    }

    /**
     * copy a file from file storage area to different directory ie. working directory
     * @param path full path of source
     * @param to full path of target
     * @return boolean result
     * @throws Exception
     */
    public boolean copyFromDatastore(String path, String to) throws Exception {
        return this.copy(ToolPool.buildFilePath(fileStoragePath, path), to);
    }

    /**
     * get full path of a file with its UUID
     * @param fileId file UUID
     * @return full path of a file
     * @throws Exception
     */
    public String getFilePath(String fileId) throws Exception {
        String filePath;
        FileInfo fileInfo = this.getFileInfoById(fileId);
        if(fileInfo==null)
            throw new Exception("File record does not exist!");
        else
            filePath = ToolPool.buildFilePath(fileStoragePath, fileInfo.getPath(), fileInfo.getName());

        return filePath;
    }

    /**
     * delete actual file by UUID as well as record in file table
     * @param fileId file UUID
     * @param userName user ID
     * @return boolean result
     * @throws Exception
     */
    public boolean delete(String fileId, String userName) throws Exception {
        boolean rtnVal = false;
        try {
            FileInfo fileInfo = this.getFileInfoById(fileId);

            if(fileInfo!=null) {
                if(!userName.equals(fileInfo.getUserId()))
                    throw new Exception("The file belongs to another user");

                this.deleteFileRecord(fileId);

                String filePath = ToolPool.buildFilePath(fileStoragePath, fileInfo.getPath(), fileInfo.getName());
                File file = new File(filePath);
                if(file.exists())
                    rtnVal = file.delete();
            }
        } catch(Exception ex) {
            throw ex;
        }

        return rtnVal;
    }

    /**
     * get list of files that belong to given user
     * @param userName user ID
     * @return list of files information objects
     * @throws Exception
     */
    public List<FileInfo> getFileList(String userName) throws Exception {
        List<FileInfo> files = null;
        List<ITableServiceEntity> fileList = tableOperator.queryListBySearchKey(fileTableName, StaticValues.COLUMN_USER_ID, userName, null, null, FileInfo.class);
        if(fileList!=null && fileList.size()>0) {
            files = new ArrayList<FileInfo>();
            for(ITableServiceEntity entity : fileList) {
                files.add((FileInfo)entity);
            }
        }
        return files;
    }
}

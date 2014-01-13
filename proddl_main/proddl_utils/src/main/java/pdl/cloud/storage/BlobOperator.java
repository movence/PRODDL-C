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

package pdl.cloud.storage;

import org.soyatec.windowsazure.blob.BlobStorageClient;
import org.soyatec.windowsazure.blob.IBlobContainer;
import org.soyatec.windowsazure.blob.IBlockBlob;
import org.soyatec.windowsazure.blob.internal.BlobContents;
import org.soyatec.windowsazure.blob.internal.BlobProperties;
import org.soyatec.windowsazure.blob.internal.ContainerAccessControl;
import org.soyatec.windowsazure.blob.io.BlobFileStream;
import org.soyatec.windowsazure.blob.io.BlobMemoryStream;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.error.StorageServerException;
import org.soyatec.windowsazure.internal.util.NameValueCollection;
import pdl.utils.Configuration;
import pdl.utils.StaticValues;
import pdl.utils.ToolPool;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 11/7/11
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class BlobOperator {
    private Configuration conf;

    private BlobStorageClient blobStorageClient;

    ContainerAccessControl publicAccessACL = new ContainerAccessControl(true);

    public BlobOperator() {
        conf = Configuration.getInstance();
    }

    public BlobOperator(Configuration conf) {
        this.conf = conf;
    }

    private void initBlobClient() throws Exception {
        blobStorageClient = BlobStorageClient.create(
                URI.create(StaticValues.AZURE_BLOB_HOST_NAME),
                false,
                conf.getStringProperty(StaticValues.CONFIG_KEY_CSTORAGE_NAME),
                conf.getStringProperty(StaticValues.CONFIG_KEY_CSTORAGE_PKEY)
        );
        //blobStorageClient.setRetryPolicy( RetryPolicies.retryN( 1, TimeSpan.fromSeconds( 5 ) ) );
    }

    private IBlobContainer initBlobContainer(String containerName) throws Exception {
        IBlobContainer container = null;
        if (blobStorageClient == null)
            initBlobClient();

        if (!blobStorageClient.isContainerExist(containerName))
            container = blobStorageClient.createContainer(containerName);
        else
            container = blobStorageClient.getBlobContainer(containerName);
        container.setAccessControl(publicAccessACL);

        return container;
    }

    private IBlobContainer getBlobContainer(String containerName) throws Exception {
        IBlobContainer container = null;
        container = initBlobContainer(containerName);
        if (!container.isContainerExist())
            throw new IllegalArgumentException("container does not exist. - " + containerName);
        return container;
    }

    public boolean getBlob(String containerName, String blobName, String filePath, boolean isOverwrite) {
        boolean fSuccess = false;
        try {
            if(blobName!=null && !blobName.isEmpty() && filePath!=null && !filePath.isEmpty()) {
                IBlobContainer theContainer = getBlobContainer(containerName);
                if (!theContainer.isBlobExist(blobName))
                    throw new Exception("blob not found - " + blobName);

                File blobFile = new File(filePath);
                if(blobFile!=null && blobFile.exists()) {
                    if(isOverwrite) {
                        blobFile.delete();
                    }
                }

                BlobFileStream objStream = new BlobFileStream(filePath);

                IBlockBlob blob = theContainer.getBlockBlobReference(blobName);
                blob.getContents(objStream);

                blobFile = new File(filePath);
                if(blobFile==null || !blobFile.exists()) {
                    throw new Exception("filed to get blob - " + blobName);
                }

                objStream.close();

                fSuccess = true;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fSuccess;
    }

    public boolean download(String containerName, String fileName, String storagePath) {
        boolean rtnVal = false;
        try {
            String filePath = ToolPool.buildFilePath(storagePath, fileName);
            File file = new File(filePath);
            if (!file.exists() || !file.canRead()) {
                getBlob(containerName, fileName, filePath, true);
            }
            rtnVal = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnVal;
    }

    public boolean uploadBlob(String containerName, String blobName, String fileName, byte[] fileBytes, String fileType, boolean overWrite) throws Exception{
        boolean rtnVal = false;
        try {
            IBlobContainer theContainer = getBlobContainer(containerName);
            if (theContainer.isBlobExist(blobName) && !overWrite)
                throw new Exception(String.format("Blob '%s' Already Exists! Cannot overwrite.", blobName));

            if (null == fileBytes || fileBytes.length == 0)
                throw new Exception("File Binary is null or empty");

            BlobProperties blobProperties = new BlobProperties(blobName);
            blobProperties.setContentType(fileType);

            NameValueCollection blobMetaData = new NameValueCollection();
            blobMetaData.put("FileName", fileName);
            blobMetaData.put("Submitter", "Automated Encoder");
            if (null != blobMetaData && (blobMetaData instanceof NameValueCollection))
                blobProperties.setMetadata(blobMetaData);

            BlobMemoryStream blobStream = new BlobMemoryStream(fileBytes);
            BlobContents blobContents = new BlobContents(blobStream);

            theContainer.createBlockBlob(blobProperties, blobContents);
            if (!theContainer.isBlobExist(blobName))
                throw new Exception(String.format("Fails to create Blob '%s'!", blobName));

            rtnVal = true;
        } catch (StorageServerException ex) {
            ex.printStackTrace();
        } catch (StorageException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            throw ex;
        }

        return rtnVal;
    }

    public boolean uploadFileToBlob(String container, String blobName, String filePath, String fileType, boolean overWrite) throws Exception {
        boolean rtnVal = false;
        try {
            File uploadingFile = new File(filePath);
            if (!uploadingFile.exists())
                throw new Exception(String.format("File '%s' was not found!", filePath));

            BlobFileStream fileStream = new BlobFileStream(uploadingFile);
            rtnVal = this.uploadBlob(
                    container,
                    blobName,
                    uploadingFile.getName(),
                    fileStream.getBytes(),
                    fileType,
                    overWrite);
        } catch (Exception ex) {
            throw ex;
        }

        return rtnVal;
    }

    public boolean deleteBlob(String containerName, String blobName) {
        boolean rtnVal = false;
        try {
            IBlobContainer theContainer = getBlobContainer(containerName);
            if (!theContainer.isBlobExist(blobName))
                throw new Exception("Blob " + blobName + " does not exist in Container: " + containerName + "!");

            rtnVal = theContainer.deleteBlob(blobName);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return rtnVal;
    }
}

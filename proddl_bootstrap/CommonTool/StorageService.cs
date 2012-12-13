/*
 * 
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
 *
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Diagnostics;

using Microsoft.WindowsAzure;
using Microsoft.WindowsAzure.StorageClient;
using Microsoft.WindowsAzure.ServiceRuntime;

namespace CommonTool
{
    public class StorageService
    {
        public CloudStorageAccount _account { get; set; }

        private CloudDrive _cloudDrive { get; set; }
        public String _cloudDrivePath { get; set; }

        public StorageService(String connectionString)
        {
            _account = CloudStorageAccount.Parse(connectionString);
        }

        public void downloadBlob()
        {

        }

        private Boolean mountDriveByUri(String driveUri)
        {
            Boolean rtnVal = false;
            try
            {
                /* Drive initialization */
                LocalResource localStorage = RoleEnvironment.GetLocalResource("LocalStorage");
                int cacheSize = int.Parse(RoleEnvironment.GetConfigurationSettingValue(SharedTools.KEY_VHD_SIZE)) + 100;
                CloudDrive.InitializeCache(Path.Combine(Path.GetPathRoot(localStorage.RootPath) + "drivecache"), cacheSize);
                _cloudDrive = _account.CreateCloudDrive(driveUri);
                _cloudDrivePath = _cloudDrive.Mount(cacheSize, DriveMountOptions.None);
                rtnVal = true;
            }
            catch (Exception ex)
            {
                Trace.TraceError("mountMasterDrive() - " + ex.ToString());
            }

            return rtnVal;
        }

        public void unMountCloudDrive()
        {
            try
            {
                if (_cloudDrive != null && !String.IsNullOrEmpty(_cloudDrivePath))
                    _cloudDrive.Unmount();
            }
            catch (Exception ex)
            {
                Trace.TraceError("unMountMasterDrive() - " + ex.ToString());
            }
        }

        public String getMountedDrivePath(String driveUri)
        {
            IDictionary<String, Uri> listDrives = CloudDrive.GetMountedDrives();
            if ((listDrives == null || listDrives.Count == 0) || _cloudDrivePath == null)
            {
                this.mountDriveByUri(driveUri);
            }
            return _cloudDrivePath;
        }

        public Boolean uploadCloudDrive(string filePath, String bloblContainer, String vhdName)
        {
            int PageBlobPageSize = 512;
            int OneMegabyteAsBytes = 1024 * 1024;
            int FourMegabytesAsBytes = 4 * OneMegabyteAsBytes;

            try
            {
                FileInfo vhdFileInfo = new FileInfo(filePath);

                CloudBlobContainer cloudBlobContainer = _account.CreateCloudBlobClient().GetContainerReference(bloblContainer);
                cloudBlobContainer.CreateIfNotExist();
                CloudPageBlob cloudPageBlob = cloudBlobContainer.GetPageBlobReference(vhdName);
                cloudPageBlob.Properties.ContentType = "binary/octet-stream";

                long blobSize = (vhdFileInfo.Length + PageBlobPageSize - 1) & ~(PageBlobPageSize - 1);
                cloudPageBlob.Create(blobSize);

                FileStream stream = new FileStream(filePath, FileMode.Open, FileAccess.Read);
                BinaryReader reader = new BinaryReader(stream);

                long totalUploaded = 0;
                long vhdOffset = 0;
                int offsetToTransfer = -1;

                while (vhdOffset < vhdFileInfo.Length)
                {
                    byte[] range = reader.ReadBytes(FourMegabytesAsBytes);

                    int offsetInRange = 0;

                    // Make sure end is page size aligned
                    if ((range.Length % PageBlobPageSize) > 0)
                    {
                        int grow = (int)(PageBlobPageSize - (range.Length % PageBlobPageSize));
                        Array.Resize(ref range, range.Length + grow);
                    }

                    // Upload groups of contiguous non-zero page blob pages.  
                    while (offsetInRange <= range.Length)
                    {
                        if ((offsetInRange == range.Length) || IsAllZero(range, offsetInRange, PageBlobPageSize))
                        {
                            if (offsetToTransfer != -1)
                            {
                                // Transfer up to this point
                                int sizeToTransfer = offsetInRange - offsetToTransfer;
                                MemoryStream memoryStream = new MemoryStream(range, offsetToTransfer, sizeToTransfer, false, false);
                                cloudPageBlob.WritePages(memoryStream, vhdOffset + offsetToTransfer);
                                totalUploaded += sizeToTransfer;
                                offsetToTransfer = -1;
                            }
                        }
                        else
                        {
                            if (offsetToTransfer == -1)
                            {
                                offsetToTransfer = offsetInRange;
                            }
                        }
                        offsetInRange += PageBlobPageSize;
                    }
                    vhdOffset += range.Length;
                }
                Trace.TraceInformation("uploadCloudDrive() DONE with upload vhd file at " + filePath);
                return true;
            }
            catch (Exception ex)
            {
                Trace.TraceError(ex.Message);
                return false;
            }
        }
        private static bool IsAllZero(byte[] range, long rangeOffset, long size)
        {
            for (long offset = 0; offset < size; offset++)
            {
                if (range[rangeOffset + offset] != 0)
                {
                    return false;
                }
            }
            return true;
        }

        public void deleteDiagnosticsTables(String connectionString)
        {
            try
            {
                CloudStorageAccount diagnosticsAccount = CloudStorageAccount.Parse(connectionString);
                CloudTableClient tableClient = diagnosticsAccount.CreateCloudTableClient();
                tableClient.DeleteTableIfExist("WADLogsTable");
                tableClient.DeleteTableIfExist("WADPerformanceCountersTable");
            }
            catch (Exception ex)
            {
                Trace.TraceError(ex.Message);
            }
        }

        public void uploadLogToBlob(String blobName, String deploymentId, String identifier, String logPath)
        {
            try {
                CloudBlobContainer cloudBlobContainer = _account.CreateCloudBlobClient().GetContainerReference(blobName);
                cloudBlobContainer.CreateIfNotExist();
                CloudBlob blob = cloudBlobContainer.GetBlobReference(deploymentId+"_"+identifier+".log");
                blob.UploadFile(logPath);
            }
            catch(Exception ex) 
            {
                Trace.TraceError(ex.Message);
            }
        }

    }
}

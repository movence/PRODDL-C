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
using System.Diagnostics;
using System.Threading;
using System.IO;

using CommonTool;
using CommonTool.data;
using Microsoft.WindowsAzure.StorageClient;
using Microsoft.WindowsAzure.ServiceRuntime;
using System.Net;
using Microsoft.WindowsAzure.Diagnostics;

namespace PRODDLMaster
{
    class MasterHelper
    {
        private StorageService storageHelper;
        private InfoServiceContext _infoContext;
        private PerformanceDataServiceContext _perfServiceContext;
        private String localStoragePath;
        private string logPath;

        private const String INFOS_TABLE_DRIVE_KEY_NAME = "MasterDriveIntialized";
        private const String INFOS_TABLE_CATALOG_ADDRESS = "CatalogServerAddress";
        private const String INFOS_TABLE_CATALOG_PORT = "CatalogServerPort";

        private readonly string roleToolsPath;

        public MasterHelper(string logPath) {
            this.logPath = logPath;
            roleToolsPath = Path.Combine(Environment.GetEnvironmentVariable("RoleRoot"), "approot", SharedTools.ROLE_DIRECTORY_TOOLS);
        }

        private String[] INFOS_TABLE_KEYS = 
        { 
            INFOS_TABLE_CATALOG_ADDRESS, 
            INFOS_TABLE_CATALOG_PORT
        };

        public void OnStop()
        {
            try
            {
                if (storageHelper != null)
                {
                    storageHelper.unMountCloudDrive();
                    //Delete diagnositcs tables
                    //storageHelper.deleteDiagnosticsTables(RoleEnvironment.GetConfigurationSettingValue("StorageConnectionString"));
                    
                    //Delete peprformance data for current deployemt
                    _perfServiceContext.deleteDataByDeploymentId(RoleEnvironment.DeploymentId);

                    //Clean up Dynamic Data table except azure drive information
                    this.deleteInfos();

                    Trace.Flush();
                    Trace.Close();
                    storageHelper.uploadLogToBlob(
                        "logs",
                        RoleEnvironment.DeploymentId,
                        "master",
                        logPath
                    );
                }
            }
            catch (Exception ex)
            {
                Trace.TraceError(ex.ToString());
            }
        }

        public void Run()
        {
            LocalResource localStorage = RoleEnvironment.GetLocalResource("LocalStorage");
            localStoragePath = Path.GetPathRoot(localStorage.RootPath);

            storageHelper = new StorageService(RoleEnvironment.GetConfigurationSettingValue("StorageConnectionString"));
            initializeTableContext();

            string vhdName = RoleEnvironment.GetConfigurationSettingValue("VHDName");

            if (!this.IsMasterDriveExist())
            {
                String vhdFilePath = createDriveFromCMD(vhdName);
                if (!String.IsNullOrEmpty(vhdFilePath))
                {
                    if (storageHelper.uploadCloudDrive(vhdFilePath, SharedTools.BLOB_CONTAINER_NAME, vhdName))
                    {
                        _infoContext.insertInfoData("proddl_info", INFOS_TABLE_DRIVE_KEY_NAME, "1");
                        File.Delete(@vhdFilePath);
                    }
                }
                else
                {
                    Trace.TraceError("failed to get vhd file.");
                }
            }

            String drivePath = storageHelper.getMountedDrivePath(String.Format("{0}/{1}", SharedTools.BLOB_CONTAINER_NAME, vhdName));
            if (drivePath == null)
            {
                throw new Exception("Failed to mount Azure Drive for master node.");
            }

            String webServerPort = RoleEnvironment.CurrentRoleInstance.InstanceEndpoints["HttpIn"].IPEndpoint.Port.ToString();
            IPEndPoint internalAddress = RoleEnvironment.CurrentRoleInstance.InstanceEndpoints["CatalogServer"].IPEndpoint;

            //create property file for java application
            Dictionary<string, string> properties = new Dictionary<string, string>();
            properties.Add(SharedTools.KEY_SUBSCRIPTION_ID, RoleEnvironment.GetConfigurationSettingValue(SharedTools.KEY_SUBSCRIPTION_ID));
            properties.Add(SharedTools.KEY_WORKER_NAME, SharedTools.KEY_WORKER_NAME_VALUE);
            properties.Add(SharedTools.KEY_MASTER_INSTANCE, "true");
            properties.Add(SharedTools.KEY_WEBSERVER_PORT, webServerPort);
            properties.Add(SharedTools.KEY_INTERNAL_ADDR, internalAddress.Address.ToString());
            properties.Add(SharedTools.KEY_INTERNAL_PORT, internalAddress.Port.ToString());
            properties.Add(SharedTools.KEY_DATASTORE_PATH, drivePath+Path.DirectorySeparatorChar);
            properties.Add(SharedTools.KEY_ROLE_TOOLS_PATH, roleToolsPath);
            SharedTools.createPropertyFile(properties);

            SharedTools.startJavaMainOperator("[Master]", localStoragePath);
        }

        private void initializeTableContext()
        {
            try
            {
                _infoContext = new InfoServiceContext(
                    storageHelper._account.TableEndpoint.ToString(), 
                    storageHelper._account.Credentials
                    );
                _infoContext.RetryPolicy = RetryPolicies.Retry(3, TimeSpan.FromSeconds(3));

                storageHelper._account.CreateCloudTableClient().CreateTableIfNotExist(_infoContext.getInfosTableName());

                _perfServiceContext = new PerformanceDataServiceContext(
                    storageHelper._account.TableEndpoint.ToString(),
                    storageHelper._account.Credentials
                    );
                _perfServiceContext.RetryPolicy = RetryPolicies.Retry(3, TimeSpan.FromSeconds(3));
            }
            catch (Exception ex)
            {
                Trace.TraceError("initializeTableClient() - " + ex.ToString());
            }
        }

        private Boolean IsMasterDriveExist()
        {
            InfoModel driveData = _infoContext.getInfoData(INFOS_TABLE_DRIVE_KEY_NAME);
            if (driveData != null && !String.IsNullOrEmpty(driveData.iKey))
            {
                if (driveData.iValue.Equals("1"))
                    return true;
            }
            return false;
        }

        private void deleteInfos()
        {
            for (int i = 0; i < INFOS_TABLE_KEYS.Length; i++)
            {
                _infoContext.deleteInfoData(INFOS_TABLE_KEYS[i]);
            }
        }

        private String createDriveFromCMD(string vhdName)
        {
            string vhdFilePath = null;
            try
            {
                vhdFilePath = Path.Combine(localStoragePath, vhdName);
                String scriptPath = createVHDScriptFile(localStoragePath, vhdFilePath);

                Process proc = SharedTools.buildCloudProcessWithError(
                    System.Environment.GetEnvironmentVariable("WINDIR") + "\\System32\\diskpart.exe",
                    "/s" + " " + scriptPath,
                    "createDriveFromCMD()");

                proc.Start();
                proc.BeginErrorReadLine();
                proc.WaitForExit();

                Trace.WriteLine("DONE: createDriveFromCMD()", this.ToString());
            }
            catch (Exception ex)
            {
                Trace.TraceError("createDriveFromCMD() - " + ex.ToString());
                vhdFilePath = null;
            }
            return vhdFilePath;
        }

        private String createVHDScriptFile(String resourcePath, String vhdPath)
        {
            String scriptPath = Path.Combine(resourcePath, "vhd.txt");
            TextWriter tw = new StreamWriter(scriptPath);
            tw.WriteLine(String.Format("create vdisk file={0} type=fixed maximum={1}", vhdPath, RoleEnvironment.GetConfigurationSettingValue("VHDSize")));
            tw.WriteLine(String.Format("select vdisk file={0}", vhdPath));
            tw.WriteLine("attach vdisk");
            tw.WriteLine("create partition primary");
            tw.WriteLine("format fs=ntfs label=vhd quick");
            tw.WriteLine("assign letter=v");
            tw.WriteLine("detach vdisk");
            tw.Close();

            return scriptPath;
        }
    }
}

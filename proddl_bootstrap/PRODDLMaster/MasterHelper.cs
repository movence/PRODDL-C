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
        private DynamicDataServiceContext _dynamicDataContext;
        private PerformanceDataServiceContext _perfServiceContext;
        private String localStoragePath;
        private string logPath;

        private const String DYNAMIC_TABLE_DRIVE_KEY_NAME = "MasterDriveIntialized";
        private const String DYNAMIC_TABLE_CATALOG_ADDRESS = "CatalogServerAddress";
        private const String DYNAMIC_TABLE_CATALOG_PORT = "CatalogServerPort";

        public MasterHelper(string logPath) {
            this.logPath = logPath;
        }

        private String[] DYNAMIC_TABLE_KEYS = 
        { 
            DYNAMIC_TABLE_CATALOG_ADDRESS, 
            DYNAMIC_TABLE_CATALOG_PORT
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
                    deleteDynamicData();

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

            string vhdName = RoleEnvironment.GetConfigurationSettingValue("VHDName");
            string blobContainer = RoleEnvironment.GetConfigurationSettingValue("BlobContainer");

            if (!this.IsMasterDriveExist())
            {
                String vhdFilePath = createDriveFromCMD(vhdName);
                if (!String.IsNullOrEmpty(vhdFilePath))
                {
                    if (storageHelper.uploadCloudDrive(vhdFilePath, blobContainer, vhdName))
                    {
                        this.updateMasterDriveData(DYNAMIC_TABLE_DRIVE_KEY_NAME, "true");
                        File.Delete(vhdFilePath);
                    }
                }
                else
                {
                    Trace.TraceError("master.vhd does not exist");
                }
            }
            else
            {
                Trace.WriteLine("Azure drive file already exist");
            }

            String drivePath = storageHelper.getMountedDrivePath(String.Format("{0}/{1}", blobContainer, vhdName));
            //this.updateMasterDriveData(DYNAMIC_TABLE_DRIVE_PATH, drivePath);

            String webServerPort = RoleEnvironment.CurrentRoleInstance.InstanceEndpoints["HttpIn"].IPEndpoint.Port.ToString();
            IPEndPoint internalAddress = RoleEnvironment.CurrentRoleInstance.InstanceEndpoints["CatalogServer"].IPEndpoint;

            Dictionary<string, string> properties = new Dictionary<string, string>();
            properties.Add(SharedTools.KEY_SUBSCRIPTION_ID, RoleEnvironment.GetConfigurationSettingValue(SharedTools.KEY_SUBSCRIPTION_ID));
            properties.Add(SharedTools.KEY_WORKER_NAME, SharedTools.KEY_WORKER_NAME_VALUE);
            /*
            properties.Add(SharedTools.KEY_CERTIFICATE_NAME, RoleEnvironment.GetConfigurationSettingValue(SharedTools.KEY_CERTIFICATE_NAME));
            properties.Add(SharedTools.KEY_CERT_PASSWORD, RoleEnvironment.GetConfigurationSettingValue(SharedTools.KEY_CERT_PASSWORD));
            properties.Add(SharedTools.KEY_CERT_ALIAS, RoleEnvironment.GetConfigurationSettingValue(SharedTools.KEY_CERT_ALIAS));
            */
            properties.Add(SharedTools.KEY_MASTER_INSTANCE, "true");
            properties.Add(SharedTools.KEY_WEBSERVER_PORT, webServerPort);
            properties.Add(SharedTools.KEY_INTERNAL_ADDR, internalAddress.Address.ToString());
            properties.Add(SharedTools.KEY_INTERNAL_PORT, internalAddress.Port.ToString());
            properties.Add(SharedTools.KEY_DATASTORE_PATH, drivePath+Path.DirectorySeparatorChar);

            SharedTools.createPropertyFile(properties);
            SharedTools.startJavaMainOperator("[Master]", localStoragePath);
            //startJavaMainOperator(); //this call never return, it's on JAVA's hand now
        }

        private void initializeTableContext()
        {
            try
            {
                _dynamicDataContext = new DynamicDataServiceContext(
                    storageHelper._account.TableEndpoint.ToString(), 
                    storageHelper._account.Credentials
                    );
                _dynamicDataContext.RetryPolicy = RetryPolicies.Retry(3, TimeSpan.FromSeconds(3));

                storageHelper._account.CreateCloudTableClient().CreateTableIfNotExist(_dynamicDataContext.getDynamicTableName());

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
            if (_dynamicDataContext == null)
                initializeTableContext();

            DynamicDataModel driveData = _dynamicDataContext.getDynamicData(DYNAMIC_TABLE_DRIVE_KEY_NAME);
            if (driveData != null && !String.IsNullOrEmpty(driveData.dataValue))
            {
                if (driveData.dataValue.Equals("true"))
                    return true;
            }
            return false;
        }

        private void updateMasterDriveData(String key, String value)
        {
            if (_dynamicDataContext == null)
                initializeTableContext();
            _dynamicDataContext.insertDynamicData("dynamicdata_masterdrive", key, value);
        }

        private void deleteDynamicData()
        {
            if (_dynamicDataContext == null)
                initializeTableContext();

            for (int i = 0; i < DYNAMIC_TABLE_KEYS.Length; i++)
            {
                _dynamicDataContext.deleteDynamicData(DYNAMIC_TABLE_KEYS[i]);
            }
        }

        private String createDriveFromCMD(string vhdName)
        {
            try
            {
                string vhdFilePath = Path.Combine(localStoragePath, vhdName);
                String scriptPath = createVHDScriptFile(localStoragePath, vhdFilePath);

                Process proc = SharedTools.buildCloudProcessWithError(
                    System.Environment.GetEnvironmentVariable("WINDIR") + "\\System32\\diskpart.exe",
                    "/s" + " " + scriptPath,
                    "createDriveFromCMD()");

                proc.Start();
                proc.BeginErrorReadLine();
                proc.WaitForExit();

                Trace.WriteLine("DONE: createDriveFromCMD()");
                return vhdFilePath;
            }
            catch (Exception ex)
            {
                Trace.TraceError("createDriveFromCMD() - " + ex.ToString());
            }
            return null;
        }

        private String createVHDScriptFile(String resourcePath, String vhdPath)
        {
            String scriptPath = Path.Combine(resourcePath, @"vhd.txt");
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

        private bool startJavaMainOperator()
        {
            try
            {
                Trace.WriteLine("START: startJavaMainOperator()");

                String jettyPort = RoleEnvironment.CurrentRoleInstance.InstanceEndpoints["HttpIn"].IPEndpoint.Port.ToString();
                String deploymentId = RoleEnvironment.DeploymentId;
                IPEndPoint internalAddress = RoleEnvironment.CurrentRoleInstance.InstanceEndpoints["CatalogServer"].IPEndpoint;
                String roleRoot = Environment.GetEnvironmentVariable("RoleRoot");

                String jarPath = roleRoot + @"\approot\tools\proddl_core-1.0.jar";
                String jreHome = localStoragePath + @"jre";

                Process proc = SharedTools.buildCloudProcess(
                    String.Format("\"{0}\\bin\\java.exe\"", jreHome),
                    String.Format(
                        "-jar {0} {1} {2} {3} {4} {5} {6}",
                        jarPath, "true", localStoragePath, internalAddress.Address, internalAddress.Port, jettyPort, RoleEnvironment.DeploymentId
                    ),
                    "[Master]"
                );

                proc.Start();
                proc.BeginOutputReadLine();
                proc.BeginErrorReadLine();
                proc.WaitForExit();

                Trace.WriteLine("DONE: startJavaMainOperator()");
                return true;
            }
            catch (Exception ex)
            {
                Trace.TraceError("EXCEPTION: startJavaMainOperator() - " + ex.ToString());
                return false;
            }
        }
    }
}

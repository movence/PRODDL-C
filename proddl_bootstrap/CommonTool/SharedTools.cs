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
using System.IO;

using SevenZip;

using Microsoft.WindowsAzure.ServiceRuntime;

namespace CommonTool
{
    public class SharedTools
    {
        //readonly variables for properties
        public static readonly string KEY_SUBSCRIPTION_ID = "SubscriptionId";
        public static readonly string KEY_DEPLOYMENT_NAME = "DeploymentName";
        public static readonly string KEY_DEPLOYMENT_ID = "DeploymentId";

        public static readonly string KEY_STORAGE_NAME = "StorageAccountName";
        public static readonly string KEY_STORAGE_PKEY = "StorageAccountPkey";
        public static readonly string KEY_STORAGE_PATH = "StoragePath";
        public static readonly string KEY_DATASTORE_PATH = "DataStorePath";
        public static readonly string KEY_ROLE_TOOLS_PATH = "RoleToolsPath";

        public static readonly string KEY_WORKER_NAME = "CloudRoleWorkerName";
        public static readonly string KEY_WORKER_NAME_VALUE = "PRODDLJobRunner";

        public static readonly string KEY_CERTIFICATE_NAME = "CertificateName";
        public static readonly string KEY_CERT_PASSWORD = "CertificatePassword";
        public static readonly string KEY_CERT_ALIAS = "CertificateAlias";

        public static readonly string KEY_MASTER_INSTANCE = "IsMaster";
        public static readonly string KEY_WEBSERVER_PORT = "WebserverPort";
        public static readonly string KEY_INTERNAL_ADDR = "InternalAddress";
        public static readonly string KEY_INTERNAL_PORT = "InternalPort";

        public static readonly string KEY_VHD_SIZE = "VHDSize";
        public static readonly string KEY_VHD_NAME = "VHDName";

        public static readonly string BLOB_CONTAINER_NAME = "tools";
        public static readonly string ROLE_DIRECTORY_TOOLS = "tools";

        public static Process buildCloudProcess(String fileName, String args, String outputTag)
        {
            Process proc = buildCloudProcessWithError(fileName, args, outputTag);
            proc.StartInfo.RedirectStandardOutput = true;
            proc.OutputDataReceived += (sender, e) => { if (e.Data != null) Trace.WriteLine(outputTag + ":OUTPUT>> " + e.Data); };
            return proc;
        }

        public static Process buildCloudProcessWithError(String fileName, String args, String outputTag)
        {
            Process proc = new Process();
            proc.StartInfo.RedirectStandardError = true;
            proc.StartInfo.ErrorDialog = false;
            proc.StartInfo.CreateNoWindow = true;
            proc.StartInfo.UseShellExecute = false;
            proc.EnableRaisingEvents = false;
            proc.ErrorDataReceived += (sender, e) => { if (e.Data != null) Trace.WriteLine(outputTag + ":ERROR>> " + e.Data); };

            proc.StartInfo.FileName = fileName;
            proc.StartInfo.Arguments = args;

            return proc;
        }

        public static bool extractZipFile(String filePath, String extractTo)
        {
            bool rtnVal = false;

            try
            {
                string sevenZipPath = Path.Combine(Directory.GetCurrentDirectory(), ROLE_DIRECTORY_TOOLS, "7z64.dll");
                SevenZipExtractor.SetLibraryPath(sevenZipPath);
                SevenZipExtractor extractor = new SevenZipExtractor(filePath);
                extractor.ExtractArchive(extractTo);
                rtnVal = true;
            }
            catch (Exception ex)
            {
                Trace.TraceError("EXCEPTION: extractZipFile() - " + ex.ToString());
            }

            return rtnVal;
        }

        public static Dictionary<string, string> setDefaultProps()
        {
            Dictionary<string, string> props = new Dictionary<string, string>();
            props.Add(KEY_DEPLOYMENT_NAME, RoleEnvironment.GetConfigurationSettingValue(KEY_DEPLOYMENT_NAME));
            props.Add(KEY_DEPLOYMENT_ID, RoleEnvironment.DeploymentId);
            props.Add(KEY_STORAGE_PATH, Path.GetPathRoot(RoleEnvironment.GetLocalResource("LocalStorage").RootPath)+Path.DirectorySeparatorChar);

            string connectionString = RoleEnvironment.GetConfigurationSettingValue("StorageConnectionString");
            string[] pairs = connectionString.Split(';');
            foreach (string pair in pairs)
            {
                string value = pair.Substring(pair.IndexOf('=') + 1);
                if (pair.Contains("AccountKey="))
                {
                    props.Add(SharedTools.KEY_STORAGE_PKEY, value);
                }
                else if (pair.Contains("AccountName="))
                {
                    props.Add(SharedTools.KEY_STORAGE_NAME, value);
                }
            }
            return props;
        }

        public static bool createPropertyFile(Dictionary<string, string> kvd)
        {
            bool rtnVal = false;

            try
            {
                Dictionary<string, string> props = setDefaultProps();
                props = props.Concat(kvd).ToDictionary(kvp=>kvp.Key, kvp=>kvp.Value);

                List<string> lines = new List<string>(kvd.Count);
                foreach (KeyValuePair<string, string> pair in props)
                {
                    lines.Add(pair.Key + "=" + pair.Value);
                }
                File.WriteAllLines(Path.Combine(Directory.GetCurrentDirectory(), ROLE_DIRECTORY_TOOLS, "proddl.properties"), lines.ToArray<string>());
                rtnVal = true;
            }
            catch (Exception ex)
            {
                Trace.TraceError("EXCEPTION: createPropertyFile() - " + ex.ToString());
            }

            return rtnVal;
        }

        public static bool extractJRE(String storagePath)
        {
            bool rtnVal = false;

            try
            {
                extractZipFile(Path.Combine(Directory.GetCurrentDirectory(), ROLE_DIRECTORY_TOOLS, "jre6x64.zip"),storagePath);
                rtnVal = true;
            }
            catch (Exception ex)
            {
                Trace.TraceError("EXCEPTION: extractJRE() - " + ex.ToString());
            }

            return rtnVal;
        }

        public static bool startJavaMainOperator(String tag, String storagePath)
        {
            bool rtnVal = false;

            try
            {
                extractJRE(storagePath);

                string toolsDir = Path.Combine(Directory.GetCurrentDirectory(), ROLE_DIRECTORY_TOOLS);
                string classPath = toolsDir + Path.PathSeparator + Path.Combine(toolsDir, "proddl_core-1.0.jar");
                Process proc = buildCloudProcess(
                    Path.Combine(storagePath + @"jre\bin\java.exe"),
                    String.Format("-cp {0} {1}", classPath, "pdl.operator.ServiceOperator"),
                    tag
                );

                proc.Start();
                proc.BeginOutputReadLine();
                proc.BeginErrorReadLine();
                proc.WaitForExit();

                rtnVal = true;
            }
            catch (Exception ex)
            {
                Trace.TraceError("EXCEPTION: startJavaMainOperator() - " + ex.ToString());
            }

            return rtnVal;
        }
    }
}

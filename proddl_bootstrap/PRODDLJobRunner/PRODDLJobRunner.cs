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
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Threading;
using System.IO;

using Microsoft.WindowsAzure;
using Microsoft.WindowsAzure.Diagnostics;
using Microsoft.WindowsAzure.ServiceRuntime;
using Microsoft.WindowsAzure.StorageClient;

using CommonTool;

namespace PRODDLJobRunner
{
    class PRODDLJobRunnerMain : RoleEntryPoint
    {
        public String localStoragePath;

        public override void Run()
        {
            try
            {
                LocalResource localStorage = RoleEnvironment.GetLocalResource("LocalStorage");
                localStoragePath = Path.GetPathRoot(localStorage.RootPath);

                extractJRE();
                startJavaMainOperator();
            }
            catch (Exception ex)
            {
                Trace.TraceError(ex.Message);
            }

            //while (true)
            //{
            //   Thread.Sleep(10000);
            //}
        }

        public override bool OnStart()
        {
            // Set the maximum number of concurrent connections 
            ServicePointManager.DefaultConnectionLimit = 12;
            
            DiagnosticMonitorConfiguration dmc = DiagnosticMonitor.GetDefaultInitialConfiguration();

            dmc.Logs.ScheduledTransferPeriod = TimeSpan.FromMinutes(3.0);
            dmc.Logs.ScheduledTransferLogLevelFilter = LogLevel.Verbose;

            PerformanceCounterConfiguration pcc = new PerformanceCounterConfiguration();
            pcc.CounterSpecifier = @"\Processor(_Total)\% Processor Time";
            pcc.SampleRate = System.TimeSpan.FromSeconds(30);
            dmc.PerformanceCounters.DataSources.Add(pcc);
            dmc.PerformanceCounters.ScheduledTransferPeriod = TimeSpan.FromMinutes(3.0);

            DiagnosticMonitor.AllowInsecureRemoteConnections = true;
            //DiagnosticMonitor.Start("DiagnosticConnectionString", dmc);
            DiagnosticMonitor.Start("StorageConnectionString", dmc);


            RoleEnvironment.Changing += RoleEnvironmentChanging;

            CloudStorageAccount.SetConfigurationSettingPublisher((configName, configSetter) =>
            {
                configSetter(RoleEnvironment.GetConfigurationSettingValue(configName));
                RoleEnvironment.Changed += (sender, arg) =>
                {
                    if (arg.Changes.OfType<RoleEnvironmentConfigurationSettingChange>()
                        .Any((change) => (change.ConfigurationSettingName == configName)))
                    {
                        if (!configSetter(RoleEnvironment.GetConfigurationSettingValue(configName)))
                        {
                            RoleEnvironment.RequestRecycle();
                        }
                    }
                };
            });

            return base.OnStart();
        }

        private void RoleEnvironmentChanging(object sender, RoleEnvironmentChangingEventArgs e)
        {
            // If a configuration setting is changing
            if (e.Changes.Any(change => change is RoleEnvironmentConfigurationSettingChange))
            {
                // Set e.Cancel to true to restart this role instance
                e.Cancel = true;
            }
        }

        private bool extractJRE()
        {
            try
            {
                new SharedTools().extractZipFile(Path.Combine(Directory.GetCurrentDirectory(), @"tools\jre6x64.zip"), localStoragePath);

                Trace.TraceInformation("DONE: extractJRE()");
                return true;
            }
            catch (Exception ex)
            {
                Trace.TraceError("EXCEPTION: extractJRE() - " + ex.ToString());
                return false;
            }
        }

        private bool startJavaMainOperator()
        {
            try
            {
                Trace.Write("START: startJavaMainOperator()");

                string roleRoot = Environment.GetEnvironmentVariable("RoleRoot");

                string jarPath = roleRoot + @"\approot\tools\proddl_core-1.0.jar";
                string jreHome = localStoragePath + @"jre";

                Process proc = new SharedTools().buildCloudProcess(
                    String.Format("\"{0}\\bin\\java.exe\"", jreHome),
                    String.Format("-jar {0} {1} {2} {3} {4} {5} {6}", jarPath, "false", localStoragePath, "-", "-", "-", RoleEnvironment.DeploymentId),
                    "JobRunner - Java Main Operator");

                proc.Start();
                proc.BeginOutputReadLine();
                proc.BeginErrorReadLine();
                proc.WaitForExit();

                Trace.Write("DONE: startJavaMainOperator()");
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

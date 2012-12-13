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
        private string localStoragePath;
        private string logPath;

        public override void Run()
        {
            try
            {
                //check configuration status before doing anything
                while (true)
                {
                    string c_finalized = RoleEnvironment.GetConfigurationSettingValue("ConfigurationFinalized");
                    if (c_finalized != null & "1".Equals(c_finalized))
                        break;
                    else
                        Thread.Sleep(6 * 60 * 60 * 1000); //sleeps for 6 hours until user inputs configuration data 
                }

                DiagnosticMonitorConfiguration dmc = DiagnosticMonitor.GetDefaultInitialConfiguration();
                PerformanceCounterConfiguration pcc = new PerformanceCounterConfiguration();
                pcc.CounterSpecifier = @"\Processor(_Total)\% Processor Time";
                pcc.SampleRate = System.TimeSpan.FromSeconds(30);
                dmc.PerformanceCounters.DataSources.Add(pcc);
                dmc.PerformanceCounters.ScheduledTransferPeriod = TimeSpan.FromMinutes(3.0);

                //DiagnosticMonitor.AllowInsecureRemoteConnections = true;
                DiagnosticMonitor.Start("StorageConnectionString", dmc);

                SharedTools.createPropertyFile(new Dictionary<string,string>());
                SharedTools.startJavaMainOperator("[Worker]", localStoragePath);
            }
            catch (Exception ex)
            {
                Trace.TraceError(ex.Message);
            }

            //while (true)
            //{
            //   Thread.Sleep(10000);4
            //}
        }

        public override bool OnStart()
        {
            //ServicePointManager.UseNagleAlgorithm = false;
            ServicePointManager.UseNagleAlgorithm = false;

            LocalResource localStorage = RoleEnvironment.GetLocalResource("LocalStorage");
            localStoragePath = Path.GetPathRoot(localStorage.RootPath);
            logPath = Path.Combine(localStoragePath, "trace.log");
            
            Trace.Listeners.Clear();
            TextWriterTraceListener twtl = new TextWriterTraceListener(
                logPath,
                "TextLogger");
            twtl.TraceOutputOptions = TraceOptions.ThreadId | TraceOptions.DateTime;
            ConsoleTraceListener ctl = new ConsoleTraceListener(false);
            ctl.TraceOutputOptions = TraceOptions.DateTime;
            ctl.Name = "ConsoleLogger";
            Trace.Listeners.Add(twtl);
            Trace.Listeners.Add(ctl);
            Trace.AutoFlush = true;

            /*
            dmc.Logs.ScheduledTransferPeriod = TimeSpan.FromMinutes(3.0);
            dmc.Logs.ScheduledTransferLogLevelFilter = LogLevel.Verbose;
            */

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
            if (e.Changes.Any(change => change is RoleEnvironmentConfigurationSettingChange))
            {
                e.Cancel = true;
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

                Process proc = SharedTools.buildCloudProcess(
                    String.Format("\"{0}\\bin\\java.exe\"", jreHome),
                    String.Format("-jar {0} {1} {2} {3} {4} {5} {6}", jarPath, "false", localStoragePath, "-", "-", "-", RoleEnvironment.DeploymentId),
                    "[JobRunner]");

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

        public override void OnStop()
        {
            Trace.Flush();
            Trace.Close();
            StorageService storageHelper = new StorageService(RoleEnvironment.GetConfigurationSettingValue("StorageConnectionString"));
            storageHelper.uploadLogToBlob(
                "logs",
                RoleEnvironment.DeploymentId,
                "worker",
                logPath
            );
            base.OnStop();
        }
    }
}

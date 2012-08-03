﻿/*
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

using Microsoft.WindowsAzure;
using Microsoft.WindowsAzure.Diagnostics;
using Microsoft.WindowsAzure.ServiceRuntime;
using System.IO;

namespace PRODDLMaster
{
	class MasterMain : RoleEntryPoint
	{
		MasterHelper helper;
		public override void Run()
		{
			helper = new MasterHelper();
			helper.Run();
		}

		public override bool OnStart()
		{
			// Set the maximum number of concurrent connections 
			ServicePointManager.DefaultConnectionLimit = 10;
            ServicePointManager.MaxServicePointIdleTime = 2500;
            //ServicePointManager.UseNagleAlgorithm = false;

            Trace.Listeners.Clear();
            TextWriterTraceListener twtl = new TextWriterTraceListener(
                Path.Combine(RoleEnvironment.GetLocalResource("LocalStorage").RootPath, "trace.log"),
                "TextLogger");
            twtl.TraceOutputOptions = TraceOptions.ThreadId | TraceOptions.DateTime;
            ConsoleTraceListener ctl = new ConsoleTraceListener(false);
            ctl.TraceOutputOptions = TraceOptions.DateTime;
            ctl.Name = "ConsoleLogger";
            Trace.Listeners.Add(twtl);
            Trace.Listeners.Add(ctl);
            Trace.AutoFlush = true;
			
            /*
			DiagnosticMonitorConfiguration dmc = DiagnosticMonitor.GetDefaultInitialConfiguration();

			dmc.Logs.ScheduledTransferPeriod = TimeSpan.FromMinutes(1.0);
			dmc.Logs.ScheduledTransferLogLevelFilter = LogLevel.Verbose;

			DiagnosticMonitor.AllowInsecureRemoteConnections = true;
			//DiagnosticMonitor.Start("DiagnosticConnectionString", dmc);
            DiagnosticMonitor.Start("StorageConnectionString", dmc);
            */

			RoleEnvironment.Changing += RoleEnvironmentChanging;
			CloudStorageAccount.SetConfigurationSettingPublisher((configName, configSetter) =>
			{
				configSetter(RoleEnvironment.GetConfigurationSettingValue(configName));
				RoleEnvironment.Changed += (sender, arg) =>
				{
					if (arg.Changes.OfType<RoleEnvironmentConfigurationSettingChange>().Any((change) => (change.ConfigurationSettingName == configName)))
					{
						if (!configSetter(RoleEnvironment.GetConfigurationSettingValue(configName)))
						{
							RoleEnvironment.RequestRecycle();
						}
					}
				};
			});

            //RoleEnvironment.Stopping += RoleEnvironmentStopping;

            //application temp directory for Jetty
            string customTempLocalResourcePath = RoleEnvironment.GetLocalResource("TempStorage").RootPath;
            Environment.SetEnvironmentVariable("TMP", customTempLocalResourcePath);
            Environment.SetEnvironmentVariable("TEMP", customTempLocalResourcePath);

			return base.OnStart();
		}

		private void RoleEnvironmentChanging(object sender, RoleEnvironmentChangingEventArgs e)
		{
			if (e.Changes.Any(change => change is RoleEnvironmentConfigurationSettingChange))
			{
				e.Cancel = true;
			}
		}

        /*
        private void RoleEnvironmentStopping(object sender, RoleEnvironmentStoppingEventArgs e)
        {
            helper.Stopping();
        }
        */

		public override void OnStop()
		{
			helper.OnStop();
			base.OnStop();
		}
	}
}

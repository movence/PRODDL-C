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
using System.Linq;
using System.Text;

using Microsoft.WindowsAzure.StorageClient;
using Microsoft.WindowsAzure;

namespace CommonTool.data
{
    public class PerformanceDataServiceContext : TableServiceContext
    {
        public PerformanceDataServiceContext(string baseAddress, StorageCredentials credentials) : base(baseAddress, credentials) {}

        private static readonly String PerformanceCountersTableName = "WADPerformanceCountersTable";

        public IQueryable<PerformanceDataModel> PerformanceCounters
        {
            get
            {
                return CreateQuery<PerformanceDataModel>(PerformanceCountersTableName);
            }
        }

        public void deleteDataByDeploymentId(string deploymentId)
        {
            List<PerformanceDataModel> perfDataList = (from d in this.PerformanceCounters
                                              where d.DeploymentId == deploymentId
                                              select d).ToList();
            foreach(PerformanceDataModel tempData in perfDataList) {
                this.DeleteObject(tempData);
            }
            this.SaveChanges();
        }

    }
}

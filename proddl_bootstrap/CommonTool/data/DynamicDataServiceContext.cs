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
using Microsoft.WindowsAzure.StorageClient;
using Microsoft.WindowsAzure;
using Microsoft.WindowsAzure.ServiceRuntime;

namespace CommonTool.data
{
    public class DynamicDataServiceContext : TableServiceContext
    {
        private static readonly String dynamicDataTableName = "dynamicData"+RoleEnvironment.GetConfigurationSettingValue("DeploymentName");

        public DynamicDataServiceContext(String baseAddress, StorageCredentials credentials) : base(baseAddress, credentials) { }

        public IQueryable<DynamicDataModel> DynamicDataTable
        {
            get
            {
                return CreateQuery<DynamicDataModel>(dynamicDataTableName);
            }
        }

        public String getDynamicTableName()
        {
            return dynamicDataTableName;
        }
        
        public void insertDynamicData(String partitionKey, String key, String value)
        {
            DynamicDataModel data = new DynamicDataModel(partitionKey);
            data.dataKey = key;
            data.dataValue = value;

            this.AddObject(dynamicDataTableName, data);
            this.SaveChanges();
        }

        public DynamicDataModel getDynamicData(String key)
        {
            try
            {

                DynamicDataModel driveData = (from d in this.DynamicDataTable
                                              where d.dataKey == key
                                              select d).FirstOrDefault();
                return driveData;
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.ToString());
            }
            return null;
        }

        public void deleteDynamicData(String key)
        {
            try
            {
                DynamicDataModel data = this.getDynamicData(key);
                this.IgnoreResourceNotFoundException = true;
                this.DeleteObject(data);
                this.SaveChanges();
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.ToString());
            }
        }
    }
}

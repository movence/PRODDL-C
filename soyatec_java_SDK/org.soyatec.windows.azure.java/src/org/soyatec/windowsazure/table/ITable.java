/**
 * Copyright  2006-2010 Soyatec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */
package org.soyatec.windowsazure.table;

import java.net.URI;
import java.sql.Timestamp;
import java.util.List;

import org.soyatec.windowsazure.blob.IRetryPolicy;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.internal.util.TimeSpan;
import org.soyatec.windowsazure.table.internal.CloudTable;
import org.soyatec.windowsazure.table.internal.CloudTableQuery;
import org.soyatec.windowsazure.table.internal.SimpleTableServiceEntity;

/**
 * API entry point for using structured storage.</p>
 *
 * The underlying usage pattern is designed to be similar to the one used in
 * blob and queue services in this library.</p>
 *
 *
 */
public interface ITable {

	/**
	 * Creates a new table in the service
	 *
	 */
	public boolean createTable();

	/**
	 * Checks whether a table with the same name already exists.
	 *
	 * @return True if the table already exists.
	 */
	public boolean isTableExist();

	/**
	 * Deletes a table from the service. </p>
	 *
	 * When a table is successfully deleted, it is immediately marked for
	 * deletion and is no longer accessible to clients. The table is later
	 * removed from the Table service during <strong>garbage
	 * collection.</strong></p>
	 *
	 * Note that deleting a table is likely to take at least 40 seconds to
	 * complete. If an operation is attempted against the table while it was
	 * being deleted, the service returns status code 409 (Conflict), with
	 * additional error information indicating that the table is being
	 * deleted.</p>
	 *
	 * @param tableName
	 *            The name of the table to be deleted
	 */
	public boolean deleteTable();


	/**
	 * Create a ICloudTableColumn.
	 *
	 * @param name
	 * @param value
	 * @param type
	 * @return
	 */
	public ICloudTableColumn createCloudTableColumn(String name, String value, ETableColumnType type) ;

	/**
	 * Create a ITableServiceEntity with partitionKey, rowKey and a list of ICloudTableColumn
	 *
	 * @param partitionKey
	 * @param rowKey
	 * @param values
	 * @return
	 */
	public ITableServiceEntity createTableServiceEntity(String partitionKey, String rowKey, List<ICloudTableColumn> values) ;

	/**
	 * Create a ITableServiceEntity with partitionKey, rowKey and timestamp
	 *
	 * @param partitionKey
	 * @param rowKey
	 * @param timestamp
	 * @return
	 */
	public ITableServiceEntity createTableServiceEntity(String partitionKey, String rowKey, Timestamp timestamp);

	/**
	 * Get the base uri of the table service
	 *
	 * @return
	 */
	public URI getBaseUri();

	/**
	 * Get the name of the storage account
	 *
	 * @return
	 */
	public String getAccountName();

	/**
	 * Get the name of specified table.
	 *
	 * @return
	 */
	public String getTableName();

	/**
	 * Whether use/generate path-style or host-style URIs
	 *
	 * @return
	 */
	public boolean isUsePathStyleUris();

	/**
	 * Get the timeout per requeet
	 *
	 * @return
	 */
	public TimeSpan getTimeout();

	/**
	 * Get the retry policy used for retrying requests
	 *
	 * @return
	 */
	public IRetryPolicy getRetryPolicy();

	/**
	 * Set timeout per request
	 *
	 * @param timeout
	 */
	public void setTimeout(TimeSpan timeout);

	/**
	 * Set the {@link IRetryPolicy} userd for retrying requests
	 *
	 * @param retryPolicy
	 */
	public void setRetryPolicy(IRetryPolicy retryPolicy);


	public TableServiceContext getTableServiceContext();

//	/**
//	 * Get the last status of CloudTable
//	 * @return the last status of CloudTable
//	 */
//	public String getLastStatus();

}
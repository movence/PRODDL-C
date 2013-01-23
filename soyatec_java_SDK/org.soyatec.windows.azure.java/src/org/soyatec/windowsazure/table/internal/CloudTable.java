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
package org.soyatec.windowsazure.table.internal;

import java.net.URI;
import java.util.List;

import org.soyatec.windowsazure.blob.IRetryPolicy;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.internal.util.TimeSpan;
import org.soyatec.windowsazure.table.AbstractTableServiceEntity;
import org.soyatec.windowsazure.table.IBatchExecutor;
import org.soyatec.windowsazure.table.ITable;
import org.soyatec.windowsazure.table.ITableServiceEntity;

/**
 * API entry point for using structured storage.</p>
 * 
 * The underlying usage pattern is designed to be similar to the one used in
 * blob and queue services in this library.</p>
 * 
 * <h5>Batch operations</h5>
 * 
 * The {@link CloudTable} implements the {@link IBatchExecutor}. Some TRANSCATION
 * supported operations can be enlist a single batch operation. Example:
 * 
 * <pre>
 *      SampleEntity sampleEntity1 = createSampleEntity();
 * 		SampleEntity sampleEntity2 = createSampleEntity();
 * 		try {
 * 			table.startBatch();
 * 			table.insertEntity(sampleEntity1);
 * 			table.insertEntity(sampleEntity2);
 * 			table.executeBatch();
 * 	    catch(Exception e){
 * 	    	//
 * 	    }
 * </pre>
 * 
 * For more detail about <strong>Batch operation</strong>, see
 * {@link IBatchExecutor}.
 * 
 */
public abstract class CloudTable implements ITable {

	/**
	 * The base uri of the table service
	 */
	private URI baseUri;
	
	/**
	 * The name of storage account
	 */
	private String accountName;

	/**
	 * TThe name of the specified table
	 */
	protected String tableName;

	/**
	 * Indicates whether to use/generate path-style or host-style URIs
	 */
	private boolean usePathStyleUris;

	/**
	 * The time out for each request to the storage service.
	 */
	TimeSpan timeout;

	/**
	 * The retry policy used for retrying requests
	 */
	private IRetryPolicy retryPolicy;

	/**
	 * Default, azure table entities are retrieved as the instances of class
	 * {@link SimpleTableServiceEntity}. A list of
	 * {@link SimpleTableServiceEntity} is return when retrieve entities from
	 * table service.</p>
	 * 
	 * <strong>Model</strong> class can be changed to specified table service to
	 * represent more <strong>Model</strong> details. </p>
	 * 
	 * <strong>Model</strong> class must be subclass of
	 * {@link AbstractTableServiceEntity}.
	 */
	private Class<? extends ITableServiceEntity> modelClass;

	/**
	 * Create a new azure table.
	 * 
	 * It't not make a call to service. Subclass extends in RESTful style call
	 * this to create a new azure table instance.
	 * 
	 * @param baseUri
	 *            The base uri of the table service.
	 * 
	 * @param usePathStyleUris
	 *            Indicates whether to use/generate path-style or host-style
	 *            URIs
	 * @param accountName
	 *            The name of storage account.
	 * @param tableName
	 *            The name of the specified table.
	 * 
	 * @param base64Key
	 *            Authentication key used for signing requests.
	 * @param timeout
	 *            The time out for each request to the storage service.
	 * @param retryPolicy
	 *            The retry policy used for retrying requests.
	 */
	protected CloudTable(URI baseUri, boolean usePathStyleUris,
			String accountName, String tableName, String base64Key,
			TimeSpan timeout, IRetryPolicy retryPolicy) {
		this.baseUri = baseUri;
		this.usePathStyleUris = usePathStyleUris;
		this.accountName = accountName;
		this.tableName = tableName;
		this.timeout = timeout;
		this.retryPolicy = retryPolicy;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#createTable()
	 */
	public abstract boolean createTable();

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#isTableExist()
	 */
	public abstract boolean isTableExist();

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#deleteTable()
	 */
	public abstract boolean deleteTable();

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#insertEntity(org.soyatec.windowsazure.table.ITableServiceEntity)
	 */
	public abstract void insertEntity(ITableServiceEntity obj)
			throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#updateEntity(org.soyatec.windowsazure.table.ITableServiceEntity)
	 */
	public abstract void updateEntity(ITableServiceEntity obj)
			throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#updateEntityIfNotModified(org.soyatec.windowsazure.table.ITableServiceEntity)
	 */
	public abstract void updateEntityIfNotModified(ITableServiceEntity obj)
			throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#mergeEntity(org.soyatec.windowsazure.table.ITableServiceEntity)
	 */
	public abstract void mergeEntity(ITableServiceEntity obj)
			throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#deleteEntity(org.soyatec.windowsazure.table.ITableServiceEntity)
	 */
	public abstract void deleteEntity(ITableServiceEntity obj)
			throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#deleteEntityIfNotModified(org.soyatec.windowsazure.table.ITableServiceEntity)
	 */
	public abstract void deleteEntityIfNotModified(ITableServiceEntity obj)
			throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#loadEntity(T)
	 */
	public abstract <T extends ITableServiceEntity> T loadEntity(T entity)
			throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#retrieveEntities(java.lang.String)
	 */
	public abstract List<ITableServiceEntity> retrieveEntities(
			String queryExpression,Class<? extends ITableServiceEntity> modelClass) throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#retrieveEntities(org.soyatec.windowsazure.table.internal.CloudTableQuery)
	 */
	public abstract List<ITableServiceEntity> retrieveEntities(final CloudTableQuery query,Class<? extends ITableServiceEntity> modelClass)
			throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#retrieveEntities()
	 */
	public abstract List<ITableServiceEntity> retrieveEntities(Class<? extends ITableServiceEntity> modelClass)
			throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#retrieveEntitiesByKey(java.lang.String, java.lang.String)
	 */
	public abstract List<ITableServiceEntity> retrieveEntitiesByKey(
			String partitionKey, String rowKey,Class<? extends ITableServiceEntity> modelClass) throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#getBaseUri()
	 */
	public URI getBaseUri() {
		return baseUri;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#getAccountName()
	 */
	public String getAccountName() {
		return accountName;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#getTableName()
	 */
	public String getTableName() {
		return tableName;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#isUsePathStyleUris()
	 */
	public boolean isUsePathStyleUris() {
		return usePathStyleUris;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#getTimeout()
	 */
	public TimeSpan getTimeout() {
		return timeout;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#getRetryPolicy()
	 */
	public IRetryPolicy getRetryPolicy() {
		return retryPolicy;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#setTimeout(org.soyatec.windowsazure.util.TimeSpan)
	 */
	public void setTimeout(TimeSpan timeout) {
		this.timeout = timeout;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#setRetryPolicy(org.soyatec.windowsazure.blob.RetryPolicy)
	 */
	public void setRetryPolicy(IRetryPolicy retryPolicy) {
		this.retryPolicy = retryPolicy;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#getModelClass()
	 */
	public Class<? extends ITableServiceEntity> getModelClass() {
		return modelClass;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#setModelClass(java.lang.Class)
	 */
	public void setModelClass(Class<? extends ITableServiceEntity> modelClass) {
		this.modelClass = modelClass;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ICloudTable#getLastStatus()
	 */
	public abstract String getLastStatus();
}

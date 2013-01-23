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
import java.util.List;

import org.soyatec.windowsazure.authenticate.SharedKeyCredentials;
import org.soyatec.windowsazure.authenticate.StorageAccountInfo;
import org.soyatec.windowsazure.blob.IRetryPolicy;
import org.soyatec.windowsazure.blob.internal.RetryPolicies;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.internal.constants.StandardPortalEndpoints;
import org.soyatec.windowsazure.internal.util.TimeSpan;
import org.soyatec.windowsazure.proxy.AbstractProxyDelegate;
import org.soyatec.windowsazure.table.internal.TableStorageRest;

/**
 * API entry point for using structured storage. The underlying usage pattern is
 * designed to be similar to the one used in blob and queue services in this
 * library. Users create a TableStorage object by calling the static Create()
 * method passing account credential information to this method.
 */
public abstract class TableStorageClient extends AbstractProxyDelegate {

	/**
	 * Indicates whether to use/generate path-style or host-style URIs
	 */
	private boolean usePathStyleUris;

	/**
	 * The base URI of the table storage service
	 */
	private URI baseUri;

	/**
	 * The name of the storage account
	 */
	private String accountName;

	private String base64Key;

	/**
	 * The retry policy used for retrying requests
	 */
	private IRetryPolicy retryPolicy;

	/**
	 * The time out for each request to the storage service.
	 */
	private TimeSpan timeout;

	private SharedKeyCredentials credentials;

	/**
	 * The default retry policy
	 */
	public static final IRetryPolicy DefaultRetryPolicy = RetryPolicies
			.noRetry();

	/**
	 * The default timeout
	 */
	public static final TimeSpan DefaultTimeout = TimeSpan.fromSeconds(30);

	protected TableStorageClient(URI baseUri, boolean usePathStyleUris,
			String accountName, String base64Key) {
		this.baseUri = baseUri;
		this.usePathStyleUris = usePathStyleUris;
		this.accountName = accountName;
		this.base64Key = base64Key;
		this.retryPolicy = DefaultRetryPolicy;
		this.timeout = DefaultTimeout;
	}

	/**
	 * Check if the table exists.
	 *
	 * @param tableName
	 *            of the Table.
	 * @return true if the Table exists, false otherwise.
	 * @throws StorageException
	 */
	public abstract boolean isTableExist(String tableName)	throws StorageException;

	/**
	 * Creates a TableStorage service object. This object is the entry point
	 * into the table storage API.
	 *
	 * @param baseUri
	 *            The base URI of the blob storage service
	 * @param usePathStyleUris
	 *            If true, path-style URIs
	 *            (http://baseuri/accountname/containername/objectname) are
	 *            used. If false host-style URIs
	 *            (http://accountname.baseuri/containername/objectname) are
	 *            used, where baseuri is the URI of the service. If null, the
	 *            choice is made automatically: path-style URIs if host name
	 *            part of base URI is an IP addres, host-style otherwise.
	 * @param accountName
	 *            The name of the storage account
	 * @param base64Key
	 *            Authentication key used for signing requests
	 * @return A newly created TableStorage instance
	 */
	public static TableStorageClient create(URI baseUri, boolean usePathStyleUris,
			String accountName, String base64Key) {
		return new TableStorageRest(baseUri, usePathStyleUris, accountName,
				base64Key);
	}

	/**
	 * Creates a Table object.
	 *
	 * @param tableName
	 *            The name of the table.
	 * @return
	 */
	public abstract void createTable(String tableName);

	/**
	 * Creates a Table object.
	 *
	 * @param tableName
	 *            The name of the table.
	 * @return true: if table was created / false: otherwise
	 */
	public abstract boolean createTableIfNotExist(String tableName);

	/**
	 * Delete a table if exist.
	 *
	 * @param tableName
	 *            The name of the table.
	 * @return true: delete success / false: otherwise
	 */
	public abstract boolean deleteTableIfExist(String tableName);

	/**
	 * Delete a table.
	 *
	 * @param tableName
	 *            The name of the table.
	 * @return
	 */
	public abstract void deleteTable(String tableName);

	/**
	 * Creates a TableStorage service object. This object is the entry point
	 * into the table storage API.
	 *
	 * @param usePathStyleUris
	 *            If true, path-style URIs
	 *            (http://baseuri/accountname/containername/objectname) are
	 *            used. If false host-style URIs
	 *            (http://accountname.baseuri/containername/objectname) are
	 *            used, where baseuri is the URI of the service. If null, the
	 *            choice is made automatically: path-style URIs if host name
	 *            part of base URI is an IP addres, host-style otherwise.
	 * @param accountName
	 *            The name of the storage account
	 * @param base64Key
	 *            Authentication key used for signing requests
	 * @return A newly created TableStorage instance
	 */
	public static TableStorageClient create(boolean usePathStyleUris,
			String accountName, String base64Key) {
		URI hostUri = null;
		if (usePathStyleUris) {
			hostUri = URI.create(StandardPortalEndpoints.HttpProtocolPrefix
					+ StandardPortalEndpoints.DevTableEndpoint);
		} else {
			hostUri = URI.create(StandardPortalEndpoints.HttpProtocolPrefix
					+ StandardPortalEndpoints.TableStorageEndpoint);
		}
		return new TableStorageRest(hostUri, usePathStyleUris, accountName,
				base64Key);
	}

	/**
	 * Creates a TableStorage service object. This object is the entry point
	 * into the table storage API.
	 *
	 * @param info
	 *            {@link StorageAccountInfo}
	 * @return A newly created TableStorage instance
	 */
	public static TableStorageClient create(StorageAccountInfo info) {
		return TableStorageClient.create(info.getBaseUri(),
				info.isUsePathStyleUris(), info.getAccountName(), info
						.getBase64Key());
	}



	/**
	 * Lists all the tables under this service's URL
	 */
	public abstract List<String> listTables() throws StorageException;

	/**
	 * Lists all the tables with the prefix under this service's URL
	 *
	 * @param prefix
	 */
	public abstract List<String> listTables(String prefix) throws StorageException;

	/**
	 * Get a reference to a Table object with a specified name. The method
	 * does not make call to a table service.
	 *
	 * @param tableName
	 *            The name of the table
	 * @return A newly created table object
	 */
	public abstract ITable getTableReference(String tableName);


	/**
	 * Indicates whether to use/generate path-style or host-style URIs
	 *
	 * @return
	 */
	public boolean isUsePathStyleUris() {
		return usePathStyleUris;
	}

	/**
	 * Get The base URI of the table storage service
	 *
	 * @return
	 */
	public URI getBaseUri() {
		return baseUri;
	}

	/**
	 * Get the name of storage account
	 *
	 * @return
	 */
	public String getAccountName() {
		return accountName;
	}

	/**
	 * Get the authenticate key for of storage account
	 *
	 * @return
	 */
	public String getBase64Key() {
		return base64Key;
	}

	/**
	 * Set the authenticate key for of storage account
	 *
	 * @param base64Key
	 */
	public void setBase64Key(String base64Key) {
		this.base64Key = base64Key;
	}

	/**
	 * Get the {@link IRetryPolicy}
	 *
	 * @return
	 */
	public IRetryPolicy getRetryPolicy() {
		return retryPolicy;
	}

	/**
	 * Set the {@link IRetryPolicy}
	 *
	 * @param retryPolicy
	 */
	public void setRetryPolicy(IRetryPolicy retryPolicy) {
		this.retryPolicy = retryPolicy;
	}

	/**
	 * Get time out per request
	 *
	 * @return
	 */
	public TimeSpan getTimeout() {
		return timeout;
	}

	/**
	 * Set timeout per request
	 *
	 * @param timeout
	 */
	public void setTimeout(TimeSpan timeout) {
		this.timeout = timeout;
	}

	/**
	 * Set the credential
	 *
	 * @return
	 */
	public SharedKeyCredentials getCredentials() {
		return credentials;
	}

	/**
	 * Get credential
	 *
	 * @param credentials
	 */
	public void setCredentials(SharedKeyCredentials credentials) {
		this.credentials = credentials;
	}

}

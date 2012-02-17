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
package org.soyatec.windowsazure.blob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.soyatec.windowsazure.authenticate.StorageAccountInfo;
import org.soyatec.windowsazure.blob.internal.BlobContents;
import org.soyatec.windowsazure.blob.internal.BlobProperties;
import org.soyatec.windowsazure.blob.internal.BlobStorageRest;
import org.soyatec.windowsazure.blob.internal.RetryPolicies;
import org.soyatec.windowsazure.blob.io.BlobFileStream;
import org.soyatec.windowsazure.blob.io.BlobStream;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.error.StorageServerException;
import org.soyatec.windowsazure.internal.constants.StandardPortalEndpoints;
import org.soyatec.windowsazure.internal.util.NameValueCollection;
import org.soyatec.windowsazure.internal.util.TimeSpan;
import org.soyatec.windowsazure.proxy.AbstractProxyDelegate;

/**
 * The entry point of the blob storage API
 * 
 */
public abstract class BlobStorageClient extends AbstractProxyDelegate{
	/**
	 * Indicates whether to use/generate path-style or host-style URIs
	 */
	private final boolean usePathStyleUris;
	/**
	 * The base URI of the blob storage service
	 */
	private final URI baseUri;
	/**
	 * The name of the storage account
	 */
	private final String accountName;

	/**
	 * Authentication key used for signing requests
	 */
	protected String base64Key;

	/**
	 * The time out for each request to the storage service.
	 */
	private TimeSpan timeout;
	/**
	 * The retry policy used for retrying requests
	 */
	private IRetryPolicy retryPolicy;

	/**
	 * The default timeout
	 */
	public static final TimeSpan DefaultTimeout = TimeSpan.fromSeconds(360);

	/**
	 * The default retry policy
	 */
	public static final IRetryPolicy DefaultRetryPolicy = RetryPolicies.retryN(
			5, TimeSpan.fromMilliseconds(500));

	/**
	 * Factory method for BlobStorageClient
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
	 * @return A newly created BlobStorage instance
	 */
	public static BlobStorageClient create(URI baseUri, boolean usePathStyleUris,
			String accountName, String base64Key) {
		return new BlobStorageRest(baseUri, usePathStyleUris, accountName,
				base64Key);
	}

	/**
	 * Convenient way to create a new BlobStorageClient.
	 * 
	 * @param usePathStyleUris
	 * 			  If true, path-style URIs
	 *            (http://baseuri/accountname/containername/objectname) are
	 *            used. If false host-style URIs
	 *            (http://accountname.baseuri/containername/objectname) are
	 *            used, where baseuri is the URI of the service. If null, the
	 *            choice is made automatically: path-style URIs if host name
	 *            part of base URI is an IP addres, host-style otherwise.
	 * @param accountName
	 * 			  The name of the storage account
	 * @param base64key
	 * 			  Authentication key used for signing requests
	 * @return A newly created BlobStorageClient instance
	 */
	public static BlobStorageClient create(boolean usePathStyleUris,
			String accountName, String base64key) {
		URI hostUri = null;
		if (usePathStyleUris) {
			hostUri = URI.create(StandardPortalEndpoints.HttpProtocolPrefix
					+ StandardPortalEndpoints.DevBlobEndpoint);
		} else {
			hostUri = URI.create(StandardPortalEndpoints.HttpProtocolPrefix
					+ StandardPortalEndpoints.BlobStorageEndpoint);
		}
		return new BlobStorageRest(hostUri, usePathStyleUris, accountName,
				base64key);
	}

	/**
	 * Factory method for BlobStorageClient
	 * 
	 * @param accountInfo
	 *            Account information
	 * @return A newly created BlobStorageClient instance
	 */
	public static BlobStorageClient create(StorageAccountInfo accountInfo) {
		return new BlobStorageRest(accountInfo.getBaseUri(), accountInfo
				.isUsePathStyleUris(), accountInfo.getAccountName(),
				accountInfo.getBase64Key());
	}

	/**
	 * Create a IBlobProperties for given Blob name
	 * 
	 * @param blobName
	 * 			The name of the BLOB.
	 * @return A newly created BlobProperties instance
	 */
	static public IBlobProperties createBlobProperties(String blobName) {
		return new BlobProperties(blobName);
	}
	
	/**
	 * Create a IBlobContents from File
	 * 
	 * @param file
	 * @return A newly created BlobContents instance
	 * @throws IOException
	 */
	static public IBlobContents createBlobContents(File file) throws IOException {
		return new BlobContents(new BlobFileStream(file));
	}

	/**
	 * Create a IBlobContents from direct values
	 * 
	 * @param value
	 * 			A byte array.
	 * @return A newly created BlobContents instance
	 * @throws IOException
	 */
	static public IBlobContents createBlobContents(byte[] value) {
		return new BlobContents(value);
	}

	/**
	 * Create a IBlobContents from InputStream
	 * 
	 * @param stream
	 * 			inputStream.
	 * @return A newly created BlobContents instance
	 * @throws IOException
	 */
	static public IBlobContents createBlobContents(InputStream stream) throws IOException {
		return new BlobContents(stream);
	}

	/**
	 * Create a IBlobContents from BlobStream
	 * 
	 * @param stream
	 * 			blobStream
	 * @return A newly created BlobContents instance
	 * @throws IOException
	 */
	static public IBlobContents createBlobContents(BlobStream stream) {
		return new BlobContents(stream);
	}

	/**
	 * Check if the blob container exists
	 * 
	 * @param containerName
	 *            of the BLOB.
	 * @return true if the blob exists, false otherwise.
	 * @throws StorageException
	 */
	public abstract boolean isContainerExist(String containerName)	throws StorageException;

	/**
	 * Create the container with the specified metadata and access control if it
	 * does not exist. Metadata Names are case-insensitive. If two or more
	 * headers with the same name are submitted for a resource, the headers will
	 * be combined into a single header with a comma delimiting each value.The
	 * total size of the metadata, including both the name and value together,
	 * may not exceed 8 KB in size.
	 * 
	 * @param containerName
	 *            The name of the container
	 * @param metadata
	 *            The metadata for the container. Can be null to indicate no
	 *            metadata
	 * @param accessControl
	 *            The access control (public or private) with which to create
	 *            the container
	 * @return the container that is created
	 * @throws StorageException If container already exists, it will raise exception.
	 */
	public abstract IBlobContainer createContainer(String containerName, NameValueCollection metadata,
			IContainerAccessControl accessControl) throws StorageException;
	
	/**
	 * Create the container if it does not exist. The container is created with
	 * private access control and no metadata.
	 * 
	 * @return the container that is created
	 * @throws StorageException If container already exists, it will raise exception.
	 */
	public abstract IBlobContainer createContainer(String containerName) throws StorageException;
	/**
	 * Get a reference to a newly created BlobContainer object. This method does
	 * not make any calls to the storage service.
	 * 
	 * @param containerName
	 *            The name of the container
	 * @return A reference to a newly created BlobContainer object
	 */
	public abstract IBlobContainer getBlobContainer(String containerName);

	/**
	 * Lists the containers within the account.
	 * 
	 * @return A list of containers
	 * @throws Exception
	 */
	public abstract List<IBlobContainer> listBlobContainers()
			throws StorageServerException;

	/**
	 * Create a SharedAccessUrl
	 * @param containerName 
	 * 			The name of container.
	 * @param blobName
	 * 			The name of Bolb.
	 * @param resource
	 * 			The resource type.
	 * @param permissions
	 * 			ONE = 0;READ = 1;WRITE = 2;DELETE = 4;LIST = 8
	 * @param start
	 * 			The start time.
	 * @param expiry
	 * 			The expiry time.
	 * @param identifier
	 * 			The identifier of SharedAccessUrl.
	 * @return
	 * @throws StorageServerException
	 */
	public abstract ISharedAccessUrl createSharedAccessUrl(String containerName,
			String blobName, ResourceType resource, int permissions,
			DateTime start, DateTime expiry, String identifier)
			throws StorageServerException;

	/**
	 * Deletes the container.
	 * 
	 * @return true:delete success/ false:delete failed
	 * @throws StorageException
	 */
	public abstract boolean deleteContainer(String name) throws StorageException;
	
	/**
	 * Creates a <code>BlobStorageClient</code> object with all of its fields set to
	 * the passed-in arguments.
	 * 
	 * @param baseUri
	 *            The base URI of the blob storage service
	 * @param usePathStyleUris
	 *            use/generate path-style or host-style URIs
	 * @param accountName
	 *            The name of the storage account
	 * @param base64Key
	 *            Authentication key used for signing requests
	 */
	protected BlobStorageClient(URI baseUri, boolean usePathStyleUris,
			String accountName, String base64Key) {
		this.baseUri = baseUri;
		this.accountName = accountName;
		this.base64Key = base64Key;
		this.usePathStyleUris = usePathStyleUris;
		timeout = DefaultTimeout;
		retryPolicy = DefaultRetryPolicy;
	}

	/**
	 * Indicates whether to use/generate path-style or host-style URIs.
	 * 
	 * @return true/false
	 */
	public boolean isUsePathStyleUris() {
		return usePathStyleUris;
	}

	/**
	 * @return The base URI of the blob storate service.
	 */
	public URI getBaseUri() {
		return baseUri;
	}

	/**
	 * @return The name of the storage account.
	 */
	public String getAccountName() {
		return accountName;
	}

	/**
	 * @return Authentication key used for signing requests
	 */
	public String getBase64Key() {
		return base64Key;
	}

	/**
	 * @return The time out for each request to the storage service.
	 */
	public TimeSpan getTimeout() {
		return timeout;
	}

	/**
	 * @return The retry policy used for retrying requests.
	 */
	public IRetryPolicy getRetryPolicy() {
		return retryPolicy;
	}

	/**
	 * @param base64Key
	 *            Authentication key used for signing requests
	 */
	public void setBase64Key(String base64Key) {
		this.base64Key = base64Key;
	}

	/**
	 * @param timeout
	 *            The time out for each request to the storage service.
	 */
	public void setTimeout(TimeSpan timeout) {
		this.timeout = timeout;
	}

	/**
	 * @param retryPolicy
	 *            The retry policy used for retrying requests.
	 */
	public void setRetryPolicy(IRetryPolicy retryPolicy) {
		this.retryPolicy = retryPolicy;
	}

	public abstract String getLastStatus();
}

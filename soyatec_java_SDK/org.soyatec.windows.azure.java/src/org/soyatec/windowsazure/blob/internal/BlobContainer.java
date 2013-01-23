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
package org.soyatec.windowsazure.blob.internal;

import java.net.URI;
import java.sql.Timestamp;
import java.text.MessageFormat;

import org.soyatec.windowsazure.blob.BlobStorageClient;
import org.soyatec.windowsazure.blob.IBlobContainer;
import org.soyatec.windowsazure.blob.IRetryPolicy;
import org.soyatec.windowsazure.blob.ISharedAccessUrl;
import org.soyatec.windowsazure.internal.ResourceUriComponents;
import org.soyatec.windowsazure.internal.util.HttpUtilities;
import org.soyatec.windowsazure.internal.util.NameValueCollection;
import org.soyatec.windowsazure.internal.util.TimeSpan;
import org.soyatec.windowsazure.internal.util.Utilities;

/**
 * The <code>BlobContainer</code> class is used to access and enumerate blobs in
 * the container. Storage key credentials are needed to access private blobs but
 * not for public blobs.
 *
 */
public abstract class BlobContainer implements IBlobContainer {

	private final URI baseUri;
	private final String accountName;
	private final String containerName;
	private final boolean usePathStyleUris;
	private URI containerUri;
	private Timestamp lastModifiedTime;
	private TimeSpan timeout;
	private IRetryPolicy retryPolicy;


	protected BlobContainer(URI baseUri, String accountName,
			String containerName) {
		this(baseUri, true, accountName, containerName, Utilities.minTime());
	}

	protected BlobContainer(URI baseUri, boolean usePathStyleUris,
			String accountName, String containerName) {
		this(baseUri, usePathStyleUris, accountName, containerName, Utilities
				.minTime());
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobContainer#useSharedAccessUrl(org.soyatec.windowsazure.blob.SharedAccessUrl)
	 */
	public abstract void useSharedAccessUrl(ISharedAccessUrl url);

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobContainer#clearSharedAccessUrl()
	 */
	public abstract void clearSharedAccessUrl();

	/**
	 * Use this constructor to access private blobs.
	 *
	 * @param baseUri
	 *            The base Uri for the storage endpoint
	 * @param usePathStyleUris
	 *            If true, path-style URIs
	 *            (http://baseuri/accountname/containername/objectname) are used
	 *            and if false host-style URIs
	 *            (http://accountname.baseuri/containername/objectname) are
	 *            used, where baseuri is the URI of the service
	 * @param accountName
	 *            Name of the storage account
	 * @param containerName
	 *            Name of the container
	 * @param lastModified
	 *            Date of last modification
	 */
	protected BlobContainer(URI baseUri, boolean usePathStyleUris,
			String accountName, String containerName, Timestamp lastModified) {
		if (!ROOT_CONTAINER.equals(containerName)
				&& !Utilities.isValidContainerOrQueueName(containerName)) {
			throw new IllegalArgumentException(
					MessageFormat
							.format(
									"The specified container name \"{0}\" is not valid!"
											+ "\nPlease choose a name that conforms to the naming conventions for containers!"
											+ "\nSee <a>http://msdn.microsoft.com/en-us/library/dd135715.aspx</a> for more information.",
									containerName));
		}
		this.baseUri = baseUri;
		this.usePathStyleUris = usePathStyleUris;
		this.accountName = accountName;
		this.containerName = containerName;
		this.timeout = BlobStorageClient.DefaultTimeout;
		this.retryPolicy = BlobStorageClient.DefaultRetryPolicy;
		this.lastModifiedTime = lastModified;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobContainer#getURL()
	 */
	public String getURL() {
		ResourceUriComponents uriComponents = new ResourceUriComponents(
				getAccountName(), getName(), null);
		URI uri = HttpUtilities.createRequestUri(getBaseUri(),
				isUsePathStyleUris(), getAccountName(), getName(),
				null, null, new NameValueCollection(), uriComponents);
		return uri.toString();
	}


	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobContainer#getBaseUri()
	 */
	public URI getBaseUri() {
		return baseUri;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobContainer#getAccountName()
	 */
	public String getAccountName() {
		return accountName;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobContainer#getContainerName()
	 */
	public String getName() {
		return containerName;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobContainer#isUsePathStyleUris()
	 */
	public boolean isUsePathStyleUris() {
		return usePathStyleUris;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobContainer#getContainerUri()
	 */
	public URI getUri() {
		return containerUri;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobContainer#getLastModifiedTime()
	 */
	public Timestamp getLastModifiedTime() {
		return lastModifiedTime;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobContainer#getTimeout()
	 */
	public TimeSpan getTimeout() {
		return timeout;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobContainer#getRetryPolicy()
	 */
	public IRetryPolicy getRetryPolicy() {
		return retryPolicy;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobContainer#setTimeout(org.soyatec.windowsazure.util.TimeSpan)
	 */
	public void setTimeout(TimeSpan timeout) {
		this.timeout = timeout;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobContainer#setRetryPolicy(org.soyatec.windowsazure.blob.RetryPolicy)
	 */
	public void setRetryPolicy(IRetryPolicy retryPolicy) {
		this.retryPolicy = retryPolicy;
	}

	/**
	 * @param lastModifiedTime
	 *            The timestamp for last modification of container.
	 */
	void setLastModifiedTime(Timestamp lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobContainer#setContainerUri(java.net.URI)
	 */
	public void setUri(URI containerUri) {
		this.containerUri = containerUri;
	}



//	/* (non-Javadoc)
//	 * @see org.soyatec.windowsazure.blob.IBlobContainer#getBlockSize()
//	 */
//	public long getBlockSize() {
//		return blockSize;
//	}
//
//	/* (non-Javadoc)
//	 * @see org.soyatec.windowsazure.blob.IBlobContainer#setBlockSize(long)
//	 */
//	public void setBlockSize(long blockSize) {
//		this.blockSize = blockSize;
//	}

}

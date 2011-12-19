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

import java.net.URI;
import java.sql.Timestamp;
import java.util.Iterator;

import org.soyatec.windowsazure.blob.internal.ContainerAccessControl;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.internal.util.NameValueCollection;
import org.soyatec.windowsazure.internal.util.TimeSpan;

/**
 * The <code>BlobContainer</code> class is used to access and enumerate blobs in
 * the container. Storage key credentials are needed to access private blobs but
 * not for public blobs.
 * 
 */
public interface IBlobContainer {
	
	public void setSSLProperty(String keystore, String keystorePasswd,
			String truststore, String truststorepasswd, String keyalias);

	public void clearSSLProperty();

	/**
	 * The root container.
	 */
	public static final String ROOT_CONTAINER = "$root";

	/**
	 * Use shared access URl.
	 * 
	 * @param url
	 */
	public void useSharedAccessUrl(ISharedAccessUrl url);

	/**
	 * Clear shared access url. User need to call explicitly.
	 */
	public void clearSharedAccessUrl();

	/**
	 * @param The
	 *            metadata for the container to set
	 * @param metadata
	 *            The metadata for the container. Can be null to indicate no
	 *            metadata
	 * @throws StorageException
	 */
	public void setMetadata(final NameValueCollection metadata)
			throws StorageException;

	/**
	 * Check if the blob container exists
	 * 
	 * @return True if the container exists, false otherwise.
	 * @throws StorageException
	 */
	public boolean isContainerExist() throws StorageException;

	/**
	 * Check if the blob exists
	 * 
	 * @param blobName
	 *            of the BLOB.
	 * @return true if the blob exists, false otherwise.
	 * @throws StorageException
	 */
	public boolean isBlobExist(String blobName) throws StorageException;

	/**
	 * Enumerates all blobs
	 * 
	 * @return Blobs list
	 * @throws StorageException
	 */
	public Iterator<IBlobProperties> listBlobs() throws StorageException;

	/**
	 * Enumerates all blobs with a given prefix.
	 * 
	 * @param prefix
	 * @param combineCommonPrefixes
	 *            If true common prefixes with "/" as separator
	 * @return The list of blob properties and common prefixes
	 * @throws StorageException
	 */
	public Iterator<IBlobProperties> listBlobs(String prefix,
			boolean combineCommonPrefixes) throws StorageException;

	/**
	 * Enumerates all blobs with a given prefix.
	 * 
	 * @param prefix
	 * @param combineCommonPrefixes
	 *            If true common prefixes with "/" as separator
	 * @param maxResults
	 *            Specifies the maximum number of blobs to return per call to
	 *            Azure storage. This does NOT affect list size returned by this
	 *            function.
	 * @return The list of blob properties and common prefixes
	 * @throws StorageException
	 */
	public Iterator<IBlobProperties> listBlobs(String prefix,
			boolean combineCommonPrefixes, int maxResults)
			throws StorageException;

	/**
	 * Set the access control permissions associated with the container.
	 * 
	 * @param acl
	 *            The permission to set
	 */
	public void setAccessControl(IContainerAccessControl acl)
			throws StorageException;

	/**
	 * Get the access control permissions associated with the container.
	 * 
	 * @throws StorageException
	 */
	public ContainerAccessControl getAccessControl() throws StorageException;

	/**
	 * Get the properties for the container if it exists.
	 * 
	 * @return The properties for the container if it exists, null otherwise
	 * @throws StorageException
	 */
	public IContainerProperties getProperties() throws StorageException;

	/**
	 * Copies a blob to a destination within the storage account. </p>
	 * <strong>Note</strong></br> The Copy Blob operation is available only in
	 * the 2009-04-14 version of the Blob service. It is currently available
	 * only in Windows Azure storage, and not in development storage, nor within
	 * the StorageClient sample included in the Windows Azure SDK.
	 * 
	 * @param destContainer
	 *            the destination blob container;
	 * @param destBlobName
	 *            the destination blob's name;
	 * @param sourceBlobName
	 *            the source blob's name;
	 * @return
	 * @throws StorageException
	 */
	public boolean copyBlob(String destContainer, String destBlobName,
			String sourceBlobName) throws StorageException;

	/**
	 * Copies a blob to a destination within the storage account. </p>
	 * <strong>Note</strong></br> The Copy Blob operation is available only in
	 * the 2009-04-14 version of the Blob service. It is currently available
	 * only in Windows Azure storage, and not in development storage, nor within
	 * the StorageClient sample included in the Windows Azure SDK.
	 * 
	 * @param destContainer
	 *            the destination blob container;
	 * @param destBlobName
	 *            the destination blob's name;
	 * @param sourceBlobName
	 *            the source blob's name;
	 * @param metadata
	 *            The metadata for the Blob. Can be null to indicate no
	 *            metadata;
	 * @param constraints
	 *            The blob constraints for the blob copy operation.
	 * @return
	 * @throws StorageException
	 */
	public boolean copyBlob(String destContainer, String destBlobName,
			String sourceBlobName, final NameValueCollection metadata,
			final IBlobConstraints constraints) throws StorageException;

	/**
	 * Delete a blob with the given name.
	 * 
	 * @param name
	 *            The name of the blob
	 * @return true if the blob exists and was successfully deleted, false if
	 *         the blob does not exist
	 * @throws StorageException
	 */
	public boolean deleteBlob(String name) throws StorageException;

	/**
	 * Delete a blob with the given name if the blob has not been modified since
	 * it was last obtained. Use this method for optimistic concurrency to avoid
	 * deleting a blob that has been modified since the last time you retrieved
	 * it
	 * 
	 * @param blob
	 *            A blob object (typically previously obtained from a GetBlob
	 *            call)
	 * @return true if the blob exists and was successfully deleted, false if
	 *         the blob does not exist or was not deleted because the blob was
	 *         modified.
	 * @StorageException If the blob was not deleted because the blob was
	 *                   modified, then throw an storage StorageException.
	 */
	public boolean deleteBlobIfNotModified(IBlobProperties blob)
			throws StorageException;

	/**
	 * Create a new page blob.
	 * 
	 * @param blobProperties
	 *            The properties of blob.
	 * @param size
	 *            The maximum size for page blob
	 * @param headerParameters
	 *            The header parameters.
	 * @throws StorageException
	 */
	public IPageBlob createPageBlob(IBlobProperties blobProperties, int size,
			NameValueCollection headerParameters) throws StorageException;

	/**
	 * @return The base URI of the blob storate service.
	 */
	public URI getBaseUri();

	/**
	 * @return The name of the storage account.
	 */
	public String getAccountName();

	/**
	 * @return The name of the blob container.
	 */
	public String getName();

	/**
	 * Indicates whether to use/generate path-style or host-style URIs.
	 * 
	 * @return true/false
	 */
	public boolean isUsePathStyleUris();

	/**
	 * @return The URI of the container.
	 */
	public URI getUri();

	/**
	 * @return The timestamp for last modification of container.
	 */
	public Timestamp getLastModifiedTime();

	/**
	 * @return The time out for each request to the storage service.
	 */
	public TimeSpan getTimeout();

	/**
	 * @return The retry policy used for retrying requests.
	 */
	public IRetryPolicy getRetryPolicy();

	/**
	 * @param timeout
	 *            The time out for each request to the storage service.
	 */
	public void setTimeout(TimeSpan timeout);

	/**
	 * @param retryPolicy
	 *            The retry policy used for retrying requests.
	 */
	public void setRetryPolicy(IRetryPolicy retryPolicy);

	/**
	 * @param containerUri
	 *            The URI of the container.
	 */
	public void setUri(URI containerUri);

	/**
	 * The Lease Blob operation establishes and manages a one-minute lock on a
	 * blob for write operations.
	 * 
	 * @param blobProperties
	 *            The properties of the blob
	 * @param mode
	 *            Lease Blob mode for this operation. There are four modes:
	 *            Acquire, to request a new lease. Renew, to renew an existing
	 *            lease. Release, to free the lease if it is no longer needed so
	 *            that another client may immediately acquire a lease against
	 *            the blob. Break, to end the lease but ensure that another
	 *            client cannot acquire a new lease until the current lease
	 *            period has expired.
	 * @param headerParameters
	 * @return
	 * @throws StorageException
	 */
	public String leaseBlob(IBlobProperties blobProperties, LeaseMode mode,
			NameValueCollection headerParameters) throws StorageException;

	/**
	 * Update a existing blob.
	 * 
	 * @param blobProperties
	 *            The properties of the blob
	 * @param blobContents
	 *            The contents of the blob
	 * @return If the blob already exists, update the blob, else throw a
	 *         exception. The LastModifiedTime property of <paramref
	 *         name="blobProperties"/> is set as a result of this call. This
	 *         method also has an effect on the ETag values that are managed by
	 *         the service.
	 * @throws StorageException
	 */
	public IBlockBlob updateBlockBlob(IBlobProperties blobProperties,
			IBlobContents blobContents) throws StorageException;

	/**
	 * Create a new block blob.
	 * 
	 * @param blobProperties
	 *            The properties of the blob
	 * @param blobContents
	 *            The contents of the blob
	 * @return the create blob. The LastModifiedTime property of <paramref
	 *         name="blobProperties"/> is set as a result of this call. This
	 *         method also has an effect on the ETag values that are managed by
	 *         the service.
	 * @throws StorageException
	 */
	public IBlockBlob createBlockBlob(IBlobProperties blobProperties,
			IBlobContents blobContents) throws StorageException;

	/**
	 * Get a reference to a Blob object with a specified name.
	 * 
	 * @param name
	 *            The name of the blob
	 * @return The blob
	 */
	public IBlockBlob getBlockBlobReference(String name)
			throws StorageException;

	/**
	 * Get a reference to a Blob object with a specified name.
	 * 
	 * @param name
	 *            The name of the blob
	 * @return The blob
	 */
	public IPageBlob getPageBlobReference(String name) throws StorageException;

	/**
	 * Get a reference to a Blob object with a specified name.
	 * 
	 * @param name
	 *            The name of the blob
	 * @return The blob
	 */
	public IBlob getBlobReference(String name) throws StorageException;

	// /**
	// * Updates an existing blob if it has not been modified since the
	// specified
	// * time which is typically the last modified time of the blob when you
	// * retrieved it. Use this method to implement optimistic concurrency by
	// * avoiding clobbering changes to the blob made by another writer.
	// *
	// * @param blobProperties
	// * The properties of the blob. This object should be one
	// * previously obtained from a call to GetBlob or
	// * GetBlobProperties and have its LastModifiedTime property set.
	// * @param contents
	// * The contents of the blob. The contents of the blob should be
	// * readable
	// * @return true if the blob was updated. false if the blob has changed
	// since
	// * the last time. The LastModifiedTime property of parameter
	// * "properties" is set as a result of this call.
	// * @throws StorageException
	// */
	// public boolean updateBlobIfNotModified(
	// IBlobProperties blobProperties, IBlobContents contents)
	// throws StorageException;

	// /**
	// * Get a reference to a Blob object with a specified name.
	// *
	// * @param name
	// * The name of the blob
	// * @param blobContentsStream
	// * A writable stream or a default constructed object.
	// * The stream of the blobContents¡£
	// * @param transferAsChunks
	// * Should the blob be gotten in pieces. This requires more
	// * round-trips, but will retry smaller pieces in case of failure.
	// * @return The blob which contains the blobProperties and blobContents.
	// */
	// public IBlob getBlobReference(String name, BlobStream blobContentsStream,
	// boolean transferAsChunks) throws StorageException;
	//
	// /**
	// * Set the metadata of an existing blob.
	// *
	// * @param blobProperties
	// * The blob properties object whose metadata is to be updated
	// * @throws StorageException
	// */
	// public void updateBlobMetadata(IBlobProperties blobProperties)
	// throws StorageException;
	//
	// /**
	// * Set the metadata of an existing blob if it has not been modified since
	// it
	// * was last retrieved.
	// *
	// * @param blobProperties
	// * The blob properties object whose metadata is to be updated.
	// * Typically obtained by a previous call to GetBlob or
	// * GetBlobProperties
	// * @return true if the blob metadata was updated. false if it was not
	// * updated because the blob has been modified
	// * @exception StorageException
	// */
	// public boolean updateBlobMetadataIfNotModified(
	// IBlobProperties blobProperties) throws StorageException;

	// public Timestamp createSnapshot(IBlobProperties blobProperties,
	// NameValueCollection headerParameters) throws StorageException;

	// /**
	// * Set if the fetch process should be stopped.
	// *
	// * @param stop
	// *
	// */
	// public void stopFetchProgress(boolean stop);

	// /**
	// * The get Page Regions operation returns the list of valid page ranges
	// for a page blob or snapshot.
	// * @param blobProperties
	// * @param headerParameters
	// * @return
	// * @throws StorageException
	// */
	// public List<PageRange> getPageRegions(
	// IBlobProperties blobProperties, NameValueCollection headerParameters)
	// throws StorageException;
	//
	// /**
	// * Update the container page.
	// *
	// * @param blobProperties
	// * The properties of blob.
	// * @param pageData
	// * The stream of page data.
	// * @param range
	// * For a page update operation, the page range can be up to 4 MB in size.
	// * For a page clear operation, the page range can be up to the value of
	// the
	// * blob's full size. Given that pages must be aligned with 512-byte
	// * boundaries, the start offset must be a modulus of 512 and the end
	// offset
	// * must be a modulus of 512 ¨C 1. Examples of valid byte ranges are 0-511,
	// * 512-1023, etc. The Blob service accepts only a single byte range for
	// the
	// * Range header, and the byte range must be specified in the following
	// * format: bytes=startByte-endByte.
	// * @param headerParameters
	// * The header parameters.
	// * @throws StorageException
	// */
	// public void writePages(IBlobProperties blobProperties,
	// BlobStream pageData, IPageRange range,
	// NameValueCollection headerParameters) throws StorageException;

	// /**
	// * Get the properties of the blob if it exists. This method is also the
	// * simplest way to check if a blob exists.
	// *
	// * @param name
	// * The name of the blob
	// * @return The properties of the blob if it exists. null otherwise. // /
	// The
	// * properties for the contents of the blob are not set
	// */
	// public IBlobProperties getBlobProperties(String name)
	// throws StorageException;

}
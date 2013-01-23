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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.soyatec.windowsazure.blob.BlobType;
import org.soyatec.windowsazure.blob.IBlob;
import org.soyatec.windowsazure.blob.IBlobContainer;
import org.soyatec.windowsazure.blob.IBlobContents;
import org.soyatec.windowsazure.blob.IBlobProperties;
import org.soyatec.windowsazure.blob.IRetryPolicy;
import org.soyatec.windowsazure.blob.io.BlobStream;
import org.soyatec.windowsazure.constants.BlobBlockConstants;
import org.soyatec.windowsazure.error.StorageErrorCode;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.error.StorageServerException;
import org.soyatec.windowsazure.internal.OutParameter;
import org.soyatec.windowsazure.internal.ResourceUriComponents;
import org.soyatec.windowsazure.internal.constants.CompConstants;
import org.soyatec.windowsazure.internal.constants.HeaderNames;
import org.soyatec.windowsazure.internal.constants.HeaderValues;
import org.soyatec.windowsazure.internal.constants.HttpMethod;
import org.soyatec.windowsazure.internal.constants.HttpWebResponse;
import org.soyatec.windowsazure.internal.constants.QueryParams;
import org.soyatec.windowsazure.internal.constants.XmsVersion;
import org.soyatec.windowsazure.internal.util.HttpUtilities;
import org.soyatec.windowsazure.internal.util.NameValueCollection;
import org.soyatec.windowsazure.internal.util.Utilities;
import org.soyatec.windowsazure.internal.util.ssl.SslUtil;

/**
 * The <code>Blob</code> class specifies contents and properties for blob.
 *
 */
public class Blob implements IBlob {

	long blockSize = BlobBlockConstants.BlockSize;

	BlobContainerRest container;

	String blobName;

	static final double PageSize = 1024 * 128;

	/**
	 * When fetch blob data slice by slice, the fetch progress can be stopped by
	 * setting flag to true.
	 */
	private final AtomicBoolean stopFlag = new AtomicBoolean(Boolean.FALSE);

	Blob(BlobContainerRest container, String blobName) {
		this.container = container;
		this.blobName = blobName;
	}

	public void stopFetchProgress(boolean stop) {
		stopFlag.set(stop);
	}

	public boolean isStopped() {
		return stopFlag.get();
	}

	public IBlobContents getContents(BlobStream stream) throws StorageException {
		try {
			getPropertiesImpl(blobName, stream, null, true,
					new OutParameter<Boolean>(false));
			return new BlobContents(stream);
		} catch (Exception e) {
			throw HttpUtilities.translateWebException(e);
		}
	}

	public void setContents(IBlobContents contents) throws StorageException {
		// implement in sub class
	}

	public IBlobProperties getProperties() throws StorageException {
		try {
			return getPropertiesImpl(blobName, null, null, false,
					new OutParameter<Boolean>(false));
		} catch (Exception e) {
			throw HttpUtilities.translateWebException(e);
		}
	}

	public void setProperties(IBlobProperties properties)
			throws StorageException {
		setPropertiesImpl(properties, null);
	}

	public long getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(long blockSize) {
		this.blockSize = blockSize;
	}

	/**
	 * Check if a blob has been modified since you retrieve it.
	 *
	 * @param blobProperties
	 *            The properties of the blob. This object should be one
	 *            previously obtained from a call to getProperties.
	 * @return true if the blob is modified.
	 * @throws StorageException
	 */
	public boolean isModified(final IBlobProperties blobProperties)
			throws StorageException {
		final OutParameter<Boolean> modified = new OutParameter<Boolean>(false);
		try {
			container.getRetryPolicy().execute(new Callable<Object>() {

				public Object call() throws Exception {
					// Set the position to rewind in case of a retry.
					BlobProperties blob = downloadData(blobName, null, null,
							blobProperties.getETag(), 0, 0,
							new NameValueCollection(), modified);
					return blob;
				}
			});

			return modified.getValue();
		} catch (Exception e) {
			if (e instanceof StorageServerException) {
				int code = ((StorageServerException) e).getStatusCode();
				if (code == HttpStatus.SC_PRECONDITION_FAILED
						|| code == HttpStatus.SC_NOT_MODIFIED) {
					return modified.getValue();
				}
			}
			throw HttpUtilities.translateWebException(e);
		}
	}

	BlobProperties getPropertiesImpl(final String blobName,
			final BlobStream stream, final String oldETag,
			boolean transferAsChunks, OutParameter<Boolean> modified)
			throws Exception {
		if (Utilities.isNullOrEmpty(blobName)) {
			throw new IllegalArgumentException(
					"Blob name cannot be null or empty!");
		}

		final BlobProperties[] blobProperties = new BlobProperties[1];
		final OutParameter<Boolean> localModified = new OutParameter<Boolean>(
				true);
		// Reset the stop flag
		stopFetchProgress(Boolean.FALSE);
		// If we are interested only in the blob properties (stream ==null) or
		// we are performing
		// a chunked download we first obtain just the blob properties
		if (stream == null || transferAsChunks) {
			container.getRetryPolicy().execute(new Callable<Object>() {

				public Object call() throws Exception {
					// Set the position to rewind in case of a retry.
					BlobProperties blob = downloadData(blobName, null, oldETag,
							null, 0, 0, new NameValueCollection(),
							localModified);
					blobProperties[0] = blob;
					return blob;
				}
			});

			modified.setValue(localModified.getValue());
			if (stream == null) {
				return blobProperties[0];
			}

		}

		IRetryPolicy rp = stream.canSeek() ? container.getRetryPolicy()
				: RetryPolicies.noRetry();
		final long originalPosition = stream.canSeek() ? stream.getPosition()
				: 0;
		if (transferAsChunks && blobProperties != null
				&& blobProperties[0].getContentLength() > 0) {
			// Chunked download. Obtain ranges of the blobs in 'BlockSize'
			// chunks
			// Ensure that the If-Match <Etag>header is used on each request so
			// that we are assured that all data belongs to the single blob we
			// started downloading.
			final long[] location = new long[] { 0 };

			while (location[0] < blobProperties[0].getContentLength()) {
				if (isStopped()) {
					throw new IOException(
							"Download blob progress is terminated.");
				}

				final long nBytes = Math.min(
						blobProperties[0].getContentLength() - location[0],
						getBlockSize());

				rp.execute(new Callable<Object>() {

					public Object call() throws Exception {
						// Set the position to rewind in case of a retry.
						if (stream.canSeek()) {
							stream.setPosition(originalPosition + location[0]);
						}
						downloadData(blobName, stream, oldETag,
								blobProperties[0].getETag(), location[0],
								nBytes, new NameValueCollection(),
								localModified);
						return null;
					}
				});
				location[0] += nBytes;
			}
		}

		else {
			rp.execute(new Callable<Object>() {
				public Object call() throws Exception {
					// Set the position to rewind in case of a retry.
					if (stream.canSeek()) {
						stream.setPosition(originalPosition);
					}
					BlobProperties blob = downloadData(blobName, stream,
							oldETag, null, 0, 0, new NameValueCollection(),
							localModified);
					blobProperties[0] = blob;
					return blob;
				}
			});
		}
		modified.setValue(localModified.getValue());
		return blobProperties[0];
	}

	protected BlobProperties downloadData(String blobName, BlobStream stream,
			String eTagIfNoneMatch, String eTagIfMatch, long offset,
			long length, NameValueCollection nvc,
			OutParameter<Boolean> localModified) throws StorageException {
		String containerName = container.getName();
		if (containerName.equals(IBlobContainer.ROOT_CONTAINER)) {
			containerName = "";
		}
		ResourceUriComponents uriComponents = new ResourceUriComponents(
				container.getAccountName(), containerName, blobName);
		
		
		
		URI blobUri = HttpUtilities.createRequestUri(container.getBaseUri(),
				container.isUsePathStyleUris(), container.getAccountName(),
				containerName, blobName, container.getTimeout(), nvc,
				uriComponents);
		
		if (SSLProperties.isSSL()) {
			try {
				URI newBlobUri = new URI("https", null, blobUri.getHost(), 443,
						blobUri.getPath(), blobUri.getQuery(), blobUri
								.getFragment());
				blobUri = newBlobUri;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		String httpMethod = (stream == null ? HttpMethod.Head : HttpMethod.Get);
		HttpRequest request = createHttpRequestForGetBlob(blobUri, httpMethod,
				eTagIfNoneMatch, eTagIfMatch);

		if (offset != 0 || length != 0) {
			// Use the blob storage custom header for range since the standard
			// HttpWebRequest.
			// AddRange accepts only 32 bit integers and so does not work for
			// large blobs.
			String rangeHeaderValue = MessageFormat
					.format(HeaderValues.RangeHeaderFormat, offset, offset
							+ length - 1);
			request.addHeader(HeaderNames.StorageRange, rangeHeaderValue);
		}
		request.addHeader(HeaderNames.ApiVersion, XmsVersion.VERSION_2009_09_19);
		container.getCredentials().signRequest(request, uriComponents);
		BlobProperties blobProperties;

		try {
			HttpWebResponse response = null;
			if (SSLProperties.isSSL()) {
				SSLSocketFactory factory = SslUtil.createSSLSocketFactory(
						SSLProperties.getKeyStore(),
						SSLProperties.getKeyStorePasswd(),
						SSLProperties.getTrustStore(),
						SSLProperties.getTrustStorePasswd(),
						SSLProperties.getKeyAlias());
				response = HttpUtilities.getSSLReponse(
						(HttpUriRequest) request, factory);
			} else {
				response = HttpUtilities.getResponse(request);
			}
			if (response.getStatusCode() == HttpStatus.SC_OK
					|| response.getStatusCode() == HttpStatus.SC_PARTIAL_CONTENT) {

				blobProperties = blobPropertiesFromResponse(blobName, blobUri,
						response);
				if (stream != null) {
					InputStream responseStream = response.getStream();
					long byteCopied = Utilities.copyStream(responseStream,
							stream);
					response.close();
					if (blobProperties.getContentLength() > 0
							&& byteCopied < blobProperties.getContentLength()) {
						throw new StorageServerException(
								StorageErrorCode.ServiceTimeout,
								"Unable to read complete data from server",
								HttpStatus.SC_REQUEST_TIMEOUT, null);
					}
				} else {
					response.close();
				}
			} else if (response.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED
					|| response.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {

				if (eTagIfMatch != null)
					localModified.setValue(true);
				else if (eTagIfNoneMatch != null)
					localModified.setValue(false);
				HttpUtilities.processUnexpectedStatusCode(response);
				return null;
			} else if(response.getStatusCode() == HttpStatus.SC_NOT_FOUND &&  stream == null){
				return null; // check blob exist
			}else{
				HttpUtilities.processUnexpectedStatusCode(response);
				return null;
			}
			return blobProperties;
		} catch (Exception we) {
			throw HttpUtilities.translateWebException(we);
		}
	}

	private HttpRequest createHttpRequestForGetBlob(URI blobUri,
			String httpMethod, String tagIfNoneMatch, String tagIfMatch) {
		HttpRequest request = HttpUtilities.createHttpRequestWithCommonHeaders(
				blobUri, httpMethod, container.getTimeout());
		if (tagIfNoneMatch != null) {
			request.addHeader(HeaderNames.IfNoneMatch, tagIfNoneMatch);
		}
		if (tagIfMatch != null) {
			request.addHeader(HeaderNames.IfMatch, tagIfMatch);
		}
		return request;
	}

	private BlobProperties blobPropertiesFromResponse(String blobName,
			URI blobUri, HttpWebResponse response) throws URISyntaxException {

		BlobProperties blobProperties = new BlobProperties(blobName);
		blobProperties.setUri(container.constructBlobUri(blobName));// removeQueryParams(blobUri));
		blobProperties.setContentEncoding(response
				.getHeader(HeaderNames.ContentEncoding));
		blobProperties.setLastModifiedTime(response.getLastModified());
		blobProperties.setETag(response.getHeader(HeaderNames.ETag));
		blobProperties.setContentLanguage(response
				.getHeader(HeaderNames.ContentLanguage));
		blobProperties.setContentLength(response.getContentLength());
		blobProperties.setContentType(response.getContentType());
		blobProperties.setBlobType(BlobType.valueOf(response
				.getHeader(HeaderNames.BlobType)));
		blobProperties.setContentMD5( response.getHeader(HeaderNames.ContentMD5) );
		NameValueCollection metadataEntries = container
				.metadataFromHeaders(response.getHeaders());

		if (metadataEntries.size() > 0) {
			blobProperties.setMetadata(metadataEntries);
		}
		return blobProperties;
	}

	boolean setPropertiesImpl(final IBlobProperties blobProperties,
			final String eTag) {
		if (blobProperties == null) {
			throw new IllegalArgumentException(
					"Blob properties cannot be null or empty!");
		}

		final OutParameter<Boolean> retval = new OutParameter<Boolean>(false);
		container.getRetryPolicy().execute(new Callable<Boolean>() {

			public Boolean call() throws Exception {
				NameValueCollection queryParams = new NameValueCollection();
				queryParams.put(QueryParams.QueryParamComp,
						CompConstants.Metadata);
				final ResourceUriComponents uriComponents = new ResourceUriComponents(
						container.getAccountName(), container.getName(),
						blobProperties.getName());
				URI uri = HttpUtilities.createRequestUri(
						container.getBaseUri(), container.isUsePathStyleUris(),
						container.getAccountName(), container.getName(),
						blobProperties.getName(), container.getTimeout(),
						queryParams, uriComponents);
				HttpRequest request = HttpUtilities
						.createHttpRequestWithCommonHeaders(uri,
								HttpMethod.Put, container.getTimeout());
				if (blobProperties.getMetadata() != null) {
					HttpUtilities.addMetadataHeaders(request,
							blobProperties.getMetadata());
				}
				if(blobProperties.getContentMD5() != null){
					request.addHeader(HeaderNames.ContentMD5, blobProperties.getContentMD5());
				}
				if (!Utilities.isNullOrEmpty(eTag)) {
					request.addHeader(HeaderNames.IfMatch, eTag);
				}
				request.addHeader(HeaderNames.ApiVersion,
						XmsVersion.VERSION_2009_09_19);
				container.getCredentials().signRequest(request, uriComponents);
				HttpWebResponse response = HttpUtilities.getResponse(request);
				int statusCode = response.getStatusCode();
				if (statusCode == HttpStatus.SC_OK) {
					retval.setValue(true);
					blobProperties.setLastModifiedTime(response
							.getLastModified());
					blobProperties.setETag(response.getHeader(HeaderNames.ETag));
					response.close();
				} else if (statusCode == HttpStatus.SC_PRECONDITION_FAILED
						|| statusCode == HttpStatus.SC_NOT_MODIFIED) {
					retval.setValue(false);
					response.close();
				} else {
					retval.setValue(false);
					HttpUtilities.processUnexpectedStatusCode(response);
				}
				return null;
			}
		});
		return retval.getValue();
	}

	public Timestamp createSnapshot(final NameValueCollection headerParameters)
			throws StorageException {
		String containerName = container.getName();
		if (containerName.equals(IBlobContainer.ROOT_CONTAINER)) {
			containerName = "";
		}

		// append ?comp=lease in url
		NameValueCollection queryParams = new NameValueCollection();
		queryParams.put(QueryParams.QueryParamComp, CompConstants.Snapshot);

		final ResourceUriComponents uriComponents = new ResourceUriComponents(
				container.getAccountName(), containerName, blobName);
		final URI blobUri = HttpUtilities.createRequestUri(
				container.getBaseUri(), container.isUsePathStyleUris(),
				container.getAccountName(), containerName, blobName,
				container.getTimeout(), queryParams, uriComponents);
		IRetryPolicy policy = container.getRetryPolicy();
		Timestamp result = (Timestamp) policy
				.execute(new Callable<Timestamp>() {
					public Timestamp call() throws Exception {
						HttpRequest request = HttpUtilities
								.createHttpRequestWithCommonHeaders(blobUri,
										HttpMethod.Put, container.getTimeout());

						// add header
						request.addHeader(HeaderNames.ApiVersion,
								XmsVersion.VERSION_2009_09_19);

						container.appendHeaders(request, headerParameters);
						container.getCredentials().signRequest(request,
								uriComponents);
						try {
							HttpWebResponse response = HttpUtilities
									.getResponse(request);
							int status = response.getStatusCode();
							if (status == HttpStatus.SC_CREATED) {
								String snapshot = response
										.getHeader(HeaderNames.Snapshot);
								response.close();
								return Utilities
										.tryGetDateTimeFromTableEntry(snapshot);
							} else {
								response.close();
							}
						} catch (Exception ioe) {
							HttpUtilities.translateWebException(ioe);
						}
						return null;
					}
				});
		return result;
	}

	public boolean updateIfNotModified(IBlobProperties blobProperties,
			IBlobContents contents) throws StorageException {
		return false;
	}

}

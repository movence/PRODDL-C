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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ByteArrayEntity;
import org.dom4j.Document;
import org.dom4j.Element;
import org.soyatec.windowsazure.blob.IBlobContainer;
import org.soyatec.windowsazure.blob.IBlobContents;
import org.soyatec.windowsazure.blob.IBlobProperties;
import org.soyatec.windowsazure.blob.IPageBlob;
import org.soyatec.windowsazure.blob.IPageRange;
import org.soyatec.windowsazure.blob.IRetryPolicy;
import org.soyatec.windowsazure.blob.io.BlobMemoryStream;
import org.soyatec.windowsazure.blob.io.BlobStream;
import org.soyatec.windowsazure.constants.XmlElementNames;
import org.soyatec.windowsazure.error.StorageErrorCode;
import org.soyatec.windowsazure.error.StorageErrorCodeStrings;
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
import org.soyatec.windowsazure.internal.util.xml.XPathQueryHelper;
import org.soyatec.windowsazure.internal.util.xml.XmlUtil;

public class PageBlob extends Blob implements IPageBlob {

	PageBlob(BlobContainerRest container, String blobName) {
		super(container, blobName);
	}

	public void setContents(IBlobContents blobContents) throws StorageException {
		setContentsImpl( new BlobProperties(blobName),blobContents , null );
	}

	void setContentsImpl(IBlobProperties blobProperties, IBlobContents blobContents, final NameValueCollection headerParameters){
		//To create a new page blob, first initialize the blob by calling Put Blob and specify its maximum size, up to 1 TB.
		// When creating a page blob, do not include content in the request body. Once the blob has been created, call Put Page to add content to the blob or to modify it.
		BlobStream stream = blobContents.getStream();
		if(stream == null)
			throw new IllegalArgumentException("Stream is null.");
		try {
			int size = (int) stream.length();
			if( size % 512 != 0)
				size = (int) (Math.ceil( 1.0 * size / 512) * 512);
			// create empty page
			container.createPageBlob(blobProperties, size, headerParameters);

			int numPages = (int) Math.ceil(1.0 * size / PageSize);
			int startOffset = 0;
			int endOffset = 0;
			for(int i = 0; i < numPages; i ++){
				endOffset = (int) Math.min( size, startOffset  + PageSize);
				// pages must be aligned with 512-byte boundaries
				PageRange range = new PageRange(startOffset, endOffset - 1);
				writePages(stream, range, null);
				startOffset = endOffset;
			}
		} catch (IOException e) {
			throw HttpUtilities.translateWebException(e);
		}
	}


	public boolean updateIfNotModified(IBlobProperties blobProperties,
			IBlobContents contents) throws StorageException {
		try {
			NameValueCollection headerParameters = new NameValueCollection();
			headerParameters.put( HeaderNames.ETag , blobProperties.getETag());
			setContentsImpl(blobProperties, contents, headerParameters);
			return true;
		} catch (Exception e) {
			throw HttpUtilities.translateWebException(e);
		}
	}

	/**
	 * For a page update operation, the page range can be up to 4 MB in size.
	 * For a page clear operation, the page range can be up to the value of the
	 * blob's full size. Given that pages must be aligned with 512-byte
	 * boundaries, the start offset must be a modulus of 512 and the end offset
	 * must be a modulus of 512 ¨C 1. Examples of valid byte ranges are 0-511,
	 * 512-1023, etc. The Blob service accepts only a single byte range for the
	 * Range header, and the byte range must be specified in the following
	 * format: bytes=startByte-endByte.
	 */
	public void writePages(final BlobStream pageData, final IPageRange range,
			final NameValueCollection headerParameters) throws StorageException {

		if (blobName == null || blobName.equals("")) {
			throw new IllegalArgumentException("Blob name is empty.");
		}

		String containerName = container.getName();
		if (containerName.equals(IBlobContainer.ROOT_CONTAINER)) {
			containerName = "";
		}

		NameValueCollection queryParams = new NameValueCollection();
		queryParams.put(QueryParams.QueryParamComp, CompConstants.Page);

		final ResourceUriComponents uriComponents = new ResourceUriComponents(
				container.getAccountName(), containerName, blobName);
		final URI blobUri = HttpUtilities.createRequestUri(
				container.getBaseUri(), container.isUsePathStyleUris(),
				container.getAccountName(), containerName, blobName,
				container.getTimeout(), queryParams, uriComponents);

		container.getRetryPolicy().execute(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				HttpRequest request = HttpUtilities
						.createHttpRequestWithCommonHeaders(blobUri,
								HttpMethod.Put, container.getTimeout());

				// add header
				request.addHeader(HeaderNames.ApiVersion,
						XmsVersion.VERSION_2009_09_19);
				request.addHeader(HeaderNames.Range, range.toString());
				container.appendHeaders(request, headerParameters);
				try {
					if (pageData == null) {
						request.addHeader(HeaderNames.PageWrite,
								HeaderValues.Clear);
						// request.addHeader(HeaderNames.ContentLength, "0");
						container.getCredentials().signRequest(request,
								uriComponents);
					} else {
						request.addHeader(HeaderNames.PageWrite,
								HeaderValues.Update);
						request.addHeader(HeaderNames.ContentLength,
								String.valueOf(range.length()));
						container.getCredentials().signRequest(request,
								uriComponents);
						BlobStream requestStream = new BlobMemoryStream();
						Utilities.copyStream(pageData, requestStream,
								(int) range.length());
						((HttpEntityEnclosingRequest) request)
								.setEntity(new ByteArrayEntity(requestStream
										.getBytes()));
					}
					// avoid error using put
					request.removeHeaders(HeaderNames.ContentLength);
					HttpWebResponse response = HttpUtilities
							.getResponse(request);
					if (response.getStatusCode() == HttpStatus.SC_CREATED) {
						response.close();
						return Boolean.TRUE;
					} else {
						XmlUtil.load(response.getStream());
						HttpUtilities.processUnexpectedStatusCode(response);
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (e instanceof StorageServerException) {
						StorageServerException sse = (StorageServerException) e;
						String detailMessage = sse.getMessage();
						StorageErrorCode errorCode = sse.getErrorCode();
						if (detailMessage != null && detailMessage.startsWith(StorageErrorCodeStrings.InvalidPageRange)
								&& errorCode == StorageErrorCode.ServiceBadResponse) {
							throw new StorageServerException(StorageErrorCode.BadRange, detailMessage, sse
									.getStatusCode(), null);
						}
					}
				}
				return Boolean.FALSE;
			}
		});
	}

	public List<PageRange> getPageRegions(
			final NameValueCollection headerParameters) throws StorageException {

		if (blobName == null || blobName.equals("")) {
			throw new IllegalArgumentException("Blob name is empty.");
		}

		String containerName = container.getName();
		if (containerName.equals(IBlobContainer.ROOT_CONTAINER)) {
			containerName = "";
		}

		NameValueCollection queryParams = new NameValueCollection();
		queryParams.put(QueryParams.QueryParamComp, CompConstants.PageList);

		final ResourceUriComponents uriComponents = new ResourceUriComponents(
				container.getAccountName(), containerName, blobName);
		final URI blobUri = HttpUtilities.createRequestUri(
				container.getBaseUri(), container.isUsePathStyleUris(),
				container.getAccountName(), containerName, blobName, null,
				queryParams, uriComponents);

		List<PageRange> result = (List<PageRange>) container.getRetryPolicy()
				.execute(new Callable<List<PageRange>>() {
					public List<PageRange> call() throws Exception {

						HttpRequest request = HttpUtilities
								.createHttpRequestWithCommonHeaders(blobUri,
										HttpMethod.Get, null);

						// add header
						request.addHeader(HeaderNames.ApiVersion,
								XmsVersion.VERSION_2009_09_19);
						container.appendHeaders(request, headerParameters);
						container.getCredentials().signRequest(request,
								uriComponents);

						HttpWebResponse response;
						try {
							response = HttpUtilities.getResponse(request);
							if (response.getStatusCode() == HttpStatus.SC_OK) {
								List<PageRange> result = parsePageRegions(response.getStream());
								response.close();
								return result;
							} else {
								XmlUtil.load(response.getStream());
								HttpUtilities
										.processUnexpectedStatusCode(response);
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}
				});
		return result;
	}

	/**
	 * Parse the page regions of inputStream.
	 *
	 * @param stream
	 * @return the list of page range.
	 */
	List<PageRange> parsePageRegions(InputStream stream) {
		List<PageRange> ranges = new ArrayList<PageRange>();

		Document document = XmlUtil.load(stream);
		// List xmlNodes = document
		// .selectNodes(XPathQueryHelper.CommonPrefixQuery);

		// Get all the blobs returned as the listing results
		List xmlNodes = document.selectNodes(XPathQueryHelper.PageRangeQuery);
		for (Iterator iterator = xmlNodes.iterator(); iterator.hasNext();) {
			/*
			 * Parse the Blob meta-data from response XML content.
			 */
			Element node = (Element) iterator.next();

			String start = XPathQueryHelper.loadSingleChildStringValue(node,
					XmlElementNames.Start, true);
			String end = XPathQueryHelper.loadSingleChildStringValue(node,
					XmlElementNames.End, true);

			ranges.add(new PageRange(Integer.parseInt(start), Integer
					.parseInt(end)));
		}
		return ranges;

	}
	
	public IBlobContents getContents(BlobStream stream, IPageRange range) throws StorageException {
		try {
			getPropertiesImpl(blobName, stream, range, null, true, new OutParameter<Boolean>(false));
			return new BlobContents(stream);
		} catch (Exception e) {
			throw HttpUtilities.translateWebException(e);
		}
	}

	BlobProperties getPropertiesImpl(final String blobName, final BlobStream stream, IPageRange range,
			final String oldETag, boolean transferAsChunks, OutParameter<Boolean> modified) throws Exception {
		if (Utilities.isNullOrEmpty(blobName)) {
			throw new IllegalArgumentException("Blob name cannot be null or empty!");
		}

		final BlobProperties[] blobProperties = new BlobProperties[1];
		final OutParameter<Boolean> localModified = new OutParameter<Boolean>(true);
		// Reset the stop flag
		stopFetchProgress(Boolean.FALSE);
		// If we are interested only in the blob properties (stream ==null) or
		// we are performing
		// a chunked download we first obtain just the blob properties
		if (stream == null || transferAsChunks) {
			container.getRetryPolicy().execute(new Callable<Object>() {

				public Object call() throws Exception {
					// Set the position to rewind in case of a retry.
					BlobProperties blob = downloadData(blobName, null, oldETag, null, 0, 0, new NameValueCollection(),
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

		IRetryPolicy rp = stream.canSeek() ? container.getRetryPolicy() : RetryPolicies.noRetry();//container.getRetryPolicy();
		final long originalPosition = stream.canSeek() ? stream.getPosition() : 0;
		if (transferAsChunks && blobProperties != null && blobProperties[0].getContentLength() > 0) {
			// Chunked download. Obtain ranges of the blobs in 'BlockSize'
			// chunks
			// Ensure that the If-Match <Etag>header is used on each request so
			// that we are assured that all data belongs to the single blob we
			// started downloading.
			final long[] location = new long[] { range.getStartOffset() };

			while (location[0] < blobProperties[0].getContentLength() && location[0] < range.getEndOffset()) {
				if (isStopped()) {
					throw new IOException("Download blob progress is terminated.");
				}

				long contentToBeRead = Math.min(blobProperties[0].getContentLength() - location[0],
						range.getEndOffset() - location[0]);
				final long nBytes = Math.min(contentToBeRead, getBlockSize());

				rp.execute(new Callable<Object>() {

					public Object call() throws Exception {
						// Set the position to rewind in case of a retry.
						if (stream.canSeek()) {
							stream.setPosition(originalPosition + location[0]);
						}
						downloadData(blobName, stream, oldETag, blobProperties[0].getETag(), location[0], nBytes,
								new NameValueCollection(), localModified);
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
					BlobProperties blob = downloadData(blobName, stream, oldETag, null, 0, 0,
							new NameValueCollection(), localModified);
					blobProperties[0] = blob;
					return blob;
				}
			});
		}
		modified.setValue(localModified.getValue());
		return blobProperties[0];
	}
}

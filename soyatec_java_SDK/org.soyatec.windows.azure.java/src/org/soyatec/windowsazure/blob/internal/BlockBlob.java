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
import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.soyatec.windowsazure.authenticate.Base64;
import org.soyatec.windowsazure.blob.IBlobContainer;
import org.soyatec.windowsazure.blob.IBlobContents;
import org.soyatec.windowsazure.blob.IBlobProperties;
import org.soyatec.windowsazure.blob.IBlockBlob;
import org.soyatec.windowsazure.blob.IRetryPolicy;
import org.soyatec.windowsazure.blob.io.BlobMemoryStream;
import org.soyatec.windowsazure.blob.io.BlobStream;
import org.soyatec.windowsazure.constants.BlobBlockConstants;
import org.soyatec.windowsazure.constants.XmlElementNames;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.internal.ResourceUriComponents;
import org.soyatec.windowsazure.internal.constants.CompConstants;
import org.soyatec.windowsazure.internal.constants.HeaderNames;
import org.soyatec.windowsazure.internal.constants.HttpMethod;
import org.soyatec.windowsazure.internal.constants.HttpWebResponse;
import org.soyatec.windowsazure.internal.constants.QueryParams;
import org.soyatec.windowsazure.internal.constants.XmsVersion;
import org.soyatec.windowsazure.internal.util.HttpUtilities;
import org.soyatec.windowsazure.internal.util.Logger;
import org.soyatec.windowsazure.internal.util.NameValueCollection;
import org.soyatec.windowsazure.internal.util.Utilities;
import org.soyatec.windowsazure.internal.util.ssl.SslUtil;

public class BlockBlob extends Blob  implements  IBlockBlob {

	BlockBlob(BlobContainerRest container, String blobName) {
		super(container, blobName);
	}

	public void setContents(IBlobContents contents) throws StorageException {
		try {
			putBlobImpl(new BlobProperties(blobName), contents.getStream(),
					true, null);
		} catch (Exception e) {
			throw HttpUtilities.translateWebException(e);
		}
	}


	boolean putBlobImpl(final IBlobProperties blobProperties,
			final BlobStream stream, final boolean overwrite, final String eTag)
			throws Exception {
		if (blobProperties == null) {
			throw new IllegalArgumentException(
					"Blob properties cannot be null or empty!");
		}

		if (stream == null) {
			throw new IllegalArgumentException(
					"Stream cannot be null or empty!");
		}

		if (container.getName().equals(IBlobContainer.ROOT_CONTAINER)
				&& blobProperties.getName().indexOf('/') > -1) {
			throw new IllegalArgumentException(
					"Blobs stored in the root container can not have a name containing a forward slash (/).");
		}

		// If the blob is large, we should use blocks to upload it in pieces.
		// This will ensure that a broken connection will only impact a single
		// piece
		final long originalPosition = stream.getPosition();
		final long length = stream.length() - stream.getPosition();
		if (length > BlobBlockConstants.MaximumBlobSizeBeforeTransmittingAsBlocks) {
			return putLargeBlobImpl(blobProperties, stream, overwrite, eTag);
		}

		boolean retval = false;
		IRetryPolicy policy = stream.canSeek() ? container.getRetryPolicy()
				: RetryPolicies.noRetry();
		retval = (Boolean) policy.execute(new Callable<Boolean>() {

			public Boolean call() throws Exception {
				if (stream.canSeek()) {
					stream.setPosition(originalPosition);
				}

				return uploadData(blobProperties, stream, length, overwrite,
						eTag, new NameValueCollection());
			}

		});

		return retval;
	}

	boolean uploadData(IBlobProperties blobProperties, BlobStream stream,
			long length, boolean overwrite, String eTag,
			NameValueCollection queryParameters) throws Exception {

		// fix root container
		boolean isRoot = container.getName().equals(
				IBlobContainer.ROOT_CONTAINER);
		String containerName = isRoot ? "" : container.getName();
		ResourceUriComponents uriComponents = new ResourceUriComponents(
				container.getAccountName(), containerName,
				blobProperties.getName());
		URI blobUri = HttpUtilities.createRequestUri(container.getBaseUri(),
				container.isUsePathStyleUris(), container.getAccountName(),
				containerName, blobProperties.getName(),
				container.getTimeout(), queryParameters, uriComponents);
		
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

		HttpRequest request = createHttpRequestForPutBlob(blobUri,
				HttpMethod.Put, blobProperties, length, overwrite, eTag);
		if (isRoot) {
			request.addHeader(HeaderNames.ApiVersion,
					XmsVersion.VERSION_2009_07_17);
		}
		
		if(blobProperties.getContentMD5() != null){
			request.addHeader(HeaderNames.BlobContentMD5, blobProperties.getContentMD5());
		}
		
		container.credentials.signRequest(request, uriComponents);
		boolean retval = false;
		BlobStream requestStream = new BlobMemoryStream();
		Utilities.copyStream(stream, requestStream, (int) length);
		((HttpEntityEnclosingRequest) request).setEntity(new ByteArrayEntity(
				requestStream.getBytes()));
		HttpWebResponse response = null;
		if (SSLProperties.isSSL()) {
			SSLSocketFactory factory = SslUtil.createSSLSocketFactory(
					SSLProperties.getKeyStore(), SSLProperties
							.getKeyStorePasswd(), SSLProperties
							.getTrustStore(), SSLProperties
							.getTrustStorePasswd(), SSLProperties
							.getKeyAlias());
			response = HttpUtilities.getSSLReponse((HttpUriRequest) request,
					factory);
		} else {
			response = HttpUtilities.getResponse(request);
		}
		if (response.getStatusCode() == HttpStatus.SC_CREATED) {
			retval = true;
		} else if (!overwrite
				&& (response.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED || response
						.getStatusCode() == HttpStatus.SC_NOT_MODIFIED)) {
			retval = false;
		} else {
			retval = false;
			HttpUtilities.processUnexpectedStatusCode(response);
		}

		blobProperties.setLastModifiedTime(response.getLastModified());
		blobProperties.setETag(response.getHeader(HeaderNames.ETag));
		requestStream.close();
		response.close();
		return retval;
	}

	private HttpRequest createHttpRequestForPutBlob(URI blobUri,
			String httpMethod, IBlobProperties blobProperties,
			long contentLength, boolean overwrite, String eTag) {
		HttpRequest request = HttpUtilities.createHttpRequestWithCommonHeaders(
				blobUri, httpMethod, container.getTimeout());
		if (blobProperties.getContentEncoding() != null) {
			request.addHeader(HeaderNames.ContentEncoding,
					blobProperties.getContentEncoding());
		}
		if (blobProperties.getContentLanguage() != null) {
			request.addHeader(HeaderNames.ContentLanguage,
					blobProperties.getContentLanguage());
		}
		if (blobProperties.getContentType() != null) {
			request.addHeader(HeaderNames.ContentType,
					blobProperties.getContentType());
		}
		if (eTag != null) {
			request.addHeader(HeaderNames.IfMatch, eTag);
		}

		if (blobProperties.getMetadata() != null
				&& blobProperties.getMetadata().size() > 0) {
			HttpUtilities.addMetadataHeaders(request,
					blobProperties.getMetadata());
		}
		// request.addHeader(HeaderNames.ContentLength,
		// String.valueOf(contentLength));

		if (!overwrite) {
			request.addHeader(HeaderNames.IfNoneMatch, "*");
		}
		return request;
	}

	boolean putLargeBlobImpl(final IBlobProperties blobProperties,
			final BlobStream stream, final boolean overwrite, final String eTag)
			throws Exception {
		boolean retval = false;
		// Since we got a large block, chunk it into smaller pieces called
		// blocks
		// final long blockSize = BlobBlockConstants.BlockSize;
		final long startPosition = stream.getPosition();
		final long length = stream.length() - startPosition;
		int numBlocks = (int) Math.ceil((double) length / blockSize);
		String[] blockIds = new String[numBlocks];

		// We can retry only if the stream supports seeking. An alternative is
		// to buffer the data in memory
		// but we do not do this currently.
		IRetryPolicy policy = stream.canSeek() ? container.getRetryPolicy()
				: RetryPolicies.noRetry();
		// Upload each of the blocks, retrying any failed uploads
		String md5 = blobProperties.getContentMD5();
		blobProperties.setContentMD5(null);
		for (int i = 0; i < numBlocks; ++i) {
			String generateBlockId = generateBlockId(i);
			final String blockId = Base64.encode(generateBlockId
					.getBytes("UTF-8"));
			blockIds[i] = blockId;
			Logger.debug("Block Id:" + blockIds[i]);
			final int index = i;
			retval = (Boolean) policy.execute(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					// Rewind the stream to appropriate location in case this is
					// a retry
					if (stream.canSeek()) {
						stream.setPosition(startPosition + index * blockSize);
					}
					NameValueCollection params = new NameValueCollection();
					params.put(QueryParams.QueryParamComp, CompConstants.Block);
					params.put(QueryParams.QueryParamBlockId, blockId);
					long blockLength = Math.min(blockSize,
							length - stream.getPosition());
					return uploadData(blobProperties, stream, blockLength,
							overwrite, eTag, params);

				}
			});
		}
		blobProperties.setContentMD5(md5);
		retval = putBlockListImpl(blobProperties, blockIds, overwrite, eTag);

		return retval;
	}

	boolean putBlockListImpl(final IBlobProperties blobProperties,
			String[] blockIds, final boolean overwrite, final String eTag)
			throws Exception, IOException {
		boolean retval;
		// Now commit the list
		// First create the output
		Document doc = DocumentHelper.createDocument();
		Element blockListElement = doc.addElement(XmlElementNames.BlockList);
		for (String id : blockIds) {
			blockListElement.addElement(XmlElementNames.Block).setText(id);
		}

		NameValueCollection params = new NameValueCollection();
		params.put(QueryParams.QueryParamComp, CompConstants.BlockList);
		BlobStream buffer = new BlobMemoryStream(doc.asXML().getBytes());
		retval = uploadData(blobProperties, buffer, buffer.length(), overwrite,
				eTag, params);
		return retval;
	}

	/**
	 * For a given blob, the length of the value specified for the blockid
	 * parameter must be the same size for each block.
	 *
	 * For more, see <a
	 * href="http://msdn.microsoft.com/en-us/library/dd135726.aspx">Put
	 * block</a>
	 *
	 *
	 * @param i
	 * @return
	 */
	String generateBlockId(int i) {
		String value = String.valueOf(i);
		while (value.length() < 64) {
			value = "0" + value;
		}
		return value;
	}

	public boolean updateIfNotModified(IBlobProperties blobProperties,
			IBlobContents contents) throws StorageException {
		try {
			if(blobProperties == null)
				blobProperties = new BlobProperties(blobName);
			if(contents != null){
				return putBlobImpl(blobProperties, contents.getStream(), true,
							blobProperties.getETag());
			}else{
				return setPropertiesImpl(blobProperties, blobProperties.getETag());
			}
		} catch (Exception e) {
			throw HttpUtilities.translateWebException(e);
		}
	}

	public void putBlockList(List<String> blockList) {
		try{
			putBlockListImpl(new BlobProperties(blobName), blockList.toArray(new String[blockList.size()]), true, null);
		} catch (Exception e) {
			throw HttpUtilities.translateWebException(e);
		}
	}

	public void putBlock(String blockId, IBlobContents contents){
		NameValueCollection params = new NameValueCollection();
		params.put(QueryParams.QueryParamComp, CompConstants.Block);
		params.put(QueryParams.QueryParamBlockId, blockId);
		BlobStream stream = contents.getStream();
		try{
			uploadData(new BlobProperties(blobName), stream, stream.length(), true,	null, params);
		} catch (Exception e) {
			throw HttpUtilities.translateWebException(e);
		}
	}

}

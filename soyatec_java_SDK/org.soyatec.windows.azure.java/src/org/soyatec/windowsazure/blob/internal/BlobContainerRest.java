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

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.dom4j.Document;
import org.dom4j.Element;
import org.soyatec.windowsazure.authenticate.Base64;
import org.soyatec.windowsazure.authenticate.HttpRequestAccessor;
import org.soyatec.windowsazure.authenticate.IAccessPolicy;
import org.soyatec.windowsazure.authenticate.SharedKeyCredentials;
import org.soyatec.windowsazure.authenticate.SharedKeyCredentialsWrapper;
import org.soyatec.windowsazure.blob.BlobType;
import org.soyatec.windowsazure.blob.DateTime;
import org.soyatec.windowsazure.blob.IBlob;
import org.soyatec.windowsazure.blob.IBlobConstraints;
import org.soyatec.windowsazure.blob.IBlobContents;
import org.soyatec.windowsazure.blob.IBlobProperties;
import org.soyatec.windowsazure.blob.IBlockBlob;
import org.soyatec.windowsazure.blob.IContainerAccessControl;
import org.soyatec.windowsazure.blob.IContainerProperties;
import org.soyatec.windowsazure.blob.IPageBlob;
import org.soyatec.windowsazure.blob.IRetryPolicy;
import org.soyatec.windowsazure.blob.ISharedAccessUrl;
import org.soyatec.windowsazure.blob.LeaseMode;
import org.soyatec.windowsazure.blob.LeaseStatus;
import org.soyatec.windowsazure.blob.SharedAccessPermissions;
import org.soyatec.windowsazure.constants.ConstChars;
import org.soyatec.windowsazure.constants.XmlElementNames;
import org.soyatec.windowsazure.error.StorageErrorCode;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.error.StorageServerException;
import org.soyatec.windowsazure.internal.AccessPolicy;
import org.soyatec.windowsazure.internal.OutParameter;
import org.soyatec.windowsazure.internal.ResourceUriComponents;
import org.soyatec.windowsazure.internal.SignedIdentifier;
import org.soyatec.windowsazure.internal.constants.CompConstants;
import org.soyatec.windowsazure.internal.constants.HeaderNames;
import org.soyatec.windowsazure.internal.constants.HttpMethod;
import org.soyatec.windowsazure.internal.constants.HttpWebResponse;
import org.soyatec.windowsazure.internal.constants.ListingConstants;
import org.soyatec.windowsazure.internal.constants.QueryParams;
import org.soyatec.windowsazure.internal.constants.XmsVersion;
import org.soyatec.windowsazure.internal.util.HttpUtilities;
import org.soyatec.windowsazure.internal.util.Logger;
import org.soyatec.windowsazure.internal.util.NameValueCollection;
import org.soyatec.windowsazure.internal.util.TimeSpan;
import org.soyatec.windowsazure.internal.util.Utilities;
import org.soyatec.windowsazure.internal.util.xml.AtomUtil;
import org.soyatec.windowsazure.internal.util.xml.XPathQueryHelper;
import org.soyatec.windowsazure.internal.util.xml.XmlUtil;

public class BlobContainerRest extends BlobContainer {

	private byte[] key;
	SharedKeyCredentials credentials;
	private ISharedAccessUrl shareAccessUrl;

	public BlobContainerRest(URI baseUri, boolean usePathStyleUris,
			String accountName, String containerName, String base64Key,
			Timestamp lastModified, TimeSpan timeOut, IRetryPolicy retryPolicy) {
		super(baseUri, usePathStyleUris, accountName, containerName,
				lastModified);
		ResourceUriComponents uriComponents = new ResourceUriComponents(
				accountName, containerName, null);
		URI containerUri = HttpRequestAccessor.constructResourceUri(baseUri,
				uriComponents, usePathStyleUris);
		setUri(containerUri);
		if (base64Key != null) {
			key = Base64.decode(base64Key);
		}
		credentials = new SharedKeyCredentials(accountName, key);
		setTimeout(timeOut);
		setRetryPolicy(retryPolicy);
	}

	public void setSSLProperty(String keystore, String keystorePasswd,
			String truststore, String truststorepasswd, String keyalias) {
		SSLProperties.setSSLSettings(keystore, keystorePasswd,
				truststore, truststorepasswd, keyalias);
	}

	public void clearSSLProperty() {
		SSLProperties.clearSSLSettings();
	}

	/**
	 * Create a new blob or overwrite an existing blob.
	 *
	 * @param blobProperties
	 *            The properties of the blob
	 * @param blobContents
	 *            The contents of the blob
	 * @param overwrite
	 *            Should this request overwrite an existing blob
	 * @return true if the blob was created. false if the blob already exists
	 *         and parameter "overwrite" was set to false. The LastModifiedTime
	 *         property of <paramref name="blobProperties"/> is set as a result
	 *         of this call. This method also has an effect on the ETag values
	 *         that are managed by the service.
	 * @throws StorageException
	 */
	private boolean createOrUpdateBlockBlob(IBlobProperties blobProperties,
			IBlobContents blobContents, boolean overwrite)
			throws StorageException {
		String blobName = blobProperties.getName();
		if (blobName == null || blobName.equals("")) {
			throw new IllegalArgumentException("Blob name is empty.");
		}
		if (blobName.lastIndexOf('.') == blobName.length() - 1) {
			throw new IllegalArgumentException(
					MessageFormat
							.format(
									"The specified blob name \"{0}\" is not valid!"
											+ "\nPlease choose a name that conforms to the naming conventions for blob!"
											+ "\nSee <a>http://msdn.microsoft.com/en-us/library/dd135715.aspx</a> for more information.",
									blobName));
		}
		try {
			BlockBlob blob = (BlockBlob) getBlockBlobReference(blobName);
			boolean putBlobImpl = blob.putBlobImpl(blobProperties, blobContents
					.getStream(), overwrite, null);
			return putBlobImpl;
		} catch (Exception e) {
			throw HttpUtilities.translateWebException(e);
		}
	}



	public boolean isContainerExist() throws StorageException {
		boolean result = false;
		result = (Boolean) getRetryPolicy().execute(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				ResourceUriComponents uriComponents = new ResourceUriComponents(
						getAccountName(), getName(), null);
				NameValueCollection queryParams = new NameValueCollection();
				queryParams.put(QueryParams.QueryRestType,
						CompConstants.Container);

				URI uri = HttpUtilities.createRequestUri(getBaseUri(),
						isUsePathStyleUris(), getAccountName(),
						getName(), null, getTimeout(), queryParams,
						uriComponents);
				HttpRequest request = HttpUtilities
						.createHttpRequestWithCommonHeaders(uri,
								HttpMethod.Get, getTimeout());
				request.addHeader(HeaderNames.ApiVersion,
						XmsVersion.VERSION_2009_07_17);
				credentials.signRequest(request, uriComponents);
				try {
					HttpWebResponse response = HttpUtilities
							.getResponse(request);
					if (response.getStatusCode() == HttpStatus.SC_OK) {
						response.close();
						return true;
					} else if (response.getStatusCode() == HttpStatus.SC_GONE
							|| response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
						response.close();
						return false;
					} else {
						HttpUtilities.processUnexpectedStatusCode(response);
						return false;
					}
				} catch (StorageException we) {
					throw HttpUtilities.translateWebException(we);
				}
			}
		});
		return result;
	}

	public boolean isBlobExist(String blobName) throws StorageException {
		try {
			return getBlobReference(blobName).getProperties() != null;
		} catch (Exception e) {
			if (e instanceof StorageException) {
				StorageException se = (StorageException) e;
				if (se.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
					return false;
				} else {
					throw se;
				}
			}
		}
		return false;
	}

	// / <summary>
	// / Get the properties for the container if it exists.
	// / </summary>
	// / <returns>The metadata for the container if it exists, null
	// otherwise</returns>
	public IContainerProperties getProperties()
			throws StorageException {
		IContainerProperties result = null;
		try {
			result = (IContainerProperties) getRetryPolicy().execute(
					new Callable<ContainerProperties>() {
						public ContainerProperties call() throws Exception {
							ResourceUriComponents uriComponents = new ResourceUriComponents(
									getAccountName(), getName(), null);
							NameValueCollection queryParams = new NameValueCollection();
							queryParams.put(QueryParams.QueryRestType,
									CompConstants.Container);
							URI uri = HttpUtilities.createRequestUri(
									getBaseUri(), isUsePathStyleUris(),
									getAccountName(), getName(), null,
									getTimeout(), queryParams, uriComponents);
							HttpRequest request = HttpUtilities
									.createHttpRequestWithCommonHeaders(uri,
											HttpMethod.Get, getTimeout());
							request.addHeader(HeaderNames.ApiVersion,
									XmsVersion.VERSION_2009_07_17);
							credentials.signRequest(request, uriComponents);

							HttpWebResponse response = HttpUtilities
									.getResponse(request);
							if (response.getStatusCode() == HttpStatus.SC_OK) {
								return containerPropertiesFromResponse(response);
							} else if (response.getStatusCode() == HttpStatus.SC_GONE
									|| response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
								response.close();
								return null;
							} else {
								HttpUtilities
										.processUnexpectedStatusCode(response);
								return null;
							}

						}
					});
		} catch (StorageException e) {
			throw HttpUtilities.translateWebException(e);
		}
		return result;
	}

	private ContainerProperties containerPropertiesFromResponse(
			HttpWebResponse response) {
		ContainerProperties prop = new ContainerProperties(getName());
		prop.setLastModifiedTime(response.getLastModified());
		prop.setETag(response.getHeader(HeaderNames.ETag));
		prop.setUri(getUri());
		prop.setMetadata(metadataFromHeaders(response.getHeaders()));
		return prop;
	}

	NameValueCollection metadataFromHeaders(NameValueCollection headers) {
		int prefixLength = HeaderNames.PrefixForMetadata.length();

		NameValueCollection metadataEntries = new NameValueCollection();
		for (Object key : headers.keySet()) {
			String headerName = (String) key;
			if (headerName.toLowerCase().startsWith(
					HeaderNames.PrefixForMetadata)) {
				// strip out the metadata prefix
				metadataEntries.putAll(headerName.substring(prefixLength),
						headers.getCollection(headerName));
			}
		}
		return metadataEntries;
	}

	// / Get the access control permissions associated with the container.
	public void setAccessControl(final IContainerAccessControl acl)
			throws StorageException {
		try {
			getRetryPolicy().execute(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					NameValueCollection queryParams = new NameValueCollection();
					queryParams.put(QueryParams.QueryRestType,
							CompConstants.Container);
					queryParams.put(QueryParams.QueryParamComp,
							CompConstants.Acl);
					ResourceUriComponents uriComponents = new ResourceUriComponents(
							getAccountName(), getName(), null);
					URI uri = HttpUtilities.createRequestUri(getBaseUri(),
							isUsePathStyleUris(), getAccountName(),
							getName(), null, getTimeout(),
							queryParams, uriComponents);
					HttpRequest request = HttpUtilities
							.createHttpRequestWithCommonHeaders(uri,
									HttpMethod.Put, getTimeout());
					request.addHeader(HeaderNames.PublicAccess, String
							.valueOf(acl.isPublic()));
					addVerisonHeader(request);

					attachBody(acl, request);

					credentials.signRequest(request, uriComponents);
					HttpWebResponse response = HttpUtilities
							.getResponse(request);
					if (response.getStatusCode() != HttpStatus.SC_OK) {
						HttpUtilities.processUnexpectedStatusCode(response);
					} else {
						response.close();
					}
					return true;
				}

				private void attachBody(final IContainerAccessControl acl,
						HttpRequest request) {
					String atom = AtomUtil.convertACLToXml(acl);
					((HttpEntityEnclosingRequest) request)
							.setEntity(new ByteArrayEntity(atom.getBytes()));
				}

			});
		} catch (StorageException e) {
			throw HttpUtilities.translateWebException(e);

		}
	}

	private void addVerisonHeader(HttpRequest request) {
		request
				.addHeader(HeaderNames.ApiVersion,
						XmsVersion.VERSION_2009_07_17);
	}

	public ContainerAccessControl getAccessControl()
			throws StorageException {
		ContainerAccessControl accessControl = IContainerAccessControl.Private;
		try {
			accessControl = (ContainerAccessControl) getRetryPolicy().execute(
					new Callable<ContainerAccessControl>() {

						public ContainerAccessControl call() throws Exception {
							NameValueCollection queryParams = new NameValueCollection();
							queryParams.put(QueryParams.QueryParamComp,
									CompConstants.Acl);
							// New version container ACL
							queryParams.put(QueryParams.QueryRestType,
									CompConstants.Container);

							ResourceUriComponents uriComponents = new ResourceUriComponents(
									getAccountName(), getName(), null);
							URI uri = HttpUtilities.createRequestUri(
									getBaseUri(), isUsePathStyleUris(),
									getAccountName(), getName(), null,
									getTimeout(), queryParams, uriComponents);
							HttpRequest request = HttpUtilities
									.createHttpRequestWithCommonHeaders(uri,
											HttpMethod.Get, getTimeout());
							request.addHeader(HeaderNames.ApiVersion,
									XmsVersion.VERSION_2009_07_17);

							credentials.signRequest(request, uriComponents);
							HttpWebResponse response = HttpUtilities
									.getResponse(request);
							if (response.getStatusCode() == HttpStatus.SC_OK) {
								String acl = response
										.getHeader(HeaderNames.PublicAccess);
								boolean publicAcl = false;
								if (acl != null) {
									publicAcl = Boolean.parseBoolean(acl);
									List<SignedIdentifier> identifiers = getSignedIdentifiersFromResponse(response);
									ContainerAccessControl aclEntity = null;
									if (identifiers != null
											&& identifiers.size() > 0) {
										aclEntity = new ContainerAccessControl(
												publicAcl);
										aclEntity
												.setSigendIdentifiers(identifiers);
									} else {
										aclEntity = publicAcl ? IContainerAccessControl.Public
												: IContainerAccessControl.Private;
									}
									response.close();
									return aclEntity;
								} else {
									response.close();
									throw new StorageServerException(
											StorageErrorCode.ServiceBadResponse,
											"The server did not respond with expected container access control header",
											response.getStatusCode(), null);
								}
							} else {
								HttpUtilities
										.processUnexpectedStatusCode(response);
								return null;
							}
						}

					});
		} catch (Exception e) {
			throw HttpUtilities.translateWebException(e);

		}
		return accessControl;
	}

	@SuppressWarnings("unchecked")
	private List<SignedIdentifier> getSignedIdentifiersFromResponse(
			HttpWebResponse response) {
		InputStream stream = response.getStream();
		if (stream == null) {
			return Collections.EMPTY_LIST;
		}
		try {
			Document doc = XmlUtil.load(stream,
					"Container access control parsed error.");
			List selectNodes = doc
					.selectNodes(XPathQueryHelper.SignedIdentifierListQuery);
			List<SignedIdentifier> result = new ArrayList<SignedIdentifier>();
			if (selectNodes.size() > 0) {
				for (Iterator iter = selectNodes.iterator(); iter.hasNext();) {
					Element element = (Element) iter.next();
					SignedIdentifier identifier = new SignedIdentifier();
					identifier
							.setId(XPathQueryHelper
									.loadSingleChildStringValue(
											element,
											XmlElementNames.ContainerSignedIdentifierId,
											true));
					IAccessPolicy policy = new AccessPolicy();
					Element accesPlocy = (Element) element
							.selectSingleNode(XmlElementNames.ContainerAccessPolicyName);
					if (accesPlocy != null && accesPlocy.hasContent()) {
						policy
								.setStart(new DateTime(
										XPathQueryHelper
												.loadSingleChildStringValue(
														accesPlocy,
														XmlElementNames.ContainerAccessPolicyStart,
														true)));
						policy
								.setExpiry(new DateTime(
										XPathQueryHelper
												.loadSingleChildStringValue(
														accesPlocy,
														XmlElementNames.ContainerAccessPolicyExpiry,
														true)));
						policy
								.setPermission(SharedAccessPermissions
										.valueOf(XPathQueryHelper
												.loadSingleChildStringValue(
														accesPlocy,
														XmlElementNames.ContainerAccessPolicyPermission,
														true)));
						identifier.setPolicy(policy);
					}

					result.add(identifier);
				}
			}
			return result;
		} catch (Exception e) {
			// For dev local storage, Container access control may have no
			// detail.
			Logger.error("Parse container accesss control error", e);
			return Collections.EMPTY_LIST;
		}
	}

	public boolean deleteBlob(final String name) throws StorageException {
		return deleteBlobImpl(name, null, new OutParameter<Boolean>(false));
	}

	public boolean deleteBlobIfNotModified(IBlobProperties blob)
			throws StorageException {
		OutParameter<Boolean> modified = new OutParameter<Boolean>(false);
		boolean result = deleteBlobImpl(blob.getName(), blob.getETag(),
				modified);
		if (modified.getValue()) {
			throw new StorageException(
					"The blob was not deleted because it was modified.");
		} else {
			return result;
		}
	}

	private boolean deleteBlobImpl(final String name, final String eTag,
			final OutParameter<Boolean> unused) throws StorageException {
		if (Utilities.isNullOrEmpty(name)) {
			throw new IllegalArgumentException(
					"Blob name cannot be null or empty!");
		}

		final OutParameter<Boolean> retval = new OutParameter<Boolean>(false);
		final OutParameter<Boolean> localModified = new OutParameter<Boolean>(
				false);

		getRetryPolicy().execute(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				String container = getName();
				if (container.equals(ROOT_CONTAINER)) {
					container = "";
				}
				ResourceUriComponents uriComponents = new ResourceUriComponents(
						getAccountName(), container, name);
				URI blobUri = HttpUtilities.createRequestUri(getBaseUri(),
						isUsePathStyleUris(), getAccountName(), container,
						name, getTimeout(), new NameValueCollection(),
						uriComponents);
				HttpRequest request = HttpUtilities
						.createHttpRequestWithCommonHeaders(blobUri,
								HttpMethod.Delete, getTimeout());
				request.addHeader(HeaderNames.ApiVersion,
						XmsVersion.VERSION_2009_07_17);
				if (!Utilities.isNullOrEmpty(eTag)) {
					request.addHeader(HeaderNames.IfMatch, eTag);
				}
				credentials.signRequest(request, uriComponents);
				try {
					HttpWebResponse response = HttpUtilities
							.getResponse(request);
					int status = response.getStatusCode();
					if (status == HttpStatus.SC_OK
							|| status == HttpStatus.SC_ACCEPTED) {
						response.close();
						retval.setValue(true);
					} else if (status == HttpStatus.SC_NOT_FOUND
							|| status == HttpStatus.SC_GONE) {
						localModified.setValue(true);
						HttpUtilities.processUnexpectedStatusCode(response);
					} else if (status == HttpStatus.SC_PRECONDITION_FAILED
							|| status == HttpStatus.SC_NOT_MODIFIED) {
						localModified.setValue(true);
						HttpUtilities.processUnexpectedStatusCode(response);
					} else {
						HttpUtilities.processUnexpectedStatusCode(response);
					}
				} catch (StorageException ioe) {
					HttpUtilities.translateWebException(ioe);
				}
				return null;
			}
		});
		unused.setValue(localModified.getValue());
		return retval.getValue();
	}

	URI constructBlobUri(String blobName) {
		ResourceUriComponents uriComponents = new ResourceUriComponents(
				getAccountName(), getName(), blobName);
		return HttpUtilities.createRequestUri(getBaseUri(),
				isUsePathStyleUris(), getAccountName(), getName(),
				blobName, null, new NameValueCollection(), uriComponents);
	}

	private URI removeQueryParams(URI blobUri) throws URISyntaxException {
		String uri = blobUri.toString();
		int pos = uri.indexOf('?');
		if (pos < 0) {
			return blobUri;
		} else {
			return new URI(uri.substring(0, pos));
		}
	}



	public Iterator<IBlobProperties> listBlobs() {
		return listBlobs(null, false);
	}

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
			throws StorageException {
		if (maxResults <= 0) {
			throw new IllegalArgumentException(
					"maxResults should be positive value.");
		}

		ListBlobsResult all = new ListBlobsResult(
				new ArrayList<IBlobProperties>(), new ArrayList<String>(), "");
		String marker = "";

		String delimiter = combineCommonPrefixes ? ConstChars.Slash : Utilities
				.emptyString();
		List<IBlobProperties> blobs = all.getBlobsProperties();
		do {
			ListBlobsResult partResult = listBlobsImpl(prefix, marker,
					delimiter, maxResults);
			marker = partResult.getNextMarker();
			blobs.addAll(partResult.getBlobsProperties());
			all.getCommonPrefixs().addAll(partResult.getCommonPrefixs());
		} while (marker != null);

		if (blobs != null) {
			return blobs.iterator();
		} else {
			return null;
		}
	}

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
			boolean combineCommonPrefixes) throws StorageException {
		final int maxResults = ListingConstants.MaxBlobListResults;
		try {
			return listBlobs(prefix, combineCommonPrefixes, maxResults);
		} catch (StorageException se) {
			throw HttpUtilities.translateWebException(se);
		}
	}

	private ListBlobsResult listBlobsImpl(final String prefix,
			final String fromMarker, final String delimiter,
			final int maxResults) throws StorageException {
		final OutParameter<ListBlobsResult> result = new OutParameter<ListBlobsResult>();
		getRetryPolicy().execute(new Callable<Object>() {
			public Object call() throws Exception {
				NameValueCollection queryParameters = createRequestUriForListing(
						prefix, fromMarker, delimiter, maxResults);
				ResourceUriComponents uriComponents = new ResourceUriComponents(
						getAccountName(), getName(), null);
				queryParameters.put(QueryParams.QueryRestType,
						CompConstants.Container);
				URI uri = HttpUtilities.createRequestUri(getBaseUri(),
						isUsePathStyleUris(), getAccountName(),
						getName(), null, getTimeout(),
						queryParameters, uriComponents);
				HttpRequest request = HttpUtilities
						.createHttpRequestWithCommonHeaders(uri,
								HttpMethod.Get, getTimeout());
				request.setHeader(HeaderNames.ApiVersion,
				// XmsVersion.VERSION_2009_07_17);
						XmsVersion.VERSION_2009_09_19);
				credentials.signRequest(request, uriComponents);

				HttpWebResponse response = HttpUtilities.getResponse(request);
				if (response.getStatusCode() == HttpStatus.SC_OK) {
					result
							.setValue(parseBlobFromResponse(response
									.getStream()));
					response.close();
				} else {
					XmlUtil.load(response.getStream());
					HttpUtilities.processUnexpectedStatusCode(response);
				}
				return null;
			}
		});
		return result.getValue();
	}

	/**
	 * Retrieve the blob meta-data from the response body in XML format.
	 *
	 * @param stream
	 *            HTTP response body
	 * @return an ListBlobsResult
	 * @throws StorageServerException
	 */
	@SuppressWarnings("unchecked")
	private ListBlobsResult parseBlobFromResponse(InputStream stream)
			throws StorageServerException {
		List<IBlobProperties> blobs = new ArrayList<IBlobProperties>();
		List<String> commonPrefixes = new ArrayList<String>();
		String nextMarker = null;
		Document document = XmlUtil.load(stream);
		// Get the commonPrefixes
		List xmlNodes = document
				.selectNodes(XPathQueryHelper.CommonPrefixQuery);
		for (Iterator iterator = xmlNodes.iterator(); iterator.hasNext();) {
			Element element = (Element) iterator.next();
			String blobPrefix = XPathQueryHelper.loadSingleChildStringValue(
					element, XmlElementNames.BlobPrefixName, false);
			commonPrefixes.add(blobPrefix);
		}

		// Get all the blobs returned as the listing results
		xmlNodes = document.getRootElement().element("Blobs").elements("Blob");
		//xmlNodes = document.selectNodes(XPathQueryHelper.BlobQuery);
		for (Iterator iterator = xmlNodes.iterator(); iterator.hasNext();) {
			/*
			 * Parse the Blob meta-data from response XML content.
			 */
			Element blobNode = (Element) iterator.next();
			// @Note: update for new version
			Element propNode = blobNode.element(XmlElementNames.BlobProperties);
			if (propNode == null)
				blobs.add(parseBlobInfo(blobNode));
			else
				blobs.add(parseBlobInfo2(blobNode, propNode));
		}

		// Get the nextMarker
		Element nextMarkerNode = (Element) document
				.selectSingleNode(XPathQueryHelper.NextMarkerQuery);
		if (nextMarkerNode != null && nextMarkerNode.hasContent()) {
			nextMarker = nextMarkerNode.getStringValue();
		}

		return new ListBlobsResult(blobs, commonPrefixes, nextMarker);
	}

	private BlobProperties parseBlobInfo(Element blobNode) {

		String blobUrl = XPathQueryHelper.loadSingleChildStringValue(blobNode,
				XmlElementNames.Url, true);
		String blobName = XPathQueryHelper.loadSingleChildStringValue(blobNode,
				XmlElementNames.BlobName, true);

		Timestamp lastModified = XPathQueryHelper.loadSingleChildDateTimeValue(
				blobNode, XmlElementNames.LastModified, false);
		String eTag = XPathQueryHelper.loadSingleChildStringValue(blobNode,
				XmlElementNames.Etag, false);

		String contentType = XPathQueryHelper.loadSingleChildStringValue(
				blobNode, XmlElementNames.ContentType, false);
		String contentEncoding = XPathQueryHelper.loadSingleChildStringValue(
				blobNode, XmlElementNames.ContentEncoding, false);
		String contentLanguage = XPathQueryHelper.loadSingleChildStringValue(
				blobNode, XmlElementNames.ContentLanguage, false);
		Long blobSize = XPathQueryHelper.loadSingleChildLongValue(blobNode,
				XmlElementNames.Size, false);

		BlobProperties properties = new BlobProperties(blobName);
		if (lastModified != null) {
			properties.setLastModifiedTime(lastModified);
		}
		properties.setContentType(contentType);
		properties.setContentEncoding(contentEncoding);
		properties.setContentLanguage(contentLanguage);
		properties.setETag(eTag);
		properties.setContentLength(blobSize);
		// @Note: blank character in url
		blobUrl = Utilities.fixRootContainer(blobUrl);

		blobUrl = blobUrl.replaceAll(" ", "%20");
		properties.setUri(URI.create(blobUrl));
		return properties;
	}

	// <Blob><Name>"aaa"</Name><Url>http://storageexplorer.blob.core.windows.net/publicfiles/"aaa"</Url><Properties><Last-Modified>Thu,
	// 28 Jan 2010 23:28:19
	// GMT</Last-Modified><Etag>0x8CC6E88B57B45ED</Etag><Content-Length>0</Content-Length><Content-Type>application/octet-stream</Content-Type><Content-Encoding
	// /><Content-Language /><Content-MD5 /><Cache-Control
	// /><BlobType>BlockBlob</BlobType><LeaseStatus>unlocked</LeaseStatus></Properties></Blob>
	private BlobProperties parseBlobInfo2(Element blobNode, Element propNode) {

		String blobUrl = XPathQueryHelper.loadSingleChildStringValue(blobNode,
				XmlElementNames.Url, true);
		String blobName = XPathQueryHelper.loadSingleChildStringValue(blobNode,
				XmlElementNames.BlobName, true);
		String snapshot = XPathQueryHelper.loadSingleChildStringValue(blobNode,
				XmlElementNames.BlobSnapshot, false);

		String blobType = XPathQueryHelper.loadSingleChildStringValue(propNode,
				XmlElementNames.BlobType, false);

		String leaseStatus = XPathQueryHelper.loadSingleChildStringValue(
				propNode, XmlElementNames.LeaseStatus, false);

		Timestamp lastModified = XPathQueryHelper.loadSingleChildDateTimeValue(
				propNode, HeaderNames.LastModifiedTime, false);
		String eTag = XPathQueryHelper.loadSingleChildStringValue(propNode,
				XmlElementNames.Etag, false);

		String contentType = XPathQueryHelper.loadSingleChildStringValue(
				propNode, HeaderNames.ContentType, false);
		String contentEncoding = XPathQueryHelper.loadSingleChildStringValue(
				propNode, HeaderNames.ContentEncoding, false);
		String contentLanguage = XPathQueryHelper.loadSingleChildStringValue(
				propNode, HeaderNames.ContentLanguage, false);
		Long blobSize = XPathQueryHelper.loadSingleChildLongValue(propNode,
				HeaderNames.ContentLength, false);
		String contentMd5  = XPathQueryHelper.loadSingleChildStringValue(propNode,
				HeaderNames.ContentMD5, false);

		BlobProperties properties = new BlobProperties(blobName);
		if (lastModified != null) {
			properties.setLastModifiedTime(lastModified);
		}
		properties.setContentType(contentType);
		properties.setContentEncoding(contentEncoding);
		properties.setContentLanguage(contentLanguage);
		properties.setETag(eTag);
		properties.setContentMD5(contentMd5);

		if (blobSize != null)
			properties.setContentLength(blobSize);

		// @Note: blank character in url
		blobUrl = Utilities.fixRootContainer(blobUrl);

		blobUrl = blobUrl.replaceAll(" ", "%20");
		properties.setUri(URI.create(blobUrl));

		properties.setBlobType(BlobType.parse(blobType));
		properties.setLeaseStatus(LeaseStatus.parse(leaseStatus));

		if (snapshot != null)
			try {
				properties.setSnapshot(Utilities
						.tryGetDateTimeFromTableEntry(snapshot));
			} catch (ParseException e) {
			}
		return properties;
	}

	private NameValueCollection createRequestUriForListing(String prefix,
			String fromMarker, String delimiter, int maxResults) {
		NameValueCollection queryParams = new NameValueCollection();
		queryParams.put(QueryParams.QueryParamComp, CompConstants.List);

		if (!Utilities.isNullOrEmpty(prefix)) {
			queryParams.put(QueryParams.QueryParamPrefix, prefix);
		}

		if (!Utilities.isNullOrEmpty(fromMarker)) {
			queryParams.put(QueryParams.QueryParamMarker, fromMarker);
		}

		if (!Utilities.isNullOrEmpty(delimiter)) {
			queryParams.put(QueryParams.QueryParamDelimiter, delimiter);
		}

		queryParams.put(QueryParams.QueryParamMaxResults, Integer
				.toString(maxResults));
		return queryParams;
	}

//	/**
//	 * Gets the blob contents and properties if the blob has been modified
//	 * since the time specified. Use this method if you have cached the contents
//	 * of a blob and want to avoid retrieving the blob if it has not changed
//	 * since the last time you retrieved it.
//	 *
//	 * @param blobProperties
//	 *            The properties of the blob obtained from an earlier call to
//	 *            GetBlob. This parameter is updated by the call if the blob has
//	 *            been modified
//	 * @param blobContents
//	 *            Contains the stream to which the contents of the blob are
//	 *            written if it has been modified
//	 * @param transferAsChunks
//	 *            Should the blob be gotten in pieces. This requires more
//	 *            round-trips, but will retry smaller pieces in case of failure.
//	 * @return true if the blob has been modified, false otherwise
//	 */
//	public boolean isBlobModified(BlobProperties blobProperties,
//			IBlobContents blobContents, boolean transferAsChunks)
//			throws StorageException {
//		try {
//			OutParameter<Boolean> modified = new OutParameter<Boolean>(true);
//			Blob blob = (Blob) getBlobReference(blobProperties.getName());
//			BlobProperties newBlob = blob.getPropertiesImpl(blobProperties
//					.getName(), blobContents.getStream(), blobProperties
//					.getETag(), transferAsChunks, modified);
//			if (modified.getValue()) {
//				blobProperties.assign(newBlob);
//			}
//			return modified.getValue();
//		} catch (Exception e) {
//			throw HttpUtilities.translateWebException(e);
//		}
//	}

	public IBlob getBlobReference(String name){
		BlockBlob blob = new BlockBlob(this, name);
		IBlobProperties properties = blob.getProperties();
		if(properties == null)
			return blob;  // blob is not created
		else if( BlobType.BlockBlob.equals(properties.getBlobType() ))
			return blob;

		return getPageBlobReference(name);
	}

	public IBlockBlob getBlockBlobReference(String name){
		return new BlockBlob(this, name);
	}

	public IPageBlob getPageBlobReference(String name){
		return new PageBlob(this, name);
	}

	public boolean copyBlob(String destContainer, String destBlobName,
			String sourceBlobName) throws StorageException {
		return copyBlobImpl(destContainer, destBlobName, sourceBlobName, null,
				null);
	}

	public boolean copyBlob(String destContainer, String destBlobName,
			String sourceBlobName, NameValueCollection metadata,
			IBlobConstraints constraints) throws StorageException {
		return copyBlobImpl(destContainer, destBlobName, sourceBlobName,
				metadata, constraints);
	}

	private boolean copyBlobImpl(String destContainer, String destBlobName,
			final String sourceBlobName, final NameValueCollection metadata,
			final IBlobConstraints constraints) throws StorageException {

		if (Utilities.isNullOrEmpty(sourceBlobName)) {
			throw new IllegalArgumentException(
					"Source blob name cannot be null or empty!");
		}

		final String container = Utilities.isNullOrEmpty(destContainer) ? getName()
				: destContainer;
		final String blob = Utilities.isNullOrEmpty(destBlobName) ? sourceBlobName
				: destBlobName;

		if (container.equals(getName()) && blob.equals(sourceBlobName)) {
			throw new IllegalArgumentException(
					"Destnation blob and source blob could not be the same.");
		}

		final OutParameter<Boolean> retval = new OutParameter<Boolean>(false);
		getRetryPolicy().execute(new Callable<Object>() {

			public Object call() throws Exception {
				final ResourceUriComponents uriComponents = new ResourceUriComponents(
						getAccountName(), container, blob);
				URI uri = HttpUtilities.createRequestUri(getBaseUri(),
						isUsePathStyleUris(), getAccountName(), container,
						blob, getTimeout(), null, uriComponents);
				HttpRequest request = HttpUtilities
						.createHttpRequestWithCommonHeaders(uri,
								HttpMethod.Put, getTimeout());

				request.addHeader(HeaderNames.ApiVersion,
						XmsVersion.VERSION_2009_07_17);
				// @NOTE: blank name
				String blobName = createCopySourceHeaderValue(sourceBlobName)
						.replaceAll(" ", "%20");
				request.addHeader(HeaderNames.CopySource, blobName);
				// add constraints
				addMoreConstraints(constraints, request);

				if (metadata != null) {
					HttpUtilities.addMetadataHeaders(request, metadata);
				}

				credentials.signRequest(request, uriComponents);
				HttpWebResponse response = HttpUtilities.getResponse(request);
				int statusCode = response.getStatusCode();
				if (statusCode == HttpStatus.SC_CREATED) {
					response.close();
					retval.setValue(true);
				} else {
					retval.setValue(false);
					HttpUtilities.processUnexpectedStatusCode(response);
				}
				return retval;
			}

		});
		return retval.getValue();
	}

	private void addMoreConstraints(final IBlobConstraints constraints,
			HttpRequest request) {
		if (constraints != null) {
			List<BasicHeader> headers = constraints.getConstraints();
			if (headers != null && !headers.isEmpty()) {
				for (BasicHeader header : headers) {
					request.addHeader(header);
				}
			}
		}
	}

	private String createCopySourceHeaderValue(final String sourceBlobName) {
		return String.format("/%s/%s/%s", getAccountName(), getName(),
				sourceBlobName);
	}


	@Override
	public void clearSharedAccessUrl() {
		this.shareAccessUrl = null;
		if (credentials instanceof SharedKeyCredentialsWrapper) {
			SharedKeyCredentialsWrapper warpper = (SharedKeyCredentialsWrapper) credentials;
			credentials = warpper.getCredentials();
		}
	}

	@Override
	public void useSharedAccessUrl(ISharedAccessUrl url) {
		if (url == null) {
			throw new IllegalArgumentException("Share access url invalid");
		}
		this.shareAccessUrl = url;
		credentials = new SharedKeyCredentialsWrapper(credentials,
				shareAccessUrl, this);

	}

	/**
	 * @return the shareAccessUrl
	 */
	public ISharedAccessUrl getShareAccessUrl() {
		return shareAccessUrl;
	}


	public String leaseBlob(IBlobProperties blobProperties,
			final LeaseMode mode, final NameValueCollection headerParameters)
			throws StorageException {
		String container = getName();
		if (container.equals(ROOT_CONTAINER)) {
			container = "";
		}
		String blobName = blobProperties.getName();
		// append ?comp=lease in url
		NameValueCollection queryParams = new NameValueCollection();
		queryParams.put(QueryParams.QueryParamComp, CompConstants.Lease);

		final ResourceUriComponents uriComponents = new ResourceUriComponents(
				getAccountName(), container, blobName);
		final URI blobUri = HttpUtilities.createRequestUri(getBaseUri(),
				isUsePathStyleUris(), getAccountName(), container, blobName,
				null, queryParams, uriComponents);

		String id = (String) getRetryPolicy().execute(new Callable<String>() {
			public String call() throws Exception {
				HttpRequest request = HttpUtilities
						.createHttpRequestWithCommonHeaders(blobUri,
								HttpMethod.Put, null);

				// add header
				request.addHeader(HeaderNames.ApiVersion,
						XmsVersion.VERSION_2009_09_19);
				request.addHeader(HeaderNames.LeaseAction, mode.getLiteral());

				appendHeaders(request, headerParameters);
				credentials.signRequest(request, uriComponents);
				try {
					HttpWebResponse response = HttpUtilities
							.getResponse(request);
					int status = response.getStatusCode();

					/**
					 * The success status codes returned for lease operations
					 * are the following: Acquire: A successful operation
					 * returns status code 201 (Created). Renew: A successful
					 * operation returns status code 200 (OK). Release: A
					 * successful operation returns status code 200 (OK). Break:
					 * A successful operation returns status code 202
					 * (Accepted).
					 */
					if (status == HttpStatus.SC_CREATED
							|| status == HttpStatus.SC_OK
							|| status == HttpStatus.SC_ACCEPTED) {
						String result = response.getHeader(HeaderNames.LeaseId);
						response.close();
						return result;
					} else {
						response.close();
					}
				} catch (Exception ioe) {
					HttpUtilities.translateWebException(ioe);
				}
				return null;
			}
		});

		return id;
	}


	public IPageBlob createPageBlob(IBlobProperties blobProperties, final int size,
			final NameValueCollection headerParameters) throws StorageException {
		String blobName = blobProperties.getName();
		if (blobName == null || blobName.equals("")) {
			throw new IllegalArgumentException("Blob name is empty.");
		}

		String container = getName();
		if (container.equals(ROOT_CONTAINER)) {
			container = "";
		}

		final ResourceUriComponents uriComponents = new ResourceUriComponents(
				getAccountName(), container, blobName);
		final URI blobUri = HttpUtilities.createRequestUri(getBaseUri(),
				isUsePathStyleUris(), getAccountName(), container, blobName,
				null, new NameValueCollection(), uriComponents);
		getRetryPolicy().execute(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				HttpRequest request = HttpUtilities
						.createHttpRequestWithCommonHeaders(blobUri,
								HttpMethod.Put, null);

				// add header
				request.addHeader(HeaderNames.ApiVersion,
						XmsVersion.VERSION_2009_09_19);
				request.addHeader(HeaderNames.BlobType, BlobType.PageBlob
						.getLiteral());
				// Required for page blobs. This header specifies the maximum
				// size for
				// the page blob, up to 1 TB. The page blob size must be aligned
				// to a
				// 512-byte boundary.
				request.addHeader(HeaderNames.BlobContentLength, String
						.valueOf(size));

				appendHeaders(request, headerParameters);
				credentials.signRequest(request, uriComponents);
				try {
					HttpWebResponse response = HttpUtilities
							.getResponse(request);
					int status = response.getStatusCode();

					if (status == HttpStatus.SC_CREATED) {
						response.close();
						return Boolean.TRUE;
					}
					response.close();
				} catch (Exception ioe) {
					HttpUtilities.translateWebException(ioe);
				}
				return Boolean.FALSE;
			}
		});
		return getPageBlobReference(blobName) ;
	}


	public void setMetadata(final NameValueCollection metadata) {
		try {
			getRetryPolicy().execute(new Callable<Object>() {
				public Object call() throws Exception {
					ResourceUriComponents uriComponents = new ResourceUriComponents(
							getAccountName(), getName(), null);

					NameValueCollection queryParams = new NameValueCollection();
					queryParams.put(QueryParams.QueryParamComp,
							CompConstants.Metadata);
					queryParams.put(QueryParams.QueryRestType,
							CompConstants.Container);

					URI uri = HttpUtilities.createRequestUri(getBaseUri(),
							isUsePathStyleUris(), getAccountName(),
							getName(), null, getTimeout(),
							queryParams, uriComponents);
					HttpRequest request = HttpUtilities
							.createHttpRequestWithCommonHeaders(uri,
									HttpMethod.Put, getTimeout());
					if (metadata != null) {
						HttpUtilities.addMetadataHeaders(request, metadata);
					}
					request.addHeader(HeaderNames.ApiVersion,
							XmsVersion.VERSION_2009_07_17);
					credentials.signRequest(request, uriComponents);
					try {
						HttpWebResponse response = HttpUtilities
								.getResponse(request);
						if (response.getStatusCode() == HttpStatus.SC_OK) {
							response.close();
							return true;
						} else {
							HttpUtilities.processUnexpectedStatusCode(response);
							return false; // Can't return
						}
					} catch (StorageException we) {
						throw HttpUtilities.translateWebException(we);
					}
				}
			});
		} catch (StorageException e) {
			throw e;
		}
	}

	public IBlockBlob updateBlockBlob(IBlobProperties blobProperties, IBlobContents blobContents)
			throws StorageException {
		IBlockBlob blob = null;
		boolean blobExist = isBlobExist(blobProperties.getName());
		if (!blobExist) {
			throw new StorageException("The blob does not exist!");
		}
		boolean updateBlob = createOrUpdateBlockBlob(blobProperties, blobContents,
				true);
		if (updateBlob) {
			blob = getBlockBlobReference(blobProperties.getName());
		}
		return blob;
	}

	public IBlockBlob createBlockBlob(IBlobProperties blobProperties,
			IBlobContents blobContents)
			throws StorageException {
		IBlockBlob blob = null;
		boolean blobExist = isBlobExist(blobProperties.getName());
		if (blobExist) {
			throw new StorageException("The blob already exists!");
		}
		boolean createBlob = createOrUpdateBlockBlob(blobProperties, blobContents,
				false);
		if (createBlob) {
			blob = getBlockBlobReference(blobProperties.getName());
		}
		return blob;
	}

	/**
	 * Append headers to the request.
	 *
	 * @param request
	 * @param headerParameters
	 */
	void appendHeaders(HttpRequest request,
			NameValueCollection headerParameters) {
		if (headerParameters == null || headerParameters.size() == 0)
			return;
		for (Object key : headerParameters.keySet()) {
			String value = headerParameters
					.getMultipleValuesAsString((String) key);
			if (value != null)
				request.addHeader(key.toString(), value);
		}
	}

	SharedKeyCredentials getCredentials() {
		return credentials;
	}


//private boolean setBlobMetadataImpl(final IBlobProperties blobProperties,
//	final String eTag) {
//if (blobProperties == null) {
//	throw new IllegalArgumentException(
//			"Blob properties cannot be null or empty!");
//}
//
//final OutParameter<Boolean> retval = new OutParameter<Boolean>(false);
//getRetryPolicy().execute(new Callable<Boolean>() {
//
//	public Boolean call() throws Exception {
//		NameValueCollection queryParams = new NameValueCollection();
//		queryParams.put(QueryParams.QueryParamComp,
//				CompConstants.Metadata);
//		final ResourceUriComponents uriComponents = new ResourceUriComponents(
//				getAccountName(), getName(), blobProperties
//						.getName());
//		URI uri = HttpUtilities.createRequestUri(getBaseUri(),
//				isUsePathStyleUris(), getAccountName(),
//				getName(), blobProperties.getName(),
//				getTimeout(), queryParams, uriComponents);
//		HttpRequest request = HttpUtilities
//				.createHttpRequestWithCommonHeaders(uri,
//						HttpMethod.Put, getTimeout());
//		if (blobProperties.getMetadata() != null) {
//			HttpUtilities.addMetadataHeaders(request, blobProperties
//					.getMetadata());
//		}
//		if (!Utilities.isNullOrEmpty(eTag)) {
//			request.addHeader(HeaderNames.IfMatch, eTag);
//		}
//		credentials.signRequest(request, uriComponents);
//		HttpWebResponse response = HttpUtilities.getResponse(request);
//		int statusCode = response.getStatusCode();
//		if (statusCode == HttpStatus.SC_OK) {
//			retval.setValue(true);
//			blobProperties.setLastModifiedTime(response
//					.getLastModified());
//			blobProperties
//					.setETag(response.getHeader(HeaderNames.ETag));
//		} else if (statusCode == HttpStatus.SC_PRECONDITION_FAILED
//				|| statusCode == HttpStatus.SC_NOT_MODIFIED) {
//			retval.setValue(false);
//		} else {
//			retval.setValue(false);
//			HttpUtilities.processUnexpectedStatusCode(response);
//		}
//		return null;
//	}
//});
//return retval.getValue();
//}

}

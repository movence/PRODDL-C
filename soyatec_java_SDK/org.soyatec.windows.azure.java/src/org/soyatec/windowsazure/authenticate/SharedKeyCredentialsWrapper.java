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
package org.soyatec.windowsazure.authenticate;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.soyatec.windowsazure.blob.IBlobContainer;
import org.soyatec.windowsazure.blob.ISharedAccessUrl;
import org.soyatec.windowsazure.internal.ResourceUriComponents;
import org.soyatec.windowsazure.internal.constants.HeaderNames;
import org.soyatec.windowsazure.internal.constants.XmsVersion;
import org.soyatec.windowsazure.internal.util.HttpUtilities;
import org.soyatec.windowsazure.internal.util.Logger;
import org.soyatec.windowsazure.internal.util.Utilities;

/**
 * The wrapper of the shared key credentials.
 */
public class SharedKeyCredentialsWrapper extends SharedKeyCredentials {

	private final SharedKeyCredentials credentials;
	private final ISharedAccessUrl shareAccessUrl;
	private final IBlobContainer container;

	/**
	 * Create a SharedKeyCredentialsWrapper object given credentials,
	 * sharedAccessUrl and a blobContainer.
	 * 
	 * @param credentials
	 * @param url
	 * @param container
	 */
	public SharedKeyCredentialsWrapper(SharedKeyCredentials credentials,
			ISharedAccessUrl url, IBlobContainer container) {
		this.credentials = credentials;
		this.shareAccessUrl = url;
		this.container = container;
	}

	/**
	 * @param request
	 * @param uriComponents
	 * @see org.soyatec.windowsazure.authenticate.SharedKeyCredentials#signRequest(org.apache.http.HttpRequest,
	 *      org.soyatec.windowsazure.internal.ResourceUriComponents)
	 */
	public void signRequest(HttpRequest request,
			ResourceUriComponents uriComponents) {
		if (request instanceof HttpRequestBase) {
			HttpRequestBase hrb = ((HttpRequestBase) request);
			URI uri = hrb.getURI();
			// replace the container name
			// replace the blob name
			// replace the account name
			uri = replaceAccountName(uri, shareAccessUrl.getAccountName());
			uri = replaceContainerName(uri, shareAccessUrl.getContainerName());
			uri = appendSignString(uri, shareAccessUrl.getSignedString());
			((HttpRequestBase) request).setURI(uri);
		}

		//addVerisonHeader(request);
	}

	/**
	 * Append the signedString to the uri.
	 * 
	 * @param uri
	 * @param signedString
	 * @return The uri after be appended with signedString.
	 */
	private URI appendSignString(URI uri, String signedString) {
		try {
			return URIUtils.createURI(uri.getScheme(), uri.getHost(), uri
					.getPort(), HttpUtilities.getNormalizePath(uri), (uri
					.getQuery() == null ? Utilities.emptyString() : uri
					.getQuery())
					+ "&" + signedString, uri.getFragment());
		} catch (URISyntaxException e) {
			Logger.error("", e);
		}
		return uri;
	}

	/**
	 * Add the version header to the request.
	 * 
	 * @param request
	 */
	private void addVerisonHeader(HttpRequest request) {
		request
				.addHeader(HeaderNames.ApiVersion,
						XmsVersion.VERSION_2009_07_17);
	}

	/**
	 * Replace the uri container name.
	 * 
	 * @param uri
	 * @param accountName
	 * @return The uri after be replaced the account name with the input
	 *         accountName.
	 */
	private URI replaceAccountName(URI uri, String accountName) {
		try {
			String host = uri.getHost();
			String[] temp = host.split("\\.");
			temp[0] = accountName;
			return URIUtils.createURI(uri.getScheme(), join(".", temp), uri
					.getPort(), HttpUtilities.getNormalizePath(uri), (uri
					.getQuery() == null ? Utilities.emptyString() : uri
					.getQuery()), uri.getFragment());
		} catch (URISyntaxException e) {
			Logger.error("", e);
		}
		return uri;
	}

	/**
	 * Replace the uri container name.
	 * 
	 * @param uri
	 * @param containerName
	 * @return The uri after be replaced the container name with the input
	 *         containerName.
	 */
	private URI replaceContainerName(URI uri, String containerName) {
		if (containerName == null) {
			return uri;
		}
		try {
			String host = uri.getPath();
			String[] temp = host.split("/");
			temp[0] = containerName;
			return URIUtils.createURI(uri.getScheme(), uri.getHost(), uri
					.getPort(), join("/", temp),
					(uri.getQuery() == null ? Utilities.emptyString() : uri
							.getQuery()), uri.getFragment());
		} catch (URISyntaxException e) {
			Logger.error("", e);
		}
		return uri;
	}

	/**
	 * Inserted between each source with string.
	 * @param se
	 * 			The string insert between each source.
	 * @param sources
	 * 			The sources to join.
	 * @return The string of sources joined with string.
	 */
	public static final String join(String se, String[] sources) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0, n = sources.length; i < n; i++) {
			sb.append(sources[i]);
			if (i < n - 1) {
				sb.append(se);
			}
		}
		return sb.toString();
	}

	/**
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return credentials.equals(obj);
	}

	/**
	 * @return
	 * @see org.soyatec.windowsazure.authenticate.SharedKeyCredentials#getAccountName()
	 */
	public String getAccountName() {
		return credentials.getAccountName();
	}

	/**
	 * @return
	 * @see org.soyatec.windowsazure.authenticate.SharedKeyCredentials#getKey()
	 */
	public byte[] getKey() {
		return credentials.getKey();
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return credentials.hashCode();
	}

	/**
	 * @param request
	 * @param uriComponents
	 * @see org.soyatec.windowsazure.authenticate.SharedKeyCredentials#signRequestForSharedKeyLite(org.apache.http.HttpRequest,
	 *      org.soyatec.windowsazure.internal.ResourceUriComponents)
	 */
	public void signRequestForSharedKeyLite(HttpRequest request,
			ResourceUriComponents uriComponents) {
		credentials.signRequestForSharedKeyLite(request, uriComponents);
	}

	/**
	 * @param permissions
	 * @param start
	 * @param expiry
	 * @param canonicalizedResource
	 * @param identifier
	 * @return
	 * @see org.soyatec.windowsazure.authenticate.SharedKeyCredentials#signSharedAccessUrl(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public String signSharedAccessUrl(String permissions, String start,
			String expiry, String canonicalizedResource, String identifier) {
		return credentials.signSharedAccessUrl(permissions, start, expiry,
				canonicalizedResource, identifier);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return credentials.toString();
	}

	/**
	 * @return the shareAccessUrl
	 */
	public ISharedAccessUrl getShareAccessUrl() {
		return shareAccessUrl;
	}

	/**
	 * @return the credentials
	 */
	public SharedKeyCredentials getCredentials() {
		return credentials;
	}

}

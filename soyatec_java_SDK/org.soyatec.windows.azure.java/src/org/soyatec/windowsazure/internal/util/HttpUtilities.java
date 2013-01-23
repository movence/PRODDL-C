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
package org.soyatec.windowsazure.internal.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.dom4j.Document;
import org.dom4j.Element;
import org.soyatec.windowsazure.authenticate.HttpRequestAccessor;
import org.soyatec.windowsazure.blob.io.BlobMemoryStream;
import org.soyatec.windowsazure.blob.io.BlobStream;
import org.soyatec.windowsazure.constants.ConstChars;
import org.soyatec.windowsazure.constants.XmlElementNames;
import org.soyatec.windowsazure.error.StorageClientException;
import org.soyatec.windowsazure.error.StorageErrorCode;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.error.StorageExtendedErrorInformation;
import org.soyatec.windowsazure.error.StorageServerException;
import org.soyatec.windowsazure.error.WebException;
import org.soyatec.windowsazure.internal.HttpMerge;
import org.soyatec.windowsazure.internal.ResourceUriComponents;
import org.soyatec.windowsazure.internal.StorageErrorCodeTranslator;
import org.soyatec.windowsazure.internal.constants.HeaderNames;
import org.soyatec.windowsazure.internal.constants.HttpMethod;
import org.soyatec.windowsazure.internal.constants.HttpWebResponse;
import org.soyatec.windowsazure.internal.constants.QueryParams;
import org.soyatec.windowsazure.internal.constants.XmsVersion;
import org.soyatec.windowsazure.internal.util.xml.XmlUtil;
import org.soyatec.windowsazure.proxy.ProxyConfiguration;

/**
 * Tools for create http request and send request.
 * 
 */
public class HttpUtilities {
	static ProxyConfiguration proxy;
	// static final DefaultHttpRequestRetryHandler retryhandler = new
	// DefaultHttpRequestRetryHandler();
	// retryhandler.setRequestSentRetryEnabled(false);
	// retryhandler.setRetryCount(3);

	
	private static HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {
		static final int MAX_RETRY = 3;

		public boolean retryRequest(IOException exception, int executionCount,
				HttpContext context) {
			if (executionCount >= MAX_RETRY) {
				// Do not retry if over max retry count
				return false;
			}
			if (exception instanceof NoHttpResponseException) {
				// Retry if the server dropped connection on us
				return true;
			}
			if (exception instanceof SSLHandshakeException) {
				// Do not retry on SSL handshake exception
				return false;
			}
			HttpRequest request = (HttpRequest) context
					.getAttribute(ExecutionContext.HTTP_REQUEST);
			boolean idempotent = (request instanceof HttpEntityEnclosingRequest);
			if (!idempotent) {
				// Retry if the request is considered idempotent
				return true;
			}
			return false;
		}
	};

	public static void setProxy(ProxyConfiguration proxy) {
		HttpUtilities.proxy = proxy;
	}

	private static void addProxyConfig(DefaultHttpClient httpClient) {
		Properties prop = System.getProperties();
		String proxyHost = prop.getProperty("http.proxyHost");
		Integer proxyPort = null;
		if(prop.getProperty("http.proxyPort")!=null){
			proxyPort = Integer.parseInt(prop.getProperty("http.proxyPort"));
		}
		String proxyUserName = prop.getProperty("http.proxyUser");
		String proxyPassword = prop.getProperty("http.proxyPassword");
		String proxyDomain = prop.getProperty("http.proxyDomain");
		if (proxy != null) {
			proxyHost = proxy.getProxyHost();
			proxyPort = proxy.getProxyPort();
			proxyUserName = proxy.getProxyUsername();
			proxyPassword = proxy.getProxyPassword();
			proxyDomain = proxy.getProxyDomainname();
		}

		if (proxyHost != null && proxyPort > 0) {
			Logger.debug(String.format(
					"Using the proxy, proxyHost %s and proxyPort %d",
					proxyHost, proxyPort));

			HttpHost proxyServer = new HttpHost(proxyHost, proxyPort);
			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					proxyServer);

			if (proxyUserName != null && proxyUserName.trim().length() > 0
					&& proxyPassword != null && proxyPassword.length() > 0) {
				Logger.debug("Proxy Username:" + proxyUserName);
				Logger.debug("Proxy Domain:" + proxyDomain);

				AuthScope authScope = new AuthScope(proxyHost, proxyPort, null);
				NTCredentials credentials = new NTCredentials(proxyUserName,
						proxyPassword, "", proxyDomain);

				httpClient.getCredentialsProvider().setCredentials(authScope,
						credentials);

				// Set NTLM authentication
				List<String> authPrefs = new ArrayList<String>();
				authPrefs.add(AuthPolicy.NTLM);
				httpClient.getParams().setParameter(AuthPNames.PROXY_AUTH_PREF,
						authPrefs);
			}
		}
	}
//	private static void addProxyConfig(DefaultHttpClient httpClient) {
//		if (!proxyExists())
//			return;
//
//		Properties prop = System.getProperties();
//		String proxyHost = prop.getProperty("http.proxyHost");
//		int proxyPort = Integer.parseInt(prop.getProperty("http.proxyPort"));
//		String username = prop.getProperty("http.proxyUser");
//		String password = prop.getProperty("http.proxyPassword");
//		
//		ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
//				httpClient.getConnectionManager().getSchemeRegistry(),
//				ProxySelector.getDefault());
//		httpClient.setRoutePlanner(routePlanner);
//		
//		httpClient.getCredentialsProvider().setCredentials(
//				new AuthScope(proxyHost, proxyPort),
//				new UsernamePasswordCredentials(username, password));
//		
//		HttpHost proxy = new HttpHost(proxyHost, proxyPort);
//		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
//				proxy);
//
//	}

//	public static void setProxy(String host, int port) {
//		setProxy(host, port, null, null);
//	}
//
//	public static void setProxy(String host, int port, String username,
//			String password) {
//		Properties prop = System.getProperties();
//
//		prop.setProperty("proxySet", "true");
//		System.setProperty("http.proxyUser", username);
//		System.setProperty("http.proxyPassword", password);
//		prop.setProperty("http.proxyHost", host);
//		prop.setProperty("http.proxyPort", String.valueOf(port));
//		prop.setProperty("https.proxyHost", host);
//		prop.setProperty("https.proxyPort", String.valueOf(port));
//
//	}

	public static boolean proxyExists() {
		Properties prop = System.getProperties();
		if (prop.get("http.proxyHost") != null && prop
				.get("http.proxyPort") != null)
			return true;
		
		if(proxy != null   ){
			String proxyHost = proxy.getProxyHost();
			int proxyPort = proxy.getProxyPort();

			if (proxyHost != null && proxyPort > 0) 
				return true;
		}
		return false;
	}

	public static void removeProxyConfig() {
		Properties prop = System.getProperties();
		prop.remove("proxySet");
		prop.remove("http.proxyHost");
		prop.remove("http.proxyPort");
		prop.remove("http.nonProxyHosts");
		prop.remove("https.proxyHost");
		prop.remove("https.proxyPort");
		prop.remove("https.nonProxyHosts");
		prop.remove("http.proxyUser");
		prop.remove("http.proxyPassword");
		
		proxy = null;
	}

	/**
	 * Create the http request with common headers.
	 * 
	 * @param uri
	 * @param method
	 * @param timeout
	 * @return HttpRequest
	 */
	public static HttpRequest createHttpRequestWithCommonHeaders(URI uri,
			String method, TimeSpan timeout) {
		HttpUriRequest request = createHttpRequest(uri, method);
		// Some header setting
		// request.Timeout = (int)timeout.TotalMilliseconds;
		if (timeout != null) {
			request.addHeader(HeaderNames.Sotimeout,
					Long.toString(timeout.toMilliseconds()));
		}
		// request.ReadWriteTimeout = (int)timeout.TotalMilliseconds;
		// request.ContentLength = 0;
		if (request.getHeaders(HeaderNames.ContentLength) == null
				|| request.getHeaders(HeaderNames.ContentLength).length <= 0) {
			if (!request.getMethod().equals(HttpMethod.Put)
					&& !request.getMethod().equals(HttpMethod.Post)) {
				// not set content length header for put method
				request.addHeader(HeaderNames.ContentLength, "0");
			}

		}
		// set timeout
		request.addHeader(HeaderNames.StorageDateTime, Utilities.getUTCTime());
		return request;
	}

	/**
	 * Create the service httpReauest.
	 * 
	 * @param uri
	 * @param method
	 * @return HttpRequest
	 */
	public static HttpUriRequest createServiceHttpRequest(URI uri, String method) {
		HttpUriRequest request = createHttpRequest(uri, method);
		request.addHeader(HeaderNames.ApiVersion, XmsVersion.VERSION_2009_10_01);
		return request;
	}

	/**
	 * Add the metadata headers to the HttpRequest.
	 * 
	 * @param request
	 * @param metadata
	 */
	public static void addMetadataHeaders(HttpRequest request,
			NameValueCollection metadata) {
		for (Object keyObj : metadata.keySet()) {
			String key = (String) keyObj;
			String headerName = HeaderNames.PrefixForMetadata + key;
			request.addHeader(headerName.toLowerCase(),
					metadata.getMultipleValuesAsString(key));
		}
	}

	/**
	 * Create a httpRequest with the uri and method.
	 * 
	 * @param uri
	 * @param method
	 * @return HttpUriRequest
	 */
	public static HttpUriRequest createHttpRequest(URI uri, String method) {
		HttpUriRequest request;
		if (method.equals(HttpMethod.Get)) {
			request = new HttpGet(uri);
		} else if (method.equals(HttpMethod.Post)) {
			request = new HttpPost(uri);
		} else if (method.equals(HttpMethod.Delete)) {
			request = new HttpDelete(uri);
		} else if (method.equals(HttpMethod.Head)) {
			request = new HttpHead(uri);
		} else if (method.equals(HttpMethod.Options)) {
			request = new HttpOptions(uri);
		} else if (method.equals(HttpMethod.Put)) {
			request = new HttpPut(uri);
		} else if (method.equals(HttpMethod.Trace)) {
			request = new HttpTrace(uri);
		} else if (method.equals(HttpMerge.METHOD_NAME)) {
			request = new HttpMerge(uri);
		} else {
			throw new IllegalArgumentException(MessageFormat.format(
					"{0} is not a valid HTTP method.", method));
		}
		return request;
	}

	/**
	 * Get the HttpsClient of a SSLSocketFactory.
	 * 
	 * @param factory
	 * @return HttpsClient
	 * @throws Exception
	 */
	public static HttpClient createHttpsClient(SSLSocketFactory factory)
			throws Exception {
		HttpParams params = new BasicHttpParams();

		// params.setParameter(CoreProtocolPNames.USER_AGENT,
		// "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
		// params.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE,
		// Boolean.FALSE);
		// params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET,
		// "UTF-8");

		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		// HttpProtocolParams.setContentCharset(params, "UTF-8");
		final SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		registry.register(new Scheme("https", factory, 443));
		final ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(
				params, registry);

		DefaultHttpClient client = new DefaultHttpClient(manager, params);

		addProxyConfig(client);
		// client.setHttpRequestRetryHandler(requestRetryHandler);
		return client;
	}

	private static HttpClient createHttpClient() {
		HttpParams params = new BasicHttpParams();
		ConnManagerParams.setMaxTotalConnections(params, 100);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory
				.getSocketFactory(), 443));

		// Create an HttpClient with the ThreadSafeClientConnManager.
		// This connection manager must be used if more than one thread will
		// be using the HttpClient.
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
				schemeRegistry);
		DefaultHttpClient httpClient = new DefaultHttpClient(cm, params);
		addProxyConfig(httpClient);
		return httpClient;
	}

	/**
	 * Get the HttpWebResponse with a HttpRequest and SSLSocketFactory.
	 * 
	 * @param request
	 * @param factory
	 * @return the HttpWebResponse with a HttpRequest and SSLSocketFactory.
	 * @throws Exception
	 */
	public static HttpWebResponse getSSLReponse(HttpUriRequest request,
			SSLSocketFactory factory) throws Exception {
		HttpClient client = HttpUtilities.createHttpsClient(factory);
		try {
			return new HttpWebResponse(client.execute(request));
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			// client.getConnectionManager().shutdown();
		}
	}

	/**
	 * Get the HttpWebResponse with a HttpRequest.
	 * 
	 * @param request
	 * @return HttpWebResponse
	 * @throws Exception
	 */
	public static HttpWebResponse getResponse(HttpRequest request)
			throws Exception {
		HttpClient httpClient = createHttpClient();

		HttpParams params = httpClient.getParams();
		try {
			Long parseLong = Long.parseLong(request.getLastHeader(
					HeaderNames.Sotimeout).getValue());
			request.removeHeader(request.getLastHeader(HeaderNames.Sotimeout));

			// so timeout
			HttpConnectionParams.setConnectionTimeout(params,
					parseLong.intValue());

			// connection timeout
			HttpConnectionParams.setSoTimeout(params, parseLong.intValue());
		} catch (Exception e) {
			// Use default timeout setting...
		}
		try {
			if (request instanceof HttpUriRequest) {
				return new HttpWebResponse(
						httpClient.execute((HttpUriRequest) request));
			} else {
				throw new IllegalArgumentException("Request is invalid");
			}
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			// httpClient.getConnectionManager().shutdown();
		}
	}

	/**
	 * Unexpected response code. Just throw a exception
	 * 
	 * @param response
	 * @throws StorageServerException
	 *             Warp all exception for bad response
	 */
	public static void processUnexpectedStatusCode(HttpWebResponse response)
			throws StorageServerException {
		StorageExtendedErrorInformation detail = null;
		if (response.getStream() != null) {
			detail = new StorageExtendedErrorInformation();
			detail.setErrorBody(convertStreamToString(response.getStream()));
		}

		// Append the error response to exception detail message
		String exceptionMessage = response.getStatusDescription();
		if (detail != null) {
			exceptionMessage = exceptionMessage + "\r\n"
					+ detail.getErrorBody();
		}
		response.close();
		throw new StorageServerException(StorageErrorCode.ServiceBadResponse,
				exceptionMessage, response.getStatusCode(), detail, null);
	}

	/**
	 * Convert the inputStream to a string.
	 * 
	 * @param is
	 *            InputStream
	 * @return string
	 */
	public static String convertStreamToString(InputStream is) {
		if (is == null) {
			return null;
		}
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = null;
		StringBuilder sb = new StringBuilder();
		try {
			reader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			// e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
		// FIX "xx?xml version="1.0" encoding="utf-8"?>"
		String xmlContent = sb.toString();
		int pos = xmlContent.indexOf("?xml");
		if (pos > -1)
			return "<" + xmlContent.substring(pos);
		else
			return xmlContent;
	}

	/**
	 * Parse query string to NameValueCollection object
	 * 
	 * @param query
	 * @return NameValueCollection
	 */
	public static NameValueCollection parseQueryString(String query) {
		NameValueCollection map = new NameValueCollection();
		if (query == null) {
			return map;
		}
		String[] params = query.split(ConstChars.Ampersand);
		for (String param : params) {
			if (!param.contains(ConstChars.Equal)) {
				throw new IllegalArgumentException(MessageFormat.format(
						"Query string \"{0}\" is invalid.", param));
			}

			String[] nameValue = param.split(ConstChars.Equal);
			map.put(nameValue[0], nameValue[1]);
		}
		return map;
	}

	/**
	 * Parse the httpRequest to NameValueCollection object
	 * 
	 * @param request
	 * @return NameValueCollection
	 */
	public static NameValueCollection parseHttpHeaders(HttpRequest request) {
		NameValueCollection map = new NameValueCollection();
		for (Header header : request.getAllHeaders()) {
			map.put(header.getName(), header.getValue());
		}
		return map;
	}

	/**
	 * Parse the httpRequest to string.
	 * 
	 * @param request
	 * @return string after parse
	 */
	public static String parseRequestContentType(HttpRequest request) {
		Header[] headers = request.getHeaders(HeaderNames.ContentType);
		if (headers != null) {
			for (Header header : headers) {
				if (header != null
						&& !Utilities.isNullOrEmpty(header.getValue())) {
					return header.getValue();
				}
			}
		}
		return Utilities.emptyString();
	}

	/**
	 * Translate Exception to StorageException.
	 * 
	 * @param e
	 * @return StorageException
	 */
	public static StorageException translateWebException(Exception e) {
		if (e instanceof StorageException) {
			return (StorageException) e;
		}
		if ((e instanceof WebException)) {
			WebException we = (WebException) e;
			HttpWebResponse response = we.getResponse();
			if (response != null) {
				StorageExtendedErrorInformation extendedError = getExtendedErrorDetailsFromResponse(
						response.getStream(), response.getContentLength());
				StorageException translatedException = null;
				if (extendedError != null) {
					translatedException = translateExtendedError(extendedError,
							response.getStatusCode(),
							response.getStatusDescription(), e);
					if (translatedException != null) {
						return translatedException;
					}
				}
				translatedException = translateFromHttpStatus(
						response.getStatusCode(),
						response.getStatusDescription(), extendedError, we);
				if (translatedException != null) {
					return translatedException;
				}
			}
			switch (we.getStatus()) {
			case RequestCanceled:
				return new StorageServerException(
						StorageErrorCode.ServiceTimeout,
						"The server request did not complete within the specified timeout",
						HttpStatus.SC_GATEWAY_TIMEOUT, we);
			case ConnectFailure:
				return new StorageServerException(
						StorageErrorCode.TransportError,
						"Connect to server failed",
						HttpStatus.SC_FAILED_DEPENDENCY, we);

			default:
				return new StorageServerException(
						StorageErrorCode.ServiceInternalError,
						"The server encountered an unknown failure: "
								+ e.getMessage(),
						HttpStatus.SC_INTERNAL_SERVER_ERROR, we);
			}
		}

		return new StorageException(e);
	}

	private static StorageException translateFromHttpStatus(int statusCode,
			String statusDescription, StorageExtendedErrorInformation details,
			Exception inner) {
		switch (statusCode) {
		case HttpStatus.SC_FORBIDDEN:
			return new StorageClientException(StorageErrorCode.AccessDenied,
					statusDescription, HttpStatus.SC_FORBIDDEN, details, inner);

		case HttpStatus.SC_GONE:
		case HttpStatus.SC_NOT_FOUND:
			return new StorageClientException(
					StorageErrorCode.ResourceNotFound, statusDescription,
					statusCode, details, inner);

		case HttpStatus.SC_BAD_REQUEST:
			return new StorageClientException(StorageErrorCode.BadRequest,
					statusDescription, statusCode, details, inner);

		case HttpStatus.SC_PRECONDITION_FAILED:
		case HttpStatus.SC_NOT_MODIFIED:
			return new StorageClientException(StorageErrorCode.BadRequest,
					statusDescription, statusCode, details, inner);

		case HttpStatus.SC_CONFLICT:
			return new StorageClientException(
					StorageErrorCode.ResourceAlreadyExists, statusDescription,
					statusCode, details, inner);

		case HttpStatus.SC_GATEWAY_TIMEOUT:
			return new StorageServerException(StorageErrorCode.ServiceTimeout,
					statusDescription, statusCode, details, inner);

		case HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE:
			return new StorageClientException(StorageErrorCode.BadRequest,
					statusDescription, statusCode, details, inner);

		case HttpStatus.SC_INTERNAL_SERVER_ERROR:
			return new StorageServerException(
					StorageErrorCode.ServiceInternalError, statusDescription,
					statusCode, details, inner);

		case HttpStatus.SC_BAD_GATEWAY:
			return new StorageServerException(StorageErrorCode.BadGateway,
					statusDescription, statusCode, details, inner);
		}
		return null;
	}

	private static StorageException translateExtendedError(
			StorageExtendedErrorInformation details, int statusCode,
			String statusDescription, Exception e) {

		StorageErrorCode errorCode = StorageErrorCodeTranslator
				.translateStorageErrorCodeString(details.getErrorCode());

		if (errorCode != StorageErrorCode.None) {
			return new StorageClientException(errorCode, statusDescription,
					statusCode, details, e);
		}
		errorCode = StorageErrorCodeTranslator
				.translateStorageErrorCodeString(details.getErrorCode());
		if (errorCode != StorageErrorCode.None) {
			return new StorageServerException(errorCode, statusDescription,
					statusCode, details, e);
		}

		return null;
	}

	// This is the limit where we allow for the error message returned by the
	// server.
	// Message longer than that will be truncated.
	private final static int ErrorTextSizeLimit = 8 * 1024;

	private static StorageExtendedErrorInformation getExtendedErrorDetailsFromResponse(
			InputStream stream, long contentLength) {

		int bytesToRead = (int) Math.max(contentLength, ErrorTextSizeLimit);
		byte[] responseBuffer = new byte[bytesToRead];
		int bytesRead = copyStreamToBuffer(stream, responseBuffer, bytesToRead);

		return getErrorDetailsFromStream(new BlobMemoryStream(responseBuffer,
				0, bytesRead));
	}

	private static StorageExtendedErrorInformation getErrorDetailsFromStream(
			BlobStream stream) {
		StorageExtendedErrorInformation extendedError = new StorageExtendedErrorInformation();
		try {
			Document doc = XmlUtil
					.parseXmlString(new String(stream.getBytes()));
			Element root = doc.getRootElement();
			extendedError.setErrorCode(root
					.elementText(XmlElementNames.ErrorCode));
			extendedError.setErrorMessage(root
					.elementText(XmlElementNames.ErrorMessage));
			NameValueCollection details = new NameValueCollection();
			extendedError.setAdditionalDetails(details);

			for (Object o : root.elements()) {
				Element e = (Element) o;
				if (e.getName().equals(XmlElementNames.ErrorException)) {
					details.put(XmlElementNames.ErrorExceptionMessage, e
							.elementText(XmlElementNames.ErrorExceptionMessage));
					details.put(
							XmlElementNames.ErrorExceptionStackTrace,
							e.elementText(XmlElementNames.ErrorExceptionStackTrace));
				} else {
					details.put(e.getName(), e.getText());
				}
			}
		} catch (Exception e) {
			return null;
		}
		return extendedError;
	}

	private static int copyStreamToBuffer(InputStream stream, byte[] buffer,
			int bytesToRead) {
		int n = 0;
		int amountLeft = bytesToRead;
		do {
			try {
				n = stream.read(buffer, bytesToRead - amountLeft, amountLeft);
			} catch (IOException e) {
				Logger.error("", e);
				break;
			}
			amountLeft -= n;
		} while (n > 0);
		return bytesToRead - amountLeft;
	}

	/**
	 * Create a request URI.
	 * 
	 * @param baseUri
	 * @param usePathStyleUris
	 * @param accountName
	 * @param containerName
	 * @param blobName
	 * @param timeout
	 * @param queryParameters
	 * @param uriComponents
	 * @return a URI
	 */
	public static URI createRequestUri(URI baseUri, boolean usePathStyleUris,
			String accountName, String containerName, String blobName,
			TimeSpan timeout, NameValueCollection queryParameters,
			ResourceUriComponents uriComponents) {
		return createRequestUri(baseUri, usePathStyleUris, accountName,
				containerName, blobName, timeout, queryParameters,
				uriComponents, null);
	}

	/**
	 * Create a request URI.
	 * 
	 * @param baseUri
	 * @param usePathStyleUris
	 * @param accountName
	 * @param containerName
	 * @param blobName
	 * @param timeout
	 * @param queryParameters
	 * @param uriComponents
	 * @param appendQuery
	 * @return URI
	 */
	public static URI createRequestUri(URI baseUri, boolean usePathStyleUris,
			String accountName, String containerName, String blobName,
			TimeSpan timeout, NameValueCollection queryParameters,
			ResourceUriComponents uriComponents, String appendQuery) {
		URI uri = HttpRequestAccessor.constructResourceUri(baseUri,
				uriComponents, usePathStyleUris);
		if (queryParameters != null) {
			if (queryParameters.get(QueryParams.QueryParamTimeout) == null
					&& timeout != null) {
				queryParameters.put(QueryParams.QueryParamTimeout,
						timeout.toSeconds());
			}
			StringBuilder sb = new StringBuilder();
			boolean firstParam = true;

			boolean appendBlockAtTail = false;
			for (Object key : queryParameters.keySet()) {
				String queryKey = (String) key;
				if (queryKey.equalsIgnoreCase(QueryParams.QueryParamBlockId)) {
					appendBlockAtTail = true;
					continue;
				}
				if (!firstParam) {
					sb.append("&");
				}
				sb.append(Utilities.encode(queryKey));
				sb.append('=');
				sb.append(Utilities.encode(queryParameters
						.getSingleValue(queryKey)));
				firstParam = false;
			}

			/*
			 * You shuold add blockid as the last query parameters for put block
			 * request, or an exception you will get.
			 */
			if (appendBlockAtTail) {
				String queryKey = QueryParams.QueryParamBlockId;
				sb.append("&");
				sb.append(Utilities.encode(queryKey));
				sb.append('=');
				sb.append(Utilities.encode(queryParameters
						.getSingleValue(queryKey)));
			}

			if (!Utilities.isNullOrEmpty(appendQuery)) {
				if (sb.length() > 0) {
					sb.append("&");
				}
				// @NOTE: escape char
				sb.append(appendQuery.replaceAll(" ", "%20"));
			}
			if (sb.length() > 0) {
				try {
					String p = getNormalizePath(uri).replaceAll(" ", "%20");
					return URIUtils.createURI(uri.getScheme(), uri.getHost(),
							uri.getPort(), p,
							(uri.getQuery() == null ? Utilities.emptyString()
									: uri.getQuery()) + sb.toString(),
							uri.getFragment());
				} catch (URISyntaxException e) {
					Logger.error("", e);
				}
			}
			return uri;
		} else {
			return uri;
		}
	}

	/**
	 * Get normalize path
	 * 
	 * @param uri
	 * @return String
	 */
	public static String getNormalizePath(URI uri) {
		if (Utilities.isNullOrEmpty(uri.getPath())) {
			return ConstChars.Slash;
		} else {
			if (!uri.getPath().startsWith(ConstChars.Slash)) {
				return ConstChars.Slash + uri.getPath();
			} else {
				return uri.getPath();
			}
		}
	}

	/**
	 * Remove the query part from URI.
	 * 
	 * @param uri
	 * @return URI after remove the query part.
	 */
	public static URI removeQueryPart(URI uri) {
		try {
			return URIUtils.createURI(uri.getScheme(), uri.getHost(),
					uri.getPort(), uri.getPath(), null, null);
		} catch (URISyntaxException e) {
			Logger.error("Remove query part from uri failed.", e);
			return uri;
		}
	}

}

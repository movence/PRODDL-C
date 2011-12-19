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
package org.soyatec.windowsazure.internal;

import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.soyatec.windowsazure.authenticate.HttpRequestAccessor;
import org.soyatec.windowsazure.constants.ConstChars;
import org.soyatec.windowsazure.internal.constants.HeaderNames;
import org.soyatec.windowsazure.internal.util.HttpUtilities;
import org.soyatec.windowsazure.internal.util.NameValueCollection;
import org.soyatec.windowsazure.internal.util.Utilities;

//http://msdn.microsoft.com/en-us/library/dd179428.aspx update for  2009-09-19 version
public class MessageCanonicalizer2 {
	/**
	 * Create a canonicalized string with the httpRequest and resourceUriComponents. 
	 * 
	 * @param request
	 * 			The HttpWebRequest object.
	 * @param uriComponents
	 * 			Components of the Uri extracted out of the request.
	 * @return A canonicalized string of the HTTP request.
	 */
	public static String canonicalizeHttpRequest(HttpRequest request,
			ResourceUriComponents uriComponents) {
		if (!(request instanceof HttpUriRequest)) {
			throw new IllegalArgumentException(
					"Request should be a URI http request");
		}
		HttpUriRequest rq = (HttpUriRequest) request;
		return canonicalizeHttpRequest(rq.getURI(), uriComponents, rq
				.getMethod(), HttpUtilities.parseRequestContentType(rq),
				Utilities.emptyString(), HttpUtilities.parseHttpHeaders(rq));
	}

	/**
	 * Compute the canonicalized httpRequest for sharedKeyLite
	 * @param request
	 * 			The HttpWebRequest object.
	 * @param uriComponents
	 * 			Components of the Uri extracted out of the request.
	 * @param date
	 * 			The date of the HTTP request.
	 * @return canonicalized httpRequest for sharedKeyLite
	 */
	public static String canonicalizeHttpRequestForSharedKeyLite(
			HttpRequest request, ResourceUriComponents uriComponents,
			String date) {
		if (!(request instanceof HttpUriRequest)) {
			throw new IllegalArgumentException(
					"Request should be a URI http request");
		}
		StringBuilder canonicalizedString = new StringBuilder(date);
		appendStringToCanonicalizedString(
				canonicalizedString, getCanonicalizedResource(((HttpUriRequest) request)
								.getURI(), uriComponents));
		return canonicalizedString.toString();
	}

	/**
	 * Append string to canonicalizedString.
	 * 
	 * @param canonicalizedString
	 * 			The stringBuilder of canonicalizedString.
	 * @param stringToAppend
	 * 			The canonicalized resource.
	 * @return canonicalizedString
	 */
	private static String appendStringToCanonicalizedString(
			StringBuilder canonicalizedString, String stringToAppend) {
		canonicalizedString.append(ConstChars.Linefeed);
		canonicalizedString.append(stringToAppend);
		return canonicalizedString.toString();
	}
	
	/**
	 * 
	 * @param headers
	 * 			The headers contain headers of the HTTP request.
	 * @param headerName
	 * 			Name of the header that we want to get values of.
	 * @return header value with header name.
 	 */
	private static String extractHeader(NameValueCollection headers, String headerName) {
		String value = Utilities.emptyString();

		// First extract all the content MD5 values from the header.
		List<String> httpContentMD5Values = HttpRequestAccessor
				.getHeaderValues(headers, headerName);

		// If we only have one, then set it to the value we want to append to
		// the canonicalized string.
		if (httpContentMD5Values != null && httpContentMD5Values.size() == 1) {
			value = (String) httpContentMD5Values.get(0);
		}
		return value;
	}

	/**
	 * Create a canonicalized string out of HTTP request header contents for
	 * signing blob/queue requests with the Shared Authentication scheme.
	 * 
	 * @param address
	 *            The uri address of the HTTP request.
	 * @param uriComponents
	 *            Components of the Uri extracted out of the request.
	 * @param method
	 *            The method of the HTTP request (GET/PUT, etc.).
	 * @param contentType
	 *            The content type of the HTTP request.
	 * @param date
	 *            The date of the HTTP request.
	 * @param headers
	 *            Should contain other headers of the HTTP request.
	 * @return A canonicalized string of the HTTP request.
	 */
	public static String canonicalizeHttpRequest(URI address,
			ResourceUriComponents uriComponents, String method,
			String contentType, String date, NameValueCollection headers) {
		
//		StringToSign = VERB + "\n" +
//        Content-Encoding + "\n"
//        Content-Language + "\n"
//        Content-Length + "\n"
//        Content-MD5 + "\n" +
//        Content-Type + "\n" +
//        Date + "\n" +
//        If-Modified-Since + "\n"
//        If-Match + "\n"
//        If-None-Match + "\n"
//        If-Unmodified-Since + "\n"
//        Range + "\n"
//        CanonicalizedHeaders + 
//        CanonicalizedResource;
		
		// The first element should be the Method of the request.
		// I.e. GET, POST, PUT, or HEAD.
		CanonicalizedString canonicalizedString = new CanonicalizedString(
				method);

		
		canonicalizedString.appendCanonicalizedElement(extractHeader(headers, HeaderNames.ContentEncoding));
		canonicalizedString.appendCanonicalizedElement(extractHeader(headers, HeaderNames.ContentLanguage));
		String length = extractHeader(headers, HeaderNames.ContentLength);
		if(length.equals(""))
			length = "0";
		canonicalizedString.appendCanonicalizedElement(length);
		canonicalizedString.appendCanonicalizedElement(extractHeader(headers, HeaderNames.ContentMD5));

		// content type.
		canonicalizedString.appendCanonicalizedElement(contentType);

		// The fourth element should be the request date.
		// See if there's an storage date header.
		// If there's one, then don't use the date header.
		List<String> httpStorageDateValues = HttpRequestAccessor
				.getHeaderValues(headers, HeaderNames.StorageDateTime);
		if (httpStorageDateValues != null && httpStorageDateValues.size() > 0) {
			date = "";
		}
		if (date != null) {
			canonicalizedString.appendCanonicalizedElement(date);
		}
		
		canonicalizedString.appendCanonicalizedElement(extractHeader(headers, HeaderNames.IfModifiedSince));
		canonicalizedString.appendCanonicalizedElement(extractHeader(headers, HeaderNames.IfMatch));
		canonicalizedString.appendCanonicalizedElement(extractHeader(headers, HeaderNames.IfNoneMatch));
		canonicalizedString.appendCanonicalizedElement(extractHeader(headers, HeaderNames.IfUnmodifiedSince));
		canonicalizedString.appendCanonicalizedElement(extractHeader(headers, HeaderNames.Range));

		
		// Look for header names that start with
		// StorageHttpConstants.HeaderNames.PrefixForStorageHeader
		// Then sort them in case-insensitive manner.
		ArrayList<String> httpStorageHeaderNameArray = new ArrayList<String>();
		for (Object keyObj : headers.keySet()) {
			String key = (String) keyObj;
			if (key.toLowerCase()
					.startsWith(HeaderNames.PrefixForStorageHeader)) {
				httpStorageHeaderNameArray.add(key.toLowerCase());
			}
		}

		Collections.sort(httpStorageHeaderNameArray, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		});

		for (String key : httpStorageHeaderNameArray) {
			StringBuilder canonicalizedElement = new StringBuilder(key);
			String delimiter = ConstChars.Colon;
			List<String> values = HttpRequestAccessor.getHeaderValues(headers,
					key);
			for (String headerValue : values) {
				// Unfolding is simply removal of CRLF.
				String unfoldedValue = headerValue.replaceAll(
						ConstChars.CarriageReturnLinefeed, Utilities
								.emptyString());
				// Append it to the canonicalized element string.
				canonicalizedElement.append(delimiter);
				canonicalizedElement.append(unfoldedValue);
				delimiter = ConstChars.Comma;
			}
			// Now, add this canonicalized element to the canonicalized header
			// string.
			canonicalizedString.appendCanonicalizedElement(canonicalizedElement
					.toString());
		}

		
		
		// Now we append the canonicalized resource element.
		String canonicalizedResource = getCanonicalizedResource(address,
				uriComponents);
		canonicalizedString.appendCanonicalizedElement(canonicalizedResource);

		return canonicalizedString.getValue();
	}

	

	/**
	 * Create the canonicalized resource with the url address and resourceUriComponents.
	 * @param address
	 * 			The uri address of the HTTP request.
	 * @param uriComponents
	 * 			Components of the Uri extracted out of the request.
	 * @return canonicalized resource
	 */
	private static String getCanonicalizedResource(URI address,
			ResourceUriComponents uriComponents) {
//		Beginning with an empty string (""), append a forward slash (/), followed by the name of the account that owns the resource being accessed.
//		Append the resource's encoded URI path, without any query parameters.
//		Retrieve all query parameters on the resource URI, including the comp parameter if it exists.
//		Convert all parameter names to lowercase.
//		Sort the query parameters lexicographically by parameter name, in ascending order.
//		URL-decode each query parameter name and value.
//		Append each query parameter name and value to the string in the following format, making sure to include the colon (:) between the name and the value:
//		parameter-name:parameter-value
//		If a query parameter has more than one value, sort all values lexicographically, then include them in a comma-separated list:
//		parameter-name:parameter-value-1,parameter-value-2,parameter-value-n
//		Append a new line character (\n) after each name-value pair. 
		

//		Get Container Metadata
//		   GET http://myaccount.blob.core.windows.net/mycontainer?restype=container&comp=metadata 
//		CanonicalizedResource:
//		    /myaccount/mycontainer\ncomp:metadata\nrestype:container
//
//		List Blobs operation:
//		    GET http://myaccount.blob.core.windows.net/container?restype=container&comp=list&include=snapshots&include=metadata&include=uncommittedblobs
//		CanonicalizedResource:
//		    /myaccount/mycontainer\ncomp:list\ninclude:metadata,snapshots,uncommittedblobs\nrestype:container


		StringBuilder canonicalizedResource = new StringBuilder(
				ConstChars.Slash);
		canonicalizedResource.append(uriComponents.getAccountName());

		// Note that AbsolutePath starts with a '/'.
		 
		String path = address.getRawPath();
//		path = path.replaceAll(" ", "%20");
//		path = java.net.URLEncoder.encode(path);
		canonicalizedResource.append(path);

		NameValueCollection query = HttpUtilities
				.parseQueryString(address.getQuery());
		
		ArrayList<String> paramNames = new ArrayList<String>();
		paramNames.addAll( query.keySet());
		Collections.sort(paramNames, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		});
		
		for (String key : paramNames) {
			StringBuilder canonicalizedElement = new StringBuilder(URLDecoder.decode( key));
			canonicalizedElement.append( ConstChars.Colon);
			String value = query.getMultipleValuesAsString(key);
			canonicalizedElement.append(URLDecoder.decode(value) );
		
			canonicalizedResource.append(ConstChars.Linefeed);
			canonicalizedResource.append(canonicalizedElement
					.toString());
		}

		return canonicalizedResource.toString();
	}

	/**
	 * An internal class that stores the canonicalized string version of an HTTP
	 * request.
	 * 
	 */
	private static class CanonicalizedString {

		private StringBuilder canonicalizedString = new StringBuilder();

		/**
		 * Append additional canonicalized element to the string.
		 * 
		 * @return
		 */
		public String getValue() {
			return canonicalizedString.toString();
		}

		/**
		 * Constructor for the class.
		 * 
		 * @param initialElement
		 *            The first canonicalized element to start the string with.
		 */
		public CanonicalizedString(String initialElement) {
			canonicalizedString.append(initialElement);
		}

		/**
		 * Append additional canonicalized element to the string.
		 * 
		 * @param element
		 *            An additional canonicalized element to append to the
		 *            string.
		 */
		public void appendCanonicalizedElement(String element) {
			canonicalizedString.append(ConstChars.Linefeed);
			canonicalizedString.append(element);
		}
	}
}

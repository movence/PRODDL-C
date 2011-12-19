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
package org.soyatec.windowsazure.internal.util.xml;

import java.io.InputStream;
import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.soyatec.windowsazure.error.StorageErrorCode;
import org.soyatec.windowsazure.error.StorageServerException;
import org.soyatec.windowsazure.internal.constants.HttpStatusConstant;

/**
 * Help to parse HTTP response
 * 
 */
public class XmlUtil {

	/**
	 * Load the stream and parse it to XML document.
	 * 
	 * @param stream
	 * @return Document in dom4j
	 * @throws StorageServerException
	 */
	public static Document load(final InputStream stream)
			throws StorageServerException {
		SAXReader reader = new SAXReader();
		try {
			return reader.read(stream);
		} catch (DocumentException e) {
			throw new StorageServerException(
					StorageErrorCode.ServiceBadResponse,
					"The result of a List operation could not be parsed",
					HttpStatusConstant.DEFAULT_STATUS, e);
		}
		/*
		 * try { String str = Utilities.convertStreamToString(stream); str =
		 * str.substring(str.indexOf("?>")+2); return parseXmlString(str); }
		 * catch (Exception e) { throw new StorageServerException(
		 * StorageErrorCode.ServiceBadResponse,
		 * "The result of a List operation could not be parsed",
		 * HttpStatusConstant.DEFAULT_STATUS, e); }
		 */
	}

	/**
	 * Load the stream and parse it to XML document.
	 * 
	 * @param stream
	 * @param parseMessage
	 * @return Document in dom4j
	 * @throws DatabaseServerException
	 */
	public static Document load(final InputStream stream, String parseMessage)
			throws StorageServerException {
		SAXReader reader = new SAXReader();
		try {
			return reader.read(stream);
		} catch (DocumentException e) {
			throw new StorageServerException(
					StorageErrorCode.ServiceBadResponse, parseMessage,
					HttpStatusConstant.DEFAULT_STATUS, e);
		}
	}

	/**
	 * Parse the string xml content to XML Document.
	 * @param xmlContent
	 * @return Document in dom4j
	 * @throws Exception
	 */
	public static Document parseXmlString(final String xmlContent)
			throws Exception {
		SAXReader reader = new SAXReader();
		return reader.read(new StringReader(xmlContent));
	}
}

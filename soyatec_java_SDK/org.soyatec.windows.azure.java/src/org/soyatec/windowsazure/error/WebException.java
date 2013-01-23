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
package org.soyatec.windowsazure.error;

import org.soyatec.windowsazure.constants.WebExceptionStatus;
import org.soyatec.windowsazure.internal.constants.HttpWebResponse;

/**
 * 
 * The <code>WebException</code> class represents the exception relative to Web communication.
 *
 */
public class WebException extends Exception {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -3447285880472563808L;
	private HttpWebResponse response;
	private WebExceptionStatus status;

	public WebException(HttpWebResponse response) {
		this.response = response;
	}

	/**
	 * @return response of WebException
	 */
	public HttpWebResponse getResponse() {
		return response;
	}

	/**
	 * @param response
	 * 			the response to set
	 */
	public void setResponse(HttpWebResponse response) {
		this.response = response;
	}

	/**
	 * @return status of WebException
	 */
	public WebExceptionStatus getStatus() {
		return status;
	}

	/**
	 * @param status
	 * 			the status
	 */
	public void setStatus(WebExceptionStatus status) {
		this.status = status;
	}
}

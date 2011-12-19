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

import org.soyatec.windowsazure.internal.util.NameValueCollection;

/**
 * 
 * The class represent the storage extended error information of the
 * StorageException.
 * 
 */
public class StorageExtendedErrorInformation {
	private String errorCode;
	private String errorMessage;
	private NameValueCollection additionalDetails;

	/**
	 * Error body content return by http response
	 */
	private String errorBody;

	/**
	 * @return errorCode
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * @param errorCode
	 *            the errorCode to set
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * @return errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage
	 *            the error message to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @return additionalDetails
	 */
	public NameValueCollection getAdditionalDetails() {
		return additionalDetails;
	}

	/**
	 * @param additionalDetails
	 *            the additionalDetails to set
	 */
	public void setAdditionalDetails(NameValueCollection additionalDetails) {
		this.additionalDetails = additionalDetails;
	}

	/**
	 * @return the errorBody
	 */
	public String getErrorBody() {
		return errorBody;
	}

	/**
	 * @param errorBody
	 *            the errorBody to set
	 */
	public void setErrorBody(String errorBody) {
		this.errorBody = errorBody;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "StorageExtendedErrorInformation [additionalDetails="
				+ additionalDetails + ", errorBody=" + errorBody
				+ ", errorCode=" + errorCode + ", errorMessage=" + errorMessage
				+ "]";
	}

}

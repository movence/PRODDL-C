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
package org.soyatec.windowsazure.management;

/**
 * This class defines the Certificate structure.
 * 
 * @author yyang
 */
public class Certificate {
	private String certificateUrl;

	private String thumbprintAlgorithm;

	private String thumbprint;

	private byte[] data;

	/**
	 * @return certificateUrl
	 */
	public String getCertificateUrl() {
		return certificateUrl;
	}

	/**
	 * @param certificateUrl
	 * 			the certificateUrl to set
	 */
	public void setCertificateUrl(String certificateUrl) {
		this.certificateUrl = certificateUrl;
	}

	/**
	 * @return thumbprintAlgorithm
	 */
	public String getThumbprintAlgorithm() {
		return thumbprintAlgorithm;
	}

	/**
	 * @param thumbprintAlgorithm
	 * 			the thumbprintAlgorithm to set
	 */
	public void setThumbprintAlgorithm(String thumbprintAlgorithm) {
		this.thumbprintAlgorithm = thumbprintAlgorithm;
	}

	/**
	 * @return thumbprint
	 */
	public String getThumbprint() {
		return thumbprint;
	}

	/**
	 * @param thumbprint
	 * 			the thumbprint to set
	 */
	public void setThumbprint(String thumbprint) {
		this.thumbprint = thumbprint;
	}

	/**
	 * @return data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @param data
	 * 			the data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

}

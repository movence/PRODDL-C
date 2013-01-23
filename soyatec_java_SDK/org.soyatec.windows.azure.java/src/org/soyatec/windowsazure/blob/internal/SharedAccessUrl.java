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

import org.soyatec.windowsazure.blob.ISharedAccessUrl;

public class SharedAccessUrl implements ISharedAccessUrl {

	private String accountName;

	private String blobName;

	private String containerName;

	private String signedResource;

	private String signedStart;

	private String signedExpiry;

	private String signedPermissions;

	private String signedIdentifier;

	private String signature;

	private String restUrl;

	private String signedString;

	public static ISharedAccessUrl parse(String url) {
		SharedAccessUrl share = new SharedAccessUrl();
		String[] parts = url.split("\\?");
		share.restUrl = parts[0];
		share.signedString = parts[1];
		int pos = url.indexOf(".blob.core.windows.net");
		if (pos > -1) {
			share.accountName = url.substring("http://".length(), pos);
			url = url.substring(pos + ".blob.core.windows.net/".length(), url
					.indexOf('?'));
		} else {
			pos = url.indexOf("blob.core.windows.net/")
					+ "blob.core.windows.net/".length();
			share.accountName = url.substring(pos, url.indexOf('/', pos));
			url = url.substring(url.indexOf('/', pos) + 1, url
					.indexOf('?', pos));
		}
		parts = url.split("/");
		share.containerName = parts[0];
		if (parts.length > 1)
			share.blobName = parts[1];

		String[] params = share.signedString.split("&");
		for (String param : params) {
			if (param.indexOf("st=") > -1)
				share.signedStart = param.substring(3);
			else if (param.indexOf("sr=") > -1)
				share.signedResource = param.substring(3);
			else if (param.indexOf("se=") > -1)
				share.signedExpiry = param.substring(3);
			else if (param.indexOf("sp=") > -1)
				share.signedPermissions = param.substring(3);
			else if (param.indexOf("si=") > -1)
				share.signedIdentifier = param.substring(3);
			else if (param.indexOf("sig=") > -1)
				share.signature = param.substring(4);
		}

		return share;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#getAccountName()
	 */
	public String getAccountName() {
		return accountName;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#setAccountName(java.lang.String)
	 */
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#getBlobName()
	 */
	public String getBlobName() {
		return blobName;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#setBlobName(java.lang.String)
	 */
	public void setBlobName(String blobName) {
		this.blobName = blobName;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#getContainerName()
	 */
	public String getContainerName() {
		return containerName;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#setContainerName(java.lang.String)
	 */
	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#getSignedResource()
	 */
	public String getSignedResource() {
		return signedResource;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#setSignedResource(java.lang.String)
	 */
	public void setSignedResource(String signedResource) {
		this.signedResource = signedResource;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#getSignedStart()
	 */
	public String getSignedStart() {
		return signedStart;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#setSignedStart(java.lang.String)
	 */
	public void setSignedStart(String signedStart) {
		this.signedStart = signedStart;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#getSignedExpiry()
	 */
	public String getSignedExpiry() {
		return signedExpiry;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#setSignedExpiry(java.lang.String)
	 */
	public void setSignedExpiry(String signedExpiry) {
		this.signedExpiry = signedExpiry;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#getSignedPermissions()
	 */
	public String getSignedPermissions() {
		return signedPermissions;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#setSignedPermissions(java.lang.String)
	 */
	public void setSignedPermissions(String signedPermissions) {
		this.signedPermissions = signedPermissions;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#getSignedIdentifier()
	 */
	public String getSignedIdentifier() {
		return signedIdentifier;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#setSignedIdentifier(java.lang.String)
	 */
	public void setSignedIdentifier(String signedIdentifier) {
		this.signedIdentifier = signedIdentifier;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#getSignature()
	 */
	public String getSignature() {
		return signature;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#setSignature(java.lang.String)
	 */
	public void setSignature(String signature) {
		this.signature = signature;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#getRestUrl()
	 */
	public String getRestUrl() {
		return restUrl;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#setRestUrl(java.lang.String)
	 */
	public void setRestUrl(String restUrl) {
		this.restUrl = restUrl;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#getSignedString()
	 */
	public String getSignedString() {
		return signedString;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.ISharedAccessUrl#setSignedString(java.lang.String)
	 */
	public void setSignedString(String signedString) {
		this.signedString = signedString;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ShareAccessUrl [accountName=" + accountName + ", blobName="
				+ blobName + ", containerName=" + containerName + ", restUrl="
				+ restUrl + ", signature=" + signature + ", signedExpiry="
				+ signedExpiry + ", signedIdentifier=" + signedIdentifier
				+ ", signedPermissions=" + signedPermissions
				+ ", signedResource=" + signedResource + ", signedStart="
				+ signedStart + ", signedString=" + signedString + "]";
	}

}

package org.soyatec.windowsazure.blob;

/**
 * 
 * The <code>SharedAccessUrl</code> class defines a shared access url.
 * 
 */
public interface ISharedAccessUrl {

	/**
	 * @return the accountName
	 */
	public abstract String getAccountName();

	/**
	 * @param accountName
	 *            the accountName to set
	 */
	public abstract void setAccountName(String accountName);

	/**
	 * @return the blobName
	 */
	public abstract String getBlobName();

	/**
	 * @param blobName
	 *            the blobName to set
	 */
	public abstract void setBlobName(String blobName);

	/**
	 * @return the containerName
	 */
	public abstract String getContainerName();

	/**
	 * @param containerName
	 *            the containerName to set
	 */
	public abstract void setContainerName(String containerName);

	/**
	 * @return the signedResource
	 */
	public abstract String getSignedResource();

	/**
	 * @param signedResource
	 *            the signedResource to set
	 */
	public abstract void setSignedResource(String signedResource);

	/**
	 * @return the signedStart
	 */
	public abstract String getSignedStart();

	/**
	 * @param signedStart
	 *            the signedStart to set
	 */
	public abstract void setSignedStart(String signedStart);

	/**
	 * @return the signedExpiry
	 */
	public abstract String getSignedExpiry();

	/**
	 * @param signedExpiry
	 *            the signedExpiry to set
	 */
	public abstract void setSignedExpiry(String signedExpiry);

	/**
	 * @return the signedPermissions
	 */
	public abstract String getSignedPermissions();

	/**
	 * @param signedPermissions
	 *            the signedPermissions to set
	 */
	public abstract void setSignedPermissions(String signedPermissions);

	/**
	 * @return the signedIdentifier
	 */
	public abstract String getSignedIdentifier();

	/**
	 * @param signedIdentifier
	 *            the signedIdentifier to set
	 */
	public abstract void setSignedIdentifier(String signedIdentifier);

	/**
	 * @return the signature
	 */
	public abstract String getSignature();

	/**
	 * @param signature
	 *            the signature to set
	 */
	public abstract void setSignature(String signature);

	/**
	 * @return the restUrl
	 */
	public abstract String getRestUrl();

	/**
	 * @param restUrl
	 *            the restUrl to set
	 */
	public abstract void setRestUrl(String restUrl);

	/**
	 * @return the signedString
	 */
	public abstract String getSignedString();

	/**
	 * @param signedString
	 *            the signedString to set
	 */
	public abstract void setSignedString(String signedString);

}
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

import java.net.URI;
import java.sql.Timestamp;

import org.soyatec.windowsazure.blob.BlobType;
import org.soyatec.windowsazure.blob.IBlobProperties;
import org.soyatec.windowsazure.blob.LeaseStatus;
import org.soyatec.windowsazure.internal.util.NameValueCollection;


/// The properties of a blob.
/// No member of this class makes a storage service request.
public class BlobProperties implements IBlobProperties {

	/**
	 * The name of the blob BlobProperties
	 */
	private String name;

	/**
	 * The base URI of the blob BlobProperties
	 */
	private URI uri;

	/**
	 * The contentEncoding of the blob BlobProperties
	 */
	private String contentEncoding;

	/**
	 * The contentType of the blob BlobProperties
	 */
	private String contentType;

	/**
	 * The contentLanguage of the blob BlobProperties
	 */
	private String contentLanguage;

	/**
	 * The contentLength of the blob BlobProperties
	 */
	private long contentLength;

	/**
	 * The metadata of the blob BlobProperties
	 */
	private NameValueCollection metadata;

	/**
	 * The lastModifiedTime of the blob BlobProperties
	 */
	private Timestamp lastModifiedTime;

	/**
	 * The eTag of the blob BlobProperties
	 */
	private String eTag;

	/**
	 * The snapshot of the blob BlobProperties
	 */
	private Timestamp snapshot;
	
	/**
	 * The blobType of the blob BlobProperties
	 */
	private BlobType blobType;
	
	/**
	 * The leaseStatus of the blob BlobProperties
	 */
	private LeaseStatus leaseStatus;
	
	
	/**
	 * The md5 for blob content
	 */
	private String contentMD5;
	
	public BlobProperties(String name) {
		this.name = name;
	}

	void assign(BlobProperties other) {
		name = other.name;
		uri = other.uri;
		contentEncoding = other.contentEncoding;
		contentLength = other.contentLength;
		contentType = other.contentType;
		contentMD5 = other.contentMD5;
		eTag = other.eTag;
		lastModifiedTime = other.lastModifiedTime;
		if(other.metadata!=null){
			metadata = new NameValueCollection();
			metadata.putAll(other.metadata);
		}
	}
    
	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#getUri()
	 */
	public URI getUri() {
		return uri;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#getContentEncoding()
	 */
	public String getContentEncoding() {
		return contentEncoding;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#getContentType()
	 */
	public String getContentType() {
		return contentType;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#getContentLanguage()
	 */
	public String getContentLanguage() {
		return contentLanguage;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#getContentLength()
	 */
	public long getContentLength() {
		return contentLength;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#getMetadata()
	 */
	public NameValueCollection getMetadata() {
		return metadata;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#getLastModifiedTime()
	 */
	public Timestamp getLastModifiedTime() {
		return lastModifiedTime;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#getETag()
	 */
	public String getETag() {
		return eTag;
	}

	/**
	 * 
	 * @param name
	 *          the blob name to set
	 */
	void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @param uri
	 *          the blob uri to set
	 */
	public void setUri(URI uri) {
		this.uri = uri;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#setContentEncoding(java.lang.String)
	 */
	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#setContentType(java.lang.String)
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#setContentLanguage(java.lang.String)
	 */
	public void setContentLanguage(String contentLanguage) {
		this.contentLanguage = contentLanguage;
	}

	/**
	 * 
	 * @param contentLength
	 *          the blob contentLength to set
	 */
	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#setMetadata(org.soyatec.windowsazure.util.NameValueCollection)
	 */
	public void setMetadata(NameValueCollection metadata) {
		this.metadata = metadata;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#setLastModifiedTime(java.sql.Timestamp)
	 */
	public void setLastModifiedTime(Timestamp lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#setETag(java.lang.String)
	 */
	public void setETag(String tag) {
		eTag = tag;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#getSnapshot()
	 */
	public Timestamp getSnapshot() {
		return snapshot;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#setSnapshot(java.sql.Timestamp)
	 */
	public void setSnapshot(Timestamp snapshot) {
		this.snapshot = snapshot;
	}

    /* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#getBlobType()
	 */
	public BlobType getBlobType() {
		return blobType;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#setBlobType(org.soyatec.windowsazure.blob.BlobType)
	 */
	public void setBlobType(BlobType blobType) {
		this.blobType = blobType;
	}

    /* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#getLeaseStatus()
	 */
	public LeaseStatus getLeaseStatus() {
		return leaseStatus;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobProperties#setLeaseStatus(org.soyatec.windowsazure.blob.impl.LeaseStatus)
	 */
	public void setLeaseStatus(LeaseStatus leaseStatus) {
		this.leaseStatus = leaseStatus;
	}

	public void setContentMD5(String md5) {
		contentMD5 = md5;
	}

	public String getContentMD5() {		
		return contentMD5;
	}
	
}

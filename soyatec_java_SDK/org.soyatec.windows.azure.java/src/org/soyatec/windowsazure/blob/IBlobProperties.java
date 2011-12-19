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
package org.soyatec.windowsazure.blob;

import java.net.URI;
import java.sql.Timestamp;

import org.soyatec.windowsazure.internal.util.NameValueCollection;

/**
 * The properties of a blob.
 * No member of this class makes a storage service request.
 */
public interface IBlobProperties {

	/**
	 * @return the blob name
	 */
	public String getName();

	/**
	 * @return the blob uri
	 */
	public URI getUri();

	/**
	 * @return the blob contentEncoding
	 */
	public String getContentEncoding();

	/**
	 * @return the blob contentType
	 */
	public String getContentType();

	/**
	 * @return the blob contentType
	 */
	public String getContentLanguage();

	/**
	 * @return the blob contentLength
	 */
	public long getContentLength();
	
	/**
	 * 
	 * @param value the md5 value for blob content
	 */
	public void setContentMD5(String value);
	
	/**
	 * 
	 * @return the md5 value for blob content
	 */
	public String getContentMD5();
	

	/**
	 * @return the blob metadata
	 */
	public NameValueCollection getMetadata();

	/**
	 * @return the blob lastModifiedTime
	 */
	public Timestamp getLastModifiedTime();

	/**
	 * @return the blob eTag
	 */
	public String getETag();

	/**
	 * 
	 * @param contentEncoding
	 *          the blob contentEncoding to set
	 */
	public void setContentEncoding(String contentEncoding);

	/**
	 * 
	 * @param contentType
	 *          the blob contentType to set
	 */
	public void setContentType(String contentType);

	/**
	 * 
	 * @param contentLanguage
	 *          the blob contentLanguage to set
	 */
	public void setContentLanguage(String contentLanguage);

	/**
	 * 
	 * @param metadata
	 *          the blob metadata to set
	 */
	public void setMetadata(NameValueCollection metadata);

	/**
	 * 
	 * @param lastModifiedTime
	 *          the blob lastModifiedTime to set
	 */
	public void setLastModifiedTime(Timestamp lastModifiedTime);

	/**
	 * 
	 * @param tag
	 *          the blob tag to set
	 */
	public void setETag(String tag);

	/**
	 * @return the blob snapshot
	 */
	public Timestamp getSnapshot();

	/**
	 * @param snapshot
	 *          the blob snapshot to set
	 */
	public void setSnapshot(Timestamp snapshot);

	/**
	 * @return the blob blobType
	 */
	public BlobType getBlobType();

	/**
	 * 
	 * @param blobType
	 *          the blob blobType to set
	 */
	public void setBlobType(BlobType blobType);

	/**
	 * @return the blob leaseStatus
	 */
	public LeaseStatus getLeaseStatus();

	/**
	 * 
	 * @param leaseStatus
	 *          the blob leaseStatus to set
	 */
	public void setLeaseStatus(LeaseStatus leaseStatus);

}
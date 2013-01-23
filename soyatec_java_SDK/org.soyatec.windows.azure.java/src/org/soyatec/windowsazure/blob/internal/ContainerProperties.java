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

import org.soyatec.windowsazure.blob.IContainerProperties;
import org.soyatec.windowsazure.internal.util.NameValueCollection;


/// The properties of a container.
/// No member of this class makes a storage service request.
public class ContainerProperties implements IContainerProperties {
	
	/**
	 * The name of the Container
	 */
	private String name;
	
	private String eTag;
	
	private Timestamp lastModifiedTime;
	
	private URI uri;
	
	private NameValueCollection metadata;
	
	public ContainerProperties(String name){
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IContainerProperties#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IContainerProperties#getETag()
	 */
	public String getETag() {
		return eTag;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IContainerProperties#getLastModifiedTime()
	 */
	public Timestamp getLastModifiedTime() {
		return lastModifiedTime;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IContainerProperties#getUri()
	 */
	public URI getUri() {
		return uri;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IContainerProperties#getMetadata()
	 */
	public NameValueCollection getMetadata() {
		return metadata;
	}

	void setName(String name) {
		this.name = name;
	}

	void setETag(String tag) {
		eTag = tag;
	}

	void setLastModifiedTime(Timestamp lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}

	void setUri(URI uri) {
		this.uri = uri;
	}

	void setMetadata(NameValueCollection metadata) {
		this.metadata = metadata;
	}
}

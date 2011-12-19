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
 * The <code>ContainerProperties</code> class represents the properties of a container.
 * No member of this class makes a storage service request.
 * 
 */ 
public interface IContainerProperties {

	/**
	 * @return name
	 */
	public String getName();

	/**
	 * @return eTag
	 */
	public String getETag();

	/**
	 * @return last modified time
	 */
	public Timestamp getLastModifiedTime();

	/**
	 * @return uri
	 */
	public URI getUri();

	/**
	 * @return metadata
	 */
	public NameValueCollection getMetadata();
}
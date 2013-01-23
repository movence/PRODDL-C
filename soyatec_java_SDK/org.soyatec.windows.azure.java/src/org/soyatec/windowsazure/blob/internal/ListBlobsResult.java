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

import java.util.List;

import org.soyatec.windowsazure.blob.IBlobProperties;


/**
 * @internal
 */
final class ListBlobsResult {
	private final List<IBlobProperties> blobsProperties;
	private final List<String> commonPrefixs;
	private final String nextMarker;

	/**
	 * 
	 * @param blobs
	 * @param commonPrefixs
	 * @param nextMarker
	 *         the list of blob to set
	 */
	public ListBlobsResult(final List<IBlobProperties> blobs, final List<String> commonPrefixs, final String nextMarker) {
		this.blobsProperties = blobs;
		this.commonPrefixs = commonPrefixs;
		this.nextMarker = nextMarker;
	}

	/**
	 * 
	 * @return the list of Blobs
	 */
	public List<IBlobProperties> getBlobsProperties() {
		return blobsProperties;
	}
    
	/**
	 * 
	 * @return the list of commonPrefixs
	 */
	public List<String> getCommonPrefixs() {
		return commonPrefixs;
	}

	/**
	 * 
	 * @return the nextMarker
	 */
	public String getNextMarker() {
		return nextMarker;
	}

}

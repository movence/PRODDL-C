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

import java.util.List;

import org.soyatec.windowsazure.blob.internal.PageRange;
import org.soyatec.windowsazure.blob.io.BlobStream;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.internal.util.NameValueCollection;

/**
 * This interface representing page blobs, which are optimized for random read/write operations and which provide the ability to write to a range of bytes in a blob.
 * Page blobs are a collection of pages. A page is a range of data that is identified by its offset from the start of the blob. http://msdn.microsoft.com/en-us/library/ee691964.aspx
 */
public interface IPageBlob extends IBlob {

	/**
	 * This operation returns the list of valid page ranges for a page blob or snapshot.
	 * @param headerParameters
	 * @return
	 * @throws StorageException
	 */
	public List<PageRange> getPageRegions( NameValueCollection headerParameters)
			throws StorageException;

	/**
	 * This operation writes a range of pages to a page blob.
	 *
	 * @param pageData
	 * 			The stream of page data.
	 * @param range
	 * 			For a page update operation, the page range can be up to 4 MB in size.
	 * 			For a page clear operation, the page range can be up to the value of the
	 * 			blob's full size. Given that pages must be aligned with 512-byte
	 * 			boundaries, the start offset must be a modulus of 512 and the end offset
	 * 			must be a modulus of 512 ¨C 1. Examples of valid byte ranges are 0-511,
	 * 			512-1023, etc. The Blob service accepts only a single byte range for the
	 * 			Range header, and the byte range must be specified in the following
	 * 			format: bytes=startByte-endByte.
	 * @param headerParameters
	 * 			The optional header request parameters.
	 * @throws StorageException
	 */
	public void writePages(	BlobStream pageData, IPageRange range,
			NameValueCollection headerParameters) throws StorageException;

	/**
	 * Get the blob content with range information
	 * 
	 * @param stream
	 *            The stream for hold content
	 * @param range
	 *            the range to be read
	 * @return the blob content
	 * @throws StorageException
	 */
	public IBlobContents getContents(BlobStream stream, IPageRange range) throws StorageException;
}

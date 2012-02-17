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

import java.sql.Timestamp;

import org.soyatec.windowsazure.blob.io.BlobStream;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.internal.util.NameValueCollection;

/**
 * The <code>Blob</code> class specifies contents and properties for blob.
 *
 */
public interface IBlob {

	/**
	 * The contents of the Blob in various forms.
	 *
	 * @return the contents of the Blob.
	 */
	public IBlobContents getContents(BlobStream stream)
			throws StorageException;

	/**
	 * Set the contents of the Blob.
	 *
	 * @param contents
	 */
	public void setContents(IBlobContents contents)
			throws StorageException;

	/**
	 * The properties of a blob.
	 *
	 * @return the properties of a blob.
	 */
	public IBlobProperties getProperties() throws StorageException;

	/**
	 * Set the properties of a blob.
	 *
	 * @param properties
	 */
	public void setProperties(IBlobProperties properties)
			throws StorageException;

	/**
	 * Updates an existing blob if it has not been modified since you retrieve
	 * it. Use this method to implement optimistic concurrency by avoiding
	 * clobbering changes to the blob made by another writer.
	 *
	 * @param blobProperties
	 *            The properties of the blob. This object should be one
	 *            previously obtained from a call to getProperties.
	 * @param contents
	 *            The contents of the blob. The contents of the blob should be
	 *            readable
	 * @return true if the blob is updated. false if the blob has changed since
	 *         the last time.
	 * @throws StorageException
	 */
	public boolean updateIfNotModified(IBlobProperties blobProperties,
			IBlobContents contents) throws StorageException;


	/**
	 * This operation creates a read-only snapshot of a blob.
	 *
	 * @param headerParameters
	 *            Optional request header parameters
	 * @return Timestamp value that uniquely identifies the snapshot. The value
	 *         indicates the snapshot version, and may be used in subsequent
	 *         requests to access the snapshot.
	 * @throws StorageException
	 */
	public Timestamp createSnapshot(NameValueCollection headerParameters)
			throws StorageException;

}
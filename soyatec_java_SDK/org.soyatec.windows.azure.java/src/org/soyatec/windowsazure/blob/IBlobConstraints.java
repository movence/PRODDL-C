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
import java.util.List;

import org.apache.http.message.BasicHeader;

/**
 * The <code>BlobConstraints</code> class specifies constraints for blob that
 * are used in the <code>BlobContainer</code> class.
 * 
 */
public interface IBlobConstraints {
	/**
	 * Gets all the blob constraints.
	 * 
	 * @return Constraint list.
	 */
	public List<BasicHeader> getConstraints();

	/**
	 * Sets the blob constraints.
	 * 
	 * @param constraints
	 *            Constraint list
	 */
	public void setConstraints(List<BasicHeader> constraints);

	/**
	 * Gets a new blob constraints to specifies the modification time for
	 * resource objects.
	 * 
	 * @param time
	 *            The timestamp to specifies after what time the source is
	 *            modified.
	 * @return BlobConstraints
	 */
	public IBlobConstraints isSourceModifiedSince(Timestamp time);

	/**
	 * Gets a new blob constraints to specifies the unmodification time for
	 * resource objects.
	 * 
	 * @param time
	 *            The timestamp to specifies after what time the source is not
	 *            modified.
	 * @return BlobConstraints
	 */
	public IBlobConstraints isSourceUnmodifiedSince(Timestamp time);

	/**
	 * Gets a new blob constraints to specifies the matching resource objects.
	 * 
	 * @param etag
	 *            the matched etag.
	 * @return BlobConstraints
	 */
	public IBlobConstraints isSourceMatch(String etag);

	/**
	 * Gets a new blob constraints to specifies the unmatching resource objects.
	 * 
	 * @param etag
	 *            the unmatched etag.
	 * @return BlobConstraints
	 */
	public IBlobConstraints isSourceNoneMatch(String etag);

	/**
	 * Gets a new blob constraints to specifies the modification time for
	 * destination object.
	 * 
	 * @param time
	 *            The timestamp to specifies after what time the destination is
	 *            modified.
	 * @return BlobConstraints
	 */
	public abstract IBlobConstraints isDestinationModifiedSince(Timestamp time);

	/**
	 * Gets a new blob constraints to specifies the unmodification time for
	 * destination object.
	 * 
	 * @param time
	 *            The timestamp to specifies after what time the destination is
	 *            not modified.
	 * @return BlobConstraints
	 */
	public IBlobConstraints isDestinationUnmodifiedSince(Timestamp time);

	/**
	 * Gets a new blob constraints to specifies the matching destination
	 * objects.
	 * 
	 * @param etag
	 *            the matched etag.
	 * @return BlobConstraints
	 */
	public IBlobConstraints isDestinationMatch(String etag);

	/**
	 * Gets a new blob constraints to specifies the unmatching destination
	 * objects.
	 * 
	 * @param etag
	 *            the matched etag.
	 * @return BlobConstraints
	 */
	public IBlobConstraints isDestinationNoneMatch(String etag);

}
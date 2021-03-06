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
package org.soyatec.windowsazure.queue.internal;

import org.soyatec.windowsazure.internal.util.NameValueCollection;
import org.soyatec.windowsazure.queue.IQueueProperties;

/**
 * The properties of a queue.
 * 
 */
public class QueueProperties implements IQueueProperties {

	// The approximated amount of messages in the queue.
	private int approximateMessageCount;

	// Metadata for the queue in the form of name-value pairs.
	private NameValueCollection metadata;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.IQueueProperties#getApproximateMessageCount()
	 */
	public int getApproximateMessageCount() {
		return approximateMessageCount;
	}

	/**
	 * Specify the approximated amount of messages in the queue.
	 * 
	 * @param approximateMessageCount
	 *            The approximated amount of messages in the queue.
	 */
	void setApproximateMessageCount(int approximateMessageCount) {
		this.approximateMessageCount = approximateMessageCount;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.IQueueProperties#getMetadata()
	 */
	public NameValueCollection getMetadata() {
		return metadata;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.IQueueProperties#setMetadata(org.soyatec.windowsazure.util.NameValueCollection)
	 */
	public void setMetadata(NameValueCollection metadata) {
		this.metadata = metadata;
	}

}

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
package org.soyatec.windowsazure.queue;

import java.sql.Timestamp;

/**
 * Objects of this class represent a single message in the queue.
 */
public interface IMessage {

	/**
	 * The maximum message size in bytes.
	 */
	public static final int MaxMessageSize = 8 * 1024;
	/**
	 * The maximum amount of time a message is kept in the queue. Max value is 7 days. Value is given in seconds.
	 */
	public static final int MaxTimeToLive = 7 * 24 * 60 * 60;

	/**
	 * Returns the the contents of the message as a string.
	 * 
	 * @return
	 */
	public String getContentAsString();

	/**
	 * Returns the content of the message as a byte array
	 */
	public byte[] getContentAsBytes();

	/**
	 * Returns the unique ID of the message.
	 * 
	 * @return The unique ID of the message
	 */
	public String getId();

	/**
	 * Return popReceipt which is used when deleting a message from the queue.
	 * 
	 * @return PopReceipt
	 */
	public String getPopReceipt();

	/**
	 * Returns the point in time when the message was put into the queue.
	 * 
	 * @return The point in time when the message was put into the queue.
	 */
	public Timestamp getInsertTime();

	/**
	 * Returns the message's expiration time.
	 * 
	 * @return The message's expiration time.
	 */
	public Timestamp getExpirationTime();

	/**
	 * Return the point in time when a message becomes visible again after a Get() operation was called that returned the message.
	 * 
	 * @return The point in time when a message becomes visible again after a Get() operation was called that returned the message.
	 */
	public Timestamp getNextVisibleTime();

}
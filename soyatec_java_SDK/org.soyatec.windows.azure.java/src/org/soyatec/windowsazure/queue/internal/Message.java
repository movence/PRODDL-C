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

import java.sql.Timestamp;

import org.soyatec.windowsazure.authenticate.Base64;
import org.soyatec.windowsazure.internal.util.Utilities;
import org.soyatec.windowsazure.queue.IMessage;

/**
 * Objects of this class represent a single message in the queue.
 */
public class Message implements IMessage {

	// A unique ID of the message as returned from queue operations.
	private String id;

	// When a message is retrieved from a queue, a PopReceipt is returned. The
	// PopReceipt is used when
	// deleting a message from the queue.
	private String popReceipt;

	// The point in time when the message was put into the queue.
	private Timestamp insertTime;

	// A message's expiration time.
	private Timestamp expirationTime;

	// The point in time when a message becomes visible again after a Get()
	// operation was called
	// that returned the message.
	private Timestamp nextVisibleTime;

	private byte[] content;

	/**
	 * Empty constructor.
	 */
	public Message() {
	}

	/**
	 * Creates a message and initializes the content of the message to be the specified string.
	 * 
	 * @param content
	 *            A string representing the contents of the message.
	 */
	public Message(String content) {
		if (content == null) {
			throw new IllegalArgumentException("Content cannot be null!");
		}
		this.content = content.getBytes();
	}

	/**
	 * Creates a message and given the specified byte contents. In this implementation, regardless of whether an XML or binary data is passed into this function, message contents are converted to base64 before passing the data to the queue service. When calculating the size of the message, the size of the base64 encoding is thus the important parameter.
	 * 
	 * @param content
	 */
	public Message(byte[] content) {
		if (content == null) {
			throw new IllegalArgumentException("Content cannot be null!");
		}
		if (Base64.encode(content).length() > MaxMessageSize) {
			throw new IllegalArgumentException("Message body is too big!");
		}
		this.content = content;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.IMessage#getContentAsString()
	 */
	public String getContentAsString() {
		return new String(content);
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.IMessage#getContentAsBytes()
	 */
	public byte[] getContentAsBytes() {
		return content;
	}

	/**
	 * When calling the Get() operation on a queue, the content of messages returned in the REST protocol are represented as Base64-encoded strings. This internal function transforms the Base64 representation into a byte array.
	 * 
	 * @param str
	 *            The Base64-encoded string.
	 */
	void setContentFromBase64String(String str) {
		if (Utilities.isNullOrEmpty(str)) {
			// we got a message with an empty <MessageText> element
			this.content = Utilities.emptyString().getBytes();
		} else {
			this.content = Base64.decode(str);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.IMessage#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.IMessage#getPopReceipt()
	 */
	public String getPopReceipt() {
		return popReceipt;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.IMessage#getInsertTime()
	 */
	public Timestamp getInsertTime() {
		return insertTime;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.IMessage#getExpirationTime()
	 */
	public Timestamp getExpirationTime() {
		return expirationTime;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.IMessage#getNextVisibleTime()
	 */
	public Timestamp getNextVisibleTime() {
		return nextVisibleTime;
	}

	/**
	 * Specify the unique ID of the message.
	 * 
	 * @param id
	 *            The unique ID of the message
	 */
	void setId(String id) {
		this.id = id;
	}

	/**
	 * Specify the popReceipt for this message which is used when deleting a message from the queue.
	 * 
	 * @param popReceipt
	 *            The popReceipt for this message which is used when deleting a message from the queue.
	 */
	void setPopReceipt(String popReceipt) {
		this.popReceipt = popReceipt;
	}

	/**
	 * Specify the point in time when the message was put into the queue.
	 * 
	 * @param insertTime
	 *            The point in time when the message was put into the queue.
	 */
	void setInsertTime(Timestamp insertTime) {
		this.insertTime = insertTime;
	}

	/**
	 * Specify the message's expiration time.
	 * 
	 * @param expirationTime
	 *            The message's expiration time.
	 */
	void setExpirationTime(Timestamp expirationTime) {
		this.expirationTime = expirationTime;
	}

	/**
	 * Specify the point in time when a message becomes visible again after a Get() operation was called that returned the message.
	 * 
	 * @param nextVisibleTime
	 *            The point in time when a message becomes visible again after a Get() operation was called that returned the message.
	 */
	void setNextVisibleTime(Timestamp nextVisibleTime) {
		this.nextVisibleTime = nextVisibleTime;
	}

}

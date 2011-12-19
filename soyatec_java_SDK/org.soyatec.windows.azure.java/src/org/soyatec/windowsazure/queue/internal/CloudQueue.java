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

import java.net.URI;
import java.text.MessageFormat;
import java.util.List;

import org.apache.http.HttpStatus;
import org.soyatec.windowsazure.authenticate.StorageAccountInfo;
import org.soyatec.windowsazure.blob.IRetryPolicy;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.internal.util.TimeSpan;
import org.soyatec.windowsazure.internal.util.Utilities;
import org.soyatec.windowsazure.queue.IMessage;
import org.soyatec.windowsazure.queue.IMessageReceivedListener;
import org.soyatec.windowsazure.queue.IQueue;
import org.soyatec.windowsazure.queue.IQueueProperties;

/**
 * 
 * Objects of this class represent a queue in a user's storage account.
 * 
 */
public abstract class CloudQueue implements IQueue {

	/**
	 * Holding a collection of listeners who will be notified when a new message
	 * is put into the queue.
	 */
	protected transient MessageReceivedListenerSupport changeSupport = new MessageReceivedListenerSupport();

	/**
	 * The name of the queue.
	 */
	private String name;

	/**
	 * The user account this queue lives in.
	 */
	private StorageAccountInfo account;

	/**
	 * Indicates whether to use/generate path-style or host-style URIs
	 */
	private boolean usePathStyleUris = Boolean.TRUE;

	/**
	 * The URI of the queue
	 */
	private URI queueUri;

	/**
	 * The retry policy used for retrying requests; this is the retry policy of
	 * the storage account where this queue was created
	 */
	private IRetryPolicy retryPolicy;

	/**
	 * The timeout of requests.
	 */
	private TimeSpan timeout;

	/**
	 * This constructor is only called by subclasses.
	 */
	protected CloudQueue() {
		// queues are generated using factory methods
	}

	/**
	 * This constructor is only called by subclasses.
	 * 
	 * @param name
	 *            The name of the queue.
	 * @param account
	 *            The user account this queue lives in
	 */
	protected CloudQueue(String name, StorageAccountInfo account) {
		if (Utilities.isNullOrEmpty(name)) {
			throw new IllegalArgumentException(
					"Queue name cannot be null or empty!");
		}
		if (account == null) {
			throw new IllegalArgumentException(
					"Account information is not given!");
		}
		if (!Utilities.isValidContainerOrQueueName(name)) {
			throw new IllegalArgumentException(
					MessageFormat
							.format(
									"The specified queue name \"{0}\" is not valid!"
											+ "\nPlease choose a name that conforms to the naming conventions for queues!"
											+ ""
											+ "\nSee <a>http://msdn.microsoft.com/en-us/library/dd179349.aspx</a> for more information.",
									name));
		}
		this.name = name;
		this.account = account;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#createQueue()
	 */
	public abstract boolean createQueue() throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#isQueueExist()
	 */
	public boolean isQueueExist() {
		try {
			getProperties();
			return true;
		} catch (StorageException e) {
			if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				return false;
			}
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#deleteQueue()
	 */
	public abstract boolean deleteQueue() throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#setProperties(org.soyatec.windowsazure.queue.IQueueProperties)
	 */
	public abstract boolean setProperties(IQueueProperties properties)
			throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#getProperties()
	 */
	public abstract IQueueProperties getProperties() throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#getApproximateCount()
	 */
	public abstract int getApproximateCount() throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#putMessage(org.soyatec.windowsazure.queue.IMessage)
	 */
	public abstract boolean putMessage(IMessage msg) throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#putMessage(org.soyatec.windowsazure.queue.IMessage, int)
	 */
	public abstract boolean putMessage(IMessage msg, int timeToLiveInSeconds)
			throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#getMessage()
	 */
	public abstract IMessage getMessage() throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#getMessage(int)
	 */
	public abstract IMessage getMessage(int visibilityTimeoutInSeconds)
			throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#getMessages(int)
	 */
	public abstract List<Message> getMessages(int numberOfMessages)
			throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#getMessages(int, int)
	 */
	public abstract List<Message> getMessages(int numberOfMessages,
			int visibilityTimeoutInSeconds) throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#peekMessage()
	 */
	public abstract IMessage peekMessage() throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#peekMessages(int)
	 */
	public abstract List<Message> peekMessages(int numberOfMessages)
			throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#deleteMessage(org.soyatec.windowsazure.queue.IMessage)
	 */
	public abstract boolean deleteMessage(IMessage msg) throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#clear()
	 */
	public abstract boolean clear() throws StorageException;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#startReceiving()
	 */
	public abstract boolean startReceiving();

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#stopReceiving()
	 */
	public abstract void stopReceiving();

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#addMessageReceivedListener(org.soyatec.windowsazure.queue.IMessageReceivedListener)
	 */
	public void addMessageReceivedListener(IMessageReceivedListener listener) {
		changeSupport.addListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#removeMessageReceivedListener(org.soyatec.windowsazure.queue.IMessageReceivedListener)
	 */
	public void removeMessageReceivedListener(IMessageReceivedListener listener) {
		changeSupport.removeListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#getAccount()
	 */
	public StorageAccountInfo getAccount() {
		return account;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#isUsePathStyleUris()
	 */
	public boolean isUsePathStyleUris() {
		return usePathStyleUris;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#getQueueUri()
	 */
	public URI getQueueUri() {
		return queueUri;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#setQueueUri(java.net.URI)
	 */
	public void setQueueUri(URI uri) {
		this.queueUri = uri;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#getRetryPolicy()
	 */
	public IRetryPolicy getRetryPolicy() {
		return retryPolicy;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#setRetryPolicy(org.soyatec.windowsazure.blob.IRetryPolicy)
	 */
	public void setRetryPolicy(IRetryPolicy retryPolicy) {
		this.retryPolicy = retryPolicy;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#getTimeout()
	 */
	public TimeSpan getTimeout() {
		return timeout;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#setTimeout(org.soyatec.windowsazure.util.TimeSpan)
	 */
	public void setTimeout(TimeSpan timeout) {
		this.timeout = timeout;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#getPollInterval()
	 */
	public abstract int getPollInterval();

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#setPollInterval(int)
	 */
	public abstract void setPollInterval(int pollInterval);

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.queue.ICloudQueue#getLastStatus()
	 */
	public abstract String getLastStatus();
}

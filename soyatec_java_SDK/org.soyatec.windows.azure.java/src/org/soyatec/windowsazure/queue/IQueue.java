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

import java.net.URI;
import java.util.List;

import org.soyatec.windowsazure.authenticate.StorageAccountInfo;
import org.soyatec.windowsazure.blob.IRetryPolicy;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.internal.util.TimeSpan;
import org.soyatec.windowsazure.queue.internal.Message;

/**
 * 
 * Objects of this class represent a queue in a user's storage account.
 * 
 */
public interface IQueue {

	/**
	 * The default time interval between polling the queue for messages. Polling
	 * is only enabled if the user has called {@link startReceiving()} .
	 */
	public static final int DEFAULT_POLL_INTERVAL = 5000;

	/**
	 * Creates a queue in the specified storage account.
	 * 
	 * @return true if the queue was successfully created.
	 * @throws StorageException
	 *             If queue is exist, a StorageException is throwed.
	 */
	public boolean createQueue() throws StorageException;

	/**
	 * Determines whether a queue with the same name already exists in an
	 * account.
	 * 
	 * @return true if a queue with the same name already exists.
	 */
	public boolean isQueueExist();

	/**
	 * Deletes the queue. The queue will be deleted regardless of whether there
	 * are messages in the queue or not.
	 * 
	 * @return true if the queue was successfully deleted.
	 * @exception StorageException
	 */
	public boolean deleteQueue() throws StorageException;

	/**
	 * Sets the properties of a queue.
	 * 
	 * @param properties
	 *            The queue's properties to set.
	 * @return true if the properties were successfully written to the queue.
	 * @throws StorageException
	 */
	public boolean setProperties(IQueueProperties properties)
			throws StorageException;

	/**
	 * Retrieves the queue's properties.
	 * 
	 * @return The queue's properties.
	 * @throws StorageException
	 */
	public IQueueProperties getProperties() throws StorageException;

	/**
	 * Retrieves the approximate number of messages in a queue.
	 * 
	 * @return The approximate number of messages in this queue.
	 * @throws StorageException
	 */
	public int getApproximateCount() throws StorageException;

	/**
	 * Puts a message in the queue.
	 * 
	 * @param msg
	 *            The message to store in the queue.
	 * @return true if the message has been successfully enqueued.
	 * @throws StorageException
	 */
	public boolean putMessage(IMessage msg) throws StorageException;

	/**
	 * Puts a message in the queue.
	 * 
	 * @param msg
	 *            The message to store in the queue.
	 * @param timeToLiveInSeconds
	 *            The time to live for the message in seconds.
	 * @return true if the message has been successfully enqueued.
	 * @throws StorageException
	 */
	public boolean putMessage(IMessage msg, int timeToLiveInSeconds)
			throws StorageException;

	/**
	 * Retrieves a message from the queue.
	 * 
	 * @return The message retrieved or null if the queue is empty.
	 * @throws StorageException
	 */
	public IMessage getMessage() throws StorageException;

	/**
	 * Retrieves a message and sets its visibility timeout to the specified
	 * number of seconds.
	 * 
	 * @param visibilityTimeoutInSeconds
	 *            Visibility timeout of the message retrieved in seconds.
	 * @return
	 * @throws StorageException
	 */
	public IMessage getMessage(int visibilityTimeoutInSeconds)
			throws StorageException;

	/**
	 * Tries to retrieve the given number of messages.
	 * 
	 * @param numberOfMessages
	 *            Maximum number of messages to retrieve.
	 * @return The list of messages retrieved.
	 * @throws StorageException
	 */
	public List<Message> getMessages(int numberOfMessages)
			throws StorageException;

	/**
	 * Tries to retrieve the given number of messages.
	 * 
	 * @param numberOfMessages
	 *            Maximum number of messages to retrieve.
	 * @param visibilityTimeoutInSeconds
	 *            The visibility timeout of the retrieved messages in seconds.
	 * @return The list of messages retrieved.
	 * @throws StorageException
	 */
	public List<Message> getMessages(int numberOfMessages,
			int visibilityTimeoutInSeconds) throws StorageException;

	/**
	 * Get a message from the queue but do not actually dequeue it. The message
	 * will remain visible for other parties requesting messages.
	 * 
	 * @return The message retrieved or null if there are no messages in the
	 *         queue.
	 * @throws StorageException
	 */
	public IMessage peekMessage() throws StorageException;

	/**
	 * Tries to get a copy of messages in the queue without actually dequeuing
	 * the messages. The messages will remain visible in the queue.
	 * 
	 * @param numberOfMessages
	 *            Maximum number of message to retrieve.
	 * @return The list of messages retrieved.
	 * @throws StorageException
	 */
	public List<Message> peekMessages(int numberOfMessages)
			throws StorageException;

	/**
	 * Deletes a message from the queue.
	 * 
	 * @param msg
	 *            The message to retrieve with a valid popreceipt.
	 * @return true if the operation was successful.
	 * @throws StorageException
	 */
	public boolean deleteMessage(IMessage msg) throws StorageException;

	/**
	 * Delete all messages in a queue.
	 * 
	 * @return true if all messages were deleted successfully.
	 * @throws StorageException
	 */
	public boolean clear() throws StorageException;

	/**
	 * Starts the automatic receiving messages.
	 * 
	 * @return true if the operation was successful.
	 */
	public boolean startReceiving();

	/**
	 * Stop the automatic receiving messages.
	 * 
	 */
	public void stopReceiving();

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when a new message is put into the queue.
	 * 
	 * @param listener
	 *            the listener which should be notified
	 */
	public void addMessageReceivedListener(
			final IMessageReceivedListener listener);

	/**
	 * Removes the listener from the collection of listeners who will when a new
	 * message is put into the queue.
	 * 
	 * @param listener
	 *            the listener which should no longer be notified
	 */
	public void removeMessageReceivedListener(
			final IMessageReceivedListener listener);

	/**
	 * Returns the name of this queue.
	 * 
	 * @return The name of this queue.
	 */
	public String getName();

	/**
	 * Returns the account info object this queue lives in.
	 * 
	 * @return The account info object this queue lives in
	 */
	public StorageAccountInfo getAccount();

	/**
	 * Returns whether to use/generate path-style or host-style URIs.
	 * 
	 * @return whether to use/generate path-style or host-style URIs.
	 */
	public boolean isUsePathStyleUris();

	/**
	 * Return the URI of the queue.
	 * 
	 * @return The URI of the queue
	 */
	public URI getQueueUri();

	/**
	 * Set the URI of the queue
	 * 
	 * @param uri
	 */
	public void setQueueUri(URI uri);

	/**
	 * Returns the retry policy used for retrying requests.
	 * 
	 * @return The retry policy used for retrying requests.
	 */
	public IRetryPolicy getRetryPolicy();

	/**
	 * Specify the retry policy used for retrying requests.
	 * 
	 * @param retryPolicy
	 *            The retry policy used for retrying requests.
	 */
	public void setRetryPolicy(IRetryPolicy retryPolicy);

	/**
	 * Returns the timeout of requests.
	 * 
	 * @return The timeout of requests.
	 */
	public TimeSpan getTimeout();

	/**
	 * Specify the timeout of requests.
	 * 
	 * @param timeout
	 *            The timeout of requests.
	 */
	public void setTimeout(TimeSpan timeout);

	/**
	 * Return the poll interval of checking new messages in milliseconds.
	 * 
	 * @return The poll interval in milliseconds.
	 */
	public int getPollInterval();

	/**
	 * Specify the poll interval of checking new messages in milliseconds.
	 * 
	 * @param pollInterval
	 *            The poll interval in milliseconds.
	 */
	public void setPollInterval(int pollInterval);

	/**
	 * Return the last status of the queue.
	 * @return the last status of the queue.
	 */
	public String getLastStatus();

}
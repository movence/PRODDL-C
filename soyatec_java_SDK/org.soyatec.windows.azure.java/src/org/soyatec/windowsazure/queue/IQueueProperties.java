package org.soyatec.windowsazure.queue;

import org.soyatec.windowsazure.internal.util.NameValueCollection;

/**
 * The properties of a queue.
 */
public interface IQueueProperties {

	/**
	 * Returns the approximated amount of messages in the queue.
	 * 
	 * @return The approximated amount of messages in the queue.
	 */
	public int getApproximateMessageCount();

	/**
	 * Returns metadata for the queue in the form of name-value pairs.
	 * 
	 * @return Metadata for the queue in the form of name-value pairs.
	 */
	public NameValueCollection getMetadata();

	/**
	 * Specify metadata for the queue in the form of name-value pairs.
	 * 
	 * @param metadata
	 *            Metadata for the queue in the form of name-value pairs.
	 */
	public void setMetadata(NameValueCollection metadata);

}
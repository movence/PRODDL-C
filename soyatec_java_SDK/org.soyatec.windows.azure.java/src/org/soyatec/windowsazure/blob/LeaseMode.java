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

/**
 * Represent lease blob operation mode. The Lease Blob operation can be called in one of four modes:
 * Acquire, to request a new lease.
 * Renew, to renew an existing lease.
 * Release, to free the lease if it is no longer needed so that another client may immediately acquire a lease against the blob.
 * Break, to end the lease but ensure that another client cannot acquire a new lease until the current lease period has expired.
 *
 */
public enum LeaseMode {
	Acquire("acquire"), Renew("renew"), Release("release"), Break("break");

	private String literal;

	LeaseMode(final String literal) {
		this.literal = literal;
	}

	/**
	 * Get the literal of the data type
	 *
	 * @return the the literal of the data type string
	 */
	public String getLiteral() {
		return this.literal;
	}
}

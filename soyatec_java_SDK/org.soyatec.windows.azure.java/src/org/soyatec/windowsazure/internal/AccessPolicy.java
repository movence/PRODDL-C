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
package org.soyatec.windowsazure.internal;

import org.soyatec.windowsazure.authenticate.IAccessPolicy;
import org.soyatec.windowsazure.blob.DateTime;

/**
 * @author xiaowei.ye@soyatec.com
 * 
 */
public class AccessPolicy implements IAccessPolicy {

	public AccessPolicy() {

	}

	public AccessPolicy(DateTime start, DateTime expiry, int permission) {
		this.start = start;
		this.expiry = expiry;
		this.permission = permission;
	}

	private int permission;
	private DateTime start;
	private DateTime expiry;

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.authenticate.IAccessPolicy#getPermission()
	 */
	public int getPermission() {
		return permission;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.authenticate.IAccessPolicy#setPermission(int)
	 */
	public void setPermission(int permission) {
		this.permission = permission;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.authenticate.IAccessPolicy#getStart()
	 */
	public DateTime getStart() {
		return start;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.authenticate.IAccessPolicy#setStart(org.soyatec.windowsazure.blob.DateTime)
	 */
	public void setStart(DateTime start) {
		this.start = start;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.authenticate.IAccessPolicy#getExpiry()
	 */
	public DateTime getExpiry() {
		return expiry;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.authenticate.IAccessPolicy#setExpiry(org.soyatec.windowsazure.blob.DateTime)
	 */
	public void setExpiry(DateTime expiry) {
		this.expiry = expiry;
	}
}

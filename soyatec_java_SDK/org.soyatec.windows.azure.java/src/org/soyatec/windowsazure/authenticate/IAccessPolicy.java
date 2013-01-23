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
package org.soyatec.windowsazure.authenticate;

import org.soyatec.windowsazure.blob.DateTime;

/**
 * 
 * Represents the access policy of a signed identifier.
 * 
 * 
 */
public interface IAccessPolicy {

	/**
	 * @return the permission
	 */
	public int getPermission();

	/**
	 * @param permission
	 *            the permission to set
	 */
	public void setPermission(int permission);

	/**
	 * @return the start
	 */
	public DateTime getStart();

	/**
	 * @param start
	 *            the start to set
	 */
	public void setStart(DateTime start);

	/**
	 * @return the expiry
	 */
	public DateTime getExpiry();

	/**
	 * @param expiry
	 *            the expiry to set
	 */
	public void setExpiry(DateTime expiry);

}
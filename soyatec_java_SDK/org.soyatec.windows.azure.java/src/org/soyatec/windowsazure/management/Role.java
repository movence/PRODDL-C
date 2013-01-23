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
package org.soyatec.windowsazure.management;

/**
 * Represents a role.
 */
public class Role {
	private String roleName;
	private String osVersion;

	/**
	 * Construct a new Role object.
	 */
	public Role() {

	}

	/**
	 * Construct a new Role object with a byte roleName, osVersion
	 */
	public Role(String roleName, String osVersion) {

		this.roleName = roleName;
		this.osVersion = osVersion;
	}

	/**
	 * @return the role name of Role.
	 */
	public String getRoleName() {
		return roleName;
	}

	/**
	 * Set the role name of Role.
	 * 
	 * @param roleName
	 */
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	/**
	 * @return the osVersion of Role.
	 */
	public String getOsVersion() {
		return osVersion;
	}

	/**
	 * Set the osVersion of Role.
	 * 
	 * @param osVersion
	 */
	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

}

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
 * The class contains the account keys for accessing the Windows Azure storage
 * services.
 * 
 */
public class StorageAccountKey {
	private String primaryKey;
	private String secondaryKey;

	/**
	 * Construct a new StorageAccountKey object with a primary key and secondary
	 * key.
	 */
	public StorageAccountKey(String primaryKey, String secondaryKey) {

		this.primaryKey = primaryKey;
		this.secondaryKey = secondaryKey;
	}

	/**
	 * Construct a new StorageAccountKey object.
	 */
	public StorageAccountKey() {

	}

	/**
	 * @return the primaryKey of StorageAccountKey.
	 */
	public String getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * Set the primaryKey of StorageAccountKey.
	 * 
	 * @param primaryKey
	 */
	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	/**
	 * @return the secondaryKey of StorageAccountKey.
	 */
	public String getSecondaryKey() {
		return secondaryKey;
	}

	/**
	 * Set the secondaryKey of StorageAccountKey.
	 * 
	 * @param secondaryKey
	 */
	public void setSecondaryKey(String secondaryKey) {
		this.secondaryKey = secondaryKey;
	}

	/**
	 * Get the key of the type.
	 * 
	 * @param type
	 * @return If the type equals to the Primary key type, return primaryKey;
	 *         else if the type equals to the Secondary key type, return
	 *         secondaryKey. else return empty.
	 */
	public String getKey(String type) {
		if (type.equals(KeyType.Primary.getLiteral()))
			return this.primaryKey;
		else if (type.equals(KeyType.Secondary.getLiteral()))
			return this.secondaryKey;
		return "";
	}
}

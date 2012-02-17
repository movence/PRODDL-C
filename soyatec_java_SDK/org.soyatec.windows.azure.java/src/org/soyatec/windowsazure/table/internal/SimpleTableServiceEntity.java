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
package org.soyatec.windowsazure.table.internal;

import java.sql.Timestamp;

import org.soyatec.windowsazure.table.AbstractTableServiceEntity;


/**
 * A direct children for AbstractTableServiceEntity. It does not add any new properties.
 * </p>
 *
 * When query entities from table service, A list of SimpleTableServiceEntity
 * instances is returned if no model class is specified.
 *
 *
 */
public class SimpleTableServiceEntity extends AbstractTableServiceEntity {

	/**
	 * Create a new instance with specified partitionKey and rowKey and the
	 * timestamp
	 *
	 * @param partitionKey
	 * @param rowKey
	 * @param timestamp
	 */
	public SimpleTableServiceEntity(String partitionKey, String rowKey,
			Timestamp timestamp) {
		super(partitionKey, rowKey);
		this.timestamp = timestamp;
	}

	/**
	 * Create a new instance with specified partitionKey and rowKey
	 *
	 * @param partitionKey
	 * @param rowKey
	 */
	public SimpleTableServiceEntity(String partitionKey, String rowKey) {
		super(partitionKey, rowKey);
	}
}

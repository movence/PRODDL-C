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
package org.soyatec.windowsazure.table;

import java.sql.Timestamp;
import java.util.List;

/**
 * This class represents an entity (row) in a table in table storage. </p>
 *
 * Two entries are considered to same when they have same PartitionKey and same
 * RowKey in azure table service. This feature is used when update/merge/load
 * entity from table service.
 *
 */
public interface ITableServiceEntity {

	/**
	 * @return cloudTableColumn list of ITableServiceEntity.
	 */
	public List<ICloudTableColumn> getValues();

	/**
	 * Set the cloudTableColumn list of ITableServiceEntity.
	 * @param values
	 */
	public void setValues(List<ICloudTableColumn> values);

	/**
	 * @return the Timestamp of entity
	 */
	public Timestamp getTimestamp();

	/**
	 * Set the Timestamp of entity.
	 * @param timestamp
	 */
	public void setTimestamp(Timestamp timestamp);

	/**
	 * The partition key of a table entity. The concatenation of the partition
	 * key and row key must be unique per table.
	 */
	public String getPartitionKey();

	/**
	 * Set the partition key of a table entity.
	 * @param partitionKey
	 */
	public void setPartitionKey(String partitionKey);

	/**
	 * The row key of a table entity.
	 */
	public String getRowKey();

	/**
	 * Set the row key of a table entity.
	 * @param rowKey
	 */
	public void setRowKey(String rowKey);

	/**
	 * Etag property</p>
	 *
	 * A property generaged by table service when insert/modify the entity
	 * within table service.
	 *
	 * It should not be setting by users.
	 *
	 */
	public String getETag();

	/**
	 * Different with equals(). Two entieis are considered to same when they
	 * have same PartitionKey and same RowKey in azure table service. This
	 * feature is used when update/merge/load entity from table service.
	 *
	 * @param entity
	 * @return True only if the entity's PartitionKey and RowKey are all same
	 *         with given entity.
	 */
	public boolean isSameEntity(ITableServiceEntity entity);

}
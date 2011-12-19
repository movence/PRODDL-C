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
import java.util.Date;
import java.util.List;


/**
 * This abstract class defines the common concepts of a table in Windows Azure.
 * It represents an entity (row) in a table in table storage. </p>
 *
 * Two entries are considered to same when they have same PartitionKey and same
 * RowKey in azure table service. This feature is used when update/merge/load
 * entity from table service.
 */
public abstract class AbstractTableServiceEntity implements ITableServiceEntity {

	protected Timestamp timestamp;

	/**
	 * The partition key of a table entity. The concatenation of the partition
	 * key and row key must be unique per table.
	 */
	protected String partitionKey;

	/**
	 * The row key of a table entity.
	 */
	protected String rowKey;

	/**
	 * Etag property</p>
	 *
	 * A property generaged by table service when insert/modify the entity
	 * within table service.
	 *
	 * It should not be setting by users.
	 *
	 */
	protected String eTag;

	protected transient List<ICloudTableColumn> values;

	public AbstractTableServiceEntity(String partitionKey, String rowKey) {
		this.timestamp = new Timestamp(new Date().getTime());
		this.partitionKey = partitionKey;
		this.rowKey = rowKey;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ITableServiceEntity#getValues()
	 */
	public List<ICloudTableColumn> getValues() {
		return values;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ITableServiceEntity#setValues(java.util.List)
	 */
	public void setValues(List<ICloudTableColumn> values) {
		this.values = values;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ITableServiceEntity#getTimestamp()
	 */
	public Timestamp getTimestamp() {
		return timestamp;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ITableServiceEntity#setTimestamp(java.sql.Timestamp)
	 */
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ITableServiceEntity#getPartitionKey()
	 */
	public String getPartitionKey() {
		return partitionKey;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ITableServiceEntity#setPartitionKey(java.lang.String)
	 */
	public void setPartitionKey(String partitionKey) {
		this.partitionKey = partitionKey;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ITableServiceEntity#getRowKey()
	 */
	public String getRowKey() {
		return rowKey;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ITableServiceEntity#setRowKey(java.lang.String)
	 */
	public void setRowKey(String rowKey) {
		this.rowKey = rowKey;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ITableServiceEntity#getETag()
	 */
	public String getETag() {
		return eTag;
	}

	public void setETag(String tag) {
		eTag = tag;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eTag == null) ? 0 : eTag.hashCode());
		result = prime * result
				+ ((partitionKey == null) ? 0 : partitionKey.hashCode());
		result = prime * result + ((rowKey == null) ? 0 : rowKey.hashCode());
		result = prime * result
				+ ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ITableServiceEntity))
			return false;
		ITableServiceEntity other = (ITableServiceEntity) obj;
		if (eTag == null) {
			if (other.getETag() != null)
				return false;
		} else if (!eTag.equals(other.getETag()))
			return false;
		if (partitionKey == null) {
			if (other.getPartitionKey() != null)
				return false;
		} else if (!partitionKey.equals(other.getPartitionKey()))
			return false;
		if (rowKey == null) {
			if (other.getRowKey() != null)
				return false;
		} else if (!rowKey.equals(other.getRowKey()))
			return false;
		if (timestamp == null) {
			if (other.getTimestamp() != null)
				return false;
		} else if (!timestamp.equals(other.getTimestamp()))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.table.ITableServiceEntity#sameEntity(org.soyatec.windowsazure.table.ITableServiceEntity)
	 */
	public boolean isSameEntity(ITableServiceEntity entity) {
		if (entity == null) {
			return false;
		}

		if (this.equals(entity)) {
			return true;
		}

		if (getPartitionKey() == null || getRowKey() == null)
			return false;
		if (entity.getPartitionKey() == null || entity.getRowKey() == null)
			return false;

		return getPartitionKey().equals(entity.getPartitionKey())
				&& getRowKey().equals(entity.getRowKey());
	}

}

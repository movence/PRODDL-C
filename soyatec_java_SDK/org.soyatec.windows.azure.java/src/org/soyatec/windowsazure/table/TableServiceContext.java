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

import java.util.List;

import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.table.internal.CloudTableQuery;
import org.soyatec.windowsazure.table.internal.CloudTableRest;
import org.soyatec.windowsazure.table.internal.SimpleTableServiceEntity;

/**
 *
 * This class provide API to manipulate ITableServiceEntity, such as insert, update, delete etc.
 *
 * <h5>Batch operations</h5>
 *
 * The {@link TableServiceContext} implements the {@link IBatchExecutor}. Some TRANSCATION
 * supported operations can be enlist a single batch operation. Example:
 *
 * <pre>
 *      SampleEntity sampleEntity1 = createSampleEntity();
 * 		SampleEntity sampleEntity2 = createSampleEntity();
 * 		try {
 * 			tableServiceContext.startBatch();
 * 			tableServiceContext.insertEntity(sampleEntity1);
 * 			tableServiceContext.insertEntity(sampleEntity2);
 * 			tableServiceContext.executeBatch();
 * 	    catch(Exception e){
 * 	    	//
 * 	    }
 * </pre>
 *
 * For more detail about <strong>Batch operation</strong>, see
 * {@link IBatchExecutor}.
 *
 */
public class TableServiceContext implements IBatchExecutor {

	private CloudTableRest table;

	public TableServiceContext(ITable table) {
		this.table = (CloudTableRest) table;
	}

	/**
	 * Deletes an entity within a table </p>
	 *
	 * When an entity is successfully deleted, the entity is immediately marked
	 * for deletion and is no longer accessible to clients. The entity is later
	 * removed from the Table service during garbage collection.
	 *
	 * @param obj
	 *            the object to be deleted
	 * @throws StorageException
	 *             If entity is not exists, an exception is also thrown.
	 */
	public void deleteEntity(ITableServiceEntity obj)
			throws StorageException{
		table.deleteEntity(obj);
	}

	/**
	 * Deletes a table entity if the entity is not modified after it is loaded
	 * from azure table storage. ITableServiceEntity should have etag value which
	 * is retrieved from table storage.
	 *
	 * @param obj
	 *            the object to be deleted
	 * @throws StorageException
	 * @see {@link #deleteEntity(AbstractTableServiceEntity)}
	 */
	public void deleteEntityIfNotModified(ITableServiceEntity obj)
			throws StorageException{
		table.deleteEntityIfNotModified(obj);
	}

	/**
	 * Load the entity within table by the entity identifier(PartitionKey and
	 * RowKey).
	 *
	 * @param <T>
	 *            {@link AbstractTableServiceEntity} instance of subclass of
	 *            {@link AbstractTableServiceEntity} instance
	 * @param entity
	 *            Specified Entity
	 *
	 * @return Entity within table
	 * @throws StorageException
	 */
	public <T extends ITableServiceEntity> T loadEntity(T entity)
			throws StorageException{
		return table.loadEntity(entity);
	}

	/**
	 * This operation queries entities in a table and set the model class of the table.. A query against a table
	 * returns a list of entities conforming to the criteria specified in the
	 * query.
	 *
	 *
	 * @param queryExpression
	 *            If queryExpression is not given, all rows are return.
	 * @param modelClass
	 *            If modelClass is not null, set the given class to the table's model class.
	 * @return A list of ITableServiceEntity
	 * @throws StorageException
	 */
	public List<ITableServiceEntity> retrieveEntities(
			String queryExpression, Class<? extends ITableServiceEntity> modelClass) throws StorageException{
		return table.retrieveEntities(queryExpression, modelClass);
	}

	/**
	 * This operation queries entities in a table and set the model class of the table.. A query against a table
	 * returns a list of entities conforming to the criteria specified in the
	 * query.
	 *
	 * @param query
	 * 			A cloudTableQuery
	 * @param modelClass
	 *            If modelClass is not null, set the given class to the table's model class.
	 * @return A list of ITableServiceEntity
	 * @throws StorageException
	 */
	public List<ITableServiceEntity> retrieveEntities(
			final CloudTableQuery query, Class<? extends ITableServiceEntity> modelClass) throws StorageException{
		return table.retrieveEntities(query, modelClass);
	}

	/**
	 * This operation queries entities in a table and set the model class of the table. A query against a table
	 * returns a list of entities conforming to the criteria specified in the
	 * query.
	 *
	 * @param modelClass
	 *            If modelClass is not null, set the given class to the table's model class.
	 *
	 * @return A list of ITableServiceEntity
	 * @throws StorageException
	 */
	public List<ITableServiceEntity> retrieveEntities(Class<? extends ITableServiceEntity> modelClass)
			throws StorageException{
		return table.retrieveEntities(modelClass);
	}

	/**
	 *
	 * @param partitionKey
	 * @param rowKey
	 * @param modelClass
	 *            If modelClass is not null, set the given class to the table's model class.
	 * @return A list of entities who's partition key and row key are all euqals
	 *         with the given.
	 * @throws StorageException
	 */
	public List<ITableServiceEntity> retrieveEntitiesByKey(
			String partitionKey, String rowKey, Class<? extends ITableServiceEntity> modelClass) throws StorageException{
		return table.retrieveEntitiesByKey(partitionKey, rowKey, modelClass);
	}


	/**
	 * Inserts a new entity into a table. </p>
	 *
	 * When inserting an entity into a table, you must specify values for the
	 * <strong>PartitionKey</strong> and <strong>RowKey</strong> system
	 * properties. Together, these properties form the primary key and must be
	 * unique within the table. </p>
	 *
	 * Both the <strong>PartitionKey</strong> and <strong>RowKey</strong> values
	 * must be string values; each key value may be up to 64 KB in size. If you
	 * are using an integer value for the key value, you should convert the
	 * integer to a fixed-width string, because they are canonically sorted. For
	 * example, you should convert the value 1 to 0000001 to ensure proper
	 * sorting.</p>
	 *
	 * Reference <a
	 * href="http://msdn.microsoft.com/en-us/library/dd179433.aspx"> Insert
	 * entity </a>
	 *
	 * @param obj
	 *            The object to be inserted. The entity shoule be instance of
	 *            {@link AbstractTableServiceEntity} or subclass of
	 *            {@link AbstractTableServiceEntity}
	 * @throws StorageException
	 */
	public void insertEntity(ITableServiceEntity obj)
			throws StorageException{
		table.insertEntity(obj);
	}

	/**
	 * Updates an existing entity within a table by replacing it.
	 *
	 * When updating an entity, you must specify the
	 * <strong>PartitionKey</strong> and <strong>RowKey</strong> system
	 * properties as part of the update operation. </p>
	 *
	 * An entity's ETag provides default optimistic concurrency for update
	 * operations. The ETag value is opaque and should not be read or relied
	 * upon. Before an update operation occurs, the Table service verifies that
	 * the entity's current ETag value is identical to the ETag value included
	 * with the update request. If the values are identical, the Table service
	 * determines that the entity has not been modified since it was retrieved,
	 * and the update operation proceeds. </p>
	 *
	 * If the entity's ETag differs from that specified with the update request,
	 * the update operation fails with status code 412 (Precondition Failed).
	 * This error indicates that the entity has been changed on the server since
	 * it was retrieved. To resolve this error, retrieve the entity again and
	 * reissue the request. </p>
	 *
	 * To force an unconditional update operation, set the value of the If-Match
	 * header to the wildcard character (*) on the request. Passing this value
	 * to the operation will override the default optimistic concurrency and
	 * ignore any mismatch in ETag values. </p>
	 *
	 * If the If-Match header is missing from the request, the service returns
	 * status code 400 (Bad Request). A request malformed in other ways may also
	 * return 400; see Table Service Error Codes for more information. </p>
	 *
	 * If the request specifies a property with a null value, that property is
	 * ignored, the update proceeds, and the existing entity is replaced. </p>
	 *
	 * Reference <a
	 * href="http://msdn.microsoft.com/en-us/library/dd179427.aspx"> Update
	 * entity </a>
	 *
	 * @param obj
	 *            the object to be updated
	 * @throws StorageException
	 *
	 */
	public void updateEntity(ITableServiceEntity obj)
			throws StorageException{
		table.updateEntity(obj);
	}

	/**
	 * Updates table entity if the entity is not modified after it is loaded
	 * from azure table storage. TableStorageEntity should have etag value which
	 * is retrieved from table storage.
	 *
	 *
	 * @param obj
	 *            the object to be updated
	 * @throws StorageException
	 *
	 * @see {@link #updateEntity(AbstractTableServiceEntity)}
	 */
	public void updateEntityIfNotModified(ITableServiceEntity obj)
			throws StorageException{
		table.updateEntityIfNotModified(obj);
	}

	/**
	 * Default, azure table entities are retrieved as the instances of class
	 * {@link SimpleTableServiceEntity}. A list of
	 * {@link SimpleTableServiceEntity} is return when retrieve entities from
	 * table service.</p>
	 *
	 * <strong>Model</strong> class can be changed to specified table service to
	 * represent more <strong>Model</strong> details. </p>
	 *
	 * <strong>Model</strong> class must be subclass of
	 * {@link AbstractTableServiceEntity}.
	 *
	 * Get the model class for this table.
	 *
	 * @return {@link #modelClass}
	 */
	public Class<? extends ITableServiceEntity> getModelClass(){
		return table.getModelClass();
	}

	/**
	 * Set model class for this table.
	 *
	 * @param modelClass
	 * @see {@link #modelClass}
	 */
	public void setModelClass(
			Class<? extends ITableServiceEntity> modelClass){
		table.setModelClass(modelClass);
	}


	/**
	 * Merges table entity </p>
	 *
	 * Updates an existing entity within a table by merging new property values
	 * into the entity.
	 *
	 * <h3>Remark</h3>
	 *
	 * Any properties with null values are ignored by the <strong>Merge
	 * Entity</strong> operation. All other properties will be updated. </p>
	 *
	 * A property cannot be removed with a Merge Entity operation. To remove a
	 * property from an entity, replace the entity by calling the Update Entity
	 * operation.</p>
	 *
	 * When merging an entity, you must specify the PartitionKey and RowKey
	 * system properties as part of the merge operation.</p>
	 *
	 * An entity's ETag provides default optimistic concurrency for merge
	 * operations. The ETag value is opaque and should not be read or relied
	 * upon. Before a merge operation occurs, the Table service verifies that
	 * the entity's current ETag value is identical to the ETag value included
	 * with the request. If the values are identical, the Table service
	 * determines that the entity has not been modified since it was retrieved,
	 * and the merge operation proceeds.</p>
	 *
	 * If the entity's ETag differs from that specified with the merge request,
	 * the merge operation fails with status code 412 (Precondition Failed).
	 * This error indicates that the entity has been changed on the server since
	 * it was retrieved. To resolve this error, retrieve the entity again and
	 * reissue the request.</p>
	 *
	 * To force an unconditional merge operation, set the value of the If-Match
	 * header to the wildcard character (*) on the request. Passing this value
	 * to the operation will override the default optimistic concurrency and
	 * ignore any mismatch in ETag values.</p>
	 *
	 * Reference <a
	 * href="http://msdn.microsoft.com/en-us/library/dd179392.aspx">Merge entity
	 * </a>
	 *
	 * @param obj
	 *            the object to be merged
	 * @throws StorageException
	 */
	public void mergeEntity(ITableServiceEntity obj)
			throws StorageException{
		table.mergeEntity(obj);
	}


	/**
	 * Starts a new batch operation set. Don't make call to service.
	 */
	public void startBatch(){
		table.startBatch();
	}

	/**
	 * Cleanup current batch
	 */
	public void clearBatch(){
		table.clearBatch();
	}

	/**
	 * Commit current batch. Make call to table service. Whether the batch
	 * operation is success or fail, current batch will be cleaned.
	 */
	public void executeBatch(){
		table.executeBatch();
	}

	/**
	 * This operation indicates whether there is batch.
	 *
	 * @return
	 */
	public boolean isInBatch(){
		return table.isInBatch();
	}
}

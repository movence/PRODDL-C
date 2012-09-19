/*
 * Copyright J. Craig Venter Institute, 2011
 *
 * The creation of this program was supported by the U.S. National
 * Science Foundation grant 1048199 and the Microsoft allocation
 * in the MS Azure cloud.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pdl.cloud.storage;

import org.soyatec.windowsazure.blob.IRetryPolicy;
import org.soyatec.windowsazure.blob.internal.RetryPolicies;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.internal.util.TimeSpan;
import org.soyatec.windowsazure.table.*;
import org.soyatec.windowsazure.table.internal.CloudTableQuery;
import pdl.common.Configuration;
import pdl.common.StaticValues;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 11/8/11
 * Time: 8:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class TableOperator {
    private Configuration conf;

    public TableStorageClient tableStorageClient;

    public TableOperator() {
        try {
            conf = Configuration.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TableOperator(Configuration conf) {
        this.conf = conf;
    }

    private void initTableClient() {
        try {
            tableStorageClient = TableStorageClient.create(
                    URI.create(StaticValues.AZURE_TABLE_HOST_NAME),
                    false,
                    conf.getStringProperty(StaticValues.CONFIG_KEY_CSTORAGE_NAME),
                    conf.getStringProperty(StaticValues.CONFIG_KEY_CSTORAGE_PKEY)
            );

            tableStorageClient.setRetryPolicy(RetryPolicies.retryN(3, TimeSpan.fromSeconds(3)));
            /*IRetryPolicy retryPolicy = new TableRetryPolicy();
            tableStorageClient.setRetryPolicy(retryPolicy);*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ITable createTable(String tableName) {
        ITable table = null;
        try {
            if (tableStorageClient == null)
                initTableClient();

            table = tableStorageClient.getTableReference(tableName);
            if (null == table) {
                throw new NullPointerException(String.format("TableStorageClient returned null ITable '%s'.", tableName));
            }
            if (!table.isTableExist()) {
                table.createTable();
                if (!table.isTableExist())
                    throw new Exception("Table - " + tableName + " is not created.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return table;
    }

    public boolean insertSingleEntity(String tableName, ITableServiceEntity entity) {
        boolean rtnVal = false;
        try {

            if (tableStorageClient == null)
                initTableClient();

            ITable table = tableStorageClient.getTableReference(tableName);
            if (!table.isTableExist())
                table = createTable(tableName);

            table.getTableServiceContext().insertEntity(entity);
            rtnVal = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnVal;
    }

    public boolean insertMultipleEntities(String tableName, List<ITableServiceEntity> entityList) {
        boolean rtnVal = false;
        try {

            if (tableStorageClient == null)
                initTableClient();

            ITable table = tableStorageClient.getTableReference(tableName);
            if (!table.isTableExist())
                table = createTable(tableName);

            TableServiceContext context = table.getTableServiceContext();
            context.setModelClass(entityList.get(0).getClass());
            context.startBatch();

            for (ITableServiceEntity entity : entityList)
                context.updateEntity(entity);

            context.executeBatch();

            rtnVal = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnVal;
    }

    public AbstractTableServiceEntity queryEntityBySearchKey(
            String tableName, String searchColumn,
            Object searchKey, Class model) {
        AbstractTableServiceEntity entity = null;

        try {
            List<ITableServiceEntity> entityList = this.queryListBySearchKey(tableName, searchColumn, searchKey, null, null, model);

            if (entityList != null && entityList.size() > 0) {
                /*if( entityList.size() > 1 )
                    throw new Exception(
                            String.format(
                                    "More than 1 record has been found for %s(%s) in %s" ,
                                    searchKey, searchColumn, tableName )
                    );*/

                entity = (AbstractTableServiceEntity) entityList.get(0);
            }
        } catch (StorageException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return entity;
    }

    public List<ITableServiceEntity> queryListBySearchKey(
            String tableName, String searchColumn, Object searchKey,
            String order, String orderColumn, Class model) throws StorageException, Exception {
        List<ITableServiceEntity> entityList = null;
        ITable table;

        try {
            if (tableStorageClient == null)
                initTableClient();

            CloudTableQuery sql = CloudTableQuery.select();
            if (searchColumn != null && searchKey != null)
                sql.eq(searchColumn, searchKey);
            /*if( order != null && orderColumn != null ) {
                if( order.equals( "asc" ) )
                    sql.orderAsc( orderColumn );
                else if( order.equals( "desc" ) )
                    sql.orderDesc( orderColumn );
            }*/

            table = tableStorageClient.getTableReference(tableName);

            if (table.isTableExist())
                entityList = table.getTableServiceContext().retrieveEntities(sql.toAzureQuery(), model);

        } catch (StorageException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ex;
        }

        return entityList;
    }

    public AbstractTableServiceEntity queryEntityByCondition(
            String tableName, String condition, Class model) {
        AbstractTableServiceEntity entity = null;
        List<ITableServiceEntity> entityList;
        try {
            entityList = this.queryListByCondition(tableName, condition, model);

            if (entityList != null && entityList.size() >= 1)
                entity = (AbstractTableServiceEntity) entityList.get(0);

        } catch (StorageException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return entity;
    }

    public List<ITableServiceEntity> queryListByCondition(String tableName, String condition, Class model) {
        List<ITableServiceEntity> entityList = null;
        ITable table;

        try {
            if (tableStorageClient == null)
                initTableClient();

            CloudTableQuery sql = CloudTableQuery.select();
            sql.where(condition);

            table = tableStorageClient.getTableReference(tableName);

            if (table.isTableExist())
                entityList = table.getTableServiceContext().retrieveEntities(sql.toAzureQuery(), model);

        } catch (StorageException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return entityList;
    }

    public void deleteEntity(String tableName, ITableServiceEntity entity) {
        ITable table;

        try {
            if(tableStorageClient == null)
                initTableClient();

            table = tableStorageClient.getTableReference(tableName);

            if (table != null && table.isTableExist())
                table.getTableServiceContext().deleteEntity(entity);

        } catch (StorageException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean updateSingleEntity(String tableName, ITableServiceEntity entity) {
        boolean rtnVal = false;

        try {
            if (tableStorageClient == null)
                initTableClient();

            ITable table = tableStorageClient.getTableReference(tableName);
            if (!table.isTableExist())
                throw new Exception("updateEntity - Table does not exist: " + tableName);

            entity.setValues(null);
            table.getTableServiceContext().updateEntity(entity);

            rtnVal = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rtnVal;
    }

    public <B extends ITableServiceEntity> void updateMultipleEntities(String tableName, List<B> entityList) {
        try {
            if (tableStorageClient == null)
                initTableClient();

            ITable table = tableStorageClient.getTableReference(tableName);
            if (!table.isTableExist())
                throw new Exception("updateEntity - Table does not exist: " + tableName);

            TableServiceContext context = table.getTableServiceContext();
            context.setModelClass(entityList.get(0).getClass());
            context.startBatch();

            for (B entity : entityList) {
                entity.setValues(null);
                context.updateEntity(entity);
            }

            context.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Custom retry policy for azure table client
     */
    private class TableRetryPolicy implements IRetryPolicy {
        private int numberOfTriesLeft;
        private long timeToWait = 2000;

        @Override
        public Object execute(Callable callable) throws StorageException {
            numberOfTriesLeft = 5;
            while (true) {
                try {
                    System.err.println("Trying TableStorage process again after failure.");
                    return callable.call();
                }
                catch (Exception e) {
                    numberOfTriesLeft--;
                    if (numberOfTriesLeft == 0) {
                        throw new StorageException("Ran out of retry attempts!!");
                    }
                    try {
                        Thread.sleep(timeToWait);
                    } catch(Exception ex) {
                        throw new StorageException("Exception in TableRetryPolicy!");
                    }
                }
            }
        }
    }
}



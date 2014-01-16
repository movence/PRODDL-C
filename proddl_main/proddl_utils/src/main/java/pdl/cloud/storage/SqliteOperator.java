/*
 * Copyright J. Craig Venter Institute, 2014
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package pdl.cloud.storage;

import org.sqlite.SQLiteConfig;
import pdl.cloud.model.AbstractModel;
import pdl.utils.Configuration;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: hkim
 * Date: 1/14/14
 * Time: 3:46 PM
 * pdl.cloud.storage
 */
public class SqliteOperator {

    private Connection connection;
    private String dbFilePath;
    private boolean isOpened = false;

    private final static String QUERY_SELECT_BY_NAME = "SELECT * FROM media WHERE FilePath=?;";
    private final static String QUERY_SELECT_BY_NAME_HASHCODE = "SELECT * FROM media WHERE FilePath=? AND CheckSum=?;";
    private final static String QUERY_SELECT_THUMBNAIL = "SELECT Thumbnail FROM media WHERE FilePath=?;";

    public final static String DATABASE = "proddlc.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public SqliteOperator(String databaseFileName) {
        Configuration configuration = Configuration.getInstance();
        String storagePath = configuration.getStringProperty("storage_path");

        this.dbFilePath = storagePath + databaseFileName;
    }

    public SqliteOperator() {
        this(DATABASE);
    }

    public boolean open(boolean isReadOnly) {
        try {
            SQLiteConfig config = new SQLiteConfig();

            if(isReadOnly) {
                config.setReadOnly(true);
            }
            this.connection = DriverManager.getConnection("jdbc:sqlite:/" + this.dbFilePath, config.toProperties());
            this.connection.setAutoCommit(false);
            isOpened = true;
        } catch(SQLException e) {
            e.printStackTrace();
            isOpened = false;
        }
        return isOpened;
    }

    public boolean close() {
        if(!this.isOpened) {
            return true;
        }

        try {
            this.connection.close();
            this.isOpened = false;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void commit() {
        try {
            this.connection.commit();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void rollback() {
        try {
            this.connection.rollback();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, String>> query(String tableName, String sql) {
        List<Map<String, String>> results = new ArrayList<Map<String, String>>();
        Statement statement = null;
        ResultSet rs = null;

        if(!this.isOpened) {
            this.open(true);
        }

        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("SELECT * FROM " + tableName + (sql == null || sql.isEmpty()? "" : sql));
            if(rs.next()) {

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                do {
                    Map<String, String> row = new HashMap<String, String>(columnCount);
                    for(int i=1;i<=columnCount;i++) {
                        row.put(metaData.getColumnName(i), rs.getString(i));
                    }
                    results.add(row);
                } while(rs.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if(rs != null) {
                    rs.close();
                }
                if(statement != null) {
                    statement.close();
                }
                this.commit();
                this.close();
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }

        return results;
    }

    public boolean update(String sql, boolean isTable) {
        List<String> sqls = new ArrayList<String>(1);
        sqls.add(sql);
        return this.update(sqls, isTable);
    }

    public boolean update(List<String> sqls, boolean isTable) {
        boolean result = true;
        Statement statement = null;
        int executeResult = isTable ? 0 : 1;

        if(!this.isOpened) {
            this.open(false);
        }

        try {
            statement = connection.createStatement();
            statement.setQueryTimeout(30);
            for(String sql : sqls) {
                System.err.println(sql);
                statement.addBatch(sql);
            }
            int[] batchResults = statement.executeBatch();
            for(int batchResult : batchResults) {
                result = result && batchResult == executeResult;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            this.rollback();
            result = false;
        } finally {
            try {
                if(statement != null) {
                    statement.close();
                }
                this.commit();
                this.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public boolean createTable(String tableName, String columns) {
        StringBuilder createSql = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(tableName);
        createSql.append(" (");
        createSql.append(columns);
        createSql.append(")");
        return this.update(createSql.toString(), true);
    }

    public boolean deleteTable(String tableName) {
        return this.update("DROP TABLE IF EXISTS " + tableName, true);
    }

    public <M extends AbstractModel> boolean insertEntity(String tableName, M entity) {
        List<M> entities = new ArrayList<M>(1);
        entities.add(entity);
        return this.insertMultipleEntities(tableName, entities);
    }

    public <M extends AbstractModel> boolean insertMultipleEntities(String tableName, List<M> rows) {
        List<String> sqls = new ArrayList<String>(rows.size());
        String sql = "INSERT INTO " + tableName + " ";

        for(M entity : rows) {
            sqls.add(sql + entity.getInsertSql());
        }

        return this.update(sqls, false);
    }

    public <M extends AbstractModel> boolean updateEntity(String tableName, M entity) {
        List<M> entities = new ArrayList<M>(1);
        entities.add(entity);
        return this.updateMultipleEntities(tableName, entities);
    }

    public <M extends AbstractModel> boolean updateMultipleEntities(String tableName, List<M> rows) {
        List<String> sqls = new ArrayList<String>(rows.size());
        String sql = "UPDATE " + tableName + " SET ";

        for(M entity : rows) {
            sqls.add(sql + entity.getUpdateSql());
        }

        return this.update(sqls, false);
    }

    public <M extends AbstractModel> boolean deleteEntity(String tableName, M entity) {
        List<M> entities = new ArrayList<M>(1);
        entities.add(entity);
        return this.deleteMultipleEntities(tableName, entities);
    }

    public <M extends AbstractModel> boolean deleteMultipleEntities(String tableName, List<M> rows) {
        List<String> sqls = new ArrayList<String>(rows.size());
        String sql = "DELETE FROM " + tableName + " WHERE uuid= ";

        for(M entity : rows) {
            sqls.add(sql + "'" + entity.getUuid() + "'");
        }

        return this.update(sqls, false);
    }
}

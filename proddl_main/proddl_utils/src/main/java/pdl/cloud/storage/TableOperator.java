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
import pdl.utils.StaticValues;

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
public class TableOperator {
    private static TableOperator tableOperator;

    private Connection connection;
    private String dbFilePath;
    private boolean isOpened = false;

    private Connection readConnection;
    private boolean isReadOpen = false;
    private Connection writeConnection;
    private boolean isWriteOpen = false;

    public final static String DATABASE = "proddlc.db";

    public static synchronized TableOperator getInstance(String path) {
        if(tableOperator == null) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            if(path == null) {
                tableOperator = new TableOperator();
            } else {
                tableOperator = new TableOperator(path);
            }
        }
        return tableOperator;
    }

    /*static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }*/

    public TableOperator() {
        this(Configuration.getInstance().getStringProperty(StaticValues.CONFIG_KEY_STORAGE_PATH));
    }

    public TableOperator(Configuration conf) {
        this(conf.getStringProperty(StaticValues.CONFIG_KEY_STORAGE_PATH));
    }

    public TableOperator(String storagePath) {
        this.dbFilePath = storagePath + DATABASE;
    }

    public boolean open(boolean isReadOnly) {
        try {
            SQLiteConfig config = new SQLiteConfig();

            if(isReadOnly) {
                config.setReadOnly(true);
                readConnection = DriverManager.getConnection("jdbc:sqlite:/" + this.dbFilePath, config.toProperties());
                isReadOpen = true;
            } else {
                writeConnection = DriverManager.getConnection("jdbc:sqlite:/" + this.dbFilePath, config.toProperties());
                isWriteOpen = true;
            }
            /*connection = DriverManager.getConnection("jdbc:sqlite:/" + this.dbFilePath, config.toProperties());
            isOpened = true;*/
        } catch(SQLException e) {
            e.printStackTrace();
            isOpened = false;
        }
        return isOpened;
    }

    public boolean close() {
        if(!isOpened) {
            return true;
        }

        try {
            connection.close();
            isOpened = false;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void commit() {
        try {
            connection.commit();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void rollback() {
        try {
            writeConnection.rollback();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean tableExists(String tableName) {
        boolean exists = false;

        List<Map<String, String>> result = this.query("sqlite_master", "type='table' and name='" + tableName + "'");
        if(result != null && result.size() > 0) {
            Map<String, String> row = result.get(0);
            if(tableName.equals(row.get("name"))) {
                exists = true;
            }
        }
        return exists;
    }

    public List<Map<String, String>> query(String tableName, String where) {
        List<Map<String, String>> results = new ArrayList<Map<String, String>>();
        Statement statement = null;
        ResultSet rs = null;

        if(!this.isReadOpen) {
            this.open(true);
        }

        try {
            statement = readConnection.createStatement();
            StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName).append(" ");
            if(!where.startsWith("WHERE")) {
                sql.append("WHERE ");
            }
            sql.append(where == null || where.isEmpty()? "" : where);
            rs = statement.executeQuery(sql.toString());
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
                //this.close();
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }

        return results;
    }

    public Map<String, String> queryEntity(String tableName, String where) {
        Map<String, String> entity = null;

        List<Map<String, String>> list = this.queryList(tableName, where);
        if(list != null && list.size() > 0) {
            entity = list.get(0);
        }

        return entity;
    }

    public List<Map<String, String>> queryList(String tableName, String where) {
        List<Map<String, String>> list = null;

        if(this.tableExists(tableName)) {
            list = this.query(tableName, where);
        }

        return list;
    }

    public List<Map<String, String>> queryListBySearchKey(String tableName, String searchColumn, Object value) {
        List<Map<String, String>> list = null;

        if(this.tableExists(tableName)) {
            StringBuilder where = new StringBuilder("WHERE ");
            where.append(searchColumn).append("=");

            if(value.getClass() == java.lang.String.class) {
                where.append("'").append(value).append("'");
            } else {
                where.append(value);
            }
            list = this.query(tableName, where.toString());
        }
        return list;
    }

    public Map<String, String> queryEntityBySearchKey(String tableName, String searchColumn, Object value) {
        Map<String, String> entity = null;
        List<Map<String, String>> list = this.queryListBySearchKey(tableName, searchColumn, value);
        if(list != null && list.size() > 0) {
            entity = list.get(0);
        }
        return entity;
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

        if(!this.isWriteOpen) {
            this.open(false);
        }

        try {
            writeConnection.setAutoCommit(false);

            statement = writeConnection.createStatement();
            statement.setQueryTimeout(30);
            for(String sql : sqls) {
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
                writeConnection.commit();
                //this.close();
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
        boolean result = false;

        if(rows != null && rows.size() > 0) { //run only when there is data
            //create table if not exist with top entity
            if(!this.tableExists(tableName)) {
                M top = rows.get(0);
                this.createTable(tableName, top.generate("create"));
            }

            //batch update
            List<String> sqls = new ArrayList<String>(rows.size());
            String sql = "INSERT INTO " + tableName + " ";
            for(M entity : rows) {
                sqls.add(sql + entity.generate("insert"));
            }
            result = this.update(sqls, false);
        }
        return result;
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
            sqls.add(sql + entity.generate("update"));
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

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

package pdl.cloud;


import pdl.cloud.model.AbstractModel;
import pdl.cloud.storage.TableOperator;
import pdl.utils.StaticValues;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 2/13/12
 * Time: 3:16 PM
 */
public class StorageServices {
    private TableOperator tableOperator;

    public StorageServices() {}

    /*
     *TABLE OPERATIONS
     */
    private TableOperator getTableOperator() {
        if (tableOperator == null) {
            tableOperator = TableOperator.getInstance(null);
        }
        return tableOperator;
    }

    public boolean insertSingleEntity(String tableName, AbstractModel entity) {
        return this.getTableOperator().insertEntity(tableName, entity);
    }

    public String getFileNameById(String fileId) {
        Map<String, String> entity = getTableOperator().queryEntityBySearchKey(
                StaticValues.TABLE_NAME_FILES, StaticValues.COLUMN_ROW_KEY, fileId
        );
        return entity == null ? null : entity.get("name");
    }

    public Map<String, String> queryEntityBySearchKey(String tableName, String column, String value) {
        return this.getTableOperator().queryEntityBySearchKey(tableName, column, value);
    }

    public boolean updateEntity(String tableName, AbstractModel entity) {
        return getTableOperator().updateEntity(tableName, entity);
    }

    public void deleteEntity(String tableName, AbstractModel entity) {
        getTableOperator().deleteEntity(tableName, entity);
    }
}

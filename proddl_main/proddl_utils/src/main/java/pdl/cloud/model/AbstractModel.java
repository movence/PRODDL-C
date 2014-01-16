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

package pdl.cloud.model;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * User: hkim
 * Date: 1/15/14
 * Time: 11:07 AM
 * pdl.cloud.model
 */
public abstract class AbstractModel {
    String uuid;

    Long createTime;
    Long updateTime;

    public AbstractModel() {
        this(UUID.randomUUID().toString());
    }

    public AbstractModel(String uuid) {
        this.createTime = new Date().getTime();
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    abstract void setValues(Map<String, String> map);
    abstract String getValuesWithComma();
    abstract String getValuesWithEqual();

    public String getInsertSql() {
        String fields = this.fields(false);
        String values = " values ";

        return "(" + fields + ")" + values + "(" + getValuesWithComma() + ")";
    }

    public String getUpdateSql() {
        StringBuilder sb = new StringBuilder(this.getValuesWithEqual());
        sb.append(",updateTime='" + new Date().getTime() + "'");
        sb.append(" WHERE uuid='" + this.getUuid() + "'");
        return sb.toString();
    }

    public String fields(boolean withType) {
        //manually add fields from this abstract class since getDeclaredFields() only returns fields of current class
        StringBuilder fields = new StringBuilder("uuid");
        if(withType) {
            fields.append(" string primary key not null, createTime date, updateTime date");
        } else {
            fields.append(", createTime, updateTime");
        }
        for(Field field : this.getClass().getDeclaredFields()) {
            fields.append(", ");
            fields.append(field.getName());

            if(withType) {
                String type = "string";
                fields.append(" ").append(type);
            }
        }
        return fields.toString();
    }
}

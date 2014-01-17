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

import java.lang.reflect.Method;
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

    public void map(Map<String, String> map) {
        Class obj = this.getClass();
        for(Method method : obj.getMethods()) {
            String methodName = method.getName();
            if(methodName.startsWith("set")) {
                String field = methodName.substring(3);
                field = field.substring(0, 1).toLowerCase() + field.substring(1);
                if(map.containsKey(field)) {
                    String type = (method.getParameterTypes())[0].toString();
                    type = type.substring(type.lastIndexOf(".") + 1);

                    Object objectValue = null;
                    String stringValue = map.get(field);
                    if(stringValue != null && !stringValue.equals("null")) {
                        if(type.equals("String")) {
                            objectValue = "" + stringValue;
                        } else if(type.equals("Long")) {
                            objectValue = Long.valueOf(stringValue);
                        } else {
                            objectValue = Integer.valueOf(stringValue);
                        }

                        try {
                            method.invoke(this, objectValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public String generate(String which) {
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        boolean isCreate = which.equals("create");
        boolean isInsert = which.equals("insert");

        Class obj = this.getClass();
        Method[] methods = obj.getMethods();
        for(Method method : methods) {
            String methodName = method.getName();
            if(methodName.startsWith("get") && !methodName.endsWith("Class")) {
                String field = methodName.substring(3);
                field = field.substring(0, 1).toLowerCase() + field.substring(1);

                String type = method.getReturnType().getName();
                type = type.substring(type.lastIndexOf(".") + 1);

                if(isCreate) {
                    sb1.append(",").append(field).append(" ").append(type);
                    if(field.equals("uuid")) {
                        sb1.append(" primary key not null");
                    }
                } else {
                    Object value = null;
                    try {
                        value = method.invoke(this);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    String stringfyValue = "";
                    if(value!=null && value.getClass() == String.class) {
                        stringfyValue = "'" + value + "'";
                    } else {
                        stringfyValue += value;
                    }

                    if(isInsert) {
                        sb1.append(",").append(field);
                        sb2.append(",").append(stringfyValue);
                    } else { //update
                        if(field.equals("uuid")) {
                            sb2.append(field).append("=").append(stringfyValue);
                        } else {
                            if(field.equals("updateTime")) {
                                stringfyValue = "" + new Date().getTime();
                            }
                            sb1.append(",").append(field).append("=").append(stringfyValue);
                        }
                    }
                }
            }
        }

        sb1 = sb1.deleteCharAt(0);
        if(!isCreate) {
            if(isInsert) {
                sb1.insert(0, "(").append(") values (").append(sb2.deleteCharAt(0)).append(")");
            } else {
                sb1.append(" WHERE ").append(sb2);
            }
        }

        return sb1.toString();
    }
}

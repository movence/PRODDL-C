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

package pdl.cloud.model;

import org.soyatec.windowsazure.table.AbstractTableServiceEntity;
import org.soyatec.windowsazure.table.Guid;
import pdl.utils.StaticValues;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/12/12
 * Time: 10:55 AM
 */
public class FileInfo extends AbstractTableServiceEntity {
    //String iuuid; this is unique identifier same as rowKey property of table entity
    String type;
    String suuid; //file name
    String content;
    String userId;
    String container;
    String path;
    String originalName;
    int status; //reserved:1, committed:2

    public FileInfo(String partitionKey, String rowKey) {
        super(partitionKey, rowKey);
    }

    public FileInfo(String partitionKey) {
        this(partitionKey+"_file", new Guid().getValue());
    }

    public FileInfo() {
        this("generic");
    }

    public String getIuuid() {
        return this.getRowKey();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSuuid() {
        return suuid;
    }

    public void setSuuid(String suuid) {
        this.suuid = suuid;
    }

    public String getName() {
        return this.suuid;
    }

    public void setName(String name) {
        this.setSuuid(name);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusInString() {
        return status== StaticValues.FILE_STATUS_COMMITTED?"committed"
                :status==StaticValues.FILE_STATUS_RESERVED?"reserved"
                :"N/A";
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }
}

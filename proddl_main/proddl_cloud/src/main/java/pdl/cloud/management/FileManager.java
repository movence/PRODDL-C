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

package pdl.cloud.management;

import pdl.cloud.model.FileInfo;
import pdl.cloud.storage.TableOperator;
import pdl.common.Configuration;
import pdl.common.StaticValues;
import pdl.common.ToolPool;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 3/19/12
 * Time: 3:00 PM
 */
public class FileManager {
    private Configuration conf;
    private TableOperator tableOperator;
    private String filesTableName;

    public FileManager() {
        conf = Configuration.getInstance();
        tableOperator = new TableOperator(conf);
        filesTableName = ToolPool.buildTableName(StaticValues.TABLE_NAME_FILES);
    }

    public boolean submitJob(FileInfo fileInfo) throws Exception {
        boolean rtnVal = false;
        try {
            rtnVal = tableOperator.insertSingleEntity(filesTableName, fileInfo);

        } catch (Exception ex) {
            throw ex;
        }
        return rtnVal;
    }

    public String getFileNameById(String id) throws Exception {
        String name = null;
        try {
            FileInfo info = (FileInfo)tableOperator.queryEntityBySearchKey(filesTableName, StaticValues.COLUMN_ROW_KEY, id, FileInfo.class);
            if(info!=null)
                name=info.getSuuid();
        } catch(Exception ex) {
            throw ex;
        }
        return name;
    }
}

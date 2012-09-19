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

package pdl.operator.app;

import pdl.cloud.StorageServices;
import pdl.common.StaticValues;
import pdl.common.ToolPool;
import pdl.utils.ZipHandler;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 8/10/11
 * Time: 2:05 PM
 */
public abstract class AbstractApplicationOperator implements IApplicationOperator {
    protected String storagePath;
    protected String toolPath;
    protected String toolName;
    protected String arg;
    protected String flagFile;

    public AbstractApplicationOperator(String storagePath, String toolName, String flagFile, String arg) {
        this.storagePath = storagePath;
        this.toolName = toolName;
        toolPath = ToolPool.buildDirPath(storagePath, toolName);
        this.flagFile = flagFile;
        this.arg = arg;
    }

    public void run(StorageServices storageService) throws Exception {
        boolean result = false;

        String toolFileName = toolName+StaticValues.FILE_ZIP_EXTENTION;
        String toolFilePath = ToolPool.buildFilePath(storagePath, toolFileName);

        result = ToolPool.isDirectoryExist(toolPath);
        if(!result){
            result = ToolPool.canReadFile(toolFilePath);
            if(!result) {
                this.download(storageService, toolFileName);
            }
            result = this.unzip(toolFilePath);

            File toolFile = new File(toolFilePath);
            toolFile.delete();
        }

        if (!result)
            throw new Exception(String.format("Failed to obtain tool - %s%n", toolName));

    }

    public boolean download(StorageServices storageService, String toolFileName) throws Exception {
        return storageService.downloadToolsByName(toolFileName, ToolPool.buildFilePath(storagePath, toolFileName));
    }

    public boolean unzip(String toolFilePath) throws Exception {
        boolean rtnVal = false;

        ZipHandler zipOperator = new ZipHandler();
        if (zipOperator.unZip(toolFilePath, storagePath)) {
            if (ToolPool.isDirectoryExist(toolPath) && (new File(ToolPool.buildFilePath(toolPath, flagFile))).exists())
                rtnVal = true;
        }

        return rtnVal;
    }
}

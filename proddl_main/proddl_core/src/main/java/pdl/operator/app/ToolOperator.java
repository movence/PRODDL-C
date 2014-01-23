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

package pdl.operator.app;


import org.apache.commons.io.FileUtils;
import pdl.utils.StaticValues;
import pdl.utils.ToolPool;
import pdl.utils.ZipHandler;

import java.io.File;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 8/31/12
 * Time: 2:49 PM
 */
public class ToolOperator implements IApplicationOperator {
    //public volatile List<String> currentTools;
    protected String storagePath;
    protected String toolPath;
    protected String toolName;
    protected String flagFile;

    public ToolOperator(String storagePath, String toolName) {
        this(storagePath, toolName, null);
    }

    public ToolOperator(String storagePath, String toolName, String flagFile) {
        this.storagePath = storagePath;
        this.toolName = toolName;
        toolPath = ToolPool.buildDirPath(storagePath, toolName);
        this.flagFile = flagFile;
    }

    public boolean run() throws Exception{
        boolean result = false;

        String toolFileName = toolName + StaticValues.FILE_EXTENSION_ZIP;
        String toolFilePath = ToolPool.buildFilePath(storagePath, toolFileName);

        if(!ToolPool.isDirectoryExist(toolPath)) {
            while(!this.isToolReady(toolFileName, toolFilePath)) { //waits until any of three sources have the tool available
                Thread.sleep(10 * 60 * 1000);
            }
            result = this.unzip(toolFilePath);
            if(result) {
                File toolFile = new File(toolFilePath);
                FileUtils.deleteQuietly(toolFile);
            } else {
                throw new Exception("ToolOperator failed - " + toolName);
            }
        }

        return result;
    }

    public boolean isToolReady(String toolFileName, String toolFilePath) throws Exception {
        return ToolPool.canReadFile(toolFilePath) || this.downloadFromUrl("", toolFilePath);
    }

    private boolean downloadFromUrl(String url, String toolFilePath) throws Exception {
        boolean rtnVal = false;
        if(url!=null && !url.isEmpty()) {
            File tool = new File(toolFilePath);
            FileUtils.copyURLToFile(new URL(url), tool);
            rtnVal = tool.exists() && tool.isFile();
        }
        return rtnVal;
    }

    public boolean unzip(String toolFilePath) throws Exception {
        boolean rtnVal = false;
        ZipHandler zipOperator = new ZipHandler();
        if (zipOperator.unZip(toolFilePath, storagePath)) {
            if (ToolPool.isDirectoryExist(toolPath)) {
                rtnVal = (flagFile==null || flagFile.isEmpty() || (new File(ToolPool.buildFilePath(toolPath, flagFile))).exists());
            }
        }
        return rtnVal;
    }
}

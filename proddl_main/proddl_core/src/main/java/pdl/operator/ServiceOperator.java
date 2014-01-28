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

package pdl.operator;

import pdl.utils.Configuration;
import pdl.utils.StaticValues;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 12/13/11
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServiceOperator {

    /**
     * Main executor
     * @param args
     */
    public static void main(String[] args) {
        Configuration conf = null;

        if(args.length < 1) {
            throw new IllegalArgumentException("Usage: --conf=<path to " + StaticValues.CONFIG_FILENAME + ">");
        } else {
            String configArg = args[0];
            configArg = configArg.replace(" = ", "=");
            String iniPath = configArg.substring(configArg.indexOf("=") + 1);

            String iniFilePath = null;
            File iniFile = new File(iniPath);
            if(iniFile.exists() && iniFile.canRead()) {
                iniFilePath = iniFile.getAbsolutePath();
            } else {
                throw new IllegalArgumentException("cannot find config file - " + StaticValues.CONFIG_FILENAME);
            }

            ServiceOperatorHelper helper = new ServiceOperatorHelper(iniFilePath);
            helper.run();
        }
    }
}

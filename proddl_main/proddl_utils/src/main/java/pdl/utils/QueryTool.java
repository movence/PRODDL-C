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

package pdl.utils;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/27/12
 * Time: 9:25 AM
 */
public class QueryTool {

    public static String mergeConditions(String cond1, String condition, String cond2) {
        return String.format("%s %s %s", cond1, condition, cond2);
    }

    public static String getSingleConditionalStatement(String column, String condition, Object value) {

        String valueInString;
        if (value.getClass() != String.class)
            valueInString = String.valueOf(value);
        else
            valueInString = "'" + value + "'";

        return String.format("%s %s %s", column, condition, valueInString);
    }
}

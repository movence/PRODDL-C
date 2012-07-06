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

package pdl.operator;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 12/13/11
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServiceOperator {
    public static void main(String[] args) {
        ServiceOperatorHelper helper = new ServiceOperatorHelper();
        helper.run(
                args[0]/*master role identifier*/, args[1]/*storage path*/,
                args[2]/*current master's IP address*/, args[3]/*catalog_server port*/,
                args[4]/*jetty port*/, args[5]/*deployment ID*/);
    }
}

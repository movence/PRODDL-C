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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        List<String> argsList = new ArrayList<String>();
        HashMap<String, String> optsList = new HashMap<String, String>();
        List<String> doubleOptsList = new ArrayList<String>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i].charAt(0)) {
                case '-':
                    if (args[i].length() < 2)
                        throw new IllegalArgumentException("Not a valid argument: "+args[i]);
                    if (args[i].charAt(1) == '-') {
                        if (args[i].length() < 3)
                            throw new IllegalArgumentException("Not a valid argument: "+args[i]);
                        // --opt
                        doubleOptsList.add(args[i].substring(2, args[i].length()));
                    } else {
                        if (args.length-1 == i)
                            throw new IllegalArgumentException("Expected arg after: "+args[i]);
                        // -opt
                        optsList.put(args[i], args[i+1]);
                        i++;
                    }
                    break;
                default:
                    // arg
                    argsList.add(args[i]);
                    break;
            }
        }

        ServiceOperatorHelper helper = new ServiceOperatorHelper();
        helper.run();
    }
}

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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 8/10/11
 * Time: 11:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class NodeOperator extends AbstractApplicationOperator {
    public NodeOperator(String storagePath, String packageName, String flagFile, String param) {
        super( storagePath, packageName, flagFile, param );
    }

    public boolean start( String port ){
        boolean rtnVal = false;
        String line;

        try{
            System.out.println("NodeOperator: start START");

            List<String> command = new ArrayList<String>();
            command.add( packagePath + File.separator + "node" );
            command.add( "-Djetty.home=" + packagePath );

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            builder.directory(new File( packagePath ));

            process = builder.start();

            BufferedReader reader = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
            while ((line = reader.readLine ()) != null) {
                //System.out.println( "JETTY OUTPUT: " + line );
                if( line.contains( "Started" ) && line.endsWith( "STARTING" ) ) {
                    rtnVal = true;
                    break;
                }
            }

            System.out.println("NodeOperator: start DONE");
        }catch( Exception ex ){
            System.out.println( "NodeOperator.start threw : " + ex.toString() );
            ex.printStackTrace();
        }

        return rtnVal;
    }
}

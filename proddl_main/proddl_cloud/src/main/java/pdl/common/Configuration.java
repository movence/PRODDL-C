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

package pdl.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/23/12
 * Time: 11:28 AM
 */
public class Configuration {

    private static Configuration instance;
    Map<String, Object> properties;

    public Configuration() {
        this.properties = new HashMap<String, Object>();
    }

    public static Configuration getInstance() {
        if ( instance == null ) {
            try {
                instance = Configuration.load();
            }
            catch ( IOException e ) {
                e.printStackTrace();
                instance = new Configuration();
            }
        }
        return instance;
    }

    public static Configuration load() throws IOException {
        Configuration config = new Configuration();

        InputStream propStream = Configuration.class.getClassLoader().getResourceAsStream( "property/proddl.properties" );
        if ( propStream != null ) {
            Properties properties = new Properties();
            properties.load( propStream );
            for ( Object key : properties.keySet() ) {
                config.setProperty( key.toString() , properties.get( key ) );
            }
        } else
            System.err.println( "property/proddl.properties is missing." );

        return config;
    }

    public Object getProperty(String key) {
        return properties.get( key );
    }

    public void setProperty(String key, Object value) {
        properties.put( key, value );
    }

}

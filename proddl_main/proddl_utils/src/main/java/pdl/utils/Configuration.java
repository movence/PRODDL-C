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

package pdl.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
        return Configuration.getInstance(null);
    }

    public static Configuration getInstance(String path) {
        if (instance == null) {
            instance = Configuration.load(path);
        }
        return instance;
    }

    public static Configuration load(String path) {
        Configuration config = new Configuration();
        InputStream in = null;

        try {

            if(path == null || path.isEmpty()) { //when empty path is given
                //try to load a configuration file from the classloader
                in = Configuration.class.getClassLoader().getResourceAsStream(StaticValues.CONFIG_FILENAME);

                //if no file present at a path, load from the classpath
                if(in == null) {
                    URL url = null;
                    ClassLoader loader = ClassLoader.getSystemClassLoader();
                    if(loader!=null) {
                        url = loader.getResource(StaticValues.CONFIG_FILENAME);
                        if(url == null) {
                            url = loader.getResource("/"+StaticValues.CONFIG_FILENAME);
                        }
                        if(url != null) {
                            in = url.openStream();
                        }
                    }
                }
                //throw new Exception(StaticValues.CONFIG_FILENAME + " cannot be found.");
            } else {
                in = new FileInputStream(path);
            }

            if(in != null) {
                Properties props = new Properties();
                props.load(in);
                for (Object key : props.keySet()) {
                    config.setProperty(key.toString(), props.get(key));
                }
            } else {
                throw new Exception(StaticValues.CONFIG_FILENAME + " cannot be found.");
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            if(in!=null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return config;
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public String getStringProperty(String key) {
        Object value = this.getProperty(key);
        return value != null ? (String)value : null;
    }

    public Integer getIntegerProperty(String key) {
        return Integer.parseInt(getStringProperty(key));
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
}


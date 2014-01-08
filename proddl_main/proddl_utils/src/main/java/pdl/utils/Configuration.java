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


/**
 *
 * Sample Configuration file
 *
 * <Master>
 IsMaster={master|worker identifier}
 DeploymentName={postfix to blob directory and table names}
 DeploymentId={deployment ID}
 StoragePath={path to general storage area}
 DataStorePath={path to storage area for task and files}
 StorageAccountName={storage container name}
 StorageAccountPkey={primary access key to storage container}
 SubscriptionId={AZURE subscription ID: can be found in Azure portal}
 CloudRoleWorkerName={worker role name}
 CertificateName={certificate file name of keystore and trustcacert}
 CertificatePassword={password for certificate file}
 CertificateAlias={alias for certificate file}
 WebserverPort={web container port}
 InternalAddress={internal IP address}
 InternalPort={internal port number}
 * </Master>
 *
 * <Worker>
 IsMaster={master|worker identifier, can be omitted}
 DeploymentName={postfix to blob directory and table names}
 DeploymentId={deployment ID}
 StoragePath={path to general storage area}
 StorageAccountName={storage container name}
 StorageAccountPkey={primary access key to storage container}
 * </Worker>
 *
 */

public class Configuration {

    private static Configuration instance;
    Map<String, Object> properties;

    public Configuration() {
        this.properties = new HashMap<String, Object>();
    }

    public static Configuration getInstance() {
        if (instance == null) {
            try {
                instance = Configuration.load();
            } catch (IOException e) {
                e.printStackTrace();
                instance = new Configuration();
            }
        }
        return instance;
    }

    public static Configuration load() throws IOException {
        Configuration config = new Configuration();
        InputStream in = null;

        try {
            in = Configuration.class.getClassLoader().getResourceAsStream(StaticValues.CONFIG_FILENAME);

            if(in==null) {
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

            if(in!=null) {
                Properties props = new Properties();
                props.load(in);
                for (Object key : props.keySet()) {
                    config.setProperty(key.toString(), props.get(key));
                }
            } else {
                System.err.println(StaticValues.CONFIG_FILENAME + " cannot be found.");
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            if(in!=null)
                in.close();
        }

        return config;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public String getStringProperty(String key) {
        return (String) getProperty(key);
    }

    public Integer getIntegerProperty(String key) {
        return Integer.parseInt(getStringProperty(key));
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
}


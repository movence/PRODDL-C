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

package pdl.cloud.management;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.soyatec.windowsazure.blob.io.BlobMemoryStream;
import org.soyatec.windowsazure.blob.io.BlobStream;
import org.soyatec.windowsazure.management.Deployment;
import org.soyatec.windowsazure.management.HostedService;
import org.soyatec.windowsazure.management.HostedServiceProperties;
import org.soyatec.windowsazure.management.ServiceManagementRest;
import org.soyatec.windowsazure.table.ITableServiceEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import pdl.cloud.model.Info;
import pdl.cloud.model.PerformanceData;
import pdl.cloud.storage.TableOperator;
import pdl.utils.Configuration;
import pdl.utils.StaticValues;
import pdl.utils.ToolPool;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 8/11/11
 * Time: 10:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class CloudInstanceManager {
    public static final String TABLE_PERFORMANCE_COUNTER_NAME = "WADPerformanceCountersTable";

    private Configuration conf;
    private ServiceManagementRest manager;
    private String hostedServiceName;
    private TableOperator tableOperator;

    private Document configurationDocument;
    private Element countElement;

    public CloudInstanceManager() {
        conf = Configuration.getInstance();
        initializeManager();
    }

    /**
     * initialize cloud instance manager with certificates and password from infos table
     * configuration document is xml configuration file for current deployment
     */
    private void initializeManager() {
        try {
            String dataStorePath = conf.getStringProperty(StaticValues.CONFIG_KEY_DATASTORE_PATH);

            tableOperator = new TableOperator(conf);
            String keystoreFilaName = StaticValues.CERTIFICATE_NAME + ".keystore";
            String trustcaFieName = StaticValues.CERTIFICATE_NAME + ".trustcacerts";
            String keystoreFilePath = ToolPool.buildFilePath(dataStorePath, StaticValues.DIRECTORY_FILE_AREA, keystoreFilaName);
            String trustcaFilePath = ToolPool.buildFilePath(dataStorePath, StaticValues.DIRECTORY_FILE_AREA, trustcaFieName);

            String certPass = conf.getStringProperty(StaticValues.CONFIG_KEY_CERT_PASSWORD);
            if (certPass == null) {
                Info passData = (Info) tableOperator.queryEntityBySearchKey(
                        ToolPool.buildTableName(StaticValues.TABLE_NAME_INFOS),
                        StaticValues.COLUMN_INFOS_KEY,
                        StaticValues.CONFIG_KEY_CERT_PASSWORD,
                        Info.class);
                certPass = passData.getiValue();
            }
            manager = new ServiceManagementRest(
                    conf.getStringProperty(StaticValues.CONFIG_KEY_SUBSCRIPTION_ID),
                    keystoreFilePath, certPass,
                    trustcaFilePath, certPass,
                    StaticValues.CERTIFICATE_ALIAS
            );
            configurationDocument = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get hosted cloud service name for current deployment
     * This method assumes that there is only one available hosted service
     * @return current hosted service name
     * @throws Exception
     */
    public String getHostedServiceName() throws Exception {
        String serviceName = null;
        try {
            //TODO should handle multiple hosted services
            List<HostedService> hostedServices = manager.listHostedServices();
            if (hostedServices != null) { // && hostedServices.size() == 1) {
                serviceName = hostedServices.get(0).getName();
            }
        } catch (Exception e) {
            //TODO remove exception stack trace
            e.printStackTrace();
            throw new Exception("Failed to get hosted service name!");
        }
        return serviceName;
    }

    /**
     * get a list of deployed instances
     * @return list of deployments
     * @throws Exception
     */
    private List<Deployment> getDeploymentList() throws Exception {
        List<Deployment> deploymentList = null;
        try {
            hostedServiceName = getHostedServiceName();

            if (hostedServiceName != null) {
                HostedServiceProperties serviceProperties = manager.getHostedServiceProperties(hostedServiceName, true);
                deploymentList = serviceProperties.getDeployments();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Failed to get list of deployments!");
        }
        return deploymentList;
    }

    /**
     * get configuration values for a deployment by matching deployment ID
     * This method should return null in case of the instance is in "transitioning" stage
     * @return string of deployment name
     * @throws Exception
     */
    private String getDeploymentName() throws Exception {
        String deploymentName = null;

        List<Deployment> deployments = getDeploymentList();
        if (deployments == null || deployments.size() == 0)
            throw new Exception("No deployment is available.");

        for (Deployment deployment : deployments) {
            //finds correct deployment by id
            if ((deployment.getStatus() != null && deployment.getStatus().getLiteral().equals("RunningTransitioning"))
                    || !deployment.getPrivateId().equals(conf.getStringProperty(StaticValues.CONFIG_KEY_DEPLOYMENT_ID))) {
                continue;
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(deployment.getConfiguration()));

            DOMParser domParser = new DOMParser();
            domParser.parse(is);
            configurationDocument = domParser.getDocument();
            deploymentName = deployment.getName();
        }
        return deploymentName;
    }

    /**
     * get number of current role instances by navigating configuration document
     * @return integer of current role count
     */
    public int getCurrentInstanceCount() {
        int count = 0;

        try {
            String deploymentName = null;
            while(configurationDocument==null || deploymentName==null) {
                deploymentName = this.getDeploymentName();
            }

            NodeList nodes = configurationDocument.getElementsByTagName("Role");
            for (int i = 0; i < nodes.getLength(); i++) {

                Element roleElement = (Element) nodes.item(i);

                if (roleElement.getAttribute("name").equals(conf.getStringProperty(StaticValues.CONFIG_KEY_WORKER_NAME))) {
                    NodeList instanceCountNode = roleElement.getElementsByTagName("Instances");
                    countElement = (Element) instanceCountNode.item(0);
                    count = Integer.valueOf(countElement.getAttribute("count"));
                    break;
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return count;
    }

    /**
     * change the number of worker role
     * @param count target number of instance count
     * @return boolean of job result
     */
    public boolean scaleService(int count) {
        boolean rtnVal = false;
        try {
            if(count>0 && count<StaticValues.MAX_TOTAL_WORKER_INSTANCE) {
                String deploymentName = this.getDeploymentName();
                int currentInstanceCount = this.getCurrentInstanceCount();

                if(currentInstanceCount!=0 && currentInstanceCount!=count) {
                    countElement.setAttribute("count", ""+count);

                    StringWriter out = new StringWriter();
                    XMLSerializer serializer = new XMLSerializer(out, new OutputFormat(configurationDocument));
                    serializer.serialize(configurationDocument);

                    BlobStream blobFileStream = new BlobMemoryStream(new ByteArrayInputStream(out.toString().getBytes("UTF-8")));
                    manager.changeDeploymentConfiguration(hostedServiceName, deploymentName, blobFileStream, null);

                    rtnVal = true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rtnVal;
    }

    /**
     * !currently not used
     * scale worker role by averaging CPU usage of all available workers
     * add another worker when average CPU usage is greater than 50%, scale down by half when the usage is below 15%
     */
    public void executeScalingByProcessorTime() {
        try {
            /*
            * No reason to query by Counter Name.
            * Query performance data by deployment id since there could be multiple instances sharing the same storage account
            * 8/3/12 by hkim
            String COLUMN_PERFORMANCE_COUNTER_NAME = "CounterName";
            String COLUMN_PERFORMANCE_COUNTER_VALUE = "\\Processor(_Total)\\% Processor Time";

            List<ITableServiceEntity> entityList = tableOperator.queryListBySearchKey(
                 TABLE_PERFORMANCE_COUNTER_NAME, COLUMN_PERFORMANCE_COUNTER_NAME,
                 COLUMN_PERFORMANCE_COUNTER_VALUE, null, null, PerformanceData.class);
            */

            List<ITableServiceEntity> entityList = tableOperator.queryListBySearchKey(
                    TABLE_PERFORMANCE_COUNTER_NAME,
                    StaticValues.CONFIG_KEY_DEPLOYMENT_ID,
                    conf.getStringProperty(StaticValues.CONFIG_KEY_DEPLOYMENT_ID),
                    null, null,
                    PerformanceData.class);

            if (entityList != null && entityList.size() > 0) {
                float processorTimeFactor = 0;
                for (ITableServiceEntity entity : entityList) {
                    processorTimeFactor += (float) ((PerformanceData) entity).getCounterValue();
                    tableOperator.deleteEntity(TABLE_PERFORMANCE_COUNTER_NAME, entity);
                }

                processorTimeFactor /= entityList.size();

                int currCount = this.getCurrentInstanceCount();
                if (processorTimeFactor > StaticValues.MAXIMUM_AVERAGE_CPU_USAGE) {
                    this.scaleService(currCount+1);
                } else if (processorTimeFactor < StaticValues.MINIMUM_AVERAGE_CPU_USAGE) {
                    this.scaleService(currCount/2);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

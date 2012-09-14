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
import pdl.cloud.model.PerformanceData;
import pdl.cloud.storage.BlobOperator;
import pdl.cloud.storage.TableOperator;
import pdl.common.Configuration;
import pdl.common.StaticValues;

import java.io.ByteArrayInputStream;
import java.io.File;
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

    private String storagePath;
    private TableOperator tableOperator;

    public CloudInstanceManager() {
        conf = Configuration.getInstance();
        initializeManager();
    }

    private void initializeManager() {
        try {
            this.storagePath = conf.getStringProperty(StaticValues.CONFIG_KEY_STORAGE_PATH);

            tableOperator = new TableOperator(conf);

            BlobOperator blobOperator = new BlobOperator(conf);
            String keystoreFilaName = String.format("%s.%s", conf.getStringProperty(StaticValues.CONFIG_KEY_CERTIFICATE_NAME), "keystore");
            String trustcaFieName = String.format("%s.%s", conf.getStringProperty(StaticValues.CONFIG_KEY_CERTIFICATE_NAME), "trustcacerts");
            String keystoreFilePath = storagePath+keystoreFilaName;
            String trustcaFiePath = storagePath+trustcaFieName;

            //download keystore file for azure management
            if (!new File(keystoreFilePath).exists())
                blobOperator.download(
                        StaticValues.BLOB_CONTAINER_TOOLS,
                        keystoreFilaName,
                        storagePath
                );
            if (!new File(trustcaFiePath).exists())
                blobOperator.download(
                        StaticValues.BLOB_CONTAINER_TOOLS,
                        trustcaFieName,
                        storagePath
                );

            manager = new ServiceManagementRest(
                    conf.getStringProperty(StaticValues.CONFIG_KEY_SUBSCRIPTION_ID),
                    keystoreFilePath,
                    conf.getStringProperty(StaticValues.CONFIG_KEY_CERT_PASSWORD),
                    trustcaFiePath,
                    conf.getStringProperty(StaticValues.CONFIG_KEY_CERT_PASSWORD),
                    conf.getStringProperty(StaticValues.CONFIG_KEY_CERT_ALIAS));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    private List<Deployment> getDeploymentList() throws Exception {
        List<Deployment> deploymentList = null;
        try {
            hostedServiceName = getHostedServiceName();

            if(hostedServiceName != null) {
                HostedServiceProperties serviceProperties = manager.getHostedServiceProperties(hostedServiceName, true);
                deploymentList = serviceProperties.getDeployments();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Failed to get list of deployments!");
        }
        return deploymentList;
    }

    private boolean scaleService(String flag, String deploymentName, int count) {
        boolean rtnVal = false;
        try {
            List<Deployment> deployments = getDeploymentList();
            if (deployments == null || deployments.size() == 0)
                throw new Exception("scaleService threw Exception: There is no deployed service");

            for (Deployment deployment : deployments) {
                System.err.printf("****Scaling Service Log: %s, %s", deployment.getName(), deployment.getStatus());

                //skips deployments by given name or status
                if ((deploymentName != null && !deploymentName.equals(deployment.getName()))
                        || (deployment.getStatus()!=null && deployment.getStatus().equals("RunningTransitioning"))
                        || !conf.getStringProperty(StaticValues.CONFIG_KEY_DEPLOYMENT_ID).equals(deployment.getPrivateId()))
                    continue;

                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(deployment.getConfiguration()));

                DOMParser domParser = new DOMParser();
                domParser.parse(is);
                Document doc = domParser.getDocument();
                NodeList nodes = doc.getElementsByTagName("Role");

                for (int i = 0; i < nodes.getLength(); i++) {

                    Element roleElement = (Element) nodes.item(i);

                    if (roleElement.getAttribute("name").equals(conf.getStringProperty(StaticValues.CONFIG_KEY_WORKER_NAME))) {

                        NodeList instanceCountNode = roleElement.getElementsByTagName("Instances");
                        Element instanceCountElement = (Element) instanceCountNode.item(0);

                        int currentInstanceCount = Integer.valueOf(instanceCountElement.getAttribute("count"));

                        if ("up".equals(flag)) {
                            if (currentInstanceCount == StaticValues.MAX_TOTAL_WORKER_INSTANCE) {
                                break;
                            }

                            currentInstanceCount++;
                        } else {
                            //prevents instance count becomes 0
                            if (currentInstanceCount == 1) {
                                break;
                            }

                            //currentInstanceCount--;
                            //reduce number of instances by half
                            currentInstanceCount/=2;
                        }
                        instanceCountElement.setAttribute("count", (count!=0?count:currentInstanceCount)+"");

                        StringWriter out = new StringWriter();
                        XMLSerializer serializer = new XMLSerializer(out, new OutputFormat(doc));
                        serializer.serialize(doc);

                        BlobStream blobFileStream = new BlobMemoryStream(new ByteArrayInputStream(out.toString().getBytes("UTF-8")));
                        manager.changeDeploymentConfiguration(hostedServiceName, deployment.getName(), blobFileStream, null);


                        rtnVal = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnVal;
    }

    public boolean scaleUp(int count) {
        return scaleService("up", null, count);
    }

    public boolean scaleDown(int count) {
        return scaleService("down", null, count);
    }

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
                    COLUMN_PERFORMANCE_COUNTER_VALUE, null, null, PerformanceData.class);*/

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

                if (processorTimeFactor > StaticValues.MAXIMUM_AVERAGE_CPU_USAGE) {
                    this.scaleUp(0);
                } else if (processorTimeFactor < StaticValues.MINIMUM_AVERAGE_CPU_USAGE) {
                    this.scaleDown(0);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

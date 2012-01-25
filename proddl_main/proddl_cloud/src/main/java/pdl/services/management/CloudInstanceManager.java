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

package pdl.services.management;

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
import pdl.common.Configuration;
import pdl.common.StaticValues;
import pdl.services.model.PerformanceData;
import pdl.services.storage.BlobOperator;
import pdl.services.storage.TableOperator;

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
    public static final String COLUMN_PERFORMANCE_COUNTER_NAME = "CounterName";
    public static final String COLUMN_PERFORMANCE_COUNTER_VALUE = "\\Processor(_Total)\\% Processor Time";

    private Configuration conf;
    private ServiceManagementRest manager;
    private String hostedServiceName;

    private String storagePath;
    private TableOperator tableOperator;

    public CloudInstanceManager() {
        try {
            conf = Configuration.getInstance();
            initializeManager( conf );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CloudInstanceManager( Configuration conf ) {
        this.conf = conf;
        initializeManager( conf );
    }

    private void initializeManager( Configuration conf ) {
        try {
            this.storagePath = (String)conf.getProperty( "STORAGE_PATH" );

            tableOperator = new TableOperator( conf );
            tableOperator.initDiagnosticsTableClient();

            BlobOperator blobOperator = new BlobOperator( conf );
            String keystoreFilePath = storagePath + File.separator + conf.getProperty( "KEYSTORE_FILE_NAME" );
            String trustcacertFiePath = storagePath + File.separator + conf.getProperty( "TRUSTCACERT_FILE_NAME" );

            //download keystore file for azure management
            if( !new File( keystoreFilePath ).exists() )
                blobOperator.download( "tools", (String)conf.getProperty( "KEYSTORE_FILE_NAME" ), storagePath );
            if( !new File( trustcacertFiePath ).exists() )
                blobOperator.download( "tools", (String)conf.getProperty( "TRUSTCACERT_FILE_NAME" ), storagePath );

            manager = new ServiceManagementRest(
                    (String)conf.getProperty( "SUBSCRIPTION_ID" ),
                    keystoreFilePath,
                    (String)conf.getProperty( "CERTIFICATE_PASS" ),
                    trustcacertFiePath,
                    (String)conf.getProperty( "CERTIFICATE_PASS" ),
                    (String)conf.getProperty( "CERTIFICATE_ALIAS" ));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void getServiceStatus() {
        //TODO need to implement initialization checkups (i.e. table creation for workers' info, etc.)
    }

    public String getHostedServiceName() {
        try {
            List<HostedService> hostedServices = manager.listHostedServices();
            if ( hostedServices != null && hostedServices.size() == 1 ) {
                return hostedServiceName = hostedServices.get( 0 ).getName();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Deployment> getDeploymentList() {
        try {
            if( hostedServiceName == null )
                hostedServiceName = getHostedServiceName();
            HostedServiceProperties servicePropertieses = manager.getHostedServiceProperties( hostedServiceName, true );
            return servicePropertieses.getDeployments();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean scaleService(String flag, String deploymentName) {
        try {
            List<Deployment> deployments = getDeploymentList();
            if( deployments == null || deployments.size() == 0 )
                throw new Exception( "scaleService threw Exception: There is no deployed service" );

            for( Deployment deployment : deployments ) {
                if( deploymentName != null && !deploymentName.equals( deployment.getName() ) )
                    continue; //skips deployments by given name

                InputSource is = new InputSource();
                is.setCharacterStream( new StringReader( deployment.getConfiguration() ) );

                DOMParser domParser = new DOMParser();
                domParser.parse( is );
                Document doc = domParser.getDocument();
                NodeList nodes = doc.getElementsByTagName( "Role" );

                for( int i = 0; i < nodes.getLength(); i++ ) {

                    Element roleElement = (Element) nodes.item(i);

                    if( roleElement.getAttribute("name").equals( StaticValues.CLOUD_ROLE_WORKER_NAME ) ) {
                        NodeList instanceCountNode = roleElement.getElementsByTagName( "Instances" );
                        Element instanceCountElement = (Element)instanceCountNode.item( 0 );

                        int currentInstanceCount = Integer.valueOf( instanceCountElement.getAttribute( "count" ) );


                        if( "up".equals( flag ) ) {
                            //prevents number of total instances go over 100
                            //TODO need to change instance number limit
                            if( currentInstanceCount == 100 ) {
                                System.out.println( "The number of worker instances is at the limit.");
                                break;
                            }

                            currentInstanceCount++;
                        } else {
                            //prevents instance count becomes 0
                            if( currentInstanceCount == 1 ) {
                                System.out.println( "There is only one worker instance.");
                                break;
                            }

                            currentInstanceCount--;
                        }
                        instanceCountElement.setAttribute( "count", currentInstanceCount + "" );

                        StringWriter out = new StringWriter();
                        XMLSerializer serializer = new XMLSerializer(out, new OutputFormat( doc ));
                        serializer.serialize( doc );

                        BlobStream blobFileStream = new BlobMemoryStream( new ByteArrayInputStream( out.toString().getBytes("UTF-8") ) );
                        manager.changeDeploymentConfiguration( hostedServiceName, deployment.getName(), blobFileStream, null );

                        break;
                    }
                }
            }

            return true;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean scaleUp() {
        System.out.println( "MainManager - Scale Up" );
        return scaleService( "up" , null );
    }

    public boolean scaleDown() {
        System.out.println( "MainManager - Scale Down" );
        return scaleService( "down", null );
    }

    public void executeScalingByProcessorTime() {
        try {
            List<ITableServiceEntity> entityList = tableOperator.queryListBySearchKey(
                    TABLE_PERFORMANCE_COUNTER_NAME, COLUMN_PERFORMANCE_COUNTER_NAME,
                    COLUMN_PERFORMANCE_COUNTER_VALUE, null, null, new PerformanceData() );

            float processorTimeFactor = 0;
            for( ITableServiceEntity entity : entityList ) {
                processorTimeFactor += (float)( ( PerformanceData )entity ).getCounterValue();
                tableOperator.deleteEntity( TABLE_PERFORMANCE_COUNTER_NAME, entity );
            }

            processorTimeFactor /= entityList.size();
            System.err.println( "Average Processor Time of Total Processors: " + processorTimeFactor );

            /*if( processorTimeFactor > 90 || processorTimeFactor < 30 ) {
                //deletes all performance data after scaling process happens
                for( ITableServiceEntity entity : entityList ) {
                    storageOperator.deleteEntity( TABLE_PERFORMANCE_COUNTER_NAME, entity );
                }*/

            if( processorTimeFactor > 90 ) {
                this.scaleUp();
            } else if ( processorTimeFactor < 30 ) {
                this.scaleDown();
            }
            /*}*/
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }
}

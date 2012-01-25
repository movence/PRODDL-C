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

import pdl.common.Configuration;
import pdl.operator.app.CctoolsOperator;
import pdl.operator.app.CygwinOperator;
import pdl.operator.app.JettyThreadedOperator;
import pdl.operator.app.PythonOperator;
import pdl.services.management.ScheduledInstanceMonitor;
import pdl.services.storage.BlobOperator;

import java.io.File;
import java.util.Timer;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 12/13/11
 * Time: 2:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServiceOperatorHelper {
    public ServiceOperatorHelper() {}

    /**
     *
     * @param isMaster
     * @param storagePath
     * @param masterAddress
     * @param catalogServerPort
     * @param jettyPort
     */
    public void run(String isMaster, String storagePath, String masterAddress, String catalogServerPort, String jettyPort) {
        try {
            Configuration conf = Configuration.getInstance();
            storagePath = storagePath.replace( "/", File.separator );
            if( !storagePath.endsWith( File.separator ) )
                storagePath += File.separator;

            conf.setProperty( "STORAGE_PATH", storagePath );

            BlobOperator blobOperator = new BlobOperator( conf );

            PythonOperator pythonOperator = new PythonOperator(
                    storagePath,
                    (String)conf.getProperty( "PYTHON_NAME" ),
                    (String)conf.getProperty( "PYTHON_FLAG_FILE" ),
                    null
            );
            pythonOperator.runOperator( blobOperator );

            CctoolsOperator cctoolsOperator = new CctoolsOperator(
                    storagePath,
                    (String)conf.getProperty( "CCTOOLS_NAME" ),
                    "bin" + File.separator + conf.getProperty( "CCTOOLS_FLAG_FILE" ),
                    null
            );
            cctoolsOperator.runOperator( blobOperator );

            CygwinOperator cygwinOperator = new CygwinOperator(
                    storagePath,
                    (String)conf.getProperty( "CYGWIN_NAME" ),
                    (String)conf.getProperty( "CYGWIN_FLAG_FILE" ),
                    null
            );
            cygwinOperator.runOperator( blobOperator );

            if( isMaster.equals( "true" ) ) {
                /*JettyOperator jettyOperator = new JettyOperator( storagePath, prop.getProperty( "JETTY_NAME" ) );
                if( jettyOperator.download( blobOperator, prop.getProperty( "JETTY_FLAG_FILE" ) ) ) {
                    jettyOperator.start( jettyPort );
                }*/
                JettyThreadedOperator jettyOperator = new JettyThreadedOperator( jettyPort );
                jettyOperator.start();

                String tempCatalogServerAddress = cctoolsOperator.getCatalogServerAddress();
                if( tempCatalogServerAddress == null) {
                    if( !cctoolsOperator.startCatalogServer( masterAddress, catalogServerPort ) ) {
                        throw new Exception( "Failed to start Catalog Server at " + masterAddress + ":" + catalogServerPort );
                    }
                }

                //TODO hkim For local Testing Purpose
                blobOperator.download( "tools", (String)conf.getProperty( "KEYSTORE_FILE_NAME" ), storagePath );
                blobOperator.download( "tools", (String)conf.getProperty( "TRUSTCACERT_FILE_NAME" ), storagePath );

                //Adds processor time monitor to timer
                ScheduledInstanceMonitor instanceMonitor = new ScheduledInstanceMonitor( conf );
                Timer instanceMonitorTimer = new Timer();
                instanceMonitorTimer.scheduleAtFixedRate( instanceMonitor, 300000, 300000 );

                while( true ) {
                    cctoolsOperator.startMakeflow( "test" );
                    Thread.sleep( 600000 );
                }
            } else { //For JobRunner
                //blobOperator.download( "tools", "tester.jar", storagePath );

                while( true ) {

                    while( cctoolsOperator.getCatalogServerAddress() == null ) {
                        //storageOperator.dequeue( StaticValues.QUEUE_JOBQUEUE_NAME, false ) == null ) {
                        System.out.println(
                                "CatalogServer or Makeflow has not been initialized. Worker role waits for 10s."
                        );
                        Thread.sleep(10000);
                    }

                    cctoolsOperator.startWorkQ();
                    Thread.sleep( 600000 );
                }

            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}

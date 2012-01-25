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

import pdl.common.Configuration;
import pdl.common.StaticValues;
import pdl.services.model.DynamicData;
import pdl.services.storage.BlobOperator;
import pdl.services.storage.TableOperator;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 8/15/11
 * Time: 9:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class CctoolsOperator extends AbstractApplicationOperator {
    private static final String KEY_DYNAMIC_DATA_CATALOGSERVERADDRESS = "CatalogServerAddress";
    private static final String KEY_DYNAMIC_DATA_CATALOGSERVERPORT = "CatalogServerPort";

    private Configuration conf;

    private String cctoolsBinPath;
    private String cygwinBinPath;

    private String catalogServerAddress;
    private String catalogServerPort;

    private boolean isEnvironmentVarialbeSet = false;

    public CctoolsOperator(String storagePath, String packageName, String flagFile, String param) {
        super( storagePath, packageName, flagFile, param );
        setPaths();
    }

    private void setPaths() {
        try {
            conf = Configuration.getInstance();

            cctoolsBinPath = packagePath + File.separator + "bin";
            cygwinBinPath = storagePath + conf.getProperty( "CYGWIN_NAME" ) + File.separator + "bin";
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean start( String param ){
        return true;
    }

    private ProcessBuilder buildProcessBuilder( String... args ) {
        if( cygwinBinPath == null || cctoolsBinPath == null )
            this.setPaths();

        ProcessBuilder pb = new ProcessBuilder( args );
        pb.directory( new File( cctoolsBinPath ) );
        pb.redirectErrorStream(true);

        if( !isEnvironmentVarialbeSet ) {
            Map<String, String> env = pb.environment();
            for( String key : env.keySet() ) {
                if( key.toLowerCase().equals( "path" ) ) {
                    env.put( key, cygwinBinPath + File.pathSeparator + cctoolsBinPath + File.pathSeparator + env.get( key ) );
                    break;
                }
            }
            isEnvironmentVarialbeSet = true;
        }
        return pb;
    }

    public String getCatalogServerAddress() {
        try {
            if( this.catalogServerAddress == null || this.catalogServerAddress.isEmpty() )
                this.catalogServerAddress = this.getCatalogServerInfo( KEY_DYNAMIC_DATA_CATALOGSERVERADDRESS );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this.catalogServerAddress;
    }

    public String getCatalogServerPort() {
        try {
            if( this.catalogServerPort == null || this.catalogServerPort.isEmpty() )
                this.catalogServerPort = this.getCatalogServerInfo( KEY_DYNAMIC_DATA_CATALOGSERVERPORT );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this.catalogServerPort;
    }

    private void validateCatalogServerInformation() {
        this.getCatalogServerAddress();
        this.getCatalogServerPort();
    }

    public boolean startMakeflow(String taskName) {
        boolean rtnVal = false;
        String makeflowFileName = "copytest.makeflow";
        try {
            validateCatalogServerInformation();

            File testMakeflowFile = new File( storagePath + File.separatorChar + makeflowFileName );
            if( !testMakeflowFile.exists() || !testMakeflowFile.canRead() ) {
                BlobOperator blobOperator = new BlobOperator( conf );
                blobOperator.download( "tools", makeflowFileName, storagePath );
            }

            ProcessBuilder pb = this.buildProcessBuilder(
                    cctoolsBinPath + File.separator + "makeflow",
                    "-T", "wq",
                    "-C", catalogServerAddress + ":" + catalogServerPort, "-a",
                    "-N", taskName,
                    storagePath + makeflowFileName );

            process = pb.start();
            BufferedReader ireader = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
            String line;

            //storageOperator.enqueue( StaticValues.QUEUE_JOBQUEUE_NAME, taskName );

            while ( (line = ireader.readLine ()) != null ) {
                System.out.println( "MAKEFLOW OUTPUT: " + line );
            }

            System.out.println( "Makeflow process for " + taskName + " has been completed." );

            rtnVal = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnVal;
    }

    public boolean startWorkQ() {
        boolean rtnVal = false;
        try {
            validateCatalogServerInformation();

            //String taskName = storageOperator.dequeue( StaticValues.QUEUE_JOBQUEUE_NAME, true );

            ProcessBuilder pb = this.buildProcessBuilder(
                    cctoolsBinPath + File.separator + "work_queue_worker",
                    "-C",  catalogServerAddress + ":" + catalogServerPort, "-a", "-s" );
                    //"-N", taskName );

            process = pb.start();
            BufferedReader ireader = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
            String line;
            while ( (line = ireader.readLine ()) != null ) {
                System.out.println( "WORKQ OUTPUT: " + line );
            }

            System.out.println( "Makeflow process for has been completed." );

            rtnVal = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnVal;
    }

    public boolean startCatalogServer(String catalogServerAddress, String catalogServerPort) {
        boolean rtnVal = false;

        try {
            this.catalogServerAddress = catalogServerAddress;
            this.catalogServerPort = catalogServerPort;

            this.updateCatalogServerInfo( KEY_DYNAMIC_DATA_CATALOGSERVERADDRESS, catalogServerAddress );
            this.updateCatalogServerInfo( KEY_DYNAMIC_DATA_CATALOGSERVERPORT, catalogServerPort );

            ProcessBuilder pb = this.buildProcessBuilder( cctoolsBinPath + File.separator + "catalog_server", "-p", catalogServerPort );

            Process catalogServerProcess = pb.start();
            BufferedReader ireader = new BufferedReader( new InputStreamReader( catalogServerProcess.getInputStream() ) );
            String line;
            while ( (line = ireader.readLine ()) != null ) {
                if( line.contains( "catalog_server" ) && line.contains( "starting" ) ) {
                    rtnVal = true;
                    break;
                }
            }

            System.out.println("CctoolsOperator : STARTING CATALOG_SERVER DONE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnVal;
    }

    public  void updateCatalogServerInfo(String key, String value) {
        try {
            DynamicData catalogServerInfo = new DynamicData( "catalogserver_info" );
            catalogServerInfo.setDataKey( key );
            catalogServerInfo.setDataValue( value );

            TableOperator tableOperator = new TableOperator( conf );
            tableOperator.insertSingleEntity( StaticValues.TABLE_DYNAMIC_DATA_NAME, catalogServerInfo );
            System.out.println("Update catalogServerPort(" + key + ":" + value + ") information to " + StaticValues.TABLE_DYNAMIC_DATA_NAME );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public String getCatalogServerInfo( String key ) {
        String rtnVal = null;

        try {
            TableOperator tableOperator = new TableOperator( conf );

            DynamicData catalogServerInfo = new DynamicData( null, null );
            catalogServerInfo = (DynamicData)tableOperator.queryEntityBySearchKey(
                    StaticValues.TABLE_DYNAMIC_DATA_NAME, StaticValues.COLUMN_DYNAMIC_DATA_NAME, key, catalogServerInfo
            );

            if( catalogServerInfo != null )
                rtnVal = catalogServerInfo.getDataValue();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return rtnVal;
    }

}

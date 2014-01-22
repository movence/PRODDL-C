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

import pdl.cloud.StorageServices;
import pdl.cloud.model.FileInfo;
import pdl.cloud.model.Info;
import pdl.utils.Configuration;
import pdl.utils.FileTool;
import pdl.utils.StaticValues;
import pdl.utils.ToolPool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 9/14/12
 * Time: 1:04 PM
 * Online tutorial on creating and converting Windows certificate http://www.windowsazure4j.org/learn/labs/Management/index.html
 */
public class CertificateManager {
    Configuration conf;

    public CertificateManager() {
        conf = Configuration.getInstance();
    }

    /**
     * generate keystore and trustcacert certificates for use in azure management API
     * @param pfxFileId file id for pfx file
     * @param cerFileId file id for cer file
     * @param password  password for certificate
     * @return boolean of job result
     */
    public boolean execute(String pfxFileId, String cerFileId, String password) {
        boolean rtnVal = false;
        String keystorePath = this.getKeystorePath(pfxFileId, password);
        String trustPath = this.getTrustPath(cerFileId, ToolPool.buildDirPath(conf.getStringProperty(StaticValues.CONFIG_KEY_STORAGE_PATH), "jre"));
        if(keystorePath!=null && trustPath!=null) {
            rtnVal = true;
        }
        return rtnVal;
    }

    /**
     * generate keystore with pfx
     * @param fileId file id for pfx file
     * @param password password for certificate
     * @return string of path keystore certificate
     */
    public String getKeystorePath(String fileId, String password) {
        String pfxPath = getPfxCertificate(fileId);
        return this.createKeystoreCertificate(pfxPath, password);
    }

    /**
     * generate trustcacert with cer
     * @param fileId file id for cer file
     * @param javaPath path to JRE
     * @return string of path trustcacert certificate
     */
    public String getTrustPath(String fileId, String javaPath) {
        String cerPath = this.getCerCertificate(fileId);
        return this.createTrustCertificate(javaPath, cerPath);
    }

    /**
     * copy a certificate from file area to datastore area
     * @param fileId file id for a certificate
     * @param type pfx or cer
     * @return string of path to a certificcate file
     */
    private String getCertificate(String fileId, String type) {
        String filePath = null;
        try {
            boolean gotCertificate = false;
            String ext = type!=null && type.equals("pfx")?".pfx":".cer";
            FileTool fileTool = new FileTool();
            FileInfo certFileInfo = fileTool.getFileInfoById(fileId);
            filePath = ToolPool.buildFilePath(fileTool.getFileStoragePath(), StaticValues.CERTIFICATE_NAME+ext);
            if(certFileInfo!=null) {
                gotCertificate = fileTool.copyFromDatastore(ToolPool.buildFilePath(certFileInfo.getPath(), certFileInfo.getName()), filePath);

                if(!gotCertificate)
                    throw new Exception("failed to get certificate.");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            filePath = null;
        }
        return filePath;
    }

    private String getPfxCertificate(String fileId) {
        return this.getCertificate(fileId, "pfx");
    }
    private String getCerCertificate(String fileId) {
        return this.getCertificate(fileId, "cer");
    }

    /**
     * generate keystore with pfx using Java's security module
     * @param pfxPath path to pfx file
     * @param password certificate password
     * @return string of path to keystore file
     */
    private String createKeystoreCertificate(String pfxPath, String password) {
        String keystorePath = null;

        try {
            File pfxFile = new File(pfxPath);
            keystorePath = ToolPool.buildFilePath(pfxFile.getParent(), StaticValues.CERTIFICATE_NAME+".keystore");
            File keystoreFile = new File(keystorePath);
            if (!pfxFile.canRead()) {
                throw new Exception("Unable to access input keystore: " + pfxFile.getPath());
            }
            if (keystoreFile.exists() && !keystoreFile.canWrite()) {
                throw new Exception("Output file is not writable: " + keystoreFile.getPath());
            }
            KeyStore kspkcs12 = KeyStore.getInstance("pkcs12");
            KeyStore ksjks = KeyStore.getInstance("jks");
            char[] inphrase = password.toCharArray();
            char[] outphrase = password.toCharArray();
            kspkcs12.load(new FileInputStream(pfxFile), inphrase);
            ksjks.load((keystoreFile.exists()) ? new FileInputStream(keystoreFile) : null, outphrase);
            Enumeration eAliases = kspkcs12.aliases();
            int n = 0;
            List<String> list = new ArrayList<String>();
            if (!eAliases.hasMoreElements()) {
                throw new Exception("certificate validation falied - no alias.");
            }

            while (eAliases.hasMoreElements()) {
                String strAlias = (String) eAliases.nextElement();
                if (kspkcs12.isKeyEntry(strAlias)) {
                    Key key = kspkcs12.getKey(strAlias, inphrase);
                    Certificate[] chain = kspkcs12.getCertificateChain(strAlias);
                    strAlias = StaticValues.CERTIFICATE_ALIAS;
                    ksjks.setKeyEntry(strAlias, key, outphrase, chain);
                    list.add(strAlias);
                }
            }
            OutputStream out = new FileOutputStream(keystoreFile);
            ksjks.store(out, outphrase);
            out.close();
            keystorePath=keystoreFile.getPath();

            //store certificate password in dynamic data table in case cloud instance gets rebootes
            StorageServices storageServices = new StorageServices();
            Info passData = new Info();
            passData.setKey(StaticValues.CONFIG_KEY_CERT_PASSWORD);
            passData.setValue(password);
            storageServices.insertSingleEnttity(StaticValues.TABLE_NAME_INFOS, passData);

            conf.setProperty(StaticValues.CONFIG_KEY_CERT_PASSWORD, password);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return keystorePath;
    }

    /**
     * generate trustcacert with cer file using JRE's keytool
     * @param javaPath path to local jre
     * @param cerPath path to cer file
     * @return string of path to trustcacert file
     */
    private String createTrustCertificate(String javaPath, String cerPath) {
        String trustPath;
        try {
            trustPath = ToolPool.buildFilePath(new File(cerPath).getParent(), StaticValues.CERTIFICATE_NAME+".trustcacerts");
            String[] args = {
                    ToolPool.buildFilePath(javaPath, "bin", "keytool"),
                    "-importcert",  "-trustcacerts", "-noprompt",
                    "-keystore", trustPath,
                    "-storepass", conf.getStringProperty(StaticValues.CONFIG_KEY_CERT_PASSWORD),
                    "-alias", StaticValues.CERTIFICATE_ALIAS,
                    "-file", cerPath
            };
            Process certProcess = Runtime.getRuntime().exec(args);
            certProcess.waitFor();
        } catch(Exception ex) {
            ex.printStackTrace();
            trustPath = null;
        }
        return trustPath;
    }
}

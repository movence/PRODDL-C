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

package pdl.utils.cmd;

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
 * Date: 8/11/11
 * Time: 9:36 AM
 * Online tutorial on creating and converting Windows certificate http://www.windowsazure4j.org/learn/labs/Management/index.html
 */
public class CertificateToolLocal {
    public boolean convert(String pfxFile, String keystoreFile,
                           String inputKey, String outputKey, String alias) throws Exception {
        boolean rtnVal = false;

        try {
            File fileIn = new File(pfxFile);
            File fileOut = new File(keystoreFile);
            if (!fileIn.canRead()) {
                throw new Exception("Unable to access input keystore: " + fileIn.getPath());
            }
            if (fileOut.exists() && !fileOut.canWrite()) {
                throw new Exception("Output file is not writable: " + fileOut.getPath());
            }
            KeyStore kspkcs12 = KeyStore.getInstance("pkcs12");
            KeyStore ksjks = KeyStore.getInstance("jks");
            char[] inphrase = inputKey.toCharArray();
            char[] outphrase = outputKey.toCharArray();
            kspkcs12.load(new FileInputStream(fileIn), inphrase);
            ksjks.load((fileOut.exists()) ? new FileInputStream(fileOut) : null, outphrase);
            Enumeration eAliases = kspkcs12.aliases();
            int n = 0;
            List<String> list = new ArrayList<String>();
            if (!eAliases.hasMoreElements()) {
                throw new Exception("Certificate is not valid. It does not contain any alias.");
            }
            while (eAliases.hasMoreElements()) {
                String strAlias = (String) eAliases.nextElement();
                System.out.println("Alias " + n++ + ": " + strAlias);
                if (kspkcs12.isKeyEntry(strAlias)) {
                    System.out.println("Adding key for alias " + strAlias);
                    Key key = kspkcs12.getKey(strAlias, inphrase);
                    Certificate[] chain = kspkcs12.getCertificateChain(strAlias);

                    if (alias != null)
                        strAlias = alias;

                    ksjks.setKeyEntry(strAlias, key, outphrase, chain);
                    list.add(strAlias);
                }
            }
            OutputStream out = new FileOutputStream(fileOut);
            ksjks.store(out, outphrase);
            out.close();

            rtnVal = true;
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return rtnVal;
    }

    //cmd command for .trustcacerts file
    //keytool -import -trustcacerts -noprompt -keystore C:\Users\hkim\Documents\Backups\AZURE\certs\jcviManagement.trustcacerts -storepass managementjcvi -alias jcvimanagement -file C:\Users\hkim\Documents\Backups\AZURE\certs\jcviManagement.cer
}

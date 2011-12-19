/**
 * Copyright  2006-2010 Soyatec
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *
 * $Id$
 */
package org.soyatec.windowsazure.management;

import org.soyatec.windowsazure.authenticate.Base64;
import org.soyatec.windowsazure.blob.io.BlobStream;

/**
 * This class is used to setup the configuration of Upgrade operation.
 * 
 * @author yyang
 *
 */
public class UpgradeConfiguration extends Configuration {
	private UpgradeType mode;
	private String upgradeRole;
	private String deploymentLabel;
	private BlobStream configurationFileStream;

	/**
	 * Construct a new UpgradeConfiguration object.
	 */
	public UpgradeConfiguration() {

	}

	/**
	 * Construct a new UpgradeConfiguration object.
	 */
	public UpgradeConfiguration(String pkgUrl, String configurefileUrl,
			UpgradeType mode, String upgradeRole, String label) {
		this.packageBlobUrl = pkgUrl;
		this.configurationFileUrl = configurefileUrl;
		this.mode = mode;
		this.upgradeRole = upgradeRole;
		this.deploymentLabel = label;
	}
	
	/**
	 * Construct a new UpgradeConfiguration object.
	 */
	public UpgradeConfiguration(String pkgUrl, BlobStream configurationFileStream,
			UpgradeType mode, String upgradeRole, String label) {
		this.packageBlobUrl = pkgUrl;
		this.configurationFileStream = configurationFileStream;
		this.mode = mode;
		this.upgradeRole = upgradeRole;
		this.deploymentLabel = label;
	}

	public BlobStream getConfigurationFileStream() {
		return configurationFileStream;
	}

	public void setConfigurationFileStream(BlobStream configurationFileStream) {
		this.configurationFileStream = configurationFileStream;
	}

	/**
	 * The validate operation is to check whether the deploymentLabel,
	 * packageBlobUrl, configurationFileUrl or upgradeRole is empty, if any of
	 * them is empty, throw a exception.
	 */
	public void validate() {
		if (isEmpty(deploymentLabel)) {
			throw new IllegalStateException("DeploymentLabel is required!");
		}

		if (isEmpty(packageBlobUrl)) {
			throw new IllegalStateException("Package blob url is required!");
		}

		if (isEmpty(configurationFileUrl)) {
			throw new IllegalStateException("Configuration file is required!");
		}

		/*if (isEmpty(upgradeRole)) {
			throw new IllegalStateException("UpgradeRole is required!");
		}*/

		readConfigurationContent();
	}

	/**
	 * @return the label
	 */
	public String getDeploymentLabel() {
		return deploymentLabel;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setDeploymentLabel(String label) {
		this.deploymentLabel = label;
	}

	/**
	 * @return the base64Label
	 */
	public String getBase64Label() {
		return Base64.encode(getDeploymentLabel().getBytes());
	}

	/**
	 * @return the mode of UpgradeConfiguration.
	 */
	public UpgradeType getMode() {
		return mode;
	}

	/**
	 * Set the mode of UpgradeConfiguration.
	 * @param mode
	 */
	public void setMode(UpgradeType mode) {
		this.mode = mode;
	}

	/**
	 * @return the upgradeRole of UpgradeConfiguration.
	 */
	public String getUpgradeRole() {
		return upgradeRole;
	}

	/**
	 * Set the upgradeRole of UpgradeConfiguration.
	 * @param upgradeRole
	 */
	public void setUpgradeRole(String upgradeRole) {
		this.upgradeRole = upgradeRole;
	}

}

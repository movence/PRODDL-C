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
 * This class defines the configuration of a deployment.
 * 
 * @author xiaowei.ye@soyatec.com
 * 
 */
public class DeploymentConfiguration extends Configuration {

	/**
	 *  Required. The name for the deployment. The deployment name must be unique among other deployments for the hosted service.
	 */
	private String name;

	/**
	 * Required. A label for this deployment that is Base64-encoded. The label may be up to 100 characters in length.
	 */
	private String label;
	
	/**
	 * Optional. Indicates whether to start the deployment immediately after it is created. Supported for versions 2010-04-01 and above. The default value is false.
	 */
	private boolean startDeployment;
	
	/**
	 * Optional. Indicates whether to treat package validation warnings as errors. Supported for versions 2010-04-01 and above. The default value is false. If set to true, the Created Deployment operation fails if there are validation warnings on the service package.
	 */
	private boolean treatWarningsAsError;

	private BlobStream configurationFileStream;

	/**
	 * Construct a new DeploymentConfiguration object.
	 */
	public DeploymentConfiguration() {
		startDeployment = false;
		treatWarningsAsError = false;
	}

	/**
	 * Construct a new DeploymentConfiguration object.
	 */
	public DeploymentConfiguration(String name, String pkgUrl,
			String configurefileUrl, String label) {
		this.name = name;
		this.packageBlobUrl = pkgUrl;
		this.configurationFileUrl = configurefileUrl;
		this.label = label;
	}

	/**
	 * Construct a new DeploymentConfiguration object.
	 */
	public DeploymentConfiguration(String name, String pkgUrl,
			BlobStream stream, String label) {
		this.name = name;
		this.packageBlobUrl = pkgUrl;
		this.configurationFileStream = stream;
		this.label = label;
	}

	

	public BlobStream getConfigurationFileStream() {
		return configurationFileStream;
	}

	public void setConfigurationFileStream(BlobStream configurationFileStream) {
		this.configurationFileStream = configurationFileStream;
	}

	/**
	 * Validate whether the name, packageBlobUrl, configurationFileUrl or label
	 * of the DeploymentConfiguration is empty.
	 */
	public void validate() {
		if (isEmpty(name)) {
			throw new IllegalStateException("Name is required!");
		}

		if (isEmpty(packageBlobUrl)) {
			throw new IllegalStateException("Package blob url is required!");
		}

		if (isEmpty(configurationFileUrl)) {
			throw new IllegalStateException("Configuration file is required!");
		}

		if (isEmpty(label)) {
			throw new IllegalStateException("Label is required!");
		}

		readConfigurationContent();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the base64Label
	 */
	public String getBase64Label() {
		return Base64.encode(getLabel().getBytes());
	}

	public void setStartDeployment(boolean startDeployment) {
		this.startDeployment = startDeployment;
	}

	public boolean isStartDeployment() {
		return startDeployment;
	}

	public void setTreatWarningsAsError(boolean treatWarningsAsError) {
		this.treatWarningsAsError = treatWarningsAsError;
	}

	public boolean isTreatWarningsAsError() {
		return treatWarningsAsError;
	}

}

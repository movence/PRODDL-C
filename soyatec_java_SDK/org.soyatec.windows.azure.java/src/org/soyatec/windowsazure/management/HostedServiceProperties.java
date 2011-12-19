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

import java.util.ArrayList;
import java.util.List;

/**
 * This class defines all properties of a HostedService 
 * 
 * @author yyang
 *
 */
public class HostedServiceProperties {
	private String name;
	private String url;
	private String description;
	private String affinityGroup;
	private String location;
	private String label;
	private List<Deployment> deployments;

	/**
	 * Add a deployment to the deployments of HostedServiceProperties
	 * 
	 * @param deploy
	 * 			a deployment
	 */
	public void addDeployment(Deployment deploy) {
		if (deployments == null)
			deployments = new ArrayList<Deployment>();
		deployments.add(deploy);
	}

	/**
	 * @return deployment list of HostedServiceProperties.
	 */
	public List<Deployment> getDeployments() {
		return deployments;
	}

	/**
	 * Set the deployment list of HostedServiceProperties
	 * @param deployments
	 */
	public void setDeployments(List<Deployment> deployments) {
		this.deployments = deployments;
	}

	/**
	 * @return the name of HostedServiceProperties
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of HostedServiceProperties
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the url of HostedServiceProperties
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Set the url of HostedServiceProperties
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the description of HostedServiceProperties
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description of HostedServiceProperties
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the affinityGroup of HostedServiceProperties
	 */
	public String getAffinityGroup() {
		return affinityGroup;
	}

	/**
	 * Set the affinityGroup of HostedServiceProperties
	 * @param affinityGroup
	 */
	public void setAffinityGroup(String affinityGroup) {
		this.affinityGroup = affinityGroup;
	}

	/**
	 * @return the location of HostedServiceProperties
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Set location of HostedServiceProperties
	 * @param location
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the label of HostedServiceProperties
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set the label of HostedServiceProperties
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

}

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
 * This class defines the properties of AffinityGroup
 * 
 * @author yyang
 */
public class AffinityGroupProperties {
	
	private String description;
	
	private String location;
	
	private List<String> hostedServices;
	
	private List<String> storageServices;

	/**
	 * @return return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 * 			the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location
	 * 			the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return hostedServices list
	 */
	public List<String> getHostedServices() {
		return hostedServices;
	}

	/**
	 * @param hostedServices
	 * 			the hostedServices list to set
	 */
	public void setHostedServices(List<String> hostedServices) {
		this.hostedServices = hostedServices;
	}
	
	/**
	 * Add the service to hostedServices.
	 * @param service
	 */
	public void addHostedService(String service){
		if(hostedServices == null)
			hostedServices = new ArrayList<String>();
		hostedServices.add(service);
	}
	
	/**
	 * Add service to storageServices
	 * @param service
	 */
	public void addStorageService(String service){
		if( storageServices == null)
			storageServices = new ArrayList<String>();
		storageServices.add(service);
	}

	/**
	 * @return storageServices
	 */
	public List<String> getStorageServices() {
		return storageServices;
	}

	/**
	 * @param storageServices
	 * 			the storageServices list to set
	 */
	public void setStorageServices(List<String> storageServices) {
		this.storageServices = storageServices;
	}
	
	
}

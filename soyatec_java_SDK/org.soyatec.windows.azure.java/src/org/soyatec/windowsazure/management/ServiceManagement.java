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

import java.util.List;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.soyatec.windowsazure.internal.util.ssl.SslUtil;
import org.soyatec.windowsazure.proxy.AbstractProxyDelegate;

/**
 * The class is to manage storage accounts and hosted services, service
 * deployments, and affinity groups.
 */
public abstract class ServiceManagement extends AbstractProxyDelegate{

	private String endPointHost = null;
	private final String subscriptionId;
	private final SSLSocketFactory sslSocketFactory;

	private boolean blocking = false;

	/**
	 * Construct a new ServiceManagement object with a subscription id, store
	 * file key, store password key, trust store file, trust store password,
	 * certificate alias and endPointHost.
	 */
	public ServiceManagement(String subscriptionId, String keyStoreFile,
			String keyStorePassword, String trustStoreFile,
			String trustStorePassword, String certificateAlias,
			String endPointHost) throws Exception {
		this.subscriptionId = subscriptionId;
		this.endPointHost = endPointHost;
		this.sslSocketFactory = SslUtil.createSSLSocketFactory(keyStoreFile,
				keyStorePassword, trustStoreFile, trustStorePassword,
				certificateAlias);
	}

	/**
	 * @return true: is blocking / false: not blocking
	 */
	public boolean isBlocking() {
		return blocking;
	}

	/**
	 * Set true: is blocking / false: not blocking
	 * 
	 * @param blocking
	 */
	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

	/**
	 * @return the subscriptionId
	 */
	public String getSubscriptionId() {
		return subscriptionId;
	}

	/**
	 * @return the sslSocketFactory
	 */
	SSLSocketFactory getSslSocketFactory() {
		return sslSocketFactory;
	}

	/**
	 * @return the base url.
	 */
	String getBaseUrl() {
		if (this.endPointHost == null)
			return "https://management.core.windows.net:443/" + subscriptionId;
		else
			return "https://" + this.endPointHost + ":443/" + subscriptionId;

	}

	/**
	 * List the hosted services.
	 * 
	 * @return hostedServices
	 */
	public abstract List<HostedService> listHostedServices();

	/**
	 * The Create Deployment operation uploads a new service package and creates
	 * a new deployment on staging or production.
	 * 
	 * </p>
	 * 
	 * Note that it is possible to call Create Deployment only for a hosted
	 * service that has previously been created via the Windows Azure Developer
	 * Portal. You cannot upload a new hosted service via the Service Management
	 * API.
	 * 
	 * </p>
	 * 
	 * The Create Deployment operation is an asynchronous operation. To
	 * determine whether the management service has finished processing the
	 * request, call Get Operation Status.
	 * 
	 * @param serviceName
	 * @param deploySlotName
	 * @param define
	 * @return
	 */
	public abstract String createDeployment(String serviceName,
			DeploymentSlotType deploySlotName, DeploymentConfiguration define,
			AsyncResultCallback callback);

	/**
	 * The Get Deployment operation get the deployment with the service name and
	 * deploymentSlotType.
	 * 
	 * @param serviceName
	 * @param type
	 * @return
	 */
	public abstract Deployment getDeployment(String serviceName,
			DeploymentSlotType type);

	/**
	 * The Get Deployment operation get the deployment with the service name and
	 * deployment name.
	 * 
	 * @param serviceName
	 * @param deploymentName
	 * @return
	 */
	public abstract Deployment getDeployment(String serviceName,
			String deploymentName);

	/**
	 * The Delete Deployment operation Delete the deployment with the service
	 * name, deploymentSlotType and asyncResultCallback.
	 * 
	 * @param serviceName
	 * @param type
	 * @param callback
	 * @return
	 */
	public abstract String deleteDeployment(String serviceName,
			DeploymentSlotType type, AsyncResultCallback callback);

	/**
	 * The Delete Deployment operation Delete the deployment with the service
	 * name, deployment name and asyncResultCallback.
	 * 
	 * @param serviceName
	 * @param deploymentName
	 * @param callback
	 * @return
	 */
	public abstract String deleteDeployment(String serviceName,
			String deploymentName, AsyncResultCallback callback);

	/**
	 * The Get Service Properties operation Get the hosted service properties
	 * with the service name and embedDetail(true/false).
	 * 
	 * @param serviceName
	 *            The name of your hosted service.
	 * @param embedDetail
	 *            When the embedDetail parameter is specified, the management
	 *            service returns properties for all deployments of the service,
	 *            as well as for the service itself. The default value is false.
	 * @return
	 */
	public abstract HostedServiceProperties getHostedServiceProperties(
			String serviceName, boolean embedDetail);

	/**
	 * The Get Operation Status Get the operation status with request id.
	 * 
	 * @param requestId
	 * @return
	 */
	public abstract OperationStatus getOperationStatus(String requestId);

	/**
	 * The List Storage Accounts operation lists the storage accounts available
	 * under the current subscription.
	 * 
	 * @return
	 */
	public abstract List<StorageAccount> listStorageAccounts();

	/**
	 * The List Affinity Accounts operation lists the affinity groups available
	 * under the current subscription.
	 * 
	 * @return
	 */
	public abstract List<AffinityGroup> listAffinityGroups();

	/**
	 * The Get Storage Account Keys operation lists the storage account keys for
	 * the specified storage account.
	 * 
	 * @param serviceName
	 * @return
	 */
	public abstract StorageAccountKey getStorageAccountKeys(String serviceName);

	/**
	 * The Regenerate Keys operation regenerates the primary or secondary access
	 * key for the specified storage account.
	 * 
	 * @param serviceName
	 * @param type
	 * @return
	 */
	public abstract StorageAccountKey regenerateKeys(String serviceName,
			KeyType type);

	/**
	 * The Get Storage Account Properties operation get the storage account
	 * properties.
	 * 
	 * @param serviceName
	 * @param type
	 * @return
	 */
	public abstract StorageAccountProperties getStorageAccountProperties(
			String serviceName);

	/**
	 * The Get Affinity Group Properties operation get the Affinity Group
	 * Properties with group name.
	 * 
	 * @param groupName
	 * @return
	 */
	public abstract AffinityGroupProperties getAffinityGroupProperties(
			String groupName);

	/**
	 * The Update Deployment Status operation is update the status of the
	 * Deployment.
	 * 
	 * @param serviceName
	 * @param type
	 * @param status
	 * @param callback
	 * @return
	 */
	public abstract String updateDeplymentStatus(String serviceName,
			DeploymentSlotType type, UpdateStatus status,
			AsyncResultCallback callback);

	/**
	 * The Update Deployment Status operation is update the status of the
	 * Deployment.
	 * 
	 * @param serviceName
	 * @param deploymentName
	 * @param status
	 * @param callback
	 * @return
	 */
	public abstract String updateDeplymentStatus(String serviceName,
			String deploymentName, UpdateStatus status,
			AsyncResultCallback callback);

	/**
	 * The Upgrade Deployment operation initiates an upgrade. The Upgrade
	 * Deployment operation is an asynchronous operation. To determine whether
	 * the Management service has finished processing the request, call Get
	 * Operation Status.
	 * 
	 * @param serviceName
	 * @param type
	 * @param configuration
	 * @param callback
	 * @return
	 */
	public abstract String upgradeDeployment(String serviceName,
			DeploymentSlotType type, UpgradeConfiguration configuration,
			AsyncResultCallback callback);

	/**
	 * The Upgrade Deployment operation initiates an upgrade. The Upgrade
	 * Deployment operation is an asynchronous operation. To determine whether
	 * the Management service has finished processing the request, call Get
	 * Operation Status.
	 * 
	 * @param serviceName
	 * @param deploymentName
	 * @param configuration
	 * @param callback
	 * @return
	 */
	public abstract String upgradeDeployment(String serviceName,
			String deploymentName, UpgradeConfiguration configuration,
			AsyncResultCallback callback);

	/**
	 * The Walk Upgrade Domain operation is walk upgrade the deployment.
	 * 
	 * @param serviceName
	 * @param deploymentName
	 * @param domainId
	 *            An integer value that identifies the upgrade domain to walk.
	 *            Upgrade domains are identified with a zero-based index: the
	 *            first upgrade domain has an ID of 0, the second has an ID of
	 *            1, and so on.
	 * @param callback
	 * @return
	 */
	public abstract String walkUpgradeDomain(String serviceName,
			String deploymentName, int domainId, AsyncResultCallback callback);

	/**
	 * The Walk Upgrade Domain operation is walk upgrade the deployment.
	 * 
	 * @param serviceName
	 * @param type
	 * @param domainId
	 *            An integer value that identifies the upgrade domain to walk.
	 *            Upgrade domains are identified with a zero-based index: the
	 *            first upgrade domain has an ID of 0, the second has an ID of
	 *            1, and so on.
	 * @param callback
	 * @return
	 */
	public abstract String walkUpgradeDomain(String serviceName,
			DeploymentSlotType type, int domainId, AsyncResultCallback callback);

	/**
	 * The Change Deployment Configuration operation is to change the
	 * configuration of the Deployment.
	 * 
	 * @param serviceName
	 * @param type
	 * @param configurationFileUrl
	 * @param callback
	 * @return
	 */
	public abstract String changeDeploymentConfiguration(String serviceName,
			DeploymentSlotType type, String configurationFileUrl,
			AsyncResultCallback callback);

	/**
	 * The Change Deployment Configuration operation is to change the
	 * configuration of the Deployment.
	 * 
	 * @param serviceName
	 * @param deploymentName
	 * @param configurationFileUrl
	 * @param callback
	 * @return
	 */
	public abstract String changeDeploymentConfiguration(String serviceName,
			String deploymentName, String configurationFileUrl,
			AsyncResultCallback callback);

	/**
	 * The Swap Deployment operation is to swap deployment.
	 * 
	 * @param serviceName
	 * @param productName
	 * @param sourceName
	 * @param callback
	 * @return
	 */
	public abstract String swapDeployment(String serviceName,
			String productName, String sourceName, AsyncResultCallback callback);

	/**
	 * The List Certificates operation lists all certificates associated with
	 * the specified hosted service.
	 * 
	 * @param serviceName
	 * @return
	 */
	public abstract List<Certificate> listCertificates(String serviceName);

	/**
	 * The Get Certificate operation returns the public data for the specified
	 * certificate.
	 * 
	 * @param serviceName
	 * @param thumbprintAlgorithm
	 *            the algorithm for the certificate's thumbprint
	 * @param thumbprint
	 *            the hexadecimal representation of the thumbprint
	 * @return
	 */
	public abstract Certificate getCertificate(String serviceName,
			String thumbprintAlgorithm, String thumbprint);

	/**
	 * The Delete Certificate operation deletes a certificate from the
	 * subscription's certificate store. see
	 * http://msdn.microsoft.com/en-us/library/ee460803.aspx
	 * 
	 * @param serviceName
	 * @param thumbprintAlgorithm
	 *            the algorithm for the certificate's thumbprint
	 * @param thumbprint
	 *            the hexadecimal representation of the thumbprint
	 * @return
	 */
	public abstract void deleteCertificate(String serviceName,
			String thumbprintAlgorithm, String thumbprint);

	/**
	 * The Add Certificate operation adds a certificate to the subscription.
	 * http://msdn.microsoft.com/en-us/library/ee460817.aspx
	 * 
	 * @param serviceName
	 * @param data
	 * @param format
	 * @param password
	 */
	public abstract void addCertificate(String serviceName, byte[] data,
			CertificateFormat format, String password);

	/**
	 * The List OS Versions operation lists the versions of the guest operating
	 * system that are currently available in Windows Azure.
	 * http://msdn.microsoft.com/en-us/library/ff684168.aspx
	 */
	public abstract List<OperatingSystem> listOSVersions();

	/**
	 * Set the interval in milliseconds for polling operation status in
	 * asynchronous operations.
	 * 
	 * @param interval
	 *            the interval in milliseconds for polling operation status
	 */
	public abstract void setPollStatusInterval(int interval);

	/**
	 * Get the interval in milliseconds for polling operation status in
	 * asynchronous operations.
	 * 
	 * @return the interval in milliseconds for polling operation status
	 */
	public abstract int getPollStatusInterval();

	/**
	 * List Operating System Families
	 * 
	 * @return a list of OperatingSystemFamily
	 */
	public abstract List<OperatingSystemFamily> listOSFamilies();

	/**
	 * List all of the data center locations that are valid for your
	 * subscription.
	 * 
	 * @return a list of locations
	 */
	public abstract List<Location> listLocations();

	/**
	 * The Create Hosted Service operation creates a new hosted service in
	 * Windows Azure.
	 * 
	 * @param serviceName
	 *            Required. A name for the hosted service that is unique to the
	 *            subscription. It is also used as the prefix of service URL.
	 * @param label
	 *            A label for the hosted service. The label may be up to 100
	 *            characters in length.
	 * @param description
	 *            A description for the hosted service. The description may be
	 *            up to 1024 characters in length.
	 * @param location
	 *            The location where the hosted service will be created. To list
	 *            available locations, use the listLocations operation.
	 * @param affinityGroup
	 *            The name of an existing affinity group associated with this
	 *            subscription. To list available affinity groups, use the
	 *            listAffinityGroups operation.
	 */
	public abstract void createHostedService(String serviceName, String label,
			String description, String location, String affinityGroup);

	/**
	 * The Delete Hosted Service operation deletes the specified hosted service
	 * from Windows Azure.
	 * 
	 * @param serviceName
	 *            The name of your hosted service.
	 */
	public abstract void deleteHostedService(String serviceName);

	/**
	 * The Update Hosted Service operation updates the label and/or the
	 * description for a hosted service in Windows Azure.
	 * 
	 * @param serviceName
	 *            The name of your hosted service.
	 * @param label
	 *            A label for the hosted service. The label may be up to 100
	 *            characters in length.
	 * @param description
	 *            A description for the hosted service. The description may be
	 *            up to 1024 characters in length.
	 * 
	 */
	public abstract void updateHostedService(String serviceName, String label,
			String description);

	/**
	 * The Reboot Role Instance operation requests a reboot of a role instance
	 * that is running in a deployment. The Reboot Role Instance operation is an
	 * asynchronous operation.
	 * 
	 * @param serviceName
	 * @param deploySlotName
	 * @param roleInstanceName
	 * @param callback
	 * @return
	 */
	public abstract String rebootRoleInstance(String serviceName,
			DeploymentSlotType deploySlotName, String roleInstanceName,
			AsyncResultCallback callback);

	/**
	 * The Reboot Role Instance operation requests a reboot of a role instance
	 * that is running in a deployment. The Reboot Role Instance operation is an
	 * asynchronous operation.
	 * 
	 * @param serviceName
	 * @param deploymentName
	 * @param roleInstanceName
	 * @param callback
	 * @return
	 */
	public abstract String rebootRoleInstance(String serviceName,
			String deploymentName, String roleInstanceName,
			AsyncResultCallback callback);

	/**
	 * The Reimage Role Instance operation requests a reimage of a role instance
	 * that is running in a deployment. The Reimage Role Instance operation is
	 * an asynchronous operation.
	 * 
	 * @param serviceName
	 * @param deploySlotName
	 * @param roleInstanceName
	 * @param callback
	 * @return
	 */
	public abstract String reimageRoleInstance(String serviceName,
			DeploymentSlotType deploySlotName, String roleInstanceName,
			AsyncResultCallback callback);

}

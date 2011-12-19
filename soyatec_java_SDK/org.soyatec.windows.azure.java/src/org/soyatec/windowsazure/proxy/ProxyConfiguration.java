package org.soyatec.windowsazure.proxy;

/**
 * The http proxy configuration information used for HttpClient.
 * 
 */
public class ProxyConfiguration {

	private String proxyHost = null;

	private int proxyPort = -1;

	private String proxyUsername = null;

	private String proxyDomainname = null;

	private String proxyPassword = null;

	public ProxyConfiguration() {

	}

	public ProxyConfiguration(String proxyHost, int proxyPort) {
		this(proxyHost, proxyPort, null, null);
	}

	public ProxyConfiguration(String proxyHost, int proxyPort,
			String proxyUsername, String proxyPassword) {
		this(proxyHost, proxyPort, proxyUsername, proxyPassword, null);
	}

	public ProxyConfiguration(String proxyHost, int proxyPort,
			String proxyUsername, String proxyPassword, String proxyDomainname) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.proxyUsername = proxyUsername;
		this.proxyPassword = proxyPassword;
		this.proxyDomainname = proxyDomainname;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyUsername() {
		return proxyUsername;
	}

	public void setProxyUsername(String proxyUsername) {
		this.proxyUsername = proxyUsername;
	}

	public String getProxyDomainname() {
		return proxyDomainname;
	}

	public void setProxyDomainname(String proxyDomainname) {
		this.proxyDomainname = proxyDomainname;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

}

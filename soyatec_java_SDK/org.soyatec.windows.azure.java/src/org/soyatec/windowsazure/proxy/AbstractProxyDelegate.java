package org.soyatec.windowsazure.proxy;

import org.soyatec.windowsazure.internal.util.HttpUtilities;

public abstract class AbstractProxyDelegate {
	
	public boolean isProxyEnabled() {
		return HttpUtilities.proxyExists();
	}

	public void disableProxy() {
		HttpUtilities.removeProxyConfig();
	}

	public void setProxyConfiguration(ProxyConfiguration proxy) {
		HttpUtilities.setProxy(proxy);
	}
}

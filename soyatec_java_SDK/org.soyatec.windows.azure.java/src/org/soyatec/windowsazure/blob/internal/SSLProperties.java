package org.soyatec.windowsazure.blob.internal;

public final class SSLProperties {
	private static String keyStore;
	private static String keyStorePasswd;
	private static String trustStore;
	private static String trustStorePasswd;
	private static String keyAlias;
	private static boolean ssl = false;
	
	public static synchronized void setSSLSettings(String _keystore,
			String _keystorePasswd, 
			String _truststore, 
			String _truststorepasswd, 
			String _keyalias) {
		keyStore = _keystore;
		keyStorePasswd = _keystorePasswd;
		trustStore = _truststore;
		trustStorePasswd = _truststorepasswd;
		keyAlias = _keyalias;
		ssl = true;
	}
	
	public static synchronized void clearSSLSettings() {
		keyStore = "";
		keyStorePasswd = "";
		trustStore = "";
		trustStorePasswd = "";
		keyAlias = "";
		ssl = false;
	}
	
	public static synchronized String getKeyStore() {
		return keyStore;
	}
	public static synchronized String getKeyStorePasswd() {
		return keyStorePasswd;
	}
	public static synchronized String getTrustStore() {
		return trustStore;
	}
	public static synchronized String getTrustStorePasswd() {
		return trustStorePasswd;
	}
	public static synchronized String getKeyAlias() {
		return keyAlias;
	}
	public static synchronized boolean isSSL() {
		return ssl;
	}
}

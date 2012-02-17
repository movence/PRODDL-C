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
package org.soyatec.windowsazure.internal.util.ssl;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;

/**
 * Instances of this class manage which X509 certificate-based key pairs are
 * used to authenticate the local side of a secure socket.
 */
public class HttpsX509KeyManager implements X509KeyManager {

	private final X509ExtendedKeyManager manager;
	private final String certAlias;
	private final PrivateKey privateKey;
	private final X509Certificate certificate;

	/**
	 * Constructor for HttpsX509KeyManager.
	 */
	public HttpsX509KeyManager(X509ExtendedKeyManager x509ExtendedKeyManager,
			String certAlias, PrivateKey privateKey, X509Certificate cert) {
		this.manager = x509ExtendedKeyManager;
		this.certAlias = certAlias;
		this.privateKey = privateKey;
		this.certificate = cert;
	}

	/**
	 * Choose an alias to authenticate the client side of a secure socket given
	 * the public key type and the list of certificate issuer authorities
	 * recognized by the peer (if any).
	 * 
	 * @param keyType
	 *            the key algorithm type name(s), ordered with the
	 *            most-preferred key type first.
	 * @param issuers
	 *            the list of acceptable CA issuer subject names or null if it
	 *            does not matter which issuers are used.
	 * @param socket
	 *            the socket to be used for this connection. This parameter can
	 *            be null, which indicates that implementations are free to
	 *            select an alias applicable to any socket.
	 * @return the alias name for the desired key, or null if there are no
	 *         matches.
	 */
	public String chooseClientAlias(String[] keyType, Principal[] issuers,
			Socket socket) {
		return certAlias;
	}

	/**
	 * Choose an alias to authenticate the client side of an SSLEngine
	 * connection given the public key type and the list of certificate issuer
	 * authorities recognized by the peer (if any). The default implementation
	 * returns null.
	 * 
	 * @param arg0
	 *            the key algorithm type name(s), ordered with the
	 *            most-preferred key type first.
	 * @param arg1
	 *            the list of acceptable CA issuer subject names or null if it
	 *            does not matter which issuers are used.
	 * @param arg2
	 *            the SSLEngine to be used for this connection. This parameter
	 *            can be null, which indicates that implementations of this
	 *            interface are free to select an alias applicable to any
	 *            engine.
	 * @return the alias name for the desired key, or null if there are no
	 *         matches.
	 */
	public String chooseEngineClientAlias(String[] arg0, Principal[] arg1,
			SSLEngine arg2) {
		return certAlias;
	}

	/**
	 * Choose an alias to authenticate the server side of an SSLEngine
	 * connection given the public key type and the list of certificate issuer
	 * authorities recognized by the peer (if any). The default implementation
	 * returns null.
	 * 
	 * @param arg0
	 *            the key algorithm type name
	 * @param arg1
	 *            the list of acceptable CA issuer subject names or null if it
	 *            does not matter which issuers are used.
	 * @param arg2
	 *            the SSLEngine to be used for this connection. This parameter
	 *            can be null, which indicates that implementations of this
	 *            interface are free to select an alias applicable to any
	 *            engine.
	 * @return the alias name for the desired key, or null if there are no
	 *         matches.
	 */
	public String chooseEngineServerAlias(String arg0, Principal[] arg1,
			SSLEngine arg2) {
		return manager.chooseEngineServerAlias(arg0, arg1, arg2);
	}

	/**
	 * Choose an alias to authenticate the server side of a secure socket given
	 * the public key type and the list of certificate issuer authorities
	 * recognized by the peer (if any).
	 * 
	 * @param keyType
	 *            the key algorithm type name
	 * @param issuers
	 *            the list of acceptable CA issuer subject names or null if it
	 *            does not matter which issuers are used.
	 * @param socket
	 *            the socket to be used for this connection. This parameter can
	 *            be null, which indicates that implementations are free to
	 *            select an alias applicable to any socket.
	 * @return the alias name for the desired key, or null if there are no
	 *         matches.
	 */
	public String chooseServerAlias(String keyType, Principal[] issuers,
			Socket socket) {
		return manager.chooseServerAlias(keyType, issuers, socket);
	}

	/**
	 * Returns the certificate chain associated with the given alias.
	 * 
	 * @param alias
	 *            the alias name
	 * @return the certificate chain (ordered with the user's certificate first
	 *         and the root certificate authority last), or null if the alias
	 *         can't be found.
	 */
	public X509Certificate[] getCertificateChain(String alias) {

		return new X509Certificate[] { certificate };
	}

	/**
	 * Get the matching aliases for authenticating the client side of a secure
	 * socket given the public key type and the list of certificate issuer
	 * authorities recognized by the peer (if any).
	 * 
	 * @param keyType
	 *            the key algorithm type name
	 * @param issuers
	 *            the list of acceptable CA issuer subject names, or null if it
	 *            does not matter which issuers are used.
	 * @return an array of the matching alias names, or null if there were no
	 *         matches.
	 */
	public String[] getClientAliases(String keyType, Principal[] issuers) {
		return manager.getClientAliases(keyType, issuers);
	}

	/**
	 * Returns the key associated with the given alias.
	 * 
	 * @param alias
	 *            the alias name
	 * @return the requested key, or null if the alias can't be found.
	 */
	public PrivateKey getPrivateKey(String alias) {
		if (alias.equals(this.certAlias))
			return privateKey;
		else
			return manager.getPrivateKey(alias);
	}

	/**
	 * Get the matching aliases for authenticating the server side of a secure
	 * socket given the public key type and the list of certificate issuer
	 * authorities recognized by the peer (if any).
	 * 
	 * @param keyType
	 *            the key algorithm type name
	 * @param issuers
	 *            the list of acceptable CA issuer subject names or null if it
	 *            does not matter which issuers are used.
	 * @return an array of the matching alias names, or null if there were no
	 *         matches.
	 */
	public String[] getServerAliases(String keyType, Principal[] issuers) {
		return manager.getServerAliases(keyType, issuers);
	}
}

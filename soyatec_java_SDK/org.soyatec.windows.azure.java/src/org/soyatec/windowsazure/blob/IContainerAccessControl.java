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
package org.soyatec.windowsazure.blob;

import java.util.List;

import org.soyatec.windowsazure.authenticate.ISignedIdentifier;
import org.soyatec.windowsazure.blob.internal.ContainerAccessControl;
import org.soyatec.windowsazure.internal.SignedIdentifier;

/**
 * 
 * The <code>ContainerAccessControl</code> class represents the access control for
 * containers.
 */
public interface IContainerAccessControl {
	/**
	 * The private containerAccessControl.
	 */
	public static final ContainerAccessControl Private = new ContainerAccessControl(
			false, false);
	/**
	 * The public containerAccessControl.
	 */
	public static final ContainerAccessControl Public = new ContainerAccessControl(
			true, false);

	/**
	 * 
	 * @return true: is public / false: not public
	 */
	public boolean isPublic();

	/**
	 * Add signedIdentifier.
	 * @param id
	 * @return this
	 */
	public IContainerAccessControl addSignedIdentifier(SignedIdentifier id);

	/**
	 * @return the size of sigendIdentifiers
	 */
	public int getSize();

	/**
	 * Get the signedIdentifier of the index.
	 * @param index
	 * @return signedIdentifier
	 */
	public ISignedIdentifier getSignedIdentifier(int index);

	/**
	 * Get the list of signedIdentifier.
	 * @return signedIdentifier list
	 */
	public List<SignedIdentifier> getSigendIdentifiers();

	/**
	 * @param sigendIdentifiers
	 *            the sigendIdentifiers to set
	 */
	public void setSigendIdentifiers(List<SignedIdentifier> sigendIdentifiers);

}
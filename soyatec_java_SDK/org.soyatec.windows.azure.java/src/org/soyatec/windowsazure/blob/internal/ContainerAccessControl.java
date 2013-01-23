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
package org.soyatec.windowsazure.blob.internal;

import java.util.ArrayList;
import java.util.List;

import org.soyatec.windowsazure.authenticate.ISignedIdentifier;
import org.soyatec.windowsazure.blob.IContainerAccessControl;
import org.soyatec.windowsazure.internal.SignedIdentifier;

/**
 * 
 * The <code>ContainerAccessControl</code> class is the access control for
 * containers.
 */
public class ContainerAccessControl implements IContainerAccessControl {

	private boolean isPublic;
	private boolean isMutable;

	private List<SignedIdentifier> sigendIdentifiers;

	/**
	 * Construct a new ContainerAccessControl object with isPublic.
	 * 
	 * @param isPublic
	 */
	public ContainerAccessControl(boolean isPublic) {
		this(isPublic, true);
	}

	public ContainerAccessControl(boolean isPublic, boolean isMutable) {
		this.isPublic = isPublic;
		this.isMutable = isMutable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.soyatec.windowsazure.blob.IContainerAccessControl#isPublic()
	 */
	public boolean isPublic() {
		return isPublic;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.soyatec.windowsazure.blob.IContainerAccessControl#addSignedIdentifier
	 * (org.soyatec.windowsazure.authenticate.SignedIdentifier)
	 */
	public IContainerAccessControl addSignedIdentifier(SignedIdentifier id) {
		if (!isMutable)
			throw new IllegalStateException(
					"The built-in instance is not mutable! Please create a new instance by yourself.");
		getSigendIdentifiers().add(id);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.soyatec.windowsazure.blob.IContainerAccessControl#getSize()
	 */
	public int getSize() {
		return getSigendIdentifiers().size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.soyatec.windowsazure.blob.IContainerAccessControl#getSignedIdentifier
	 * (int)
	 */
	public ISignedIdentifier getSignedIdentifier(int index) {
		List<SignedIdentifier> list = getSigendIdentifiers();
		if (index > list.size())
			return null;
		return list.get(index);
	}

	/**
	 * @return the sigendIdentifiers
	 */
	public List<SignedIdentifier> getSigendIdentifiers() {
		if (sigendIdentifiers == null) {
			sigendIdentifiers = new ArrayList<SignedIdentifier>();
		}
		return sigendIdentifiers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.soyatec.windowsazure.blob.IContainerAccessControl#setSigendIdentifiers
	 * (java.util.List)
	 */
	public void setSigendIdentifiers(List<SignedIdentifier> sigendIdentifiers) {
		if (!isMutable)
			throw new IllegalStateException(
					"The built-in instance is not mutable! Please create a new instance by yourself.");
		this.sigendIdentifiers = sigendIdentifiers;
	}

}

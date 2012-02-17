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

/**
 *
 *  The <code>PageRange</code> class represents the pageRange of a container.
 *
 */
public interface IPageRange {

	/**
	 * 
	 * @return the startOffset
	 */
	public abstract int getStartOffset();

	/**
	 * 
	 * @param startOffset
	 *          the startOffset to set
	 */
	public abstract void setStartOffset(int startOffset);

	/**
	 * 
	 * @return the endOffset
	 */
	public abstract int getEndOffset();

	/**
	 * 
	 * @param endOffset
	 *          the endOffset to set
	 */
	public abstract void setEndOffset(int endOffset);

	public abstract int length();

}
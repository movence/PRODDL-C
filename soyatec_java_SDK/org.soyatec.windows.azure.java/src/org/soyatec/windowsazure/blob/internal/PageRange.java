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

import org.soyatec.windowsazure.blob.IPageRange;

public class PageRange implements IPageRange {
	
	private int startOffset;
	private int endOffset;

	public PageRange() {
		super();
	}
	
	public PageRange(int startOffset, int endOffset) {
		super();
		this.startOffset = startOffset;
		this.endOffset = endOffset;
	}
	
	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IPageRange#getStartOffset()
	 */
	public int getStartOffset() {
		return startOffset;
	}
	
	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IPageRange#setStartOffset(int)
	 */
	public void setStartOffset(int startOffset) {
		this.startOffset = startOffset;
	}
	
	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IPageRange#getEndOffset()
	 */
	public int getEndOffset() {
		return endOffset;
	}
	
	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IPageRange#setEndOffset(int)
	 */
	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IPageRange#length()
	 */
	public int length(){
		return endOffset - startOffset + 1;
	}
	
	@Override
	public String toString() {		
		return "bytes=" + startOffset + "-" + endOffset; 
	}
}

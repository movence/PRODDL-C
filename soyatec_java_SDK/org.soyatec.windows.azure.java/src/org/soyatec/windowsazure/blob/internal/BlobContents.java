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

import java.io.IOException;
import java.io.InputStream;

import org.soyatec.windowsazure.blob.IBlobContents;
import org.soyatec.windowsazure.blob.io.BlobMemoryStream;
import org.soyatec.windowsazure.blob.io.BlobStream;

/// The contents of the Blob in various forms.
public class BlobContents implements IBlobContents {
	
	private BlobStream stream;
	private byte[] bytes;

    public BlobContents(BlobStream stream)
    {
        this.stream = stream;
    }
    
    /**
     * 
     * @param stream
     * @throws IOException
     */
    public BlobContents(InputStream stream) throws IOException
    {
        this.stream = new BlobMemoryStream(stream);
    }

    /**
     * Construct a new BlobContents object from a byte array.
     */
    public BlobContents(byte[] value)
    {
        this.bytes = value;
        this.stream = new BlobMemoryStream(value);
    }

    /* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobContents#getStream()
	 */
    public BlobStream getStream() {
		return stream;
	}

    /* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.IBlobContents#getBytes()
	 */
	public byte[] getBytes() throws IOException {
		if (bytes == null && stream != null) {
			return stream.getBytes();
		}
		return bytes;
	}

}

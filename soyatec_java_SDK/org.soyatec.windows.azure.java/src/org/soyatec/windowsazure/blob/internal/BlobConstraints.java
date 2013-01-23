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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.message.BasicHeader;
import org.soyatec.windowsazure.blob.IBlobConstraints;
import org.soyatec.windowsazure.internal.constants.HeaderNames;

/**
 * The <code>BlobConstraints</code> class specifies constraints for blob that
 * are used in the <code>BlobContainer</code> class.
 * 
 */
public class BlobConstraints implements IBlobConstraints {

	/**
	 * The list of all constraints to be used.
	 */
	private List<BasicHeader> constraints;

	/**
	 * Creates a <code>BlobConstraints</code> object.
	 */
	private BlobConstraints() {
		constraints = new ArrayList<BasicHeader>();
	}

	/**
	 * Creates a new BlobConstraint instance.
	 * 
	 * @return BlobConstraint instance
	 */
	public static IBlobConstraints newInstance() {
		return new BlobConstraints();
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.internal.IBlobConstraints#getConstraints()
	 */
	public List<BasicHeader> getConstraints() {
		return constraints;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.internal.IBlobConstraints#setConstraints(java.util.List)
	 */
	public void setConstraints(List<BasicHeader> constraints) {
		this.constraints = constraints;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.internal.IBlobConstraints#isSourceModifiedSince(java.sql.Timestamp)
	 */
	public IBlobConstraints isSourceModifiedSince(Timestamp time) {
		constraints.add(new BasicHeader(HeaderNames.IfSourceModifiedSince,
				formatTime(time)));
		return this;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.internal.IBlobConstraints#isSourceUnmodifiedSince(java.sql.Timestamp)
	 */
	public IBlobConstraints isSourceUnmodifiedSince(Timestamp time) {
		constraints.add(new BasicHeader(HeaderNames.IfSourceUnmodifiedSince,
				formatTime(time)));
		return this;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.internal.IBlobConstraints#isSourceMatch(java.lang.String)
	 */
	public IBlobConstraints isSourceMatch(String etag) {
		constraints.add(new BasicHeader(HeaderNames.IfSourceMatch, etag));
		return this;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.internal.IBlobConstraints#isSourceNoneMatch(java.lang.String)
	 */
	public IBlobConstraints isSourceNoneMatch(String etag) {
		constraints.add(new BasicHeader(HeaderNames.IfSourceNoneMatch, etag));
		return this;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.internal.IBlobConstraints#isDestModifiedSince(java.sql.Timestamp)
	 */
	public IBlobConstraints isDestinationModifiedSince(Timestamp time) {
		constraints.add(new BasicHeader(HeaderNames.IfModifiedSince,
				formatTime(time)));
		return this;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.internal.IBlobConstraints#isDestUnmodifiedSince(java.sql.Timestamp)
	 */
	public IBlobConstraints isDestinationUnmodifiedSince(Timestamp time) {
		constraints.add(new BasicHeader(HeaderNames.IfUnmodifiedSince,
				formatTime(time)));
		return this;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.internal.IBlobConstraints#isDestMatch(java.lang.String)
	 */
	public IBlobConstraints isDestinationMatch(String etag) {
		constraints.add(new BasicHeader(HeaderNames.IfMatch, etag));
		return this;
	}

	/* (non-Javadoc)
	 * @see org.soyatec.windowsazure.blob.internal.IBlobConstraints#isDestNoneMatch(java.lang.String)
	 */
	public IBlobConstraints isDestinationNoneMatch(String etag) {
		constraints.add(new BasicHeader(HeaderNames.IfNoneMatch, etag));
		return this;
	}

	/**
	 * Formats a time into a date/time string with GMT.
	 * 
	 * @param time
	 * @return the formatted time string
	 */
	private String formatTime(Timestamp time) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss", Locale.US);
		return formatter.format(time) + " GMT";
	}
}

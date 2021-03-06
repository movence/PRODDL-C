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
package org.soyatec.windowsazure.table;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import org.soyatec.windowsazure.internal.util.Logger;

/**
 * A <strong>GUID</strong> is a 128-bit integer (16 bytes) that can be used
 * across all computers and networks wherever a unique identifier is required.
 * Such an identifier has a very low probability of being duplicated. </p>
 * 
 * Example: c9c108d3-371d-c3fa-b189-687657445da3
 * 
 * http://msdn.microsoft.com/en-us/library/96ff78dc.aspx
 * A string that contains a GUID in one of the following formats ("d" represents a hexadecimal digit whose case is ignored): 
 * 32 contiguous digits:
 * dddddddddddddddddddddddddddddddd
 * -or-
 * Groups of 8, 4, 4, 4, and 12 digits with hyphens between the groups. The entire GUID can optionally be enclosed in matching braces or parentheses:
 * dddddddd-dddd-dddd-dddd-dddddddddddd
 * -or-
 * {dddddddd-dddd-dddd-dddd-dddddddddddd}
 * -or-
 * (dddddddd-dddd-dddd-dddd-dddddddddddd) 
 */
public class Guid {

	private String valueBeforeMD5 = "";
	private String valueAfterMD5 = "";
	private static Random myRand;
	private static SecureRandom mySecureRand;

	private static String s_id;

	private String value;

	/*
	 * Static block to take care of one time secureRandom seed. It takes a few
	 * seconds to initialize SecureRandom. You might want to consider removing
	 * this static block or replacing it with a "time since first loaded" seed
	 * to reduce this time. This block will run only once per JVM instance.
	 */

	static {
		mySecureRand = new SecureRandom();
		long secureInitializer = mySecureRand.nextLong();
		myRand = new Random(secureInitializer);
		try {
			s_id = InetAddress.getLocalHost().toString();
		} catch (UnknownHostException e) {
			Logger.error("", e);
		}
	}

	/**
	 * 
	 * Default constructor. With no specification of security option, this
	 * constructor defaults to lower security, high performance.
	 */
	public Guid() {
		getRandomGUID(false);
	}

	/**
	 * Constructor with security option. Setting secure true enables each random
	 * number generated to be cryptographically strong. Secure false defaults to
	 * the standard Random function seeded with a single cryptographically
	 * strong random number.
	 */
	public Guid(boolean secure) {
		getRandomGUID(secure);
	}

	/*
	 * Method to generate the random GUID
	 */
	private void getRandomGUID(boolean secure) {
		MessageDigest md5 = null;
		StringBuffer sbValueBeforeMD5 = new StringBuffer();

		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			Logger.error("NoSuchAlgorithmException", e);
		}

		try {
			long time = System.currentTimeMillis();
			long rand = 0;

			if (secure) {
				rand = mySecureRand.nextLong();
			} else {
				rand = myRand.nextLong();
			}

			// This StringBuffer can be a long as you need; the MD5
			// hash will always return 128 bits. You can change
			// the seed to include anything you want here.
			// You could even stream a file through the MD5 making
			// the odds of guessing it at least as great as that
			// of guessing the contents of the file!

			sbValueBeforeMD5.append(s_id);
			sbValueBeforeMD5.append(":");
			sbValueBeforeMD5.append(Long.toString(time));
			sbValueBeforeMD5.append(":");
			sbValueBeforeMD5.append(Long.toString(rand));

			valueBeforeMD5 = sbValueBeforeMD5.toString();
			md5.update(valueBeforeMD5.getBytes());

			byte[] array = md5.digest();
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < array.length; ++j) {
				int b = array[j] & 0xFF;
				if (b < 0x10)
					sb.append('0');
				sb.append(Integer.toHexString(b));
			}

			valueAfterMD5 = sb.toString();

		} catch (Exception e) {
			Logger.error("Error:", e);
		}
	}

	/*
	 * Convert to the standard format for GUID (Useful for SQL Server
	 * UniqueIdentifiers, etc.) Example: C2FEEEAC-CFCD-11D1-8B05-00600806D9B6
	 */
	public String toString() {
		return getValue();
	}

	private String computeValue() {
		String raw = valueAfterMD5.toLowerCase();
		StringBuffer sb = new StringBuffer();
		sb.append(raw.substring(0, 8));
		sb.append("-");
		sb.append(raw.substring(8, 12));
		sb.append("-");
		sb.append(raw.substring(12, 16));
		sb.append("-");
		sb.append(raw.substring(16, 20));
		sb.append("-");
		sb.append(raw.substring(20));
		String currentValue = sb.toString().toLowerCase();
		if (value == null || !value.equals(currentValue)) {
			value = currentValue;
		}
		return currentValue;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		if (value != null)
			return value;
		computeValue();
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getValue() == null) ? 0 : getValue().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Guid other = (Guid) obj;
		return getValue().equals(other.getValue());
	}

}
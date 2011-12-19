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
package org.soyatec.windowsazure.internal.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.soyatec.windowsazure.authenticate.Base64;
import org.soyatec.windowsazure.blob.io.BlobStream;
import org.soyatec.windowsazure.internal.constants.RegularExpressionStrings;
import org.soyatec.windowsazure.table.internal.TableStorageConstants;

/**
 * Utilities for deal with string and time.
 * 
 */
public class Utilities {

	private static final String ERROR_MESSAGE = "<message (.*)>(.*)<\\/message>";

	private static final Pattern PATTERN = Pattern.compile(ERROR_MESSAGE, Pattern.MULTILINE|Pattern.DOTALL);
	 
	private static final String YYYY_MM_DD_T_HH_MM_SS_SSS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	private static final String YYYY_MM_DD_T_HH_MM_SS_SSS_DETAIL_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS";

	private static final String EEE_DD_MMM_YYYY_HH_MM_SS_Z_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";

	private static final String TIME_ZONE_GMT = "GMT";

	private static final String MIN_TIME = "0000-00-00 00:00:00.000000000";

	/**
	 * Check whether the string is null or empty.
	 * 
	 * @param str
	 * @return true: the string is null or empty./ false: the string is not null
	 *         and empty.
	 */
	public static boolean isNullOrEmpty(String str) {
		return (str == null || str.trim().length() == 0);
	}

	/**
	 * @return a empty string.
	 */
	public static String emptyString() {
		return "";
	}

	public static String encodeQuery(String query) {
		// http://msdn.microsoft.com/en-us/library/dd894031.aspx
		// The following characters must be encoded if they are to be used in a
		// query string:
		// Forward slash (/)
		// Question mark (?)
		// Colon (:)
		// 'At' symbol (@)
		// Ampersand (&)
		// Equals sign (=)
		// Plus sign (+)
		// Comma (,)
		// Dollar sign ($)
		StringBuilder sb = new StringBuilder(query.length() * 2);
		for (int i = 0; i < query.length(); i++) {
			char ch = query.charAt(i);
			switch (ch) {
			case '\\':
			case '/':
			case '?':
			case ':':
			case '@':
			case '&':
			case '=':
			case '+':
			case ',':
			case '$':
				sb.append(URLEncoder.encode("" + ch));
				break;
			default:
				sb.append(ch);
			}
		}

		return sb.toString();
	}

	
	/**
	 * Encode the string use java.net.URLEncoder except the chars:
	 * "=",",","/","(","'",")","\\$","\\%20"
	 * 
	 * @param input
	 * @return the encoded string.
	 */
	public static String encode(String input) {
		try {
			input = java.net.URLEncoder.encode(input, "UTF-8");
			input = input.replaceAll("%3D", "=");
			input = input.replaceAll("%2C", ",");
			input = input.replaceAll("%2F", "/");
			input = input.replaceAll("%28", "(");
			input = input.replaceAll("%27", "'");
			input = input.replaceAll("%29", ")");
			input = input.replaceAll("%24", "\\$"); // %24
			input = input.replaceAll("\\+", "\\%20"); // %24
		} catch (UnsupportedEncodingException e) {
			Logger.error(e.getMessage(), e);
		}
		return input;
	}

	/**
	 * The value of this constant is equivalent to 0000-00-00
	 * 00:00:00.000000000, January 1, 0001.
	 * 
	 * @return Timestamp
	 */
	public static Timestamp minTime() {
		// yyyy-mm-dd hh:mm:ss[.fffffffff]
		return Timestamp.valueOf(MIN_TIME);
	}

	/**
	 * Check is valid table name.
	 * 
	 * @param name
	 * @return true: is valid table name/ false: not valid table name.
	 */
	public static boolean isValidTableName(String name) {
		if (isNullOrEmpty(name)) {
			return false;
		}
		Pattern pattern = Pattern
				.compile(RegularExpressionStrings.ValidTableNameRegex);
		return pattern.matcher(name).matches();
	}

	/**
	 * Replace the "\\$root/" and "\\$root" with empty in the string.
	 * 
	 * @param url
	 * @return the string after be fixed.
	 */
	public static String fixRootContainer(String url) {
		if (url.indexOf("$root") > -1) {
			int len = url.length();
			url = url.replaceAll("\\$root/", "");
			if (url.length() == len) {
				// unchanged
				url = url.replaceAll("\\$root", "");
			}
		}
		return url;
	}

	/**
	 * Check is valid container name or queue name.
	 * 
	 * @param name
	 * @return true or false
	 */
	public static boolean isValidContainerOrQueueName(String name) {
		if (isNullOrEmpty(name)) {
			return false;
		}
		Pattern pattern = Pattern
				.compile(RegularExpressionStrings.ValidContainerNameRegex);
		return pattern.matcher(name).matches();
	}

	/**
	 * Convert the string to Timestamp.
	 * 
	 * @param time
	 * @return Timestamp
	 */
	public static Timestamp convertTime(String time) {
		SimpleDateFormat formatter = (SimpleDateFormat) DateFormat
				.getDateTimeInstance(DateFormat.MONTH_FIELD, DateFormat.LONG,
						Locale.US);
		formatter.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_GMT));
		Date date = null;
		try {
			date = formatter.parse(time);

		} catch (ParseException e) {
			date = new Date();
		}
		return new Timestamp(date.getTime());
	}

	/**
	 * @return UTC time
	 */
	public static String getUTCTime() {
		SimpleDateFormat formatter = (SimpleDateFormat) DateFormat
				.getDateTimeInstance(DateFormat.MONTH_FIELD, DateFormat.LONG,
						Locale.US);
		formatter.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_GMT));
		formatter.applyPattern(EEE_DD_MMM_YYYY_HH_MM_SS_Z_FORMAT);
		return formatter.format(new Date());
	}

	/**
	 * Copy the sourceStream to destinationStream.
	 * 
	 * @param sourceStream
	 *            The copy-from stream.
	 * @param destinationStream
	 *            The copy-to stream.
	 * @return destinationStream
	 * @throws IOException
	 */
	public static long copyStream(InputStream sourceStream,
			BlobStream destinationStream) throws IOException {
		final int bufferSize = 0x10000;
		byte[] buffer = new byte[bufferSize];
		int n = 0;
		long totalRead = 0;

		try {
			do {
				n = sourceStream.read(buffer, 0, bufferSize);
				if (n > 0) {
					totalRead += n;
					destinationStream.write(buffer, 0, n);
				}
			} while (n > 0);
		} catch (IOException e) {
			Logger.error(e.getMessage(), e);
			sourceStream.close();
			destinationStream.close();
			throw e;
		}
		return totalRead;
	}

	/**
	 * Copy the sourceStream to destinationStream.
	 * 
	 * @param sourceStream
	 *            The copy-from stream.
	 * @param destinationStream
	 *            The copy-to stream.
	 * @param length
	 *            The copy length.
	 * @throws IOException
	 */
	public static void copyStream(BlobStream sourceStream,
			BlobStream destinationStream, int length) throws IOException {

		final int bufferSize = 0x10000;
		byte[] buffer = new byte[bufferSize];
		int n = 0;
		int amountLeft = length;

		try {
			do {
				amountLeft -= n;
				n = sourceStream.read(buffer, 0,
						Math.min(bufferSize, amountLeft));
				if (n > 0) {
					destinationStream.write(buffer, 0, n);
				}
			} while (n > 0);

			Arrays.fill(buffer, (byte) 0);
			while (amountLeft > 0) {
				amountLeft -= n;
				n = Math.min(bufferSize, amountLeft);
				if (n > 0)
					destinationStream.write(buffer, 0, n);
			}
		} catch (IOException e) {
			Logger.error(e.getMessage(), e);
			sourceStream.close();
			destinationStream.close();
			throw e;
		} 
	}

	/**
	 * Try to get the date time from Http string.
	 * 
	 * @param stringValue
	 * @return java.sql.Timestamp
	 * @throws ParseException
	 */
	public static Timestamp tryGetDateTimeFromHttpString(String stringValue)
			throws ParseException {
		SimpleDateFormat formatter = (SimpleDateFormat) DateFormat
				.getDateTimeInstance(DateFormat.MONTH_FIELD, DateFormat.LONG,
						Locale.US);
		formatter.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_GMT));
		formatter.applyPattern(EEE_DD_MMM_YYYY_HH_MM_SS_Z_FORMAT);
		Date date = formatter.parse(stringValue);
		return new Timestamp(date.getTime());
	}

	/**
	 * Parse time string like 2009-03-11T20:25:15.9334924Z or
	 * 2009-06-01T06:12:45Z
	 * 
	 * @param stringValue
	 * @return java.sql.Timestamp
	 * @throws ParseException
	 */
	public static Timestamp tryGetDateTimeFromTableEntry(String stringValue)
			throws ParseException {
		SimpleDateFormat formatter = (SimpleDateFormat) DateFormat
				.getDateTimeInstance(DateFormat.MONTH_FIELD, DateFormat.LONG,
						Locale.US);
		formatter.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_GMT));
		// 2009-03-11T20:25:15.9334924Z
		Date date = null;
		try {

			formatter.applyPattern(YYYY_MM_DD_T_HH_MM_SS_SSS_FORMAT);
			date = formatter.parse(stringValue);
		} catch (Exception e) {
			formatter.setLenient(true);
			formatter.applyPattern(YYYY_MM_DD_T_HH_MM_SS_SSS_DETAIL_FORMAT);
			date = formatter.parse(stringValue);
		}
		return new Timestamp(date.getTime());

	}

	/**
	 * Check the string parameter.
	 * 
	 * @param s
	 *            the check string.
	 * @param canBeNullOrEmpty
	 *            true: can be null or empty/ can not be null or empty.
	 * @param name
	 */
	public static void checkStringParameter(String s, boolean canBeNullOrEmpty,
			String name) {
		if (isNullOrEmpty(s) && !canBeNullOrEmpty) {
			throw new IllegalArgumentException(MessageFormat.format(
					"The parameter {0} cannot be null or empty.", name));
		}
		if (s.length() > TableStorageConstants.MaxStringPropertySizeInChars) {
			throw new IllegalArgumentException(MessageFormat.format(
					"The parameter {0} cannot be longer than {1} characters.",
					name, TableStorageConstants.MaxStringPropertySizeInChars));
		}
	}

	/**
	 * Get the TimeStamp like 2009-06-01T06:12:45Z
	 * @return Currently TimeStamp like 2009-06-01T06:12:45Z
	 */
	public static String getTimestamp() {
		return formatTimeStamp(new Timestamp(new Date().getTime()));
	}

	/**
	 * Format the TimeStamp like 2009-06-01T06:12:45Z
	 * @param t
	 *        TimeStamp
	 * @return formatted TimeStamp
	 */
	public static String formatTimeStamp(Timestamp t) {
		SimpleDateFormat formatter = (SimpleDateFormat) DateFormat
				.getDateTimeInstance(DateFormat.MONTH_FIELD, DateFormat.LONG,
						Locale.US);
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		formatter.applyPattern("yyyy-MM-dd");
		String timestamp = formatter.format(t);
		formatter.applyPattern("hh:mm:ss");
		return timestamp + "T" + formatter.format(t) + "Z";
	}

	/**
	 * Format the TimeStamp like 2009-06-01 06:12:45 
	 * @param s
	 *        TimeStamp string
	 * @return java.sql.Timestamp
	 */
	public static Timestamp formatStringToTimeStamp(String s) {
		return Timestamp.valueOf(s.replace("T", " ").replace("Z", ""));
	}

	/**
	 * Convert object to string,return a string
	 * @param object
	 *        an object
	 * @return string
	 * @throws IOException
	 */
	public static String convertObjectToString(Object object)
			throws IOException {
		// if (object instanceof java.io.Serializable) {
		// throw new IllegalArgumentException(object
		// + " is not support Serializable. See java.io.Serializable");
		// }
		String string;
		ByteArrayOutputStream byteStream = null;
		ObjectOutputStream out = null;
		try {
			byteStream = new ByteArrayOutputStream();
			out = new ObjectOutputStream(byteStream);
			out.writeObject(object);
			out.flush();
			string = Base64.encode(byteStream.toByteArray());
		} catch (IOException e) {
			throw e;
		} finally {
			out.close();
			byteStream.close();
		}
		return string;
	}

	/**
	 * Convert string to object,return an object
	 * @param string
	 *        string containing Base64 data
	 * @return object
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object convertStringToObject(String string)
			throws IOException, ClassNotFoundException {
		if (string == null) {
			throw new IllegalArgumentException("Null String");
		}
		byte[] decode = Base64.decode(string);
		if (decode == null) {
			throw new IOException(MessageFormat.format(
					"Base64 decode for {0} running error.", string));
		}
		ObjectInputStream ois = null;
		Object readObject;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(decode));
			readObject = ois.readObject();
		} catch (IOException e) {
			throw e;
		} finally {
			ois.close();
		}
		return readObject;
	}

	/**
	 * Compute MD5 by given string
	 * @param str
	 * @return MD5
	 */
	public static String computeMD5(String str) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(str.getBytes());
			byte[] array = m.digest();
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < array.length; ++j) {
				int b = array[j] & 0xFF;
				if (b < 0x10) {
					sb.append('0');
				}
				sb.append(Integer.toHexString(b));
			}

			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Retrieve error messages
	 * @param content
	 *        The string to be matched
	 * @return string ,if the content is null or the content length is 0,it will be return null
	 */
	public static String retrieveErrorMessages(String content) {
		if (isNullOrEmpty(content)) {
			return null;
		}
		Matcher matcher = PATTERN.matcher(content);
		StringBuffer buf = new StringBuffer();
		while (matcher.find()) {
			buf.append(matcher.group(2));
		}
		if (buf.length() == 0) {
			return null;
		} else {
			return buf.toString();
		}

		// return content.substring(matcher.start(), matcher.end());
	}

//	public static void main(String[] args) {
//		//String content	= "--batchresponse_fd5a1752-7518-4966-8428-86d7371092ff\nContent-Type: multipart/mixed; boundary=changesetresponse_ad1c3651-234f-47e7-bb1b-7588dd33419b\n\n--changesetresponse_ad1c3651-234f-47e7-bb1b-7588dd33419b\nContent-Type: application/http\nContent-Transfer-Encoding: binary\n\nHTTP/1.1 400 Bad Request\nContent-ID: 2\nContent-Type: application/xml\nCache-Control: no-cache\nDataServiceVersion: 1.0;\n\n<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\n<error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">\n  <code>InvalidInput</code>\n  <message xml:lang=\"en-US\">1:One of the request inputs is not valid.\nRequestId:0cf93bfe-df3d-4a16-aa48-444979360123\nTime:2010-12-31T04:31:38.0574899Z</message>\n</error>\n--changesetresponse_ad1c3651-234f-47e7-bb1b-7588dd33419b--\n--batchresponse_fd5a1752-7518-4966-8428-86d7371092ff--\n" ;
//		
//		String content = "<message xml:lang=\"en-US\">1:One of the request inputs is not valid.\nRequestId:0cf93bfe-df3d-4a16-aa48-444979360123Time:2010-12-31T04:31:38.0574899Z</message>";
//		System.out.println(retrieveErrorMessages(content));
//	}
	/**
	 * Get bytes from the given file,the file's length can not large than Integer.MAX_VALUE
	 * @param file
	 *        the file to be opened for reading.
	 * @return byte[]
	 * @throws IOException
	 */
	public static byte[] getBytesFromFile(File file) throws IOException  {
		InputStream is = new FileInputStream(file);
		long length = file.length();
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		try {
			while (offset < bytes.length
					&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}

			// Ensure all the bytes have been read in
			if (offset < bytes.length) {
				throw new IOException("Could not completely read file "
						+ file.getName());
			}
		} catch (IOException e) {
			throw e;
		} finally {
			// Close the input stream and return bytes
			is.close();
		}
		return bytes;
	}

	/**
	 * To convert the stream to string
	 * @param is
	 *        the InputStream
	 * @return string
	 */
	public static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	/**
	 * Convert Iterator to list
	 * @param <T>
	 * @param itor
	 *       An Iterator contains specify data
	 * @return list contains specify data
	 */
	public static <T> List<T> convertToList(Iterator<T> itor) {
		if (itor == null)
			return Collections.EMPTY_LIST;
		List<T> list = new ArrayList<T>();
		while (itor.hasNext()) {
			list.add(itor.next());
		}
		return list;
	}
	
	/**
	 * Get bytes from the given url
	 * @param urlStr
	 *        the url to be opened for reading.
	 * @return  byte[]
	 */
	public static byte[] getBytesFromUrl(String urlStr) throws IOException {
		URL url;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e1) {
			try {
				url = new URL("file:" + urlStr);
			} catch (MalformedURLException e) {
				throw e;
			}
		}
		return getBytesFromStream(url.openStream());
	}
	
	/**
	 * Get bytes from the given InputStream
	 * @param is
	 *        the InputStream to be readed.
	 * @return byte[]
	 */
	public static byte[] getBytesFromStream(InputStream is) {
		byte[] bts = null;
		final int bufferSize = 0x10000;
		byte[] buffer = new byte[bufferSize];
		ByteArrayOutputStream bas = new ByteArrayOutputStream();
		try {
			int num;
			while ((num = is.read(buffer)) >= 0) {
				bas.write(buffer, 0, num);
			}
			bts = bas.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				bas.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bts;
	}
	
}

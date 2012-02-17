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
package org.soyatec.windowsazure.internal.constants;

public final class HeaderNames {
	public static final String PrefixForStorageProperties = "x-ms-prop-";
	public static final String PrefixForMetadata = "x-ms-meta-";
	public static final String PrefixForStorageHeader = "x-ms-";
	public static final String PrefixForTableContinuation = "x-ms-continuation-";
	public static final String ApiVersion = "x-ms-version";
	public static final String CopySource = "x-ms-copy-source";

	public static final String IfSourceModifiedSince = "x-ms-source-if-modified-since";
	public static final String IfSourceUnmodifiedSince = "x-ms-source-if-unmodified-since";

	public static final String IfSourceMatch = "x-ms-source-if-match";
	public static final String IfSourceNoneMatch = "x-ms-source-if-none-match";
	public static final String ContentID = "Content-ID";
	//
	// Standard headers...
	//
	public static final String ContentLanguage = "Content-Language";
	public static final String ContentLength = "Content-Length";
	public static final String ContentType = "Content-Type";
	public static final String ContentEncoding = "Content-Encoding";
	public static final String ContentMD5 = "Content-MD5";
	public static final String BlobContentMD5 = "x-ms-blob-content-md5";
	public static final String ContentRange = "Content-Range";
	public static final String Sotimeout = "So-Timeout";
	public static final String LastModifiedTime = "Last-Modified";
	public static final String Server = "Server";
	public static final String Allow = "Allow";
	public static final String ETag = "ETag";
	public static final String Range = "Range";
	public static final String Date = "Date";
	public static final String Authorization = "Authorization";
	public static final String IfModifiedSince = "If-Modified-Since";
	public static final String IfUnmodifiedSince = "If-Unmodified-Since";
	public static final String IfMatch = "If-Match";
	public static final String IfNoneMatch = "If-None-Match";
	public static final String IfRange = "If-Range";
	public static final String NextPartitionKey = "NextPartitionKey";
	public static final String NextRowKey = "NextRowKey";
	public static final String NextTableName = "NextTableName";

	//
	// Storage specific custom headers...
	//
	public static final String StorageDateTime = PrefixForStorageHeader
			+ "date";
	public static final String PublicAccess = PrefixForStorageProperties
			+ "publicaccess";
	public static final String StorageRange = PrefixForStorageHeader + "range";

	public static final String CreationTime = PrefixForStorageProperties
			+ "creation-time";
	public static final String ForceUpdate = PrefixForStorageHeader
			+ "force-update";
	public static final String ApproximateMessagesCount = PrefixForStorageHeader
			+ "approximate-messages-count";

	public static final String TableStorageNextTableName = PrefixForStorageHeader
			+ "continuation-NextTableName";
	public static final String TableStorageNextTablePartitionKey = PrefixForStorageHeader
			+ "continuation-NextPartitionKey";
	public static final String TableStorageNextTableRowKey = PrefixForStorageHeader
			+ "continuation-NextRowKey ";

	
	// lease blob
	public static final String LeaseId = "x-ms-lease-id";
	
	public static final String LeaseTime = "x-ms-lease-time";
	
	public static final String LeaseAction = "x-ms-lease-action"; 
	
	public static final String LeaseStatus = "x-ms-lease-status";
	
	public static final String BlobType = "x-ms-blob-type";
	
	public static final String BlobContentType = "x-ms-blob-content-type";
	
	public static final String BlobContentEncoding = "x-ms-blob-content-encoding";
	
	public static final String BlobContentLanguage = "x-ms-blob-content-language";
	
	public static final String BlobContentMd5 = "x-ms-blob-content-md5";
	
	public static final String BlobCacheControl = "x-ms-blob-cache-control";
		
	public static final String BlobRange = "x-ms-range";
	
	
	// page blob
	public static final String BlobContentLength = "x-ms-blob-content-length";
	
	public static final String PageWrite = "x-ms-page-write";
	
	
	
	//Set for page blobs only. The sequence number is a user-controlled value that you can use to track requests. The value of the sequence number must be between 0 and 2^63 - 1.The default value is 0.
	public static final String BlobSequenceNumber = "x-ms-blob-sequence-number";
	
	public static final String IfSequenceNumberLt = "x-ms-if-sequence-number-lt";
	
	public static final String IfSequenceNumberEq = "x-ms-if-sequence-number-eq";
	
	public static final String Snapshot = "x-ms-snapshot";
		
	/*
	 * management
	 */
	public static final String ManagementRequestId = PrefixForStorageHeader
			+ "request-id";
}

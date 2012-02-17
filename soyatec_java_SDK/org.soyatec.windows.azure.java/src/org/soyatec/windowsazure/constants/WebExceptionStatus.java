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
package org.soyatec.windowsazure.constants;

/**
 * 
 * The class contains the web exception status.
 *
 */
public enum WebExceptionStatus {
	/*
	 * No error was encountered.
	 */
	Success,
	/*
	 * The name resolver service could not resolve the host name.
	 */
	NameResolutionFailure,
	/*
	 * The remote service point could not be contacted at the transport level.
	 */
	ConnectFailure,
	/*
	 * A complete response was not received from the remote server.
	 */
	ReceiveFailure,
	/*
	 * A complete request could not be sent to the remote server.
	 */
	SendFailure,
	/*
	 * The request was a piplined request and the connection was closed before
	 * the response was received.
	 */
	PipelineFailure,
	/*
	 * The request was canceled, the System.Net.WebRequest.Abort() method was
	 * called, or an unclassifiable error occurred. This is the default value
	 * for System.Net.WebException.Status.
	 */
	RequestCanceled,
	/*
	 * The response received from the server was complete but indicated a
	 * protocol-level error. For example, an HTTP protocol error such as 401
	 * Access Denied would use this status.
	 */
	ProtocolError,
	/*
	 * The connection was prematurely closed.
	 */
	ConnectionClosed,
	/*
	 * A server certificate could not be validated.
	 */
	TrustFailure,
	/*
	 * An error occurred while establishing a connection using SSL.
	 */
	SecureChannelFailure,
	/*
	 * The server response was not a valid HTTP response.
	 */
	ServerProtocolViolation,
	/*
	 * The connection for a request that specifies the Keep-alive header was
	 * closed unexpectedly.
	 */
	KeepAliveFailure,
	/*
	 * An internal asynchronous request is pending.
	 */
	Pending,
	/*
	 * No response was received during the time-out period for a request.
	 */
	Timeout,
	/*
	 * The name resolver service could not resolve the proxy host name.
	 */
	ProxyNameResolutionFailure,
	/*
	 * An exception of unknown type has occurred.
	 */
	UnknownError,
	/*
	 * A message was received that exceeded the specified limit when sending a
	 * request or receiving a response from the server.
	 */
	MessageLengthLimitExceeded,
	/*
	 * The specified cache entry was not found.
	 */
	CacheEntryNotFound,
	/*
	 * The request was not permitted by the cache policy. In general, this
	 * occurs when a request is not cacheable and the effective policy prohibits
	 * sending the request to the server. You might receive this status if a
	 * request method implies the presence of a request body, a request method
	 * requires direct interaction with the server, or a request contains a
	 * conditional header.
	 */
	RequestProhibitedByCachePolicy,
	/*
	 * This request was not permitted by the proxy.
	 */
	RequestProhibitedByProxy
}

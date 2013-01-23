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
package org.soyatec.windowsazure.table.internal;

import java.io.InputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.dom4j.Document;
import org.dom4j.Element;
import org.soyatec.windowsazure.authenticate.Base64;
import org.soyatec.windowsazure.authenticate.SharedKeyCredentials;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.internal.OutParameter;
import org.soyatec.windowsazure.internal.ResourceUriComponents;
import org.soyatec.windowsazure.internal.constants.HeaderNames;
import org.soyatec.windowsazure.internal.constants.HttpMethod;
import org.soyatec.windowsazure.internal.constants.HttpWebResponse;
import org.soyatec.windowsazure.internal.constants.QueryParams;
import org.soyatec.windowsazure.internal.util.HttpUtilities;
import org.soyatec.windowsazure.internal.util.NameValueCollection;
import org.soyatec.windowsazure.internal.util.Utilities;
import org.soyatec.windowsazure.internal.util.xml.XPathQueryHelper;
import org.soyatec.windowsazure.internal.util.xml.XmlUtil;
import org.soyatec.windowsazure.table.ITable;
import org.soyatec.windowsazure.table.TableServiceContext;
import org.soyatec.windowsazure.table.TableStorageClient;

/**
 * Rest implementation of {@link TableStorageClient}.
 *
 */
@SuppressWarnings("unchecked")
public class TableStorageRest extends TableStorageClient {

	public TableStorageRest(URI baseUri, boolean usePathStyleUris,
			String accountName, String base64Key) {
		super(baseUri, usePathStyleUris, accountName, base64Key);
		byte[] key = null;
		setBase64Key(base64Key);
		if (base64Key != null) {
			key = Base64.decode(getBase64Key());
		}
		setCredentials(new SharedKeyCredentials(accountName, key));
	}

	/**
	 * Lists all the tables within the account
	 */
	@Override
	public List<String> listTables() {
		ListTableResult all = new ListTableResult(new ArrayList<String>(),
				Utilities.emptyString());
		String marker = Utilities.emptyString();
		do {
			ListTableResult partResult = listTablesImpl(marker);
			marker = partResult.getNextTableName();
			all.getNames().addAll(partResult.getNames());
			all.setNextTableName(marker);
		} while (marker != null);

		return all.getNames();
	}

	/**
	 * Lists the table within the account
	 *
	 * @param nextTableName
	 *            Table prefix
	 * @return List of table names
	 */
	private ListTableResult listTablesImpl(final String nextTableName) {

		final OutParameter<ListTableResult> result = new OutParameter<ListTableResult>();
		getRetryPolicy().execute(new Callable<Object>() {
			public Object call() throws Exception {
				NameValueCollection queryParams = new NameValueCollection();
				if (!Utilities.isNullOrEmpty(nextTableName)) {
					queryParams.put(QueryParams.QueryParamTableNextName,
							nextTableName);
				}
				ResourceUriComponents uriComponents = new ResourceUriComponents(
						getAccountName(), TableStorageConstants.TablesQuery,
						null);
				URI uri = HttpUtilities.createRequestUri(getBaseUri(),
						isUsePathStyleUris(), getAccountName(),
						TableStorageConstants.TablesQuery, null, getTimeout(),
						queryParams, uriComponents);

				HttpRequest request = HttpUtilities.createHttpRequest(uri,
						HttpMethod.Get);

				getCredentials().signRequestForSharedKeyLite(request,
						uriComponents);

				HttpWebResponse response = HttpUtilities.getResponse(request);
				if (response.getStatusCode() == HttpStatus.SC_OK) {
					ListTableResult tablesResult = getTableResultFromResponse(response
							.getStream());
					result.setValue(tablesResult);
					String nextTableNameHeader = response
							.getHeader(HeaderNames.TableStorageNextTableName);
					if (!Utilities.isNullOrEmpty(nextTableNameHeader)) {
						tablesResult.setNextTableName(nextTableNameHeader);
					}
					response.close();
				} else {
					HttpUtilities.processUnexpectedStatusCode(response);
				}
				return null;
			}
		});
		return result.getValue();
	}

	private ListTableResult getTableResultFromResponse(InputStream stream) {
		List<String> names = new ArrayList<String>();
		String nextMarker = null;

		Document document = XmlUtil.load(stream,
				"The result of a ListTable operation could not be parsed");
		// get queue names and urls
		List xmlNodes = XPathQueryHelper.parseEntryFromFeed(document);
		for (Iterator iterator = xmlNodes.iterator(); iterator.hasNext();) {
			Element tableEntryNode = (Element) iterator.next();
			String queueName = XPathQueryHelper
					.loadTableNameFromTableEntry(tableEntryNode);
			names.add(queueName);
		}
		// Get the nextMarker
		Element nextMarkerNode = (Element) document
				.selectSingleNode(XPathQueryHelper.NextMarkerQuery);
		// the -enableassertions when run.
		if (nextMarkerNode != null && nextMarkerNode.hasContent()) {
			nextMarker = nextMarkerNode.getStringValue();
		}

		return new ListTableResult(names, nextMarker);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.soyatec.windowsazure.table.TableStorageClient#getTableReference(java.lang
	 * .String)
	 */
	@Override
	public ITable getTableReference(String tableName) {
		Utilities.checkStringParameter(tableName, false, "tableName");
		if (!Utilities.isValidTableName(tableName)) {
			throw new IllegalArgumentException(
					MessageFormat
							.format(
									"The specified table name \"{0}\" is not valid!"
											+ "\nPlease choose a name that conforms to the naming conventions for tables!"
											+ "\nSee <a>http://msdn.microsoft.com/en-us/library/dd179338.aspx</a> for more information.",
									tableName));
		}
		return new CloudTableRest(getBaseUri(), isUsePathStyleUris(),
				getAccountName(), tableName, getBase64Key(), getTimeout(),
				getRetryPolicy());
	}

	@Override
	public boolean isTableExist(String tableName) throws StorageException {
		ITable table = getTableReference(tableName);
		return table.isTableExist();
	}

	@Override
	public void createTable(String tableName){
		ITable table = getTableReference(tableName);
		boolean createTable = table.createTable();
		if(!createTable){
			throw new StorageException("Create table failed.");
		}
	}

	@Override
	public boolean createTableIfNotExist(String tableName){
		ITable table = getTableReference(tableName);
		boolean createTable = table.createTable();
		return createTable;
	}

	@Override
	public boolean deleteTableIfExist(String tableName) {
		ITable table = getTableReference(tableName);
		return table.deleteTable();
	}

	@Override
	public void deleteTable(String tableName){
		ITable table = getTableReference(tableName);
		if(!table.deleteTable()){
			throw new StorageException("Delete table failed.");
		}
	}

	@Override
	public List<String> listTables(String prefix){
		List<String> listTables = listTables();
		List<String> listTablesWithPrefix = new ArrayList<String>();
		for(String listTable:listTables){
			if(listTable.startsWith(prefix)){
				listTablesWithPrefix.add(listTable);
			}
		}
		return listTablesWithPrefix;
	}

}

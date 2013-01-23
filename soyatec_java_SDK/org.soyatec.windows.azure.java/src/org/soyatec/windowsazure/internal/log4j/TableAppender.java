package org.soyatec.windowsazure.internal.log4j;

import java.net.URI;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.table.AbstractTableServiceEntity;
import org.soyatec.windowsazure.table.ITable;
import org.soyatec.windowsazure.table.TableStorageClient;

public class TableAppender extends AppenderSkeleton {
	public static final String DEV_STORE_ACCOUNT = "devstoreaccount1";

	public static final String DEV_STORE_KEY = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";

	public static final URI DEV_TABLE_URI = URI
			.create("http://127.0.0.1:10002");

	public final static URI CLOUD_TABLE_URI = URI
			.create("http://table.core.windows.net");

	private String accountName = DEV_STORE_ACCOUNT;
	private String accountKey = DEV_STORE_KEY;
	private String tableName = "WADLogsTable";

	private String pattern = "%-4r %-5p %d{yyyy-MM-dd HH:mm:ss} %c %m%n";

	private TableStorageClient storage;

	private ITable table;

	private String endPoint = null;

	public String getEndPoint() {
		return this.endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getAccountKey() {
		return accountKey;
	}

	public void setAccountKey(String accountKey) {
		this.accountKey = accountKey;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Derived appenders should override this method if option structure
	 * requires it.
	 */
	public void activateOptions() {
		// disable logging for httpclient to avoid infinite loop
		Logger.getLogger("org.apache.http").setLevel(Level.OFF);
		init();

	}

	private void init() {
		if (DEV_STORE_ACCOUNT.equals(accountName)) {
			storage = TableStorageClient.create(DEV_TABLE_URI, true,
					accountName, accountKey);
		} else {
			if (this.endPoint == null) {
				storage = TableStorageClient.create(CLOUD_TABLE_URI, false,
						accountName, accountKey);
			} else {
				String url = this.endPoint.indexOf("://") > -1 ? this.endPoint
						: "http://" + this.endPoint;
				storage = TableStorageClient.create(URI.create(url), false,
						accountName, accountKey);
			}
		}

		if (tableName == null) {
			LogLog.error("TableName is required!");
			return;
		}
		createLogTable();
		if (this.layout == null) {
			LogLog.debug("The layout is not loaded... we set it.");
			this.setLayout(new org.apache.log4j.PatternLayout(pattern));
		}
	}

	private void createLogTable() {
		try {
			tableName = tableName.trim();
			table = storage.getTableReference(tableName);
			table.createTable();
		} catch (Exception e) {
			LogLog.error("Error in createLogTable", e);
		}
	}

	@Override
	protected void append(LoggingEvent event) {
		if (table == null)
			return;
		String msg = event.getLevel().toString() + ": "
				+ this.getLayout().format(event);
		LogEntity entity = new LogEntity(
				"log4j." + event.getLevel().toString(), String.valueOf(System
						.currentTimeMillis()));
		entity.setMessage(msg);

		try {
			table.getTableServiceContext().insertEntity(entity);
		} catch (StorageException e) {
			LogLog.error("Error in append", e);
		}
	}

	public void close() {
	}

	public boolean requiresLayout() {
		return true;
	}

	static class LogEntity extends AbstractTableServiceEntity {

		private String message = "";

		public LogEntity(String partitionKey, String rowKey) {
			super(partitionKey, rowKey);
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

}

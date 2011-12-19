package org.soyatec.windowsazure.table;

/**
 * A table column represents all attributes in a Azure Table in azure table
 * service. Azure table composed by more columns. One column have 3 attributes:
 * 
 * <ul>
 * <li><strong>name</strong> Column name</li>
 * <li><strong>value</strong> Column value</li>
 * <li><strong>type</strong> Data type of the column. For more about see
 * {@link ETableColumnType}</li>
 * </ul>
 * @author yyang
 */
public interface ICloudTableColumn {

	/**
	 * Get column name
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Set the column name
	 * 
	 * @param name
	 */
	public void setName(String name);

	/**
	 * Get the column value
	 * 
	 * @return
	 */
	public String getValue();

	/**
	 * Set the column value
	 * 
	 * @param value
	 */
	public void setValue(String value);

	/**
	 * Get the datatype of this column
	 * 
	 * @return
	 */
	public ETableColumnType getType();

	/**
	 * Set data type
	 * 
	 * @param type
	 *            Must be one of {@link ETableColumnType}
	 */
	public void setType(ETableColumnType type);

}
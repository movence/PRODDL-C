package org.soyatec.windowsazure.management;

import java.util.List;

public class OperatingSystemFamily {
	private String name;
	private String label;
	private List<OperatingSystem> operatingSystems;

	/**
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set the label
	 * 
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * 
	 * @return the operatingSystems
	 */
	public List<OperatingSystem> getOperatingSystems() {
		return operatingSystems;
	}

	/**
	 * Set the operatingSystems
	 * 
	 * @param operatingSystems
	 */
	public void setOperatingSystems(List<OperatingSystem> operatingSystems) {
		this.operatingSystems = operatingSystems;
	}

}

package org.soyatec.windowsazure.management;

/**
 * This class represents the operation system. 
 * 
 * @author yyang
 *
 */
public class OperatingSystem {
	private String version;
	private String label;
	private boolean _default;
	private boolean active;

	/**
	 * 
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Set the version
	 * @param version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * 
	 * @return label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set the label 
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * 
	 * @return true or false
	 */
	public boolean isDefault() {
		return _default;
	}

	/**
	 * Set the default1
	 * @param default1
	 */
	public void setDefault(boolean default1) {
		_default = default1;
	}

	/**
	 * 
	 * @return true or false
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * set the active
	 * @param active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

}

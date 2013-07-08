package de.ecspride.indyaspectwrapper.model;

import java.lang.invoke.MethodType;

/**
 * Helper Data Type to store data retrieved from a MemberName object.
 * 
 */
public class MemberNameInfo {

	private Class<?> clazz;
	private String name;
	private MethodType type;

	/**
	 * 
	 * The constructor.
	 * 
	 * @param clazz
	 *            the class of the signature
	 * @param name
	 *            the method name
	 * @param type
	 *            the type of the signature
	 */
	public MemberNameInfo(Class<?> clazz, String name, MethodType type) {

		this.clazz = clazz;
		this.name = name;
		this.type = type;
	}

	/**
	 * Empty constructor to set the fields later.
	 */
	public MemberNameInfo() {

	}

	/**
	 * @return the clazz
	 */
	public Class<?> getDeclaringClass() {
		return clazz;
	}

	/**
	 * @param clazz
	 *            the clazz to set
	 */
	public void setDeclaringClass(Class<?> clazz) {
		this.clazz = clazz;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public MethodType getMethodType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setMethodType(MethodType type) {
		this.type = type;
	}

}

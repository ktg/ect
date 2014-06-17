/**
 * DirectoryCategory.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package equip.ect.components.websearch;

public class DirectoryCategory implements java.io.Serializable
{
	private java.lang.String fullViewableName;

	private java.lang.String specialEncoding;

	private java.lang.Object __equalsCalc = null;

	private boolean __hashCodeCalc = false;

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
			DirectoryCategory.class, true);

	static
	{
		typeDesc.setXmlType(new javax.xml.namespace.QName("urn:GoogleSearch", "DirectoryCategory"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("fullViewableName");
		elemField.setXmlName(new javax.xml.namespace.QName("", "fullViewableName"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("specialEncoding");
		elemField.setXmlName(new javax.xml.namespace.QName("", "specialEncoding"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
	}

	/**
	 * Get Custom Deserializer
	 */
	public static org.apache.axis.encoding.Deserializer getDeserializer(final java.lang.String mechType,
			final java.lang.Class _javaType, final javax.xml.namespace.QName _xmlType)
	{
		return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType, _xmlType, typeDesc);
	}

	/**
	 * Get Custom Serializer
	 */
	public static org.apache.axis.encoding.Serializer getSerializer(final java.lang.String mechType,
			final java.lang.Class _javaType, final javax.xml.namespace.QName _xmlType)
	{
		return new org.apache.axis.encoding.ser.BeanSerializer(_javaType, _xmlType, typeDesc);
	}

	/**
	 * Return type metadata object
	 */
	public static org.apache.axis.description.TypeDesc getTypeDesc()
	{
		return typeDesc;
	}

	public DirectoryCategory()
	{
	}

	public DirectoryCategory(final java.lang.String fullViewableName, final java.lang.String specialEncoding)
	{
		this.fullViewableName = fullViewableName;
		this.specialEncoding = specialEncoding;
	}

	@Override
	public synchronized boolean equals(final java.lang.Object obj)
	{
		if (!(obj instanceof DirectoryCategory)) { return false; }
		final DirectoryCategory other = (DirectoryCategory) obj;
		if (obj == null) { return false; }
		if (this == obj) { return true; }
		if (__equalsCalc != null) { return (__equalsCalc == obj); }
		__equalsCalc = obj;
		boolean _equals;
		_equals = true
				&& ((this.fullViewableName == null && other.getFullViewableName() == null) || (this.fullViewableName != null && this.fullViewableName
						.equals(other.getFullViewableName())))
				&& ((this.specialEncoding == null && other.getSpecialEncoding() == null) || (this.specialEncoding != null && this.specialEncoding
						.equals(other.getSpecialEncoding())));
		__equalsCalc = null;
		return _equals;
	}

	/**
	 * Gets the fullViewableName value for this DirectoryCategory.
	 * 
	 * @return fullViewableName
	 */
	public java.lang.String getFullViewableName()
	{
		return fullViewableName;
	}

	/**
	 * Gets the specialEncoding value for this DirectoryCategory.
	 * 
	 * @return specialEncoding
	 */
	public java.lang.String getSpecialEncoding()
	{
		return specialEncoding;
	}

	@Override
	public synchronized int hashCode()
	{
		if (__hashCodeCalc) { return 0; }
		__hashCodeCalc = true;
		int _hashCode = 1;
		if (getFullViewableName() != null)
		{
			_hashCode += getFullViewableName().hashCode();
		}
		if (getSpecialEncoding() != null)
		{
			_hashCode += getSpecialEncoding().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

	/**
	 * Sets the fullViewableName value for this DirectoryCategory.
	 * 
	 * @param fullViewableName
	 */
	public void setFullViewableName(final java.lang.String fullViewableName)
	{
		this.fullViewableName = fullViewableName;
	}

	/**
	 * Sets the specialEncoding value for this DirectoryCategory.
	 * 
	 * @param specialEncoding
	 */
	public void setSpecialEncoding(final java.lang.String specialEncoding)
	{
		this.specialEncoding = specialEncoding;
	}

}

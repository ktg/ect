/**
 * GoogleSearchResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package equip.ect.components.websearch;

public class GoogleSearchResult implements java.io.Serializable
{
	private boolean documentFiltering;

	private java.lang.String searchComments;

	private int estimatedTotalResultsCount;

	private boolean estimateIsExact;

	private ResultElement[] resultElements;

	private java.lang.String searchQuery;

	private int startIndex;

	private int endIndex;

	private java.lang.String searchTips;

	private DirectoryCategory[] directoryCategories;

	private double searchTime;

	private java.lang.Object __equalsCalc = null;

	private boolean __hashCodeCalc = false;

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
			GoogleSearchResult.class, true);

	static
	{
		typeDesc.setXmlType(new javax.xml.namespace.QName("urn:GoogleSearch", "GoogleSearchResult"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("documentFiltering");
		elemField.setXmlName(new javax.xml.namespace.QName("", "documentFiltering"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("searchComments");
		elemField.setXmlName(new javax.xml.namespace.QName("", "searchComments"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("estimatedTotalResultsCount");
		elemField.setXmlName(new javax.xml.namespace.QName("", "estimatedTotalResultsCount"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("estimateIsExact");
		elemField.setXmlName(new javax.xml.namespace.QName("", "estimateIsExact"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("resultElements");
		elemField.setXmlName(new javax.xml.namespace.QName("", "resultElements"));
		elemField.setXmlType(new javax.xml.namespace.QName("urn:GoogleSearch", "ResultElement"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("searchQuery");
		elemField.setXmlName(new javax.xml.namespace.QName("", "searchQuery"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("startIndex");
		elemField.setXmlName(new javax.xml.namespace.QName("", "startIndex"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("endIndex");
		elemField.setXmlName(new javax.xml.namespace.QName("", "endIndex"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("searchTips");
		elemField.setXmlName(new javax.xml.namespace.QName("", "searchTips"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("directoryCategories");
		elemField.setXmlName(new javax.xml.namespace.QName("", "directoryCategories"));
		elemField.setXmlType(new javax.xml.namespace.QName("urn:GoogleSearch", "DirectoryCategory"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("searchTime");
		elemField.setXmlName(new javax.xml.namespace.QName("", "searchTime"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
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

	public GoogleSearchResult()
	{
	}

	public GoogleSearchResult(final boolean documentFiltering, final java.lang.String searchComments,
			final int estimatedTotalResultsCount, final boolean estimateIsExact, final ResultElement[] resultElements,
			final java.lang.String searchQuery, final int startIndex, final int endIndex,
			final java.lang.String searchTips, final DirectoryCategory[] directoryCategories, final double searchTime)
	{
		this.documentFiltering = documentFiltering;
		this.searchComments = searchComments;
		this.estimatedTotalResultsCount = estimatedTotalResultsCount;
		this.estimateIsExact = estimateIsExact;
		this.resultElements = resultElements;
		this.searchQuery = searchQuery;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.searchTips = searchTips;
		this.directoryCategories = directoryCategories;
		this.searchTime = searchTime;
	}

	@Override
	public synchronized boolean equals(final java.lang.Object obj)
	{
		if (!(obj instanceof GoogleSearchResult)) { return false; }
		final GoogleSearchResult other = (GoogleSearchResult) obj;
		if (obj == null) { return false; }
		if (this == obj) { return true; }
		if (__equalsCalc != null) { return (__equalsCalc == obj); }
		__equalsCalc = obj;
		boolean _equals;
		_equals = true
				&& this.documentFiltering == other.isDocumentFiltering()
				&& ((this.searchComments == null && other.getSearchComments() == null) || (this.searchComments != null && this.searchComments
						.equals(other.getSearchComments())))
				&& this.estimatedTotalResultsCount == other.getEstimatedTotalResultsCount()
				&& this.estimateIsExact == other.isEstimateIsExact()
				&& ((this.resultElements == null && other.getResultElements() == null) || (this.resultElements != null && java.util.Arrays
						.equals(this.resultElements, other.getResultElements())))
				&& ((this.searchQuery == null && other.getSearchQuery() == null) || (this.searchQuery != null && this.searchQuery
						.equals(other.getSearchQuery())))
				&& this.startIndex == other.getStartIndex()
				&& this.endIndex == other.getEndIndex()
				&& ((this.searchTips == null && other.getSearchTips() == null) || (this.searchTips != null && this.searchTips
						.equals(other.getSearchTips())))
				&& ((this.directoryCategories == null && other.getDirectoryCategories() == null) || (this.directoryCategories != null && java.util.Arrays
						.equals(this.directoryCategories, other.getDirectoryCategories())))
				&& this.searchTime == other.getSearchTime();
		__equalsCalc = null;
		return _equals;
	}

	/**
	 * Gets the directoryCategories value for this GoogleSearchResult.
	 * 
	 * @return directoryCategories
	 */
	public DirectoryCategory[] getDirectoryCategories()
	{
		return directoryCategories;
	}

	/**
	 * Gets the endIndex value for this GoogleSearchResult.
	 * 
	 * @return endIndex
	 */
	public int getEndIndex()
	{
		return endIndex;
	}

	/**
	 * Gets the estimatedTotalResultsCount value for this GoogleSearchResult.
	 * 
	 * @return estimatedTotalResultsCount
	 */
	public int getEstimatedTotalResultsCount()
	{
		return estimatedTotalResultsCount;
	}

	/**
	 * Gets the resultElements value for this GoogleSearchResult.
	 * 
	 * @return resultElements
	 */
	public ResultElement[] getResultElements()
	{
		return resultElements;
	}

	/**
	 * Gets the searchComments value for this GoogleSearchResult.
	 * 
	 * @return searchComments
	 */
	public java.lang.String getSearchComments()
	{
		return searchComments;
	}

	/**
	 * Gets the searchQuery value for this GoogleSearchResult.
	 * 
	 * @return searchQuery
	 */
	public java.lang.String getSearchQuery()
	{
		return searchQuery;
	}

	/**
	 * Gets the searchTime value for this GoogleSearchResult.
	 * 
	 * @return searchTime
	 */
	public double getSearchTime()
	{
		return searchTime;
	}

	/**
	 * Gets the searchTips value for this GoogleSearchResult.
	 * 
	 * @return searchTips
	 */
	public java.lang.String getSearchTips()
	{
		return searchTips;
	}

	/**
	 * Gets the startIndex value for this GoogleSearchResult.
	 * 
	 * @return startIndex
	 */
	public int getStartIndex()
	{
		return startIndex;
	}

	@Override
	public synchronized int hashCode()
	{
		if (__hashCodeCalc) { return 0; }
		__hashCodeCalc = true;
		int _hashCode = 1;
		_hashCode += (isDocumentFiltering() ? Boolean.TRUE : Boolean.FALSE).hashCode();
		if (getSearchComments() != null)
		{
			_hashCode += getSearchComments().hashCode();
		}
		_hashCode += getEstimatedTotalResultsCount();
		_hashCode += (isEstimateIsExact() ? Boolean.TRUE : Boolean.FALSE).hashCode();
		if (getResultElements() != null)
		{
			for (int i = 0; i < java.lang.reflect.Array.getLength(getResultElements()); i++)
			{
				final java.lang.Object obj = java.lang.reflect.Array.get(getResultElements(), i);
				if (obj != null && !obj.getClass().isArray())
				{
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getSearchQuery() != null)
		{
			_hashCode += getSearchQuery().hashCode();
		}
		_hashCode += getStartIndex();
		_hashCode += getEndIndex();
		if (getSearchTips() != null)
		{
			_hashCode += getSearchTips().hashCode();
		}
		if (getDirectoryCategories() != null)
		{
			for (int i = 0; i < java.lang.reflect.Array.getLength(getDirectoryCategories()); i++)
			{
				final java.lang.Object obj = java.lang.reflect.Array.get(getDirectoryCategories(), i);
				if (obj != null && !obj.getClass().isArray())
				{
					_hashCode += obj.hashCode();
				}
			}
		}
		_hashCode += new Double(getSearchTime()).hashCode();
		__hashCodeCalc = false;
		return _hashCode;
	}

	/**
	 * Gets the documentFiltering value for this GoogleSearchResult.
	 * 
	 * @return documentFiltering
	 */
	public boolean isDocumentFiltering()
	{
		return documentFiltering;
	}

	/**
	 * Gets the estimateIsExact value for this GoogleSearchResult.
	 * 
	 * @return estimateIsExact
	 */
	public boolean isEstimateIsExact()
	{
		return estimateIsExact;
	}

	/**
	 * Sets the directoryCategories value for this GoogleSearchResult.
	 * 
	 * @param directoryCategories
	 */
	public void setDirectoryCategories(final DirectoryCategory[] directoryCategories)
	{
		this.directoryCategories = directoryCategories;
	}

	/**
	 * Sets the documentFiltering value for this GoogleSearchResult.
	 * 
	 * @param documentFiltering
	 */
	public void setDocumentFiltering(final boolean documentFiltering)
	{
		this.documentFiltering = documentFiltering;
	}

	/**
	 * Sets the endIndex value for this GoogleSearchResult.
	 * 
	 * @param endIndex
	 */
	public void setEndIndex(final int endIndex)
	{
		this.endIndex = endIndex;
	}

	/**
	 * Sets the estimatedTotalResultsCount value for this GoogleSearchResult.
	 * 
	 * @param estimatedTotalResultsCount
	 */
	public void setEstimatedTotalResultsCount(final int estimatedTotalResultsCount)
	{
		this.estimatedTotalResultsCount = estimatedTotalResultsCount;
	}

	/**
	 * Sets the estimateIsExact value for this GoogleSearchResult.
	 * 
	 * @param estimateIsExact
	 */
	public void setEstimateIsExact(final boolean estimateIsExact)
	{
		this.estimateIsExact = estimateIsExact;
	}

	/**
	 * Sets the resultElements value for this GoogleSearchResult.
	 * 
	 * @param resultElements
	 */
	public void setResultElements(final ResultElement[] resultElements)
	{
		this.resultElements = resultElements;
	}

	/**
	 * Sets the searchComments value for this GoogleSearchResult.
	 * 
	 * @param searchComments
	 */
	public void setSearchComments(final java.lang.String searchComments)
	{
		this.searchComments = searchComments;
	}

	/**
	 * Sets the searchQuery value for this GoogleSearchResult.
	 * 
	 * @param searchQuery
	 */
	public void setSearchQuery(final java.lang.String searchQuery)
	{
		this.searchQuery = searchQuery;
	}

	/**
	 * Sets the searchTime value for this GoogleSearchResult.
	 * 
	 * @param searchTime
	 */
	public void setSearchTime(final double searchTime)
	{
		this.searchTime = searchTime;
	}

	/**
	 * Sets the searchTips value for this GoogleSearchResult.
	 * 
	 * @param searchTips
	 */
	public void setSearchTips(final java.lang.String searchTips)
	{
		this.searchTips = searchTips;
	}

	/**
	 * Sets the startIndex value for this GoogleSearchResult.
	 * 
	 * @param startIndex
	 */
	public void setStartIndex(final int startIndex)
	{
		this.startIndex = startIndex;
	}

}

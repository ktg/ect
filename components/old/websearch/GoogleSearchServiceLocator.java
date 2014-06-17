/**
 * GoogleSearchServiceLocator.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis 1.3 Oct 05, 2005
 * (05:23:37 EDT) WSDL2Java emitter.
 */

package equip.ect.components.websearch;

public class GoogleSearchServiceLocator extends org.apache.axis.client.Service implements GoogleSearchService
{

	// Use to get a proxy class for GoogleSearchPort
	private java.lang.String GoogleSearchPort_address = "http://api.google.com/search/beta2";

	// The WSDD service name defaults to the port name.
	private java.lang.String GoogleSearchPortWSDDServiceName = "GoogleSearchPort";

	private java.util.HashSet ports = null;

	public GoogleSearchServiceLocator()
	{
	}

	public GoogleSearchServiceLocator(final java.lang.String wsdlLoc, final javax.xml.namespace.QName sName)
			throws javax.xml.rpc.ServiceException
	{
		super(wsdlLoc, sName);
	}

	public GoogleSearchServiceLocator(final org.apache.axis.EngineConfiguration config)
	{
		super(config);
	}

	@Override
	public GoogleSearchPort getGoogleSearchPort() throws javax.xml.rpc.ServiceException
	{
		java.net.URL endpoint;
		try
		{
			endpoint = new java.net.URL(GoogleSearchPort_address);
		}
		catch (final java.net.MalformedURLException e)
		{
			throw new javax.xml.rpc.ServiceException(e);
		}
		return getGoogleSearchPort(endpoint);
	}

	@Override
	public GoogleSearchPort getGoogleSearchPort(final java.net.URL portAddress) throws javax.xml.rpc.ServiceException
	{
		try
		{
			final GoogleSearchBindingStub _stub = new GoogleSearchBindingStub(portAddress, this);
			_stub.setPortName(getGoogleSearchPortWSDDServiceName());
			return _stub;
		}
		catch (final org.apache.axis.AxisFault e)
		{
			return null;
		}
	}

	@Override
	public java.lang.String getGoogleSearchPortAddress()
	{
		return GoogleSearchPort_address;
	}

	public java.lang.String getGoogleSearchPortWSDDServiceName()
	{
		return GoogleSearchPortWSDDServiceName;
	}

	/**
	 * For the given interface, get the stub implementation. If this service has no port for the
	 * given interface, then ServiceException is thrown.
	 */
	public java.rmi.Remote getPort(final Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException
	{
		try
		{
			if (GoogleSearchPort.class.isAssignableFrom(serviceEndpointInterface))
			{
				final GoogleSearchBindingStub _stub = new GoogleSearchBindingStub(new java.net.URL(
						GoogleSearchPort_address), this);
				_stub.setPortName(getGoogleSearchPortWSDDServiceName());
				return _stub;
			}
		}
		catch (final java.lang.Throwable t)
		{
			throw new javax.xml.rpc.ServiceException(t);
		}
		throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  "
				+ (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
	}

	/**
	 * For the given interface, get the stub implementation. If this service has no port for the
	 * given interface, then ServiceException is thrown.
	 */
	public java.rmi.Remote getPort(final javax.xml.namespace.QName portName, final Class serviceEndpointInterface)
			throws javax.xml.rpc.ServiceException
	{
		if (portName == null) { return getPort(serviceEndpointInterface); }
		final java.lang.String inputPortName = portName.getLocalPart();
		if ("GoogleSearchPort".equals(inputPortName))
		{
			return getGoogleSearchPort();
		}
		else
		{
			final java.rmi.Remote _stub = getPort(serviceEndpointInterface);
			((org.apache.axis.client.Stub) _stub).setPortName(portName);
			return _stub;
		}
	}

	public java.util.Iterator getPorts()
	{
		if (ports == null)
		{
			ports = new java.util.HashSet();
			ports.add(new javax.xml.namespace.QName("urn:GoogleSearch", "GoogleSearchPort"));
		}
		return ports.iterator();
	}

	public javax.xml.namespace.QName getServiceName()
	{
		return new javax.xml.namespace.QName("urn:GoogleSearch", "GoogleSearchService");
	}

	/**
	 * Set the endpoint address for the specified port name.
	 */
	public void setEndpointAddress(final java.lang.String portName, final java.lang.String address)
			throws javax.xml.rpc.ServiceException
	{

		if ("GoogleSearchPort".equals(portName))
		{
			setGoogleSearchPortEndpointAddress(address);
		}
		else
		{ // Unknown Port Name
			throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
		}
	}

	/**
	 * Set the endpoint address for the specified port name.
	 */
	public void setEndpointAddress(final javax.xml.namespace.QName portName, final java.lang.String address)
			throws javax.xml.rpc.ServiceException
	{
		setEndpointAddress(portName.getLocalPart(), address);
	}

	public void setGoogleSearchPortEndpointAddress(final java.lang.String address)
	{
		GoogleSearchPort_address = address;
	}

	public void setGoogleSearchPortWSDDServiceName(final java.lang.String name)
	{
		GoogleSearchPortWSDDServiceName = name;
	}

}

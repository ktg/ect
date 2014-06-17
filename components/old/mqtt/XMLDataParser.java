package equip.ect.components.mqtt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//import semanticmedia.mqtt.DataParser;

// based on Jan's version, but changed to JAXP standard API with generics
public class XMLDataParser
// implements DataParser
{

	private final DocumentBuilder parser;

	XMLDataParser() throws Exception
	{
		this.parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	}

	public HashMap<String, String> parseData(final String data)
	{
		try
		{
			final Document doc = parser.parse(new InputSource(new ByteArrayInputStream(data.getBytes())));
			final NodeList msgs = doc.getElementsByTagName("msg");
			final NodeList nl = msgs.item(0).getChildNodes();
			final HashMap<String, String> props = new HashMap<String, String>(nl.getLength());
			for (int i = 0; i < nl.getLength(); i++)
			{
				final Node node = nl.item(i);
				final String key = node.getNodeName();
				final String value = node.getTextContent();
				System.out.println("Parsing key=" + key + " value=" + value);
				props.put(key, value);
			}
			return props;
		}
		catch (final SAXException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
}

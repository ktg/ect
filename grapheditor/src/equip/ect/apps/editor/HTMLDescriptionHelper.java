/*
 <COPYRIGHT>

 Copyright (c) 2004-2005, University of Nottingham
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 - Neither the name of the University of Nottingham
 nor the names of its contributors may be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 </COPYRIGHT>

 Created by: Stefan Rennick Egglestone (University of Nottingham)
 Contributors:

 Stefan Rennick Egglestone (University of Nottingham)
  
 */

package equip.ect.apps.editor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.html.dom.HTMLDocumentImpl;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLHeadingElement;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Used to extract sections from a component HTML description. Sections begin with a heading, and
 * finish when the next heading or the end of the html string is reached. Headings should be
 * formatted using the appropriate html heading element eg if constant HEADING_LEVEL defined below
 * is 3, then headings should start with the &lt;h3&gt; or &lt;H3&gt; tags and end with the
 * equivalent closing tags. Note that this class uses the <a
 * href="http://people.apache.org/~andyc/neko/doc/html/">neko</a> html parser.
 */

public class HTMLDescriptionHelper
{
	protected static final String SIMPLE_EDITOR_HEADING1 = "Summary";
	protected static final String ADVANCED_EDITOR_HEADING1 = "Description";
	protected static final String ADVANCED_EDITOR_HEADING2 = "Usage";
	protected static final String ADVANCED_EDITOR_HEADING3 = "Configuration";
	protected static final String ADVANCED_EDITOR_HEADING4 = "Installation";
	protected static final String ADVANCED_EDITOR_HEADING5 = "Technical Details";

	public static int HEADING_LEVEL = 3;

	protected Map<String, String> headingToText = new HashMap<String, String>();
	protected boolean headingsExist = false;

	protected String correctedHTML = null;

	public HTMLDescriptionHelper(final String htmlDesc, final boolean removeHead) throws HTMLException
	{
		Exception caught = null;

		try
		{
			// first, load up html into document
			// this uses the neko html parser, which corrects some minor
			// errors in the document (such as missing html, body tags)

			final HTMLDocumentImpl doc = parseHTML(htmlDesc);

			final HTMLElement headElement = doc.getHead();

			if (headElement != null)
			{
				headElement.getParentNode().removeChild(headElement);
			}

			// write corrected document to string for use by other classes

			correctedHTML = getHTMLString(doc);

			// System.out.println(correctedHTML);

			// get all nodes in the body of the html
			final NodeList nl1 = doc.getElementsByTagName("body");
			final Element body = (Element) (nl1.item(0));

			if (body == null) { return; }

			final NodeList nl = body.getChildNodes();

			String currentHeading = null;
			List<Node> currentNodes = new ArrayList<Node>();

			final Map<String, List<Node>> hash = new HashMap<String, List<Node>>();

			// loop through these nodes, looking for headings, and adding
			// non-heading nodes to vectors containing html within each heading

			for (int i = 0; i < nl.getLength(); i++)
			{
				final Node n = nl.item(i);

				if (n instanceof HTMLHeadingElement)
				{
					final HTMLHeadingElement headingElement = (HTMLHeadingElement) n;

					final String ectHeader = getECTHeader(headingElement);

					// if not null, then it is a heading we are looking for
					if (ectHeader != null)
					{
						headingsExist = true;

						if (currentHeading != null)
						{
							// if we were previously adding new nodes
							// for a previous heading, then
							// store these nodes for later processing
							hash.put(currentHeading, currentNodes);
						}

						currentHeading = ectHeader;
						currentNodes = new ArrayList<Node>();

					}
					else
					{
						if (currentHeading != null)
						{
							currentNodes.add(n);
						}
					}
				}
				else
				{
					if (currentHeading != null)
					{
						currentNodes.add(n);
					}
				}
			}

			// if we're still adding stuff for a heading after
			// finsihign the document, then finish the contents of this heading
			if (currentHeading != null)
			{
				hash.put(currentHeading, currentNodes);
			}

			// now loop through all available headings
			for(String key: hash.keySet())
			{
				// now construct strings representing
				// text in sections
				final List<Node> vec = hash.get(key);

				// construct document fragment containing nodes
				// within heading
				final DocumentFragment df = doc.createDocumentFragment();
				for (Node aVec : vec)
				{
					df.appendChild(aVec);
				}

				// write doc frag to string, then reload through
				// neko parser to generate valid html

				final String docString = getHTMLString(df);

				final String chs = correctHTMLString(docString);

				headingToText.put(key, chs);
			}
		}

		catch (final SAXException e)
		{
			caught = e;
		}
		catch (final TransformerException e)
		{
			caught = e;
		}
		catch (final IOException e)
		{
			caught = e;
		}
		finally
		{
			if (caught != null) { throw (new HTMLException(caught)); }
		}
	}

	public String[] getAdvancedEditorHeadings()
	{

		return new String[]{ ADVANCED_EDITOR_HEADING1, ADVANCED_EDITOR_HEADING2, ADVANCED_EDITOR_HEADING3,
									ADVANCED_EDITOR_HEADING4, ADVANCED_EDITOR_HEADING5 };
	}

	public String[] getAllEditorHeadings()
	{

		return new String[]{ SIMPLE_EDITOR_HEADING1, ADVANCED_EDITOR_HEADING1, ADVANCED_EDITOR_HEADING2,
									ADVANCED_EDITOR_HEADING3, ADVANCED_EDITOR_HEADING4, ADVANCED_EDITOR_HEADING5 };
	}

	public String getCorrectedHTML()
	{
		return correctedHTML;
	}

	/**
	 * Will return true if any of the headings identified by the constants above have been found,
	 * false otherwise
	 */

	public boolean getHeadingsExist()
	{
		return headingsExist;
	}

	/**
	 * Returns the text contained in a section identified by a particular heading name. Returns null
	 * if section or text does not exist
	 */

	public String getSectionText(final String headingName)
	{
		if (!(headingToText.containsKey(headingName)))
		{
			return null;
		}
		else
		{
			return headingToText.get(headingName);
		}
	}

	protected String correctHTMLString(final String htmlDesc) throws SAXException, IOException, TransformerException,
			TransformerConfigurationException
	{
		return getHTMLString(parseHTML(htmlDesc));
	}

	protected String getECTHeader(final HTMLHeadingElement he)
	{
		// first, check to see if right heading level
		final String tagName = he.getTagName();
		if (!tagName.toLowerCase().equals("h3")) { return null; }

		// now, see if it is an intersting header
		final NodeList nl = he.getChildNodes();
		if (nl.getLength() == 0) { return null; }

		final Node node = nl.item(0);

		if (node instanceof Text)
		{
			final Text text = (Text) node;
			final String data = text.getData();

			final String[] headings = getAllEditorHeadings();

			for (final String heading : headings)
			{
				if (data.equalsIgnoreCase(heading)) { return heading; }
			}
			return null;
		}
		else
		{
			return null;
		}
	}

	protected String getHTMLString(final Node node) throws TransformerException
	{
		final TransformerFactory factory = TransformerFactory.newInstance();
		final Transformer transformer = factory.newTransformer();

		final DOMSource domSource = new DOMSource(node);

		final StringWriter sw = new StringWriter();
		final StreamResult streamResult = new StreamResult(sw);

		transformer.transform(domSource, streamResult);

		return (sw.toString());
	}

	protected HTMLDocumentImpl parseHTML(final String htmlDesc) throws SAXException, IOException
	{

		final DOMParser parser = new DOMParser();

		final StringReader sr = new StringReader(htmlDesc);
		final InputSource is = new InputSource(sr);

		parser.parse(is);

		return (HTMLDocumentImpl) (parser.getDocument());
	}
}

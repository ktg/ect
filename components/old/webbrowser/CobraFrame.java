package equip.ect.components.webbrowser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.w3c.dom.Document;
import org.xamjwg.html.HtmlParserContext;
import org.xamjwg.html.HtmlRendererContext;
import org.xamjwg.html.gui.HtmlPanel;
import org.xamjwg.html.parser.InputSourceImpl;
import org.xamjwg.html.test.SimpleHtmlParserContext;
import org.xamjwg.html.test.SimpleHtmlRendererContext;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class CobraFrame extends JFrame
{
	HtmlPanel htmlPanel;
	HtmlParserContext parserContext;
	HtmlRendererContext rendererContext;
	DocumentBuilderImpl builder;

	static final String FRAME_TITLE = "Web browser";

	static CobraFrame frame = null;

	public synchronized static CobraFrame getSingleFrame()
	{
		if (frame == null)
		{
			frame = new CobraFrame();
		}
		return frame;
	}

	private CobraFrame()
	{
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		htmlPanel = new HtmlPanel();
		this.getContentPane().add(htmlPanel);
		parserContext = new LocalHtmlParserContext();
		rendererContext = new LocalHtmlRendererContext(htmlPanel, parserContext);
		builder = new DocumentBuilderImpl(parserContext, rendererContext);

		this.setSize(800, 600);
		this.setTitle(FRAME_TITLE);
		this.setVisible(true);
	}

	public synchronized void processURL(final String url) throws MalformedURLException, IOException, SAXException
	{
		if (url == null)
		{
			htmlPanel.setDocument(builder.newDocument(), rendererContext, parserContext);
		}
		else
		{
			final URL urlObject = new URL(url);

			final URLConnection connection = urlObject.openConnection();
			final InputStream in = connection.getInputStream();

			final Reader reader = new InputStreamReader(in);

			final InputSource is = new InputSourceImpl(reader, url);

			// DocumentBuilderImpl builder = new DocumentBuilderImpl(parserContext,
			// rendererContext);
			final Document document = builder.parse(is);

			htmlPanel.setDocument(document, rendererContext, parserContext);
		}
	}
}

class LocalHtmlParserContext extends SimpleHtmlParserContext
{
	// Override methods here to implement browser functionality
}

class LocalHtmlRendererContext extends SimpleHtmlRendererContext
{
	// Override methods here to implement browser functionality

	public LocalHtmlRendererContext(final HtmlPanel contextComponent, final HtmlParserContext parserContext)
	{
		super(contextComponent, parserContext);
	}
}

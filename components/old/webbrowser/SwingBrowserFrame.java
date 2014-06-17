package equip.ect.components.webbrowser;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class SwingBrowserFrame extends JFrame
{
	class ControllerFrame extends JFrame
	{
		JCheckBox box;

		ControllerFrame()
		{
			setTitle(CONTROLLER_TITLE);
			getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

			final JButton reloadButton = new JButton("Reload content");
			reloadButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							reloadContent();
						}
					}).start();
				}
			});

			getContentPane().add(reloadButton);

			box = new JCheckBox("Enable?", true);
			box.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					enableDivider(box.isSelected());

				}
			});

			getContentPane().add(box);
			pack();
		}
	}

	static final String FRAME_TITLE = "Document browser";

	static final String CONTROLLER_TITLE = "Document browser controller";

	static final String DEFAULT_LOCATION = "c:/inScape/ect/resources/arauthoring/documents/default.html";

	static final String INFORMATION_LOCATION = "c:/inScape/ect/resources/arauthoring/documents/info.html";

	static final String DEFAULT_TITLE = "Home";

	static final int TITLE_LENGTH = 20;

	static SwingBrowserFrame frame = null;

	public synchronized static SwingBrowserFrame getFrameReference()
	{
		if (frame == null)
		{
			// System.out.println("getframe: null");
			frame = new SwingBrowserFrame();
		}

		return frame;
	}

	Vector holdingComponents;

	Hashtable componentToVisibility;

	Hashtable componentToTab;

	JEditorPane defaultPane;

	// the first time the frame is made visible, need to do
	// some things to the user interface
	boolean frameHasBeenMadeVisible = false;

	// JEditorPane htmlPane;
	JTabbedPane tabbedPane;

	JEditorPane informationPane;

	JFrame controllerFrame;

	JSplitPane splitPane;

	SwingBrowserFrame()
	{
		controllerFrame = new ControllerFrame();

		holdingComponents = new Vector();
		componentToVisibility = new Hashtable();
		componentToTab = new Hashtable();

		// try and load from a particular location
		informationPane = createPane(INFORMATION_LOCATION);

		if (informationPane == null)
		{
			informationPane = new JEditorPane();
			informationPane.setContentType("text/html");
			informationPane.setEditable(false);
			informationPane.setText("<html></html>");
		}

		tabbedPane = new JTabbedPane();
		// tabbedPane.setPreferredSize(new Dimension(800,900));

		defaultPane = createPane(DEFAULT_LOCATION);

		if (defaultPane != null)
		{
			tabbedPane.addTab(DEFAULT_TITLE, defaultPane);
		}

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		splitPane.setOneTouchExpandable(true);
		splitPane.setEnabled(true);
		getContentPane().add(splitPane);

		informationPane.setMinimumSize(new Dimension(0, 0));
		splitPane.setTopComponent(informationPane);

		tabbedPane.setMinimumSize(new Dimension(0, 0));
		splitPane.setBottomComponent(tabbedPane);

		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setSize(800, 600);
		this.setTitle(FRAME_TITLE);
		// this.validate();
	}

	public String constructTitle(final String content)
	{
		// just display the last part of the string defining the content location
		// eg if content is "c:/inscape/resources/default.html"
		// then just display default.html

		int lastSeperatorPos = -1;
		final int contentSize = content.length();

		for (int i = contentSize - 1; i >= 0; i--)
		{
			final char nextChar = content.charAt(i);
			if ((nextChar == '/') || (nextChar == '\\'))
			{
				lastSeperatorPos = i;
				break;
			}
		}

		String lastPartOfContent = null;

		if (lastSeperatorPos == -1)
		{
			lastPartOfContent = content;
		}
		else
		{
			lastPartOfContent = content.substring(lastSeperatorPos + 1);
		}

		return lastPartOfContent;
	}

	public synchronized void deRegisterComponent(final SwingBrowserComponent component)
	{
		if (componentToTab.containsKey(component))
		{
			final Component tab = (Component) (componentToTab.get(component));
			tabbedPane.remove(tab);

			componentToTab.remove(component);
		}

		componentToVisibility.remove(component);

		holdingComponents.remove(component);

		if (holdingComponents.size() == 0)
		{
			frame.setVisible(false);
			controllerFrame.setVisible(false);
		}
	}

	public void enableDivider(final boolean enabled)
	{
		splitPane.setEnabled(enabled);
		splitPane.setOneTouchExpandable(enabled);
	}

	public void makeRemainingComponentVisible()
	{
		boolean visibleComponentFound = false;

		for (int i = 0; i < holdingComponents.size(); i++)
		{
			final Object sourceComponent = holdingComponents.elementAt(i);

			// does this component have an associate tab?

			if (componentToTab.containsKey(sourceComponent))
			{
				final boolean componentVisibility = ((Boolean) (componentToVisibility.get(sourceComponent)))
						.booleanValue();

				if (componentVisibility == true)
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							final Component tab = (Component) (componentToTab.get(sourceComponent));
							tabbedPane.setSelectedComponent(tab);
						}
					});

					visibleComponentFound = true;
					break;
				}
			}
		}

		if (!visibleComponentFound)
		{
			if (defaultPane != null)
			{
				// if there is a default pane to display

				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						tabbedPane.setSelectedComponent(defaultPane);
					}
				});
			}
		}
	}

	public synchronized void registerComponent(final SwingBrowserComponent toRegister)
	{
		holdingComponents.add(toRegister);

		if (holdingComponents.size() == 1)
		{
			// if frame was previously invisible
			// due to containing no content, then now make
			// it visible

			frame.setVisible(true);
			controllerFrame.setVisible(true);

			if (!frameHasBeenMadeVisible)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						splitPane.setDividerLocation(0.25);
					}
				});

				frameHasBeenMadeVisible = true;
			}
		}
	}

	public synchronized void reloadContent()
	{
		// iterate through all of the tabs, reloading the
		// content that is shown there

		final int selectedIndex = tabbedPane.getSelectedIndex();
		System.out.println("selected index: " + selectedIndex);

		for (int i = 0; i < holdingComponents.size(); i++)
		{
			final Object holdingComponent = holdingComponents.elementAt(i);

			if (componentToTab.containsKey(holdingComponent))
			{
				final JEditorPane pane = (JEditorPane) (componentToTab.get(holdingComponent));
				final URL page = pane.getPage();

				final int index = tabbedPane.indexOfComponent(pane);

				System.out.println("found tab at: " + index);
				final String title = tabbedPane.getTitleAt(index);

				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						tabbedPane.remove(pane);
						componentToTab.remove(holdingComponent);

						final JEditorPane newPane = createPane(page.toString());

						if (newPane != null)
						{
							System.out.println("inserting tab at: " + index);
							tabbedPane.add(newPane, index);
							tabbedPane.setTitleAt(index, title);
							componentToTab.put(holdingComponent, newPane);
						}

					}
				});

			}
		}

		try
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					System.out.println("setting: " + selectedIndex);
					tabbedPane.setSelectedIndex(selectedIndex);
				}
			});

		}
		catch (final IndexOutOfBoundsException e)
		{
			// tab that was selected has been removed. So look for another tab that
			// can be selected
			makeRemainingComponentVisible();
		}

		// now reload the stuff in the information pane

		informationPane = createPane(INFORMATION_LOCATION);

		if (informationPane == null)
		{
			informationPane = new JEditorPane();
			informationPane.setContentType("text/html");
			informationPane.setEditable(false);
			informationPane.setText("<html></html>");
		}

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				final int location = splitPane.getDividerLocation();

				splitPane.setTopComponent(informationPane);
				splitPane.setDividerLocation(location);
			}
		});

	}

	public synchronized void submitContentChange(final SwingBrowserComponent component, final String content)
	{
		// if this is the first time that the component has submitted
		// some content, then create a component to display this content

		// else, update old content

		if (componentToTab.containsKey(component))
		{
			final Component tab = (Component) (componentToTab.get(component));

			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					tabbedPane.remove(tab);
				}
			});

			componentToTab.remove(component);
		}

		final JEditorPane pane = createPane(content);

		if (pane != null)
		{
			final String title = constructTitle(content);

			componentToTab.put(component, pane);

			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					final Component tab = tabbedPane.add(title, pane);
				}
			});
		}
	}

	public synchronized void submitVisibilityChange(final SwingBrowserComponent component, final boolean visibility)
	{
		// if this component has just become visible then select its tab
		// otherwise look for other tabs that are visible and select one of those

		componentToVisibility.put(component, new Boolean(visibility));

		// first, see if there is tab associated with this component

		if (!(componentToTab.containsKey(component)))
		{
			makeRemainingComponentVisible();
			return;
		}

		if (visibility == true)
		{
			// if there is a tab, and it should be selected, then do so
			final Component tab = (Component) (componentToTab.get(component));

			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					tabbedPane.setSelectedComponent(tab);
				}
			});
		}
		else
		{
			makeRemainingComponentVisible();
		}
	}

	JEditorPane createPane(final String content)
	{
		final JEditorPane newPane = new JEditorPane();
		newPane.setEditable(false);
		newPane.setContentType("text/html");

		// attempts to fill the pane using the location
		// this might either be a url or a file

		URL url = null;

		try
		{
			url = new URL(content);
			System.out.println("its a url");
		}
		catch (final MalformedURLException e)
		{
			// it isn't a valid url. So maybe it is a file?

			System.out.println("it isn't a url");

			final File file = new File(content);
			if (file.isFile())
			{
				System.out.println("its a file");

				try
				{
					url = file.toURL();
				}
				catch (final MalformedURLException f)
				{
					f.printStackTrace();
				}
			}
		}

		if (url == null)
		{
			// couldn't find document to load, so don't
			// create a pane
			return null;
		}
		else
		{
			try
			{
				System.out.println("trying: " + url);
				newPane.setPage(url);
				System.out.println("got it!");
				return newPane;
			}
			catch (final IOException e)
			{
				// error in loading documnet, so don't
				// return a tab
				return null;
			}
		}
	}
}

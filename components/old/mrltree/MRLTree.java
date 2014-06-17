package equip.ect.components.mrltree;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.ImageObserver;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import equip.data.DictionaryImpl;
import equip.data.StringBoxImpl;

public class MRLTree implements Serializable
{

	class MRLTreePanel extends JPanel implements ImageObserver
	{
		MRLTreePanel()
		{
			setOpaque(false);
		}

		@Override
		public boolean imageUpdate(final Image img, final int infoflags, final int x, final int y, final int width,
				final int height)
		{
			System.out.println("image update");
			return true;
		}

		@Override
		public void paintComponent(final Graphics g)
		{
			System.out.println("start of paint component");

			if ((image != null) && (answers != null) && (positions != null))
			{
				// draw tree;
				g.drawImage(image, 0, 0, this);

				// now draw a bauble for everyone who has got an answer right

				for (int i = 1; i < 24; i++)
				{
					final String key = i + "";

					if (answeringUsers.containsKey(key))
					{
						final String userID = (String) (answeringUsers.get(key));

						System.out.println(key + userID);

						final Point p = (Point) (positions.get(key));

						if (i != 23)
						{
							// obtain the position to render

							drawBauble(p, Color.RED, Color.GREEN, userID, key, g);
						}
						else
						{
							drawStar(p, Color.YELLOW, Color.RED, userID, g);
						}
					}
				}
			}
			else
			{
				System.out.println("something is null");
			}

			super.paintComponent(g);
		}

		void drawBauble(final Point p, final Color back, final Color fore, final String userID, final String question,
				final Graphics g)
		{
			g.setColor(back);
			g.fillOval(p.x, p.y, getBaubleSize(), getBaubleSize());

			g.setColor(fore);

			final int textX = p.x + (int) (getBaubleSize() / 4.0);
			int textY = p.y + (getBaubleSize() / 3);

			// int ascent = g.getFontMetrics().getAscent();
			// textY = textY + (int)((double)ascent/2.0);

			g.drawString(question + ".", textX, textY);

			textY = textY + (getBaubleSize() / 3);
			g.drawString(userID, textX, textY);
		}
	}

	final static double STAR_SIDE_LENGTH = 60.0;
	final static double COS_30 = 0.866;

	final static double SIN_30 = 0.5;
	JFrame frame;

	JPanel treePanel;
	Image image = null;

	String imageURL;
	final static String SUBJECT_KEY = "subject";

	final static String FROM_KEY = "from";
	Hashtable answers = null;
	Hashtable answeringUsers = new Hashtable();

	Hashtable positions = null;

	String configAnswersFile;

	String configPositionsFile;

	int baubleSize = 40;

	final static String SUBJECT_PREFIX = "ANSWER";

	DictionaryImpl message;

	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public MRLTree()
	{
		System.out.println("start of constructor");

		frame = new JFrame();

		frame.setTitle("The MRL christmas tree!");

		treePanel = new MRLTreePanel();

		frame.getContentPane().add(treePanel);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		frame.setSize(1000, 700);

		frame.setVisible(true);

		setConfigAnswersFile("z:/mrltree/answers.txt");
		setConfigPositionsFile("z:/mrltree/positions.txt");
		setConfigImageURL("http://www.mrl.nott.ac.uk/~sre/mrltree/tree.PNG");

		System.out.println("start of constructor");
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public int getBaubleSize()
	{
		return baubleSize;
	}

	public String getConfigAnswersFile()
	{
		return configAnswersFile;
	}

	public String getConfigImageURL()
	{
		return imageURL;
	}

	public String getConfigPositionsFile()
	{
		return configPositionsFile;
	}

	public DictionaryImpl getMessage()
	{
		return this.message;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setBaubleSize(final int newValue)
	{
		final int oldValue = this.baubleSize;
		this.baubleSize = newValue;

		propertyChangeListeners.firePropertyChange("baubleSize", oldValue, newValue);
	}

	public void setConfigAnswersFile(final String newValue)
	{
		final String oldValue = this.configAnswersFile;
		this.configAnswersFile = newValue;

		propertyChangeListeners.firePropertyChange("configAnswersFile", oldValue, newValue);

		try
		{
			final File file = new File(configAnswersFile);
			final FileReader fr = new FileReader(file);
			final BufferedReader br = new BufferedReader(fr);

			String lastLine = null;

			answers = new Hashtable();

			while ((lastLine = br.readLine()) != null)
			{
				lastLine = lastLine.trim();

				if (lastLine.length() > 0)
				{
					final String[] bits = lastLine.split(",");
					answers.put(bits[0], bits[1]);

					System.out.println(bits[0] + " " + bits[1]);
				}
			}
		}
		catch (final IOException e)
		{
			answers = null;
		}

	}

	public void setConfigImageURL(final String newValue)
	{
		final String oldValue = this.imageURL;
		this.imageURL = newValue;

		propertyChangeListeners.firePropertyChange("configImageURL", oldValue, newValue);

		try
		{
			final URL url = new URL(imageURL);
			final ImageIcon icon = new ImageIcon(url);
			image = icon.getImage();
		}
		catch (final MalformedURLException e)
		{
			e.printStackTrace();
			image = null;
		}

		treePanel.repaint();
	}

	public void setConfigPositionsFile(final String newValue)
	{
		final String oldValue = this.configPositionsFile;
		this.configPositionsFile = newValue;

		propertyChangeListeners.firePropertyChange("configPositionsFile", oldValue, newValue);

		try
		{
			final File file = new File(configPositionsFile);
			final FileReader fr = new FileReader(file);
			final BufferedReader br = new BufferedReader(fr);

			String lastLine = null;

			positions = new Hashtable();

			while ((lastLine = br.readLine()) != null)
			{
				lastLine = lastLine.trim();

				if (lastLine.length() > 0)
				{
					final String[] bits = lastLine.split(",");

					final int posX = Integer.parseInt(bits[1]);
					final int posY = Integer.parseInt(bits[2]);

					positions.put(bits[0], new Point(posX, posY));
				}
			}
		}
		catch (final IOException e)
		{
			positions = null;
		}
	}

	public synchronized void setMessage(final DictionaryImpl newValue)
	{
		final DictionaryImpl oldValue = this.message;
		this.message = newValue;

		propertyChangeListeners.firePropertyChange("message", oldValue, newValue);

		final Hashtable hash = message.getHashtable();

		System.out.println("called setmessage");

		if (hash.containsKey(SUBJECT_KEY))
		{
			final String subjectText = getString(SUBJECT_KEY, hash);

			System.out.println("looking to see if answer if correct");

			if (subjectText.startsWith(SUBJECT_PREFIX))
			{
				System.out.println("anser in correct format");

				final String submittedAnswer = (subjectText.substring(SUBJECT_PREFIX.length() + 1)).trim();

				System.out.println("Found submitted answer: " + submittedAnswer);

				// now see what question the answer is for

				final String[] bits = submittedAnswer.split(":");

				if (bits.length == 2)
				{
					// see if this answer has been answered

					if (answers.containsKey(bits[0]))
					{
						final String realAnswer = (String) (answers.get(bits[0]));

						if (realAnswer.equals(bits[1]))
						{
							answers.remove(bits[0]);

							final String from = getString(FROM_KEY, hash);
							final String userID = getUserID(from);

							answeringUsers.put(bits[0], userID);

							System.out.println("correct answer");
						}
					}
					else
					{
						System.out.println("answer has already been given");
					}
				}
			}
		}
		else
		{
			System.out.println("can't find subject");
		}

		treePanel.repaint();
	}

	public void stop()
	{
		frame.dispose();
	}

	void drawStar(final Point centre, final Color back, final Color fore, final String text, final Graphics g)
	{
		final Polygon triangleOne = new Polygon();
		final Polygon triangleTwo = new Polygon();

		triangleOne.addPoint(centre.x, centre.y - (int) STAR_SIDE_LENGTH);

		triangleOne.addPoint(centre.x + ((int) (STAR_SIDE_LENGTH * COS_30)), centre.y
				+ ((int) (STAR_SIDE_LENGTH * SIN_30)));

		triangleOne.addPoint(centre.x - ((int) (STAR_SIDE_LENGTH * COS_30)), centre.y
				+ ((int) (STAR_SIDE_LENGTH * SIN_30)));

		triangleTwo.addPoint(centre.x + ((int) (STAR_SIDE_LENGTH * COS_30)), centre.y
				- ((int) (STAR_SIDE_LENGTH * SIN_30)));

		triangleTwo.addPoint(centre.x, centre.y + (int) STAR_SIDE_LENGTH);

		triangleTwo.addPoint(centre.x - ((int) (STAR_SIDE_LENGTH * COS_30)), centre.y
				- ((int) (STAR_SIDE_LENGTH * SIN_30)));

		g.setColor(back);

		g.fillPolygon(triangleOne);
		g.fillPolygon(triangleTwo);

		g.setColor(fore);
		g.drawString(text, centre.x, centre.y);
	}

	String getString(final String key, final Hashtable hash)
	{
		final Object ob = hash.get(key);

		if (ob instanceof String) { return (String) ob; }

		if (ob instanceof StringBoxImpl)
		{
			final StringBoxImpl sb = (StringBoxImpl) ob;
			return sb.value;
		}

		return ob.toString();
	}

	String getUserID(final String wholeAddress)
	{
		final int index = wholeAddress.indexOf('@');

		return (wholeAddress.substring(index - 3, index));
	}
}

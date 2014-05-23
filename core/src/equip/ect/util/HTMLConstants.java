package equip.ect.util;

/**
 * Defines a number of constants useful for constructing HTML descriptions of components.
 */

public class HTMLConstants
{
	public static String OPEN_TITLE = "<h1>";
	public static String CLOSE_TITLE = "</h1>";

	public static String OPEN_HEADING_1 = "<h2>";
	public static String CLOSE_HEADING_1 = "</h2>";

	public static String PARAGRAPH_END = "<BR><BR>";

	/**
	 * Call with information specific to a component to generate html documentation for a component
	 * in a standard format
	 */

	public static String createHTMLDescription(final String title, final String[] sectionHeaders,
			final String[] sectionTexts)
	{
		String htmlDescription = OPEN_TITLE + title + CLOSE_TITLE;

		for (int i = 0; i < sectionHeaders.length; i++)
		{
			htmlDescription = htmlDescription + OPEN_HEADING_1 + sectionHeaders[i] + CLOSE_HEADING_1;

			htmlDescription = htmlDescription + sectionTexts[i];

		}

		return htmlDescription;
	}
}

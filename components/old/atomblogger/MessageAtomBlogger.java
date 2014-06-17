package equip.ect.components.atomblogger;

import equip.data.DictionaryImpl;
import equip.data.StringBoxImpl;
import org.gnu.stealthp.rsslib.RSSHandler;

import java.util.Hashtable;

public class MessageAtomBlogger extends AtomBloggerBase
{
	public MessageAtomBlogger()
	{
		super();
	}

	public synchronized DictionaryImpl getMessage()
	{
		return null;
	}

	public synchronized void setMessage(final DictionaryImpl message)
	{
		if (message == null)
		{
			return;
		}

		// work out what entry to create from this message

		final Hashtable hash = message.getHashtable();

		if (hash == null)
		{
			return;
		}

		if (hash.containsKey("_containsRSS"))
		{
			final StringBoxImpl title = (StringBoxImpl) (hash.get("_rss." + RSSHandler.TITLE_TAG));

			final StringBoxImpl desc = (StringBoxImpl) (hash.get("_rss." + RSSHandler.DESCRIPTION_TAG));

			uploadEntry(title.value, desc.value);
		}
		else
		{
			// if there is a value field in this
			// dictionary, use it as the title
			// of the entry

			if (hash.containsKey("value"))
			{
				final String description = "&lt;no content&gt;";

				final Object value = hash.get("value");

				if (value instanceof String)
				{
					uploadEntry((String) value, description);
					return;
				}
				if (value instanceof StringBoxImpl)
				{
					final StringBoxImpl sb = (StringBoxImpl) value;
					uploadEntry(sb.value, description);
					return;
				}
			}
		}
	}
}

package equip.ect;

import java.util.HashMap;
import java.util.Map;

class BeanJarContent
{
	public static final String CLASSIFICATION_KEY = "CLASSIFICATION";
	public static final String SHORT_DESCRIPTION_KEY = "SHORT_DESCRIPTION";
	public static final String HTML_DESCRIPTION_KEY = "HTML_DESCRIPTION";
	public static final String DEFAULT_INPUT_PROPERTY_KEY = "DEFAULT_INPUT_PROPERTY";
	public static final String DEFAULT_OUTPUT_PROPERTY_KEY = "DEFAULT_OUTPUT_PROPERTY";
	public static final String ICON_KEY = "ICON";

	protected final Map<String, Map<String, String>> maps;

	public BeanJarContent()
	{
		maps = new HashMap<String, Map<String, String>>();
	}

	public final String get(final String beanClass, final String key)
	{
		final Map<String, String> beanContent = maps.get(beanClass);
		if (beanContent != null)
		{
			return beanContent.get(key);
		}
		return null;
	}

	public final Map<String, String> getBeanContent(final String beanClass)
	{
		return maps.get(beanClass);
	}

	public final void put(final String beanClass, final String key, final String value)
	{
		Map<String, String> beanContent = maps.get(beanClass);
		if (beanContent == null)
		{
			beanContent = createMap();
			maps.put(beanClass, beanContent);
		}
		beanContent.put(key, value);
	}

	private Map<String, String> createMap()
	{
		return new HashMap<String, String>(6);
	}
}
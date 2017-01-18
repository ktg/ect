package equip.ect;

import java.util.HashMap;
import java.util.Map;

class BeanJarContent
{
	static final String CLASSIFICATION_KEY = "CLASSIFICATION";
	static final String SHORT_DESCRIPTION_KEY = "SHORT_DESCRIPTION";
	static final String HTML_DESCRIPTION_KEY = "HTML_DESCRIPTION";
	static final String DEFAULT_INPUT_PROPERTY_KEY = "DEFAULT_INPUT_PROPERTY";
	static final String DEFAULT_OUTPUT_PROPERTY_KEY = "DEFAULT_OUTPUT_PROPERTY";
	static final String ICON_KEY = "ICON";

	private final Map<String, Map<String, String>> maps = new HashMap<>();

	BeanJarContent()
	{
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

	final Map<String, String> getBeanContent(final String beanClass)
	{
		return maps.get(beanClass);
	}

	public final void put(final String beanClass, final String key, final String value)
	{
		Map<String, String> beanContent = maps.computeIfAbsent(beanClass, k -> createMap());
		beanContent.put(key, value);
	}

	private Map<String, String> createMap()
	{
		return new HashMap<>(6);
	}
}
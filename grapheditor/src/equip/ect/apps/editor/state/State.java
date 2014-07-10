package equip.ect.apps.editor.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class State
{
	private final List<ComponentState> components = new ArrayList<ComponentState>();
	private final List<LinkState> links = new ArrayList<LinkState>();
	private final Map<String, List<ComponentState>> editors = new HashMap<String, List<ComponentState>>();

	public List<LinkState> getLinks()
	{
		return links;
	}

	public Map<String, List<ComponentState>> getEditors() { return editors; }

	public List<ComponentState> getComponents()
	{
		return components;
	}
}

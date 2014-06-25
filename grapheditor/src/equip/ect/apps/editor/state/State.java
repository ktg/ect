package equip.ect.apps.editor.state;

import java.util.ArrayList;
import java.util.List;

public class State
{
	private final List<ComponentState> components = new ArrayList<ComponentState>();
	private final List<LinkState> links = new ArrayList<LinkState>();

	public List<LinkState> getLinks()
	{
		return links;
	}

	public List<ComponentState> getComponents()
	{
		return components;
	}
}

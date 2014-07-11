package equip.ect.apps.editor.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class State
{
	private final List<ComponentState> components = new ArrayList<ComponentState>();
	private final List<LinkState> links = new ArrayList<LinkState>();
	private final List<EditorState> editors = new ArrayList<EditorState>();

	public List<LinkState> getLinks()
	{
		return links;
	}

	public List<EditorState> getEditors() { return editors; }

	public List<ComponentState> getComponents()
	{
		return components;
	}
}

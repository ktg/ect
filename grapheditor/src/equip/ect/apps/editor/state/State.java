package equip.ect.apps.editor.state;

import java.util.ArrayList;
import java.util.List;

public class State
{
	private final List<ComponentState> components = new ArrayList<>();
	private final List<LinkState> links = new ArrayList<>();
	private final List<EditorState> editors = new ArrayList<>();

	List<LinkState> getLinks()
	{
		return links;
	}

	List<EditorState> getEditors() { return editors; }

	List<ComponentState> getComponents()
	{
		return components;
	}
}

package equip.ect.apps.editor.state;

import java.util.ArrayList;
import java.util.List;

class EditorState
{
	private String name;
	private final List<ComponentState> components = new ArrayList<>();

	EditorState(final String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	List<ComponentState> getComponents()
	{
		return components;
	}
}

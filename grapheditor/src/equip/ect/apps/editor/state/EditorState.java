package equip.ect.apps.editor.state;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 11 Jul 2014.
 */
public class EditorState
{
	private String name;
	private final List<ComponentState> components = new ArrayList<ComponentState>();

	public EditorState()
	{

	}

	public EditorState(final String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public List<ComponentState> getComponents()
	{
		return components;
	}
}

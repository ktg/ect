package equip.ect.apps.editor.state;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import equip.ect.apps.editor.grapheditor.Drawer;

class ComponentState
{
	private String id;
	private String name;
	private String className;
	private Point position;
	private Drawer.State state;
	private final List<PropertyState> properties = new ArrayList<>();

	List<PropertyState> getProperties()
	{
		return properties;
	}

	public Drawer.State getState()
	{
		return state;
	}

	public void setState(Drawer.State state)
	{
		this.state = state;
	}

	String getId()
	{
		return id;
	}

	void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	String getClassName()
	{
		return className;
	}

	void setClassName(String className)
	{
		this.className = className;
	}

	Point getPosition()
	{
		return position;
	}

	void setPosition(Point position)
	{
		this.position = position;
	}
}

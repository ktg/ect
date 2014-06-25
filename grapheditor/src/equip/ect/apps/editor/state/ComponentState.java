package equip.ect.apps.editor.state;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class ComponentState
{
	private String id;
	private String name;
	private String className;
	private Point position;
	private int state;
	private final List<PropertyState> properties = new ArrayList<PropertyState>();

	public List<PropertyState> getProperties()
	{
		return properties;
	}

	public int getState()
	{
		return state;
	}

	public void setState(int state)
	{
		this.state = state;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
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

	public String getClassName()
	{
		return className;
	}

	public void setClassName(String className)
	{
		this.className = className;
	}

	public Point getPosition()
	{
		return position;
	}

	public void setPosition(Point position)
	{
		this.position = position;
	}
}

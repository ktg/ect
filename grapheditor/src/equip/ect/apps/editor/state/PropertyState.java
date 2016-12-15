package equip.ect.apps.editor.state;

class PropertyState
{
	private String name;
	//private String className;
	private String value;
	private Boolean keepVisible;
	private Boolean dynamic = null;

	int getPriority()
	{
		if(isDynamic())
		{
			return 3;
		}
		if(name != null)
		{
			if(name.equals("configured"))
			{
				return 1;
			}
			else if(name.startsWith("config"))
			{
				return 0;
			}
		}
		return 2;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	boolean isKeepVisible()
	{
		return keepVisible != null && keepVisible;
	}

	void setKeepVisible(boolean keepVisible)
	{
		this.keepVisible = keepVisible;
	}

	public boolean isDynamic()
	{
		return dynamic != null && dynamic;
	}

	void setDynamic(boolean dynamic)
	{
		this.dynamic = dynamic;
	}
}

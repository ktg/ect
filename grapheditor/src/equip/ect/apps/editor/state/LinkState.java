package equip.ect.apps.editor.state;

class LinkState
{
	private String sourceID;
	private String sourceProperty;
	private String targetID;
	private String targetProperty;

	String getTargetProperty()
	{
		return targetProperty;
	}

	void setTargetProperty(String targetProperty)
	{
		this.targetProperty = targetProperty;
	}

	String getSourceID()
	{
		return sourceID;
	}

	void setSourceID(String sourceID)
	{
		this.sourceID = sourceID;
	}

	String getSourceProperty()
	{
		return sourceProperty;
	}

	void setSourceProperty(String sourceProperty)
	{
		this.sourceProperty = sourceProperty;
	}

	String getTargetID()
	{
		return targetID;
	}

	void setTargetID(String targetID)
	{
		this.targetID = targetID;
	}
}

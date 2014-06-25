package equip.ect.apps.editor.state;

import equip.ect.PropertyLinkRequest;

public class LinkState
{
	private String sourceID;
	private String sourceProperty;
	private String targetID;
	private String targetProperty;

	public String getTargetProperty()
	{
		return targetProperty;
	}

	public void setTargetProperty(String targetProperty)
	{
		this.targetProperty = targetProperty;
	}

	public String getSourceID()
	{
		return sourceID;
	}

	public void setSourceID(String sourceID)
	{
		this.sourceID = sourceID;
	}

	public String getSourceProperty()
	{
		return sourceProperty;
	}

	public void setSourceProperty(String sourceProperty)
	{
		this.sourceProperty = sourceProperty;
	}

	public String getTargetID()
	{
		return targetID;
	}

	public void setTargetID(String targetID)
	{
		this.targetID = targetID;
	}

	public void restore()
	{
		// TODO
		//final PropertyLinkRequest l = new PropertyLinkRequest((GUID)null);
		//l.setSourcePropertyName(sourceProperty);
		//l.setDestinationPropertyName(targetProperty);
		//l.setSourcePropID(idMapping.get(stringToGUID(link.getAttribute(ATsrcpropref))));
		//l.setSourceComponentID(idMapping.get(stringToGUID(link.getAttribute(ATsrccompref))));

		//l.setDestinationPropID(idMapping.get(stringToGUID(link.getAttribute(ATdstpropref))));
		//l.setDestComponentID(idMapping.get(stringToGUID(link.getAttribute(ATdstcompref))));

		//dataspace.addPersistent(l.tuple, null);
	}
}

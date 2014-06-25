package equip.ect.apps.editor.state;

import equip.data.BooleanBox;
import equip.data.StringBox;
import equip.data.StringBoxImpl;
import equip.data.beans.DataspaceInactiveException;
import equip.ect.BeanDescriptorHelper;
import equip.ect.Capability;
import equip.ect.Coerce;
import equip.ect.ComponentAdvert;
import equip.ect.ComponentProperty;
import equip.ect.PropertyLinkRequest;
import equip.ect.apps.editor.BeanCanvasItem;
import equip.ect.apps.editor.BeanGraphPanel;
import equip.ect.apps.editor.dataspace.DataspaceMonitor;
import equip.ect.apps.editor.dataspace.DataspaceUtils;
import equip.ect.apps.editor.grapheditor.GraphComponent;
import equip.ect.apps.editor.grapheditor.GraphComponentProperty;
import equip.runtime.ValueBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateManager
{
	public static void restoreState(State state, BeanGraphPanel editor, ProgressDialog progress)
	{
		try
		{
			int progressLength = 0;
			for (final ComponentState componentState : state.getComponents())
			{
				progressLength += componentState.getProperties().size() + 1;
			}
			progressLength += state.getLinks().size();
			progress.setLength(progressLength);
			progress.setVisible(true);

			final DataspaceMonitor monitor = DataspaceMonitor.getMonitor();
			final Map<String, ComponentAdvert> componentMap = new HashMap<String, ComponentAdvert>();
			for (final ComponentState componentState : state.getComponents())
			{
				ComponentAdvert component = monitor.getComponentAdvert(componentState.getId());
				if (component == null)
				{
					component = monitor.createComponent(componentState.getClassName());
				}

				progress.increment();

				if (component != null)
				{
					progress.setStatus("Created Component " + componentState.getName());
					componentMap.put(componentState.getId(), component);

					StringBox value = new StringBoxImpl(componentState.getName());
					component.setAttribute(BeanDescriptorHelper.DISPLAY_NAME, value);
					component.updateinDataSpace(DataspaceMonitor.getMonitor().getDataspace());

					for (int stage = 0; stage < 5; stage++)
					{
						for (PropertyState propertyState : componentState.getProperties())
						{
							if (propertyState.getValue() != null && propertyState.getPriority() == stage)
							{
								ComponentProperty property = monitor.getComponentProperty(component.getID().toString(), propertyState.getName());
								if (property != null)
								{
									if (monitor.setProperty(property, propertyState.getValue()))
									{
										progress.setStatus("Set Property " + componentState.getName() + "." + propertyState.getName() + " to " + propertyState.getValue());
									}
									else
									{
										progress.setStatus("Failed to set Property " + componentState.getName() + "." + propertyState.getName() + " to " + propertyState.getValue());
									}
								}
								else
								{
									progress.setStatus("Property " + componentState.getName() + "." + propertyState.getName() + " not found");
								}
								progress.increment();
							}
						}
					}

					// Restore editor
					final BeanCanvasItem item = editor.createItem(component.getID().toString(), componentState.getPosition().x, componentState.getPosition().y);
					if (item instanceof GraphComponent)
					{
						GraphComponent graphComponent = (GraphComponent) item;
						graphComponent.getDrawer().setDrawerState(componentState.getState());
						for (GraphComponentProperty property : graphComponent.getGraphComponentProperties().values())
						{
							for (PropertyState propertyState : componentState.getProperties())
							{
								if (propertyState.getName().equals(property.getName()))
								{
									property.setKeepVisible(propertyState.isKeepVisible());
								}
							}
						}
					}
				}
				else
				{
					progress.setStatus("Failed to create Component " + componentState.getName());
				}
			}

			for (final LinkState linkState : state.getLinks())
			{
				final ComponentAdvert sourceComponent = componentMap.get(linkState.getSourceID());
				final ComponentAdvert targetComponent = componentMap.get(linkState.getTargetID());
				if (sourceComponent == null || targetComponent == null)
				{
					continue;
				}

				ComponentProperty sourceProperty = monitor.getComponentProperty(sourceComponent.getID().toString(), linkState.getSourceProperty());
				ComponentProperty targetProperty = monitor.getComponentProperty(targetComponent.getID().toString(), linkState.getTargetProperty());
				if (sourceProperty == null || targetProperty == null)
				{
					continue;
				}

				monitor.createLink(sourceProperty, targetProperty);

				progress.increment();
				progress.setStatus("Created Link " + linkState.getSourceProperty() + " -> " + linkState.getTargetProperty());
			}

			progress.finished();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static State createState(BeanGraphPanel editor) throws DataspaceInactiveException
	{
		final State state = new State();
		final DataspaceMonitor monitor = DataspaceMonitor.getMonitor();

		for (final ComponentAdvert component : monitor.getComponentAdverts().values())
		{
			final ComponentState componentState = new ComponentState();
			componentState.setId(component.getComponentID().toString());
			componentState.setName(DataspaceUtils.getCurrentName(component));

			final Capability capability = monitor.getComponentCapability(component);
			componentState.setClassName(capability.getCapabilityClass());

			final List<BeanCanvasItem> items = editor.getBeanInstances(component.getID().toString());
			if(items.size() > 0)
			{
				BeanCanvasItem item = items.get(0);
				componentState.setPosition(item.getPosition());
				if(item instanceof GraphComponent)
				{
					componentState.setState(((GraphComponent) item).getDrawer().getDrawerState());
				}
			}

			System.out.println("Items = " + items.size());

			for (final ComponentProperty property : monitor.getComponentProperties(component.getComponentID().toString()))
			{
				final PropertyState propertyState = new PropertyState();
				propertyState.setName(property.getPropertyName());

				boolean add = false;

				if(!property.isReadonly())
				{
					add = true;
					final ValueBase value = property.getAttributeValue("dynamic");
					if (value != null && value instanceof BooleanBox && ((BooleanBox) value).value)
					{
						propertyState.setDynamic(true);
					}

					try
					{
						final Object val = property.getPropertyValue();
						propertyState.setValue(Coerce.toClass(val, String.class));
					}
					catch (final Exception vex)
					{
						System.err.println("ERROR getting property value: " + vex);
						vex.printStackTrace(System.err);
					}
				}

				if(items.size() > 0)
				{
					BeanCanvasItem item = items.get(0);
					if(item instanceof GraphComponent)
					{
						for(GraphComponentProperty propertyItem: ((GraphComponent) item).getGraphComponentProperties().values())
						{
							if(propertyItem.getName().equals(property.getPropertyName()))
							{
								if(propertyItem.keepVisible())
								{
									propertyState.setKeepVisible(true);
									add = true;
								}
							}
						}
					}
				}

				if(add)
				{
					componentState.getProperties().add(propertyState);
				}
			}

			state.getComponents().add(componentState);
		}

		for (final PropertyLinkRequest link : monitor.getPropertyLinks())
		{
			final LinkState linkState = new LinkState();
			linkState.setSourceProperty(link.getSourcePropertyName());
			linkState.setTargetProperty(link.getDestinationPropertyName());
			if (link.getSourceComponentID() != null)
			{
				linkState.setSourceID(link.getSourceComponentID().toString());
			}
			if (link.getDestComponentID() != null)
			{
				linkState.setTargetID(link.getDestComponentID().toString());
			}

			state.getLinks().add(linkState);
		}

		return state;
	}
}

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
import equip.ect.apps.editor.dataspace.DataspaceMonitor;
import equip.ect.apps.editor.dataspace.DataspaceUtils;
import equip.ect.apps.editor.grapheditor.GraphComponent;
import equip.ect.apps.editor.grapheditor.GraphComponentProperty;
import equip.ect.apps.editor.grapheditor.GraphEditor;
import equip.ect.apps.editor.grapheditor.GraphEditorCanvas;
import equip.ect.apps.editor.interactive.InteractiveCanvasItem;
import equip.runtime.ValueBase;

import java.util.HashMap;
import java.util.Map;

public class StateManager
{
	public static void restoreState(final State state, final GraphEditor graphEditor, final ProgressDialog progress)
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

			graphEditor.removeAllCanvases();

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

					if (componentState.getPosition() != null)
					{
						GraphEditorCanvas editor = graphEditor.getActiveCanvas();
						if(editor == null)
						{
							editor = graphEditor.addCanvas("Editor");
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


			for (final EditorState editor : state.getEditors())
			{
				GraphEditorCanvas canvas =  graphEditor.addCanvas(editor.getName());
				for (ComponentState componentState : editor.getComponents())
				{
					ComponentAdvert component = componentMap.get(componentState.getId());
					// Restore editor
					final BeanCanvasItem item = canvas.createItem(component.getID().toString(), componentState.getPosition().x, componentState.getPosition().y);
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
			}


			progress.finished();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static State createState(GraphEditor editor) throws DataspaceInactiveException
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

			for (final ComponentProperty property : monitor.getComponentProperties(component.getComponentID().toString()))
			{
				final PropertyState propertyState = new PropertyState();
				propertyState.setName(property.getPropertyName());

				boolean add = false;

				if (!property.isReadonly())
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

				if (add)
				{
					componentState.getProperties().add(propertyState);
				}
			}

			state.getComponents().add(componentState);
		}

		if(monitor.getPropertyLinks() != null)
		{
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
		}

		for (final GraphEditorCanvas canvas : editor.getCanvases())
		{
			EditorState editorState = new EditorState(canvas.getName());
			for (InteractiveCanvasItem item : canvas.getItems())
			{
				if (item instanceof GraphComponent)
				{
					GraphComponent graphComponent = (GraphComponent) item;
					ComponentState componentState = new ComponentState();
					componentState.setId(graphComponent.getBeanID());
					componentState.setPosition(graphComponent.getPosition());
					componentState.setState(graphComponent.getDrawer().getDrawerState());

					for (GraphComponentProperty property : graphComponent.getGraphComponentProperties().values())
					{
						if (property.keepVisible())
						{
							PropertyState propertyState = new PropertyState();
							propertyState.setName(property.getName());
							propertyState.setKeepVisible(true);
							componentState.getProperties().add(propertyState);
						}
					}

					editorState.getComponents().add(componentState);
				}
			}
			state.getEditors().add(editorState);

		}

		return state;
	}
}

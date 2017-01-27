package equip.ect.components.ui;

import equip.ect.*;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.awt.Container;

@ECTComponent
@Category("UI")
public class GraphOutput extends UIBase implements DynamicProperties
{
	private final XYChart chart;
	private final DynamicPropertiesSupport dynamicProperties = new DynamicPropertiesSupport(propertyChangeListeners);


	public GraphOutput()
	{
		super();
		final Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BorderLayout());
		chart = new XYChartBuilder()
				.width(800)
				.height(600)
				.theme(Styler.ChartTheme.Matlab)
				//.title("ECT Graph")
				//.xAxisTitle("X")
				//.yAxisTitle("Y")
				.build();

		XChartPanel<XYChart> panel = new XChartPanel<>(chart);

		contentPane.add(panel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public DynamicPropertyDescriptor[] getDynamicProperties()
	{
		return dynamicProperties.getDynamicProperties();
	}

	@Override
	public Object getDynamicProperty(String name) throws NoSuchPropertyException
	{
		return dynamicProperties.getDynamicProperty(name);
	}

	@Override
	public void setDynamicProperty(String name, Object value) throws NoSuchPropertyException
	{
		dynamicProperties.setDynamicProperty(name, value);
	}
}

package equip.ect.components.functionfactory;

import equip.data.DictionaryImpl;
import ptolemy.plot.Plot;

import javax.swing.*;

public class FunctionPreviewPanel extends JPanel
{
	// number of points per unit x interval to render
	private static final int UNIT_RENDER_POINTS = 100;
	private Plot plot = new Plot();

	public FunctionPreviewPanel()
	{
		plot.setColor(false);
		add(plot);
	}

	public void renderFunction(final Function f)
	{
		final Runnable doAction = new Runnable()
		{
			@Override
			public void run()
			{
				plot.clear(0);

				final double[] sampleRange = f.getSampleRange();

				final double xMin = sampleRange[0];
				final double xMax = sampleRange[1];

				final double xRange = xMax - xMin;

				plot.setXRange(xMin, xMax);

				final int renderPoints = (int) (UNIT_RENDER_POINTS * xRange);

				final double xInc = xRange / renderPoints;

				for (int i = 0; i <= renderPoints; i++)
				{
					final double xPos = (i * xInc) + xMin;

					final DictionaryImpl di = DictionaryUtilities.constructDictionary(xPos, null);

					final DictionaryImpl yPos = f.calculate(di);

					plot.addPoint(0, xPos, DictionaryUtilities.getValue(yPos), true);
				}
			}
		};

		plot.deferIfNecessary(doAction);

	}
}

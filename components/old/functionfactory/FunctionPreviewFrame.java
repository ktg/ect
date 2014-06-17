package equip.ect.components.functionfactory;

import java.awt.Dimension;

import javax.swing.JFrame;

public class FunctionPreviewFrame extends JFrame
{
	// renders a preview of f, using default parameters that
	// it defines

	private static final Dimension DEFAULT_SIZE = new Dimension(500, 400);

	public FunctionPreviewFrame(final Function f)
	{
		final FunctionPreviewPanel p = new FunctionPreviewPanel();
		p.renderFunction(f);

		getContentPane().add(p);

		setTitle("Preview: " + f.getDisplayName());
		setSize(DEFAULT_SIZE);

		setVisible(true);
	}
}

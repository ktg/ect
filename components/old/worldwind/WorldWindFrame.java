package equip.ect.components.worldwind;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

public class WorldWindFrame extends JFrame
{
	WorldWindPanel wwjPanel;
	Dimension size;

	public WorldWindFrame(final Dimension size)
	{
		this.size = size;

		wwjPanel = new WorldWindPanel(size);
		getContentPane().add(wwjPanel, BorderLayout.CENTER);
		pack();

		setSize(size);
		setVisible(true);
	}

}

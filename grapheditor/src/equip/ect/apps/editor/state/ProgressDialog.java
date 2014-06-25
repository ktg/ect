package equip.ect.apps.editor.state;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ProgressDialog extends JFrame
{
	private final JLabel progressStatus = new JLabel();
	private final JProgressBar progress = new JProgressBar(0,100);
	private final JTextArea progressLog = new JTextArea();
	private final JButton doneButton;

	public ProgressDialog()
	{
		super("Loading...");

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent e)
			{
				setVisible(false);
			}
		});
		getContentPane().setLayout(new BorderLayout());

		final JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(500, 200));
		p.add(progressStatus);
		// SpringLayout pl = new SpringLayout();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		p.add(progress);

		progressLog.setEditable(false);
		final JScrollPane rsp = new JScrollPane(progressLog);
		p.add(rsp);
		doneButton = new JButton(new AbstractAction("Done")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				System.out.println("Done");
				setVisible(false);
			}
		});
		p.add(doneButton);
		doneButton.setEnabled(false);

		// wrap it up
		getContentPane().add(p, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(null);
	}

	public void setStatus(String status)
	{
		System.out.println(status);
		progressStatus.setText(status);
		try
		{
			progressLog.getDocument().insertString(progressLog.getDocument().getLength(), status + "\n", null);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}

	public void increment()
	{
		progress.setValue(progress.getValue() + 1);
	}

	public void setLength(int length)
	{
		progress.setValue(0);
		progress.setMinimum(0);
		progress.setMaximum(length);
	}

	public void finished()
	{
		doneButton.setEnabled(true);
	}
}

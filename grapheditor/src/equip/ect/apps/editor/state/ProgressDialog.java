package equip.ect.apps.editor.state;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;

public class ProgressDialog extends JFrame
{
	private final JLabel progressStatus = new JLabel();
	private final JProgressBar progress = new JProgressBar(0, 100);
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

	public void setLength(int length)
	{
		progress.setValue(0);
		progress.setMinimum(0);
		progress.setMaximum(length);
	}

	void setStatus(String status)
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

	void increment()
	{
		progress.setValue(progress.getValue() + 1);
	}

	void finished()
	{
		setStatus("Finished");
		progress.setValue(progress.getMaximum());
		doneButton.setEnabled(true);
	}
}

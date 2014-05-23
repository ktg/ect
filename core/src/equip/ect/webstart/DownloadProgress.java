/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Nottingham
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the University of Nottingham
   nor the names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

</COPYRIGHT>

Created by: Shahram Izadi (University of Nottingham)
Contributors:
  Shahram Izadi (University of Nottingham)

 */
package equip.ect.webstart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class DownloadProgress extends JFrame
{

	JProgressBar progress = null;
	JLabel label = null;
	int max = 1;

	public DownloadProgress(final File file, final int max)
	{
		this.max = max;
		final java.awt.Container content = getContentPane();
		String filename = "Unknown";
		if (file != null)
		{
			filename = file.getName();
		}
		setTitle("File download " + filename);
		content.setLayout(new BorderLayout());
		progress = new JProgressBar(0, max); // bytes
		content.add(label = new JLabel("Downloading " + filename + "..."), BorderLayout.NORTH);
		content.add(progress);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		pack();
		setVisible(true);
	}

	public void failed(final Exception e, final File file)
	{
		label.setForeground(Color.red);
		label.setText("ERROR copying remote file to local file " + file + ": " + e);
	}

	public void incrementValue(final int amount)
	{
		setValue(progress.getValue() + amount);
	}

	public void setValue(final int value)
	{
		try
		{
			SwingUtilities.invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					progress.setValue(value);
				}
			});
		}
		catch (final Exception e)
		{
		}
	}
}

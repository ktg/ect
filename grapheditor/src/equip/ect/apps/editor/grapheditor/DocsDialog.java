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

 Created by: Stefan Rennick Egglestone (University of Nottingham)
 Contributors:

 Stefan Rennick Egglestone (University of Nottingham)
 Jan Humble (University of Nottingham)
 
 */

package equip.ect.apps.editor.grapheditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import equip.data.StringBox;
import equip.ect.Capability;
import equip.ect.apps.editor.HTMLDescriptionHelper;
import equip.ect.apps.editor.HTMLException;

/**
 * <p>
 * Displays htmlDescription from component bean info in a dialog
 * </P>
 * <p>
 * An instance of this class is constructed when the user selects the "view docs" option on the
 * context menu for a component in the the capability browser.
 * </P>
 */
class DocsDialog extends JDialog
{
	DocsDialog(final JFrame parent, final Capability cap)
	{
		super(parent, "Documentation for " + cap.getCapabilityName(), false);
		getContentPane().setLayout(new BorderLayout());

		setSize(new Dimension(800, 600));

		final StringBox sb = (StringBox) (cap.getAttributeValue("htmlDescription"));
		String htmlString = sb.value;

		try
		{

			// passing true to the constructor indicates that
			// header section should be removed from
			// html document. Important as jeditorpane bug
			// means that some head sections cause rendering errors
			final HTMLDescriptionHelper hh = new HTMLDescriptionHelper(htmlString, true);

			// hh will do html correction (eg insert html etc tags if
			// developer has missed them

			htmlString = hh.getCorrectedHTML();

			// System.out.println("corrected: " + htmlString);

			// first, check to see if any interesting headings exist. If they
			// do, then display as tabs. If not, then just display
			// html string

			if (hh.getHeadingsExist())
			{
				final JTabbedPane tp = new JTabbedPane();
				getContentPane().add(tp, BorderLayout.CENTER);

				final JEditorPane htmlPane = new JEditorPane("text/html", htmlString);
				htmlPane.setEditable(false);
				JScrollPane sp = new JScrollPane(htmlPane);
				sp.setViewportView(htmlPane);

				sp.getVerticalScrollBar().setValue(0);

				tp.add("All documentation", sp);

				final String[] interestingHeadings = hh.getAllEditorHeadings();

				for (final String interestingHeading : interestingHeadings)
				{
					// add text for each heading to
					// an editor pane capabel of displaying
					// html.
					// note - will need to add html, body elements
					// to these html fragments so that they
					// are properly formed for display in editor pane

					final String sectionText = hh.getSectionText(interestingHeading);
					if (sectionText != null)
					{
						// sectionText =
						// "<HTML><BODY>" + sectionText +
						// "</BODY></HTML>";
						final JEditorPane disp = new JEditorPane("text/html", sectionText);
						disp.setEditable(false);

						sp = new JScrollPane(disp);

						sp.setViewportView(disp);

						tp.add(interestingHeading, sp);
					}
				}
			}
			else
			{
				final JEditorPane disp = new JEditorPane("text/html", htmlString);
				disp.setEditable(false);

				final JScrollPane sp = new JScrollPane(disp);

				sp.setViewportView(disp);
				getContentPane().add(sp, BorderLayout.CENTER);
			}
		}
		catch (final HTMLException e)
		{
			// if here, there is some problem with the format
			// of the html description which is part of the component
			// so display some details to user

			// error message in one tab, plus
			// exception trace in another

			final JTabbedPane tp = new JTabbedPane();
			getContentPane().add(tp, BorderLayout.CENTER);

			final String shortErrorString = "There is a major error in some HTML generated by "
					+ "the component that you have selected, so its " + "documentation is not viewable.";

			final JEditorPane shortErrorPane = new JEditorPane("text/plain", shortErrorString);

			shortErrorPane.setEditable(false);
			final JScrollPane sp = new JScrollPane(shortErrorPane);
			sp.setViewportView(shortErrorPane);

			tp.add("Error", sp);

			// if an exception exists, then place on a tab

			final Throwable t = e.getCause();

			if (t != null)
			{
				// now write exception to a string
				final StringWriter sw = new StringWriter();
				t.printStackTrace(new PrintWriter(sw));

				final String exceptionString = sw.toString();

				final JEditorPane exceptionPane = new JEditorPane("text/plain", exceptionString);

				exceptionPane.setEditable(false);
				final JScrollPane sp2 = new JScrollPane(exceptionPane);
				sp2.setViewportView(exceptionPane);

				tp.add("Exception trace", sp2);
			}
		}

		validate();
		setVisible(true);
	}
}

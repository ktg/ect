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

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)

 */
package equip.ect.components.floatfunction;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class FloatFunction extends JFrame implements Serializable
{

	protected static class Dump extends Thread
	{
		java.io.InputStream in;

		Dump(final java.io.InputStream in)
		{
			this.in = in;
		}

		@Override
		public void run()
		{
			try
			{
				final java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(this.in));
				do
				{
					final String s = in.readLine();
					if (s == null) { return; }
					System.out.println("Dump: " + s);
				}
				while (true);
			}
			catch (final Exception e)
			{
				System.err.println("Dump ERROR: " + e);
			}
		}
	}

	public static void main(final String[] args)
	{
		new ect.components.floatfunction.FloatFunction();
	}

	private String title = "Float Function";
	private JTextArea textArea = new JTextArea();

	private Dimension defaultSize = new Dimension(300, 100);

	private JButton update = new JButton("update");

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	// Property
	private float in1, in2, in3, out;
	static int version = 1;
	protected java.lang.reflect.Method method;
	protected Object obj;

	public FloatFunction()
	{
		setTitle(title);
		final Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(new JScrollPane(textArea), BorderLayout.CENTER);
		contentPane.add(update, BorderLayout.EAST);
		update.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				try
				{
					updateScript(textArea.getText());
				}
				catch (final Exception e)
				{
					System.err.println("ERROR: " + textArea.getText() + ": " + e);
				}
			}
		});
		setSize(defaultSize);
		setVisible(true);
	}

	@Override
	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public float getIn1()
	{
		return in1;
	}

	public float getIn2()
	{
		return in2;
	}

	public float getIn3()
	{
		return in3;
	}

	public float getOut()
	{
		return out;
	}

	// Property Change Listeners
	@Override
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public void setIn1(final float val)
	{
		propertyChangeListeners.firePropertyChange("in1", new Float(this.in1), new Float(val));

		this.in1 = val;
		runScript();
	}

	public void setIn2(final float val)
	{
		propertyChangeListeners.firePropertyChange("in2", new Float(this.in2), new Float(val));
		this.in2 = val;
		runScript();
	}

	public void setIn3(final float val)
	{
		propertyChangeListeners.firePropertyChange("in3", new Float(this.in3), new Float(val));
		this.in3 = val;
		runScript();
	}

	public void setOut(final float val)
	{
		propertyChangeListeners.firePropertyChange("out", new Float(this.out), new Float(val));
		this.out = val;
	}

	protected void runScript()
	{
		try
		{
			final Object res = method.invoke(obj, new Object[] { new Float(in1), new Float(in2), new Float(in3) });
			final float out = ((Float) res).floatValue();
			System.out.println("Run fn: " + in1 + ", " + in2 + ", " + in3 + " -> " + out);
			setOut(out);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR running script: " + e);
		}
	}

	protected void updateScript(final String text)
	{
		System.out.println("Update script: " + text);
		final String functor = "public class ScriptFunctor" + (++version) + " { public ScriptFunctor" + version
				+ "(){} public float eval(float in1, float in2, float in3) { return (" + text + "); } }";
		// com.sun.javac.tools.Main
		final String className = "ScriptFunctor" + version;
		try
		{
			final java.io.File tmpout = new java.io.File(className + ".java");
			final String tmppath = tmpout.getCanonicalPath();
			System.out.println("Write to file " + tmppath);
			final java.io.PrintWriter out = new java.io.PrintWriter(new java.io.FileOutputStream(tmpout));
			out.println(functor);
			out.close();
			// compile
			final String cmd = "javac \"" + tmppath + "\"";
			System.out.println("Compile: " + cmd);
			final Process p = Runtime.getRuntime().exec(cmd);
			new Dump(p.getInputStream()).start();
			new Dump(p.getErrorStream()).start();
			p.waitFor();
			final java.io.File tmpclass = new java.io.File(tmppath.substring(0, tmppath.lastIndexOf("ScriptFunctor"))
					+ "ScriptFunctor" + version + ".class");
			System.out.println("Load " + tmpclass + "...");
			final java.io.FileInputStream fin = new java.io.FileInputStream(tmpclass);
			final long size = fin.available();
			final byte[] data = new byte[(int) size];
			fin.read(data);
			fin.close();
			System.out.println("Read " + size + " bytes");
			final ClassLoader cl = new ClassLoader(this.getClass().getClassLoader())
			{
				@Override
				public Class loadClass(final String name) throws ClassNotFoundException
				{
					if (name.equals(className)) { return defineClass(null, data, 0, data.length); }
					return super.loadClass(name);
				}
			};
			final Class clazz = cl.loadClass(className);
			final java.lang.reflect.Constructor cons = clazz.getConstructor(new Class[0]);
			obj = cons.newInstance(new Object[0]);
			final Class floatclass = Float.TYPE; // Class.forName("float");
			method = clazz.getDeclaredMethod("eval", new Class[] { floatclass, floatclass, floatclass });
			System.out.println("OK");

			runScript();
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}

}

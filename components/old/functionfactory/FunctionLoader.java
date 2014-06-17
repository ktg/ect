package equip.ect.components.functionfactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;

public class FunctionLoader
{
	private Function[] functions;

	public FunctionLoader() throws IOException
	{
		final URL classListURL = this.getClass().getResource("/ect/components/functionfactory/functionslist.txt");

		final InputStream is = classListURL.openStream();
		final InputStreamReader isr = new InputStreamReader(is);
		final BufferedReader br = new BufferedReader(isr);

		String lastLine = null;

		final Vector classNames = new Vector();

		while ((lastLine = br.readLine()) != null)
		{
			if (lastLine.length() > 0)
			{
				classNames.add(lastLine);
			}
		}

		final Vector functionsVector = new Vector();

		for (int i = 0; i < classNames.size(); i++)
		{
			try
			{
				final String className = (String) (classNames.elementAt(i));

				System.out.println("Function factory: attempting to load " + className);

				final Class c = Class.forName(className);
				final Function f = (Function) (c.newInstance());

				functionsVector.add(f);
				System.out.println("Function factory: successfully loaded");
			}
			catch (final InstantiationException e)
			{
				e.printStackTrace();
			}
			catch (final IllegalAccessException e)
			{
				e.printStackTrace();
			}
			catch (final ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}

		functions = (Function[]) functionsVector.toArray(new Function[functionsVector.size()]);

	}

	public Function[] getFunctions()
	{
		return functions;
	}
}

package equip.ect.components.x10.javaX10project;

import java.io.Serializable;

public class CommandPair implements Serializable
{

	protected Command first;
	protected Command second;
	protected boolean executing = false;

	public CommandPair(final Command first, final Command second)
	{
		this.first = first;
		this.second = second;
	}

	@Override
	public boolean equals(final Object obj)
	{
		CommandPair pair;
		if (obj instanceof CommandPair)
		{
			pair = (CommandPair) obj;
			return (pair.getFirst().equals(this.getFirst()) && pair.getSecond().equals(this.getSecond()));
		}
		else
		{
			return false;
		}
	}

	public Command getFirst()
	{
		return this.first;
	}

	public Command getSecond()
	{
		return this.second;
	}

	public boolean isExecuting()
	{
		return this.executing;
	}

	public void setExecuting(final boolean executing)
	{
		this.executing = executing;
	}

	public void setFirst(final Command first)
	{
		this.first = first;
	}

	public void setSecond(final Command second)
	{
		this.second = second;
	}

	@Override
	public String toString()
	{
		return "Pair<" + first.toString() + ", " + second.toString()
				+ ((this.isExecuting()) ? "pair_executing" : "pair_queued") + ">";
	}

}
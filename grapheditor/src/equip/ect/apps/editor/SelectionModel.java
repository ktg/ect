package equip.ect.apps.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SelectionModel
{
	public void clear()
	{
		if (selected.size() > 0)
		{
			selected.clear();
			fireChange();
		}
	}

	public interface SelectionListener
	{
		void selectionChanged(Collection<String> selection);
	}

	private final Set<SelectionListener> listeners = new HashSet<>();
	private Set<String> selected = new HashSet<>();

	public Collection<String> getSelected()
	{
		return selected;
	}

	public void add(SelectionListener listener)
	{
		listeners.add(listener);
	}

	public void add(String selected)
	{
		if (this.selected.add(selected))
		{
			fireChange();
		}
	}

	public void remove(String selected)
	{
		if (this.selected.remove(selected))
		{
			fireChange();
		}
	}

	public void set(String... selected)
	{
		if (!sameSelection(selected))
		{
			this.selected.clear();
			Collections.addAll(this.selected, selected);
			fireChange();
		}
	}

	public void set(Collection<String> selected)
	{
		if (!sameSelection(selected))
		{
			this.selected.clear();
			this.selected.addAll(selected);
			fireChange();
		}
	}

	private boolean sameSelection(String... selected)
	{
		if (selected.length != this.selected.size())
		{
			return false;
		}

		for (String selection : selected)
		{
			if (!this.selected.contains(selection))
			{
				return false;
			}
		}

		return true;
	}

	private boolean sameSelection(Collection<String> selected)
	{
		return selected.size() == this.selected.size() && this.selected.containsAll(selected);
	}

	private void fireChange()
	{
		for (SelectionListener listener : listeners)
		{
			listener.selectionChanged(selected);
		}
	}
}

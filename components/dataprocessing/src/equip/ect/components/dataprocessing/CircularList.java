package equip.ect.components.dataprocessing;

import java.util.Iterator;

public class CircularList implements Iterable<Double> 
{
	private double[] array;
	private int size = 0;
	private int maxSize;
	private int offset = 0;
	
	public CircularList()
	{
		
	}
	
	public CircularList(int i)
	{
		setMaxSize(i);
	}

	public int size()
	{
		return size;
	}

	public boolean isEmpty()
	{
		return size == 0;
	}

	@Override
	public Iterator<Double> iterator()
	{
		return new Iterator<Double>()
		{
			int count = 0;
			
			@Override
			public boolean hasNext()
			{
				return count < size;
			}

			@Override
			public Double next()
			{
				final int index = (count + offset) % size;
				final Double result = array[index];
				count++;
				return result;
			}

			@Override
			public void remove()
			{
			}
		};
	}

	public int getOffset()
	{
		return offset;
	}
	
	public boolean add(Double e)
	{
		array[offset] = e;
		
		if(size < maxSize)
		{
			size++;
		}
		
		offset = nextValue(offset);
		return true;
	}
	
	private int nextValue(int value)
	{
		return (value + 1) % maxSize;
	}
	
	public double get(int index)
	{
		return array[index];
	}
	
	public int maxSize()
	{
		return maxSize;
	}
	
	public void setMaxSize(final int maxSize)
	{
		if(this.maxSize == maxSize)
		{
			return;
		}
		size = 0;
		offset = 0;
		this.maxSize = maxSize;
		array = new double[maxSize];
	}
}
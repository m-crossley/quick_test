package ALife;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import processing.core.PVector;
import fisica.FCircle;
import fisica.FJoint;

public class Cell implements Comparable {

	public enum Originators
	{
		NORTH,
		SOUTH,
		EAST,
		WEST;
	}	
	
	public enum CellType
	{
		CENTRAL(100, 100, 100, 0f),
		NORTH(170, 63, 57, 0.2f),
		SOUTH(60, 141, 47, 0.2f),
		EAST(170, 151, 57, 0.2f),
		WEST(62, 49, 117, 0.2f),
		NORTHEAST(170, 117, 57, 0.8f),
		//SOUTHEAST(159, 168, 56),
		SOUTHEAST(241, 253, 84, 0.8f),
		SOUTHWEST(38, 91, 106, 0.8f),
		NORTHWEST(103, 39, 112, 0.8f);
		
		Color color;
		float probabilityOfBreeding;
		
		private CellType(int r, int g, int b, float pob)
		{
			color = new Color(r, g, b);
			probabilityOfBreeding = pob;
		}
		
		public float getProbabilityOfBreeding()
		{
			return probabilityOfBreeding;
		}
		
		public Color getColor()
		{
			return color;
		}
		
	}	
	
	private static int cellCount = 0;

	FJoint joint;
	FCircle body;
	int created;
	int value;
	boolean pushing = false;
	PVector position;
	PVector velocity;
	boolean valueSet = false;
	boolean settled = false;
	//int parent;
	float f, h;
	int g;

	float eLength = 40.0f;
	float springiness = 1000.0f;
	float velocityMultiplier = 0.98f;

	HashMap<Originators, Integer> genomicValues = new HashMap<Originators, Integer>();
	HashMap<Originators, Boolean> genomicOriginator = new HashMap<Originators, Boolean>();

	int red = 0;
	int blue = 0;
	int green = 0;

	public int getSuitability(Originators o)
	{

		if (o == Originators.NORTH)
		{
			return genomicValues.get(Originators.SOUTH) - Math.abs(genomicValues.get(Originators.EAST)-genomicValues.get(Originators.WEST));
		}
		else if (o == Originators.SOUTH)
		{
			return genomicValues.get(Originators.NORTH) - Math.abs(genomicValues.get(Originators.EAST)-genomicValues.get(Originators.WEST));
		}
		else if (o == Originators.EAST)
		{
			return genomicValues.get(Originators.WEST) - Math.abs(genomicValues.get(Originators.NORTH)-genomicValues.get(Originators.SOUTH));
		}
		else if (o == Originators.WEST)
		{
			return genomicValues.get(Originators.EAST) - Math.abs(genomicValues.get(Originators.NORTH)-genomicValues.get(Originators.SOUTH));
		}
		return 0;
	}

	public int getXAxis()
	{
		return genomicValues.get(Originators.WEST) - genomicValues.get(Originators.EAST);
	}
	
	public int getYAxis()
	{
		return genomicValues.get(Originators.SOUTH) - genomicValues.get(Originators.NORTH);
	}
	
	public CellType getType()
	{
		int xAxis = getXAxis();
		int yAxis = getYAxis();

		boolean north = false;
		boolean south = false;
		boolean east = false;
		boolean west = false;
		
		if (xAxis > 2) east = true;
		if (xAxis < -2) west = true;
		if (yAxis > 2) north = true;
		if (yAxis < -2) south = true;

		if (north && west)
		{
			return CellType.NORTHWEST;
		}
		if (north && east)
		{
			return CellType.NORTHEAST;
		}
		if (south && west)
		{
			return CellType.SOUTHWEST;
		}
		if (south && east)
		{
			return CellType.SOUTHEAST;
		}
		if (north)
		{
			return CellType.NORTH;
		}
		if (south)
		{
			return CellType.SOUTH;
		}
		if (east)
		{
			return CellType.EAST;
		}
		if (west)
		{
			return CellType.WEST;
		}
		return CellType.CENTRAL;
	}
	
	public void setOriginator(Originators o, boolean to)
	{
		genomicOriginator.put(o, to);
	}

	public void setGenomicValue(Originators o, int value)
	{
		genomicValues.put(o, value);
	}

	public int getGenomicValue(Originators o)
	{
		if (isOriginatorFor(o))
		{
			return 0;
		}
		else
		{
			return genomicValues.get(o);
		}
	}

	public boolean isOriginatorFor(Originators o)
	{
		return genomicOriginator.get(o);
	}

	public boolean isOriginator()
	{
		return getOriginators().size() > 0;
	}

	public ArrayList<Originators> getOriginators()
	{
		ArrayList<Originators> returner = new ArrayList<Originators>();
		for (Originators o : Originators.values())
		{
			if (genomicOriginator.get(o))
			{
				returner.add(o);
			}
		}
		return returner;
	}

	public Cell (float x, float y)
	{
		position = new PVector(x, y);
		velocity = new PVector(0, 0);
		value = 0;
		created = cellCount++;

		for (Originators o : Originators.values())
		{
			genomicValues.put(o, 0);
			genomicOriginator.put(o, false);
		}

	}

	public int compareTo(Object o)
	{
		Cell c = (Cell) o;
		return created - c.created;
	}

	void react(ArrayList<Cell> chain)
	{
		settled = true;
		for (int i = 0; i < chain.size(); i++)
		{
			if (chain.get(i) != this)
			{
				float d = PVector.dist(position, chain.get(i).position);
				float x = d - eLength;
				PVector dir = PVector.sub(chain.get(i).position, position);
				dir.normalize();
				dir.mult(x / springiness);
				velocity.add(dir);
			}
		}
		velocity.mult(velocityMultiplier);
		if (velocity.mag() > 0.01)
		{
			settled = false;
		}
	}

	void move()
	{
		position.add(velocity);
	}
}

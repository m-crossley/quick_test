package ALife;

import fisica.FCircle;
import fisica.FCompound;
import fisica.FDistanceJoint;
import fisica.FWorld;
import fisica.Fisica;
import gifAnimation.GifMaker;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;

import ALife.Cell.Originators;
import megamu.mesh.Delaunay;
import processing.core.PApplet;
import processing.data.IntList;
import processing.event.MouseEvent;



public class ALifePrototype extends PApplet
{
	enum States
	{
		DESIGNING, 
		GROWING, 
		SETTLING, 
		HATCHING, 
		STABILISING, 
		LIVING;
	}

	States currentState = States.DESIGNING; 

	GifMaker gm;

	int maximumCells = 140;

	float offsetX = 0;
	float offsetY = 0;
	float scale = 1;

	boolean recordgif = false;
	boolean outputframes = false;
	
	void addPoint(float x, float y)
	{
		addPoint(x, y, new ArrayList<Originators>());
	}

	void addPoint(float x, float y, Originators o)
	{
		ArrayList<Originators> oo = new ArrayList<Originators>();
		oo.add(o);
		addPoint(x, y, oo); 
	}

	void addPoint(float x, float y, ArrayList<Originators> o)
	{
		Cell c = new Cell(x, y);

		for (Originators oo : o)
		{
			c.setOriginator(oo, true);
		}

		if (clickCount % 3 == 0)
		{
			c.red = 255;
			c.green = 0;
			c.blue = 0;
		}
		if (clickCount % 3 == 1)
		{
			c.red = 0;
			c.green = 255;
			c.blue = 0;
		}
		if (clickCount % 3 == 2)
		{
			c.red = 0;
			c.green = 0;
			c.blue = 255;
		}
		cells.add(c);
	}

	public void mouseWheel(MouseEvent me)
	{
		if (currentState == States.DESIGNING)
		{
			maximumCells += -me.getCount();
			maximumCells = max(4, maximumCells);
		}
		else
		{
			if (me.getCount() < 0)
			{
				scale *= 1.1;
			} else
			{
				scale *= 0.9;
			}
		}
	}

	public void mouseDragged()
	{

		offsetX += mouseX - pmouseX;
		offsetY += mouseY - pmouseY;
	}
	
	String mouseOverText = "";
	public void mouseMoved()
	{
		boolean overCell = false;
		for (Cell c : cells)
		{
			if (dist(c.position.x, c.position.y, (mouseX - offsetX)/scale, (mouseY - offsetY)/scale) < 30)
			{
				overCell = true;
				mouseOverText = "Cell " + c.created + ": " + c.getType() + "(" + c.getXAxis() + ", " + c.getYAxis() + ")";
			}
		}
		if (!overCell)
		{
			mouseOverText = "";
		}
	}

	int currentCount = 0;
	int clickCount = 0;
	public void mouseClicked()
	{
		if (currentState == States.DESIGNING)
		{
			if (mouseButton == LEFT)
			{
				//ArrayList<Originators> orig = new ArrayList<Originators>();
				//orig.add(Originators.NORTH);
				//orig.add(Originators.SOUTH);
				//orig.add(Originators.EAST);
				//orig.add(Originators.WEST);
				//addPoint((mouseX - offsetX)/scale, (mouseY - offsetY)/scale, orig);
				addPoint((mouseX - offsetX)/scale, (mouseY - offsetY - 30)/scale, Originators.NORTH);
				addPoint((mouseX - offsetX)/scale, (mouseY - offsetY + 30)/scale, Originators.SOUTH);
				addPoint((mouseX - offsetX + 30)/scale, (mouseY - offsetY)/scale, Originators.EAST);
				addPoint((mouseX - offsetX - 30)/scale, (mouseY - offsetY)/scale, Originators.WEST);
				clickCount++;
			}
		}
		if (mouseButton == RIGHT)
		{
			if (currentState == States.DESIGNING && cells.size() > 0)
			{
				currentState = States.GROWING;
			}
		}
	}

	public void settings()
	{
		size(600, 600);
	}

	Delaunay myDelaunay;
	ArrayList<Cell> cells;
	public void setup()
	{
		frameRate(280);
		if (recordgif)
		{
			gm = new GifMaker(this, "export.gif");
			gm.setRepeat(0);
		}
		//reset();
		Fisica.init(this);
		cells = new ArrayList<Cell>();
	}

	void tick()
	{
		if (cells.size() < maximumCells)
		{
			ArrayList<Cell> offspringCells = new ArrayList<Cell>();
			for (Cell c : cells)
			{
				float probabilityOfBreeding = 0.3f;
				
				if (cells.size() > 50)
				{
					probabilityOfBreeding = c.getType().getProbabilityOfBreeding();
				}

				if ((Math.random() < probabilityOfBreeding))
				{
					Cell newCell = new Cell(c.position.x + random(30)-15, c.position.y + random(30)-15);
					boolean valid = true;
					for (Cell cc : cells)
					{
						if (cc.position.dist(newCell.position) < 15)
						{
							//valid = false;
						}
					}
					for (Cell cc : offspringCells)
					{
						if (cc.position.dist(newCell.position) < 15)
						{
							//valid = false;
						}
					}
					if (valid && cells.size() + offspringCells.size() < maximumCells)
					{
						offspringCells.add(newCell);
					}
				}
			}
			cells.addAll(offspringCells);
		} else
		{
			currentState = States.SETTLING;
		}
	}

	//cells.get(cell).value = valueToSet;
	//cells.get(cell).valueSet = true;

	//int[] localLinks = myDelaunay.getLinked(cell);
	//for (int i : localLinks)
	//{
	//  print(i + " ");
	//}
	//println(": " + valueToSet++);
	//for (int i : localLinks)
	//{
	//  setValue(i, valueToSet+1);
	//}


	int getDistanceFromTo(Cell from, Cell to)
	{
	  if (from.equals(to)) // || from > cells.size())
	  {
		    return 0;
	  }
	  for (Cell c : cells)
	  {
		    c.f = 0;
		    c.g = 0;
		    c.h = 0;
	  }

	  ArrayList<Cell> openList = new ArrayList<Cell>();
	  ArrayList<Cell> closedList = new ArrayList<Cell>();

	  openList.add(from);
	  while (openList.size() > 0)
	  {
		    float minF = 10000000;
		    Cell current = null;

		    for (Cell ci: openList)
		    {
		      if (ci.f < minF)
		      {
		        minF = ci.f;
		        current = ci;
		      }
		    }

		    openList.remove(current);

		    for (Cell cs : getNeighbours(current))
		    {
		      if (cs.equals(to))
		      {
		        return current.g+1;
		      }
		      cs.g = current.g + 1;
		      cs.h = cs.position.dist(to.position);
		      cs.f = cs.g + cs.h;

		      if (!openList.contains(cs) && !closedList.contains(cs))
		      {
		        openList.add(cs);
		      }
		    }

		    closedList.add(current);
	  }
	  return 0;
	}

	void drawGraphCreature()
	{
		stroke(0);
		if (cells.size() > 0)
		{
			for (Cell c : cells)
			{
//				if (c.isOriginator())
//				{
//					fill(0);
//					stroke(100);
//					strokeWeight(2);
//				} else
//				{
//					fill(100);
//					stroke(0);
//					strokeWeight(1);
//				}
				Color color = c.getType().getColor();
				fill(color.getRed(), color.getGreen(), color.getBlue());
				
				if (c.isOriginator())
				{
					strokeWeight(3);
				}
				else
				{
					strokeWeight(1);
				}
				
				ellipse(c.position.x, c.position.y, 30, 30);
				textSize(12);
				fill(255);
				textAlign(CENTER, CENTER);

//				if (c.getOriginators().contains(Originators.NORTH))
//				{
//					fill(0, 0, 255);
//					text("Y", c.position.x, c.position.y-8);
//				}
//				else
//				{
//					fill(0, 0, 255);
//					text(c.getGenomicValue(Originators.NORTH), c.position.x, c.position.y-8);
//				}
//				if (c.getOriginators().contains(Originators.SOUTH))
//				{
//					fill(255, 255, 0);
//					text("Y", c.position.x, c.position.y+10);
//				}
//				else
//				{
//					fill(255, 255, 0);
//					text(c.getGenomicValue(Originators.SOUTH), c.position.x, c.position.y+8);
//				}
//				if (c.getOriginators().contains(Originators.EAST))
//				{
//					fill(255, 0, 0);
//					text("Y", c.position.x+8, c.position.y);
//				}
//				else
//				{
//					fill(255, 0, 0);
//					text(c.getGenomicValue(Originators.EAST), c.position.x+8, c.position.y);
//				}
//				if (c.getOriginators().contains(Originators.WEST))
//				{
//					fill(0, 255, 0);
//					text("Y", c.position.x-8, c.position.y);
//				}
//				else
//				{
//					fill(0, 255, 0);
//					text(c.getGenomicValue(Originators.WEST), c.position.x-8, c.position.y);
//				}
			}
		}
		strokeWeight(1);

		if (myDelaunay != null)
		{
			int[][] myLinks = myDelaunay.getLinks();

			for (int i=0; i<myLinks.length; i++)
			{
				int startIndex = myLinks[i][0];
				int endIndex = myLinks[i][1];
				float startX = cells.get(startIndex).position.x;
				float startY = cells.get(startIndex).position.y;
				float endX = cells.get(endIndex).position.x;
				float endY = cells.get(endIndex).position.y;
				//float startX = points[startIndex][0];
				//float startY = points[startIndex][1];
				//float endX = points[endIndex][0];
				//float endY = points[endIndex][1];
				stroke(0, 0, 0, 100);
				line( startX, startY, endX, endY );
			}
		}
	}

	void doHopsAStar(Cell c)
	{
		for (Cell ci : cells)
		{
			if (!ci.equals(c))
			{
				for (Originators o : Originators.values())
				{
					if (ci.isOriginatorFor(o))
					{
						int distance = getDistanceFromTo(c, ci);
						if (c.getGenomicValue(o) == 0 || c.getGenomicValue(o) > distance)
						{
							c.setGenomicValue(o,  distance);
						}
					}
				}
			}
		}
	}
	
	void doHop(Cell c, Originators o, int value)
	{
		int q = -1;
		for (int i = 0; i < cells.size(); i++)
		{
			if (c == cells.get(i))
			{
				q = i;
			}
		}

		if (!c.isOriginatorFor(o))
		{
			if (c.getGenomicValue(o) > value || c.getGenomicValue(o) == 0)
			{
				c.setGenomicValue(o, value);
			}
		}


		for (Cell ci : getNeighbours(q))
		{
			if ((ci.getGenomicValue(o) == 0 || ci.getGenomicValue(o) > value))
			{
				ci.setGenomicValue(o, value+1);
				doHop(ci, o, value+1);
			}
		}

	}

	void checkPassing()
	{

		for (int q = 0; q < cells.size(); q++)
		{
			Cell c = cells.get(q);
			for (Originators o : c.getOriginators())
			{
				Cell transferTarget = c;
				for (Cell linkedCell : getNeighbours(q))
				{
					if (!linkedCell.isOriginator() && linkedCell.getSuitability(o) > transferTarget.getSuitability(o))
					{
						transferTarget = linkedCell;
					}
				}

				if (c != transferTarget)
				{
					c.setOriginator(o, false);
					transferTarget.setOriginator(o, true);
				}

			}

		}

	}

	void recalculateHops()
	{
		resetHop();
		for (Cell c : cells)
		{
			doHopsAStar(c);
//			for (Originators o : c.getOriginators())
//			{
//				doHop(c, o, 0);
//			}
		}
		checkPassing();  
	}
	
	void resetHop()
	{
		for (Cell c : cells)
		{
		  for (Originators o : Originators.values())
		  {
		    c.setGenomicValue(o, 0);
		  }
		}

//		for (int q = 0; q < cells.size(); q++)
//		{
//			Cell c = cells.get(q);
//			for (Cell comparison : getNeighbours(q))
//			{
//				if (comparison != c)
//				{
//					for (Originators o : Originators.values())
//					{
//
//						if (abs(comparison.getGenomicValue(o)-c.getGenomicValue(o)) > 1)
//						{
//								c.setGenomicValue(o, 0);
//								comparison.setGenomicValue(o, 0);
//						}   
//					}
//				}
//			}
//		}
	}

	private ArrayList<Cell> getNeighbours(Cell c)
	{
		for (int i = 0; i < cells.size(); i++)
		{
			if (cells.get(i).equals(c))
			{
				return getNeighbours(i);
			}
		}
		return null;
	}

	private ArrayList<Cell> getNeighbours(int q)
	{
		ArrayList<Cell> returner = new ArrayList<Cell>();
		IntList successors = new IntList();
		if (myDelaunay != null)
		{
			for (int i = 0; i < myDelaunay.getLinks().length; i++)
			{
				if (myDelaunay.getLinks()[i][0] == q || myDelaunay.getLinks()[i][1] == q)
				{
					if (myDelaunay.getLinks()[i][0] != q && !successors.hasValue(myDelaunay.getLinks()[i][0]))
					{
						successors.append(myDelaunay.getLinks()[i][0]);
					}
					if (myDelaunay.getLinks()[i][1] != q && !successors.hasValue(myDelaunay.getLinks()[i][1]))
					{
						successors.append(myDelaunay.getLinks()[i][1]);
					}
				}
			}
		}         
		for (int i : successors)
		{
			returner.add(cells.get(i));
		}
		return returner;
	}	
	
	FWorld world;

	FCompound allBodies = new FCompound();
	int greatestRed = 0;
	int greatestGreen = 0;
	int greatestBlue = 0;
	float angle = 0;
	int frame = 0;
	int livingTick = 0;
	float startingOriginX;
	float bestDistanceMoved = 0;
	public void draw()
	{
		background(200);
		pushMatrix();
		translate(offsetX, offsetY);
		scale(scale);
		Collections.sort(cells);
		fill(255);
		ellipseMode(CENTER);

		if (currentState == States.DESIGNING)
		{
			drawGraphCreature();
		}
		if (currentState == States.GROWING)
		{

			tick();
			recalculateHops();

			float[][] points = new float[cells.size()][2];
			for (int i = 0; i < cells.size(); i++)
			{
				points[i][0]= cells.get(i).position.x;    
				points[i][1]= cells.get(i).position.y;
			}
			myDelaunay = new Delaunay( points );

			greatestRed = 0;
			greatestGreen = 0;
			greatestBlue = 0;
			for (int j = 0; j < cells.size(); j++)
			{
				Cell c = cells.get(j);
				if (c.isOriginator())
				{
					for (int i = 0; i < cells.size(); i++)
					{
						Cell cc = cells.get(i);
						if (!cc.isOriginator())
						{
							if (c.red == 255)
							{
								//cc.red += getDistanceFromTo(i, j);
								if (cc.red > greatestRed)
								{
									greatestRed = cc.red;
								}
							}
							if (c.green == 255)
							{
								//cc.green += getDistanceFromTo(i, j);
								if (cc.green > greatestGreen)
								{
									greatestGreen = cc.green;
								}
							}
							if (c.blue == 255)
							{
								//cc.blue = getDistanceFromTo(i, j);
								if (cc.blue > greatestBlue)
								{
									greatestBlue = cc.blue;
								}
							}
						}
					}
				}
			}
			drawGraphCreature();
			currentState = States.SETTLING;
		} else if (currentState == States.SETTLING)
		{
			float[][] points = new float[cells.size()][2];
			for (int i = 0; i < cells.size(); i++)
			{
				points[i][0]= cells.get(i).position.x;    
				points[i][1]= cells.get(i).position.y;
			}
			myDelaunay = new Delaunay( points );

			for (Cell c : cells)
			{
				ArrayList<Cell> links = new ArrayList<Cell>();
				for (int i = 0; i < myDelaunay.getLinks().length; i++)
				{
					Cell c1 = cells.get(myDelaunay.getLinks()[i][0]);
					Cell c2 = cells.get(myDelaunay.getLinks()[i][1]);
					if (c1 == c || c2 == c)
					{
						if (c1 != c && !links.contains(c1))
						{
							links.add(c1);
						}
						if (c2 != c && !links.contains(c2))
						{
							links.add(c2);
						}
					}
				}
				c.react(links);
			}

			for (Cell c : cells)
			{
				c.move();
			}

			recalculateHops();

			greatestRed = 0;
			greatestGreen = 0;
			greatestBlue = 0;
			for (int j = 0; j < cells.size(); j++)
			{
				Cell c = cells.get(j);
				if (c.isOriginator())
				{
					for (int i = 0; i < cells.size(); i++)
					{
						Cell cc = cells.get(i);
						if (!cc.isOriginator())
						{
							if (c.red == 255)
							{
								//cc.red += getDistanceFromTo(i, j);
								if (cc.red > greatestRed)
								{
									greatestRed = cc.red;
								}
							}
							if (c.green == 255)
							{
								//cc.green += getDistanceFromTo(i, j);
								if (cc.green > greatestGreen)
								{
									greatestGreen = cc.green;
								}
							}
							if (c.blue == 255)
							{
								//cc.blue = getDistanceFromTo(i, j);
								if (cc.blue > greatestBlue)
								{
									greatestBlue = cc.blue;
								}
							}
						}
					}
				}
			}

			boolean finishedSettling = true;
			for (Cell c : cells)
			{
				if (!c.settled)
				{
					finishedSettling= false;
				}
			}

			if (finishedSettling)
			{
				if (cells.size() >= maximumCells)
				{
					currentState = States.HATCHING;
				}
				else
				{
					currentState = States.GROWING;
				}
			}
			drawGraphCreature();
		} else if (currentState == States.HATCHING)
		{
			world = new FWorld();
			world.setGravity(0, 30);
			world.setEdges();
			world.setEdgesFriction(0.5f);
			world.setEdgesRestitution(0.5f);
			//world.remove(world.left);
			//world.remove(world.right);
			world.remove(world.top);

			for (int i = 0; i < cells.size(); i++)
			{
				cells.get(i).body = new FCircle(10);
				cells.get(i).body.setPosition(cells.get(i).position.x, cells.get(i).position.y);
				cells.get(i).body.setRestitution(0.8f);
				cells.get(i).body.setDamping(0.3f);
				cells.get(i).body.setDensity(1);
				cells.get(i).body.setFriction(0.5f);
				cells.get(i).body.setRotatable(false);
				world.add(cells.get(i).body);
			}

			for (int q = 0; q < cells.size(); q++)
			{

				//if (successors.size() >= 2)
					//{
					// FConstantVolumeJoint fdj = new FConstantVolumeJoint(); //circles[i], circles[q]);
				// fdj.addBody(cells.get(q).body);
				// fdj.setNoFill();
				// for (int i : successors)
				// {
				//   fdj.addBody(cells.get(i).body);
				// }
				// fdj.setDamping(0.3);
				// fdj.setCollideConnected(false);
				// //fdj.setLength(20);
				// cells.get(q).joint = fdj;
				// world.add(fdj);
				//} else
				//{
				for (Cell s : getNeighbours(q))
				{
					FDistanceJoint fdj = new FDistanceJoint(s.body, cells.get(q).body);
					//fdj.setLength(20);
					fdj.calculateLength();
					fdj.setDamping(0.3f);
					fdj.setCollideConnected(false);
					cells.get(q).joint = fdj;
					world.add(fdj);
				}
				//}
			}

			for (Cell c : cells)
			{
				c.position.x = c.body.getX();
				c.position.y = c.body.getY();
			}        
			drawGraphCreature();
			currentState = States.STABILISING;
			//world.add(bodies[0]);
			//allBodies.add(bodies[0]);
			//for (FBlob fb : bodies)
			//{
			//allBodies.add(fb);
			//}
		} else if (currentState == States.STABILISING)
		{    
			world.step();
			boolean stable = true;
			for (Cell c : cells)
			{
				if (!c.body.isSleeping())
				{
					stable = false;
				}
				c.position.x = c.body.getX();
				c.position.y = c.body.getY();
			}

			recalculateHops();
			drawGraphCreature();

			if (stable)
			{
				world.setGravity(0, 50);
				currentState = States.LIVING;
			}
			startingOriginX = cells.get(0).position.x;
		} else if (currentState == States.LIVING)
		{
			world.step();
			//world.draw(this);

			if (frameCount % maximumCells == 0)
			{
				livingTick++;
			}


			Cell c = cells.get(frameCount%maximumCells);

			//for (Cell c : cells)
			//{
			//if (!c.originator)
			//{
			FDistanceJoint fdj = (FDistanceJoint)c.joint;
			fdj.setDamping(0.1f);
			c.body.setDamping(0.1f);
			if (!c.isOriginator())
			{
				if (c.red > 0)
				{
					//fdj.setLength(20 + 3*(livingTick%c.red));
				}
			}
			//c.body.addImpulse(0, 0);
			//}
			//}

			for (Cell cc : cells)
			{
				cc.position.x = cc.body.getX();
				cc.position.y = cc.body.getY();
			}

			if (abs(cells.get(0).position.x - startingOriginX) > bestDistanceMoved)
			{
				bestDistanceMoved = abs(cells.get(0).position.x - startingOriginX);
			}

			recalculateHops();
			//world.draw(this);
			drawGraphCreature();

			//allBodies.draw(this);
			//poly.draw(this);
		}

		//for (Cell c : cells)
		//{
		//  ArrayList<Cell> links = new ArrayList<Cell>();
		//  for (int i = 0; i < myDelaunay.getLinks().length; i++)
		//  {
		//      Cell c1 = cells.get(myDelaunay.getLinks()[i][0]);
		//      Cell c2 = cells.get(myDelaunay.getLinks()[i][1]);
		//      if (c1 == c || c2 == c)
		//      {
		//         if (c1 != c && !links.contains(c1))
		//         {
		//           links.add(c1);
		//         }
		//         if (c2 != c && !links.contains(c2))
		//         {
		//           links.add(c2);
		//         }
		//      }
		//  }
		//  c.react(links);
		//}

		//for (Cell c : cells)
		//{
		//  c.move();
		//}

		//if (frameCount % 5 == 0)
		//{
		//  for (Cell c : cells)
		//  {
		//    if (!c.originator)
		//    {
		//      c.springiness = 1000.0;
		//      //c.velocityMultiplier = 1.0;
		//      if (c.red > 3)
		//      {
		//        if (c.pushing)
		//        {
		//          c.eLength += c.red;
		//          if (c.eLength > 40)
		//          {
		//            c.pushing = false;
		//          }
		//        } 
		//        else
		//        {
		//          c.eLength -= c.red;
		//          if (c.eLength < 20)
		//          {
		//            c.pushing = true;
		//          }
		//        }
		//      }
		//    }
		//  }
		//}
		//angle += 0.01;
		//}
		//

		//}

		fill(0);
		rect(0, height, width, 10);
		popMatrix();
		fill(0);
		textSize(18);
		textAlign(LEFT, TOP);
		text(""+currentState, 10, 30);
		text("Cells: " + cells.size() + " (max: " + maximumCells + ")", 10, 10);		
		text(mouseOverText, 10, height - 20);
		if (currentState == States.DESIGNING)
		{
			text("Left click to place a cell", 10, 50);
			text("Right click to begin growing", 10, 70);
		}
		if (currentState == States.LIVING)
		{
			text("Horizontal Distance Travelled: " + bestDistanceMoved, 10, 50);
			text("Horizontal Distance Travelled / Cycle: " + (bestDistanceMoved/(float)livingTick), 10, 70);
		}

		
		
		if (outputframes)
		{
			save("output/" + pad(frame++) + ".png");
		}
		if (recordgif)
		{
			gm.setDelay(1);
			gm.addFrame();
		}
	}

	public void stop()
	{
		if (recordgif)
		{
			gm.finish();
		}
	}

	String pad(int i)
	{
		String s = ""+i;
		while (s.length() < 5)
		{
			s = "0" + s;
		}
		return s;
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "ALife.ALifePrototype" });
	}	


}

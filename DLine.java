import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

public class DLine extends DShape
{

	public DLine(DShapeModel model, Canvas canvas) 
	{
		super(model, canvas);
	}

	public DLineModel getModel()
	{
		return (DLineModel) model;
	}

	public void draw(Graphics g, boolean selected)
	{
		DLineModel line = getModel();
		g.setColor(getColor());
		int x1,y1,x2,y2;
		x1 = line.getPoint1().x;
		y1 = line.getPoint1().y;
		x2 = line.getPoint2().x;
		y2 = line.getPoint2().y;
		g.drawLine(x1,y1,x2,y2);
		if(selected)
		{
			drawKnobs(g); 
		}
	}

    public ArrayList<Point> getKnobs() 
    { 
        if(knobs == null || needsRecomputeKnobs) 
        { 
            knobs = new ArrayList<Point>(); 
            DLineModel lineM = (DLineModel) model; 
            knobs.add(new Point(lineM.getPoint1())); 
            knobs.add(new Point(lineM.getPoint2())); 
            needsRecomputeKnobs = false; 
        } 
        return knobs; 
    }
}

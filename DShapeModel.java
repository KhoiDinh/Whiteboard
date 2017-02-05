import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;


public abstract class DShapeModel
{
    protected ArrayList<ModelListener> listeners; 
    
    protected Rectangle bounds; 
    protected Color color; 
    protected String type;
    protected boolean markedForRemoval; 
     
    protected int ID; 
     
    public DShapeModel() 
    { 
        this(0, 0, 0, 0, Color.gray); 
    } 
     
 
    public DShapeModel(int x, int y, int width, int height, Color color) 
    { 
        bounds = new Rectangle(x, y, width, height); 
        this.color = color; 
        listeners = new ArrayList<ModelListener>(); 
        markedForRemoval = false; 
    } 
     
    public void mimic(DShapeModel other) 
    { 
        setID(other.getID()); 
        setBounds(other.getBounds()); 
        setColor(other.getColor()); 
        notifyListeners(); 
    } 
     
    public int getID() 
    { 
        return ID; 
    } 
     
    public void setID(int num) 
    { 
        ID = num; 
    } 
     
    public Point getLocation()
    { 
        return bounds.getLocation(); 
    } 
     
    public void setLocation(int x, int y)
    { 
        bounds.setLocation(x, y); 
        notifyListeners(); 
    } 
     
    public void setLocation(Point pt) 
    { 
        setLocation(pt.x, pt.y); 
    } 

    
    public void move(int dx, int dy) 
    { 
        bounds.x = bounds.x + dx; 
        bounds.y = bounds.y + dy; 
        notifyListeners(); 
    } 
     
    public void modifyWithPoints(Point anchorPoint, Point movingPoint) 
    { 
        int x = (anchorPoint.x < movingPoint.x ? anchorPoint.x : movingPoint.x); 
        int y = (anchorPoint.y < movingPoint.y ? anchorPoint.y : movingPoint.y); 
        int width = Math.abs(anchorPoint.x - movingPoint.x); 
        int height = Math.abs(anchorPoint.y - movingPoint.y); 
        setBounds(new Rectangle(x, y, width, height)); 
    } 
     
    public Rectangle getBounds() 
    { 
        return bounds; 
    } 
     
    public void setBounds(int x, int y, int width, int height)
    { 
        bounds = new Rectangle(x, y, width, height); 
        notifyListeners(); 
    } 
      
    
    public void setBounds(Point pt, int width, int height)
    { 
        bounds = new Rectangle(pt.x, pt.y, width, height); 
        notifyListeners(); 
    } 

    
    public void setBounds(Rectangle newBounds) 
    { 
        bounds = new Rectangle(newBounds); 
        notifyListeners(); 
    } 

    
    public void setColor(Color color) 
    { 
        this.color = color; 
        notifyListeners(); 
    } 
     
 
    public Color getColor() 
    { 
        return color; 
    } 

    public void markForRemoval() 
    { 
        markedForRemoval = true; 
        notifyListeners(); 
    } 
     
    public boolean markedForRemoval() 
    { 
        return markedForRemoval; 
    } 
     

    public void addListener(ModelListener listener) 
    { 
        listeners.add(listener); 
    } 
     
    public boolean removeListener(ModelListener listener)
    { 
        return listeners.remove(listener); 
    } 
     
    public void notifyListeners() 
    { 
        for(ModelListener listener : listeners) 
        {
            listener.modelChanged(this); 
        }
    } 
    
    public void setType(String string) 
    { 
        type = string;
    }
    
	public String getType()
	{
		return type;
	} 
    
}

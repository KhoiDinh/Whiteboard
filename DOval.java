import java.awt.Graphics;
import java.awt.Rectangle;

public class DOval extends DShape
{   
	
    public DOval(DShapeModel model, Canvas c) 
    { 
        super(model, c); 
         
    } 
    
    public DOvalModel getModel()
    { 
        return (DOvalModel) model; 
    }
    
    public void draw(Graphics g, boolean selected) 
    { 
        g.setColor(model.getColor()); 
        Rectangle bounds = model.getBounds(); 
        int x, y, width, height;
        x = bounds.x;
        y = bounds.y;
        width = bounds.width;
        height = bounds.height;
        g.fillOval(x, y, width, height); 

        if(selected)
        {
        	drawKnobs(g); 
        }
    }
}

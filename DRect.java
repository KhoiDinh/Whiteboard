import java.awt.Graphics;
import java.awt.Rectangle;

public class DRect extends DShape
{   
	
    public DRect(DShapeModel model, Canvas c) 
    { 
        super(model, c); 
         
    } 
    
    public DRectModel getModel() { 
        return (DRectModel) model; 
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
        g.fillRect(x, y, width, height); 
        
        if(selected)
        {
        	drawKnobs(g); 
        }
    }

}

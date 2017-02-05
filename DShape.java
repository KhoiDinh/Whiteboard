import java.awt.*;
import java.util.*; 
 
public abstract class DShape implements ModelListener { 
    public static final int KNOB_SIZE = 9; 
    public static final Color KNOB_COLOR = Color.black; 
     
    public static Rectangle getBigBoundsForModel(DShapeModel model) { 
        Rectangle bounds = model.getBounds(); 
        return new Rectangle(bounds.x - KNOB_SIZE/2, bounds.y - KNOB_SIZE/2, bounds.width + KNOB_SIZE, bounds.height + KNOB_SIZE); 
    } 
     
    protected DShapeModel model; 
    protected Canvas canvas; 
    protected Rectangle lastBounds; 
     
    protected ArrayList<Point> knobs; 
     
    protected boolean needsRecomputeKnobs; 
     
    public DShape(DShapeModel model, Canvas canvas) { 
        this.model = model; 
        this.canvas = canvas; 
        lastBounds = new Rectangle(getBounds()); 
        knobs = null; 
        needsRecomputeKnobs = false; 
        model.addListener(this); 
    } 
     
    public void move(int dx, int dy) { 
        needsRecomputeKnobs = true; 
        model.move(dx, dy); 
    } 
     
    public int getModelID() { 
        return model.getID(); 
    } 
     
    public Rectangle getBounds() { 
        return model.getBounds(); 
    } 

    public Rectangle getBigBounds() { 
        return getBigBoundsForModel(model); 
    } 

    public Rectangle getBigBoundsOfLastPosition() { 
        return new Rectangle(lastBounds.x - KNOB_SIZE/2, lastBounds.y - KNOB_SIZE/2, lastBounds.width + KNOB_SIZE, lastBounds.height + KNOB_SIZE); 
    } 
     
    public void modifyShapeWithPoints(Point anchorPoint, Point movingPoint) { 
        needsRecomputeKnobs = true; 
        model.modifyWithPoints(anchorPoint, movingPoint); 
    } 

    public boolean containsPoint(Point pt) { 
        Rectangle bounds = getBounds(); 
         
        if(bounds.contains(pt)) 
            return true; 

        if(bounds.width == 0 
           && Math.abs(pt.x - bounds.x) <= 3 
           && pt.y <= bounds.y + bounds.height 
           && pt.y >= bounds.y) 
            return true; 
         
        if(bounds.height == 0 
           && Math.abs(pt.y - bounds.y) <= 3 
           && pt.x >= bounds.x 
           && pt.x <= bounds.x + bounds.width) 
            return true; 
         
        return false; 
    } 

    public Color getColor() { 
        return model.getColor(); 
    } 

    public void setColor(Color color) { 
        model.setColor(color); 
    } 

    public ArrayList<Point> getKnobs() { 
        if(knobs == null || needsRecomputeKnobs) { 
            knobs = new ArrayList<Point>(); 
            Rectangle bounds = model.getBounds(); 
            for(int i = 0; i < 2; i++) 
                for(int j = 0; j < 2; j++) 
                    knobs.add(new Point(bounds.x + bounds.width * i, bounds.y + bounds.height * j)); 

            Point temp = knobs.remove(2); 
            knobs.add(temp); 
             
            needsRecomputeKnobs = false; 
        } 
        return knobs; 
    } 

    public boolean selectedKnob(Point click, Point knobCenter) { 
        Rectangle knob = new Rectangle(knobCenter.x - KNOB_SIZE/2, knobCenter.y - KNOB_SIZE/2, KNOB_SIZE, KNOB_SIZE); 
        return knob.contains(click); 
    } 

    public Point getAnchorForSelectedKnob(Point pt) { 
        int index = getKnobs().indexOf(pt); 
        return new Point(knobs.get((index + knobs.size()/2) % knobs.size())); 
    } 

    public void modelChanged(DShapeModel model) { 
        if(this.model == model) { 
            if(model.markedForRemoval()) { 
                canvas.removeShape(this); 

                return; 
            } 
            canvas.repaintShape(this); 
            if(!lastBounds.equals(getBounds())) { 
                canvas.repaintArea(getBigBoundsOfLastPosition()); 
                lastBounds = new Rectangle(getBounds()); 
            } 
        } 
    } 
     
    public void markForRemoval() { 
        model.markForRemoval(); 
    } 
     
    protected void drawKnobs(Graphics g) { 
        g.setColor(KNOB_COLOR); 
        for(Point point : getKnobs()) 
            g.fillRect(point.x - KNOB_SIZE/2, point.y - KNOB_SIZE/2, KNOB_SIZE, KNOB_SIZE); 
    } 
     
     
    abstract public DShapeModel getModel(); 
     
    abstract public void draw(Graphics g, boolean selected); 
}

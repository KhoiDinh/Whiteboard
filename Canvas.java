import java.awt.*;
import java.awt.event.*; 
import java.awt.image.BufferedImage; 
import java.beans.XMLDecoder; 
import java.beans.XMLEncoder; 
import java.io.BufferedInputStream; 
import java.io.BufferedOutputStream; 
import java.io.File; 
import java.io.FileInputStream; 
import java.io.FileOutputStream; 
import java.io.IOException; 
import java.util.*; 

import javax.swing.*; 
 
public class Canvas extends JPanel implements ModelListener
{ 
     
    private ArrayList<DShape> shapes; 
    private DShape selected; 
    private Point move; 
    private Point anchorPoint; 
     
    private int lastX, lastY; 
     
    private WhiteBoard whiteboard; 
     
    public Canvas(WhiteBoard bard) 
    { 
        setMinimumSize(new Dimension(400, 400)); 
        setPreferredSize(getMinimumSize()); 
        setBackground(Color.white); 
         
        whiteboard = bard; 
         
        shapes = new ArrayList<DShape>(); 
        selected = null; 
        move = null; 
         
        addMouseListener(new MouseAdapter() 
        { 
            public void mousePressed(MouseEvent e) 
            { 
                if(whiteboard.isNotClient()) 
                    selectObjectForClick(e.getPoint()); 
            } 
        }); 
         
        addMouseMotionListener(new MouseMotionAdapter() 
        { 
           public void mouseDragged(MouseEvent e) 
           { 
               if(whiteboard.isNotClient()) 
               { 
                   int dx = e.getX() - lastX; 
                   int dy = e.getY() - lastY; 
                   lastX = e.getX(); 
                   lastY = e.getY(); 
                    
                   if(move != null)
                   {               	   
                	   move.x = move.x + dx; 
                	   move.y = move.y + dy; 
                       selected.modifyShapeWithPoints(anchorPoint, move); 
                   } 
                   else if(selected != null) 
                   { 
                       selected.move(dx, dy); 
                   } 
               } 
           } 
        }); 
    } 
     
    public void addShape(DShapeModel model) 
    { 
        if(whiteboard.isNotClient()) 
            model.setID(WhiteBoard.getNextIDNumber()); 
         
        if(selected != null)  
            repaintShape(selected); 
         
        DShape shape = null; 
        if(model instanceof DRectModel) 
        {
        	String type  = "Rectangle";
        	model.setType(type);
            shape = new DRect(model, this);
        }
        else if(model instanceof DOvalModel) 
        {
        	String type  = "Oval";
        	model.setType(type);
            shape = new DOval(model, this); 
        }
        else if(model instanceof DLineModel) 
        {
        	String type  = "Line";
        	model.setType(type);
            shape = new DLine(model, this); 
        }
        else if(model instanceof DTextModel) 
        { 
        	String type  = "Text";
        	model.setType(type);
            shape = new DText(model, this); 
            DText textShape = (DText) shape; 
            whiteboard.updateFontGroup(textShape.getText(), textShape.getFontName()); 
        } 
        
        model.addListener(this); 
        shapes.add(shape); 
        whiteboard.addToTable(shape); 
        
        if(whiteboard.isNotClient())  
        {
            selected = shape; 
        }
        if(whiteboard.isServer()) 
        {
            whiteboard.doSend(WhiteBoard.Message.ADD, model); 
        }
         
        repaintShape(shape); 
    } 
      
    public ArrayList<DShape> getShapes()
    { 
        return shapes; 
    } 

    public DShape getShapeWithID(int ID) 
    { 
        for(DShape shape : shapes) 
        {
            if(shape.getModelID() == ID) 
            {
                return shape; 
            }
        }
        return null; 
    } 
    
    public ArrayList<DShapeModel> getShapeModels() 
    { 
        ArrayList<DShapeModel> models = new ArrayList<DShapeModel>(); 
        for(DShape shape : shapes) 
        {
            models.add(shape.getModel()); 
        }
        return models; 
    } 
     
    public void markSelectedShapeForRemoval() 
    { 
        markForRemoval(selected); 
        selected = null; 
    } 
     
    public void markForRemoval(DShape shape) 
    { 
        shape.getModel().removeListener(this); 
        shape.markForRemoval(); 
    } 
     
    public void markAllForRemoval() 
    { 
        selected = null; 
        for(int i = shapes.size() - 1; i >= 0; i--) 
        {
            markForRemoval(shapes.get(i)); 
        }
    } 
     
    public void clearCanvas() 
    { 
        shapes.clear(); 
        selected = null; 
        whiteboard.clearTable(); 
        repaint(); 
    } 
     
    public void removeShape(DShape shape) 
    { 
        shapes.remove(shape);        
        whiteboard.didRemove(shape); 
        
        if(whiteboard.isServer()) 
        {
            whiteboard.doSend(WhiteBoard.Message.REMOVE, shape.getModel()); 
        }
        
        repaintArea(shape.getBigBounds()); 
    } 
      
    public void moveSelectedToFront() { 
        moveToFront(selected); 
    } 
     
    public void moveToFront(DShape shape)
    { 
        if(!shapes.isEmpty() && shapes.remove(shape)) 
        {
            shapes.add(shape); 
        }
        
        whiteboard.didMoveToFront(shape);
        
        if(whiteboard.isServer()) 
        {
            whiteboard.doSend(WhiteBoard.Message.FRONT, shape.getModel()); 
        }
        
        repaintShape(shape); 
    } 
     

    public void moveSelectedToBack()
    { 
        moveToBack(selected); 
    } 
     
    public void moveToBack(DShape shape) 
    { 
        if(!shapes.isEmpty() && shapes.remove(shape)) 
        {
            shapes.add(0, shape); 
        }
        
        whiteboard.didMoveToBack(shape); 
        
        if(whiteboard.isServer()) 
        {
            whiteboard.doSend(WhiteBoard.Message.BACK, shape.getModel()); 
        }
        
        repaintShape(shape); 
    } 
     

    public void setTextForSelected(String text)
    { 
        if(selected instanceof DText) 
        {
            ((DText) selected).setText(text); 
        }
    } 
     
    public void setFontForSelected(String fontName) 
    { 
        if(selected instanceof DText) 
        {
            ((DText) selected).setFontName(fontName); 
        }
    } 
     
    public void selectObjectForClick(Point pt) 
    { 
        lastX = pt.x; 
        lastY = pt.y; 
        move = null; 
        anchorPoint = null; 
         
        if(selected != null) 
        { 
            for(Point point : selected.getKnobs()) 
                if(selected.selectedKnob(pt, point)) 
                { 
                	move = new Point(point); 
                    anchorPoint = selected.getAnchorForSelectedKnob(point); 
                    break; 
                } 
        } 

        if(move == null) 
        { 
            selected = null; 
            
            for(DShape shape : shapes)
            {
                if(shape.containsPoint(pt)) 
                {
                    selected = shape; 
                }
            }
        } 
         
        if(selected != null && selected instanceof DText) 
        { 
            DText textShape = (DText) selected; 
            whiteboard.updateFontGroup(textShape.getText(), textShape.getFontName()); 
             
        } 
        else 
        { 

            whiteboard.updateFontGroup("", "Dialog"); 
        } 
         
        if(selected != null && whiteboard.isServer()) { 
            whiteboard.doSend(WhiteBoard.Message.CHANGE, selected.getModel()); 
        } 
         
        whiteboard.updateTableSelection(selected); 
         
        repaint(); 
    } 
     
    public boolean hasSelected() 
    { 
        return selected != null; 
    } 
     
    public DShape getSelected()
    { 
        return selected; 
    } 

    public void setSelectedColor(Color color) 
    { 
        selected.setColor(color); 
    } 
     
    public void repaintShape(DShape shape) 
    { 
        if(shape == selected) 
        {
            repaint(shape.getBigBounds()); 
        }
        else 
        {
            repaint(shape.getBounds()); 
        }
    } 

    public void repaintArea(Rectangle bounds) 
    { 
        repaint(bounds); 
    } 
      
    public void saveCanvas(File file) 
    { 
        try 
        { 
            XMLEncoder xmlOut = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file))); 
             
            DShapeModel[] shapeModels = getShapeModels().toArray(new DShapeModel[0]); 
             
            xmlOut.writeObject(shapeModels); 
             
            xmlOut.close(); 
        } 
        catch (IOException e) 
        { 
            e.printStackTrace(); 
        } 
    } 
     
    public void openCanvas(File file) 
    { 
        markAllForRemoval();  
        try 
        { 
            XMLDecoder xmlIn = new XMLDecoder(new BufferedInputStream(new FileInputStream(file))); 
             
            DShapeModel[] shapeModels = (DShapeModel[]) xmlIn.readObject(); 
             
            xmlIn.close(); 
             
            for(DShapeModel shapeModel : shapeModels)
            { 
                addShape(shapeModel); 
            }              
        } 
        catch (IOException e) 
        { 
            e.printStackTrace(); 
        } 
    } 

    public void saveImage(File file) 
    { 
        DShape wasSelected = selected; 
        selected = null; 
        BufferedImage image = (BufferedImage) createImage(getWidth(), getHeight()); 
         
        Graphics g = image.getGraphics(); 
        paintAll(g); 
        g.dispose(); 
         
        try 
        { 
            javax.imageio.ImageIO.write(image, "PNG", file); 
        }
        catch (IOException e) 
        { 
            e.printStackTrace(); 
        } 
         
        selected = wasSelected; 
    } 
     
    public void paintComponent(Graphics g) 
    { 
        super.paintComponent(g); 
        
        for(DShape shape : shapes)
        {
            shape.draw(g, (selected == shape)); 
        }
    } 
 
    public void modelChanged(DShapeModel model)
    { 
        if(whiteboard.isServer() && !model.markedForRemoval()) 
        {
            whiteboard.doSend(WhiteBoard.Message.CHANGE, model); 
        }
    } 
}

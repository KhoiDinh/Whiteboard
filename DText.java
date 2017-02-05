import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;

public class DText extends DShape
{
   
    private Font font; 
    private String lastFont; 
    private int lastHeight; 
    private boolean recompute; 
     
    public DText(DShapeModel model, Canvas canvas)
    { 
        super(model, canvas); 
        font = null; 
        recompute = true; 
        lastFont = ""; 
        lastHeight = -1; 
    } 

    public String getText() 
    { 
        return getModel().getText(); 
    } 

    public void setText(String newText) 
    { 
        getModel().setText(newText); 
    } 

    public String getFontName() 
    { 
        return getModel().getFontName(); 
    } 

    public void setFontName(String newName) 
    { 
        if(newName.equals(getModel().getFontName()))
        {
        	return; 
        }
        
        getModel().setFontName(newName); 
    } 
     
    public void modifyShapeWithPoints(Point point, Point movingPoint) 
    { 
        super.modifyShapeWithPoints(point, movingPoint); 
    } 

    public void draw(Graphics g, boolean selected) 
    { 
        Shape clip = g.getClip(); 
        g.setClip(clip.getBounds().createIntersection(getBounds())); 
         
        Font font = computeFont(g); 
        int setfont = (int) font.getLineMetrics(getModel().getText(), ((Graphics2D) g).getFontRenderContext()).getDescent(); 
        int yPos = getBounds().y + getBounds().height - setfont; 
         
        g.setColor(getColor()); 
        g.setFont(font); 
        
        g.drawString(getModel().getText(), getBounds().x, yPos); 
        g.setClip(clip); 
        
        if(selected)
        {
        	drawKnobs(g); 
        }
    } 

    public Font computeFont(Graphics g) 
    { 
        if(recompute) 
        { 
            double size = 1.0; 
            double previouseSize = size; 
            while(true) 
            { 
            	font = new Font(getFontName(), Font.PLAIN, (int) size); 
                FontMetrics metric = g.getFontMetrics(font);
                if(metric.getStringBounds(getText(), g).getBounds().getHeight() > getModel().getBounds().getHeight() )
                {
                	break;
                }
                previouseSize = size; 
                size = (size * 1.1) + 1; 
            } 
            font = new Font(getFontName(), Font.PLAIN, (int) previouseSize); 
            recompute = false; 
        } 
        return font;         
    } 
     
    public DTextModel getModel() 
    { 
        return (DTextModel) model; 
    } 
     
    public void modelChanged(DShapeModel model)
    { 
        DTextModel textModel = (DTextModel) model; 
        if(textModel.getBounds().height != lastHeight || !textModel.getFontName().equals(lastFont))
        { 
            lastHeight = textModel.getBounds().height; 
            lastFont = textModel.getFontName(); 
            recompute = true; 
        } 
        super.modelChanged(textModel); 
    } 
}

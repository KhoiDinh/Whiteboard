
public class DTextModel extends DShapeModel 
{
    private String text; 
    private String font; 
     
     
    public DTextModel()
    { 
        super(); 
        text = "Hello"; 
        font = "Dialog"; 
    } 
    
    public void mimic(DShapeModel other) 
    { 
        DTextModel toMimic = (DTextModel) other; 
        setText(toMimic.getText()); 
        setFontName(toMimic.getFontName()); 
        super.mimic(other); 
    } 

    public String getText() 
    { 
        return text; 
    } 
     
    public void setText(String newText) { 
        text = newText; 
        notifyListeners(); 
    } 

    public String getFontName() { 
        return font; 
    } 

    public void setFontName(String fontName) 
    { 
        font = fontName; 
        notifyListeners(); 
    } 
}

import javax.swing.table.AbstractTableModel;
import java.util.*; 
import java.awt.*; 

public class TableData extends AbstractTableModel implements ModelListener 
{ 
    public static final String[] DEFAULT_COLUMNS = {"ID", "TYPE","X", "Y", "Width", "Height"}; 
     
    private ArrayList<DShapeModel> models; 
 
    public TableData() 
    { 
        super(); 
        models = new ArrayList<DShapeModel>(); 
    } 
 
    public void addModel(DShapeModel model) 
    { 
        models.add(0, model); 
        model.addListener(this); 
        fireTableDataChanged(); 
    } 

    public void removeModel(DShapeModel model) 
    { 
        model.removeListener(this); 
        models.remove(model); 
        fireTableDataChanged(); 
    } 

    public void moveModelToBack(DShapeModel model) 
    { 
        if(!models.isEmpty() && models.remove(model)) 
            models.add(model); 
        fireTableDataChanged(); 
    } 

    public void moveModelToFront(DShapeModel model) 
    { 
        if(!models.isEmpty() && models.remove(model)) 
            models.add(0, model); 
        fireTableDataChanged(); 
    } 
     
    public void clear()
    { 
        models.clear(); 
        fireTableDataChanged(); 
    } 

    public int getRowForModel(DShapeModel model) 
    { 
        return models.indexOf(model); 
    } 

    public String getColumnName(int col) 
    { 
        return DEFAULT_COLUMNS[col]; 
    } 

    public int getColumnCount() 
    { 
        return DEFAULT_COLUMNS.length; 
    } 

    public int getRowCount() 
    { 
        return models.size(); 
    } 

    public Object getValueAt(int rowIndex, int columnIndex) 
    { 
        Rectangle bounds = models.get(rowIndex).getBounds(); 
        switch(columnIndex) 
        { 
            case 0:  return models.get(rowIndex).getID(); 
            case 1:  return models.get(rowIndex).getType(); 
            case 2:  return bounds.x; 
            case 3:  return bounds.y; 
            case 4:  return bounds.width;  
            case 5:  return bounds.height;
            default: return null; 
        } 
    } 

    public boolean isCellEditable(int row, int col) { 
        return false; 
    } 

    public void modelChanged(DShapeModel model) { 
        int index = models.indexOf(model); 
        fireTableRowsUpdated(index, index); 
    } 
 
}

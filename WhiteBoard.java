import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.net.*;


public class WhiteBoard extends JFrame
{
    private static char normal = 'n'; 
    private static char server = 's'; 
    private static char cline = 'c'; 
	static JFrame frame;
	static JPanel root;
	private Canvas canvas;
	TableData table;
	JTable t;
	JLabel label;
	private JComboBox fontSelector; 
	private HashMap<String, Integer> fontMap;
	private JTextField textField; 
	private static int nextID = 0;  
	private boolean justUpdatedField;
	private JFileChooser fileChooser; 
	private ArrayList<ObjectOutputStream> outputs = new ArrayList<ObjectOutputStream>(); 
	private char mode;
	JButton s, c;
	
	private ArrayList<JComponent> shutdown;
	public WhiteBoard()
	{
		frame = new JFrame("WhiteBoard");
		frame.setPreferredSize(new Dimension(800, 400));
		frame.setLayout(new BorderLayout());
		frame.setResizable(false);
		mode = normal;
		
		root = new JPanel();
		root.setPreferredSize(new Dimension(400, 400));
		addButton();
		addDataTable();
		setUpCanvas(); 
		frame.add(root, BorderLayout.WEST);
		
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	private void setUpCanvas() 
	{
		canvas = new Canvas(this);
		frame.add(canvas, BorderLayout.CENTER);
	}
	private void addButton() 
	{
		JPanel button = new JPanel();
		Box parentBox = Box.createVerticalBox();
		shutdown = new ArrayList<JComponent>();
		Box zero = new Box(BoxLayout.X_AXIS);
		Box network = new Box(BoxLayout.X_AXIS);
		Box first = new Box(BoxLayout.X_AXIS);
		Box second = new Box(BoxLayout.X_AXIS);
		JButton open, save, saveImage, rect, oval, line, text, getColor, front, back, remove;
		
		
		fileChooser = new JFileChooser();
		open = new JButton("Open");
		zero.add(open);
		zero.add(save = new JButton("Save"));
		zero.add(saveImage = new JButton("Save Image"));
		zero.setPreferredSize(new Dimension(400, 33));
		
		openListener(open);
		saveListener(save);
		saveImageListener(saveImage);
		
		network.add(s = new JButton("Server Start"));
		network.add(c = new JButton("Cline Start")); 
		network.add(Box.createHorizontalStrut(60));
		label = new JLabel("NORMAL MODE");
		network.add(label);
		network.setPreferredSize(new Dimension(400, 33));
		
		serverListener(s);
		clineListener(c);
		
		first.add(rect = new JButton("Rect"));
		first.add(oval = new JButton("Oval"));
		first.add(line = new JButton("Line"));
		first.add(text = new JButton("Text"));
		first.setPreferredSize(new Dimension(400, 33));
		
		rectListener(rect);
		ovalListener(oval);
		lineListener(line);
		textListener(text);
		
		second.add(getColor = new JButton("Set Color"),Box.LEFT_ALIGNMENT);
		second.setPreferredSize(new Dimension(400, 33));
		
		changeColorListener(getColor);
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment(); 
        String fonts[] = ge.getAvailableFontFamilyNames(); 
        fontMap = new HashMap<String, Integer>(); 
        for(int i = 0; i < fonts.length; i++) 
        { 
            fontMap.put(fonts[i], i); 
        }
        fontSelector = new JComboBox(fonts);
        fontSelector.addActionListener(new ActionListener() 
	    { 
            public void actionPerformed(ActionEvent e) { 
                if(canvas.hasSelected() && canvas.getSelected() instanceof DText) 
                    canvas.setFontForSelected((String) fontSelector.getSelectedItem()); 
            } 
        });
	    Box textFieldBox = new Box(BoxLayout.X_AXIS);
	    textField = new JFormattedTextField("");
	    textField.getDocument().addDocumentListener(new DocumentListener() 
	    { 
            public void changedUpdate(DocumentEvent e) 
            { 
            } 
     
            public void insertUpdate(DocumentEvent e) 
            { 
                handleTextChange(e); 
            } 
     
            public void removeUpdate(DocumentEvent e) 
            { 
                handleTextChange(e); 
            } 
        }); 
	    textFieldBox.add(textField);
	    textFieldBox.add(fontSelector);
	    textFieldBox.setPreferredSize(new Dimension(400,33));

	    
		Box position = new Box(BoxLayout.X_AXIS);
		position.add(front = new JButton("Move To Front"));
		position.add(back = new JButton("Move to Back"));
		position.add(remove = new JButton("Remove Shape"));
		position.setPreferredSize(new Dimension(400, 33));
		
		frontListener(front);
		backListener(back);
		removeListener(remove);
		parentBox.add(zero);
		parentBox.add(network);
		parentBox.add(first);
		parentBox.add(second);
		parentBox.add(textFieldBox);
		parentBox.add(position);
		
	    for (Component component : parentBox.getComponents())
	    {
	        ((JComponent) component).setAlignmentX(Box.LEFT_ALIGNMENT);
	    }
	    
		shutdown.add(open);
		shutdown.add(save);
		shutdown.add(saveImage);
		shutdown.add(rect);
		shutdown.add(oval);
		shutdown.add(line);
		shutdown.add(text);
		shutdown.add(getColor);
		shutdown.add(front);
		shutdown.add(back);
		shutdown.add(remove);
	    
	    button.add(parentBox);
	    
		root.add(button);
	}
	
	public void addDataTable()
	{
		table = new TableData();
		t = new JTable(table);
		t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane scroll = new JScrollPane(t);
		scroll.setPreferredSize(new Dimension(400, 200));
		root.add(scroll);
	}
	
    public void rectListener(JButton rect) 
    {
    	rect.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
            	addShape(new DRectModel()); 
            }
        });
    }
    
	public void ovalListener(JButton oval) 
    {
    	oval.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
            	addShape(new DOvalModel());
            }
        });
    }
	
	public void lineListener(JButton line) 
    {
    	line.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
            	addShape(new DLineModel());
            }
        });
    }
	
	public void textListener(JButton oval) 
    {
    	oval.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
            	addShape(new DTextModel());
            }
        });
    }
	
    public void changeColorListener(JButton getColor)
    {
    	getColor.addActionListener(new ActionListener() 
    	{

			public void actionPerformed(ActionEvent e) 
			{
				if(canvas.hasSelected())
				{
					Color c = JColorChooser.showDialog(frame, "", canvas.getSelected().getColor());
					if(c != null && !c.equals(canvas.getSelected().getColor()))
					{
						canvas.setSelectedColor(c);
					}
				}
			}
		});
	}
    
    private void frontListener(JButton front)
    {	
    	front.addActionListener(new ActionListener() 
    	{
			public void actionPerformed(ActionEvent e) 
			{
				if(canvas.hasSelected())
				{
					canvas.moveSelectedToFront();
				}
			}
		});
	}
    
    private void backListener(JButton back)
    {	
    	back.addActionListener(new ActionListener() 
    	{
			public void actionPerformed(ActionEvent e) 
			{
				if(canvas.hasSelected())
				{
					canvas.moveSelectedToBack();
				}
			}
		});
	}
    
    private void removeListener(JButton remove)
    {	
    	remove.addActionListener(new ActionListener() 
    	{
			public void actionPerformed(ActionEvent e) 
			{
				if(canvas.hasSelected())
				{
					canvas.markSelectedShapeForRemoval();
				}
			}
		});
	}
    
    private void openListener(JButton open)
    {	
    	open.addActionListener(new ActionListener() 
    	{
			public void actionPerformed(ActionEvent e) 
			{
				JFileChooser choose = new JFileChooser();
				int status = choose.showOpenDialog(frame);
		        if(status == JFileChooser.APPROVE_OPTION) 
		        { 
		            canvas.openCanvas(choose.getSelectedFile()); 
		            
		            for(DShape shape : canvas.getShapes()) 
		            {
		                doSend(Message.ADD, shape.getModel()); 
		            }
		            
		        } 
			}
		});
	}
    
    private void saveListener(JButton save)
    {	
    	save.addActionListener(new ActionListener() 
    	{
			public void actionPerformed(ActionEvent e) 
			{
				JFileChooser choose = createFileChooser();
				int status = choose.showSaveDialog(frame);
		        if(status == JFileChooser.APPROVE_OPTION) 
		        {
		            canvas.saveCanvas(choose.getSelectedFile()); 
		        }
			}
		});
	}
    
    private void saveImageListener(JButton saveImage)
    {	
    	saveImage.addActionListener(new ActionListener() 
    	{
			public void actionPerformed(ActionEvent e) 
			{
				JFileChooser choose = createFileChooser();
		        int status = choose.showSaveDialog(frame); 
		        if(status == JFileChooser.APPROVE_OPTION) 
		        {
		            canvas.saveImage(choose.getSelectedFile()); 
		        }
			}
		});
	}
    
	private void serverListener(JButton server) 
	{
		server.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				server();
			}
		});
	}
    
	private void server()
	{
        String result = JOptionPane.showInputDialog("Run server on port", "39587"); 
        if(result != null) 
        { 
            disable(server); 
            label.setText("SERVER MODE"); 
            mode = server; 
            ServerAccepter serverAccepter = new ServerAccepter(Integer.parseInt(result.trim())); 
            serverAccepter.start(); 
        } 
	}
	
	public class ServerAccepter extends Thread
	{		
        private int port; 
        
        public ServerAccepter(int port) { 
            this.port = port; 
        } 
         
        public void run() 
        { 
            try 
            { 
                ServerSocket serverSocket = new ServerSocket(port); 
                while(true) 
                { 
                    Socket toClient = null; 
                    toClient = serverSocket.accept(); 
                    final ObjectOutputStream out = new ObjectOutputStream(toClient.getOutputStream()); 
                    if(!outputs.contains(out)) { 
                        Thread worker = new Thread(new Runnable() 
                        { 
                            public void run() 
                            { 
                                for(DShape shape : canvas.getShapes()) 
                                    try 
                                { 
                                        out.writeObject(getXMLStringForMessage(new Message(Message.ADD, shape.getModel()))); 
                                        out.flush(); 
                                    } catch (Exception ex) 
                                	{ 
                                        ex.printStackTrace(); 
                                    } 
                            } 
                        }); 
                        worker.start(); 
                    } 
                    addOutput(out);      
                } 
            } catch (Exception ex)
            { 
                ex.printStackTrace(); 
            } 
        } 
		
	}
	
    private void disable(char mode) 
    {
		s.setEnabled(false);
		c.setEnabled(false);
		if(mode == cline)
		{
			for(JComponent c: shutdown)
			{
				c.setEnabled(false);
			}
		}
	}
    
    private void clineListener(JButton cline) 
    {
    	cline.addActionListener(new ActionListener() 
    	{
			public void actionPerformed(ActionEvent e) 
			{
				cline();
			}
		});
    }
    
    private void cline()
    {
    	String result = JOptionPane.showInputDialog("Connect to host:port", "127.0.0.1:39587"); 
        if(result != null) 
        { 
            String[] parts = result.split(":"); 
            disable(cline); 
            label.setText("CLINE MODE");
            mode = cline; 
            ClientHandler clientHandler = new ClientHandler(parts[0].trim(), Integer.parseInt(parts[1].trim())); 
            clientHandler.start(); 
        } 
    }
    
    private class ClientHandler extends Thread
    {
    	private String n;
    	private int p;
    	public ClientHandler(String name, int port)
    	{
    		this.n = name;
    		this.p = port;
    	}
    	
    	public void run()
    	{
    		try
    		{
    			Socket server = new Socket(n, p);
    			ObjectInputStream i = new ObjectInputStream(server.getInputStream());
    			boolean valid = true;
    			while(valid)
    			{
                    String xmlString = (String) i.readObject(); 
                    XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(xmlString.getBytes())); 
                    Message message = (Message) decoder.readObject(); 
                     
                    processMessage(message); 
    			}
    		}
    		catch (Exception e) 
    		{

			}
    	}
    }
    
	protected JFileChooser createFileChooser() 
    {
    	JFileChooser chooser = new JFileChooser();
    	try
    	{
    	    File dir = new File(new File(".").getCanonicalPath());
    	    chooser.setCurrentDirectory(dir);
    	}
    	catch (Exception ex) 
    	{
    	    ex.printStackTrace();
    	}
    	
    	return chooser;
    }
    
    public void processMessage(Message message) 
    {
        SwingUtilities.invokeLater(new Runnable() 
        { 
            public void run() 
            { 
                DShape shape = canvas.getShapeWithID(message.getModel().getID()); 
                switch(message.getCommand()) 
                { 
                    case Message.ADD: 
                        if(shape == null) 
                            canvas.addShape(message.getModel()); 
                        break; 
                    case Message.REMOVE:  
                        if(shape != null) 
                            canvas.markForRemoval(shape); 
                        break; 
                    case Message.BACK:  
                        if(shape != null) 
                            canvas.moveToBack(shape); 
                        break; 
                    case Message.FRONT:  
                        if(shape != null) 
                            canvas.moveToFront(shape); 
                        break; 
                    case Message.CHANGE: 
                        if(shape != null) 
                            shape.getModel().mimic(message.getModel()); 
                        updateTableSelection(shape); 
                        break; 
                    default: break; 
                } 
            } 
        }); 
		
	}
	private void handleTextChange(DocumentEvent e) 
    { 
        if(canvas.hasSelected() && canvas.getSelected() instanceof DText) 
            canvas.setTextForSelected(textField.getText()); 
    } 
    
    public boolean isNotClient() 
    { 
        return mode != cline; 
    } 
     
    public boolean isServer() { 
        return mode == server; 
    } 
    
    public void addShape(DShapeModel model) 
    { 
    	int x, y, width, heigh;
    	x = new Random().nextInt(200);
    	y = new Random().nextInt(200);
    	width = new Random().nextInt(200);
    	heigh = new Random().nextInt(200);
    	
    	if(model instanceof DLineModel)  
    	{
    		((DLineModel) model).modifyWithPoints(new Point(x,y), new Point(x + width, y + heigh));
    	}
    	else
    	{
    		model.setBounds(x,y, width, heigh); 
    	}
         
        canvas.addShape(model); 
    } 
 
    
    public void addToTable(DShape shape) 
    { 
        table.addModel(shape.getModel()); 
        updateTableSelection(shape); 
    } 
    
    public void updateTableSelection(DShape shape) 
	{
        t.clearSelection(); 
        if(shape != null)
        { 
            int index = table.getRowForModel(shape.getModel()); 
            t.setRowSelectionInterval(index, index); 
        }
	}
	
    public void didMoveToBack(DShape shape) 
    { 
    	table.moveModelToBack(shape.getModel()); 
        updateTableSelection(shape); 
    } 
     
    public void didMoveToFront(DShape shape) 
   { 
    	table.moveModelToFront(shape.getModel()); 
        updateTableSelection(shape); 
    } 
      
    public void didRemove(DShape shape) 
    { 
    	table.removeModel(shape.getModel()); 
        updateTableSelection(null); 
    } 
     
    public void clearTable() 
    { 
        updateTableSelection(null); 
        table.clear(); 
    } 
    
	public static void main(String[] args)
	{
		WhiteBoard whiteboard1 = new WhiteBoard();
		WhiteBoard whiteboard2 = new WhiteBoard();
	}
	public static int getNextIDNumber() 
	{
		nextID++;
		return nextID;
	}
	public void updateFontGroup(String text, String fontName) 
	{
        int index = fontMap.get(fontName); 
        fontSelector.setSelectedIndex(index); 
        textField.setText(text); 
        justUpdatedField = true; 
	}
	
    public static class Message 
    { 
        public static final int ADD    = 0; 
        public static final int REMOVE = 1; 
        public static final int FRONT  = 2; 
        public static final int BACK   = 3; 
        public static final int CHANGE = 4; 
         
        public int command; 
        public DShapeModel model; 
         
        public Message() { 
            command = -1; 
            model = null; 
        } 
         
        public Message(int command, DShapeModel model) { 
            this.command = command; 
            this.model = model; 
        } 
         
        public int getCommand() { 
            return command; 
        } 
         
        public void setCommand(int cmd) { 
            command = cmd; 
        } 
         
        public DShapeModel getModel() { 
            return model; 
        } 
         
        public void setModel(DShapeModel model) { 
            this.model = model; 
        } 
    }

	public void doSend(int add, DShapeModel model) 
	{
        Message message = new Message(); 
        message.setCommand(add); 
        message.setModel(model); 
        sendRemote(message); 
	}
	public void sendRemote(Message message)
	{
        String xmlString = getXMLStringForMessage(message); 
        Iterator<ObjectOutputStream> it = outputs.iterator(); 
        while(it.hasNext()) 
        { 
            ObjectOutputStream out = it.next(); 
            try 
            { 
                out.writeObject(xmlString); 
                out.flush(); 
            } catch (Exception ex)
            { 
                ex.printStackTrace(); 
                it.remove(); 
            } 
        } 
	}
	public String getXMLStringForMessage(Message message) 
	{
        OutputStream memStream = new ByteArrayOutputStream(); 
        XMLEncoder encoder = new XMLEncoder(memStream); 
        encoder.writeObject(message); 
        encoder.close(); 
        return memStream.toString(); 
	} 
	
    public synchronized void addOutput(ObjectOutputStream out)
    { 
        outputs.add(out); 
    }
    
}

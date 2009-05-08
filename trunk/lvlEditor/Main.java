
package lvlEditor;
import game.Node;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;

//the shell for a Game
class Main extends JFrame implements ActionListener  {

	Map<String,TileLabel> tileDic = new HashMap<String,TileLabel>();
	
	Vector<TileLabel> getAvailableTiles()
	{
		Vector<TileLabel> v = new Vector<TileLabel>();
		for(File f : new File("./tileImages/").listFiles())
		{
			TileLabel t = new TileLabel(f);
			v.add(t);
			tileDic.put(t.toString(),t);
		}
		return v;
	}
	class TileLabel extends JLabel
	{
		String n;
		TileLabel(File f){
			this(f, f.getName() );
		}
		TileLabel(File f, String n)
		{
			super(n,new ImageIcon(f.toString()),JLabel.LEFT);
			this.n=n;
		}
		public String toString(){return n;}
	}
	
	String[] types = {
		"walkable", // (->trainer or entity id or blank)
		"door", //(->targetmap,targetx,targety)
		"pokegrassOrCave", // (->wildgeneratorid)
		"obstacle", // (->blank or text for a sign)
		"water", //(->generator)
		"cliff" //(->T L R or D)
		};
	
	JTextField name = new JTextField("newmap", 5);
	JTextField height = new JTextField("40", 3);
	JTextField width = new JTextField("40", 3);
	JComboBox tile = new JComboBox(getAvailableTiles());
	JComboBox type = new JComboBox(types);
	JTextField target = new JTextField("",5);
	JButton save = new JButton("Save");
	JButton load = new JButton("Load");
	JLabel location = new JLabel("<hoveroveratile>");

	static int lvlWidth = 10;
	static int lvlHeight = 10;
	public static final int SQUARESIDE = 16; // each square in the origional pokemon seems to be 16x16
	//static String tileType = supportedTiles[1];
	Tile tiles[][] = new Tile[10][10];
	Tile currentTile;

	Main(){
		super("LEVEL EDITOR!");

		tile.setRenderer(new JLabelCellRenderer());

		JPanel top = new JPanel();
		top.add(new JLabel("Name:"));
		top.add(name);
		top.add(new JLabel("Width:"));
		top.add(width);
		top.add(new JLabel("Height:"));
		top.add(height);
		top.add(save);
		top.add(load);
		top.add(location);
		
		JPanel bottom = new JPanel();
		bottom.add(tile);
		bottom.add(type);
		bottom.add(new JLabel("->"));
		bottom.add(target);

		height.addActionListener(this);
		width.addActionListener(this);
		save.addActionListener(this);
		load.addActionListener(this);

		add(new JScrollPane(new DisplayView()));
		add(top, BorderLayout.NORTH);
		add(bottom, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(500,500);
		setVisible(true);
		
		resizeMap(20,20,-1);
	}

	void resizeMap(int newW, int newH, int location)
	{
		Tile[][] newTiles = new Tile[newW][newH];
		
		JOptionPane.showMessageDialog(this,  "" + location, "Done", JOptionPane.PLAIN_MESSAGE);
		
		//fill with default tiles
		for(int x=0; x<newW; x++)
			for(int y=0; y<newH; y++)
				newTiles[x][y] = new Tile(x,y);
				
		int offset_target_x = 0;
		int offset_target_y = 0;
		int offset_source_x = 0;
		int offset_source_y = 0;
		
		// resizing from text box
		if(location != -1)
		{
			// changing width
			if(newW > lvlWidth && location == 0)
				offset_target_x = newW - lvlWidth;
			else if(newW > lvlWidth && location == 1)
				offset_target_x = 0;
			else if(newW < lvlWidth && location == 0)
				offset_source_x = lvlWidth - newW;
			else if(newW < lvlWidth && location == 1)
				offset_source_x = 0;
			else if(newH > lvlHeight && location == 0)
				offset_target_y = newH - lvlHeight;
			else if(newH > lvlHeight && location == 1)
				offset_target_y = 0;
			else if(newH < lvlHeight && location == 0)
				offset_source_y = lvlHeight - newH;
			else if(newH < lvlHeight && location == 1)
				offset_source_y = 0;
		}
				
		//copy over orginals that will fit
		for(int x=0; x<(newW-offset_target_x) && x<(lvlWidth-offset_source_x); x++)
			for(int y=0; y<(newH-offset_target_y) && y<(lvlHeight-offset_source_y); y++)
				if(tiles[x+offset_source_x][y+offset_source_y]!=null)
				{
					newTiles[x+offset_target_x][y+offset_target_y] = tiles[x+offset_source_x][y+offset_source_y];
					newTiles[x+offset_target_x][y+offset_target_y].setX(x+offset_target_x);
					newTiles[x+offset_target_x][y+offset_target_y].setY(y+offset_target_y);
				}
					
		tiles = newTiles;
		lvlWidth = newW; width.setText(""+lvlWidth);
		lvlHeight = newH; height.setText(""+lvlHeight);
		currentTile = tiles[0][0];
		repaint();
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == width)
		{
			Object[] possibleValues = {"Left", "Right"};
			Object selectedValue = JOptionPane.showInputDialog(null, "Which Side?", "Input", JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[1]);
			resizeMap(new Integer(width.getText()), lvlHeight, (selectedValue=="Left")?(0):(1));
		}
		else if (e.getSource() == height)
		{
			Object[] possibleValues = {"Top", "Bottom"};
			Object selectedValue = JOptionPane.showInputDialog(null, "Which Side?", "Input", JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[1]);
			resizeMap(lvlWidth, new Integer(height.getText()), (selectedValue=="Top")?(0):(1));
		}
		else if(e.getSource() == save)
		{

			String filename = getFile("Save");
			
			Node mapNode = new Node("map");
			mapNode.addSubnode("width",""+lvlWidth);
			mapNode.addSubnode("height",""+lvlHeight);
			mapNode.addSubnode("name",name.getText());
			for(int x = 0; x < lvlWidth; x++)
				for (int y = 0; y < lvlHeight; y++)
					mapNode.addSubnode( tiles[x][y].asNode() );
					
			try
			{
				OutputStream os = new FileOutputStream(filename);
				mapNode.tryWriteOn(os); 
				os.flush();
				os.close();
			}
			catch (Exception ex) { }
			
			JOptionPane.showMessageDialog(this,  "Save complete", "Done", JOptionPane.PLAIN_MESSAGE);
		}
		else if(e.getSource() == load)
		{
			try{
				String filename = getFile("Load");
				Node mapNode = Node.parseFrom(new FileInputStream(filename));
				name.setText(mapNode.contentOf("name"));
				int w = new Integer( mapNode.contentOf("width"));
				int h = new Integer( mapNode.contentOf("height"));
				resizeMap(w,h,-1);
				
				
				
				for(Node tileNode: mapNode.subnodes("tile"))
				{
					Tile t = Tile.fromNode(tileNode);
					
					tiles[t.getX()][t.getY()] = t;
				}
				
				currentTile = tiles[0][0];
				repaint();
				
			}catch(Exception ex){}
		}
	}
	
	String getFile(String prompt)
	{
		JFileChooser chooser = new JFileChooser("./");

		if(chooser.showDialog(this, prompt ) == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile().toString();
			
		return "";
	}

	public void setTileValues()
	{
		tiles = new Tile[lvlWidth][lvlHeight];
		for(int x = 0; x < lvlWidth; x++)
			for(int y = 0; y < lvlHeight; y++)
				tiles[x][y] = new Tile(x, y);
	}

	void pushTile(Tile t)
	{
		currentTile.setImage( tile.getSelectedItem().toString() );
		currentTile.setType( type.getSelectedItem().toString() );
		currentTile.setTarget( target.getText() );
		
		location.setText(currentTile.toString());
		repaint();
	}
	
	void pullTile(Tile t)
	{
	
		target.setText( currentTile.getTarget() );
		tile.setSelectedItem( tileDic.get(currentTile.getImage()) );
		type.setSelectedItem( currentTile.getType());
			
		repaint();
	}
	
	String upToSpace(String s)
	{
		return s.substring(0,s.indexOf(' '));
	}

	class DisplayView extends JComponent implements MouseListener, MouseMotionListener
	{
		public DisplayView()
		{
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		public synchronized void paint(Graphics g)
		{
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, lvlWidth * SQUARESIDE, lvlHeight * SQUARESIDE);

			for (int x = 0; x < lvlWidth; x++)
				for (int y = 0; y < lvlHeight; y++)
					if(tiles[x][y]!=null) 
						tiles[x][y].drawImage(this, g);

			g.setColor(Color.RED);
			for (int x = 0; x <= lvlWidth * SQUARESIDE; x += SQUARESIDE)
				g.drawLine(x, 0, x, lvlHeight * SQUARESIDE);
			for (int y = 0; y <= lvlHeight * SQUARESIDE; y += SQUARESIDE)
				g.drawLine(0, y, lvlWidth * SQUARESIDE, y);

		}
		public Dimension getPreferredSize()
		{
			return new Dimension(lvlWidth * SQUARESIDE + 20, lvlHeight * SQUARESIDE + 50);
		}

		public void mouseMoved(MouseEvent e)
		{
			int mouseX = e.getX();// - 10; //Translated for width
			int mouseY = e.getY();// - 30; //Translated for height
			if(mouseX > 0 && mouseY > 0 && mouseX <= lvlWidth * SQUARESIDE - 1 && mouseY < lvlHeight * SQUARESIDE - 1)
				currentTile = tiles[mouseX / SQUARESIDE][mouseY / SQUARESIDE];
				
			location.setText(currentTile.toString());
			System.out.println(currentTile);

		}
		public void mouseDragged(MouseEvent e)
		{
			int mouseX = e.getX();// - 10; //Translated for width
			int mouseY = e.getY();// - 30; //Translated for height
			if (mouseX > 0 && mouseY > 0 && mouseX <= lvlWidth * SQUARESIDE - 1 && mouseY < lvlHeight * SQUARESIDE - 1)
				currentTile = tiles[mouseX / SQUARESIDE][mouseY / SQUARESIDE];
				pushTile(currentTile);
		}
		
		public void mouseClicked(MouseEvent e)
		{
			if (e.getButton() == 1)
			{
				pushTile(currentTile);
			}
			else pullTile(currentTile);
		}

		public void mouseEntered(MouseEvent e) { }
		// Invoked when the mouse enters a component.
		public void mouseExited(MouseEvent e)  {}
		// Invoked when the mouse exits a component.
		public void mousePressed(MouseEvent e)
		{
			if (e.getButton() == 1)
			{
				pushTile(currentTile);
			}
			else pullTile(currentTile);
		}
		public void mouseReleased(MouseEvent e) { }
		// Invoked when a mouse button has been released on a component.
	


	}
	
	
	class JLabelCellRenderer extends DefaultListCellRenderer {
	    public Component getListCellRendererComponent(JList list,Object value, int index,boolean iss,boolean chf)
	    {
			if(value instanceof JLabel)
			{
				JLabel l = (JLabel)value;
				super.getListCellRendererComponent(list, value, index, iss, chf);
				setText(l.getText());
				setIcon(l.getIcon());
				return this;
			}
			return new JLabel();
	    }
	}

	
	public static void main(String[] args) throws Exception {
	
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		new Main();
	}
}
	
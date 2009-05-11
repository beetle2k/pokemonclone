package game;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
*The shell for a game, a window containing a presenter.
*/
class Game extends JComponent implements KeyListener {

	//the current area, battle, or menu that is the root to be displayed and receive key events
	private Presenter currentPresenter;
	private Player player = new Player();
		
	private Game()
	{
		enterPresenter(new StartPresenter());
		
		new Thread(){
			public void run(){
				int ms=0;
				while(true) try{
					Thread.sleep(100);
					ms+=100;
					currentPresenter.step(ms);
					repaint();
				}catch(Exception e){}
			}
		}.start();
		
		setPreferredSize(new Dimension(16*20,16*18));
	}
	
	public void enterPresenter(Presenter p){
		currentPresenter = p;
		p.initGame(this);
		repaint();
	}
	
	public Player player()
	{
		return player;
	}
	
	public void paint(Graphics g){
		g.setColor(Color.RED);
		g.fillRect(0,0,16*20,16*18);
		currentPresenter.drawOn((Graphics2D)g);
	}
	
	public void keyPressed(KeyEvent e){
		int n = e.getKeyCode();
		char c = e.getKeyChar();
		c = Character.toUpperCase(c);
		if(n==KeyEvent.VK_UP) c='W';
		if(n==KeyEvent.VK_LEFT) c='A';
		if(n==KeyEvent.VK_DOWN) c='S';
		if(n==KeyEvent.VK_RIGHT) c='D';
		if(n==KeyEvent.VK_ENTER) c='A';
		if(n==KeyEvent.VK_BACK_SPACE) c='B';
		if(c=='Z') c='A';
		if(c=='X') c='B';
		currentPresenter.keyPressed(c);
		repaint();
	}
	
	public void keyReleased(KeyEvent e){}
	public void keyTyped(KeyEvent e){}//pressed and released

	
	
	//in the Beginning, there was Main and it was Good
	public static void main(String[] args){
		
		TM.numbered(5);
		HM.numbered(5);
		
		JFrame f = new JFrame("PokemonClone!");
		Game g = new Game();
		f.addKeyListener(g);
		f.add(g);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setResizable(false);
		f.setVisible(true);
		
		Area.named("");
		StartPresenter.loading=false;
	}
}
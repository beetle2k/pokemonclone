package game;

import java.awt.Graphics2D;
import java.awt.Color;
import java.util.*;
import java.io.*;

class StartPresenter extends Presenter {

	private List<Species> ss = Species.all();
	private Species s = ss.get(0);
	
	static boolean loading=true;
	
	public void drawOn(Graphics2D g){	
		g.setColor(Color.WHITE);
		g.fillRect(0,0,16*20,16*18);
		
		g.drawImage(s.image32(),0,0,null);
		g.drawImage(s.image80(),32,0,null);
		g.drawImage(s.imageFront(),32+80,0,null);
		g.drawImage(s.imageBack(),32+80+64,0,null);
		g.setColor(Color.BLACK);
		g.drawString("No. "+s.number(),0,70);
		g.drawString(s.name(),0,80);
		g.drawString(s.description(),0,90);
		
		g.drawString("Credits:",0,120);
		g.drawString("(add yourself but no one else!)",0,130);
		g.drawString("Ryan Macnak",0,140);
		g.drawString("Adam Hendrickson", 0, 150);
		g.drawString("Michal Broniek",0,160);
		g.drawString("Andrew Siegle",0,170);
		g.drawString("Joe Maguire",0,180);

		if(loading) g.drawString("Loading maps...",0,200);
	}
	
	public void buttonPressed(Button b){
	
		if(loading) return;
		
		Area a = Area.named("route01");
		Player p = game().player();
		a.tileAt(5,5).entity(p);
		enterPresenter(a);
	}
	
	public void step(int ms){
		if(ms%1500!=0)return;
		
		int next = ss.indexOf(s) + 1;
		if(next>=ss.size()) next=0;
		s = ss.get(next);
	}
}
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/*
 * So this could work on many levels. 
 * You can find matches at multiple "frequencies", multiple window sizes/pixel sizes
 * and then basically mash up the best matches together across many criterias
 * to form the selection. 
 */

public class MarkovPainter extends JFrame implements MouseListener{
	BufferedImage img;
	BufferedImage activePatch;
	public final int WINDOW_SIZE = 10;

	Patch[][] patches;
	
	JLabel selected;
    ImageIcon selectedIcon;
	
	public static void main(String args[]){
		   try{
			   BufferedImage rawImg = ImageIO.read(new File("sand.jpg"));
			   MarkovPainter painter = new MarkovPainter(rawImg);
			   painter.learn();
			   System.out.println("Waiting for stuff");
			   while(true){}
			   
		   }catch(Exception e){
			   System.out.println("Could not read image");
		   }	
		   
	}
	
    /* Mask is 1 at points we want to inpaint */
    public MarkovPainter(BufferedImage i){
    	super();
 	   	setSize( 1024, 768 );
 	   	setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
 	   	
 	   	JPanel panel = new JPanel();
 	   	
 	   	img = i;
 	   	JLabel originalImg = new JLabel(new ImageIcon(i));
 	   	originalImg.addMouseListener(this);
 	   	panel.add(originalImg);
 	   	
 	   	selectedIcon = new ImageIcon(i.getScaledInstance(200, 100, 0));
 	   	
 	  	selected = new JLabel(selectedIcon);
 	  	selected.setSize(200, 100);
 	  	
 	  	selected.addMouseListener(this);
 	  	panel.add(selected);
 	  	
 	  	
 	  	panel.setVisible(true);
 	  	this.add(panel);

 	  	setVisible(true);
 	  	patches = new Patch[img.getWidth() / WINDOW_SIZE][img.getHeight() / WINDOW_SIZE];
    }
    
	@Override
	public void mouseClicked(MouseEvent arg0) {
		int y = arg0.getY();
		int x = arg0.getX();
		
		// Coordinate is 375, 251
		// We fill things left to right, row by row. 
		// So first, find the row number
		int patchesPerLine = (int) (img.getWidth() / WINDOW_SIZE);
		int lineIndex = (int) Math.ceil( y  / WINDOW_SIZE );
		int subIndex = (int) (x / WINDOW_SIZE);
		System.out.println("Line " + lineIndex + " Within the line at "+subIndex);
		int coordinate = (lineIndex * patchesPerLine) + subIndex;

		System.out.println("Click at " + x + " , " + y + " is " + coordinate);
		//selectedIcon.setImage(allPatches.get(coordinate).pixels.getScaledInstance(50, 50, 0));
		selectedIcon.setImage(patches[subIndex][lineIndex].pixels.getScaledInstance(50, 50, 0));
		
		System.out.println("Set patch successfully");
		selected.repaint();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {	}
	@Override
	public void mousePressed(MouseEvent arg0) {}
	@Override
	public void mouseReleased(MouseEvent arg0) {}
    
    
    
    
    public void paint(Graphics g){
		//g.drawImage(img, 0, 0, 800, 600, null);
    	super.paint(g);
   }
    
    ArrayList<Patch> allPatches = new ArrayList<Patch>();

    public void learn(){
    	BufferedImage patch = this.img;
		for(int y = 0; y + WINDOW_SIZE < patch.getHeight(); y+=WINDOW_SIZE){
			//ArrayList<Patch> currentRow = new ArrayList<Patch>();
			Patch previousPatch = null;
			int patchY = (int) y / WINDOW_SIZE;
			for(int x = 0; x + WINDOW_SIZE < patch.getWidth(); x+=WINDOW_SIZE){
				int patchX = (int) x / WINDOW_SIZE;
				
			    BufferedImage subImage = patch.getSubimage(x, y, (int) WINDOW_SIZE, (int) WINDOW_SIZE);
			    Patch p = new Patch(subImage);
			    double weight = 1.0;
			    double decay = 0.9;
			    
			    if(previousPatch != null){
			    	PatchConnection newCon = new PatchConnection(previousPatch, p, 0.9);
			    	previousPatch.nextStates.add(newCon);
			    }
			    previousPatch = p;
			    //allPatches.add(p);
			    patches[patchX][patchY] = p;
			}
		}
    	
    	
    }
}

class PatchConnection implements Comparable<PatchConnection>{
	Patch currentState;
	Patch nextState;
	
	// Where is nextState relative to the center of the currentState. 
	int dX;
	int dY;
	
	// Just one way probability right now...
	// What's the chance of nextState being on the right of currentState. 
	double probability;
	public PatchConnection(Patch cS, Patch nS, double prob){
		currentState = cS;
		nextState = nS;
		probability = prob;
	}
	
	public int compareTo(PatchConnection pc) {
		if(pc.currentState == this.currentState){
			return pc.nextState.compareTo(nextState);
		}
		else{
			return -1;
		}
	}
}

class Patch implements Comparable{
    BufferedImage pixels;
    
    double brightness = 0.0;
    double probability;
    
    ArrayList<PatchConnection> nextStates = new ArrayList<PatchConnection>();
    ArrayList<Patch> equalStates = new ArrayList<Patch>();
    
    public Patch(BufferedImage p){
    	pixels = p;
		for(int x = 0; x < p.getWidth(); x++){
		    for(int y = 0; y < p.getHeight(); y++){
			brightness += p.getRGB(x, y);
		    }
		}
    }

    public int compareTo(Object o){
		if(o instanceof Patch){
		    Patch p = (Patch) o;
		    double MSE = 0.0;
		    double p_brightness = 0;
		    
		    for(int x = 0; x < p.pixels.getWidth(); x++){
				for(int y = 0; y < p.pixels.getHeight(); y++){
				    p_brightness += p.pixels.getRGB(x, y);
				    MSE += Math.pow( (double) pixels.getRGB(x, y) - p.pixels.getRGB(x, y), 2.0);
				}
		    }
		    
		    MSE /= (p.pixels.getWidth() * p.pixels.getHeight());
		    System.out.println("Patch difference of " + MSE + " self-brighness " + brightness + " other " + p_brightness );
	
		    if(MSE < 10.0){
		    	return 0;
		    }else if(brightness > p_brightness){
		    	return 1;
		    }
		}
		return -1;
    }
}

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class MarkovPainter{
	
	public static void main(String args[]){
		   try{
			   BufferedImage rawImg = ImageIO.read(new File("/hd/Dropbox/workspace/MarkovPainter/sand.jpg"));
			   PatchGrid imgGrid = new PatchGrid(rawImg);
			   
			   BufferedImage maskImg = ImageIO.read(new File("/hd/Dropbox/workspace/MarkovPainter/sandMask.png"));
			   PatchGrid maskGrid = new PatchGrid(maskImg);
			   
			   imgGrid.applyMask(maskGrid);
			   imgGrid.fillMask(maskGrid);
			   
			   PatchDisplay display = new PatchDisplay(imgGrid);
			   
			   System.out.println("Done initializing Markov Painter");
			   while(true){}
			   
		   }catch(IOException e){
			   //System.out.println("Could not read image");
			   e.printStackTrace();
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
    double probability = 1.0;	// 1.0 = 100% Sure about this patch. 0.0
    
    
    public Patch(BufferedImage p){
    	pixels = p;
    	double divisor = p.getWidth() * p.getHeight();
		for(int x = 0; x < p.getWidth(); x++){
		    for(int y = 0; y < p.getHeight(); y++){
		    	//brightness += (p.getRGB(x, y) / divisor);
		    	Color c = new Color(p.getRGB(x, y));
		    	brightness += (.2126 * c.getRed() + .7152 * c.getGreen() + .0722 * c.getBlue());
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
    
    public int getColorDifference(int a, int b){
    	Color colorA = new Color(a);
    	Color colorB = new Color(b);
		int rDiff = Math.abs(colorA.getRed() - colorB.getRed());
		int gDiff = Math.abs(colorA.getGreen() - colorB.getGreen());
		int bDiff = Math.abs(colorA.getBlue() - colorB.getBlue());
		return rDiff + gDiff + bDiff;
    }
    
    public ArrayList<Patch> getNextRight(ArrayList<Patch> patches){
    	double minDiff = Double.MAX_VALUE;
    	ArrayList<Patch> minPatches = new ArrayList<Patch>();
    	Patch minPatch;
    	// Match up the difference between our right most pixel and their
    	// left most pixel
    	int ourRight = this.pixels.getWidth() - 1;
    	for(Patch p : patches){
    		double totalDiff = 0;
    		
    		for(int y = 0; y < pixels.getHeight(); y++){
    			if(p != null && p.pixels != null){
    				
	    			int rDiff = Math.abs(myPix.getRed() - theirPix.getRed());
	    			int gDiff = Math.abs(myPix.getGreen() - theirPix.getGreen());
	    			int bDiff = Math.abs(myPix.getBlue() - theirPix.getBlue());
	    			int diff = rDiff + gDiff + bDiff;
	    			totalDiff += getColorDifference(pixels.getRGB(ourRight, y), p.pixels.getRGB(0, y));
    			}
    		}
    		if(totalDiff < minDiff){
    			minDiff = totalDiff;
    			minPatch = p;
    			minPatches.add(p);
    		}
    	}
    	return minPatches;
    }
}

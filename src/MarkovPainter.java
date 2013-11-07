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
			   
			   PatchDisplay display = new PatchDisplay(imgGrid);
			   
			   imgGrid.applyMask(maskGrid);
			   imgGrid.fillMask(maskGrid);
			   
			   
			   
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
    boolean isMasked = false;
    
    int patchX;
    int patchY;
    
    // = 7650
    static int MAX_EDGE_DIFF = (PatchGrid.WINDOW_SIZE) * (255 * 3);
    
    public Patch(Patch p){
    	pixels = p.pixels;
    	brightness = p.brightness;
    	probability = p.probability;
    	isMasked = p.isMasked;
    	patchX = p.patchX;
    	patchY = p.patchY;
    }
    
    public Patch(BufferedImage p, int px, int py){
    	pixels = p;
    	double divisor = p.getWidth() * p.getHeight();
		for(int x = 0; x < p.getWidth(); x++){
		    for(int y = 0; y < p.getHeight(); y++){
		    	//brightness += (p.getRGB(x, y) / divisor);
		    	Color c = new Color(p.getRGB(x, y));
		    	brightness += (.2126 * c.getRed() + .7152 * c.getGreen() + .0722 * c.getBlue());
		    }
		}
		patchX = px;
		patchY = py;
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
		
		int diff =rDiff + gDiff + bDiff;
		return diff;
    }
    
    // Look along the right edge and find a list of patches
    // that best match up with that edge
    public ArrayList<Patch> getNextRight(ArrayList<Patch> patches){
    	double minDiff = Double.MAX_VALUE;
    	ArrayList<Patch> minPatches = new ArrayList<Patch>();
    	if(isMasked){return minPatches;}
    	int ourRight = this.pixels.getWidth() - 1;
    	
    	for(Patch p : patches){
    		double totalDiff = 0;
    		if(p != null && p.pixels != null){
	    		for(int y = 0; y < pixels.getHeight(); y++){
		    		totalDiff += getColorDifference(pixels.getRGB(ourRight, y), p.pixels.getRGB(0, y));
	    		}
    		}
    		if(totalDiff <= minDiff){
    			minDiff = totalDiff;
    			Patch newMinPatch = new Patch(p);
    			newMinPatch.probability = this.probability * (1.0 - ( (double) totalDiff / MAX_EDGE_DIFF));
    			minPatches.add(newMinPatch); 
    		}
    	}
    	return minPatches;
    }
    
    // Our left edge. Their right edge
    public ArrayList<Patch> getNextLeft(ArrayList<Patch> patches){
    	double minDiff = Double.MAX_VALUE;
    	ArrayList<Patch> minPatches = new ArrayList<Patch>();
    	if(isMasked){return minPatches;}
    	int otherRight = this.pixels.getWidth() - 1;
    	for(Patch p : patches){
    		double totalDiff = 0;
    		if(p != null && p.pixels != null){
	    		for(int y = 0; y < pixels.getHeight(); y++){
		    		totalDiff += getColorDifference(pixels.getRGB(0, y), p.pixels.getRGB(otherRight, y));
	    		}
    		}
    		if(totalDiff <= minDiff){
    			minDiff = totalDiff;
    			Patch newMinPatch = new Patch(p);
    			newMinPatch.probability = this.probability * (1.0 - ( (double) totalDiff / MAX_EDGE_DIFF));
    			minPatches.add(newMinPatch); 
    		}
    	}
    	return minPatches;
    }
    
    // Which patch should come above us?
    public ArrayList<Patch> getNextAbove(ArrayList<Patch> patches){
    	double minDiff = Double.MAX_VALUE;
    	ArrayList<Patch> minPatches = new ArrayList<Patch>();
    	if(isMasked){return minPatches;}
    	int theirBase = this.pixels.getHeight() - 1;
    	for(Patch p : patches){
    		double totalDiff = 0;
    		if(p != null && p.pixels != null){
	    		for(int x = 0; x < pixels.getWidth(); x++){
		    		totalDiff += getColorDifference(pixels.getRGB(x, 0), p.pixels.getRGB(x, theirBase));
	    		}
    		}
    		if(totalDiff <= minDiff){
    			minDiff = totalDiff;
    			Patch newMinPatch = new Patch(p);
    			newMinPatch.probability = this.probability * (1.0 - ( (double) totalDiff / MAX_EDGE_DIFF));
    			minPatches.add(newMinPatch); 
    		}
    	}
    	return minPatches;
    }
    
    public ArrayList<Patch> getNextBelow(ArrayList<Patch> patches){
    	double minDiff = Double.MAX_VALUE;
    	ArrayList<Patch> minPatches = new ArrayList<Patch>();
    	
    	int ourBase = this.pixels.getHeight() - 1;
    	for(Patch p : patches){
    		double totalDiff = 0;
    		if(p != null && p.pixels != null){
	    		for(int x = 0; x < pixels.getWidth(); x++){
		    		totalDiff += getColorDifference(pixels.getRGB(x, ourBase), p.pixels.getRGB(x, 0));
	    		}
    		}
    		if(totalDiff <= minDiff){
    			minDiff = totalDiff;
    			Patch newMinPatch = new Patch(p);
    			newMinPatch.probability = this.probability * (1.0 - ( (double) totalDiff / MAX_EDGE_DIFF));
    			minPatches.add(newMinPatch); 
    		}
    	}
    	return minPatches;
    }    
}

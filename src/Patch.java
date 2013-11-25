import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

class Patch implements Comparable{
    BufferedImage pixels; 
    double brightness = 0.0;
    double probability = 1.0;	// 1.0 = 100% Sure about this patch. 0.0
    boolean isMasked = false;
    
    int patchX;
    int patchY;
    
    int variance = 0;
    
    Color avgColor = Color.white;
    
    //static int COLOR_THRESH = 512;
    static int COLOR_THRESH = 512;
    static double PROB_THRESH = 0.35;
    
    ArrayList<PatchConnection> nextStates = new ArrayList<PatchConnection>();
    ArrayList<PatchConnection> similarPatches = new ArrayList<PatchConnection>();
    
    // = 7650
    static int MAX_EDGE_DIFF = (PatchGrid.WINDOW_SIZE) * (255 * 3);
    
    public Patch(Patch p){
    	if(p != null){
        	pixels = p.pixels;
        	brightness = p.brightness;
        	probability = p.probability;
        	isMasked = p.isMasked;
        	patchX = p.patchX;
        	patchY = p.patchY;
        	avgColor = p.avgColor;    		
    	}else {
    		pixels = new BufferedImage(PatchGrid.WINDOW_SIZE, PatchGrid.WINDOW_SIZE, BufferedImage.TYPE_INT_ARGB);
    		brightness = 0;
    		probability = 0;
    		isMasked = false;
    		patchX = -1;
    		patchY = -1;
    		avgColor = Color.white;
    	}
    }
    
    public Patch(BufferedImage p, int px, int py){
    	pixels = p;
    	int rSum = 0;
    	int gSum = 0;
    	int bSum = 0;
    	int divisor = p.getWidth() * p.getHeight();
		for(int x = 0; x < p.getWidth(); x++){
		    for(int y = 0; y < p.getHeight(); y++){
		    	//brightness += (p.getRGB(x, y) / divisor);
		    	Color c = new Color(p.getRGB(x, y));
		    	brightness += (.2126 * c.getRed() + .7152 * c.getGreen() + .0722 * c.getBlue());
		    	rSum += c.getRed();
		    	gSum += c.getGreen();
		    	bSum += c.getBlue();
		    }
		}
		patchX = px;
		patchY = py;
		avgColor = new Color(rSum / divisor, gSum/divisor, bSum / divisor );
    }
    
    public void findSimilarPatches(ArrayList<Patch> allPatches){
    	
    }
    
    public void computeNextStates(Patch[][] patches, int radius){
    	/*
        BufferedImage flipped = new BufferedImage(
                bi.getWidth(),
                bi.getHeight(),
                bi.getType());
        AffineTransform tran = AffineTransform.getTranslateInstance(bi.getWidth(), 0);
        AffineTransform flip = AffineTransform.getScaleInstance(-1d, 1d);
        tran.concatenate(flip);

        Graphics2D g = flipped.createGraphics();
        g.setTransform(tran);
        g.drawImage(bi, 0, 0, null);
        g.dispose();

        return flipped; */    	
    	
    	// TODO: Flipped versions 
    	for(int xDiff = -1 * radius; xDiff < radius; xDiff++){
    		int relX = this.patchX + xDiff;
    		if(relX < 0 || relX >= patches.length){	// Check y bounds
    			break;
    		}
    		for(int yDiff = -1 * radius; yDiff < radius; yDiff++){
    			int relY = this.patchY + yDiff;
    			// Check bounds
    			if(relY < 0 || relY >= patches[0].length){
    				break;
    			}
				// Gaussian weight
				double distance = Math.sqrt(Math.pow(relX, 2) + Math.pow(relY, 2));
				double weight = 1.0 / distance;
				
    			PatchConnection conn = new PatchConnection(this, patches[relX][relY], xDiff, yDiff, weight);	    				
    		}
    	}
    	
    	
    }

    public int compareTo(Object o){
		if(o instanceof Patch){
		    Patch p = (Patch) o;
		    int total_color_err = 0;
		    double p_brightness = 0;
		    
		    int oVariance = 0;
		    int oSum = 0;
		    int oSum_sqr = 0;		    
		    
		    for(int x = 0; x < p.pixels.getWidth(); x++){
				for(int y = 0; y < p.pixels.getHeight(); y++){
				    p_brightness += p.pixels.getRGB(x, y);
				    //MSE += Math.pow( (double) pixels.getRGB(x, y) - p.pixels.getRGB(x, y), 2.0);
				    total_color_err += getColorDifference(pixels.getRGB(x, y), p.pixels.getRGB(x, y));			    
				}
		    }
		    
		    //total_color_err /= (p.pixels.getWidth() * p.pixels.getHeight());
		    System.out.println("Patch difference of " + total_color_err + " self-brighness " + brightness + " other " + p_brightness );
	
		    if(total_color_err < COLOR_THRESH * (p.pixels.getWidth() * p.pixels.getHeight())){
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
    		if(getColorDifference(avgColor.getRGB(), p.avgColor.getRed()) < COLOR_THRESH){
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
	    			if(newMinPatch.probability > PROB_THRESH)
	    				minPatches.add(newMinPatch);
	    		}
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
    		if(getColorDifference(avgColor.getRGB(), p.avgColor.getRed()) < COLOR_THRESH){
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
	    			if(newMinPatch.probability > PROB_THRESH)
	    				minPatches.add(newMinPatch); 
	    		}
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
    		if(getColorDifference(avgColor.getRGB(), p.avgColor.getRed()) < COLOR_THRESH){
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
	    			if(newMinPatch.probability > PROB_THRESH)
	    				minPatches.add(newMinPatch);
	    		}
    		}
    	}
    	return minPatches;
    }
    
    public ArrayList<Patch> getNextBelow(ArrayList<Patch> patches){
    	double minDiff = Double.MAX_VALUE;
    	ArrayList<Patch> minPatches = new ArrayList<Patch>();
    	
    	int ourBase = this.pixels.getHeight() - 1;
    	for(Patch p : patches){
    		if(getColorDifference(avgColor.getRGB(), p.avgColor.getRed()) < COLOR_THRESH){
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
	    			if(newMinPatch.probability > PROB_THRESH)
	    				minPatches.add(newMinPatch);
	    		}
    		}
    	}
    	return minPatches;
    }    
}

/*
 * Step one - break the image up into a grid of patches
 * For each patch, compute the patches that can come on the left, right, top and bottom
 * The easy answer is the current next patch is a valid next state. 
 * Secondly, the image self flipped over is a valid next state
 * Then, loop through all images and use our border-alignment check with a high threshold. 
 * For each of the patch, compute the list of patches which are most similar to it.
 * Propagate the errors through. The probability should exist outside the node, in the link. This way the same node can be used in multiple places.
 * 
 * 
 */

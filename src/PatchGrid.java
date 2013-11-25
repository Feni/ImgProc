import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

// simon chelley - PDE , natural scene statistics, multi-scale, wavet based. 
public class PatchGrid {
	BufferedImage img;
	public static final int WINDOW_SIZE = 9;
	Patch[][] patches;
	Patch selectedPatch;
	ArrayList<Patch> allPatches = new ArrayList<Patch>();
	
	public PatchGrid(BufferedImage i){
		img = i;
		allPatches = new ArrayList<Patch>();
		patches = new Patch[img.getWidth() / WINDOW_SIZE][img.getHeight() / WINDOW_SIZE];		
		for(int y = 0; y + WINDOW_SIZE < img.getHeight(); y+=WINDOW_SIZE){
			//ArrayList<Patch> currentRow = new ArrayList<Patch>();
			Patch previousPatch = null;
			int patchY = (int) y / WINDOW_SIZE;
			for(int x = 0; x + WINDOW_SIZE < img.getWidth(); x+=WINDOW_SIZE){
				int patchX = (int) x / WINDOW_SIZE;
				
			    BufferedImage subImage = img.getSubimage(x, y, (int) WINDOW_SIZE, (int) WINDOW_SIZE);
			    Patch p = new Patch(subImage, x, y);
			    
			    if(previousPatch != null){
			    	//PatchConnection newCon = new PatchConnection(previousPatch, p, 0.9);
//			    	previousPatch.nextStates.add(newCon);
			    }
			    previousPatch = p;
			    allPatches.add(p);
			    patches[patchX][patchY] = p;
			}
		}		
	}
	
	public BufferedImage getImage(){
		return img;
		//return getRenderedImage();
	}
	
	public BufferedImage getRenderedImage(){
		BufferedImage imgBuffer = new BufferedImage(img.getWidth(), img.getHeight(),
		                    BufferedImage.TYPE_INT_ARGB);
		Graphics g = imgBuffer.createGraphics();
		
		for(int x = 0; x < patches.length; x++){
			for(int y = 0; y < patches[0].length; y++){
				if(patches[x][y] != null){
					g.drawImage(patches[x][y].pixels, x * WINDOW_SIZE, y*WINDOW_SIZE, null);					
				}else{
					//System.out.println("Render at " + x + " , " + y + " is null");
				}
			}
		}
		return imgBuffer;
	}
	
	public Patch getPatchAt(int rawX, int rawY){
		int lineIndex = (int) Math.ceil( rawY  / PatchGrid.WINDOW_SIZE );
		int subIndex = (int) (rawX / WINDOW_SIZE);		
		return patches[subIndex][lineIndex];
	}
	
	public Patch findPatch(int patchX, int patchY, int deapth){
		if(patches[patchX][patchY] != null && !patches[patchX][patchY].isMasked){
			return patches[patchX][patchY];
		}
		
		// What do each of the patches surrounding us suggest?
		if(deapth > 0 && patchX >= 0 && patchX < patches.length && patchY >= 0 && patchY < patches[0].length){
			ArrayList<Patch> leftSuggestions = new ArrayList<Patch>();
			ArrayList<Patch> rightSuggestions = new ArrayList<Patch>();
			ArrayList<Patch> topSuggestions = new ArrayList<Patch>();
			ArrayList<Patch> bottomSuggestions = new ArrayList<Patch>();
			if(patchX - 1 >= 0){
				leftSuggestions = findPatch(patchX - 1, patchY, deapth - 1).getNextRight(allPatches);
			}
				
			if(patchX + 1 < patches.length){ 
				rightSuggestions = findPatch(patchX + 1, patchY, deapth - 1).getNextLeft(allPatches);
			}
			
			if(patchY -1 >= 0){
				topSuggestions = findPatch(patchX, patchY - 1, deapth - 1).getNextBelow(allPatches);				
			}
			
			if(patchY + 1 < patches[0].length){
				bottomSuggestions = findPatch(patchX, patchY + 1, deapth - 1).getNextAbove(allPatches);	
			}
			
			Patch left = null;
			Patch right = null;
			Patch top = null;
			Patch bottom = null;
			
			//ArrayList<Patch> bestCandidates = new ArrayList<Patch>();
			if(leftSuggestions.size() > 0){
				left = leftSuggestions.get(leftSuggestions.size() - 1);
				if(left.isMasked){
					left = null;
				}
			}
			if(rightSuggestions.size() > 0){
				right = rightSuggestions.get(rightSuggestions.size() - 1);
				if(right.isMasked){
					right = null;
				}
			}
			if(topSuggestions.size() > 0){
				top = topSuggestions.get(topSuggestions.size() - 1);
			}
			if(bottomSuggestions.size() > 0){
				bottom = bottomSuggestions.get(bottomSuggestions.size() - 1);	
			}
			
			BufferedImage mergedPatch = new BufferedImage(WINDOW_SIZE, WINDOW_SIZE, BufferedImage.TYPE_INT_ARGB);
			
			// Add weighted componenets to improve the average...
			if(left != null || right != null || top != null || bottom != null){
				for(int x = 0; x < WINDOW_SIZE; x++){
					for(int y = 0; y < WINDOW_SIZE; y++){
						int pixR = 0;
						int pixG = 0;
						int pixB = 0;
						double totalProb = 0.0;
						if(left != null){
							Color c = new Color(left.pixels.getRGB(x, y));
							// x = 0 should be 1. x = WINDOW_SIZE = 0. 
							double leftWeight = 1.0 - (x / (double) WINDOW_SIZE);	// left most pixel =
//							System.out.println("Left weight " + leftWeight);
							leftWeight *= left.probability;
							totalProb += leftWeight;
							
							pixR += leftWeight * c.getRed();
							pixG += leftWeight * c.getGreen();
							pixB += leftWeight * c.getBlue();							
						}
						
						if(right != null){
							Color c = new Color(right.pixels.getRGB(x, y));

							double rightWeight = (x / (double) WINDOW_SIZE);	// left most pixel =
//							System.out.println("right weight " + rightWeight);							
							rightWeight *= right.probability;
							totalProb += rightWeight;
							
							pixR += rightWeight * c.getRed();
							pixG += rightWeight * c.getGreen();
							pixB += rightWeight * c.getBlue();							
						}
						
						if(top != null){
							Color c = new Color(top.pixels.getRGB(x, y));
							// y = 0 should have weight = 1; 
							double topWeight = 1.0 - (y / (double) WINDOW_SIZE);	// left most pixel =
//							System.out.println("top weight " + topWeight);							
							topWeight *= top.probability;
							totalProb += topWeight;
							
							pixR += topWeight * c.getRed();
							pixG += topWeight * c.getGreen();
							pixB += topWeight * c.getBlue();							
						}						

						if(bottom != null){
							Color c = new Color(bottom.pixels.getRGB(x, y));
							// y = 0 should have weight = 1; 
							double bottomWeight = (y / (double) WINDOW_SIZE);	// left most pixel =
//							System.out.println("bottom weight " + bottomWeight);
							bottomWeight *= bottom.probability;
							totalProb += bottomWeight;
							
							pixR += bottomWeight * c.getRed();
							pixG += bottomWeight * c.getGreen();
							pixB += bottomWeight * c.getBlue();							
						}						
												
						
						mergedPatch.setRGB(x, y, new Color((int) (pixR / totalProb), (int) (pixG / totalProb), (int) (pixB / totalProb)).getRGB());
					}
				}

			}
			
			
			double newProb = 0.0;
			double partialProb = 0.0;
			double totalProb = 0.0;
			double divider = 0;
			if(left != null){
				partialProb += 0.25 * left.probability;
				totalProb += left.probability;
				divider+= 1;
			}
			if(right!= null){
				partialProb += 0.25 * right.probability;
				totalProb += right.probability;
				divider+= 1;
			}
			if(top != null){
				partialProb += 0.25 * top.probability;
				totalProb += top.probability;
				divider+= 1;
			}
			if(bottom != null){
				partialProb += 0.25 * bottom.probability;
				totalProb += bottom.probability;
				divider+= 1;
			}
			
			if(divider != 0){
				//newProb = totalProb / divider;
				//newProb = partialProb;
				newProb = (totalProb + partialProb) / (divider * 2);
			}else{
				newProb = 0;
			}

			
			Patch newPatch = new Patch(mergedPatch, patchX, patchY);
			newPatch.probability = newProb;
			
//			System.out.println("New prob is " + newPatch.probability);
			return newPatch;
		}
		else{
			if(patchX >= 0 && patchX < patches.length && patchY >= 0 && patchY < patches[0].length){
				Patch p = new Patch(patches[patchX][patchY]);
				p.probability = 0.0;
				return p;
			}
			else{
				return null;
			}
		}
	}
	
	public void setSelectedPatch(Patch p){
		selectedPatch = p;
		Patch[] selList = {};
		PatchDisplay.updateSelection(selectedPatch, selList);
	}
	
	public Patch getSelectedPatch(){
		return selectedPatch;
	}
	
	// Blanks out the patches at given locations
	public void applyMask(PatchGrid maskGrid){
		allPatches = new ArrayList<Patch>();
		for(int x = 0; x < maskGrid.patches.length; x++){
			for(int y = 0; y < maskGrid.patches[0].length; y++){
				Patch p = maskGrid.patches[x][y];
				if(p != null){
//					System.out.println("is non null at " + x + " ,  " + y + " = " + p.brightness );
					if(p.brightness > 0){
						patches[x][y].pixels = maskGrid.patches[x][y].pixels;
						patches[x][y].isMasked = true;
					}else{
						allPatches.add(patches[x][y]);						
					}
				}
			}
		}
	}
	
	public void fillMask(PatchGrid maskGrid){
		boolean[][] maskGridPattern = new boolean[maskGrid.patches.length][maskGrid.patches[0].length]; 
		for(int x = 0; x < maskGrid.patches.length; x++){
			for(int y = 0; y < maskGrid.patches[0].length; y++){
				Patch p = maskGrid.patches[x][y];
				if(p != null && p.brightness > 0){	// It's a patch to be filled
					maskGridPattern[x][y] = true;
				}else{
					maskGridPattern[x][y] = false;
				}
			}
		}
		
		// TODO: loop until steady state;
		boolean moreTODO = true;
		while(moreTODO){
			
			ArrayList<Patch> candidates = new ArrayList<Patch>();
			moreTODO = false;
			for(int x = 0; x < maskGridPattern.length; x++){
				for(int y = 0; y < maskGridPattern[0].length; y++){
					if(maskGridPattern[x][y]){	// It's a patch to be filled
						candidates.add(findPatch(x, y, 1));
						moreTODO = true;
					}
				}
			}
			
			if(candidates.size() > 0){
				double bestCandidateProb = 0.0;				
				Patch bestPatch = candidates.get(0);
				for(Patch p : candidates){
					if(p.probability > bestCandidateProb){
						bestCandidateProb = p.probability;
						bestPatch = p;
					}
				}
				
				System.out.println("Best probability is " + bestCandidateProb);
				maskGridPattern[bestPatch.patchX][bestPatch.patchY] = false;
				patches[bestPatch.patchX][bestPatch.patchY] = bestPatch;
				
			}
			
		}
		
	}
	
}

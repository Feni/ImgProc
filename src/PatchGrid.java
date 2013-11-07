import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;


public class PatchGrid {
	BufferedImage img;
	public static final int WINDOW_SIZE = 10;
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
			    	PatchConnection newCon = new PatchConnection(previousPatch, p, 0.9);
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
	
	public BufferedImage getSemiImg(BufferedImage i, float transparency){
		BufferedImage cloned = new BufferedImage(i.getWidth(), i.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) cloned.createGraphics();
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency);
		g.setComposite(ac);
		g.drawImage(i, 0, 0, null);
		return cloned;
	}
	
	public Patch findPatch(int patchX, int patchY, int deapth){
		if(patches[patchX][patchY] != null && !patches[patchX][patchY].isMasked){
			return patches[patchX][patchY];
		}
		
		// What do each of the patches surrounding us suggest?
		if(deapth > 0){
			ArrayList<Patch> leftSuggestions = findPatch(patchX - 1, patchY, deapth - 1).getNextRight(allPatches);
			ArrayList<Patch> rightSuggestions = findPatch(patchX + 1, patchY, deapth - 1).getNextLeft(allPatches);
			ArrayList<Patch> topSuggestions = findPatch(patchX, patchY - 1, deapth - 1).getNextBelow(allPatches);
			ArrayList<Patch> bottomSuggestions = findPatch(patchX, patchY + 1, deapth - 1).getNextAbove(allPatches);
			
			Patch left = null;
			Patch right = null;
			Patch top = null;
			Patch bottom = null;
			
			ArrayList<Patch> bestCandidates = new ArrayList<Patch>();
			if(leftSuggestions.size() > 0){
				left = leftSuggestions.get(leftSuggestions.size() - 1);
				bestCandidates.add(left);
			}
			if(rightSuggestions.size() > 0){
				right = rightSuggestions.get(rightSuggestions.size() - 1);
				bestCandidates.add(right);
			}
			if(topSuggestions.size() > 0){
				top = topSuggestions.get(topSuggestions.size() - 1);
				bestCandidates.add(top);
			}
			if(bottomSuggestions.size() > 0){
				bottom = bottomSuggestions.get(bottomSuggestions.size() - 1);
				bestCandidates.add(bottom);	
			}
			
			BufferedImage mergedPatch = new BufferedImage(WINDOW_SIZE, WINDOW_SIZE, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) mergedPatch.createGraphics();
			float transparency = (float) 1.0/bestCandidates.size();
			
			
			// Add weighted componenets to improve the average...
			if(left != null){
				for(int x = 0; x < WINDOW_SIZE; x++){
					for(int y = 0; y < WINDOW_SIZE; y++){
						int pixR = 0;
						int pixG = 0;
						int pixB = 0;
						double totalProb = 0.0;
						if(left != null){
							Color c = new Color(left.pixels.getRGB(x, y));
							// x = 0 should be 1. x = WINDOW_SIZE = 0. 
							double leftWeight = 1 - (x / (double) WINDOW_SIZE);	// left most pixel =
							leftWeight *= left.probability;
							totalProb += leftWeight;
							
							pixR += leftWeight * c.getRed();
							pixG += leftWeight * c.getGreen();
							pixB += leftWeight * c.getBlue();							
						}
						
						if(right != null){
							Color c = new Color(right.pixels.getRGB(x, y));

							double rightWeight = (x / (double) WINDOW_SIZE);	// left most pixel =
							rightWeight *= right.probability;
							totalProb += rightWeight;
							
							pixR += rightWeight * c.getRed();
							pixG += rightWeight * c.getGreen();
							pixB += rightWeight * c.getBlue();							
						}
						
						if(top != null){
							Color c = new Color(top.pixels.getRGB(x, y));
							// y = 0 should have weight = 1; 
							double topWeight = 1 - (y / (double) WINDOW_SIZE);	// left most pixel =
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
			for(Patch p : bestCandidates){
			//	g.drawImage(getSemiImg(p.pixels, (float) transparency), 0, 0, null);
				newProb += 0.25 * p.probability;
			}
			
			Patch newPatch = new Patch(mergedPatch, patchX, patchY);
			newPatch.probability = newProb;
			
//			System.out.println("New prob is " + newPatch.probability);
			return newPatch;
		}
		else{
			Patch p = new Patch(patches[patchX][patchY]);
			p.probability = 0.0;
			return p;
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
		
		for(int i = 0; i < 30; i++){
			ArrayList<Patch> candidates = new ArrayList<Patch>();

			for(int x = 0; x < maskGridPattern.length; x++){
				for(int y = 0; y < maskGridPattern[0].length; y++){
					if(maskGridPattern[x][y]){	// It's a patch to be filled
						candidates.add(findPatch(x, y, 1));
					}
				}
			}
			
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
	
	public boolean[][] fillTL(boolean[][] maskGrid){
		for(int x = 0; x < maskGrid.length; x++){
			for(int y = 0; y < maskGrid[0].length; y++){
				if(maskGrid[x][y]){	// It's a patch to be filled
					patches[x][y] = findPatch(x, y, 1);
					maskGrid[x][y] = false;
					return maskGrid;
				}
			}
		}
		return maskGrid;
	}
	
	public boolean[][] fillTR(boolean[][] maskGrid){
		for(int x = maskGrid.length - 1; x >= 0; x--){
			for(int y = 0; y < maskGrid[0].length; y++){
				if(maskGrid[x][y]){	// It's a patch to be filled
					patches[x][y] = findPatch(x, y, 1);
					maskGrid[x][y] = false;
					return maskGrid;
				}
			}
		}
		return maskGrid;
	}
	
	public boolean[][] fillBL(boolean[][] maskGrid){
		for(int y = 0; y < maskGrid[0].length; y++){
			for(int x = maskGrid.length - 1; x >= 0; x--){
				if(maskGrid[x][y]){	// It's a patch to be filled
					patches[x][y] = findPatch(x, y, 1);
					maskGrid[x][y] = false;
					return maskGrid;
				}
			}
		}
		return maskGrid;
	}
	
	public boolean[][] fillBR(boolean[][] maskGrid){
		for(int y = maskGrid[0].length - 1; y >= 0 ; y--){
			for(int x = maskGrid.length - 1; x >= 0; x--){
				if(maskGrid[x][y]){	// It's a patch to be filled
					patches[x][y] = findPatch(x, y, 1);
					maskGrid[x][y] = false;
					return maskGrid;
				}
			}
		}
		return maskGrid;
	}	
}

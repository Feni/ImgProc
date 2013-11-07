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
			    Patch p = new Patch(subImage);
			    
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
			ArrayList<Patch> leftSuggestion = findPatch(patchX - 1, patchY, deapth - 1).getNextRight(allPatches);
			ArrayList<Patch> rightSuggestion = findPatch(patchX + 1, patchY, deapth - 1).getNextLeft(allPatches);
			ArrayList<Patch> topSuggestion = findPatch(patchX, patchY - 1, deapth - 1).getNextBelow(allPatches);
			ArrayList<Patch> bottomSuggestion = findPatch(patchX, patchY + 1, deapth - 1).getNextAbove(allPatches);
			
			// Return the one with the most probability
			/*double maxProb = 0.0;
			Patch maxPatch = patches[patchX][patchY];
			for(Patch p : leftSuggestion){
				if(p.probability > maxProb){
					maxProb = p.probability;
					maxPatch = p;
				}
			}
			
			for(Patch p : rightSuggestion){
				if(p.probability > maxProb){
					maxProb = p.probability;
					maxPatch = p;
				}
			}
			for(Patch p : topSuggestion){
				if(p.probability > maxProb){
					maxProb = p.probability;
					maxPatch = p;
				}
			}
			for(Patch p : bottomSuggestion){
				if(p.probability > maxProb){
					maxProb = p.probability;
					maxPatch = p;
				}
			}*/
			ArrayList<Patch> bestCandidates = new ArrayList<Patch>();
			if(leftSuggestion.size() > 0)
				bestCandidates.add(leftSuggestion.get(leftSuggestion.size() - 1));
			if(rightSuggestion.size() > 0)
				bestCandidates.add(rightSuggestion.get(rightSuggestion.size() - 1));
			if(topSuggestion.size() > 0)
				bestCandidates.add(topSuggestion.get(topSuggestion.size() - 1));
			if(bottomSuggestion.size() > 0)
				bestCandidates.add(bottomSuggestion.get(bottomSuggestion.size() - 1));			
			
			BufferedImage mergedPatch = new BufferedImage(WINDOW_SIZE, WINDOW_SIZE, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) mergedPatch.createGraphics();
			float transparency = (float) 1.0/bestCandidates.size();
			double newProb = 1.0;
			for(int x = 0; x < WINDOW_SIZE; x++){
				for(int y = 0; y < WINDOW_SIZE; y++){
					int pixR = 0;
					int pixG = 0;
					int pixB = 0;
					
					double totalProb = 0;
					
					for(Patch p : bestCandidates){
						Color c = new Color(p.pixels.getRGB(x, y));
						
						pixR += p.probability * c.getRed();
						pixG += p.probability * c.getGreen();
						pixB += p.probability * c.getBlue();
						
						totalProb += p.probability;
					}
					
					mergedPatch.setRGB(x, y, new Color((int) (pixR / totalProb), (int) (pixG / totalProb), (int) (pixB / totalProb)).getRGB());
					
				}
			}
			
			/*for(Patch p : bestCandidates){
				g.drawImage(getSemiImg(p.pixels, (float) transparency), 0, 0, null);
				newProb *= p.probability;
			}*/
			
			Patch newPatch = new Patch(mergedPatch);
			newPatch.probability = newProb;
			return newPatch;
		}
		else{
			return patches[patchX][patchY];
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
		
		for(int i = 0; i < 20; i++){
			maskGridPattern = fillTL(maskGridPattern);
			maskGridPattern = fillTR(maskGridPattern);
			maskGridPattern = fillBL(maskGridPattern);
			maskGridPattern = fillBR(maskGridPattern);			
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

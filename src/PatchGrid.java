import java.awt.Graphics;
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
		for(int x = 0; x < maskGrid.patches.length; x++){
			for(int y = 0; y < maskGrid.patches[0].length; y++){
				Patch p = maskGrid.patches[x][y];
				if(p != null && p.brightness > 0){	// It's a patch to be filled
					ArrayList<Patch> candidates = patches[x-1][y].getNextRight(allPatches);
					if(candidates.size() > 0){
						patches[x][y] = candidates.get(candidates.size() - 1);						
					}
				}
			}
		}
	}
}

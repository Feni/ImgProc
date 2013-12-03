import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

// TODO: Weight the picks so that closer patches are given more preference.
// TODO: Detect gradient directions from multiple nearby patches and extend it. 
// This can be used to create a patch from scratch basically rather than selecting one. 
// Or figure out the next state for the patch given other GRADIENTS of similar properties
// and then apply that operation to this patch.
// So a dark patch of wall getting darker can be used to predict how a light patch of wall 
// also getting darker will progress.
// May need second degree differences to accurately predict this. 
// k-means cluster during the initial round 
// so minimize search space after that.
// DFT is't good for this but DCT's one extended periodic repeating is a useful property...
public class MarkovPainter{
	
	public static void main(String args[]){
		   try{
			   //BufferedImage rawImg = ImageIO.read(new File("/hd/Dropbox/workspace/MarkovPainter/sand.jpg"));
			   //BufferedImage rawImg = ImageIO.read(new File("/hd/Dropbox/workspace/MarkovPainter/sandMed.jpg"));
			   BufferedImage rawImg = ImageIO.read(new File("/hd/Dropbox/workspace/MarkovPainter/Lincoln_memorial_scaled.jpg"));
			   PatchGrid imgGrid = new PatchGrid(rawImg);
			   
			   //BufferedImage maskImg = ImageIO.read(new File("/hd/Dropbox/workspace/MarkovPainter/sandMask.png"));
			   //BufferedImage maskImg = ImageIO.read(new File("/hd/Dropbox/workspace/MarkovPainter/sandMaskMed.png"));
			   //BufferedImage maskImg = ImageIO.read(new File("/hd/Dropbox/workspace/MarkovPainter/Lincoln_memorial_mask2.jpg"));
			   BufferedImage maskImg = ImageIO.read(new File("/hd/Dropbox/workspace/MarkovPainter/Lincoln_memorial_mask.jpg"));
			   PatchGrid maskGrid = new PatchGrid(maskImg);
			   
			   imgGrid.initMask(maskGrid);
			   
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

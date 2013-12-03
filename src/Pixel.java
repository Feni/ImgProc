import java.awt.Color;
import java.util.ArrayList;


public abstract class Pixel {
	Color c;
	public abstract boolean isKnown();
	ArrayList<CorrespondencePatch> correspondences = new ArrayList<CorrespondencePatch>();
}

class KnownPixel extends Pixel{
	public KnownPixel(Color clr){
		c = clr;
	}
	
	public boolean isKnown(){
		return true;
	}
}

class UnknownPixel extends Pixel{
	double confidence;
	public UnknownPixel(Color clr, double conf){
		c = clr;
		confidence = conf;
	}
	
	public UnknownPixel(){
		c = null;
		confidence = 0.0;
	}
	
	public boolean isKnown(){
		return false;
	}
}

class CorrespondencePatch{
	PatchGrid grid;
	int imageX;	// Where the patch is centered at. 
	int imageY;
	int size = 3;	// width and height of this square patch
	
	public CorrespondencePatch(PatchGrid g, int iX, int iY){
		grid = g;
		imageX = iX;
		imageY = iY;
	}
	
	public int getDiffence(CorrespondencePatch other){
		// TODO : Implement by differencing the pixels of this with other. 
		return -1;
	}
}
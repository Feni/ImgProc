import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class PatchSelector implements MouseListener{
	PatchGrid imgGrid;
	
	public PatchSelector(PatchGrid grid){
		imgGrid = grid;
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
		Patch selectedPatch = imgGrid.getPatchAt(x, y);
		imgGrid.setSelectedPatch(selectedPatch);
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {	}
	@Override
	public void mousePressed(MouseEvent arg0) {}
	@Override
	public void mouseReleased(MouseEvent arg0) {}
}
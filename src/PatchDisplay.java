import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class PatchDisplay extends JFrame implements Runnable{
    static ImageIcon selectedIcon;
    static JLabel selectedIconDisplay;
    static JLabel originalImg; 
    static JList<ImageIcon> nextStates;
    static JPanel controlPanel;
    static PatchGrid imgGrid;
	
	public PatchDisplay(PatchGrid grid){
    	super();
    	imgGrid = grid;
 	   	setSize( 1024, 650 );
 	   	setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
 	   	
 	   	JPanel panel = new JPanel();
 	   	
 	   	JPanel imgPanel = new JPanel();
 	   	controlPanel = new JPanel();
 	   	
 	   	originalImg = new JLabel(new ImageIcon(imgGrid.getRenderedImage()));
 	   	originalImg.addMouseListener(new PatchSelector(imgGrid));
 	   	
 	   	selectedIcon = new ImageIcon(imgGrid.getImage().getScaledInstance(200, 100, 0));
 	  	selectedIconDisplay = new JLabel(selectedIcon);
 	  	selectedIconDisplay.setSize(200, 100);
 	  	
        DefaultListModel<ImageIcon> listModel = new DefaultListModel<ImageIcon>();
        listModel.add(0, selectedIcon);
        listModel.add(1, selectedIcon);
        listModel.add(2, selectedIcon);
        //listModel.add(1, new ImageIcon(activePatch));
        nextStates =new JList<ImageIcon>(listModel);
        nextStates.setVisibleRowCount(1);
        nextStates.setMinimumSize(new Dimension(100, 600));
        nextStates.setMaximumSize(new Dimension(100, 600));        
        
        JScrollPane imagesListPane = new JScrollPane(nextStates);
        
        //imagesListPane.setSize(120, 800);
        //imagesListPane.setPreferredSize(new Dimension(220, 500));
        
        
        //imagesListPane.setBounds(0, 0, 120, 500);

        imgPanel.add(originalImg);
        controlPanel.setLayout(new BorderLayout());
        controlPanel.add(selectedIconDisplay, BorderLayout.NORTH);
 	  	controlPanel.add(imagesListPane, BorderLayout.CENTER);
 	  	
 	  	controlPanel.setPreferredSize(new Dimension(220, 600));
 	  	
 	  	panel.add(imgPanel);
 	  	panel.add(controlPanel);
 	  	
 	  	panel.setVisible(true);
 	  	
 	  	//this.setContentPane(panel);
 	  	this.add(panel);
 	  	
 	  	this.pack();
 	  	setVisible(true);
 	  	new Thread(this).start();
	}
	
	/*
	public static void updateSelection(Patch newSelected, Patch[] patchList){
		if(selectedIcon != null && controlPanel != null){
			selectedIcon.setImage(newSelected.pixels.getScaledInstance(50, 50, 0));
			
			System.out.println("Updating next states");
			ArrayList<Patch> nextStatesComputed = newSelected.getNextRight(imgGrid.allPatches);
			
			System.out.println("Next states is " + nextStatesComputed);
			
			ArrayList<ImageIcon> nsIc = new ArrayList<ImageIcon>();
			DefaultListModel<ImageIcon> listModel = new DefaultListModel<ImageIcon>();
			int i = 0;
			for(Patch p : nextStatesComputed){
				//nsIc.add(new ImageIcon(p.pixels));
				listModel.add(i, new ImageIcon(p.pixels.getScaledInstance(50, 50, 0)));
				i++;
			}
			nextStates.setModel(listModel);

			selectedIconDisplay.repaint();
		}
	} */
	
	public void updateImage(){
		originalImg.setIcon(new ImageIcon(imgGrid.getRenderedImage()));
	}
	
	// Refresh the display every second. 
	public void run(){
		while(true){
			updateImage();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}


import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

/*
 * SeedGenerator
 * class wich provides methods to calculate seeds for region growing
 */ 
public class SeedGenerator implements MouseListener{

	private ImageProcessor ip;
	private LinkedList<Coord2D> seeds = new LinkedList<Coord2D>();
	private boolean manual;


	private ImageCanvas canvas;
	private int nClick;
	
	private int nSeeds;
	private double imSTDV;
	
	/**
	 * @param ip target {@code ImageProcessor}
	 */
	public SeedGenerator(ImageProcessor ip) {
		this.ip = ip;
		manual = false;
		nClick = 0;
	}



	/**
	 * @return seeds {@code LinkedList}
	 */
	public LinkedList getSeeds() {
		return seeds;
	}

	/**
	 * @param imp target {@code ImagePlus}
	 * @param nSeeds2 number of seeds
	 */
	public void selectSeedsManually(ImagePlus imp, int nSeeds2) {
		// manual mode
		manual = true;
		
		// setting seeds number
		this.nSeeds = nSeeds2;
		
		
		// inizializzo la lista dei semi
		this.seeds = new LinkedList<Coord2D>();
		
		// resets click number
		nClick = 0;
		
		// ottengo il canvas e gli aggiungo un MouseListener
        ImageWindow win = imp.getWindow();
        ImageStatistics ims = imp.getStatistics();
        this.imSTDV = ims.stdDev;
        canvas = win.getCanvas();
        canvas.addMouseListener(this);		
	}

	/**
	 * @param e 
	 */
	public void mouseClicked(MouseEvent e) {
		nClick++;
		if (manual && nClick==nSeeds) {

//			IJ.write("Mouse clicked: ultimo seme");

			int x = e.getX();
			int y = e.getY();
			//memorizzo le coordinate del punnto relative rispetto al canvas
			int X = canvas.offScreenX(x);
			int Y = canvas.offScreenY(y);

			this.seeds.add(new Coord2D(X,Y));
//			initializing seed grower

			SeedsGrower sgr = new SeedsGrower(seeds,ip);

			// starting growth
			sgr.growSeeds();

			// showing output
			sgr.showOutput();
		}

		else if (manual && (nClick<nSeeds)) {
//			IJ.write("Mouse clicked: seme "+nClick);
			int x = e.getX();
			int y = e.getY();
			//memorizzo le coordinate del punto relative rispetto al canvas
			int X = canvas.offScreenX(x);
			int Y = canvas.offScreenY(y);

			Coord2D point = new Coord2D(X, Y);
			this.seeds.add(point);	        
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 * method that manages mouse click on the image by user collecting coordinates and using
	 * them to start the region growing algorithm
	 */
	public void mouseEntered(MouseEvent e) {
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 * not implemented
	 */
	public void mouseExited(MouseEvent e) {
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 * not implemented
	 */
	public void mousePressed(MouseEvent e) {
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 * not implemented
	 */
	public void mouseReleased(MouseEvent e) {
		
	}

}

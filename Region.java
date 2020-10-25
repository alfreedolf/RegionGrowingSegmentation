import java.util.LinkedList;
import java.util.ListIterator;


import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.ShortProcessor;


/**
 * @author Alfonso Ridolfo
 * @version 0.1
 * This class provides methods to create and grow a region on the specified {@code ImageProcessor} 
 */
public class Region {

	private ImageProcessor ip;
	private boolean isEmpty;
	private boolean[][] enqueued;
	private LinkedList<Coord2D> store;
	private int w;
	private int h;
	private float[] gradMap;
	private ImagePlus gradIMP;
	private ImageStatistics gradStats;
	private int[] gradHist;
	private float gradThreshold;
	private float[] pixValues;
	private float mean;
	private float standardDev;
	private float graylevelThreshold;
	private Coord2D seed;
	private float[] meanRGB;
	private float[] standardDevRGB;
	private int[] colorThreshold;
	private ColorProcessor colProc;
	private double rangeThreshold;
//	private double imSTDV;
	private float edgePixMean;
	
	
	/**
	 * @param IP ImageProcessor of the region
	 * @param plus 
	 * @param fs 
	 * @param stdV 
	 * @param f 
	 */
	public Region(ImageProcessor IP, float[] fs, ImagePlus plus, double gradThresh, float epm) {
		
		// checking image type and initializing necessary data
		if (IP instanceof ByteProcessor || IP instanceof ShortProcessor)
			this.ip = (FloatProcessor)IP.convertToFloat();
		else {
			this.ip = IP;
			if (ip instanceof ColorProcessor){
				colProc = (ColorProcessor) ip;
				this.meanRGB = new float[3];
				this.standardDevRGB = new float[3];
				this.colorThreshold = new int[3];
			}
		}
		
		// getting image size
		this.w = ip.getWidth();
		this.h = ip.getHeight();
//		this.imSTDV = stdV;
		this.gradIMP = plus;
		this.gradMap = (float[]) plus.getProcessor().getPixelsCopy();
		this.gradThreshold = (float) gradThresh;
		this.edgePixMean = epm;
		
		pixValues = new float[w*h];
		int offset, l;
		
		
		// initializing a luminance/value map of the image
		for (int y=0; y<h; y++) {
			for (int x=0; x<w; x++) {
				offset = y*w;
				l = x+offset;
				this.pixValues[l] = ip.getPixelValue(x, y);
			}
		}
		

		
		// initializing a data store for the region points
		this.store = new LinkedList<Coord2D>();
		
		
		// initializing a boolean array wich keep track of points to be enqued for membership test
		enqueued = new boolean[ip.getHeight()][ip.getWidth()];
		for (int i=0; i<h; i++) {
			for (int j=0; j<w; j++)
				enqueued[i][j] = false;
		}
		
		
		isEmpty = true;
		
	}
	

	


	/**
	 * method to compute gray level threshold needed to the membership test if the image is a graylevel map
	 */
	private void glThresholdCalc() {
		int x = (int) this.seed.X();
		int y = (int) this.seed.Y();
		int offset = y*w;
		int l = x + offset;
		this.graylevelThreshold = pixValues[l];
	}

	/**
	 * method to compute color thresholds needed to the membership test if the image is a RGB color map
	 */
	private void colorThresholdCalc() {
		this.colorThreshold[0] = colProc.getColor((int)this.seed.X(), (int)this.seed.Y()).getRed();
		this.colorThreshold[1] = colProc.getColor((int)this.seed.X(), (int)this.seed.Y()).getGreen();
		this.colorThreshold[2] = colProc.getColor((int)this.seed.X(), (int)this.seed.Y()).getBlue();		
	}

	/**
	 * method that implements the growing process of the region
	 * @param pixSeed starting seed point of the region growing process
	 */
	public void grow(Coord2D pixSeed) {
		myQueue queue = new myQueue();
		int x = 0;
		int y = 0;
		this.seed = pixSeed;
		Coord2D currPix;

		queue.enqueue(pixSeed);
		
			while (!queue.empty()) {
				currPix = (Coord2D) queue.dequeue();
				x = (int) Math.round(currPix.X());
				y = (int) Math.round(currPix.Y());
				int xT, yT;
				this.enqueued[y][x] = true;
				if (this.test(x, y)) {
					this.addMember(x, y);
					try {
					if (x > 0) {
						xT = x-1;
						if (!enqueued[y][xT]) {
							queue.enqueue(new Coord2D(xT, y));
							enqueued[y][xT] = true;
						}
						yT = y-1;
						if (y > 0)
							if (!enqueued[yT][xT]) {
							queue.enqueue(new Coord2D(xT, yT));
							enqueued[yT][xT] = true;
						}
						yT = y+1;
						if (y < (h-1))
							if(!enqueued[yT][xT]) {
							queue.enqueue(new Coord2D(xT, yT));
							enqueued[yT][xT] = true;
						}
					}

					
					if (x < (w-1)) {
						xT = x+1;
						if (!enqueued[y][xT]) {
							queue.enqueue(new Coord2D(xT, y));
							enqueued[y][xT] = true;
						}
						yT = y-1;
						if (y > 0)
							if(!enqueued[yT][xT]) {
							queue.enqueue(new Coord2D(xT, yT));
							enqueued[yT][xT] = true;
						}
						yT = y+1;
						if (y < (h-1)) 
							if(!enqueued[yT][xT]) {
							queue.enqueue(new Coord2D(xT, yT));
							enqueued[yT][xT] = true;
						}
					}
					

					yT = y-1;
					if (y > 0) 
						if (!enqueued[yT][x]) {
						queue.enqueue(new Coord2D(x, yT));
						enqueued[yT][x] = true;
					}
					yT = y+1;
					if (y < (h-1))
						if(!enqueued[yT][x]) {
						queue.enqueue(new Coord2D(x, yT));
						enqueued[yT][x] = true;
					}
					} catch (IndexOutOfBoundsException e) {
						IJ.write("sei andato fuori dai margini dell'immagine! x ="+x+"y ="+y);
						e.printStackTrace();
						e.getCause();
					}		
				}
					
			}
		

	}


	/**
	 * method that adds a specified point to the region
	 * @param x abscissa value of the point to be add
	 * @param y ordinate value of the point to be add
	 */
	private void addMember(int x, int y) {
		if (this.isEmpty) {
			this.seed = new Coord2D(x, y);
			this.store = new LinkedList<Coord2D>();
			this.store.add(new Coord2D(x, y));
			if (!(ip instanceof ColorProcessor))
				this.glThresholdCalc();
			else
				this.colorThresholdCalc();
			
			this.isEmpty = false;
			
			this.meanCalc();
			this.stdCalc();
		}
		else {
			this.store.add(new Coord2D(x, y));
			this.meanCalc();
			this.stdCalc();
		}		
	}


	/**
	 * method that computes the mean value(s) for the region
	 */
	private void meanCalc(){		
		ListIterator li = this.store.listIterator();
		float dim = (float) this.store.size();
		int x, y;
		Coord2D next;		
		if (ip instanceof ColorProcessor) {
			float sumR = 0, sumG = 0, sumB = 0;
			while (li.hasNext()) {
				next = (Coord2D) li.next();
				x = (int) next.X();
				y = (int) next.Y();
				sumR += colProc.getColor(x, y).getRed();
				sumG += colProc.getColor(x, y).getGreen();
				sumB += colProc.getColor(x, y).getBlue();
			}
			this.meanRGB[0] = (sumR/dim);
			this.meanRGB[1] = (sumG/dim);
			this.meanRGB[2] = (sumB/dim);
		}
		
		
		else {
			int offset, l;
			float sum = 0;
			while (li.hasNext()) {
				next = (Coord2D) li.next();
				x = (int) next.X();
				y = (int) next.Y();
				offset = y*w;
				l = x+offset;
				sum += pixValues[l];			
			}
			this.mean = (sum/dim);
		}				
	}

	/**
	 * method that computes standard deviation of the region
	 */
	private void stdCalc() {
		ListIterator li = store.listIterator();
		Coord2D next;
		int x, y;
		float dim = (float) this.store.size();
		
				if (!(ip instanceof ColorProcessor)) {
					int offset, l;
					float sum = 0, dev;
					while (li.hasNext()) {
						next = (Coord2D) li.next();
						x = (int) next.X();
						y = (int) next.Y();
						offset = y * w;
						l = x + offset;
						dev = (float) Math.pow((pixValues[l] - mean), 2);
						sum += dev;
					}
					this.standardDev = (float) (Math.sqrt(sum / dim));
				}
				else {
					float sum = 0;
					while (li.hasNext()) {
						next = (Coord2D) li.next();
						x = (int) next.X();
						y = (int) next.Y();
						sum += Math.pow(colProc.getColor(x, y).getRed()-meanRGB[0], 2)+Math.pow(colProc.getColor(x, y).getGreen()-meanRGB[1], 2)+Math.pow(colProc.getColor(x, y).getBlue()-meanRGB[2], 2);
					}
					this.standardDev = (float) (Math.sqrt(sum / dim));
				}
	}

	/**
	 * @param x abscissa
	 * @param y ordinate
	 * @return true if the point (x,y) belongs to region, false otherwise
	 */
	private boolean test(int x, int y) {
		int offset, l;
		offset = y * w;
		l = x + offset;

		if (!(ip instanceof ColorProcessor)) {
			//		IJ.write("sto testando...");
			
			if (this.isEmpty)
				return true;
			else if (gradMap[l] <= gradThreshold
					&& pixValues[l] <= (this.edgePixMean))
				return true;
			else if (gradMap[l] > gradThreshold
					&& Math.abs(pixValues[l] - mean) <= standardDev)
				return true;
			else
				return false;
		}
		else {

			if (this.isEmpty)
				return true;
			else if (gradMap[l] <= gradThreshold
					&& pixValues[l]<= (this.edgePixMean))//Math.sqrt(Math.pow((colProc.getColor(x, y).getRed()-this.colorThreshold[0]),2)+Math.pow((colProc.getColor(x, y).getGreen()-this.colorThreshold[1]),2)+Math.pow((colProc.getColor(x, y).getBlue()-this.colorThreshold[2]),2))<=(Math.sqrt(3)*255))
				return true;
			else if (gradMap[l] > gradThreshold
					&& Math.sqrt((Math.pow(colProc.getColor(x, y).getRed() - meanRGB[0],2)) + (Math.pow(colProc.getColor(x, y).getGreen() - meanRGB[1],2)) + (Math.pow(colProc.getColor(x, y).getBlue() - meanRGB[2],2))) <= this.standardDev) 
				return true;
			else
				return false;
		}
	}

	public LinkedList<Coord2D> getPoints() {
		return this.store;
	}


	/**
	 * shows region standard deviation
	 */
	public void showStdVar() {
		if (colProc == null)
			IJ.write("deviazione standard regione = "+this.standardDev);
		else
			IJ.write("deviazione standard regione = "+this.standardDev);
	}

	/**
	 * shows region mean
	 */
	public void showMean() {
		if (colProc == null)
			IJ.write("media regione = "+this.mean);
		else
			IJ.write("media regione = "+this.meanRGB[0]+", "+this.meanRGB[1]+", "+this.meanRGB[2]);
	}

}
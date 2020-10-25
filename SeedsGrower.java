import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.Blitter;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import java.util.LinkedList;
import java.util.ListIterator;


/**
 * @author Alfonso Ridolfo
 * @version 0.1
 * This class provides methods to plant and grow a collection (a {@code LinkedList} region on the specified {@code ImageProcessor}
 */
public class SeedsGrower {

	private LinkedList<Coord2D> seeds;
	private ImageProcessor ip;
	private LinkedList<Region> regions;
	private int w, h;
	private byte[] hue;
	private byte[] sat;
	private byte[] bri;
	private float[] gradMap;
	private ImagePlus gradIMP;
	private ImageStatistics gradStats;
	private int[] gradHist;
	private float gradThreshold;
	private float edgePixMean;

	public SeedsGrower(LinkedList<Coord2D> seeds, ImageProcessor ip) {
		this.seeds = seeds;
		this.regions = new LinkedList<Region>();
		this.ip = ip;
		w = ip.getWidth();
		h = ip.getHeight();
		hue = new byte[w*h];
		sat = new byte[w*h];
		bri = new byte[w*h];
		this.gradCalc();
	}
	
	/**
	 * method to compute gradient map and gradient threshold of the input {@code ImageProcessor}
	 */
	private void gradCalc() {
		// copying the input Image
		ImagePlus impLP;
		ImageProcessor ipLP;
		byte[] pixels;
		if (ip instanceof ColorProcessor) {
			impLP = NewImage.createRGBImage("LowPassCopy", w, h, 1, NewImage.FILL_BLACK);
			ipLP = (ColorProcessor) impLP.getProcessor();			
		}
		else {
			impLP = NewImage.createFloatImage("LowPassCopy", w, h, 1, NewImage.FILL_BLACK);
			ipLP = (FloatProcessor) impLP.getProcessor();
		}
		ipLP.copyBits(ip, 0, 0, Blitter.COPY);
		ipLP.medianFilter();
		GradientCalculator gc = new GradientCalculator(ipLP);
		gc.calc();
		
		// obtaining gradient data
		this.gradMap = gc.getGradFloat();
		this.gradIMP = gc.getImage();
		this.gradStats = gradIMP.getStatistics();
		this.gradHist = gradStats.histogram;
//		gc.showGradImage();
		int nPixels = w*h;
		float sum = 0.0f;
		int i = 0;
		float counter = 0;
		
		// calculating gradient threshold
		while (counter<0.98) {
			sum += gradHist[i];			
			counter = sum/nPixels;			
			i++;
		}		
		this.gradThreshold = (float) (((i-1)*gradStats.binSize)+this.gradStats.histMin);
//		IJ.write("soglia del gradiente ="+this.gradThreshold);
		
		pixels = (byte[]) ip.convertToByte(false).getPixelsCopy();
		int offset, l;
		sum = 0;
		int countEdgePix = 0;
		for (int y=0; y<h; y++) {
			for (int x=0; x<w; x++){
				offset = y*w;
				l = x+offset;
				if (this.gradMap[l]>this.gradThreshold) {
					sum += (pixels[l] & 0xff);
					countEdgePix++;
				}
			}
		}
		edgePixMean = sum/countEdgePix;
//		IJ.write("media dei bordi: "+edgePixMean);
		return;
	}

	


	/**
	 * shows segmented output image in a new window
	 */
	public void showOutput() {
//		IJ.write("show output...");
		
		ImagePlus segOutput = NewImage.createRGBImage("Segmented Output", w, h, 1, NewImage.RGB);
		ColorProcessor segOutIP = (ColorProcessor) segOutput.getProcessor();
		segOutIP.copyBits(ip, 0, 0, Blitter.COPY);
		Region current;
		ListIterator regLi = regions.listIterator();
		int h = 0;
		int step = (int) Math.round(200.0f/(regions.size()-1));
		while (regLi.hasNext()) {
			current = (Region) regLi.next();
			this.draw(current, segOutIP, h);
			h += step;
		}
		segOutIP.setHSB(hue, sat, bri);
		segOutput.updateAndDraw();
		segOutput.show();
	}

	/**
	 * draw segmented regions on the output {@code ImagePlus}
	 * @param current region to draw
	 * @param segOutIP segmented output {@code ImageProcessor}
	 * @param color the hue value used to draw region
	 */
	private void draw(Region current, ImageProcessor segOutIP, int color) {
		LinkedList<Coord2D> pixels = current.getPoints();

		ListIterator pixLI = pixels.listIterator();
 
		int x, y, offset, l;
		Coord2D point;
		while (pixLI.hasNext()) {
			point = (Coord2D) pixLI.next();
			x = (int) point.X();
			y = (int) point.Y();
			offset = y*w;
			l = x+offset;
			hue[l] = (byte) color;
			sat[l] = (byte) 255;
			bri[l] = (byte) 255;
		}		
	}




	/**
	 * method that initializes the {@code Region}s and grows the seeds 
	 */
	public void growSeeds() {

//		IJ.write("growing seeds...");
		ListIterator sLI = seeds.listIterator();

		Coord2D curSeed;
		Region rgn;

		while (sLI.hasNext()) {



			rgn = new Region(ip, this.gradMap, this.gradIMP, this.gradThreshold, this.edgePixMean);
			curSeed = (Coord2D) sLI.next();



			rgn.grow(curSeed);

			regions.add(rgn);
//			rgn.showStdVar();
//			rgn.showMean();
		}
	}

}

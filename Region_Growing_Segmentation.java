import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


/**
 * @author Alfonso Ridolfo
 * @version 0.3
 * this class implements a Region Growing algorithm to segment 8-bit grayscale or RGB images;
 * this version does not require an inputintensity threshold value 
 */
public class Region_Growing_Segmentation implements PlugInFilter {
	ImagePlus imp;
	int w, h;
	public void run(ImageProcessor ip) {
		int nSeeds = (int)IJ.getNumber("Number of Regions/Seeds", 1);
		SeedGenerator sg = new SeedGenerator(ip);
		sg.selectSeedsManually(imp, nSeeds);		
	}

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL+NO_CHANGES;
	}

}

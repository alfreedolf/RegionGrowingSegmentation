import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.Blitter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * This class provides methods to apply gradient magnitude operator to an input {@code ImageProcessor} and to get results of this operation
 * @author Alfonso Ridolfo
 * @version 0.2
 */
public class GradientCalculator {
	
	private ImageProcessor IP;
	private ImagePlus gradImage;
	private int w, h;
	private ImageProcessor gradProcessor;
	private float[] gradPix;

	/**
	 * initialize calculator on target ImageProcessor
	 * @param processor target ImageProcessor
	 */
	public GradientCalculator(ImageProcessor processor) {
		IP = processor;
		w = IP.getWidth();
		h = IP.getHeight();
	}

	/**
	 * computes gradient magnitude of the image
	 */
	public void calc() {
		// creating 2 new ImagePlus to copy the input ImagePlus
        ImagePlus X_sobel = NewImage.createFloatImage("X-sobel", w, h, 1,
                NewImage.FILL_BLACK);    // ImagePlus storing orizontal edges convolution result
        ImagePlus Y_sobel = NewImage.createFloatImage("Y-sobel", w, h, 1,
                NewImage.FILL_BLACK);    // ImagePlus storing vertical edges convolution result
        
        ImageProcessor fIP;
        fIP = (FloatProcessor) IP.convertToFloat();
        // loading and converting the ImageProcessors
        ImageProcessor ip_X_sobel =
                (FloatProcessor) X_sobel.getProcessor().convertToFloat();
        ImageProcessor ip_Y_sobel =
                (FloatProcessor) Y_sobel.getProcessor().convertToFloat();
         
        // copying the ImageProcessor
        ip_X_sobel.copyBits(fIP, 0, 0, Blitter.COPY);
        
        // copying the ImageProcessor
        ip_Y_sobel.copyBits(fIP, 0, 0, Blitter.COPY);
        
        // creating X-Sobel kernel for the convolution
        int[] ker_X_sobel = {
        		-1, -2, -1, 0, 0, 0, 1, 2, 1
        };

        // creating Y-Sobel kernel for the convolution
        int[] ker_Y_sobel = {
        		-1, 0, 1, -2, 0, 2, -1, 0, 1
        };
        
        // calculating horizontal edges of the input image
        ip_X_sobel.convolve3x3(ker_X_sobel);
        ip_X_sobel.resetMinAndMax();

        
        // calculating vertical edges of the input image
        ip_Y_sobel.convolve3x3(ker_Y_sobel);
        ip_Y_sobel.resetMinAndMax();

        
        // initializing gradient ImagePlus
        gradImage = NewImage.createFloatImage("Gradient", w, h, 1,
                NewImage.FILL_BLACK);    // ImagePlus storing gradient result
        // initializing gradient ImageProcessor
        gradProcessor = (FloatProcessor)gradImage.getProcessor().convertToFloat();
        
        // getting pixels values
        gradPix = (float[])gradProcessor.getPixels();
        float[] XSobelPix = (float[])ip_X_sobel.getPixels();
        float[] YSobelPix = (float[])ip_Y_sobel.getPixels();
		
        int l, offset;
        for (int y=0; y<h; y++) {
        	for (int x=0; x<w; x++) {
        		offset = y*w;
        		l = offset+x;
        		gradPix[l] = (float) Math.sqrt((XSobelPix[l]*XSobelPix[l])+(YSobelPix[l]*YSobelPix[l]));
        	}
        }
	}
	
	

	/**
	 * @return a float map of the gradient magnitude
	 */
	public float[] getGradFloat() {
		return gradPix;
	}

	/**
	 * shows gradient magnitude
	 */
	public void showGradImage() {
		gradImage.updateAndDraw();
		gradImage.show();
	}
	
	/** 
	 * @return gradient magnitude as an ImagePlus
	 */
	public ImagePlus getImage() {
		return gradImage;
	}

}

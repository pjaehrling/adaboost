package de.htw.cv.ue04.helper;

import de.htw.cv.facedetection.IntegralImage;

/**
 * 
 * @author Marie Manderla, Philipp Jährling
 * @date 24.11.2015
 *
 */
public class IntegralImageMJ implements IntegralImage {
		
	private int[] ii;
	private int width;
	private int height;
	
	/**
	 * 
	 * @param srcARGBPixel
	 */
	public IntegralImageMJ(int[] srcARGBPixel, int width, int height) {
		// make sure all parameters are valid
		assert (width > 0 && height > 0 && srcARGBPixel.length > 0);
		
		this.width = width;
		this.height = height;
		
		int[] grayscale = createGrayscaleArray(srcARGBPixel);
		calculateIntegralImage(grayscale);
	}

	@Override
	public double meanValue(int x, int y, int areaWidth, int areaHeight) {
		// make sure the coordinates are in the image
		if ( x >= width || y >= height ) return 0;
		
		// System.out.println(x + "/" + y + " --> " + areaWidth + "/" + areaHeight + " --> " + width + "/" + height);
		
		// don't go beyond image boundaries
		areaWidth = (x + areaWidth >= width) ? (width - x - 1) : areaWidth;
		areaHeight = (y + areaHeight >= height) ? (height - y - 1) : areaHeight;
		
		// calculate positions in array
		int topLeft		= y * width + x;				// (x, y)
		int topRight 	= topLeft + areaWidth;			// (x + areaWidth, y)
		int bottomLeft 	= (y + areaHeight) * width + x;	// (x, y + areaHeight)
		int bottomRight = bottomLeft + areaWidth;		// (x + areaWidth, y + areaHeight)
		
		// get sum and divide by number of pixels in area
		int sum = ii[bottomRight] - ii[topRight] - ii[bottomLeft] + ii[topLeft];
		double mean = (double)sum / (areaWidth * areaHeight);
		return mean;
	}

	@Override
	public void toIntARGB(int[] dstImage) {
		if(dstImage.length == ii.length) {
			int maxVal = ii[ii.length - 1];
			double scale = 255.0 / maxVal;
			int gray = 0;
			
			for (int pos = 0; pos < ii.length; pos++) {
				gray = (int)(scale * ii[pos]);
				dstImage[pos] =  (0xFF << 24) | (gray << 16) | (gray << 8) | gray;	
			}
		}
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}
	
	/**
	 * 
	 * Fill the IntegralImage data array
	 * 
	 * Formula: ii(x, y) = ii(x, y-1) + ii(x-1, y) + grayscalePixel(x, y) - ii(x-1, y-1)
	 * 
	 * @param grayscalePixel
	 * @param width
	 * @param height
	 * @return
	 */
	private void calculateIntegralImage(int[] grayscalePixel) {
		this.ii = new int[width * height];
		int pos, leftPos, topPos, topLeftPos;
		
		// First element (0,0)
		ii[0] = grayscalePixel[0];

		// First Row -> ii(x, 0) = ii(x-1, 0) + grayscalePixel(x, 0)
		for (int x = 1; x < width; x++) {
			ii[x] = ii[x-1] + grayscalePixel[x];
		}
		
		// First Column -> ii(0, y) = ii(0, y-1) + grayscalePixel(0, y)
		for (int y = 1; y < height; y++) {
			pos = y * width;
			ii[pos] = ii[pos - width] + grayscalePixel[pos];
		}
		
		// Rest
		for (int y = 1; y < height; y++) {
			for (int x = 1; x < width; x++) {
				
				pos	= y * width + x;		// (x, y)
				leftPos = pos - 1; 			// (x-1, y)
				topPos = pos - width; 		// (x, y-1)
				topLeftPos = topPos - 1; 	// (x-1, y-1)
				
				ii[pos] = ii[leftPos] + ii[topPos] + grayscalePixel[pos] - ii[topLeftPos];
			}
		}
	}
	
	/**
	 * Get gray-scale value array for argb pixel array
	 * 
	 * @param srcARGBPixel
	 * @return int array with gray-scale values
	 */
	private int[] createGrayscaleArray(int[] srcARGBPixel) {
		int[] grayscaleValues = new int[srcARGBPixel.length];
		
		for (int i = 0; i < srcARGBPixel.length; i++) {
			int argb = srcARGBPixel[i]; // ARGB Value
			int r = (argb >> 16	) & 0xFF;
			int g = (argb >> 8	) & 0xFF;
			int b = (argb   	) & 0xFF;
					
			grayscaleValues[i] = (int) (0.299*r + 0.587*g + 0.114*b); // grayscale
		}
		
		return grayscaleValues;
	}

}

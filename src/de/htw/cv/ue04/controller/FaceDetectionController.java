/**
 * @author Nico Hezel
 */
package de.htw.cv.ue04.controller;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import de.htw.cv.facedetection.ImagePatternClassifier;
import de.htw.cv.facedetection.IntegralImage;
import de.htw.cv.facedetection.RandomClassifier;
import de.htw.cv.facedetection.TestImage;
import de.htw.cv.ue04.adaboost.AdaBoost;
import de.htw.cv.ue04.classifier.ClassifierMJ;
import de.htw.cv.ue04.classifier.StrongClassifierMJ;
import de.htw.cv.ue04.helper.IntegralImageMJ;

public class FaceDetectionController extends FaceDetectionBase {

	private static final int WHITE = 0xFF000000 | (255<<16) | (255<<8) | 255;
	private static final int BLACK = 0xFF000000 | (0<<16) | (0<<8) | 0;
	private static final int NEIGHBOUR = 5;
	
	@Override
	protected IntegralImage createIntegralImage(int[] px, int width, int height) {
		return new IntegralImageMJ(px, width, height);
	}

	@Override
	protected ImagePatternClassifier createStrongClassifier(int weakClassifierCount) {
		// TODO erstelle einen untrainierten StrongClassifier mit weakClassifierCount WeakClassifier
				
    	// First two classifiers: eye (right/left) = forehead (light), eye area (dark), cheek (light)
    	ArrayList<Rectangle> plus = new ArrayList<Rectangle>();
    	ArrayList<Rectangle> minus = new ArrayList<Rectangle>();
    	plus.add( new Rectangle(0, 0, 1, 1) ); // forehead - top
    	minus.add( new Rectangle(0, 1, 1, 1) ); // eye area - middle
    	plus.add( new Rectangle(0, 2, 1, 1) ); // cheek - bottom
    	
    	ImagePatternClassifier eyeLeft = new ClassifierMJ(0, 0, plus, minus, 0.2, 0.5);
    	ImagePatternClassifier eyeRight = new ClassifierMJ(2, 0, plus, minus, 0.2, 0.5);
    	eyeLeft = eyeLeft.getScaledInstance(48);
    	eyeRight = eyeRight.getScaledInstance(48);
    	
    	
    	// Second classifier: nose - left/right (dark) and inner (light)
    	plus = new ArrayList<Rectangle>();
    	minus = new ArrayList<Rectangle>();
    	minus.add( new Rectangle(0, 0, 10, 60) ); // outer nose - left
    	plus.add( new Rectangle(10, 0, 20, 60) ); // inner nose - middle
    	minus.add( new Rectangle(30, 0, 10, 60) ); // outer nose - right   	
    	ImagePatternClassifier nose = new ClassifierMJ(50, 60, plus, minus, 0.2, 0.5);
    	
    	// Third classifier: nose end - top (dark) and bottom (light)
    	plus = new ArrayList<Rectangle>();
    	minus = new ArrayList<Rectangle>();
    	minus.add( new Rectangle(0, 0, 40, 15) ); // end nose - top
    	plus.add( new Rectangle(0, 15, 40, 15) ); // under nose - bottom
    	ImagePatternClassifier noseEnd = new ClassifierMJ(50, 128, plus, minus, 0.2, 0.5);
 
    	
    	// Third classifier: mouth - top (dark) and chin - bottom (light)
    	plus = new ArrayList<Rectangle>();
    	minus = new ArrayList<Rectangle>();
    	minus.add( new Rectangle(0, 0, 60, 30) ); // mouth - top
    	plus.add( new Rectangle(0, 30, 60, 30) ); // chin - bottom	
    	ImagePatternClassifier mouth = new ClassifierMJ(45, 160, plus, minus, 0.2, 0.5);
    	
    	// Create weak classifier list and add classifiers
    	ArrayList<ImagePatternClassifier> weakClassifiers = new ArrayList<ImagePatternClassifier>();
    	weakClassifiers.add(eyeLeft);
    	weakClassifiers.add(eyeRight);
    	weakClassifiers.add(nose);
    	weakClassifiers.add(noseEnd);
    	weakClassifiers.add(mouth);
	
		StrongClassifierMJ sc = new StrongClassifierMJ(weakClassifiers);
		
		// TODO (PJ): Mit weak Classifiern füllen. 
		// Sein Tipp: Erstmal die aus Übung 3 nehmen und nur das Gewichten testen
		return sc;
	}

	@Override
	protected void trainStrongClassifier(TestImage image, ImagePatternClassifier strongClassifier) {
		AdaBoost ab = new AdaBoost(image, testImage.getFaceRectangles(), testImage.getNonFaceRectangles());
		
		// TODO (PJ)
		// Mir fällt nichts besseres als der cast ein und die Methode "getWeakClassifierList" in StrongClassifierMJ. 
		// Wir brauchen beim trainieren dann auf irgend einem Weg alle WeakClassifier.
		// Durch seinen Aufbau werden auch alle WC vorher zum SC hinzugefügt.
		// Es gibt aber in ImagePatternClassifier keine Möglichkeit darauf zu zu greifen.
		
		ab.train((StrongClassifierMJ) strongClassifier);
	}

	@Override
	protected void doVoilaJones(int[] srcPixels, int srcWidth, int srcHeight, int[] dstPixels, 
			double threshold, TestImage testImage, ImagePatternClassifier strongClassifier, int dstWidth, int dstHeight) {

	   	// einfacher Klassifier mit zufälligem Ergebnis
    	ImagePatternClassifier classifier = strongClassifier;
     	
     	// wie groß ist der Klassifier
		Rectangle area = classifier.getArea();

		// durchlaufe das Bild, ignoriere die Ränder
     	for (int y = 0; y < srcHeight-area.getHeight()*0.8; y++) {	
			for (int x = 0; x < srcWidth-area.getWidth()*0.8; x++)	{
				int pos = y * srcWidth + x;
				
				// berechne den Korrelationswert an jeder Position
				double correlation = classifier.matchAt(testImage, x, y, 0.0);
				
				// zeichne das Korrelationsbild
				int grey = (int)(correlation * 255.0);
				dstPixels[pos] =  (0xFF << 24) | (grey << 16) | (grey << 8) | grey;	
			}
		}
     	
     	// erstelle eine Kopie vom Eingangsbild
		BufferedImage bufferedImage = new BufferedImage(srcWidth, srcHeight, BufferedImage.TYPE_INT_ARGB);
    	bufferedImage.setRGB(0, 0, srcWidth, srcHeight, srcPixels, 0, srcWidth);
    	Graphics2D g2d = bufferedImage.createGraphics();
    	
    	// Get maxims and draw strong classifier at max positions
    	int[] maximas = new int[dstHeight * dstWidth];
     	getCorrelationMaximas(dstPixels, maximas, dstWidth, dstHeight, 0.95);
    	
    	// zeichne die Trainingsregionen ein
    	drawTrainingsRegions(g2d);
    	
    	for (int y = 0; y < dstHeight; y++) {	
			for (int x = 0; x < dstWidth; x++)	{
				int pos = y * srcWidth + x;
				
				if (maximas[pos] == WHITE) {
					classifier.drawAt(g2d, x, y);
					
					// TODO ... hacky break after one maxima was found
					y = dstHeight;
					x = dstWidth;
				}
					
			}
		}
     	     	
     	// schreibe die Kopie in die Eingangspixel zurück
    	g2d.dispose();
		bufferedImage.getRGB(0, 0, srcWidth, srcHeight, srcPixels, 0, srcWidth); 
	}
	
	/**
	 * Draw the regions that are used by AdaBoost
	 * @param g2d
	 */
	private void drawTrainingsRegions(Graphics2D g2d) {
		List<Rectangle> faceRegions = testImage.getFaceRectangles();
    	List<Rectangle> nonFaceRegions = testImage.getNonFaceRectangles();
    	
    	g2d.setColor(Color.GREEN);
    	for (Rectangle fr : faceRegions) {
    		g2d.drawRect(fr.x, fr.y, fr.width, fr.height);
    	}
    	g2d.setColor(Color.RED);
    	for (Rectangle nfr : nonFaceRegions) {
    		g2d.drawRect(nfr.x, nfr.y, nfr.width, nfr.height);
    	}
	}
	
	/**
     * 
     * @param srcPixels
     * @param dstPixels
     * @param width
     * @param height
     * @param threshold
     */
    private void getCorrelationMaximas(int srcPixels[], int dstPixels[], int width, int height, double threshold) {
     	int min = getCorrelationMin(srcPixels);
    	int max = getCorrelationMax(srcPixels);
    	if (max - min == 0) {
    		return;
    	}
    	
    	for (int y = 0; y < height; y++) {	
			for (int x = 0; x < width; x++)	{
				int pos = y * width + x;
				
				int grey = (srcPixels[pos] >> 16) & 0xFF;
				double greyNorm = normalize(grey, min, max);
    			
	    		if (greyNorm > threshold && biggerThanNeighbours(srcPixels, x, y, width, height)) {
	    			dstPixels[pos] = WHITE;
	    			System.out.println("WHITE --> " + grey);
	    		} else {
	    			dstPixels[pos] = BLACK;
	    		}
			}
    	}	
    }
    
    /**
     * 
     * @param array
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
	private boolean biggerThanNeighbours(int array[], int x, int y, int width, int height)
	{
		int pos = y * width + x;
		int mid = NEIGHBOUR / 2;
		for (int j = 0; j < NEIGHBOUR; j++) {
			for (int i = 0; i < NEIGHBOUR; i++) {
				int neighbourX = i - mid;
				int neighbourY = j - mid;
				int posComp = (y + neighbourY) * width + (x + neighbourX);
				if (isInImage(x + neighbourX, y + neighbourY, width, height)) {
					if (array[posComp] > array[pos]) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	private boolean isInImage(int  x, int y, int width, int height) {
		return  x < width && 
				x > -1 &&	
				y < height &&
				y > -1;
	}
    
	/**
	 * 
	 * @param srcPixels
	 * @return
	 */
    private int getCorrelationMin(int srcPixels[]) {
    	int min = 255;
    	
    	for (int i = 0; i < srcPixels.length; i++) {
    		int corr = (srcPixels[i]>>16)&0xFF;
    		if (corr < min) {
    			min = corr;
    		}
    	}
  
    	return min;
    }
    
    /**
     * 
     * @param srcPixels
     * @return
     */
    private int getCorrelationMax(int srcPixels[]) {
    	int max = 0;
    	
    	for (int i = 0; i < srcPixels.length; i++) {
    		int corr = (srcPixels[i]>>16)&0xFF;
    		if (corr > max) {
    			max = corr;
    		}
    	}
    	
    	return max;
    }
    
	/**
	 * Normalize a given value (used e.g. to normalize the correlation value)
	 * 
	 * @param num
	 * @param min
	 * @param max
	 * @return
	 */
	private double normalize(double num, double min, double max) {
		return (num - min) / (max - min);
	}
}

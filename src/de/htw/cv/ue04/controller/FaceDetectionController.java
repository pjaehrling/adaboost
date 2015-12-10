/**
 * @author Nico Hezel
 */
package de.htw.cv.ue04.controller;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import de.htw.cv.facedetection.ImagePatternClassifier;
import de.htw.cv.facedetection.IntegralImage;
import de.htw.cv.facedetection.RandomClassifier;
import de.htw.cv.facedetection.TestImage;

public class FaceDetectionController extends FaceDetectionBase {

	@Override
	protected IntegralImage createIntegralImage(int[] px, int width, int height) {
		
		// TODO erstelle ein eigenes IntegralImage
		return new IntegralImage() {
			
			@Override
			public void toIntARGB(int[] dstImage) {				
			}
			
			@Override
			public double meanValue(int x, int y, int width, int height) {
				return 0;
			}
			
			@Override
			public int getWidth() {
				return width;
			}
			
			@Override
			public int getHeight() {
				return height;
			}
		};
	}

	@Override
	protected ImagePatternClassifier createStrongClassifier(int weakClassifierCount) {
		// TODO erstelle einen untrainierten StrongClassifier mit weakClassifierCount WeakClassifier
		return null;
	}

	@Override
	protected void trainStrongClassifier(TestImage image, ImagePatternClassifier strongClassifier) {
		// TODO trainiere den übergebenen StrongClassifier mit AdaBoost		
	}

	@Override
	protected void doVoilaJones(int[] srcPixels, int srcWidth, int srcHeight, int[] dstPixels, 
			double threshold, TestImage testImage, ImagePatternClassifier strongClassifier) {

	   	// einfacher Klassifier mit zufälligem Ergebnis
    	ImagePatternClassifier classifier = new RandomClassifier(150, 215);
     	
     	// wie groß ist der Klassifier
		Rectangle area = classifier.getArea();

		// durchlaufe das Bild, ignoriere die Ränder
     	for (int y = 0; y < srcHeight-area.getHeight()*0.8; y++) {	
			for (int x = 0; x < srcWidth-area.getWidth()*0.8; x++)	{
				int pos = y * srcWidth + x;
				
				// berechne den Korrelationswert an jeder Position
				double correlation = classifier.matchAt(testImage, x, y);
				
				// zeichne das Korrelationsbild
				int grey = (int)(correlation * 255.0);
				dstPixels[pos] =  (0xFF << 24) | (grey << 16) | (grey << 8) | grey;	
			}
		}
     	
     	// TODO finde die Maximas im Korrelations-Bild
     	Rectangle faceRect = testImage.getFaceRectangles().get(0); // ACHTUNG: vorgegebene Region
     	
     	// erstelle eine Kopie vom Eingangsbild
		BufferedImage bufferedImage = new BufferedImage(srcWidth, srcHeight, BufferedImage.TYPE_INT_ARGB);
    	bufferedImage.setRGB(0, 0, srcWidth, srcHeight, srcPixels, 0, srcWidth);
    	Graphics2D g2d = bufferedImage.createGraphics();
    	
    	// zeichne die Gesichtsregionen ein
    	// TODO verwende hier die gefundenen Maximas
     	classifier.drawAt(g2d, (int)faceRect.getX(), (int)faceRect.getY());
     	     	
     	// schreibe die Kopie in die Eingangspixel zurück
    	g2d.dispose();
		bufferedImage.getRGB(0, 0, srcWidth, srcHeight, srcPixels, 0, srcWidth); 
	}   
}

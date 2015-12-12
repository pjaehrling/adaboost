/**
 * @author Nico Hezel
 */
package de.htw.cv.ue04.controller;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import de.htw.cv.facedetection.ImagePatternClassifier;
import de.htw.cv.facedetection.IntegralImage;
import de.htw.cv.facedetection.RandomClassifier;
import de.htw.cv.facedetection.TestImage;
import de.htw.cv.ue04.adaboost.AdaBoost;
import de.htw.cv.ue04.classifier.StrongClassifierMJ;
import de.htw.cv.ue04.helper.IntegralImageMJ;

public class FaceDetectionController extends FaceDetectionBase {

	@Override
	protected IntegralImage createIntegralImage(int[] px, int width, int height) {
		return new IntegralImageMJ(px, width, height);
	}

	@Override
	protected ImagePatternClassifier createStrongClassifier(int weakClassifierCount) {
		// TODO erstelle einen untrainierten StrongClassifier mit weakClassifierCount WeakClassifier
		
		StrongClassifierMJ sc = new StrongClassifierMJ();
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
    	
    	// zeichne die Trainingsregionen ein
    	drawTrainingsRegions(g2d);
    	
    	// zeichne die Gesichtsregionen ein
    	// TODO verwende hier die gefundenen Maximas
     	classifier.drawAt(g2d, (int)faceRect.getX(), (int)faceRect.getY());
     	     	
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
}

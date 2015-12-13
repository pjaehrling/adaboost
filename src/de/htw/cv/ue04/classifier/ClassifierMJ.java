package de.htw.cv.ue04.classifier;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import de.htw.cv.facedetection.ImagePatternClassifier;
import de.htw.cv.facedetection.IntegralImage;
import de.htw.cv.facedetection.TestImage;


/**
 * 
 * @author Marie Manderla, Philipp Jährling
 * @date 28.11.2015
 *
 */
public class ClassifierMJ implements ImagePatternClassifier {

	private Rectangle area;
	private List<Rectangle> plusAreas;
	private List<Rectangle> minusAreas;
	private double weight;
	private double treshold;
	
	public double getTreshold() {
		return treshold;
	}

	public void setTreshold(double treshold) {
		this.treshold = treshold;
	}

	/**
	 * Create a weak classifier and set it's boundaries.
	 * 
	 * @param maxWidth
	 * @param maxHeight
	 */
	public ClassifierMJ(int x, int y, int maxWidth, int maxHeight) {
		this.area = new Rectangle(x, y, maxWidth, maxHeight);
		this.plusAreas = new ArrayList<Rectangle>();
		this.minusAreas = new ArrayList<Rectangle>();
	}
	
	/**
	 * Create a weak classifier by passing plus and minus areas and a classifier weight.
	 * 
	 * @param plusAreas
	 * @param minusAreas
	 * @param weight
	 * @param treshold
	 */
	public ClassifierMJ(int x, int y, ArrayList<Rectangle> plusAreas, ArrayList<Rectangle> minusAreas, double weight, double treshold) {	
		this.area = new Rectangle(0, 0, 0, 0); // adding the plus and minus areas will change the position anyway
		this.weight = weight;
		this.treshold = treshold;
		this.plusAreas = new ArrayList<Rectangle>();
		this.minusAreas = new ArrayList<Rectangle>();
		
		for (Rectangle rec : plusAreas) {
			this.addPlusPattern(rec); // add the area
		}
		for (Rectangle rec : minusAreas) {
			this.addMinusPattern(rec); // add the area
		}
	}
	
	
	@Override
	public ImagePatternClassifier getScaledInstance(double scale) {			
		ClassifierMJ scaled = new ClassifierMJ(
			(int)(area.x * scale), 		// x
			(int)(area.y * scale),		// y
			(int)(area.width * scale),	// width
			(int)(area.height * scale)	// height
		);
		scaled.weight = this.weight;
		scaled.treshold = this.treshold;
		
		for (Rectangle rec : plusAreas) {
			scaled.addPlusPattern(scaleRec(rec, scale));
		}
		for (Rectangle rec : minusAreas) {
			scaled.addMinusPattern(scaleRec(rec, scale));
		}
		
		return scaled;
	}

	/**
	 * Create a scaled Rectangle from a given one, multiplied by the given scale factor
	 * 
	 * @param rec
	 * @param scale
	 * @return
	 */
	private Rectangle scaleRec(Rectangle rec, double scale) {
		return new Rectangle(
			(int)(rec.x * scale), 		// x
			(int)(rec.y * scale), 		// y
			(int)(rec.width * scale), 	// width
			(int)(rec.height * scale)	// height
		);
	}

	/**
	 * Add a plus (light area) pattern rectangle to the classifier
	 * 
	 * @param rec
	 */
	public void addPlusPattern(Rectangle rec) {
		this.area.add(rec); // resize the classifier rectangle -> will also change x/y!
		plusAreas.add(rec); // add the area
	}
	
	/**
	 * Add a minus (dark area) pattern rectangle to the classifier
	 * 
	 * @param rec
	 */
	public void addMinusPattern(Rectangle rec) {
		this.area.add(rec); // resize the classifier rectangle -> will also change x/y!
		minusAreas.add(rec); // add the area
	}
	
	/**
	 * Check if a given rectangle is inside of another ones boundaries.
	 * 
	 * @param rec
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 */
	private boolean isInBoundaries(Rectangle rec, int maxWidth, int maxHeight) {
		double maxX = rec.getX() + rec.getWidth();
		double maxY = rec.getY() + rec.getHeight();
		return (maxX <= maxWidth && maxY <= maxHeight);
	}
	
	@Override
	public double matchAt(TestImage image, int posX, int posY) {
		return matchAt(image, posX, posY, this.treshold);
	}

	@Override
	public double matchAt(TestImage image, int posX, int posY, double threshold) {
		IntegralImage integral = image.getIntegralImage();
		int plusMean = 0;
		int minusMean = 0;
		
		for (Rectangle r : plusAreas) {
			plusMean += integral.meanValue(posX + area.x + r.x, posY + area.y + r.y, r.width, r.height);
		}
		
		for (Rectangle r : minusAreas) {
			minusMean += integral.meanValue(posX + area.x + r.x, posY + area.y + r.y, r.width, r.height);
		}
		
		double correlation = Math.abs(plusMean - minusMean);
		correlation = normalize(correlation, 0, 255);
		
		return correlation > treshold ? correlation : 0;
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
	
	@Override
	public Rectangle getArea() {
		return area;
	}

	public void setArea(Rectangle area) {
		this.area = area;
	}
	
	@Override
	public double getWeight() {
		return weight;
	}

	@Override
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	@Override
	public String toString() {
		return "MyClassifier [area=" + area + ", weight=" + weight + "]";
	}

	@Override
	public void drawAt(Graphics2D g2d, int x, int y) {	
		//System.out.println("draw Rect: " + area.x + "/" + area.y + " - " + area.width + "/" + area.height);
		
		Color plus = new Color(0x3300FF00, true);
		for (Rectangle r : plusAreas) {
			g2d.setColor(plus);
			g2d.fillRect(x + r.x, y + r.y, r.width, r.height);
		}
		
		Color minus = new Color(0x33FF0000, true);
		for (Rectangle r : minusAreas) {
			g2d.setColor(minus);
			g2d.fillRect(x + r.x, y + r.y, r.width, r.height);
		}
	}
}

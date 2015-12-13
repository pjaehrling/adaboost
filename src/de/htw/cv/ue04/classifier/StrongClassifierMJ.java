package de.htw.cv.ue04.classifier;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import de.htw.cv.facedetection.ImagePatternClassifier;
import de.htw.cv.facedetection.TestImage;


/**
 * 
 * @author Marie Manderla, Philipp Jährling
 * @date 28.11.2015
 *
 */
public class StrongClassifierMJ implements ImagePatternClassifier {

	private List<ImagePatternClassifier> weakClassifiers;

	/**
	 * Create an empty strong classifier
	 */
	public StrongClassifierMJ() {
		weakClassifiers = new ArrayList<ImagePatternClassifier>();
	}
	
	/**
	 * Create a strong classifier using the given (weak) classifier list
	 * 
	 * @param weakClassifiers
	 */
	public StrongClassifierMJ(ArrayList<ImagePatternClassifier> weakClassifiers) {
		this.weakClassifiers = new ArrayList<ImagePatternClassifier>();
		this.weakClassifiers.addAll(weakClassifiers);
	}

	/**
	 * Add a weak classifier to the String one
	 * @param classifier
	 */
	public void addWeakClassifier(ImagePatternClassifier classifier) {
		this.weakClassifiers.add(classifier);
	}
	
	/**
	 * Get the current weak classifier list, e.g. for training
	 * 
	 * @return
	 */
	public List<ImagePatternClassifier> getWeakClassifierList() {
		return this.weakClassifiers;
	}
	
	/**
	 * Set a totally new classifier list, e.g. after the training by AdaBoost
	 * 
	 * @param classifierList
	 */
	public void setWeakClassifierList(List<ImagePatternClassifier> classifierList) {
		this.weakClassifiers = classifierList;
	}
	
	@Override
	public ImagePatternClassifier getScaledInstance(double scale) {
		StrongClassifierMJ scaled = new StrongClassifierMJ();
		
		for (ImagePatternClassifier classifier : weakClassifiers) {
			scaled.addWeakClassifier(classifier.getScaledInstance(scale));
		}
		
		return scaled;
	}

	@Override
	public double matchAt(TestImage image, int posX, int posY) {
		double match = 0;
		
		for (ImagePatternClassifier classifier : weakClassifiers) {
			match += classifier.matchAt(image, posX, posY) * classifier.getWeight();
		}
		
		return match;
	}

	@Override
	public double matchAt(TestImage image, int posX, int posY, double threshold) {
		double match = 0;
		
		for (ImagePatternClassifier classifier : weakClassifiers) {
			match += classifier.matchAt(image, posX, posY) * classifier.getWeight();
		}
		
		return match > threshold ?  match : 0;
	}

	@Override
	public Rectangle getArea() {
		Rectangle area = new Rectangle(0, 0, 0, 0);
		
		for (ImagePatternClassifier classifier : weakClassifiers) {
			area.add(classifier.getArea());
		}
		
		return area;
	}

	@Override
	public double getWeight() {
		double w = 0.0;
		
		for (ImagePatternClassifier classifier : weakClassifiers) {
			w += classifier.getWeight();
		}
		
		return w;
	}

	@Override
	public void setWeight(double weight) {
		// TODO
	}

	@Override
	public void drawAt(Graphics2D g2d, int x, int y) {
		Rectangle area = this.getArea();
		g2d.setColor(Color.GREEN);
    	g2d.drawRect(x + area.x, y + area.y, area.width, area.height);
    	
    	for (ImagePatternClassifier classifier : weakClassifiers) {
    		classifier.drawAt(g2d, x, y);
		}
    	
	}

}

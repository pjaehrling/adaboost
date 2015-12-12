package de.htw.cv.ue04.adaboost;

import java.awt.Rectangle;

import de.htw.cv.facedetection.TestImage;

/**
 * 
 * @author Marie Manderla, Philipp Jährling
 * @date 10.12.2015
 *
 */
public class TrainingsData {

	private Rectangle x; 	// the rectangle area in the source image
	private boolean y;		// is it a face region/image
	private double w;		// the weight of the image - get's 
	
	/**
	 * 
	 * 
	 * @param areaInSrcImage
	 * @param isFace
	 * @param startWeight
	 */
	public TrainingsData(Rectangle areaInSrcImage, boolean isFace, double startWeight) {
		this.x = areaInSrcImage;
		this.y = isFace;
		this.setWeight(startWeight);
	}

	/**
	 * 
	 * @return
	 */
	public double getWeight() {
		return w;
	}

	/**
	 * 
	 * @param w
	 */
	public void setWeight(double w) {
		this.w = w;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isFace() {
		return y;
	}
	
	/*
	 * Getter for the test rectangle
	 */
	public int getRectX() {
		return x.x;
	}
	public int getRectY() {
		return x.y;
	}
	public int getRectWidth() {
		return x.width;
	}
	public int getRectHeight() {
		return x.height;
	}
	
}

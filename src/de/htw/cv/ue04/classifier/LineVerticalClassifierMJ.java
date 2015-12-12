package de.htw.cv.ue04.classifier;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class LineVerticalClassifierMJ extends ClassifierMJ {

	public LineVerticalClassifierMJ(int x, int y, double weight, double treshold) {
		super(x, y, 0, 0);
		
		List<Rectangle> plusAreas = new ArrayList<Rectangle>();
		List<Rectangle> minusAreas = new ArrayList<Rectangle>();
		plusAreas.add( new Rectangle(0, 0, 1, 1) );
    	minusAreas.add( new Rectangle(1, 0, 1, 1) );
		plusAreas.add( new Rectangle(2, 0, 1, 1) );
		
		setWeight(weight);
		setTreshold(treshold);
		
		for (Rectangle rec : plusAreas) {
			this.addPlusPattern(rec); // add the area
		}
		for (Rectangle rec : minusAreas) {
			this.addMinusPattern(rec); // add the area
		}
	}
}

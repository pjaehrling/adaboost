package de.htw.cv.ue04.classifier;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class LineHorizontalClassifierMJ extends ClassifierMJ {

	public LineHorizontalClassifierMJ(int x, int y, int width, int height, double weight, double treshold) {
		super(x, y, 0, 0);
		
		List<Rectangle> plusAreas = new ArrayList<Rectangle>();
		List<Rectangle> minusAreas = new ArrayList<Rectangle>();
		plusAreas.add( new Rectangle(0, 0, width, height) );
    	minusAreas.add( new Rectangle(width, 0, width, height) );
		plusAreas.add( new Rectangle(width * 2, 0, width, height) );
		
		setArea(new Rectangle(x, y, 0, 0)); // adding the plus and minus areas will change the position anyway
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

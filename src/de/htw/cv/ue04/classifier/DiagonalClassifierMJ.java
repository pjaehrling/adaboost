package de.htw.cv.ue04.classifier;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class DiagonalClassifierMJ extends ClassifierMJ {

	public DiagonalClassifierMJ(int x, int y, int width, int height, double weight, double treshold) {
		super(0, 0, 0, 0);
		
		List<Rectangle> plusAreas = new ArrayList<Rectangle>();
		List<Rectangle> minusAreas = new ArrayList<Rectangle>();
		plusAreas.add( new Rectangle(x, y, width, height) );
    	minusAreas.add( new Rectangle(x + width, y, width, height) );
		plusAreas.add( new Rectangle(x + width, y + height, width, height) );
		minusAreas.add( new Rectangle(x, y + height, width, height));
		
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

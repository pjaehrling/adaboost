package de.htw.cv.ue04.classifier;

import java.util.ArrayList;
import java.util.Random;

import de.htw.cv.facedetection.ImagePatternClassifier;

public class ClassifierMJGenerator {
	
	public static ArrayList<ImagePatternClassifier> getBasicClassifiers(int num, int maxWidth, int maxHeight) {
		ArrayList<ImagePatternClassifier> classifiers = new ArrayList<ImagePatternClassifier>();
		
		for (int i = 0; i < num; i++) {
			classifiers.add(getBasicClassifier(maxWidth, maxHeight, 1.0 / num, 0.5));
		}
		
		return classifiers;
	}
	
	private static ImagePatternClassifier getBasicClassifier(int maxWidth, int maxHeight, double weight, double treshold) {
		ImagePatternClassifier classifier;
		
		Random rnd = new Random();
		int form = rnd.nextInt(4);
		int width = rnd.nextInt(maxWidth / 3);
		int height = rnd.nextInt(maxHeight / 3);
		int x = rnd.nextInt(maxWidth - width);
		int y = rnd.nextInt(maxHeight - height);
		switch (form) {
		case 0:
			classifier = new DiagonalClassifierMJ(x, y, width, height, weight, treshold);
			break;
		case 1:
			classifier = new EdgeHorizontalClassifierMJ(x, y, width, height, weight, treshold);
			break;
		case 2:
			classifier = new EdgeVerticalClassifierMJ(x, y, width, height, weight, treshold);
			break;
		case 3:
			classifier = new LineHorizontalClassifierMJ(x, y, width, height, weight, treshold);
			break;
		case 4:
			classifier = new LineVerticalClassifierMJ(x, y, width, height, weight, treshold);
			break;
		default:
			classifier = new ClassifierMJ(x, y, maxWidth, maxHeight);
			break;
		}
		
		return classifier;
	}
}

package de.htw.cv.ue04.classifier;

import java.util.ArrayList;
import java.util.Random;

import de.htw.cv.facedetection.ImagePatternClassifier;

public class ClassifierMJGenerator {
	
	public static ArrayList<ImagePatternClassifier> getBasicClassifiers(int num, int maxWidth, int maxHeight) {
		ArrayList<ImagePatternClassifier> classifiers = new ArrayList<ImagePatternClassifier>();
		
		for (int i = 0; i < num; i++) {
			classifiers.add(getRandomClassifier(15, maxWidth, 15, maxHeight, 1.0 / num, 0.5));
		}
		
		return classifiers;
	}
	
	private static ImagePatternClassifier getRandomClassifier(int minWidth, int maxWidth, int minHeight, int maxHeight, double weight, double treshold) {
		ImagePatternClassifier classifier;
		
		Random rnd = new Random();
		int form = rnd.nextInt(4);
		int width = rnd.nextInt(maxWidth - minWidth) + minWidth;
		int height = rnd.nextInt(maxHeight - minHeight) + minHeight;
		int x = rnd.nextInt(maxWidth - width);
		int y = rnd.nextInt(maxHeight - height);
		switch (form) {
		case 0:
			classifier = new DiagonalClassifierMJ(x, y, width / 2, height / 2, weight, treshold);
			break;
		case 1:
			classifier = new EdgeHorizontalClassifierMJ(x, y, width / 2, height / 2, weight, treshold);
			break;
		case 2:
			classifier = new EdgeVerticalClassifierMJ(x, y, width / 2, height / 2, weight, treshold);
			break;
		case 3:
			classifier = new LineHorizontalClassifierMJ(x, y, width / 3, height / 3, weight, treshold);
			break;
		case 4:
			classifier = new LineVerticalClassifierMJ(x, y, width / 3, height / 3, weight, treshold);
			break;
		default:
			classifier = new ClassifierMJ(x, y, maxWidth, maxHeight);
			break;
		}
		
		return classifier;
	}
}

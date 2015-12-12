package de.htw.cv.ue04.adaboost;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import de.htw.cv.facedetection.ImagePatternClassifier;
import de.htw.cv.facedetection.TestImage;
import de.htw.cv.ue04.classifier.StrongClassifierMJ;

/**
 * 
 * @author Marie Manderla, Philipp Jährling
 * @date 10.12.2015
 *
 */
public class AdaBoost {
	
	private TestImage image;
	private List<TrainingsData> trainingsData;
	
	/* ****************************************************************************************
	 * 							PUBLIC
	 * ****************************************************************************************/
	
	/**
	 * 
	 * @param image
	 * @param faceRegions
	 * @param nonFaceRegions
	 */
	public AdaBoost(TestImage image, List<Rectangle> faceRegions, List<Rectangle> nonFaceRegions) {
		this.image = image;
		
		// Calculate the initial weights
		double startWeightFace = 1.0 / (2 * faceRegions.size());
		double startWeightNonFace = 1.0 / (2 * nonFaceRegions.size());
		
		// Create a new list for all trainings-images
		trainingsData = new ArrayList<TrainingsData>();
		faceRegions.forEach((faceRegion) -> 
			trainingsData.add( new TrainingsData(faceRegion, true, startWeightFace) )
		);
		nonFaceRegions.forEach((nonFaceRegion) ->
			trainingsData.add( new TrainingsData(nonFaceRegion, false, startWeightNonFace) )
		);
	}

	/**
	 * 
	 * @param strongClassifier
	 */
	public void train(StrongClassifierMJ strongClassifier) {
		List<ImagePatternClassifier> classifierList = strongClassifier.getWeakClassifierList();
		
		// TODO (PJ): For t = 1, ..., T Loop. Bisher feste Anzahl. 
		// Sollte ... Bis kein Fehler mehr? Ich bin mir nicht so recht sicher wie das abbricht!
		for (int t = 0; t < 3; t++) {
			
			// save the lowest errorRate, classifier index and test results (needed for update of test data weights)
			double lowestErrorRate 			= Double.MAX_VALUE;
			boolean[] lowestErrorResults 	= new boolean[this.trainingsData.size()];
			int lowestErrorClassifierIndex	= -1; 
		
			// Normalize the test image weights
			normalizeTestDataWeights(); // ... Step 1
			
			// Test each Weak-Classifier ...
			for (int ci = 0; ci < classifierList.size(); ci++) {
				ImagePatternClassifier classifier = classifierList.get(ci);
				
				
				// save the current errorRate and test results
				double currentErrorRate  = 0.0;
				boolean[] currentResults = new boolean[this.trainingsData.size()]; // save which images got an error
				
				// ... with each image
				for (TrainingsData trainingsImage : this.trainingsData) {
					currentResults[ci] = isCorrect(classifier, trainingsImage);
					if (!currentResults[ci]) {
						currentErrorRate += trainingsImage.getWeight(); // is error, add error
						// TODO (PJ) Die Formel aus der Folie sieht irgendwie komplizierter aus, aber mit der Tabelle scheint das Sinn zu machen
					}
				} // ... Step 4
				
				
				// if the current error rate is less then the lowest one, take the current one as the best
				if (lowestErrorRate > currentErrorRate) {
					lowestErrorRate 			= currentErrorRate;
					lowestErrorResults 			= currentResults;
					lowestErrorClassifierIndex 	= ci;
				} // ... Step 3
			}
			
			updateTestDataWeights(lowestErrorRate, lowestErrorResults); // ... Step 4
			
		}
	}
	
	/* ****************************************************************************************
	 * 							PRIVATE
	 * ****************************************************************************************/
	
	private boolean isCorrect(ImagePatternClassifier classifier, TrainingsData trainingsImage) {
		// match the current classifier with the current test image rect
		double match = classifier.matchAt(image, trainingsImage.getRectX(), trainingsImage.getRectY());
		
		// TODO (PJ): Annahme = Wenn der Weak Classifier unter seinem Threshold liegt, ist es kein Gesicht und er liefert 0?
		// Richtig so?
		boolean noMatchButFace = (match == 0 && trainingsImage.isFace());
		boolean matchButNoFace = (match > 0 && !trainingsImage.isFace());
		
		if ( noMatchButFace || matchButNoFace ) {
			return false;
		}
		return true;
	}
	
	/**
	 * Update the weights for all images in the training data list
	 * 
	 * @param lowestErrorRate
	 * @param lowestErrorResults
	 */
	private void updateTestDataWeights(double lowestErrorRate, boolean[] results) {
		double newWeight;
		double beta;
		
		for (int i = 0; i < trainingsData.size(); i++) {
			TrainingsData trainingsImage = trainingsData.get(i);
			
			if (results[i]) { // was correct
				// ei = 0  -->  beta^(1-0)  -->  beta^1 = beta
				beta = lowestErrorRate / (1 - lowestErrorRate);
			} else { // was an error
				// ei = 1  -->  beta^(1-1)  -->  beta^0 = 1
				beta = 1;
			}
			newWeight = trainingsImage.getWeight() * beta;
			trainingsImage.setWeight(newWeight);
		}
	}
	
	/**
	 * Normalize the weights for all images in the training data list
	 */
	private void normalizeTestDataWeights() {
		double weightSum = getTrainingsDataWeightSum();
		System.out.print("BeforeNorm: " + weightSum);
		
		for (TrainingsData trainingsImage : trainingsData) {
			trainingsImage.setWeight( trainingsImage.getWeight() / weightSum );
		}
		System.out.println("AfterNorm: " + getTrainingsDataWeightSum());
	}
	
	/**
	 * Get the summed weight of all images in the training data list 
	 */
	private double getTrainingsDataWeightSum() {
		double sum = 0.0;
		for (TrainingsData trainingsImage : trainingsData) {
			sum += trainingsImage.getWeight();
		}
		return sum;
	}
	
}

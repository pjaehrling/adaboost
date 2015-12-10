/**
 * @author Nico Hezel
 */
package de.htw.cv.ue04.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import de.htw.cv.facedetection.ImagePatternClassifier;
import de.htw.cv.facedetection.IntegralImage;
import de.htw.cv.facedetection.TestImage;
import de.htw.cv.facedetection.TestImage.Subject;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public abstract class FaceDetectionBase {

	@FXML
	private ImageView leftImageView;

	@FXML
	private ImageView rightImageView;

	@FXML
	private Label runtimeLabel;

	@FXML
	private Label thresholdValue;

	@FXML
	private Slider thresholdSlider;

	@FXML
	private Slider randomClassifierSlider;

	@FXML
	private Slider additionalRegionSlider;

	@FXML
	private Button trainButton;

	@FXML
	private ComboBox<Subject> subjectSelection;

	protected ImagePatternClassifier strongClassifier;

	protected TestImage testImage;

	protected int[] sourceImagePixel;

	@FXML
	public void initialize() {

		subjectSelection.getItems().addAll(Subject.values());
		subjectSelection.setValue(Subject.PERSON);
		subjectSelection.setOnAction(this::loadImage);
		loadImage(null);
		
		// aktuallisiere das Label wenn sich der Slider ändert
		thresholdSlider.valueProperty().addListener((ChangeListener<Number>) (ov, oldVal, newVal) -> {
			thresholdValue.setText(String.format("%.2f Threshold", newVal));
		});

		// finden keine Slideränderungen mehr statt -> starte den Viola Jones Algorithmus
		thresholdSlider.valueChangingProperty().addListener((ChangeListener<Boolean>) (ov, oldVal, newVal) -> {
			if (newVal == false)
				runMethod(null);
		});

		randomClassifierSlider.valueChangingProperty().addListener(this::randClassifierChange);
		additionalRegionSlider.valueChangingProperty().addListener(this::additionalRegionChange);
	}

	@FXML
	public void loadImage(ActionEvent event) {
		Subject subject = subjectSelection.getSelectionModel().getSelectedItem();
		File file = new File(""+subject.toString().toLowerCase()+".jpg");
		leftImageView.setImage(new Image(file.toURI().toString()));
		Image fxImage = leftImageView.getImage();
		sourceImagePixel = imageToPixel(fxImage);
			
		IntegralImage ii = createIntegralImage(sourceImagePixel, (int) fxImage.getWidth(), (int) fxImage.getHeight());
		testImage = TestImage.createTestImage(ii, subject);
		testImage.addNonFaceRegions(additionalRegionSlider.valueProperty().intValue());
		
		// manueller und trainierter strong classifier sind zunächst gleich
		strongClassifier = createStrongClassifier(randomClassifierSlider.valueProperty().intValue());
		
		// setzte das Trainings-Label zurück
		trainButton.setText("Train (not trained)");
	}

	/**
	 * Erstelle aus den Bildpixeln ein Integralbild
	 * 
	 * @param px
	 * @return
	 */
	protected abstract IntegralImage createIntegralImage(int[] px, int width, int height);

	/**
	 * Change Listener für den randomClassifierSlider
	 * 
	 * @param ov
	 * @param old_val
	 * @param new_val
	 */
	protected void randClassifierChange(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
		if (new_val == false) {
			System.out.println("Random Classifier Slider change");

			strongClassifier = createStrongClassifier(randomClassifierSlider.valueProperty().intValue());

			// setzte das Trainings-Label zurück
			trainButton.setText("Train (not trained)");
		}
	}

	/**
	 * Erstelle einen neuen untrainierten StrongClassifier mit
	 * weakClassifierCount WeakClassifiert.
	 * 
	 * @param weakClassifierCount
	 * @return
	 */
	protected abstract ImagePatternClassifier createStrongClassifier(int weakClassifierCount);

	/**
	 * Change Listener für den additionalRegionSlider
	 * 
	 * @param ov
	 * @param old_val
	 * @param new_val
	 */
	protected void additionalRegionChange(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
		if (new_val == false) {

			// erzeuge mehr non-face regionen für das Training
			Subject subject = subjectSelection.getSelectionModel().getSelectedItem();
			testImage = TestImage.createTestImage(testImage.getIntegralImage(), subject);
			testImage.addNonFaceRegions(additionalRegionSlider.valueProperty().intValue());

			// setzte das Trainings-Label zurück
			trainButton.setText("Train (not trained)");
		}
	}

	@FXML
	public void runTraining(ActionEvent event) {

		System.out.println("Start training of StrongClassifier.");

		trainStrongClassifier(testImage, strongClassifier);

		// setzte das Trainings-Label zurück
		trainButton.setText("Train (trained)");
	}

	protected abstract void trainStrongClassifier(TestImage image, ImagePatternClassifier strongClassifier);

	@FXML
	public void runMethod(ActionEvent event) {

		// no images loaded
		if (leftImageView.getImage() == null)
			return;

		// get image dimensions
		int srcWidth = (int) leftImageView.getImage().getWidth();
		int srcHeight = (int) leftImageView.getImage().getHeight();
		int dstWidth = srcWidth;
		int dstHeight = srcHeight;

		// get pixels arrays
		int srcPixels[] = Arrays.copyOf(sourceImagePixel, sourceImagePixel.length);
		int dstPixels[] = new int[dstWidth * dstHeight];

		double threshold = thresholdSlider.getValue();

		long startTime = System.currentTimeMillis();
		doVoilaJones(srcPixels, srcWidth, srcHeight, dstPixels, threshold, testImage, strongClassifier);

		rightImageView.setImage(pixelToImage(dstPixels, dstWidth, dstHeight));
		leftImageView.setImage(pixelToImage(srcPixels, srcWidth, srcHeight));
		runtimeLabel.setText("Viola Jones took " + (System.currentTimeMillis() - startTime) + " ms");
	}

	protected abstract void doVoilaJones(int[] srcPixels, int srcWidth, int srcHeight, int[] dstPixels, 
			double threshold, TestImage testImage, ImagePatternClassifier strongClassifier);

	public static int[] imageToPixel(Image image) {
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		int[] pixels = new int[width * height];
		image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
		return pixels;
	}

	public static int[] imageToPixel(BufferedImage image) {
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		int[] pixels = new int[width * height];
		image.getRGB(0, 0, width, height, pixels, 0, width);
		return pixels;
	}

	public static Image pixelToImage(int[] pixels, int width, int height) {
		WritableImage wr = new WritableImage(width, height);
		PixelWriter pw = wr.getPixelWriter();
		pw.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
		return wr;
	}
}

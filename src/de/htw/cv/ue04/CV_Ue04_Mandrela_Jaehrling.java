/**
 * @author Nico Hezel
 */
package de.htw.cv.ue04;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CV_Ue04_Mandrela_Jaehrling extends Application {

	@Override
	public void start(Stage stage) throws Exception {
				
		Parent ui = new FXMLLoader(getClass().getResource("view/FaceDetectionView.fxml")).load();
		Scene scene = new Scene(ui);
		stage.setScene(scene);
		stage.setTitle("Face Detection & Ada Boost - Mandrela & Jährling");
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
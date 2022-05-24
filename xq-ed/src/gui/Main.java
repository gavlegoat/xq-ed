package gui;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/* High-level application layout:
 * +-----------+---+------------+
 * |           | M |            |
 * |   Board   | o |            |
 * |           | v |  Comments  |
 * +-----------+ e |            |
 * |    Nav    | s |            |
 * +----------------------------+
 * |     Graph    |  Analysis   |
 * |              |             |
 * +--------------+-------------+
 */

public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		URL fxml = getClass().getResource("/res/interface.fxml");
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(fxml);
		VBox topLevel = loader.<VBox>load();
		
		Scene scene = new Scene(topLevel);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

}

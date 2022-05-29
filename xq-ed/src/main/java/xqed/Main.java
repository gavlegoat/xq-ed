package xqed;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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
		URL fxml = getClass().getResource("/interface.fxml");
		Controller ctrl = new Controller();
		FXMLLoader loader = new FXMLLoader();
		loader.setController(ctrl);
		loader.setLocation(fxml);
		VBox topLevel = loader.<VBox>load();
		
		// Once FXML is loaded, the controller fields should be populated.
		ctrl.initialize(primaryStage);
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent e) {
				if (ctrl.getGameChanged()) {
					if (ctrl.showConfirmSaveDialog()) {
						e.consume();
					}
				}
			}
		});
		
		Scene scene = new Scene(topLevel);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Xiangqi Editor");
		primaryStage.show();
	}

}

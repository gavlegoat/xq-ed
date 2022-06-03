package xqed.gui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import xqed.Controller;

/**
 * This pane allows interaction with a Xiangqi engine.
 */
public class AnalysisPane extends Pane {

	/** Display the name of the loaded engine. */
	private Label engineName;
	/** The number of lines to show during analysis. */
	private int numLines;
	/** The lines returned by the engine. */
	private Label[] lines;
	/** The controller for the overall program. */
	private Controller controller;
	private Button start;
	private Button stop;
	private Button analysis;
	
	/**
	 * Construct a new analysis pane with no loaded engine or data.
	 */
	public AnalysisPane() {
		engineName = new Label("No engine loaded.");
		engineName.setFont(Font.font(20));
		numLines = 3;
		lines = new Label[numLines];
		for (int i = 0; i < numLines; i++) {
			lines[i] = new Label("Empty Label");
			lines[i].setFont(Font.font(16));
			lines[i].setPrefWidth(500);
		}
		HBox buttons = new HBox();
		start = new Button("Start");
		start.setOnAction(evt -> controller.startEngine());
		start.setDisable(true);
		stop = new Button("Stop");
		stop.setOnAction(evt -> controller.stopEngine());
		stop.setDisable(true);
		analysis = new Button("Run Analysis");
		analysis.setOnAction(evt -> controller.runAnalysis());
		analysis.setDisable(true);
		Button load = new Button("Load Engine");
		load.setOnAction(evt -> controller.loadEngine());
		Button config = new Button("Configure");
		config.setOnAction(evt -> controller.configureEngine());
		buttons.getChildren().addAll(start, stop, analysis, load, config);
		
		VBox contents = new VBox();
		contents.getChildren().addAll(engineName, buttons);
		for (int i = 0; i < numLines; i++) {
			contents.getChildren().add(lines[i]);
		}
		getChildren().add(contents);
	}
	
	/**
	 * Set the Controller of this analysis pane.
	 * @param ctrl The new controller.
	 */
	public void setController(Controller ctrl) {
		controller = ctrl;
	}
	
	public void setEngineName(String name) {
		engineName.setText(name);
	}
	
	/**
	 * Set the text in the engine output labels. The scores and text are assumed
	 * to be sorted so that the best scoring line is given first.
	 * @param scores The scores of the engine's lines.
	 * @param text The moves of the engines lines.
	 */
	public void setEngineInfo(double[] scores, String[] text, int nodes,
			int depth, double time) {
		int length = scores.length < text.length ? scores.length : text.length;
		length = length < numLines ? length : numLines;
		for (int i = 0; i < length; i++) {
			lines[i].setText(String.format("%f: %s", scores[i], text[i]));
		}
		// TODO
	}
	
	public void enableAnalysisButtons() {
		start.setDisable(false);
		stop.setDisable(false);
		analysis.setDisable(false);
	}
	
}

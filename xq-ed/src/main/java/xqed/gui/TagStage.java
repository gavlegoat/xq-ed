package xqed.gui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import xqed.Controller;

/**
 * This is a window where the user can edit the tags of a game.
 */
public class TagStage extends Stage {
	
	private GridPane contents;
	private TextField redField;
	private TextField blackField;
	private TextField eventField;
	private TextField siteField;
	private TextField roundField;
	private DatePicker dateField;
	private ToggleGroup resultField;
	private RadioButton redWins;
	private RadioButton draw;
	private RadioButton blackWins;
	private TextField timeControlField;
	private TextField terminationField;
	
	private double labelWidth;
	private Controller controller;
	
	public TagStage(Controller ctrl, HashMap<String, String> values) {
		
		labelWidth = 100;
		controller = ctrl;
		
		contents = new GridPane();
		ColumnConstraints column1 = new ColumnConstraints(labelWidth);
		ColumnConstraints column2 = new ColumnConstraints(300);
		contents.getColumnConstraints().addAll(column1, column2);
		contents.setHgap(5);
		contents.setVgap(5);
		
		Label red = new Label("Red:");
		red.setAlignment(Pos.BASELINE_RIGHT);
		red.setPrefWidth(labelWidth);
		redField = new TextField(values.getOrDefault("Red", ""));
		Label black = new Label("Black:");
		black.setAlignment(Pos.BASELINE_RIGHT);
		black.setPrefWidth(labelWidth);
		blackField = new TextField(values.getOrDefault("Black", ""));
		Label event = new Label("Event:");
		event.setPrefWidth(labelWidth);
		event.setAlignment(Pos.BASELINE_RIGHT);
		eventField = new TextField(values.getOrDefault("Event", ""));
		Label site = new Label("Site:");
		site.setPrefWidth(labelWidth);
		site.setAlignment(Pos.BASELINE_RIGHT);
		siteField = new TextField(values.getOrDefault("Site", ""));
		Label round = new Label("Round:");
		round.setPrefWidth(labelWidth);
		round.setAlignment(Pos.BASELINE_RIGHT);
		roundField = new TextField(values.getOrDefault("Round", ""));
		Label date = new Label("Date:");
		date.setPrefWidth(labelWidth);
		date.setAlignment(Pos.BASELINE_RIGHT);
		dateField = new DatePicker();
		if (values.containsKey("Date")) {
			try {
				dateField.setValue(LocalDate.parse(values.get("Date").replace('.', '-')));
			} catch (DateTimeParseException e) {
				// If we can't parse the date, we just skip it.
			}
		}
		Label result = new Label("Result:");
		result.setPrefWidth(labelWidth);
		result.setAlignment(Pos.BASELINE_RIGHT);
		HBox resultPane = new HBox();
		redWins = new RadioButton("1-0");
		draw = new RadioButton("1/2-1/2");
		blackWins = new RadioButton("0-1");
		RadioButton unknown = new RadioButton("*");
		resultField = new ToggleGroup();
		redWins.setToggleGroup(resultField);
		draw.setToggleGroup(resultField);
		blackWins.setToggleGroup(resultField);
		unknown.setToggleGroup(resultField);
		String res = values.getOrDefault("Result", "*");
		if (res.equals("1-0")) {
			resultField.selectToggle(redWins);
		} else if (res.equals("1/2-1/2")) {
			resultField.selectToggle(draw);
		} else if (res.equals("0-1")) {
			resultField.selectToggle(blackWins);
		} else {
			resultField.selectToggle(unknown);
		}
		resultPane.getChildren().addAll(redWins, draw, blackWins, unknown);
		Label timeControl = new Label("TimeControl:");
		timeControl.setPrefWidth(labelWidth);
		timeControl.setAlignment(Pos.BASELINE_RIGHT);
		timeControlField = new TextField(values.getOrDefault("TimeControl", ""));
		Label termination = new Label("Termination:");
		termination.setPrefWidth(labelWidth);
		termination.setAlignment(Pos.BASELINE_RIGHT);
		terminationField = new TextField(values.getOrDefault("Termination", ""));
		
		Button submit = new Button("Submit");
		submit.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				HashMap<String, String> values = parseResponses();
				controller.updateTags(values);
				close();
			}
		});
		Button cancel = new Button("Cancel");
		cancel.setOnAction(evt -> close());
		
		contents.add(new Label("Standard Tags:"), 0, 0);
		contents.add(red, 0, 1);
		contents.add(redField, 1, 1);
		contents.add(black, 0, 2);
		contents.add(blackField, 1, 2);
		contents.add(event, 0, 3);
		contents.add(eventField, 1, 3);
		contents.add(site, 0, 4);
		contents.add(siteField, 1, 4);
		contents.add(round, 0, 5);
		contents.add(roundField, 1, 5);
		contents.add(date, 0, 6);
		contents.add(dateField, 1, 6);
		contents.add(result, 0, 7);
		contents.add(resultPane, 1, 7);
		contents.add(timeControl, 0, 8);
		contents.add(timeControlField, 1, 8);
		contents.add(termination, 0, 9);
		contents.add(terminationField, 1, 9);
		contents.add(submit, 0, 10);
		contents.add(cancel, 1, 10);
		
		Scene scene = new Scene(contents);
		setTitle("Edit Game Tags");
		setScene(scene);
	}
	
	private HashMap<String, String> parseResponses() {
		HashMap<String, String> values = new HashMap<>();
		if (!redField.getText().isBlank()) {
			values.put("Red", redField.getText());
		}
		if (!blackField.getText().isBlank()) {
			values.put("Black", blackField.getText());
		}
		if (!eventField.getText().isBlank()) {
			values.put("Event", eventField.getText());
		}
		if (!siteField.getText().isBlank()) {
			values.put("Site", siteField.getText());
		}
		if (!roundField.getText().isBlank()) {
			values.put("Round", roundField.getText());
		}
		if (!timeControlField.getText().isBlank()) {
			values.put("TimeControl", timeControlField.getText());
		}
		if (!terminationField.getText().isBlank()) {
			values.put("Termination", terminationField.getText());
		}
		LocalDate date = dateField.getValue();
		if (date != null) {
			String dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE).replace('-', '.');
			values.put("Date", dateString);
		}
		Toggle toggle = resultField.getSelectedToggle();
		if (toggle == redWins) {
			values.put("Result", "1-0");
		} else if (toggle == draw) {
			values.put("Result", "1/2-1/2");
		} else if (toggle == blackWins) {
			values.put("Result", "0-1");
		} else {
			values.put("Result", "*");
		}
		return values;
	}
	
}

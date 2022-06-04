package xqed.gui;

import java.util.ArrayList;

import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import xqed.Controller;

/**
 * This pane holds a graph of the position scores over time, which helps a user
 * see where mistakes were made.
 */
public class GraphPane extends Canvas {
	
	private final static int initialWidth = 500;
	private final static int initialHeight = 200;
	private ArrayList<Double> scores;
	private Popup currentPopup;
	private Controller controller;
	
	public GraphPane() {
		super(initialWidth, initialHeight);
		scores = new ArrayList<>();
		currentPopup = null;
		controller = null;
		
		setOnMouseMoved(evt -> highlightMove(evt));
		setOnMouseClicked(evt -> gotoMove(evt));
		// Redraw the graph to clear any highlighting or tooltips when the
		// mouse exits the graph.
		setOnMouseExited(evt -> mouseExitHandler(evt));
	}
	
	public void setController(Controller ctrl) {
		controller = ctrl;
	}
	
	public void drawGraph() {
		double width = getWidth();
		double height = getHeight();
		
		GraphicsContext gc = getGraphicsContext2D();
		gc.clearRect(0, 0, width, height);
		
		// Draw the axis
		gc.setStroke(Color.color(0.0,  0.0,  0.0));
		gc.strokeLine(0, height / 2, width, height / 2);
		
		// Find the maximum score for scaling
		double maxScore = 0;
		for (double s : scores) {
			if (s > maxScore) {
				maxScore = s;
			} else if (-s > maxScore) {
				maxScore = -s;
			}
		}
		gc.setFill(Color.BLACK);
		gc.fillText(String.format("%01.2f", maxScore), 10, 10);
		
		// Scale all of the scores to maxScore * height / 2
		double[] points = new double[scores.size()];
		for (int i = 0; i < scores.size(); i++) {
			points[i] = height / 2 - scores.get(i) * height / maxScore / 2;
		}
		
		double resolution = width / scores.size();
		
		// Draw the line and shade underneath it.
		Color redFill = Color.color(1.0, 0.0, 0.0, 0.6);
		Color blackFill = Color.color(0.0, 0.0, 0.0, 0.6);
		double curX = 0;
		double lastP = height / 2;
		for (double p : points) {
			double lastX = curX;
			curX += resolution;
			if ((p > height / 2 && lastP > height / 2) ||
					(p < height / 2 && lastP < height / 2)) {
				// If current and last are on the same side of the graph, we
				// need to fill a polygon between the axis and the line.
				double[] pointX = new double[4];
				double[] pointY = new double[4];
				pointX[0] = lastX;
				pointY[0] = height / 2;
				pointX[1] = lastX;
				pointY[1] = lastP;
				pointX[2] = curX;
				pointY[2] = p;
				pointX[3] = curX;
				pointY[3] = height / 2;
				if (p > height / 2) {
					gc.setFill(blackFill);
				} else {
					gc.setFill(redFill);
				}
				gc.fillPolygon(pointX, pointY, 4);
			} else if (Math.abs(p - height / 2) < 0.001 &&
					Math.abs(lastP - height / 2) < 0.001) {
				// There is nothing to fill in this case.
			} else {	
				// If current and last are on opposite sides of the line, we
				// need to figure out where we crossed zero and fill two
				// triangles.
				double score = p - height / 2;
				double lastScore = lastP - height / 2;
				double dist = lastScore / (lastScore - score);
				double crossX = lastX + resolution * dist;
				double[] pointX = new double[3];
				double[] pointY = new double[3];
				pointX[0] = lastX;
				pointY[0] = lastP;
				pointX[1] = lastX;
				pointY[1] = height / 2;
				pointX[2] = crossX;
				pointY[2] = height / 2;
				if (lastP > height / 2) {
					gc.setFill(blackFill);
				} else {
					gc.setFill(redFill);
				}
				gc.fillPolygon(pointX, pointY, 3);
				pointX[0] = curX;
				pointY[0] = height / 2;
				pointX[1] = curX;
				pointY[1] = p;
				if (p > height / 2) {
					gc.setFill(blackFill);
				} else {
					gc.setFill(redFill);
				}
				gc.fillPolygon(pointX, pointY, 3);
			}
			gc.strokeLine(lastX, lastP, curX, p);
			lastP = p;
		}

	}
	
	/**
	 * Update the graph given a new score for a particular move.
	 * @param move The move number.
	 * @param red Whether red made the given move.
	 * @param score The score of the given move.
	 */
	public void setScore(int move, boolean red, double score) {
		int index = 2 * move + (red ? 0 : 1);
		while (index >= scores.size()) {
			scores.add(0.0);
		}
		scores.set(index, score);
	}
	
	public int getMoveNum(double x) {
		if (scores.isEmpty()) {
			return 0;
		}
		double width = getWidth();
		double resolution = width / scores.size();
		int moveNum = (int) (x / resolution + 0.5);
		if (moveNum < 0) {
			moveNum = 0;
		} else if (moveNum > scores.size()) {
			moveNum = scores.size();
		}
		return moveNum;
	}
	
	private void highlightMove(MouseEvent e) {
		// Highlight the nearest move and open a tooltip with the move number,
		// the move that was played, and the score
		if (scores.isEmpty()) {
			return;
		}
		double width = getWidth();
		double height = getHeight();
		double resolution = width / scores.size();
		int move = getMoveNum(e.getX());
		if (move == 0) {
			return;
		}
		double score = 0;
		if (move > 0) {
			score = scores.get(move - 1);
		}
		drawGraph();
		GraphicsContext gc = getGraphicsContext2D();
		gc.setFill(Color.color(1.0, 1.0, 1.0, 0.4));
		gc.fillRect(move * resolution - resolution / 2, 0, resolution, height);
		if (currentPopup != null) {
			currentPopup.hide();
			currentPopup = null;
		}
		currentPopup = new Popup();
		VBox contents = new VBox();
		String text = Integer.toString((move - 1) / 2 + 1) + ". ";
		if (move % 2 == 1) {
			text += "... ";
		}
		text += controller.getMoveName(move);
		if (move == 0) {
			text = "";
		}
		contents.getChildren().add(new Label(text));
		contents.getChildren().add(new Label(String.format("%01.2f", score)));
		currentPopup.getContent().add(contents);
		Bounds bounds = getBoundsInLocal();
		Bounds screenBounds = localToScreen(bounds);
		double paneX = screenBounds.getMinX();
		double paneY = screenBounds.getMinY();
		currentPopup.show(this, paneX + e.getX(), paneY + e.getY());
	}
	
	private void gotoMove(MouseEvent e) {
		// Set the current move on the board to the clicked event.
		controller.goToMoveNum(getMoveNum(e.getX()));
	}
	
	private void mouseExitHandler(MouseEvent e) {
		if (currentPopup != null) {
			currentPopup.hide();
			currentPopup = null;
		}
		drawGraph();
	}
	
}

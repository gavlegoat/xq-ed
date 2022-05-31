package xqed.gui;

import javafx.scene.canvas.Canvas;

/**
 * This pane holds a graph of the position scores over time, which helps a user
 * see where mistakes were made.
 */
public class GraphPane extends Canvas {

	public GraphPane() {
		// TODO
	}
	
	/**
	 * Update the graph given a new score for a particular move.
	 * @param move The move number.
	 * @param red Whether red made the given move.
	 * @param score The score of the given move.
	 */
	public void setScore(int move, boolean red, double score) {
		// TODO
	}
	
}

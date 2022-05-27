package main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import xiangqi.Piece;

/**
 * Holds the list of moves for the current game, and allows the user to click
 * on a move to go to the appropriate position.
 */
public class MovePane extends Pane {
	
	/* Pane layout:
	 * +------------------------+
	 * | 1. Move1r  Move1b      |
	 * | 2. Move2r              |
	 * | > 2. Move2r' Move2b'   |
	 * |   3. Move3r' Move3b'   |
	 * | 2. ...     Move2b      |
	 * | 3. Move3r  Move3b      |
	 * | > 3. ...     Move3b'   |
	 * |   4. Move4r'           |
	 * | > 3. ...     Move3b''  |
	 * |   4. Move4r''          | 
	 * | 4. Move4r  *Move4b*    |
	 * | > 4. ...     Move4b'   |
	 * |   5. Move5r' Move5b'   |
	 * |   > 5. ...    Move5b'' |
	 * |   6. Move5r'           |
	 * | 5. Move5r              |
	 * +------------------------+
	 * where > is some graphical indicator of indentation and * * is some kind
	 * of highlighting for the current move.
	 */
	
	public static class StringTree {
		private Optional<String> current;
		private Optional<StringTree> parent;
		private ArrayList<StringTree> children;
		
		public StringTree() {
			setCurrent(Optional.empty());
			setParent(Optional.empty());
			setChildren(new ArrayList<>());
		}
		
		public StringTree(String m, StringTree p, ArrayList<StringTree> ch) {
			setCurrent(Optional.of(m));
			setParent(Optional.of(p));
			setChildren(ch);
		}

		public ArrayList<StringTree> getChildren() {
			return children;
		}

		public void setChildren(ArrayList<StringTree> children) {
			this.children = children;
		}

		public Optional<StringTree> getParent() {
			return parent;
		}

		public void setParent(Optional<StringTree> parent) {
			this.parent = parent;
		}

		public Optional<String> getCurrent() {
			return current;
		}

		public void setCurrent(Optional<String> current) {
			this.current = current;
		}
		
	}
	
	private VBox mainPane;
	private int indentSize;
	private int moveWidth;
	private int moveHeight;
	private int numWidth;
	private double fontSize;
	
	public MovePane() {
		mainPane = new VBox();
		getChildren().add(mainPane);
		indentSize = 15;
		moveWidth = 60;
		moveHeight = 20;
		numWidth = 40;
		fontSize = 18;
		
		setPrefWidth(240);
	}
	
	public void drawRecurse(StringTree node, LinkedList<Integer> path,
			List<Integer> currentPath, int indentLevel, int moveNumber,
			Piece.Color color, Optional<HBox> prevRow, boolean newIndent) {
		HBox row = null;
		if (node.getCurrent().isPresent()) {
			// This condition may be false if this is the root node of the tree.
			// In that case, we just continue as normal without creating any
			// text.
			if (color == Piece.Color.RED || prevRow.isEmpty()) {
				if (prevRow.isPresent()) {
					mainPane.getChildren().add(prevRow.get());
				}
				row = new HBox();
				int x = 5;
				for (int i = 0; i < indentLevel - 1; i++) {
					x += indentSize;
				}
				if (!newIndent && indentLevel > 0) {
					x += indentSize;
				}
				Label indent = new Label();
				indent.setPrefWidth(x);
				indent.setPrefHeight(moveHeight);
				row.getChildren().add(indent);
				if (newIndent) {
					Label marker = new Label(">");
					marker.setPrefWidth(indentSize);
					marker.setPrefHeight(moveHeight);
					row.getChildren().add(marker);
				}
				
				Label number = new Label(String.format("%d.", moveNumber));
				number.setFont(Font.font(fontSize));
				number.setPrefWidth(numWidth);
				number.setPrefHeight(moveHeight);
				number.setAlignment(Pos.BASELINE_RIGHT);
				row.getChildren().add(number);
				
				if (color == Piece.Color.BLACK) {
					Label dots = new Label("...");
					dots.setFont(Font.font(fontSize));
					dots.setPrefWidth(moveWidth);
					dots.setPrefHeight(moveHeight);
					row.getChildren().add(dots);
				}
			} else {
				row = prevRow.get();
			}
			boolean pathsMatch = path.size() == currentPath.size();
			for (int i = 0; pathsMatch && i < path.size(); i++) {
				pathsMatch = pathsMatch && path.get(i) == currentPath.get(i);
			}
			Label move = new Label(node.getCurrent().get());
			if (pathsMatch) {
				String fontName = move.getFont().getName();
				move.setFont(Font.font(fontName, FontWeight.BOLD, fontSize));
			} else {
				move.setFont(Font.font(fontSize));
			}
			move.setPrefWidth(moveWidth);
			move.setPrefHeight(moveHeight);
			row.getChildren().add(move);
		}
		int nextMove = moveNumber;
		if (color == Piece.Color.BLACK) {
			nextMove++;
		}
		if (node.getChildren().isEmpty()) {
			if (row != null) {
				mainPane.getChildren().add(row);
			}
		} else {
			// Loop starting from one so that we print variations first.
			for (int i = 1; i < node.getChildren().size(); i++) {
				if (row != null) {
					mainPane.getChildren().add(row);
				}
				path.addLast(i);
				drawRecurse(node.getChildren().get(i), path, currentPath,
						indentLevel + 1, nextMove, Piece.switchColor(color),
						Optional.empty(), true);
				path.removeLast();
			}
			path.addLast(0);
			if (node.getChildren().size() == 1) {
				// If we didn't draw any variations, then we can just continue
				// as normal.
				drawRecurse(node.getChildren().get(0), path, currentPath,
						indentLevel, nextMove, Piece.switchColor(color),
						row == null ? Optional.empty() : Optional.of(row), false);
			} else {
				drawRecurse(node.getChildren().get(0), path, currentPath,
						indentLevel, nextMove, Piece.switchColor(color),
						Optional.empty(), false);
			}
			path.removeLast();
		}
	}
	
	public void redraw(StringTree moves, List<Integer> path) {
		getChildren().clear();
		mainPane = new VBox();
		LinkedList<Integer> currentPath = new LinkedList<Integer>();
		drawRecurse(moves, currentPath, path, 0, 0, Piece.Color.BLACK,
				Optional.empty(), false);
		getChildren().add(mainPane);
	}
	
}

package xqed.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import xqed.Controller;
import xqed.xiangqi.Piece;

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
	
	/**
	 * A tree of strings. This is used to represent the move list in a way which
	 * is independent of the representation of the game state. The controller is
	 * responsible for converting the logical move tree to a tree of labels.
	 */
	public static class StringTree {
		/** A text representation of a move. Empty at the root node. */
		private Optional<String> current;
		/** The parent of this node in the tree. Empty at the root node. */
		private Optional<StringTree> parent;
		/** The children of this node in the tree. */
		private ArrayList<StringTree> children;
		
		/**
		 * Construct a new empty node.
		 */
		public StringTree() {
			setCurrent(Optional.empty());
			setParent(Optional.empty());
			setChildren(new ArrayList<>());
		}
		
		/**
		 * Construct a new node with the given data.
		 * @param m The move label at this node.
		 * @param p The parent of this node.
		 * @param ch The children of this node.
		 */
		public StringTree(String m, StringTree p, ArrayList<StringTree> ch) {
			setCurrent(Optional.of(m));
			setParent(Optional.of(p));
			setChildren(ch);
		}

		/**
		 * Get the children of this node.
		 * @return The children of this node.
		 */
		public ArrayList<StringTree> getChildren() {
			return children;
		}

		/**
		 * Set the children of this node.
		 * @param children The new children for this node.
		 */
		public void setChildren(ArrayList<StringTree> children) {
			this.children = children;
		}

		/**
		 * Get the parent node for this node.
		 * @return The parent of this node.
		 */
		public Optional<StringTree> getParent() {
			return parent;
		}

		/**
		 * Set the parent node for this node.
		 * @param parent The new parent for this node.
		 */
		public void setParent(Optional<StringTree> parent) {
			this.parent = parent;
		}

		/**
		 * Get the move label at the current node,
		 * @return The labe of this node.
		 */
		public Optional<String> getCurrent() {
			return current;
		}

		/**
		 * Set the move label for this node.
		 * @param current The new label for this node.
		 */
		public void setCurrent(Optional<String> current) {
			this.current = current;
		}
		
	}
	
	/** The main container holding rows in the move list. */
	private VBox mainPane;
	/** The width of an indentation, used for marking variations. */
	private int indentSize;
	/** The width of a move label. */
	private int moveWidth;
	/** The height of a row in the move list. */
	private int moveHeight;
	/** The width of the move number label. */
	private int numWidth;
	/** The font size to use for moves. */
	private double fontSize;
	private int initialIndent;
	
	/** The controller interacting with this move list. */
	private Controller controller;
	
	/**
	 * Create a new empty move pane.
	 */
	public MovePane() {
		mainPane = new VBox();
		getChildren().add(mainPane);
		initialIndent = 20;
		indentSize = 15;
		moveWidth = 60;
		moveHeight = 20;
		numWidth = 20;
		fontSize = 18;
		
		// Set the desired width of this move pane.
		setPrefWidth(240);
	}
	
	/**
	 * Set the controller interacting with this pane. This is separate from the
	 * constructor to make FXML setup easier.
	 * @param ctrl The new controller.
	 */
	public void setController(Controller ctrl) {
		controller = ctrl;
	}
	
	/**
	 * Draw a move list. This is a helper function which traverses a StringTree
	 * and adds each node in the tree to the move list.
	 * @param node StringTree to traverse.
	 * @param path The path in the tree to this node.
	 * @param currentPath The path in the tree to the current board position.
	 * @param indentLevel The current number of indentations needed.
	 * @param moveNumber The number of the current move.
	 * @param color The color of the player who made the current move.
	 * @param prevRow The previously used row, if applicable.
	 * @param newIndent If true, this is a newly indented line and needs a marker.
	 */
	public void drawRecurse(StringTree node, LinkedList<Integer> path,
			List<Integer> currentPath, int indentLevel, int moveNumber,
			Piece.Color color, Optional<HBox> prevRow, boolean newIndent) {
		HBox row = null;
		if (node.getCurrent().isPresent()) {
			// This condition may be false if this is the root node of the tree.
			// In that case, we just continue as normal without creating any
			// text.
			if (color == Piece.Color.RED || prevRow.isEmpty()) {
				// In these cases, we need to start a new row.
				if (prevRow.isPresent()) {
					// If we start a new row, we should add the previous row to
					// the display.
					mainPane.getChildren().add(prevRow.get());
				}
				row = new HBox();
				// Add indentation to the new row.
				int x = initialIndent;
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
					// If this is a new indent, we need to add a marker.
					Label marker = new Label(">");
					marker.setFont(Font.font(fontSize));
					marker.setPrefWidth(indentSize);
					marker.setPrefHeight(moveHeight);
					marker.setAlignment(Pos.BASELINE_RIGHT);
					row.getChildren().add(marker);
				}
				
				// Each new row needs a move number.
				Label number = new Label(String.format("%d.", moveNumber));
				number.setFont(Font.font(fontSize));
				number.setPrefWidth(numWidth);
				number.setPrefHeight(moveHeight);
				row.getChildren().add(number);
				
				// If this is a new row and the color is black, then that means
				// the move was interupted by a variation. We should add an
				// ellipses to indicate the missing red move on this row.
				if (color == Piece.Color.BLACK) {
					Label dots = new Label("...");
					dots.setFont(Font.font(fontSize));
					dots.setPrefWidth(moveWidth);
					dots.setPrefHeight(moveHeight);
					row.getChildren().add(dots);
				}
			} else {
				// If the above doesn't apply, then this is a black move without
				// a variation on the previous move, so we should put it on the
				// same row as the previous move.
				row = prevRow.get();
			}
			// Check whether the path of this node is equal to the path of the
			// current board position.
			boolean pathsMatch = path.size() == currentPath.size();
			for (int i = 0; pathsMatch && i < path.size(); i++) {
				pathsMatch = pathsMatch && path.get(i) == currentPath.get(i);
			}
			// Display the move.
			Label move = new Label(node.getCurrent().get());
			if (pathsMatch) {
				// If the paths match, highlight this move in the move list.
				String fontName = move.getFont().getName();
				move.setFont(Font.font(fontName, FontWeight.BOLD, fontSize));
			} else {
				move.setFont(Font.font(fontSize));
			}
			move.setPrefWidth(moveWidth);
			move.setPrefHeight(moveHeight);
			// We need to copy the path in order to store it in the mouse event
			// handler. We suppress the unchecked cast warning because we are
			// always working with a concrete type and just cloning, so this is
			// actually a safe cast.
			@SuppressWarnings("unchecked")
			LinkedList<Integer> capturedPath = (LinkedList<Integer>) path.clone();
			move.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					controller.goToMove(capturedPath);
				}
			});
			row.getChildren().add(move);
		}
		// Update the move number for recursive calls.
		int nextMove = moveNumber;
		if (color == Piece.Color.BLACK) {
			nextMove++;
		}
		// If this is a leaf node, we need to add the current row. Otherwise,
		// the next recursive call will handle this.
		if (node.getChildren().isEmpty()) {
			if (row != null) {
				mainPane.getChildren().add(row);
			}
		} else {
			boolean useRow = true;
			if (node.getParent().isPresent() && node == node.getParent().get().getChildren().get(0)) {
				// We draw parent node variations in order to write the first move
				// of the main line first.
				StringTree par = node.getParent().get();
				useRow = par.getChildren().size() == 1;
				int pathEnd = path.removeLast();
				// Loop starting from one so that we print variations first.
				for (int i = 1; i < par.getChildren().size(); i++) {
					// Variations always start on a new row, so we can add this row
					// and pass an empty optional down.
					if (row != null) {
						mainPane.getChildren().add(row);
					}
					// Update the path
					path.addLast(i);
					drawRecurse(par.getChildren().get(i), path, currentPath,
							indentLevel + 1, moveNumber, color,
							Optional.empty(), true);
					// Un-update the path for future iterations
					path.removeLast();
				}
				path.addLast(pathEnd);
			}
			path.addLast(0);
			if (useRow) {
				// If we didn't draw any variations, then we can just continue
				// as normal.
				drawRecurse(node.getChildren().get(0), path, currentPath,
						indentLevel, nextMove, Piece.switchColor(color),
						row == null ? Optional.empty() : Optional.of(row), false);
			} else {
				// Otherwise, we should always start a new row
				drawRecurse(node.getChildren().get(0), path, currentPath,
						indentLevel, nextMove, Piece.switchColor(color),
						Optional.empty(), false);
			}
			path.removeLast();
		}
	}
	
	/**
	 * Redraw the move list. This is the main function for generating the move
	 * list.
	 * @param moves The tree of labels for the current game.
	 * @param path The path of the current position in the move tree.
	 */
	public void redraw(StringTree moves, List<Integer> path) {
		// Remove the existing move list.
		getChildren().clear();
		mainPane = new VBox();
		LinkedList<Integer> currentPath = new LinkedList<Integer>();
		drawRecurse(moves, currentPath, path, 0, 0, Piece.Color.BLACK,
				Optional.empty(), false);
		getChildren().add(mainPane);
	}
	
}

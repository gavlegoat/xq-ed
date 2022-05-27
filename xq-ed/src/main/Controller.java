package main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import main.MovePane.StringTree;
import xiangqi.Game;
import xiangqi.GameTree;
import xiangqi.Move;
import xiangqi.Piece;
import xiangqi.Position;

/**
 * The controller is responsible for communicating between GUI components and
 * back end data as well as maintaining a consistent state across different GUI
 * elements.
 */
public class Controller {

	/** The game currently being displayed. */
	private Game game;
	/** The game tree node corresponding to the current board position. */
	private GameTree current;
	
	/**
	 * Indicates whether a piece is currently being moved (either by a two-click
	 * method or click-and-drag).
	 */
	private boolean movingPiece;
	/**
	 * If a piece is currently moving, this keeps track of the possible
	 * destinations.
	 */
	private ArrayList<Pair<Integer, Integer>> movingList;
	/** The file where the current move started. */
	private int startFile;
	/** The rank where the current move started. */
	private int startRank;
	
	/** The pane where a user can enter comments on the current position. */
	public TextArea commentArea;
	/** The pane where the move list is displayed. */
	public MovePane movePane;
	/** The pane where the board is displayed. */
	public BoardPane boardPane;
	
	/**
	 * Construct a new controller with a fresh game.
	 */
	public Controller() {
		game = new Game();
		current = game.getGameTree();
		movingPiece = false;
		startFile = -1;
		startRank = -1;
	}
	
	/**
	 * Called after the fields are populated to set everything up. This is
	 * separated from the constructor to work nicely with FXML.
	 */
	public void initialize() {
		boardPane.drawBoard(new Position());
	}
	
	private StringTree traverseGameTree(GameTree root, Optional<StringTree> parent) {
		StringTree cur = new StringTree();
		if (root.hasMove()) {
			String move = root.getMove().write(root.getParent().getPosition(),
					Move.MoveFormat.RELATIVE);
			cur.setCurrent(Optional.of(move));
		}
		ArrayList<StringTree> children = new ArrayList<>();
		cur.setParent(parent);
		for (GameTree ch : root.getVariations()) {
			children.add(traverseGameTree(ch, Optional.of(cur)));
		}
		cur.setChildren(children);
		return cur;
	}
	
	/**
	 * Get a tree of strings representing the current moves, suitable for
	 * displaying in the move pane. Also returns the path from the root node to
	 * the current node as a list of integers, where each element of the list
	 * is an index into the children of the corresponding tree node.
	 * @return A tree of move notation and a path to the current node.
	 */
	private Pair<StringTree, LinkedList<Integer>> getCurrentMoveNames() {
		LinkedList<Integer> path = new LinkedList<>();
		GameTree root = current;
		GameTree pathNode = current;
		while (root.hasParent()) {
			root = root.getParent();
			int index = 0;
			for (GameTree ch : root.getVariations()) {
				// ch is the same object as current, so we can compare with ==
				if (ch == pathNode) {
					path.addFirst(index);
					pathNode = pathNode.getParent();
					break;
				}
				index += 1;
			}
		}
		StringTree names = traverseGameTree(root, Optional.empty());
		return new Pair<>(names, path);
	}
	
	/**
	 * Update all of the relevant areas that change when the current position
	 * changes.
	 */
	private void updateAll() {
		boardPane.drawBoard(current.getPosition());
		commentArea.setText(current.getComment());
		Pair<StringTree, LinkedList<Integer>> p = getCurrentMoveNames();
		StringTree moveNames = p.getKey();
		LinkedList<Integer> path = p.getValue();
		movePane.redraw(moveNames, path);
	}
	
	/**
	 * Move to the beginning of the current game.
	 */
	@FXML
	public void goToBeginning() {
		while (current.hasParent()) {
			current = current.getParent();
		}
		updateAll();
	}
	
	/**
	 * Move one move backward in the current game.
	 */
	@FXML
	public void goBack() {
		if (current.hasParent()) {
			current = current.getParent();
		}
		updateAll();
	}
	
	/**
	 * Move one move forward along the main line in the current game.
	 */
	@FXML
	public void goForward() {
		if (current.hasContinuation()) {
			current = current.getMainContinuation();
		}
		updateAll();
	}
	
	/**
	 * Go to the end of the main line of the current game.
	 */
	@FXML
	public void goToEnd() {
		while (current.hasContinuation()) {
			current = current.getMainContinuation();
		}
		updateAll();
	}
	
	/**
	 * Ask the user to save if necessary then exit.
	 */
	@FXML
	public void checkAndExit() {
		Platform.exit();
	}
	
	/**
	 * Make a move and update all fields as appropriate.
	 * @param m The move to make.
	 */
	private void makeMove(Move m) {
		Position newPos = current.getPosition().clone();
		newPos.clearPiece(m.getFromSquare().getKey(), m.getFromSquare().getValue());
		newPos.setPiece(m.getToSquare().getKey(), m.getToSquare().getValue(), m.getPiece());
		GameTree newNode = new GameTree(newPos, current, m);
		current.addVariation(newNode);
		current = newNode;
	}
	
	/**
	 * Generate and display legal moves if the user clicks on a piece.
	 * @param e The clicking event.
	 */
	@FXML
	public void handleClick(MouseEvent e) {
		Pair<Integer, Integer> square = boardPane.getSquareFromPixels(e.getX(), e.getY());
		int file = square.getKey();
		int rank = square.getValue();
		Position pos = current.getPosition();
		// If a piece is currently moving, then this click should either
		// complete or cancel that move.
		if (movingPiece) {
			for (Pair<Integer, Integer> move : movingList) {
				if (file == move.getKey() && rank == move.getValue()) {
					Piece p = pos.pieceAt(startFile, startRank);
					Move m = new Move(p, new Pair<>(startFile, startRank), move);
					makeMove(m);
					break;
				}
			}
			movingPiece = false;
			startFile = -1;
			startRank = -1;
			updateAll();
			return;
		}
		// Otherwise, this click should start a move.
		if (pos.hasPieceAt(file, rank) &&
				pos.pieceAt(file, rank).getColor() == current.getPlayerToMove()) {
			ArrayList<Pair<Integer, Integer>> moves =
					current.getPosition().getMovesFrom(file, rank);
			movingList = moves;
			boardPane.markSquares(moves);
			startFile = file;
			startRank = rank;
			movingPiece = true;
		}
	}
	
	/**
	 * Pick up a piece and display legal moves.
	 * @param e The dragging event.
	 */
	@FXML
	public void handleDragStart(MouseEvent e) {
		Pair<Integer, Integer> square = boardPane.getSquareFromPixels(e.getX(), e.getY());
		int file = square.getKey();
		int rank = square.getValue();
		Position pos = current.getPosition();
		if (pos.hasPieceAt(file, rank) &&
				pos.pieceAt(file, rank).getColor() == current.getPlayerToMove()) {
			ArrayList<Pair<Integer, Integer>> moves =
					current.getPosition().getMovesFrom(file, rank);
			boardPane.setMovingPiece(pos.pieceAt(file, rank));
			boardPane.setMovingX(e.getX());
			boardPane.setMovingY(e.getY());
			boardPane.markSquares(moves);
			boardPane.drawMovingPiece();
			movingList = moves;
			startFile = file;
			startRank = rank;
			movingPiece = true;
		}
	}
	
	/**
	 * Redraw the board pane as a piece is dragged.
	 * @param e The dragging event.
	 */
	@FXML
	public void handleDrag(MouseEvent e) {
		if (!movingPiece) {
			return;
		}
		Position pos = current.getPosition().clone();
		pos.clearPiece(startFile, startRank);
		boardPane.drawBoard(pos);
		boardPane.markSquares(movingList);
		boardPane.setMovingX(e.getX());
		boardPane.setMovingY(e.getY());
		boardPane.drawMovingPiece();
	}
	
	/**
	 * Put down the picked up piece.
	 * @param e The dragging event.
	 */
	@FXML
	public void handleDragEnd(MouseDragEvent e) {
		if (!movingPiece) {
			return;
		}
		Pair<Integer, Integer> square = boardPane.getSquareFromPixels(e.getX(), e.getY());
		int file = square.getKey();
		int rank = square.getValue();
		Position pos = current.getPosition();
		ArrayList<Pair<Integer, Integer>> moves =
				current.getPosition().getMovesFrom(startFile, startRank);
		for (Pair<Integer, Integer> move : moves) {
			if (file == move.getKey() && rank == move.getValue()) {
				Piece p = pos.pieceAt(startFile, startRank);
				Move m = new Move(p, new Pair<>(startFile, startRank), move);
				makeMove(m);
				break;
			}
		}
		movingPiece = false;
		startFile = -1;
		startRank = -1;
		boardPane.setMovingPiece(new Piece());
		boardPane.setMovingX(-1);
		boardPane.setMovingY(-1);
		boardPane.drawBoard(current.getPosition());
	}
	
	/**
	 * Update the comment text for the current position.
	 * @param e The key event.
	 */
	@FXML
	public void updateComment(KeyEvent e) {
		current.setComment(commentArea.getText());
	}
}

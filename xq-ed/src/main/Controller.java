package main;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
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

	/**
	 * The game currently being displayed.
	 */
	private Game game;
	private GameTree current;
	
	private boolean movingPiece;
	private ArrayList<Pair<Integer, Integer>> movingList;
	private int startFile;
	private int startRank;
	
	public TextArea commentArea;
	public MovePane movePane;
	public BoardPane boardPane;
	
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
	
	/**
	 * Move to the beginning of the current game.
	 */
	@FXML
	public void goToBeginning() {
		System.out.println("Beginning");
		// TODO
	}
	
	/**
	 * Move one move backward in the current game.
	 */
	@FXML
	public void goBack() {
		System.out.println("Back");
		// TODO
	}
	
	/**
	 * Move one move forward along the main line in the current game.
	 */
	@FXML
	public void goForward() {
		System.out.println("Forward");
		// TODO
	}
	
	/**
	 * Go to the end of the main line of the current game.
	 */
	@FXML
	public void goToEnd() {
		System.out.println("End");
		// TODO
	}
	
	/**
	 * Ask the user to save if necessary then exit.
	 */
	@FXML
	public void checkAndExit() {
		Platform.exit();
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
		if (movingPiece) {
			for (Pair<Integer, Integer> move : movingList) {
				if (file == move.getKey() && rank == move.getValue()) {
					Piece p = pos.pieceAt(startFile, startRank);
					Move m = new Move(p, new Pair<>(startFile, startRank), move);
					Position newPos = pos.clone();
					newPos.clearPiece(startFile, startRank);
					newPos.setPiece(file, rank, p);
					current = new GameTree(newPos, current, m);
					break;
				}
			}
			movingPiece = false;
			startFile = -1;
			startRank = -1;
			boardPane.drawBoard(current.getPosition());
			return;
		}
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
				Position newPos = pos.clone();
				newPos.clearPiece(startFile, startRank);
				newPos.setPiece(file, rank, p);
				current = new GameTree(newPos, current, m);
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
	 * Move forward or backward with the forward and back keys.
	 * @param e The key event.
	 */
	@FXML
	public void handleKey(KeyEvent e) {
		// TODO
	}
}

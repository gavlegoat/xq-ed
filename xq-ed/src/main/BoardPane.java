package main;

import java.io.File;
import java.net.URL;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import xiangqi.Piece;
import xiangqi.Position;

/**
 * Displays the board and allows the user to make moves.
 */
public class BoardPane extends Canvas {
	
	private static final int initialWidth = 450;
	private static final int initialHeight = 600;
	
	private String boardPath;
	private String piecesBase;
	
	private double boardRatio;
	
	private Image board = null;
	private Image redPawn = null;
	private Image redCannon = null;
	private Image redRook = null;
	private Image redHorse = null;
	private Image redElephant = null;
	private Image redAdvisor = null;
	private Image redKing = null;
	private Image blackPawn = null;
	private Image blackCannon = null;
	private Image blackRook = null;
	private Image blackHorse = null;
	private Image blackElephant = null;
	private Image blackAdvisor = null;
	private Image blackKing = null;
	
	private boolean imagesNeedToBeReloaded;
	private double cornerX;
	private double cornerY;
	private double squareSize;
	
	private Piece movingPiece;
	private double movingX;
	private double movingY;
	
	public BoardPane() {
		super(initialWidth, initialHeight);
		
		boardPath = getClass().getResource("/img/boards/default.png").toString();
		piecesBase = getClass().getResource("/img/pieces/chinese_traditional/").toString();

		board = new Image(boardPath.toString());
		boardRatio = board.getWidth() / board.getHeight();
		
		imagesNeedToBeReloaded = true;
		setMovingPiece(new Piece());
		setMovingX(-1);
		setMovingY(-1);
	}
	
	/**
	 * Draw a position on this board.
	 */
	public void drawBoard(Position pos) {
		GraphicsContext gc = getGraphicsContext2D();
		int width = (int) getWidth();
		int height = (int) getHeight();	
		
		double screenRatio = width / height;

		double bWidth;
		double bHeight;
		if (boardRatio < screenRatio) {
			// The size is limited by height
			bHeight = height;
			bWidth = boardRatio * bHeight;
			cornerX = (width - bWidth) / 2;
			cornerY = 0;
		} else {
			// The size is limited by width
			bWidth = width;
			bHeight = bWidth / boardRatio;
			cornerX = 0;
			cornerY = (height - bHeight) / 2;
		}
		double pSize = bWidth / 9;
		squareSize = pSize;
		
		if (imagesNeedToBeReloaded) {
			board = new Image(boardPath, bWidth, bHeight, true, true);
			redPawn = new Image(new File(piecesBase, "red_soldier.png").toString(), pSize, pSize, true, true);
			redCannon = new Image(new File(piecesBase, "red_cannon.png").toString(), pSize, pSize, true, true);
			redRook = new Image(new File(piecesBase, "red_chariot.png").toString(), pSize, pSize, true, true);
			redHorse = new Image(new File(piecesBase, "red_horse.png").toString(), pSize, pSize, true, true);
			redElephant = new Image(new File(piecesBase, "red_elephant.png").toString(), pSize, pSize, true, true);
			redAdvisor = new Image(new File(piecesBase, "red_advisor.png").toString(), pSize, pSize, true, true);
			redKing = new Image(new File(piecesBase, "red_general.png").toString(), pSize, pSize, true, true);
			blackPawn = new Image(new File(piecesBase, "black_soldier.png").toString(), pSize, pSize, true, true);
			blackCannon = new Image(new File(piecesBase, "black_cannon.png").toString(), pSize, pSize, true, true);
			blackRook = new Image(new File(piecesBase, "black_chariot.png").toString(), pSize, pSize, true, true);
			blackHorse = new Image(new File(piecesBase, "black_horse.png").toString(), pSize, pSize, true, true);
			blackElephant = new Image(new File(piecesBase, "black_elephant.png").toString(), pSize, pSize, true, true);
			blackAdvisor = new Image(new File(piecesBase, "black_advisor.png").toString(), pSize, pSize, true, true);
			blackKing = new Image(new File(piecesBase, "black_general.png").toString(), pSize, pSize, true, true);
			imagesNeedToBeReloaded = false;
		}
		
		gc.drawImage(board, cornerX, cornerY, bWidth, bHeight);
		
		for (int file = 0; file < 9; file++) {
			for (int rank = 0; rank < 10; rank++) {
				Piece p = pos.pieceAt(file, rank);
				if (p.isEmpty()) {
					continue;
				}
				Image piece = redPawn;
				if (p.getColor() == Piece.Color.RED) {
					switch (p.getType()) {
					case PAWN:
						piece = redPawn;
						break;
					case CANNON:
						piece = redCannon;
						break;
					case ROOK:
						piece = redRook;
						break;
					case HORSE:
						piece = redHorse;
						break;
					case ELEPHANT:
						piece = redElephant;
						break;
					case ADVISOR:
						piece = redAdvisor;
						break;
					case KING:
						piece = redKing;
						break;
					case EMPTY:
						// Do nothing -- this shouldn't happen
					}
				} else {
					switch (p.getType()) {
					case PAWN:
						piece = blackPawn;
						break;
					case CANNON:
						piece = blackCannon;
						break;
					case ROOK:
						piece = blackRook;
						break;
					case HORSE:
						piece = blackHorse;
						break;
					case ELEPHANT:
						piece = blackElephant;
						break;
					case ADVISOR:
						piece = blackAdvisor;
						break;
					case KING:
						piece = blackKing;
						break;
					case EMPTY:
						// Do nothing -- this shouldn't happen
					}
				}
				double px = cornerX + pSize * file;
				double py = cornerY + pSize * (rank + 1);
				
				gc.drawImage(piece, px, py, pSize, pSize);
			}
		}
	}
	
	public void drawMovingPiece() {
		if (movingPiece.isEmpty()) {
			return;
		}
		GraphicsContext gc = getGraphicsContext2D();
		Image piece = redPawn;
		if (movingPiece.getColor() == Piece.Color.RED) {
			switch (movingPiece.getType()) {
			case PAWN:
				piece = redPawn;
				break;
			case CANNON:
				piece = redCannon;
				break;
			case ROOK:
				piece = redRook;
				break;
			case HORSE:
				piece = redHorse;
				break;
			case ELEPHANT:
				piece = redElephant;
				break;
			case ADVISOR:
				piece = redAdvisor;
				break;
			case KING:
				piece = redKing;
				break;
			case EMPTY:
				// Do nothing -- this shouldn't happen
			}
		} else {
			switch (movingPiece.getType()) {
			case PAWN:
				piece = blackPawn;
				break;
			case CANNON:
				piece = blackCannon;
				break;
			case ROOK:
				piece = blackRook;
				break;
			case HORSE:
				piece = blackHorse;
				break;
			case ELEPHANT:
				piece = blackElephant;
				break;
			case ADVISOR:
				piece = blackAdvisor;
				break;
			case KING:
				piece = blackKing;
				break;
			case EMPTY:
				// Do nothing -- this shouldn't happen
			}
		}
		gc.drawImage(piece, movingX - 0.5 * squareSize, movingY - 0.5 * squareSize);
	}
	
	public Pair<Integer, Integer> getSquareFromPixels(double x, double y) {
		double normX = (x - cornerX) / squareSize;
		double normY = (y - cornerY) / squareSize - 1;
		return new Pair<>((int) normX, (int) normY);
	}
	
	public void markSquares(List<Pair<Integer, Integer>> moves) {
		GraphicsContext gc = getGraphicsContext2D();
		gc.setFill(Color.color(0.0, 1.0, 0.0, 0.5));
		for (Pair<Integer, Integer> sq : moves) {
			double x = cornerX + squareSize * (sq.getKey() + 0.5);
			double y = cornerY + squareSize * (sq.getValue() + 1.5);
			gc.fillOval(x - 0.2 * squareSize, y - 0.2 * squareSize, 0.4 * squareSize, 0.4 * squareSize);
		}
	}

	public Piece getMovingPiece() {
		return movingPiece;
	}

	public void setMovingPiece(Piece movingPiece) {
		this.movingPiece = movingPiece;
	}

	public double getMovingX() {
		return movingX;
	}

	public void setMovingX(double movingX) {
		this.movingX = movingX;
	}

	public double getMovingY() {
		return movingY;
	}

	public void setMovingY(double movingY) {
		this.movingY = movingY;
	}
	
}

package xqed.gui;

import java.io.File;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import xqed.xiangqi.Piece;
import xqed.xiangqi.Position;

/**
 * Displays the board and allows the user to make moves.
 */
public class BoardPane extends Canvas {
	
	/** The starting width of the board. */
	private static final int initialWidth = 450;
	/** The starting height of the board. */
	private static final int initialHeight = 600;
	
	/** A path to the board image. */
	private String boardPath;
	/** A path to the folder where piece images are stored. */
	private String piecesBase;
	
	/** The aspect ratio of the board. */
	private double boardRatio;
	
	/** The current board image. */
	private Image board = null;
	/** The current image for red pawns. */
	private Image redPawn = null;
	/** The current image for red cannons. */
	private Image redCannon = null;
	/** The current image for red rooks. */
	private Image redRook = null;
	/** The current image for red horses. */
	private Image redHorse = null;
	/** The current image for red elephants. */
	private Image redElephant = null;
	/** The current image for red advisors. */
	private Image redAdvisor = null;
	/** The current image for the red king. */
	private Image redKing = null;
	/** The current image for black pawns. */
	private Image blackPawn = null;
	/** The current image for black cannons. */
	private Image blackCannon = null;
	/** The current image for black rooks. */
	private Image blackRook = null;
	/** The current image for black horses. */
	private Image blackHorse = null;
	/** The current image for black elephants. */
	private Image blackElephant = null;
	/** The current image for black advisors. */
	private Image blackAdvisor = null;
	/** The current image for the black king. */
	private Image blackKing = null;
	
	/**
	 * Can be set to force reload all of the images. This may be useful if the
	 * size of the board changes because the scaling algorithm used on loading
	 * is smoother than the one used on drawing.
	 */
	private boolean imagesNeedToBeReloaded;
	
	/** The x coordinate of the top-left corner of the board. */
	private double cornerX;
	/** The y coordinate of the top-left corner of the board. */
	private double cornerY;
	/** The size of a single square. */
	private double squareSize;
	
	/** The piece currently being moved, if applicable. */
	private Piece movingPiece;
	/** The x coordinate of a piece being dragged. */
	private double movingX;
	/** The y coordinate of a piece being dragged. */
	private double movingY;
	
	/** The currently draw position. */
	private Position currentPos;
	
	/**
	 * Create a new pane.
	 */
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
		currentPos = null;
	}
	
	/**
	 * Draw a position on this board.
	 */
	public void drawBoard(Position pos) {
		currentPos = pos;
		GraphicsContext gc = getGraphicsContext2D();
		int width = (int) getWidth();
		int height = (int) getHeight();
		gc.clearRect(0, 0, width, height);
		
		double screenRatio = (double) width / height;

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
	
	/**
	 * Draw the piece being dragged, if applicable.
	 */
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
	
	/**
	 * Convert a pixel value on the pane to a logical board position.
	 * @param x The pixel x value.
	 * @param y The pixel y value.
	 * @return The (file, rank) position on the board.
	 */
	public Pair<Integer, Integer> getSquareFromPixels(double x, double y) {
		double normX = (x - cornerX) / squareSize;
		double normY = (y - cornerY) / squareSize - 1;
		return new Pair<>((int) normX, (int) normY);
	}
	
	/**
	 * Mark some positions on the board. This is primarily used to indicate
	 * legal moves after the user has selected a piece.
	 * @param points The points to mark.
	 */
	public void markSquares(List<Pair<Integer, Integer>> points) {
		GraphicsContext gc = getGraphicsContext2D();
		gc.setFill(Color.color(0.0, 1.0, 0.0, 0.5));
		for (Pair<Integer, Integer> sq : points) {
			double x = cornerX + squareSize * (sq.getKey() + 0.5);
			double y = cornerY + squareSize * (sq.getValue() + 1.5);
			gc.fillOval(x - 0.2 * squareSize, y - 0.2 * squareSize, 0.4 * squareSize, 0.4 * squareSize);
		}
	}

	/**
	 * Get the piece currently being moved.
	 * @return The currently selected piece.
	 */
	public Piece getMovingPiece() {
		return movingPiece;
	}

	/**
	 * Set the piece currently being moved.
	 * @param movingPiece The new piece to move.
	 */
	public void setMovingPiece(Piece movingPiece) {
		this.movingPiece = movingPiece;
	}

	/**
	 * Get the current pixel position of a piece being dragged.
	 * @return The x coordinate of a piece being dragged.
	 */
	public double getMovingX() {
		return movingX;
	}

	/**
	 * Set the pixel position of a piece being dragged.
	 * @param movingX The new x coordinate of the dragged piece.
	 */
	public void setMovingX(double movingX) {
		this.movingX = movingX;
	}

	/**
	 * Get the current pixel position of a piece being dragged.
	 * @return The y coordinate of a piece being dragged.
	 */
	public double getMovingY() {
		return movingY;
	}

	/**
	 * Set the pixel position of a piece being dragged.
	 * @param movingX The new x coordinate of the dragged piece.
	 */
	public void setMovingY(double movingY) {
		this.movingY = movingY;
	}
	
	/**
	 * Make this canvas resizable.
	 */
	@Override
	public boolean isResizable() {
		return true;
	}
	
	/**
	 * When the canvas is resized, redraw it.
	 */
	@Override
	public void resize(double width, double height) {
		imagesNeedToBeReloaded = true;
		if (currentPos != null) {
			drawBoard(currentPos);
		}
	}
	
	@Override
	public double maxWidth(double height) {
		return Double.MAX_VALUE;
	}
	
	@Override
	public double maxHeight(double width) {
		return Double.MAX_VALUE;
	}
	
	@Override
	public double minWidth(double height) {
		return initialWidth / 2;
	}
	
	@Override
	public double minHeight(double width) {
		return initialHeight / 2;
	}
	
	@Override
	public double prefWidth(double height) {
		return initialWidth;
	}
	
	@Override
	public double prefHeight(double width) {
		return initialHeight;
	}
}

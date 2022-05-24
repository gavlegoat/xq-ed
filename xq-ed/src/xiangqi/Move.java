package xiangqi;

import javafx.util.Pair;

/**
 * A Xiangqi move.
 */
public class Move {

	/**
	 * The piece that moved.
	 */
	private Piece piece;
	
	/**
	 * The square it moved from.
	 */
	private Pair<Integer, Integer> fromSquare;
	
	/**
	 * The square it moved to.
	 */
	private Pair<Integer, Integer> toSquare;
	
	/**
	 * Create a new move.
	 * @param p The piece that moved.
	 * @param f The square it moved from.
	 * @param t The square it moved to.
	 */
	public Move(Piece p, Pair<Integer, Integer> f, Pair<Integer, Integer> t) {
		piece = p;
		fromSquare = f;
		toSquare = t;
	}

	/**
	 * Get the destination square of this move.
	 * @return The square a piece moved to.
	 */
	public Pair<Integer, Integer> getToSquare() {
		return toSquare;
	}

	/**
	 * Get the starting square of this move.
	 * @return The square a piece moved from.
	 */
	public Pair<Integer, Integer> getFromSquare() {
		return fromSquare;
	}

	/**
	 * Get the piece that moved.
	 * @return The piece that moved.
	 */
	public Piece getPiece() {
		return piece;
	}
	
	// Move overrides hashCode to work correctly in the GameTree hash map.
	@Override
	public int hashCode() {
		// a square is a pair (i, j) with 0 <= i <= 8 and 0 <= j <= 9
		int fromSqHash = 8 * fromSquare.getKey() + 9 * fromSquare.getValue();
		int toSqHash = 8 * toSquare.getKey() + 9 * toSquare.getValue();
		int pieceIndex = piece.hashCode();
		return 90 * 90 * pieceIndex + 90 * fromSqHash + toSqHash;
	}
	
}

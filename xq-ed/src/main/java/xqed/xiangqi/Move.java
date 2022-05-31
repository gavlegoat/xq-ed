package xqed.xiangqi;

import java.util.ArrayList;

import javafx.util.Pair;

/**
 * A Xiangqi move.
 */
public class Move {
	
	/**
	 * Formats for representing moves as strings.
	 */
	public enum MoveFormat {
		/** Start point-end point format: e.g., h3e3. */
		UCCI,
		/** Algebraic notation similar to international chess: e.g., Che3. */
		ALGEBRAIC,
		/** Relative notation: e.g., R2=5. */
		RELATIVE
	}
	
	/**
	 * Convert a given coordinate to an algebraic string. For example, the top
	 * right corner of the board is i10 and the central two points are e5 and
	 * e6.
	 * @param file The file of the point.
	 * @param rank The rank of the point.
	 * @return A string representation of the point.
	 */
	public static String pointToString(int file, int rank) {
		char f = (char) ('a' + file);
		return String.format("%c%d", f, 10 - rank);
	}
	
	/**
	 * Get a character representing a particular piece. This follows the WXF
	 * standard.
	 * @param p The piece to represent
	 * @return A character representing the piece.
	 */
	public static char getPieceCode(Piece p) {
		return Character.toUpperCase(p.getCode());
	}

	/** The piece that moved. */
	private Piece piece;
	
	/** The square it moved from. */
	private Pair<Integer, Integer> fromSquare;
	
	/** The square it moved to. */
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
	
	/**
	 * Get a string representation of this move.
	 * @param pos The position before this move is executed.
	 * @param format The notation to use.
	 * @return This move represented in the given format.
	 */
	public String write(Position pos, MoveFormat format) {
		String start = pointToString(fromSquare.getKey(), fromSquare.getValue());
		String end = pointToString(toSquare.getKey(), toSquare.getValue());
		char code = getPieceCode(piece);
		switch (format) {
		case UCCI:
			return start + end;
		case ALGEBRAIC:
			// Check for captures
			boolean capture = false;
			if (pos.hasPieceAt(toSquare.getKey(), toSquare.getValue())) {
				capture = true;
			}
			String clarification = "";
			// Check if another piece of this kind could move to the end square
			for (int f = 0; f < 9; f++) {
				for (int r = 0; r < 10; r++) {
					if ((f != fromSquare.getKey() || r != fromSquare.getValue()) &&
							pos.pieceAt(f, r).equals(piece)) {
						ArrayList<Pair<Integer, Integer>> moves = pos.getMovesFrom(f, r, false);
						for (Pair<Integer, Integer> pt : moves) {
							if (pt.getKey() == toSquare.getKey() &&
									pt.getValue() == toSquare.getValue()) {
								if (f == fromSquare.getKey()) {
									clarification = String.format("%d", 10 - fromSquare.getValue());
									break;
								} else if (clarification.isEmpty()) {
									clarification = String.format("%c", 'a' + fromSquare.getKey());
								}
							}
						}
					}
				}
			}
			// Check for checkmate or stalemate. After making the move, if the
			// opponent has no moves, this is checkmate.
			Piece captured = pos.pieceAt(toSquare.getKey(), toSquare.getValue());
			pos.clearPiece(fromSquare.getKey(), fromSquare.getValue());
			pos.setPiece(toSquare.getKey(), toSquare.getValue(), piece);
			boolean hasMoves = false;
			for (int f = 0; f < 9; f++) {
				for (int r = 0; r < 10; r++) {
					if (pos.hasPieceAt(f, r) &&
							pos.pieceAt(f, r).getColor() != piece.getColor()) {
						ArrayList<Pair<Integer, Integer>> moves = pos.getMovesFrom(f, r);
						if (!moves.isEmpty()) {
							hasMoves = true;
							break;
						}
					}
				}
				if (hasMoves) {
					break;
				}
			}
			pos.setPiece(fromSquare.getKey(), fromSquare.getValue(), piece);
			pos.setPiece(toSquare.getKey(), toSquare.getValue(), captured);
			// Check for checks
			boolean check = pos.inCheck(Piece.switchColor(piece.getColor()),
				fromSquare.getKey(), fromSquare.getValue(),
				toSquare.getKey(), toSquare.getValue());
			String ret = String.format("%c", code) + clarification;
			if (capture) {
				ret += "x";
			}
			ret += end;
			if (!hasMoves) {
				ret += "#";
			} else if (check) {
				ret += "+";
			}
			return ret;
		case RELATIVE:
			char direction;
			int startFile = fromSquare.getKey() + 1;
			if (piece.getColor() == Piece.Color.RED) {
				startFile = 10 - startFile;
			}
			int endFile = toSquare.getKey() + 1;
			if (piece.getColor() == Piece.Color.RED) {
				endFile = 10 - endFile;
			}
			if (fromSquare.getValue() == toSquare.getValue()) {
				direction = '=';
			} else if ((fromSquare.getValue() < toSquare.getValue() && piece.getColor() == Piece.Color.RED) ||
					(fromSquare.getValue() > toSquare.getValue() && piece.getColor() == Piece.Color.BLACK)) {
				direction = '-';
			} else {
				direction = '+';
			}
			if (piece.getType() == Piece.Type.ADVISOR || piece.getType() == Piece.Type.ELEPHANT) {
				// These pieces always move + or - and never have ambiguity.
				return String.format("%c%d%c%d", code, startFile, direction, endFile);
			}
			if (piece.getType() == Piece.Type.KING) {
				// Kings also cannot have ambiguity since there's only one king
				// per side.
				if (direction == '=') {
					return String.format("K%d=%d", startFile, endFile);
				} else {
					return String.format("K%d%c1", startFile, direction);
				}
			}
			int rankChange = toSquare.getValue() - fromSquare.getValue();
			if (piece.getColor() == Piece.Color.RED) {
				rankChange *= -1;
			}
			// Search for pieces in tandem (on the same file).
			ArrayList<Integer> duplicates = new ArrayList<>();
			for (int r = 0; r < 10; r++) {
				if (r != fromSquare.getValue() && pos.hasPieceAt(fromSquare.getKey(), r) &&
						pos.pieceAt(fromSquare.getKey(), r).equals(piece)) {
					duplicates.add(r);
				}
			}
			if (duplicates.isEmpty()) {
				// There can't be ambiguity because there are no other pieces on
				// this file with the same type and color.
				if (direction == '=') {
					return String.format("%c%d=%d", code, startFile, endFile);
				} else {
					if (piece.getType() == Piece.Type.HORSE) {
						if (rankChange < 0) {
							return String.format("H%d-%d", startFile, endFile);
						} else {
							return String.format("H%d+%d", startFile, endFile);
						}
					} else {
						if (rankChange < 0) {
							return String.format("%c%d-%d", code, startFile, -rankChange);
						} else {
							return String.format("%c%d+%d", code, startFile, rankChange);
						}
					}
				}
			} else if (piece.getType() == Piece.Type.PAWN) {
				// Pawns are handled specially because they are the only piece
				// which can have three or more on a file.
				// We need to figure out which pawn this is in the order.
				int pawnsAhead = 0;
				for (Integer r : duplicates) {
					if (piece.getColor() == Piece.Color.RED && r < toSquare.getValue()) {
						pawnsAhead++;
					} else if (piece.getColor() == Piece.Color.BLACK && r > toSquare.getValue()) {
						pawnsAhead++;
					}
				}
				if (direction == '=') {
					return String.format("%d%d=%d", pawnsAhead + 1, startFile, endFile);
				} else if (rankChange < 0) {
					return String.format("%d%d-%d", pawnsAhead + 1, startFile, -rankChange);
				} else {
					return String.format("%d%d+%d", pawnsAhead + 1, startFile, rankChange);
				}
			}
			// Now we need to determine whether this piece is in front or behind
			// the other piece on the file.
			if (duplicates.get(0) > toSquare.getValue() && piece.getColor() == Piece.Color.RED ||
					duplicates.get(0) < toSquare.getValue() && piece.getColor() == Piece.Color.BLACK) {
				// This piece is in front.
				if (direction == '=') {
					return String.format("+%c=%d", code, endFile);
				} else if (direction == '-') {
					return String.format("+%c-%d", code, -rankChange);
				} else {
					return String.format("+%c+%d", code, rankChange);
				}
			} else {
				// This piece is behind
				if (direction == '=') {
					return String.format("-%c=%d", code, endFile);
				} else if (direction == '-') {
					return String.format("-%c-%d", code, -rankChange);
				} else {
					return String.format("-%c+%d", code, rankChange);
				}
			}
		default:
			return "";
		}
	}
	
	@Override
	public int hashCode() {
		// a square is a pair (i, j) with 0 <= i <= 8 and 0 <= j <= 9
		int fromSqHash = 8 * fromSquare.getKey() + 9 * fromSquare.getValue();
		int toSqHash = 8 * toSquare.getKey() + 9 * toSquare.getValue();
		int pieceIndex = piece.hashCode();
		return 90 * 90 * pieceIndex + 90 * fromSqHash + toSqHash;
	}
	
	@Override
	public boolean equals(Object other) {
		return hashCode() == other.hashCode();
	}
	
	@Override
	public String toString() {
		return write(null, MoveFormat.UCCI);
	}
	
}

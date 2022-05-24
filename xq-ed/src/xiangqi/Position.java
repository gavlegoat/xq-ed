package xiangqi;

import java.text.ParseException;

import javafx.util.Pair;
import xiangqi.Piece.Color;
import xiangqi.Piece.Type;

/**
 * Represents a Xiangqi board position.
 */
public class Position {
	
	/**
	 * The piece at each point on the board. The board is represented from the
	 * top down and left to right, so board[i][j] is at Black's i'th file and j
	 * ranks down from Black's back row.
	 */
	private Piece board[][];
	
	/**
	 * Generate the starting position.
	 */
	public Position() {
		board = new Piece[9][10];
		for (int f = 0; f < 9; f++) {
			for (int r = 0; r < 10; r++) {
				board[f][r] = new Piece();
			}
		}
		board[0][0] = new Piece(Piece.Color.BLACK, Piece.Type.ROOK);
		board[1][0] = new Piece(Piece.Color.BLACK, Piece.Type.HORSE);
		board[2][0] = new Piece(Piece.Color.BLACK, Piece.Type.ELEPHANT);
		board[3][0] = new Piece(Piece.Color.BLACK, Piece.Type.ADVISOR);
		board[4][0] = new Piece(Piece.Color.BLACK, Piece.Type.KING);
		board[5][0] = new Piece(Piece.Color.BLACK, Piece.Type.ADVISOR);
		board[6][0] = new Piece(Piece.Color.BLACK, Piece.Type.ELEPHANT);
		board[7][0] = new Piece(Piece.Color.BLACK, Piece.Type.HORSE);
		board[8][0] = new Piece(Piece.Color.BLACK, Piece.Type.ROOK);
		board[1][2] = new Piece(Piece.Color.BLACK, Piece.Type.CANNON);
		board[7][2] = new Piece(Piece.Color.BLACK, Piece.Type.CANNON);
		board[0][3] = new Piece(Piece.Color.BLACK, Piece.Type.PAWN);
		board[2][3] = new Piece(Piece.Color.BLACK, Piece.Type.PAWN);
		board[4][3] = new Piece(Piece.Color.BLACK, Piece.Type.PAWN);
		board[6][3] = new Piece(Piece.Color.BLACK, Piece.Type.PAWN);
		board[8][3] = new Piece(Piece.Color.BLACK, Piece.Type.PAWN);
		
		board[0][9] = new Piece(Piece.Color.RED, Piece.Type.ROOK);
		board[1][9] = new Piece(Piece.Color.RED, Piece.Type.HORSE);
		board[2][9] = new Piece(Piece.Color.RED, Piece.Type.ELEPHANT);
		board[3][9] = new Piece(Piece.Color.RED, Piece.Type.ADVISOR);
		board[4][9] = new Piece(Piece.Color.RED, Piece.Type.KING);
		board[5][9] = new Piece(Piece.Color.RED, Piece.Type.ADVISOR);
		board[6][9] = new Piece(Piece.Color.RED, Piece.Type.ELEPHANT);
		board[7][9] = new Piece(Piece.Color.RED, Piece.Type.HORSE);
		board[8][9] = new Piece(Piece.Color.RED, Piece.Type.ROOK);
		board[1][7] = new Piece(Piece.Color.RED, Piece.Type.CANNON);
		board[7][7] = new Piece(Piece.Color.RED, Piece.Type.CANNON);
		board[0][6] = new Piece(Piece.Color.RED, Piece.Type.PAWN);
		board[2][6] = new Piece(Piece.Color.RED, Piece.Type.PAWN);
		board[4][6] = new Piece(Piece.Color.RED, Piece.Type.PAWN);
		board[6][6] = new Piece(Piece.Color.RED, Piece.Type.PAWN);
		board[8][6] = new Piece(Piece.Color.RED, Piece.Type.PAWN);
	}
	
	/**
	 * Generate a new position from a FEN string. The given string should be
	 * only the part of the FEN string that represents the board (i.e., up to
	 * the first space). This string has the format r1/r2/.../r10 where each ri
	 * is a string representing the occupancy of one rank, with r1 being Black's
	 * back rank and r10 being Red's back rank. Each character of ri may be
	 * either a code representing a piece, or a digit. If it is a digit, that
	 * many points are empty before the next piece code. See {@link Position}
	 * for a list of valid piece codes. For example, the starting position is:
	 * "RNEAKAENR/9/1C5C1/P1P1P1P1P/9/9/p1p1p1p1p/1c5c1/9/rneakaenr".
	 * @param fen A (partial) FEN string representing a Xiangqi position.
	 * @throws ParseException
	 */
	public Position(String fen) throws ParseException {
		board = new Piece[9][10];
		String[] ranks = fen.split("/");
		if (ranks.length != 10) {
			throw new ParseException("Wrong number of ranks in FEN", 0);
		}
		for (int f = 0; f < 9; f++) {
			for (int r = 0; r < 10; r++) {
				board[f][r] = new Piece(Piece.Color.RED, Piece.Type.EMPTY);
			}
		}
		for (int r = 0; r < 10; r++) {
			int f = 0;
			while (f < 9) {
				if (Character.isDigit(ranks[r].charAt(f))) {
					f += Character.getNumericValue(ranks[r].charAt(f));
				} else {
					board[f][r] = new Piece(ranks[r].charAt(f));
				}
			}
		}
	}
	
	@Override
	public Position clone() {
		Position pos = new Position();
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 10; j++) {
				pos.setPiece(i, j, board[i][j]);
			}
		}
		return pos;
	}
	
	/**
	 * Remove the piece at a given point if it exists.
	 * @param file The file of the square to clear.
	 * @param rank The rank of the square to clear.
	 */
	public void clearPiece(int file, int rank) {
		board[file][rank] = new Piece();
	}
	
	/**
	 * Place a piece at a given point.
	 * @param file The file to put a piece at.
	 * @param rank The rank to put a piece at.
	 * @param piece The piece to put on the given point.
	 */
	public void setPiece(int file, int rank, Piece piece) {
		board[file][rank] = piece;
	}
	
	/**
	 * Execute a move in the given position.
	 * @param move The move to make
	 * @return The position reached after making the move.
	 */
	public Position makeMove(Move move) {
		Position ret = (Position) this.clone();
		Pair<Integer, Integer> fsq = move.getFromSquare();
		Pair<Integer, Integer> tsq = move.getToSquare();
		ret.clearPiece(fsq.getKey(), fsq.getValue());
		ret.setPiece(tsq.getKey(), tsq.getValue(), move.getPiece());
		return ret;
	}
	
}

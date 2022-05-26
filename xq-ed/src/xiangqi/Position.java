package xiangqi;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Optional;

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
	 * Place a piece at a given point.
	 * @param file The file to put a piece at.
	 * @param rank The rank to put a piece at.
	 * @param piece The piece to put on the given point.
	 */
	public void setPiece(int file, int rank, Piece piece) {
		board[file][rank] = piece;
	}
	
	/**
	 * Remove the piece at a given point if it exists.
	 * @param file The file of the square to clear.
	 * @param rank The rank of the square to clear.
	 */
	public void clearPiece(int file, int rank) {
		setPiece(file, rank, new Piece());
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
	
	/**
	 * Return the piece at a given point.
	 * @param file The file to find a piece at.
	 * @param rank The rank to find a piece at.
	 * @return The piece at the given position.
	 */
	public Piece pieceAt(int file, int rank) {
		return board[file][rank];
	}
	
	/**
	 * Determine whether there is a piece at a given point.
	 * @param file The file to check for a piece at.
	 * @param rank The rank to check for a piece at.
	 * @return True if there is a piece at the given square.
	 */
	public boolean hasPieceAt(int file, int rank) {
		return !pieceAt(file, rank).isEmpty();
	}
	
	/**
	 * Determine whether the given king is in check after the given move.
	 * @param color The color of the king to look for checks against.
	 * @param sf The starting file of the move.
	 * @param sr The starting rank of the move.
	 * @param ef The ending file of the move.
	 * @param er The ending rank of the move.
	 * @return True if the king is in check after the move.
	 */
	private boolean inCheck(Piece.Color color, int sf, int sr, int ef, int er) {
		Piece captured = pieceAt(ef, er);
		Piece moved = pieceAt(sf, sr);
		clearPiece(sf, sr);
		setPiece(ef, er, moved);
		boolean check = false;
		// We keep track of the king positions in order to deal with the flying
		// general rule.
		int rkFile = -1;
		int rkRank = -1;
		int bkFile = -1;
		int bkRank = -1;
		// Check every piece on the board
		for (int file = 0; file < 9; file++) {
			for (int rank = 0; rank < 10; rank++) {
				// Generate all move from the given square.
				ArrayList<Pair<Integer, Integer>> moves = getMovesFrom(file, rank, false);
				if (pieceAt(file, rank).equals(new Piece(Piece.Color.RED, Piece.Type.KING))) {
					rkFile = file;
					rkRank = rank;
				}
				if (pieceAt(file, rank).equals(new Piece(Piece.Color.BLACK, Piece.Type.KING))) {
					bkFile = file;
					bkRank = rank;
				}
				// If any of these moves end on the given king, this is a check.
				for (Pair<Integer, Integer> move : moves) {
					if (pieceAt(move.getKey(), move.getValue()).equals(new Piece(color, Piece.Type.KING))) {
						check = true;
						break;
					}
				}
				if (check) {
					break;
				}
			}
			if (check) {
				break;
			}
		}
		setPiece(sf, sr, moved);
		setPiece(ef, er, captured);
		// Flying general
		if (rkFile == bkFile) {
			boolean seeEachOther = true;
			for (int i = bkRank + 1; i < rkRank; i++) {
				if (hasPieceAt(rkFile, i)) {
					seeEachOther = false;
					break;
				}
			}
			if (seeEachOther) {
				check = true;
			}
		}
		return check;
	}
	
	/**
	 * Generate legal moves for a pawn at the given square.
	 * @param file The file of the pawn.
	 * @param rank The rank of the pawn.
	 * @return Legal moves from the square.
	 */
	private ArrayList<Pair<Integer, Integer>> getPawnMoves(int file, int rank) {
		ArrayList<Pair<Integer, Integer>> ret = new ArrayList<>();
		boolean crossed = false;   // Has this pawn crossed the river?
		if (pieceAt(file, rank).getColor() == Piece.Color.RED) {
			if (rank > 0) {
				// A red pawn can always move forward
				ret.add(new Pair<>(file, rank - 1));
			}
			if (rank < 5) {
				// This pawn has crossed the river
				crossed = true;
			}
		} else {
			if (rank < 9) {
				// A black pawn can always move forward.
				ret.add(new Pair<>(file, rank + 1));
			}
			if (rank >= 5) {
				// This pawn has crossed the river
				crossed = true;
			}
		}
		if (crossed) {
			// For pawns of either color that have crossed the river, they can
			// move sideways.
			if (file > 0) {
				ret.add(new Pair<>(file - 1, rank));
			}
			if (rank < 8) {
				ret.add(new Pair<>(file + 1, rank));
			}
		}
		return ret;
	}
	
	/**
	 * Get the legal cannon moves from a starting position.
	 * @param file The file of the cannon.
	 * @param rank The rank of the cannon.
	 * @return The legal moves for this cannon.
	 */
	private ArrayList<Pair<Integer, Integer>> getCannonMoves(int file, int rank) {
		ArrayList<Pair<Integer, Integer>> ret = new ArrayList<>();
		int f = file + 1;
		int r = rank;
		// Hit one becomes true for the first piece encountered.
		boolean hitOne = false;
		while (f < 9) {
			if (hasPieceAt(f, r)) {
				if (hitOne) {
					if (pieceAt(f, r).getColor() != pieceAt(file, rank).getColor()) {
						ret.add(new Pair<>(f, r));
					}
					break;
				} else {
					hitOne = true;
				}
			}
			if (!hitOne) {
				ret.add(new Pair<>(f, r));
			}
			f++;
		}
		f = file - 1;
		r = rank;
		hitOne = false;
		while (f >= 0) {
			if (hasPieceAt(f, r)) {
				if (hitOne) {
					if (pieceAt(f, r).getColor() != pieceAt(file, rank).getColor()) {
						ret.add(new Pair<>(f, r));
					}
					break;
				} else {
					hitOne = true;
				}
			}
			if (!hitOne) {
				ret.add(new Pair<>(f, r));
			}
			f--;
		}
		f = file;
		r = rank + 1;
		hitOne = false;
		while (r < 10) {
			if (hasPieceAt(f, r)) {
				if (hitOne) {
					if (pieceAt(f, r).getColor() != pieceAt(file, rank).getColor()) {
						ret.add(new Pair<>(f, r));
					}
					break;
				} else {
					hitOne = true;
				}
			}
			if (!hitOne) {
				ret.add(new Pair<>(f, r));
			}
			r++;
		}
		f = file;
		r = rank - 1;
		hitOne = false;
		while (r >= 0) {
			if (hasPieceAt(f, r)) {
				if (hitOne) {
					if (pieceAt(f, r).getColor() != pieceAt(file, rank).getColor()) {
						ret.add(new Pair<>(f, r));
					}
					break;
				} else {
					hitOne = true;
				}
			}
			if (!hitOne) {
				ret.add(new Pair<>(f, r));
			}
			r--;
		}
		return ret;
	}
	
	/**
	 * Get rook moves starting from the given square.
	 * @param file The file of the rook.
	 * @param rank The rank of the rook.
	 * @return The list of rook moves.
	 */
	private ArrayList<Pair<Integer, Integer>> getRookMoves(int file, int rank) {
		ArrayList<Pair<Integer, Integer>> ret = new ArrayList<>();
		int f = file + 1;
		int r = rank;
		while (f < 9) {
			if (hasPieceAt(f, r)) {
				if (pieceAt(f, r).getColor() != pieceAt(file, rank).getColor()) {
					ret.add(new Pair<>(f, r));
				}
				break;
			}
			ret.add(new Pair<>(f, r));
			f++;
		}
		f = file - 1;
		r = rank;
		while (f >= 0) {
			if (hasPieceAt(f, r)) {
				if (pieceAt(f, r).getColor() != pieceAt(file, rank).getColor()) {
					ret.add(new Pair<>(f, r));
				}
				break;
			}
			ret.add(new Pair<>(f, r));
			f--;
		}
		f = file;
		r = rank + 1;
		while (r < 10) {
			if (hasPieceAt(f, r)) {
				if (pieceAt(f, r).getColor() != pieceAt(file, rank).getColor()) {
					ret.add(new Pair<>(f, r));
				}
				break;
			}
			ret.add(new Pair<>(f, r));
			r++;
		}
		f = file;
		r = rank - 1;
		while (r >= 0) {
			if (hasPieceAt(f, r)) {
				if (pieceAt(f, r).getColor() != pieceAt(file, rank).getColor()) {
					ret.add(new Pair<>(f, r));
				}
				break;
			}
			ret.add(new Pair<>(f, r));
			r--;
		}
		return ret;
	}
	
	/**
	 * Get a list of horse moves starting from a given square.
	 * @param file The file of the horse.
	 * @param rank The rank of the horse.
	 * @return The moves available to a horse at the given square.
	 */
	private ArrayList<Pair<Integer, Integer>> getHorseMoves(int file, int rank) {
		ArrayList<Pair<Integer, Integer>> ret = new ArrayList<>();
		// Check if there is room too move and the horse isn't blocked
		if (file < 7 && !hasPieceAt(file + 1, rank)) {
			// Check that there is room to move and the piece at the target
			// square is empty or opponents.
			if (rank < 9 && (!hasPieceAt(file + 2, rank + 1) ||
					pieceAt(file + 2, rank + 1).getColor() !=
					pieceAt(file, rank).getColor())) {
				ret.add(new Pair<>(file + 2, rank + 1));
			}
			if (rank > 0 && (!hasPieceAt(file + 2, rank - 1) ||
					pieceAt(file + 2, rank - 1).getColor() !=
					pieceAt(file, rank).getColor())) {
				ret.add(new Pair<>(file + 2, rank - 1));
			}
		}
		if (file > 1 && !hasPieceAt(file - 1, rank)) {
			if (rank < 9 && (!hasPieceAt(file - 2, rank + 1) ||
					pieceAt(file - 2, rank + 1).getColor() !=
					pieceAt(file, rank).getColor())) {
				ret.add(new Pair<>(file - 2, rank + 1));
			}
			if (rank > 0 && (!hasPieceAt(file - 2, rank - 1) ||
					pieceAt(file - 2, rank - 1).getColor() !=
					pieceAt(file, rank).getColor())) {
				ret.add(new Pair<>(file - 2, rank - 1));
			}
		}
		if (rank < 8 && !hasPieceAt(file, rank + 1)) {
			if (file < 8 && (!hasPieceAt(file + 1, rank + 2) ||
					pieceAt(file + 1, rank + 2).getColor() !=
					pieceAt(file, rank).getColor())) {
				ret.add(new Pair<>(file + 1, rank + 2));
			}
			if (file > 0 && (!hasPieceAt(file - 1, rank + 2) ||
					pieceAt(file - 1, rank + 2).getColor() !=
					pieceAt(file, rank).getColor())) {
				ret.add(new Pair<>(file - 1, rank + 2));
			}
		}
		if (rank > 1 && !hasPieceAt(file, rank - 1)) {
			if (file < 8 && (!hasPieceAt(file + 1, rank - 2) ||
					pieceAt(file + 1, rank - 2).getColor() !=
					pieceAt(file, rank).getColor())) {
				ret.add(new Pair<>(file + 1, rank - 2));
			}
			if (file > 0 && (!hasPieceAt(file - 1, rank - 2) ||
					pieceAt(file - 1, rank - 2).getColor() !=
					pieceAt(file, rank).getColor())) {
				ret.add(new Pair<>(file - 1, rank - 2));
			}
		}
		return ret;
	}
	
	/**
	 * Get a list of moves for an elephant at the given position.
	 * @param file The file of the elephant.
	 * @param rank The rank of the elephant.
	 * @return The list of moves for the elephant.
	 */
	private ArrayList<Pair<Integer, Integer>> getElephantMoves(int file, int rank) {
		ArrayList<Pair<Integer, Integer>> ret = new ArrayList<>();
		// Check if the target square is on the board and empty or has an
		// opponents piece.
		if (file < 8 && rank < 9 && !hasPieceAt(file + 1, rank + 1) &&
				(!hasPieceAt(file + 2, rank + 2) ||
						pieceAt(file + 2, rank + 2).getColor() !=
						pieceAt(file, rank).getColor()) &&
				// Check if the target square is on the right side of the river.
				(pieceAt(file, rank).getColor() == Piece.Color.RED || rank < 3)) {
			ret.add(new Pair<>(file + 2, rank + 2));
		}
		if (file > 1 && rank < 9 && !hasPieceAt(file - 1, rank + 1) &&
				(!hasPieceAt(file - 2, rank + 2) ||
						pieceAt(file - 2, rank + 2).getColor() !=
						pieceAt(file, rank).getColor()) &&
				(pieceAt(file, rank).getColor() == Piece.Color.RED || rank < 3)) {
			ret.add(new Pair<>(file - 2, rank + 2));
		}
		if (file < 8 && rank > 1 && !hasPieceAt(file + 1, rank - 1) &&
				(!hasPieceAt(file + 2, rank - 2) ||
						pieceAt(file + 2, rank - 2).getColor() !=
						pieceAt(file, rank).getColor()) &&
				(pieceAt(file, rank).getColor() == Piece.Color.BLACK || rank > 6)) {
			ret.add(new Pair<>(file + 2, rank - 2));
		}
		if (file > 1 && rank > 1 && !hasPieceAt(file - 1, rank - 1) &&
				(!hasPieceAt(file - 2, rank - 2) ||
						pieceAt(file - 2, rank - 2).getColor() !=
						pieceAt(file, rank).getColor()) &&
				(pieceAt(file, rank).getColor() == Piece.Color.BLACK || rank > 6)) {
			ret.add(new Pair<>(file - 2, rank - 2));
		}
		return ret;
	}
	
	/**
	 * Get a list of moves for an advisor at a given position.
	 * @param file The file of the advisor.
	 * @param rank The rank of the advisor.
	 * @return A list of moves for the advisor.
	 */
	private ArrayList<Pair<Integer, Integer>> getAdvisorMoves(int file, int rank) {
		ArrayList<Pair<Integer, Integer>> ret = new ArrayList<>();
		Color color = pieceAt(file, rank).getColor();
		if (file < 5 && rank < 9 && (!hasPieceAt(file + 1, rank + 1) ||
				pieceAt(file + 1, rank + 1).getColor() != color) &&
				(color == Piece.Color.RED || rank < 2)) {
			ret.add(new Pair<>(file + 1, rank + 1));
		}
		if (file > 3 && rank < 9 && (!hasPieceAt(file - 1, rank + 1) ||
				pieceAt(file - 1, rank + 1).getColor() != color) &&
				(color == Piece.Color.RED || rank < 2)) {
			ret.add(new Pair<>(file - 1, rank + 1));
		}
		if (file < 5 && rank > 0 && (!hasPieceAt(file + 1, rank - 1) ||
				pieceAt(file + 1, rank - 1).getColor() != color) &&
				(color == Piece.Color.BLACK || rank > 7)) {
			ret.add(new Pair<>(file + 1, rank - 1));
		}
		if (file > 3 && rank > 0 && (!hasPieceAt(file - 1, rank - 1) ||
				pieceAt(file - 1, rank - 1).getColor() != color) &&
				(color == Piece.Color.BLACK || rank > 7)) {
			ret.add(new Pair<>(file - 1, rank - 1));
		}
		return ret;
	}
	
	/**
	 * Get a list of king moves from a given square.
	 * @param file The file of the king.
	 * @param rank The rank of the king.
	 * @return A list of moves for the king.
	 */
	private ArrayList<Pair<Integer, Integer>> getKingMoves(int file, int rank) {
		ArrayList<Pair<Integer, Integer>> ret = new ArrayList<>();
		Color color = pieceAt(file, rank).getColor();
		if (file < 5 && (!hasPieceAt(file + 1, rank) ||
				pieceAt(file + 1, rank).getColor() != color)) {
			ret.add(new Pair<>(file + 1, rank));
		}
		if (file > 3 && (!hasPieceAt(file - 1, rank) ||
				pieceAt(file - 1, rank).getColor() != color)) {
			ret.add(new Pair<>(file - 1, rank));
		}
		if (rank < 9 && (!hasPieceAt(file, rank + 1) ||
				pieceAt(file, rank + 1).getColor() != color) &&
				(color == Piece.Color.RED || rank < 2)) {
			ret.add(new Pair<>(file, rank + 1));
		}
		if (rank > 0 && (!hasPieceAt(file, rank - 1) ||
				pieceAt(file, rank - 1).getColor() != color) &&
				(color == Piece.Color.BLACK || rank > 7)) {
			ret.add(new Pair<>(file, rank - 1));
		}
		return ret;
	}
	
	/**
	 * Get a list of squares that the piece at the given square can move to.
	 * @param file The file of the starting square for the moves.
	 * @param rank The rank of the starting square for the moves.
	 * @param checkLegal If false, return moves that end in check.
	 * @return A list of legal moves from the given square.
	 */
	public ArrayList<Pair<Integer, Integer>> getMovesFrom(int file, int rank, boolean checkLegal) {
		ArrayList<Pair<Integer, Integer>> pseudoLegal;
		switch (pieceAt(file, rank).getType()) {
		case PAWN:
			pseudoLegal = getPawnMoves(file, rank);
			break;
		case CANNON:
			pseudoLegal = getCannonMoves(file, rank);
			break;
		case ROOK:
			pseudoLegal = getRookMoves(file, rank);
			break;
		case HORSE:
			pseudoLegal = getHorseMoves(file, rank);
			break;
		case ELEPHANT:
			pseudoLegal = getElephantMoves(file, rank);
			break;
		case ADVISOR:
			pseudoLegal = getAdvisorMoves(file, rank);
			break;
		case KING:
			pseudoLegal = getKingMoves(file, rank);
			break;
		default:
			pseudoLegal = new ArrayList<>();
		}
		if (checkLegal) {
			Piece.Color thisSideColor = pieceAt(file, rank).getColor();
			pseudoLegal.removeIf(sq -> inCheck(thisSideColor, file, rank, sq.getKey(), sq.getValue()));
		}
		return pseudoLegal;
	}
	
	/**
	 * Get a list of squares that the piece at the given square can move to.
	 * @param file The file of the starting square for the moves.
	 * @param rank The rank of the starting square for the moves.
	 * @return A list of legal moves from the given square.
	 */
	public ArrayList<Pair<Integer, Integer>> getMovesFrom(int file, int rank) {
		return getMovesFrom(file, rank, true);
	}
	
}

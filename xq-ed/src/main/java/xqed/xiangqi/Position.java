package xqed.xiangqi;

import java.text.ParseException;
import java.util.ArrayList;

import javafx.util.Pair;

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
	 * many points are empty before the next piece code. See {@link Piece}
	 * for a list of valid piece codes. For example, the starting position is:
	 * "rheakaehr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RHEAKAEHR".
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
			int i = 0;
			while (f < 9) {
				if (Character.isDigit(ranks[r].charAt(i))) {
					f += Character.getNumericValue(ranks[r].charAt(i));
				} else {
					board[f][r] = new Piece(ranks[r].charAt(i));
					f++;
				}
				i++;
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
	public boolean inCheck(Piece.Color color, int sf, int sr, int ef, int er) {
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
		setPiece(sf, sr, moved);
		setPiece(ef, er, captured);
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
		if (file < 7 && rank < 8 && !hasPieceAt(file + 1, rank + 1) &&
				(!hasPieceAt(file + 2, rank + 2) ||
						pieceAt(file + 2, rank + 2).getColor() !=
						pieceAt(file, rank).getColor()) &&
				// Check if the target square is on the right side of the river.
				(pieceAt(file, rank).getColor() == Piece.Color.RED || rank < 3)) {
			ret.add(new Pair<>(file + 2, rank + 2));
		}
		if (file > 1 && rank < 8 && !hasPieceAt(file - 1, rank + 1) &&
				(!hasPieceAt(file - 2, rank + 2) ||
						pieceAt(file - 2, rank + 2).getColor() !=
						pieceAt(file, rank).getColor()) &&
				(pieceAt(file, rank).getColor() == Piece.Color.RED || rank < 3)) {
			ret.add(new Pair<>(file - 2, rank + 2));
		}
		if (file < 7 && rank > 1 && !hasPieceAt(file + 1, rank - 1) &&
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
		Piece.Color color = pieceAt(file, rank).getColor();
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
		Piece.Color color = pieceAt(file, rank).getColor();
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
	
	/**
	 * Parse a string in UCCI format.
	 * @param move The move to parse.
	 * @param color The player who made the move.
	 * @return The move represented by the given string.
	 * @throws ParseException If the given string is malformed.
	 */
	private Move interpretMoveUCCI(String move, Piece.Color color) throws ParseException {
		int startFile = -1;
		int endFile = -1;
		int startRank = -1;
		int endRank = -1;
		if (move.charAt(0) < 'a' || move.charAt(0) > 'i') {
			throw new ParseException("Unparsable move: " + move, 0);
		}
		startFile = (int) (move.charAt(0) - 'a');
		int index = 2;
		if (move.charAt(1) == '1' && move.charAt(2) == '0') {
			startRank = 0;
			index = 3;
		} else {
			startRank = Character.getNumericValue(move.charAt(1));
			if (startRank < 1) {
				throw new ParseException("Unparsable move: " + move, 1);
			}
			startRank = 10 - startRank;
		}
		if (move.charAt(index) < 'a' || move.charAt(index) > 'i') {
			throw new ParseException("Unparsable move: " + move, index);
		}
		endFile = move.charAt(index) - 'a';
		if (move.length() == index + 3) {
			if (move.charAt(index + 1) != '1' || move.charAt(index + 2) != '0') {
				throw new ParseException("Unparsable move: " + move, index + 1);
			}
			endRank = 0;
		} else {
			endRank = Character.getNumericValue(move.charAt(index + 1));
			if (endRank < 1) {
				throw new ParseException("Unparsable move: " + move, index + 1);
			}
			endRank = 10 - endRank;
		}
		if (!hasPieceAt(startFile, startRank)) {
			throw new ParseException("Illegal move: " + move, 0);
		}
		return new Move(pieceAt(startFile, startRank),
				new Pair<>(startFile, startRank), new Pair<>(endFile, endRank));
	}
	
	/**
	 * Parse a string in algebraic format.
	 * @param move The move to parse.
	 * @param color The player who made the move.
	 * @return The move represented by the given string.
	 * @throws ParseException If the given string is malformed.
	 */
	private Move interpretMoveAlgebraic(String move, Piece.Color color) throws ParseException {
		int startFile = -1;
		int endFile = -1;
		int startRank = -1;
		int endRank = -1;
		Piece.Type pieceType = new Piece(move.charAt(0)).getType();
		int index = -1;
		if (Character.isDigit(move.charAt(move.length() - 2))) {
			// This should only happen if a piece moved to the tenth rank.
			if (move.charAt(move.length() - 2) != '1' ||
					move.charAt(move.length() - 1) != '0') {
				throw new ParseException("Unparsable move: " + move, move.length() - 2);
			}
			endRank = 0;
			index = move.length() - 3;
		} else {
			endRank = Character.getNumericValue(move.charAt(move.length() - 1));
			if (endRank < 1) {
				throw new ParseException("Unparsable move: " + move, move.length() - 1);
			}
			endRank = 10 - endRank;
			index = move.length() - 2;
		}
		if (move.charAt(index) < 'a' || move.charAt(index) > 'i') {
			throw new ParseException("Unparsable move: " + move, index);
		}
		endFile = move.charAt(index) - 'a';
		// Find the start square.
		ArrayList<Pair<Integer, Integer>> start = new ArrayList<>();
		for (int f = 0; f < 9; f++) {
			for (int r = 0; r < 10; r++) {
				if (pieceAt(f, r).getType() == pieceType) {
					ArrayList<Pair<Integer, Integer>> tmp =
							getMovesFrom(f, r);
					for (Pair<Integer, Integer> p : tmp) {
						if (endFile == p.getKey() && endRank == p.getValue()) {
							start.add(new Pair<>(f, r));
						}
					}
				}
			}
		}
		if (start.isEmpty()) {
			throw new ParseException("Unparsable move: " + move, 0);
		} else if (start.size() > 1) {
			// Figure out which of the potential starting moves is
			// the correct one.
			if (Character.isDigit(move.charAt(1))) {
				startFile = start.get(0).getKey();
				startRank = Character.getNumericValue(move.charAt(1));
				if (startRank < 1) {
					throw new ParseException("Unparsable move: " + move, 1);
				}
				startRank = 10 - startRank;
			} else {
				startRank = start.get(0).getValue();
				if (move.charAt(1) < 'a' || move.charAt(1) > 'i') {
					throw new ParseException("Unparsable move: " + move, 1);
				} else {
					startFile = move.charAt(1) - 'a';
				}
			}
		} else {
			startFile = start.get(0).getKey();
			startRank = start.get(0).getValue();
		}
		
		return new Move(new Piece(color, pieceType),
				new Pair<>(startFile, startRank), new Pair<>(endFile, endRank));
	}
	
	/**
	 * Parse a string in WXF/relative format.
	 * @param move The move to parse.
	 * @param color The player who made the move.
	 * @return The move represented by the given string.
	 * @throws ParseException If the given string is malformed.
	 */
	private Move interpretMoveRelative(String move, Piece.Color color) throws ParseException {
		int startFile = -1;
		int startRank = -1;
		int endFile = -1;
		int endRank = -1;
		Piece.Type pieceType = null;
		if ("PCRHEAK".indexOf(move.charAt(0)) == -1) {
			// This can happen in the WXF notation if there are pieces in tandem.
			if (Character.isDigit(move.charAt(0))) {
				// This only happens for pawns in tandem
				pieceType = Piece.Type.PAWN;
				int index = Character.getNumericValue(move.charAt(0));
				if (index == 0) {
					throw new ParseException("Unparsable move: " + move, 0);
				}
				startFile = Character.getNumericValue(move.charAt(1)) - 1;
				if (startFile < 0) {
					throw new ParseException("Unparsable move: " + move, 1);
				}
				int start = color == Piece.Color.RED ? 0 : 9;
				int diff = color == Piece.Color.RED ? 1 : -1;
				int i = 0;
				boolean found = false;
				for (int r = start; r >= 0 && r < 10; r += diff) {
					if (pieceAt(startFile, r).equals(new Piece(color, Piece.Type.PAWN))) {
						i++;
						if (i == index) {
							startRank = r;
							found = true;
							break;
						}
					}
				}
				if (!found) {
					throw new ParseException("Unparsable move: " + move, 0);
				}
			} else if (move.charAt(0) == '+' || move.charAt(0) == '-') {
				pieceType = new Piece(move.charAt(1)).getType();
				boolean foundOne = false;
				boolean foundTwo = false;
				for (int f = 0; f < 9; f++) {
					for (int r = 0; r < 10; r++) {
						if (pieceAt(f, r).equals(new Piece(color, pieceType))) {
							startFile = f;
							if (move.charAt(0) == '+' && color == Piece.Color.RED ||
									move.charAt(0) == '-' && color == Piece.Color.BLACK) {
								startRank = r;
								foundTwo = true;
								break;
							} else if (foundOne) {
								startRank = r;
								foundTwo = true;
								break;
							} else {
								foundOne = true;
							}
						}
					}
					if (foundTwo) {
						break;
					}
				}
				if (!foundTwo) {
					throw new ParseException("Unparsable move: " + move, 0);
				}
			} else {
				throw new ParseException("Unparsable move: " + move, 0);
			}
		} else {
			pieceType = new Piece(move.charAt(0)).getType();
			if ("+-".indexOf(move.charAt(1)) != -1) {
				// This is the same as the above case but with the
				// first two characters transposed
				boolean foundOne = false;
				boolean foundTwo = false;
				for (int f = 0; f < 9; f++) {
					for (int r = 0; r < 10; r++) {
						if (pieceAt(f, r).equals(new Piece(color, pieceType))) {
							startFile = f;
							if (move.charAt(1) == '+' && color == Piece.Color.RED ||
									move.charAt(1) == '-' && color == Piece.Color.BLACK) {
								startRank = r;
								foundTwo = true;
								break;
							} else if (foundOne) {
								startRank = r;
								foundTwo = true;
								break;
							} else {
								foundOne = true;
							}
						}
					}
					if (foundTwo) {
						break;
					}
				}
			} else if (Character.isDigit(move.charAt(1))) {
				startFile = Character.getNumericValue(move.charAt(1)) - 1;
				if (startFile < 0) {
					throw new ParseException("Unparsable move: " + move, 1);
				}
				if (color == Piece.Color.RED) {
					startFile = 8 - startFile;
				}
				boolean found = false;
				for (int r = 0; r < 10; r++) {
					if (pieceAt(startFile, r).equals(new Piece(color, pieceType))) {
						if (found) {
							throw new ParseException("Unparsable move: " + move, 0);
						} else {
							found = true;
							startRank = r;
						}
					}
				}
				if (!found) {
					throw new ParseException("Unparsable move: " + move, 0);
				}
			} else {
				throw new ParseException("Unparsable move: " + move, 1);
			}
		}
		if (move.charAt(2) == '=' || move.charAt(2) == '.') {
			// Get end square for horizontal moves
			endRank = startRank;
			endFile = Character.getNumericValue(move.charAt(3)) - 1;
			if (endFile < 0 || endFile > 8) {
				throw new ParseException("Unparsable move: " + move, 3);
			}
		} else {
			if (pieceType == Piece.Type.PAWN ||
					pieceType == Piece.Type.ROOK ||
					pieceType == Piece.Type.CANNON ||
					pieceType == Piece.Type.KING) {
				endFile = startFile;
				int change = Character.getNumericValue(move.charAt(3));
				if (change < 0) {
					throw new ParseException("Unparsable move: " + move, 3);
				}
				if ((move.charAt(2) == '+' && color == Piece.Color.BLACK) ||
						(move.charAt(2) == '-' && color == Piece.Color.RED)) {
					endRank = startRank + change;
				} else {
					endRank = startRank - change;
				}
				if (endRank < 0 || endRank > 9) {
					throw new ParseException("Unparsable move: " + move, 3);
				}
			} else if (pieceType == Piece.Type.ELEPHANT) {
				if ((move.charAt(2) == '+' && color == Piece.Color.BLACK) ||
						(move.charAt(2) == '-' && color == Piece.Color.RED)) {
					endRank = startRank + 2;
				} else {
					endRank = startRank - 2;
				}
				if ((color == Piece.Color.BLACK && (endRank < 0 || endRank > 4)) ||
						(color == Piece.Color.RED && (endRank < 5 || endRank > 9))) {
					throw new ParseException("Illegal move: " + move, 3);
				}
				endFile = Character.getNumericValue(move.charAt(3)) - 1;
				if (color == Piece.Color.RED) {
					endFile = 8 - endFile;
				}
				if (endFile < 0 || endFile > 8 || (endFile != startFile + 2 && endFile != startFile - 2)) {
					throw new ParseException("Illegal move: " + move, 0);
				}
			} else if (pieceType == Piece.Type.HORSE) {
				endFile = Character.getNumericValue(move.charAt(3)) - 1;
				if (color == Piece.Color.RED) {
					endFile = 8 - endFile;
				}
				if (endFile < 0 || endFile > 8) {
					throw new ParseException("Unparsable move: " + move, 0);
				}
				if (move.charAt(2) != '+' && move.charAt(2) != '-') {
					throw new ParseException("Unparsable move: " + move, 2);
				}
				if ((move.charAt(2) == '+' && color == Piece.Color.BLACK) ||
						(move.charAt(2) == '-' && color == Piece.Color.RED)) {
					if (endFile == startFile - 2 || endFile == startFile + 2) {
						endRank = startRank + 1;
					} else if (endFile == startFile - 1 || endFile == startFile + 1) {
						endRank = startRank + 2;
					} else {
						throw new ParseException("Unparsable move: " + move, 0);
					}
				} else {
					if (endFile == startFile - 2 || endFile == startFile + 2) {
						endRank = startRank - 1;
					} else if (endFile == startFile - 1 || endFile == startFile + 1) {
						endRank = startRank + 2;
					} else {
						throw new ParseException("Unparsable move: " + move, 0);
					}
				}
			} else if (pieceType == Piece.Type.ADVISOR) {
				if ((move.charAt(2) == '+' && color == Piece.Color.BLACK) ||
						(move.charAt(2) == '-' && color == Piece.Color.RED)) {
					endRank = startRank + 1;
				} else {
					endRank = startRank - 1;
				}
				if ((color == Piece.Color.BLACK && (endRank < 0 || endRank > 2)) ||
						(color == Piece.Color.RED && (endRank < 7 || endRank > 9))) {
					throw new ParseException("Illegal move: " + move, 3);
				}
				endFile = Character.getNumericValue(move.charAt(3)) - 1;
				if (color == Piece.Color.RED) {
					endFile = 8 - endFile;
				}
				if (endFile < 3 || endFile > 5 || (endFile != startFile + 1 && endFile != startFile - 1)) {
					throw new ParseException("Illegal move: " + move, 0);
				}
			} else {
				throw new ParseException("Unknown piece type", 0);
			}
		}
		return new Move(new Piece(color, pieceType),
				new Pair<>(startFile, startRank), new Pair<>(endFile, endRank));
	}
	
	/**
	 * Convert a string representation of a move to a Move object. The string
	 * may be in any of the move formats (relative/WXF, algebraic, or UCCI), and
	 * this method will attempt to figure out which format it is.
	 * @param move A string representation of the move.
	 * @param color The color whose turn it is.
	 * @return The move represented by the given string
	 * @throws An exception if the string does not represent a legal move.
	 */
	public Move interpretMove(String move, Piece.Color color) throws ParseException {
		if (move.length() < 3) {
			throw new ParseException("Unparsable move: " + move, 0);
		}
		// All moves end with a number, so we will strip any non-numeric
		// characters. These may be present as check or checkmate markers.
		while (!Character.isDigit(move.charAt(move.length() - 1))) {
			move = move.substring(0, move.length() - 1);
		}
		Move parsed = null;
		if (Character.isLowerCase(move.charAt(0))) {
			// UCCI moves (and only UCCI moves) start with lower-case characters
			parsed = interpretMoveUCCI(move, color);
		} else {
			for (int i = 0; i < move.length(); i++)  {
				// Algebraic moves always include a file letter.
				if ('a' <= move.charAt(i) && move.charAt(i) <= 'i') {
					parsed = interpretMoveAlgebraic(move, color);
					break;
				}
			}
			if (parsed == null) {
				parsed = interpretMoveRelative(move, color);
			}
		}
		
		int startFile = parsed.getFromSquare().getKey();
		int startRank = parsed.getFromSquare().getValue();
		int endFile = parsed.getToSquare().getKey();
		int endRank = parsed.getToSquare().getValue();
		
		ArrayList<Pair<Integer, Integer>> legal = getMovesFrom(startFile, startRank);
		for (Pair<Integer, Integer> m : legal) {
			if (endFile == m.getKey() && endRank == m.getValue()) {
				return new Move(pieceAt(startFile, startRank), new Pair<>(startFile, startRank), m);
			}
		}
		throw new ParseException("Illegal move: " + move, 0);
	}
	
	/**
	 * Generate a string representation of this position in FEN notation.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int r = 0; r < 10; r++) {
			int skip = 0;
			for (int f = 0; f < 9; f++) {
				if (hasPieceAt(f, r)) {
					if (skip > 0) {
						sb.append(skip);
						skip = 0;
					}
					sb.append(pieceAt(f, r).getCode());
				} else {
					skip++;
				}
			}
			if (skip > 0) {
				sb.append(skip);
			}
			if (r < 9) {
				sb.append('/');
			}
		}
		return sb.toString();
	}
	
}

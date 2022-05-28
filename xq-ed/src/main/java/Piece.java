package main.java;

import java.text.ParseException;

/**
 * Represents the occupancy of a point on the Xiangqi board. This may either be
 * a Xiangqi piece, represented by a type and color, or an empty square.
 */
public class Piece {
	
	/**
	 * The different types of pieces. The names are chosen to match the
	 * abbreviations used in FEN strings. There is an additional option for
	 * spaces which do not have a piece.
	 */
	public enum Type {
		PAWN, CANNON, HORSE, ROOK, KING, ADVISOR, ELEPHANT, EMPTY
	}
	
	/**
	 * The color of a piece.
	 */
	public enum Color {
		RED, BLACK
	}
	
	public static Piece.Color switchColor(Piece.Color color) {
		switch (color) {
		case RED:
			return Piece.Color.BLACK;
		default:
			return Piece.Color.RED;
		}
	}
	
	/** The color of this piece. */
	private Color color;
	/** The type of this piece. */
	private Type type;
	
	/**
	 * Create a new empty piece.
	 */
	public Piece() {
		this.setColor(Color.RED);
		this.setType(Type.EMPTY);
	}
	
	/**
	 * Construct a new piece given it's color and type.
	 * @param color The color of this piece.
	 * @param type The type of this piece.
	 */
	public Piece(Color color, Type type) {
		this.setColor(color);
		this.setType(type);
	}
	
	/**
	 * Construct a new piece from the character representation used in FEN
	 * strings. The piece code may be any of 'a' for advisor, 'c' for cannon,
	 * 'e' for elephant, 'h' for horse, 'k' for king, 'p' for pawn, 'r' for
	 * rook, or their capitalized versions. The lower case letters correspond
	 * to red pieces and the upper case letters correspond to black pieces.
	 * @param code The character representing this piece.
	 * @throws ParseException If the given character does not represent a piece.
	 */
	public Piece(char code) throws ParseException {
		switch (code) {
		case 'a':
			this.color = Color.RED;
			this.type = Type.ADVISOR;
			break;
		case 'A':
			this.color = Color.BLACK;
			this.type = Type.ADVISOR;
			break;
		case 'e':
			this.color = Color.RED;
			this.type = Type.ELEPHANT;
			break;
		case 'E':
			this.color = Color.BLACK;
			this.type = Type.ELEPHANT;
			break;
		case 'h':
			this.color = Color.RED;
			this.type = Type.HORSE;
			break;
		case 'H':
			this.color = Color.BLACK;
			this.type = Type.HORSE;
			break;
		case 'k':
			this.color = Color.RED;
			this.type = Type.KING;
			break;
		case 'K':
			this.color = Color.BLACK;
			this.type = Type.KING;
			break;
		case 'r':
			this.color = Color.RED;
			this.type = Type.ROOK;
			break;
		case 'R':
			this.color = Color.BLACK;
			this.type = Type.ROOK;
			break;
		case 'c':
			this.color = Color.RED;
			this.type = Type.CANNON;
			break;
		case 'C':
			this.color = Color.BLACK;
			this.type = Type.CANNON;
			break;
		case 'p':
			this.color = Color.RED;
			this.type = Type.PAWN;
			break;
		case 'P':
			this.color = Color.BLACK;
			this.type = Type.PAWN;
			break;
		default:
			throw new ParseException("Unrecognized piece code", 0);
		}
	}

	/**
	 * Get the color of this piece.
	 * @return The color of this piece.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Set the color of this piece.
	 * @param color The new color of the piece.
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Get the type of this piece.
	 * @return The type of this piece.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Set the type of this piece.
	 * @param type The new type of this piece.
	 */
	public void setType(Type type) {
		this.type = type;
	}
	
	/**
	 * Determine whether this piece is empty.
	 * @return True if this is an empty space.
	 */
	public boolean isEmpty() {
		return this.type == Type.EMPTY;
	}
	
	@Override
	public int hashCode() {
		int code;
		switch (type) {
		case PAWN:
			code = 0;
			break;
		case CANNON:
			code = 1;
			break;
		case ROOK:
			code = 2;
			break;
		case HORSE:
			code = 3;
			break;
		case ELEPHANT:
			code = 4;
			break;
		case ADVISOR:
			code = 5;
			break;
		case KING:
			code = 6;
			break;
		default:
			code = 7;
		}
		int colCode = color == Color.RED ? 1 : 0;
		return 8 * colCode + code;
	}
	
	public char getCode() {
		switch (type) {
		case PAWN:
			return color == Color.RED ? 'p' : 'P';
		case CANNON:
			return color == Color.RED ? 'c' : 'C';
		case ROOK:
			return color == Color.RED ? 'r' : 'R';
		case HORSE:
			return color == Color.RED ? 'h' : 'H';
		case ELEPHANT:
			return color == Color.RED ? 'e' : 'E';
		case ADVISOR:
			return color == Color.RED ? 'a' : 'A';
		case KING:
			return color == Color.RED ? 'k' : 'K';
		default:
			return '-';
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		Piece other = (Piece) obj;
		return this.getType() == other.getType() && this.getColor() == other.getColor();
	}
	
}

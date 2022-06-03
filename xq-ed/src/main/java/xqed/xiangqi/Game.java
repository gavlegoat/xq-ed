package xqed.xiangqi;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * A Xiangqi game including the game tree and metadata.
 */
public class Game {

	public final static String[] possiblePGNTags = {
			"Event", "Site", "Date", "Round", "Red", "Black", "Result",
			"TimeControl", "Termination"
	};
	
	/** The tags for this game. */
	private HashMap<String, String> tags;
	
	/** The moves of this game (including variations). */
	private GameTree gameTree;
	
	/**
	 * Create a new game with no tags and no moves.
	 */
	public Game() {
		tags = new HashMap<>();
		gameTree = new GameTree();
		addTag("Variant", "Xiangqi");
	}
	
	/**
	 * Initialize a game object from a PGN representation
	 * @param pgn A PGN representing this game.
	 */
	public Game(String pgn) throws ParseException {
		PGNLexer lexer = new PGNLexer(CharStreams.fromString(pgn));
		PGNParser parser = new PGNParser(new CommonTokenStream(lexer));
		ParseTree tree = parser.parse();
		PGNGameListener listener = new PGNGameListener();
		ParseTreeWalker.DEFAULT.walk(listener, tree);
		
		Optional<ParseException> error = listener.getError();
		if (error.isPresent()) {
			throw error.get();
		}
		Game g = listener.getGame();
		tags = g.getTags();
		gameTree = g.getGameTree();
	}
	
	/**
	 * Get the tags associated with this game.
	 * @return This game's tags.
	 */
	public HashMap<String, String> getTags() {
		return tags;
	}
	
	/**
	 * Add a new tag to this game.
	 * @param name The tag to set.
	 * @param value The value for that tag.
	 */
	public void addTag(String name, String value) {
		tags.put(name, value);
	}
	
	/**
	 * Determine whether a given tag is set.
	 * @param name The tag to check.
	 * @return True if this game has the given tag.
	 */
	public boolean hasTag(String name) {
		return tags.containsKey(name);
	}
	
	/**
	 * Get the value of a given tag.
	 * @param name The tag to look up.
	 * @return The value of the given tag
	 * @throws NoSuchElementException If the tag is not set.
	 */
	public String lookupTag(String name) throws NoSuchElementException {
		String ret = tags.get(name);
		if (ret == null) {
			throw new NoSuchElementException(name);
		}
		return ret;
	}
	
	/**
	 * Get the moves for this game.
	 * @return The game tree.
	 */
	public GameTree getGameTree() {
		return gameTree;
	}
	
	/**
	 * Set the game tree for this game.
	 * @param gt The new game tree.
	 */
	public void setGameTree(GameTree gt) {
		gameTree = gt;
	}
	
	/**
	 * Build up a move list in PGN format.
	 * @param node The current node in the game tree.
	 * @param format The move format to use.
	 * @param sb The string builder to add data to.
	 * @param moveNumber The current move number.
	 * @param newMove If true, we just started a variation.
	 * @param color The color of the side who's move this is.
	 * @param indent The level of indentation to use on new lines.
	 * @return true if we added any variations.
	 */
	private void pgnRecurse(GameTree node, Move.MoveFormat format,
			StringBuilder sb, int moveNumber, boolean newMove,
			Piece.Color color, int indent) throws ParseException {
		if (node.hasMove()) {
			if (color == Piece.Color.RED || newMove) {
				sb.append(moveNumber);
				sb.append(". ");
			}
			if (newMove && color == Piece.Color.BLACK) {
				sb.append("... ");
			}
			sb.append(node.getMove().write(node.getPosition(), format));
			sb.append(" ");
			if (!node.getComment().isBlank()) {
				sb.append("{");
				if (node.getComment().contains("{") || node.getComment().contains("}")) {
					throw new ParseException("PGN comments may not contain { or }", 0);
				}
				sb.append(node.getComment());
				sb.append("} ");
			}
		}
		if (node.getVariations().isEmpty()) {
			return;
		}
		int newMoveNumber = moveNumber;
		if (color == Piece.Color.BLACK) {
			newMoveNumber++;
		}
		boolean newLine = false;
		if (node.hasParent() && node == node.getParent().getMainContinuation()) {
			for (int i = 1; i < node.getParent().getVariations().size(); i++) {
				sb.append("\n");
				for (int j = 0; j <= indent; j++) {
					sb.append("  ");
				}
				sb.append("(");
				pgnRecurse(node.getParent().getVariations().get(i), format, sb,
						moveNumber, true, color, indent + 1);
				sb.append(")");
			}
			if (node.getParent().getVariations().size() > 1) {
				sb.append("\n");
				newLine = true;
			}
		}
		pgnRecurse(node.getVariations().get(0), format, sb, newMoveNumber,
				newLine, Piece.switchColor(color), indent);
	}
	
	/**
	 * Write the game as a PGN with moves written in the specified format.
	 * @param format The format to use for storing moves.
	 * @return A PGN representation of this game.
	 */
	public String toPGN(Move.MoveFormat format) throws ParseException {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> tag : tags.entrySet()) {
			sb.append("[");
			sb.append(tag.getKey().replace("\\", "\\\\").replace("\"", "\\\""));
			sb.append(" \"");
			sb.append(tag.getValue().replace("\\", "\\\\").replace("\"", "\\\""));
			sb.append("\"]\n");	
		}
		GameTree root = gameTree;
		while (root.hasParent()) {
			root = root.getParent();
		}
		pgnRecurse(root, format, sb, 0, true, Piece.Color.BLACK, 0);
		if (hasTag("Result")) {
			sb.append("\n");
			sb.append(lookupTag("Result"));
		}
		sb.append("\n");
		return sb.toString();
	}
	
	/**
	 * Write this game in PGN format. This version uses the UCCI move format.
	 * @return A string representation of this game in PGN format.
	 */
	public String toPGN() throws ParseException {
		return toPGN(Move.MoveFormat.UCCI);
	}
	
	public void clearTag(String key) {
		if (tags.containsKey(key)) {
			tags.remove(key);
		}
	}
}

package xiangqi;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import javafx.util.Pair;

/**
 * A Xiangqi game including the game tree and metadata.
 */
public class Game {

	/**
	 * The tags for this game.
	 */
	private ArrayList<Pair<String, String>> tags;
	
	/**
	 * The moves of this game (including variations).
	 */
	private GameTree gameTree;
	
	/**
	 * Create a new game with no tags and no moves.
	 */
	private Game() {
		tags = new ArrayList<>();
		gameTree = new GameTree();
	}
	
	/**
	 * Add a new tag to this game.
	 * @param name The tag to set.
	 * @param value The value for that tag.
	 */
	public void addTag(String name, String value) {
		tags.add(new Pair<>(name, value));
	}
	
	/**
	 * Determine whether a given tag is set.
	 * @param name The tag to check.
	 * @return True if this game has the given tag.
	 */
	public boolean hasTag(String name) {
		for (Pair<String, String> tag : tags) {
			if (tag.getKey().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get the value of a given tag.
	 * @param name The tag to look up.
	 * @return The value of the given tag
	 * @throws NoSuchElementException If the tag is not set.
	 */
	public String lookupTag(String name) throws NoSuchElementException {
		for (Pair<String, String> tag : tags) {
			if (tag.getKey().equals(name)) {
				return tag.getValue();
			}
		}
		throw new NoSuchElementException(name);
	}
	
	/**
	 * Get the moves for this game.
	 * @return The game tree.
	 */
	public GameTree getGameTree() {
		return gameTree;
	}
}

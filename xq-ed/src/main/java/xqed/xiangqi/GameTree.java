package xqed.xiangqi;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * A Xiangqi game with variations and comments.
 */
public class GameTree {
	
	/** The current board position. */
	private Position position;
	
	/** The position before this one in the tree, if it exists. */
	private Optional<GameTree> parent;
	
	/** The move that led to this position. */
	private Optional<Move> move;
	
	/** A comment for this position. */
	private String comment;
	
	/** Moves from the position. The first entry is the main continuation. */
	private ArrayList<GameTree> variations;
	
	/** The player who can move in this position. */
	private Piece.Color playerToMove;
	
	/**
	 * Construct a new game tree with the starting position and no successors.
	 */
	public GameTree() {
		position = new Position();
		parent = Optional.empty();
		move = Optional.empty();
		setComment("");
		variations = new ArrayList<>();
		playerToMove = Piece.Color.RED;
	}
	
	/**
	 * Construct a new node with a given position and parent move, but no
	 * successors.
	 * @param pos The current position.
	 * @param par The parent node in the game tree.
	 * @param m The move that led to this position.
	 */
	public GameTree(Position pos, GameTree par, Move m) {
		position = pos;
		parent = Optional.of(par);
		move = Optional.of(m);
		setComment("");
		variations = new ArrayList<>();
		playerToMove = par.getPlayerToMove() == Piece.Color.RED ? Piece.Color.BLACK : Piece.Color.RED;
	}
	
	/**
	 * Get the position at this node.
	 * @return The position at this node.
	 */
	public Position getPosition() {
		return position;
	}
	
	/**
	 * Determine whether this node has a parent.
	 * @return True if there is a parent node for this node.
	 */
	public boolean hasParent() {
		return parent.isPresent();
	}

	/**
	 * Get the parent of this node. This should be wrapped in a call to
	 * hasParent() because it throws an error if this node has no parent.
	 * @return The parent of this node.
	 * @throws NoSuchElementException If this node does not have a parent.
	 */
	public GameTree getParent() throws NoSuchElementException {
		return parent.orElseThrow();
	}

	/**
	 * Get the move that led to this node. This should be wrapped in a call to
	 * hasParent() because it throws an error if this node has no parent.
	 * @return The move that led to this node.
	 * @throws NoSuchElementException If this node does not have a parent.
	 */
	public Move getMove() throws NoSuchElementException {
		return move.orElseThrow();
	}

	/**
	 * Get the comment associated with this node.
	 * @return The comment for this node.
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Set the comment for this node.
	 * @param comment The new commend for this node.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	/**
	 * Determine whether this node has a successor on the main line.
	 * @return True if this move has a main line successor.
	 */
	public boolean hasContinuation() {
		return !variations.isEmpty();
	}

	/**
	 * Get the next node on the main line. This function should be wrapped in
	 * hasMainContinuation() because it throws an error if the main continuation
	 * is empty.
	 * @return The main continuation from this node.
	 */
	public GameTree getMainContinuation() {
		return variations.get(0);
	}
	
	/**
	 * Add a variation to this node.
	 * @param move The move to add as a variation.
	 */
	public void addVariation(GameTree newNode) {
		variations.add(newNode);
	}
	
	/**
	 * Move the last variation to the front. This is useful in the PGN parser
	 * for maintaining the right order of variations.
	 */
	public void setLastVariationAsMain() {
		GameTree lastVar = variations.get(variations.size() - 1);
		variations.remove(variations.size() - 1);
		variations.add(0, lastVar);
	}

	/**
	 * Check which player is set to move.
	 * @return The player whose turn it is.
	 */
	public Piece.Color getPlayerToMove() {
		return playerToMove;
	}
	
	/**
	 * Get all variations continuing from this position.
	 * @return The variations from this position.
	 */
	public ArrayList<GameTree> getVariations() {
		return variations;
	}
	
	public boolean hasMove() {
		return move.isPresent();
	}
	
	public void removeVariation(GameTree node) {
		variations.remove(node);
	}
	
	public void promoteVariation(GameTree node) {
		int index = 0;
		while (index < variations.size() && variations.get(index) != node) {
			index += 1;
		}
		variations.remove(index);
		variations.add(index - 1, node);
	}
	
	public void promoteVariationToMain(GameTree node) {
		int index = 0;
		while (index < variations.size() && variations.get(index) != node) {
			index += 1;
		}
		variations.remove(index);
		variations.add(0, node);
	}
	
}

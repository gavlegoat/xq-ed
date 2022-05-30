package xqed.xiangqi;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.Optional;

public class PGNGameListener extends PGNBaseListener {
	
	private Optional<ParseException> pgnError;
	
	private Game game;
	private GameTree treePointer;
	private LinkedList<GameTree> variationStack;
	
	public PGNGameListener() {
		pgnError = Optional.empty();
		variationStack = new LinkedList<>();
	}
	
	public Optional<ParseException> getError() {
		return pgnError;
	}
	
	public Game getGame() {
		return game;
	}
	
	@Override
	public void enterGame(PGNParser.GameContext ctx) {
		game = new Game();
		treePointer = game.getGameTree();
	}
	
//	private void promoteVariations(GameTree node) {
//		if (node.getVariations().isEmpty()) {
//			return;
//		}
//		node.setLastVariationAsMain();
//	}
//	
//	@Override
//	public void exitGame(PGNParser.GameContext ctx) {
//		// The way we handle variations leaves the main variation at the end.
//		// We solve that by promoting the last variation of each tree node here.
//		GameTree root = game.getGameTree();
//		promoteVariations(root);
//	}
	
	@Override
	public void exitTag(PGNParser.TagContext ctx) {
		if (pgnError.isPresent()) {
			return;
		}
		String name = ctx.getChild(1).getText();
		String value = ctx.getChild(2).getText();
		// Strip the quotes from the string literal
		value = value.substring(1, value.length() - 1);
		// Replace escaped sequences
		value = value.replace("\\\"", "\"");
		value = value.replace("\\\\", "\\");
		game.addTag(name, value);
	}
	
	@Override
	public void exitSanMove(PGNParser.SanMoveContext ctx) {
		if (pgnError.isPresent()) {
			return;
		}
		// Some moves are notated as '...', which indicates that this is picking
		// up after a comment or variation on black's move. These moves do not
		// actually effect parsing and can be skipped.
		boolean allDots = true;
		for (int i = 0; i < ctx.getText().length(); i++) {
			if (ctx.getText().charAt(i) != '.') {
				allDots = false;
				break;
			}
		}
		if (allDots) {
			return;
		}
		Move m;
		try {
			m = treePointer.getPosition().interpretMove(ctx.getText(),
					treePointer.getPlayerToMove());
		} catch (ParseException e) {
			pgnError = Optional.of(e);
			return;
		}
		Position newPos = treePointer.getPosition().makeMove(m);
		GameTree newNode = new GameTree(newPos, treePointer, m);
		treePointer.addVariation(newNode);
		treePointer = newNode;
	}
	
	// Note that this handling of the variations leaves the main variations at
	// the end of the variation lists. This is handled in exitGame.
	@Override
	public void enterRecursiveVariation(PGNParser.RecursiveVariationContext ctx) {
		if (pgnError.isPresent()) {
			return;
		}
		variationStack.push(treePointer);
		// The PGN notation has one move from the main line followed by
		// variations, so we need to back up the current node pointer once.
		treePointer = treePointer.getParent();
	}
	
	@Override
	public void exitRecursiveVariation(PGNParser.RecursiveVariationContext ctx) {
		if (pgnError.isPresent()) {
			return;
		}
		treePointer = variationStack.pop();
	}
	
	@Override
	public void exitComment(PGNParser.CommentContext ctx) {
		if (pgnError.isPresent()) {
			return;
		}
		String comment = ctx.getText();
		if (comment.charAt(0) == '{') {
			comment = comment.substring(1, comment.length() - 1);
		}
		treePointer.setComment(comment);
	}
	
	@Override
	public void exitTermination(PGNParser.TerminationContext ctx) {
		if (pgnError.isPresent()) {
			return;
		}
		if (game.hasTag("Termination")) {
			return;
		}
		game.addTag("Termination", ctx.getText());
	}
	
}

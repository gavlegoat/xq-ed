package main.java;

public class PGNGameListener extends PGNBaseListener {
	
	private Game game;
	private GameTree treePointer;
	
	@Override
	public void enterGame(PGNParser.GameContext ctx) {
		game = new Game();
		treePointer = game.getGameTree();
	}
	
	@Override
	public void exitTag(PGNParser.TagContext ctx) {
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
	public void enterElementSeq(PGNParser.ElementSeqContext ctx) {
		// TODO
	}
	
	@Override
	public void exitElement(PGNParser.ElementContext ctx) {
		// TODO
	}
	
	@Override
	public void exitSanMove(PGNParser.SanMoveContext ctx) {
		// TODO
	}
	
}

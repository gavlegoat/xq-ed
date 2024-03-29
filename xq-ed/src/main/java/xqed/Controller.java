package xqed;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import javafx.util.Pair;
import xqed.gui.AnalysisConfigStage;
import xqed.gui.AnalysisPane;
import xqed.gui.BoardPane;
import xqed.gui.GraphPane;
import xqed.gui.MovePane;
import xqed.gui.MovePane.StringTree;
import xqed.gui.TagStage;
import xqed.xiangqi.Game;
import xqed.xiangqi.GameTree;
import xqed.xiangqi.Move;
import xqed.xiangqi.Piece;
import xqed.xiangqi.Position;

/**
 * The controller is responsible for communicating between GUI components and
 * back end data as well as maintaining a consistent state across different GUI
 * elements.
 */
public class Controller {
	
	/** The container for the entire editor. */
	private Window topLevelWindow;

	/** The game currently being displayed. */
	private Game game;
	/** The game tree node corresponding to the current board position. */
	private GameTree current;
	
	/**
	 * Indicates whether a piece is currently being moved (either by a two-click
	 * method or click-and-drag).
	 */
	private boolean movingPiece;
	/**
	 * If a piece is currently moving, this keeps track of the possible
	 * destinations.
	 */
	private ArrayList<Pair<Integer, Integer>> movingList;
	/** The file where the current move started. */
	private int startFile;
	/** The rank where the current move started. */
	private int startRank;
	
	/** The pane where a user can enter comments on the current position. */
	public TextArea commentArea;
	/** The pane where the move list is displayed. */
	public MovePane movePane;
	/** The notation format to use when displaying moves. */
	public Move.MoveFormat format;
	/** The pane where the board is displayed. */
	public BoardPane boardPane;
	public Button navStart;
	public Button navBack;
	public Button navForward;
	public Button navEnd;
	public Pane boardParent;
	public AnalysisPane analysisPane;
	public GraphPane graphPane;
	
	/** The toggle group for the move format. */
	public ToggleGroup moveFormatGroup;
	/** The menu item for selecting WXF move format. */
	public RadioMenuItem wxfToggle;
	/** The menu item for selecting WXF move format. */
	public RadioMenuItem algebraicToggle;
	/** The menu item for selecting WXF move format. */
	public RadioMenuItem ucciToggle;
	
	/** True if the game has been edited since it was last saved. */
	private boolean gameChanged;
	/** The filename of the current game if it exists. */
	private Optional<File> gameFile;
	
	/** The engine to use for analysis. */
	private Engine engine;
	private SimpleObjectProperty<Engine.EngineInfo> engineInfo;
	
	/**
	 * Construct a new controller with a fresh game.
	 */
	public Controller() {
		game = new Game();
		current = game.getGameTree();
		movingPiece = false;
		startFile = -1;
		startRank = -1;
		format = Move.MoveFormat.RELATIVE;
		gameChanged = false;
		gameFile = Optional.empty();
		engine = new Engine(this);
		engineInfo = new SimpleObjectProperty<>();
		engineInfo.addListener(new ChangeListener<Engine.EngineInfo>() {
			@Override
			public void changed(ObservableValue<? extends Engine.EngineInfo> observable,
					Engine.EngineInfo oldInfo, Engine.EngineInfo newInfo) {
				updateEngineLines(newInfo);
			}
		});
	}
	
	/**
	 * Called after the fields are populated to set everything up. This is
	 * separated from the constructor to work nicely with FXML.
	 */
	public void initialize(Window topLevel) {
		boardPane.drawBoard(new Position());
		movePane.setController(this);
		analysisPane.setController(this);
		graphPane.setController(this);
		moveFormatGroup.selectToggle(wxfToggle);
		topLevelWindow = topLevel;

		boardPane.widthProperty().bind(boardParent.widthProperty());
		boardPane.heightProperty().bind(boardParent.heightProperty());
	}
	
	/**
	 * Convert the game tree to a tree of move names for displaying.
	 * @param root The current game tree node.
	 * @param parent The parent in the string tree.
	 * @return A tree of strings representing the moves of this game.
	 */
	private StringTree traverseGameTree(GameTree root, Optional<StringTree> parent) {
		StringTree cur = new StringTree();
		if (root.hasMove()) {
			String move = root.getMove().write(root.getParent().getPosition(),
					format);
			cur.setCurrent(Optional.of(move));
		}
		ArrayList<StringTree> children = new ArrayList<>();
		cur.setParent(parent);
		for (GameTree ch : root.getVariations()) {
			children.add(traverseGameTree(ch, Optional.of(cur)));
		}
		cur.setChildren(children);
		return cur;
	}
	
	/**
	 * Get a tree of strings representing the current moves, suitable for
	 * displaying in the move pane. Also returns the path from the root node to
	 * the current node as a list of integers, where each element of the list
	 * is an index into the children of the corresponding tree node.
	 * @return A tree of move notation and a path to the current node.
	 */
	private Pair<StringTree, LinkedList<Integer>> getCurrentMoveNames() {
		LinkedList<Integer> path = new LinkedList<>();
		GameTree root = current;
		GameTree pathNode = current;
		while (root.hasParent()) {
			root = root.getParent();
			int index = 0;
			for (GameTree ch : root.getVariations()) {
				// ch is the same object as current, so we can compare with ==
				if (ch == pathNode) {
					path.addFirst(index);
					pathNode = pathNode.getParent();
					break;
				}
				index += 1;
			}
		}
		StringTree names = traverseGameTree(root, Optional.empty());
		return new Pair<>(names, path);
	}
	
	/**
	 * Redraw the move pane.
	 */
	private void updateMoves() {
		Pair<StringTree, LinkedList<Integer>> p = getCurrentMoveNames();
		StringTree moveNames = p.getKey();
		LinkedList<Integer> path = p.getValue();
		movePane.redraw(moveNames, path);
	}
	
	/**
	 * Update all of the relevant areas that change when the current position
	 * changes.
	 */
	private void updateAll() {
		boardPane.drawBoard(current.getPosition());
		commentArea.setText(current.getComment());
		updateMoves();
		
		if (!current.hasParent()) {
			navBack.setDisable(true);
			navStart.setDisable(true);
		} else {
			navBack.setDisable(false);
			navStart.setDisable(false);
		}
		
		if (!current.hasContinuation()) {
			navForward.setDisable(true);
			navEnd.setDisable(true);
		} else {
			navForward.setDisable(false);
			navEnd.setDisable(false);
		}
		if (engine.isRunning()) {
			try {
				engine.setPosition(current.getPosition().toString(),
						current.getPlayerToMove() == Piece.Color.RED,
						current.getMoveNum());
			} catch (IOException e) {}
		}
	}
	
	/**
	 * Move to the beginning of the current game.
	 */
	@FXML
	public void goToBeginning() {
		while (current.hasParent()) {
			current = current.getParent();
		}
		updateAll();
	}
	
	/**
	 * Move one move backward in the current game.
	 */
	@FXML
	public void goBack() {
		if (current.hasParent()) {
			current = current.getParent();
		}
		updateAll();
	}
	
	/**
	 * Move one move forward along the main line in the current game.
	 */
	@FXML
	public void goForward() {
		if (current.hasContinuation()) {
			current = current.getMainContinuation();
		}
		updateAll();
	}
	
	/**
	 * Go to the end of the main line of the current game.
	 */
	@FXML
	public void goToEnd() {
		while (current.hasContinuation()) {
			current = current.getMainContinuation();
		}
		updateAll();
	}
	
	/**
	 * Show the user a dialog asking if they want to save the current file. This
	 * is shown when the user opens a new file or quits while there are unsaved
	 * changes to the current game.
	 * @return true if the user wants to cancel the operation.
	 */
	public boolean showConfirmSaveDialog() {
		Alert alert = new Alert(Alert.AlertType.WARNING,
				"The current game has not been saved. Would you like to save it?");
		ButtonType saveButton = new ButtonType("Save");
		ButtonType discardButton = new ButtonType("Discard");
		ButtonType cancelButton = new ButtonType("Cancel");
		
		alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);
		
		Optional<ButtonType> res = alert.showAndWait();
		if (res.get() == saveButton) {
			if (saveFile()) {
				return false;
			}
			return true;
		} else if (res.get() == cancelButton) {
			return true;
		}
		return false;
	}
	
	/**
	 * Determine whether the game has changed since it was last saved.
	 * @return True if the game has been edited.
	 */
	public boolean getGameChanged() {
		return gameChanged;
	}
	
	/**
	 * Open a new file.
	 */
	public void openFile() {
		if (gameChanged) {
			boolean cancel = showConfirmSaveDialog();
			if (cancel) {
				return;
			}
		}
		FileChooser fc = new FileChooser();
		fc.setTitle("Open Game");
		fc.getExtensionFilters().add(new ExtensionFilter("Games", "*.pgn"));
		File chosen = fc.showOpenDialog(topLevelWindow);
		if (chosen == null) {
			return;
		}
		try {
			game = new Game(Files.readString(chosen.toPath()));
			gameChanged = false;
			gameFile = Optional.of(chosen);
			current = game.getGameTree();
			updateAll();
		} catch (IOException e) {
			Alert a = new Alert(Alert.AlertType.ERROR,
					"Could not open file " + chosen.toString());
			a.showAndWait();
		} catch (ParseException e) {
			Alert a = new Alert(Alert.AlertType.ERROR,
					"Could not read PGN:\n" + e.getMessage());
			a.showAndWait();
		}
	}
	
	/**
	 * Save the game at the given filename.
	 * @param filename The filename to save the game to.
	 * @return true if the file was saved.
	 */
	private boolean saveFile(File filename) {
		String pgn = "";
		try {
			pgn = game.toPGN();
		} catch (ParseException e) {
			Alert a = new Alert(Alert.AlertType.ERROR,
					"Could not write PGN:\n" + e.getMessage());
			a.showAndWait();
			return false;
		}
		try {
			FileWriter writer = new FileWriter(filename);
			writer.write(pgn);
			writer.close();
			gameChanged = false;
			return true;
		} catch (IOException e) {
			Alert a = new Alert(Alert.AlertType.ERROR,
					"Could not open file " + filename + " for writing.");
			a.showAndWait();
		}
		return false;
	}
	
	/**
	 * Save the current file.
	 * @return true if the file was saved.
	 */
	public boolean saveFile() {
		if (gameFile.isEmpty()) {
			return saveFileAs();
		} else {
			return saveFile(gameFile.get());
		}
	}
	
	/**
	 * Save the current file, but always open a dialog to choose a filename.
	 * @return true if the file was saved.
	 */
	public boolean saveFileAs() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Save Game");
		fc.getExtensionFilters().add(new ExtensionFilter("Games", "*.pgn"));
		File chosen = fc.showSaveDialog(topLevelWindow);
		if (chosen == null) {
			return false;
		}
		gameFile = Optional.of(chosen);
		return saveFile(chosen);
	}
	
	/**
	 * Ask the user to save if necessary then exit.
	 */
	@FXML
	public void checkAndExit() {
		boolean cancel = false;
		if (gameChanged) {
			cancel = showConfirmSaveDialog();
		}
		if (!cancel) {
			Platform.exit();
		}
	}
	
	/**
	 * Make a move and update all fields as appropriate.
	 * @param m The move to make.
	 */
	private void makeMove(Move m) {
		gameChanged = true;
		Position newPos = current.getPosition().clone();
		newPos.clearPiece(m.getFromSquare().getKey(), m.getFromSquare().getValue());
		newPos.setPiece(m.getToSquare().getKey(), m.getToSquare().getValue(), m.getPiece());
		int moveNum = current.getMoveNum();
		if (current.getPlayerToMove() == Piece.Color.BLACK) {
			moveNum++;
		}
		GameTree newNode = new GameTree(newPos, current, m, moveNum);
		current.addVariation(newNode);
		current = newNode;
	}
	
	/**
	 * Generate and display legal moves if the user clicks on a piece.
	 * @param e The clicking event.
	 */
	@FXML
	public void handleClick(MouseEvent e) {
		if (e.getButton() != MouseButton.PRIMARY) {
			return;
		}
		Pair<Integer, Integer> square = boardPane.getSquareFromPixels(e.getX(), e.getY());
		int file = square.getKey();
		int rank = square.getValue();
		if (file < 0 || file > 8 || rank < 0 || rank > 9) {
			// Clicked out of the board. This should cancel the current move if
			// one exists.
			if (movingPiece) {
				movingPiece = false;
				startFile = -1;
				startRank = -1;
				updateAll();
			}
			return;
		}
		Position pos = current.getPosition();
		// If a piece is currently moving, then this click should either
		// complete or cancel that move.
		if (movingPiece) {
			for (Pair<Integer, Integer> move : movingList) {
				if (file == move.getKey() && rank == move.getValue()) {
					Piece p = pos.pieceAt(startFile, startRank);
					Move m = new Move(p, new Pair<>(startFile, startRank), move);
					makeMove(m);
					break;
				}
			}
			movingPiece = false;
			startFile = -1;
			startRank = -1;
			updateAll();
			return;
		}
		// Otherwise, this click should start a move.
		if (pos.hasPieceAt(file, rank) &&
				pos.pieceAt(file, rank).getColor() == current.getPlayerToMove()) {
			ArrayList<Pair<Integer, Integer>> moves =
					current.getPosition().getMovesFrom(file, rank);
			movingList = moves;
			boardPane.markSquares(moves);
			startFile = file;
			startRank = rank;
			movingPiece = true;
		}
	}
	
	/**
	 * Pick up a piece and display legal moves.
	 * @param e The dragging event.
	 */
	@FXML
	public void handleDragStart(MouseEvent e) {
		Pair<Integer, Integer> square = boardPane.getSquareFromPixels(e.getX(), e.getY());
		int file = square.getKey();
		int rank = square.getValue();
		Position pos = current.getPosition();
		if (pos.hasPieceAt(file, rank) &&
				pos.pieceAt(file, rank).getColor() == current.getPlayerToMove()) {
			ArrayList<Pair<Integer, Integer>> moves =
					current.getPosition().getMovesFrom(file, rank);
			boardPane.setMovingPiece(pos.pieceAt(file, rank));
			boardPane.setMovingX(e.getX());
			boardPane.setMovingY(e.getY());
			boardPane.markSquares(moves);
			boardPane.drawMovingPiece();
			movingList = moves;
			startFile = file;
			startRank = rank;
			movingPiece = true;
		}
	}
	
	/**
	 * Redraw the board pane as a piece is dragged.
	 * @param e The dragging event.
	 */
	@FXML
	public void handleDrag(MouseEvent e) {
		if (!movingPiece) {
			return;
		}
		Position pos = current.getPosition().clone();
		pos.clearPiece(startFile, startRank);
		boardPane.drawBoard(pos);
		boardPane.markSquares(movingList);
		boardPane.setMovingX(e.getX());
		boardPane.setMovingY(e.getY());
		boardPane.drawMovingPiece();
	}
	
	/**
	 * Put down the picked up piece.
	 * @param e The dragging event.
	 */
	@FXML
	public void handleDragEnd(MouseDragEvent e) {
		if (!movingPiece) {
			return;
		}
		Pair<Integer, Integer> square = boardPane.getSquareFromPixels(e.getX(), e.getY());
		int file = square.getKey();
		int rank = square.getValue();
		Position pos = current.getPosition();
		ArrayList<Pair<Integer, Integer>> moves =
				current.getPosition().getMovesFrom(startFile, startRank);
		for (Pair<Integer, Integer> move : moves) {
			if (file == move.getKey() && rank == move.getValue()) {
				Piece p = pos.pieceAt(startFile, startRank);
				Move m = new Move(p, new Pair<>(startFile, startRank), move);
				makeMove(m);
				break;
			}
		}
		movingPiece = false;
		startFile = -1;
		startRank = -1;
		boardPane.setMovingPiece(new Piece());
		boardPane.setMovingX(-1);
		boardPane.setMovingY(-1);
		boardPane.drawBoard(current.getPosition());
	}
	
	/**
	 * Update the comment text for the current position.
	 * @param e The key event.
	 */
	@FXML
	public void updateComment(KeyEvent e) {
		current.setComment(commentArea.getText());
		gameChanged = true;
	}

	/**
	 * Set the move in the game state indicated by path as the current board
	 * position.
	 * @param path The path of the position in the game tree.
	 */
	public void goToMove(List<Integer> path) {
		GameTree root = current;
		while (root.hasParent()) {
			root = root.getParent();
		}
		for (Integer i : path) {
			root = root.getVariations().get(i);
		}
		current = root;
		movingPiece = false;
		updateAll();
	}
	
	/**
	 * Change the move display format to WXF/relative.
	 */
	public void setFormatWXF() {
		format = Move.MoveFormat.RELATIVE;
		updateMoves();
	}
	
	/**
	 * Change the move display format to algebraic.
	 */
	public void setFormatAlgebraic() {
		format = Move.MoveFormat.ALGEBRAIC;
		updateMoves();
	}
	
	/**
	 * Change the move display format to UCCI.
	 */
	public void setFormatUCCI() {
		format = Move.MoveFormat.UCCI;
		updateMoves();
	}
	
	/**
	 * Delete the tree rooted at the current node.
	 */
	@FXML
	public void deleteVariation() {
		if (!current.hasParent()) {
			return;
		}
		GameTree node = current.getParent();
		node.removeVariation(current);
		current = node;
		gameChanged = true;
		updateAll();
	}
	
	/**
	 * Delete the tree rooted at the given path.
	 * @param path The path to the subtree to delete.
	 */
	public void deleteVariation(LinkedList<Integer> path) {
		GameTree node = current;
		goToMove(path);
		deleteVariation();
		current = node;
		updateMoves();
	}
	
	/**
	 * Promote the subtree rooted at the current node.
	 */
	public void promoteVariation() {
		if (!current.hasParent()) {
			return;
		}
		GameTree node = current.getParent();
		node.promoteVariation(current);
		gameChanged = true;
		updateMoves();
	}
	
	/**
	 * Promote the subtree rooted at the given path.
	 * @param path The path to the subtree to promote.
	 */
	public void promoteVariation(LinkedList<Integer> path) {
		GameTree node = current;
		goToMove(path);
		promoteVariation();
		current = node;
		updateMoves();
	}
	
	/**
	 * Make the subtree at the current node the main line (w.r.t. it's parent).
	 */
	public void makeMainLine() {
		if (!current.hasParent()) {
			return;
		}
		GameTree node = current.getParent();
		node.promoteVariationToMain(current);
		gameChanged = true;
		updateMoves();
	}
	
	/**
	 * Make the subtree at the given path the main line (w.r.t. it's parent).
	 * @param path The path to the subtree to promote.
	 */
	public void makeMainLine(LinkedList<Integer> path) {
		GameTree node = current;
		goToMove(path);
		makeMainLine();
		current = node;
		updateMoves();
	}
	
	/**
	 * Open a dialog for the user to edit the tags of the game.
	 */
	public void editTags() {
		TagStage stage = new TagStage(this, game.getTags());
		stage.showAndWait();
	}
	
	/**
	 * Update the values of PGN tags.
	 * @param values The new values for each tag.
	 */
	public void updateTags(HashMap<String, String> values) {
		for (String s : Game.possiblePGNTags) {
			if (values.containsKey(s)) {
				game.addTag(s, values.get(s));
			} else {
				game.clearTag(s);
			}
		}
	}
	
	/**
	 * Ask the user for an engine executable, load it, and initialize it.
	 */
	public void loadEngine() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Choose Engine");
		File chosen = fc.showOpenDialog(topLevelWindow);
		if (chosen == null) {
			return;
		}
		boolean loaded = false;
		String name = "";
		try {
			name = engine.loadEngine(chosen);
			loaded = true;
		} catch (IOException e) {
			Alert a = new Alert(Alert.AlertType.ERROR,
					"Could not start engine " + chosen.toString());
			a.showAndWait();
		} catch (ParseException e) {
			Alert a = new Alert(Alert.AlertType.ERROR,
					"Error interacting with engine: " + e.getMessage());
			a.showAndWait();
		}
		if (loaded) {
			analysisPane.enableAnalysisButtons();
			analysisPane.setEngineName(name);
		}
	}
	
	/**
	 * Set the engine to the current position and start it thinking.
	 */
	public void startEngine() {
		try {
			engine.setPosition(current.getPosition().toString(),
					current.getPlayerToMove() == Piece.Color.RED, 1);
			engine.startEngine();
		} catch (IOException e) {
			Alert a = new Alert(Alert.AlertType.ERROR,
					"Unable to start engine");
			a.showAndWait();
		}
	}
	
	/**
	 * Stop a running engine.
	 */
	public void stopEngine() {
		try {
			engine.stopEngine();
		} catch (IOException e) {
			Alert a = new Alert(Alert.AlertType.ERROR,
					"Unable to stop engine");
			a.showAndWait();
		}
	}
	
	/**
	 * Run the engine on every move up to some predetermined limits. This method
	 * first shows a dialog to the user which they can use to set limits on the
	 * search (e.g., 20 seconds / move), then runs the engine on each move with
	 * the specified limits. The dialog should also ask the user whether to
	 * write the analysis in comments or add variations. Note that the graph
	 * should always be updated.
	 */
	public void runAnalysis() {
		AnalysisConfigStage analysisStage = new AnalysisConfigStage();
		analysisStage.showAndWait();
		// TODO
	}
	
	/**
	 * Set options for the currently loaded engine.
	 */
	public void configureEngine() {
		// TODO
	}
	
	/**
	 * Used as a callback to update the analysis pane with information from the
	 * engine.
	 * @param info The information given by the engine.
	 */
	public void updateEngineLines(Engine.EngineInfo info) {
		// Convert moves to the appropriate format.
		String[] lines = info.getLines();
		String[] toWrite = new String[lines.length];
		double[] convertedScores = new double[lines.length];
		for (int i = 0; i < lines.length; i++) {
			Piece.Color toMove = current.getPlayerToMove();
			convertedScores[i] = toMove == Piece.Color.RED ?
					info.getScores()[i] : -info.getScores()[i];
			String[] moves = lines[i].strip().split("\\s+");
			Position cur = current.getPosition().clone();
			StringBuilder text = new StringBuilder();
			int moveNum = current.getMoveNum();
			for (String move : moves) {
				if (move.isBlank()) {
					continue;
				}
				if (toMove == Piece.Color.RED) {
					moveNum++;
					text.append(" ");
					text.append(Integer.toString(moveNum));
					text.append(".");
				}
				Move m = null;
				try {
					m = cur.interpretMove(move, toMove);
				} catch (ParseException e) {
					break;
				}
				text.append(" ");
				text.append(m.write(cur, format));
				cur.makeMove(m);
				// Scores are returned from the engine's point of view.
				toMove = Piece.switchColor(toMove);
			}
			// Remove the first space
			if (text.length() > 0) {
				text.deleteCharAt(0);
			}
			toWrite[i] = text.toString();
		}
		analysisPane.setEngineInfo(convertedScores, toWrite,
				info.getNodes(), info.getDepth(), info.getTime());
		graphPane.setScore(current.getMoveNum(),
				current.getPlayerToMove() == Piece.Color.RED,
				convertedScores[0]);
		graphPane.drawGraph();
	}
	
	/**
	 * Get a representation of the move with the given number. Note that this
	 * move number is in terms of plies after the start of the game. The desired
	 * move is on the path to the current node, and if it comes after the 
	 * current move then it is along the main line.
	 * @param moveNum The plies since the game start.
	 * @return A string representation of the desired move.
	 */
	public String getMoveName(int moveNum) {
		Pair<StringTree, LinkedList<Integer>> p = getCurrentMoveNames();
		StringTree names = p.getKey();
		LinkedList<Integer> path = p.getValue();
		int index = 0;
		for (Integer i : path) {
			names = names.getChildren().get(i);
			index++;
			if (index == moveNum - 1) {
				return names.getCurrent().get();
			}
		}
		while (index < moveNum - 1) {
			names = names.getChildren().get(0);
			index++;
		}
		return names.getCurrent().orElseGet(() -> "");
	}
	
	/**
	 * Go to the move with the given index. The semantics are the same as
	 * {@link getMoveName}.
	 * @param moveNum The number of the move to go to.
	 */
	public void goToMoveNum(int moveNum) {
		Pair<StringTree, LinkedList<Integer>> p = getCurrentMoveNames();
		LinkedList<Integer> path = p.getValue();
		while (path.size() > moveNum - 1) {
			path.removeLast();
		}
		while (path.size() < moveNum - 1) {
			path.addLast(0);
		}
		goToMove(path);
	}
	
}

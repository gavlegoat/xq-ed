package xqed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Connects a Xiangqi engine and passes user commands into it and analysis
 * results back out.
 */
public class Engine {
	
	/**
	 * The information from the engine to be displayed to the user.
	 */
	public class EngineInfo {
		/** The search depth. */
		private int depth;
		/** The total number of nodes visited. */
		private int nodes;
		/** The time spent searching. */
		private double time;
		/** The scores of the top moves. */
		private double[] scores;
		/** The principle variation for each top move. */
		private String[] lines;
		
		/**
		 * Construct a new set of engine information.
		 * @param numPV The number of top moves to return.
		 */
		public EngineInfo(int numPV) {
			setDepth(0);
			setNodes(0);
			setTime(0.0);
			scores = new double[numPV];
			lines = new String[numPV];
			for (int i = 0; i < numPV; i++) {
				scores[i] = 0.0;
				lines[i] = "";
			}
		}

		public int getDepth() {
			return depth;
		}

		public void setDepth(int depth) {
			this.depth = depth;
		}

		public int getNodes() {
			return nodes;
		}

		public void setNodes(int nodes) {
			this.nodes = nodes;
		}

		public double getTime() {
			return time;
		}

		public void setTime(double time) {
			this.time = time;
		}
		
		public void setScore(int index, double score) {
			scores[index] = score;
		}
		
		public double getScore(int index) {
			return scores[index];
		}
		
		public void setLine(int index, String line) {
			lines[index] = line;
		}
		
		public String getLine(int index) {
			return lines[index];
		}
		
		public double[] getScores() {
			return scores;
		}
		
		public String[] getLines() {
			return lines;
		}
		
	}
	
	/**
	 * Runs in a separate thread to handle engine information reported during
	 * a search.
	 */
	private class EngineListener extends Task<EngineInfo> {
		/** The number of lines to report. */
		private int numLines;
		private Controller controller;
		
		/**
		 * Create a new listener.
		 * @param numPV The number of lines to report.
		 */
		public EngineListener(int numPV, Controller ctrl) {
			numLines = numPV;
			controller = ctrl;
		}
		
		@Override
		public EngineInfo call() {
			EngineInfo info = new EngineInfo(numLines);
			while (true) {
				if (isCancelled()) {
					return info;
				}
				String output = "";
				try {
					output = engineOut.readLine();
				} catch (IOException e) {}
				String[] words = output.strip().split("\\s+");
				if (!words[0].equals("info")) {
					continue;
				}
				int index = 1;
				int pv = 0;
				while (index < words.length) {
					if (words[index].equals("time")) {
						info.setTime(Double.parseDouble(words[index + 1]));
						index += 2;
					} else if (words[index].equals("depth")) {
						info.setDepth(Integer.parseInt(words[index + 1]));
						index += 2;
					} else if (words[index].equals("nodes")) {
						info.setNodes(Integer.parseInt(words[index + 1]));
						index += 2;
					} else if (words[index].equals("multipv")) {
						pv = Integer.parseInt(words[index + 1]) - 1;
						index += 2;
					} else if (words[index].equals("score")) {
						// words[index + 1] = 'cp'
						info.setScore(pv, Double.parseDouble(words[index + 2]) / 100);
						index += 3;
					} else if (words[index].equals("pv")) {
						StringBuilder line = new StringBuilder();
						index++;
						while(index < words.length) {
							if (!Character.isDigit(words[index].charAt(words[index].length() - 1))) {
								break;
							}
							line.append(" ");
							line.append(words[index]);
							index++;
						}
						String text = line.toString();
						text = text.replace("9", "z");
						for (int i = 8; i >= 0; i--) {
							text = text.replace(Integer.toString(i), Integer.toString(i + 1));
						}
						text = text.replace("z", "10");
						info.setLine(pv, text);
					} else {
						index++;
					}
				}
				Platform.runLater(() -> controller.updateEngineLines(info));
			}
		}
	}
	
	/** The thread running the engine. */
	private Process exe;
	/** The output stream from the engine. */
	private BufferedReader engineOut;
	/** The input stream to the engine. */
	private BufferedWriter engineIn;
	/** The number of lines to report. */
	private int numPV;
	/** A listener to handle engine output asynchronously. */
	private EngineListener engineListener;
	/** The thread responsible for listening to engine output. */
	private Thread listenerThread;
	private Controller controller;

	public Engine(Controller ctrl) {
		exe = null;
		engineOut = null;
		engineIn = null;
		numPV = 0;
		controller = ctrl;
	}
	
	/**
	 * Get the maximum value of a spin option.
	 * @param description The option description string.
	 * @return The maximum allowed value for this option.
	 */
	private int parseMax(String description) {
		String[] words = description.strip().split("\\s+");
		int index = 0;
		while (index < words.length) {
			if (words[index].equals("max")) {
				return Integer.parseInt(words[index + 1]);
			}
			// Skip the label and the value
			index += 2;
		}
		return 0;
	}
	
	/**
	 * Start a new engine process with the given executable and initialize UCCI.
	 * @param executable The engine program.
	 * @throws IOException If the program cannot be run.
	 * @throws ParseException If the program produces unexpected output.
	 * @return The name of the engine.
	 */
	public String loadEngine(File executable) throws IOException, ParseException {
		if (exe != null) {
			engineListener.cancel();
			engineIn.write("quit");
			engineIn.newLine();
			engineIn.flush();
			try {
				exe.waitFor(5, TimeUnit.SECONDS);
				listenerThread.join(5000);
			} catch (InterruptedException e) {}
			if (exe.isAlive()) {
				exe.destroyForcibly();
			}
		}
		exe = new ProcessBuilder(executable.toString()).start();
		engineOut = new BufferedReader(new InputStreamReader(exe.getInputStream()));
		engineIn = new BufferedWriter(new OutputStreamWriter(exe.getOutputStream()));
		
		engineIn.write("ucci");
		engineIn.newLine();
		engineIn.flush();
		HashMap<String, String> id = new HashMap<>();
		HashMap<String, String> options = new HashMap<>();
		String res = engineOut.readLine();
		String name = "";
		while (!res.strip().equals("ucciok")) {
			if (res.isBlank()) {
				res = engineOut.readLine();
				continue;
			}
			String[] words = res.strip().split("\\s+");
			if (words.length < 2) {
				throw new ParseException("Unexpected response from engine: " + res, 0);
			}
			StringBuilder value = new StringBuilder();
			for (int i = 2; i < words.length; i++) {
				value.append(" ");
				value.append(words[i]);
			}
			value.deleteCharAt(0);
			if (words[0].equals("id")) {
				id.put(words[1], value.toString());
				if (words[1].equals("name")) {
					name = words[2] + value.toString();
				}
			} else if (words[0].equals("option")) {
				options.put(words[1], value.toString());
			}
			res = engineOut.readLine();
		}
		
		if (options.containsKey("Hash")) {
			int value = Integer.min(2048, parseMax(options.get("Hash")));
			engineIn.write(String.format("setoption Hash %d", value));
			engineIn.newLine();
			engineIn.flush();
		}
		if (options.containsKey("Threads")) {
			int value = Integer.min(4, parseMax(options.get("Threads")));
			engineIn.write(String.format("setoption Threads %d", value));
			engineIn.newLine();
			engineIn.flush();
		}
		if (options.containsKey("MultiPV")) {
			numPV = Integer.min(3, parseMax(options.get("MultiPV")));
			engineIn.write(String.format("setoption MultiPV %d", numPV));
			engineIn.newLine();
			engineIn.flush();
		} else {
			numPV = 1;
		}
		if (options.containsKey("UCI_Variant")) {
			engineIn.write("setoption UCI_Variant xiangqi");
			engineIn.newLine();
			engineIn.flush();
		}
		if (options.containsKey("UCI_AnalyseMode")) {
			engineIn.write("setoption UCI_AnalyseMode true");
			engineIn.newLine();
			engineIn.flush();
		}

		engineIn.write("isready");
		engineIn.newLine();
		engineIn.flush();
		res = engineOut.readLine().strip();
		if (!res.equals("readyok")) {
			throw new ParseException("Unexpected response from the engine: " + res, 0);
		}
		return name;
	}
	
	/**
	 * Send a new position to the engine.
	 * @param fen The new board position.
	 * @param redToMove Whether it is Red's turn to move.
	 * @param move The move number.
	 * @throws IOException If the engine can't be communicated with.
	 */
	public void setPosition(String fen, boolean redToMove, int move) throws IOException {
		String toWrite = fen.replace('h', 'n').replace('e', 'b')
				.replace('H', 'N').replace('E', 'B');
		toWrite += redToMove ? " w" : " b";
		toWrite += " - - 0 " + Integer.toString(move);
		engineIn.write("position fen " + toWrite);
		engineIn.newLine();
		engineIn.flush();
	}
	
	/**
	 * Set the engine to the starting position.
	 * @throws IOException If the engine can't be communicated with.
	 */
	public void setStartPosition() throws IOException {
		engineIn.write("position startpos");
		engineIn.newLine();
		engineIn.flush();
	}
	
	/**
	 * Start the engine searching on the current position.
	 * @throws IOException If the engine can't be communicated with.
	 */
	public void startEngine() throws IOException {
		engineListener = new EngineListener(numPV, controller);
		listenerThread = new Thread(engineListener);
		engineIn.write("go infinite");
		engineIn.newLine();
		engineIn.flush();
		listenerThread.setDaemon(true);
		listenerThread.start();
	}
	
	/**
	 * Stop the engine.
	 * @return The move the engine wants to ponder on, if one is given.
	 * @throws IOException If the engine can't be communicated with.
	 */
	public String stopEngine() throws IOException {
		engineListener.cancel();
		try {
			listenerThread.join(1000);
		} catch (InterruptedException e) {}
		engineIn.write("stop");
		engineIn.newLine();
		engineIn.flush();
		String line = engineOut.readLine().strip();
		String[] words = line.strip().split("\\s+");
		while (!words[0].equals("bestmove") && !words[0].equals("nobestmove")) {
			line = engineOut.readLine().strip();
			words = line.strip().split("\\s+");
		}
		if (words.length > 3 && words[2].equals("ponder")) {
			return words[3];
		}
		return "";
	}
	
}

package xqed;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Connects a Xiangqi engine and passes user commands into it and analysis
 * results back out.
 */
public class Engine {
	
	private Process exe;
	private InputStream engineOut;
	private OutputStream engineIn;

	public Engine() {
		exe = null;
		engineOut = null;
		engineIn = null;
	}
	
	// TOOD: asynchronously read and update from engine output
	
	public void loadEngine(File executable) throws IOException {
		if (exe != null) {
			exe.destroy();
		}
		exe = new ProcessBuilder(executable.toString()).start();
		engineOut = exe.getInputStream();
		engineIn = exe.getOutputStream();
		
		// TODO: initial interaction
	}
	
}

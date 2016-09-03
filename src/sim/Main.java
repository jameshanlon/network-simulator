/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim;

public class Main {
	
	/*
	 * Visualisation mode: output a graphviz-generated image of the topology
	 */
	private static void visualise(String[] args) {
		if(args.length != 2) {
			System.err.println("Error: need to specify dot and image files");
			return;
		}

		Simulator.init();
		//Visualise.writeDot(Simulator.getNetwork());
		//Visualise.createDotImage(args[1]);
	}
	
	/*
	 * Debug mode: launch the GUI and run simulation on a new thread
	 */
	private static void debug() {
		DebugMode.init();
		DebugMode.run();
	}
	
	/*
	 * Run mode: run the simulation normally
	 */
	private static void run(String[] args) {
		
		for(int i=1; i<args.length; i++) {
			String[] frags = args[i].split("=");
			Config.setArgParam(frags[0].trim(), frags[1].trim());
		}
		
		RunMode.init();
		RunMode.run();
	}
	
	private static String dumpUsage() {
		String s = "";
		s += "debug/run:          <configuration.cfg>\n";
		s += "visualise topology: <configuration.cfg> <output.ps>\n";
		return s;
	}
	
	public static void main(String[] args) {
		if(args.length < 1) {
			System.out.println(dumpUsage());
			System.exit(1);
		}

		Config.init(args[0]);
		
		switch(Config.mode()) {
		case VISUALISE: Main.visualise(args); return;
		case DEBUG:     Main.debug();         return;
		case RUN:   	Main.run(args);       return;
		}
	}
}

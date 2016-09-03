/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim;

import sim.Simulator.SimState;
import sim.stats.Stats;

public class RunMode {
	
	private static final boolean VERBOSE_EXPERIMENT = true;
	private static final boolean VERBOSE_SIM_RUN = true;
	
	private static int m_runSampleCount;
	private static int m_warmSampleCount;
	
	private RunMode() {}

    /*
     * Initialise experiments
     */
	public static void init() {
		Utilities.mkdir(Config.RESULTS_DIR);
		Simulator.init();
	}

    /*
     * Run the experiments
     */
	public static void run() {
		
		switch(Simulator.numNodes()) {
		case 32: Config.setInjStep(0.01); break;
		case 64: Config.setInjStep(0.005); break;
		}
		
		experiment();
	}
	
	/*
	 * Run full experiment for each algorithm
	 */
	private static void experiment() {
        Config.setRouting(Config.routing());
        Simulator.getNetwork().initRouting();
        Stats.init();
        //simIncreasingInj(VERBOSE_EXPERIMENT);
        
        Stats.newSimRun(0, Config.injectionRate());
        singleRun(VERBOSE_EXPERIMENT, 0);
		vPrintLn(true, Stats.dumpInfo());
        
        Stats.endExperiment();
	}
	
	/*
	 * Run a full experiment for each injection rate
	 */
	public static void simIncreasingInj(boolean v) {
		
		double injStep = Config.injectionStep();
		
		int runCount = 0;
		while(true) {
			double injRate = injStep * (runCount+1);
			Config.setInjRate(injRate);
			vPrintLn(v, "Starting simulation run "+runCount+", injection rate = "+injRate+".\n");
			Stats.newSimRun(runCount, injRate);
			if(singleRun(VERBOSE_SIM_RUN, runCount))
				break;
			vPrintLn(v, Stats.dumpInfo());
			runCount++;
		}
	}

	/*
	 * Run a single simulation
	 */
	public static boolean singleRun(boolean v, int runNum) {
		
		m_runSampleCount = 0;
		m_warmSampleCount = 0;
		boolean converged = false;
		Simulator.reset();
		
		//Simulator.setState(SimState.RUNNING);
		while(m_runSampleCount < Config.numSamples()) {
			
			// Clear the recorded stats if necessary
			Stats.newSamplePhase();
			
			// Execute simulation steps for the sample interval
			for (int i = 0; i<Config.samplePeriod(); i++)
				Simulator.step();
			
			// Calculate the latency and throughput values
			Stats.calcSampleValues();
			
			// Check the latency
			if(Config.latencyThresh() > 0 && Stats.currLatency() > Config.latencyThresh()) {
				vPrintLn(v, String.format("\nAverage latency is getting huge %.2f, "+
						"terminating simulation", Stats.currLatency()));
				Simulator.setState(SimState.WARMING_UP);
				m_runSampleCount = m_warmSampleCount = 0;
				converged = true;
				break;
			}
			
			// Check the state
			switch(Simulator.getState()) {
			
			case WARMING_UP:
				if(Config.warmupPeriod() == 0) {
					switch(Config.mode()) {
					case RUN:
						if(Stats.latencyChange() < Config.warmupThresh())
							warmedUp(v);
						break;
					/*case THROUGHPUT:
						if(Stats.throughputChange() < Config.warmupThresh())
							warmedUp(v);
						break;*/
					}
				} else if(m_warmSampleCount*Config.samplePeriod() >= Config.warmupPeriod()) {
					vPrintLn(v, "Warmed up after "+Simulator.clock()+" cycles");
					Simulator.setState(SimState.RUNNING);
				}
				m_warmSampleCount++;
				break;
			
			case RUNNING:				
				vPrint(v, String.format("\r%-12s%-12s%-12s%-12s%-12s%-12s", 
						m_runSampleCount, Stats.packetsGenerated(), Stats.packetsReceived(),
						(Stats.packetsGenerated()-Stats.packetsReceived()), 
						String.format("%.2f", Stats.currLatency()),
						String.format("%.2f", Stats.currThroughput())));

				Stats.addSample();
				m_runSampleCount++;
				break;
			}
		}
		
		// Drain any remaining packets
		vPrintLn(v, "\nFinished sampling, draining packets...");
		Simulator.setState(SimState.DRAINING);
		int drainCycles = 0;
		//int packetsReceived = Stats.packetsReceived();
		while(Stats.packetsGenerated() > Stats.packetsReceived()) {
			
			Simulator.step();
			
			if(drainCycles % 1000 == 0) {
				vPrint(v, "\r"+drainCycles+", "+
						(Stats.packetsGenerated()-Stats.packetsReceived())+" left");
			}
			
			
			drainCycles++;
		}
		
		// Add the extra latency samples
		Stats.calcSampleValues();
		Stats.addSample();
		
		// Done
		vPrintLn(v, "\nDone in "+drainCycles+" cycles");
		Stats.endSimRun();
		Simulator.setState(SimState.DONE);
		return converged;
	}
	
	private static void warmedUp(boolean v) {
		vPrintLn(v, "Warmed up after "+Simulator.clock()+" cycles");
		vPrintLn(v, String.format("%-12s%-12s%-12s%-12s%-12s%-12s", 
				"Sample", "Generated", "Received", "Flying", "Latency", "Throughput"));
		Simulator.setState(SimState.RUNNING);
	}

	public  static int sampleNum() { return m_runSampleCount; }
	private static void vPrint(boolean verbose, String s) { if(verbose) System.out.print(s); }
	private static void vPrintLn(boolean verbose, String s) { if(verbose) System.out.println(s); }
}

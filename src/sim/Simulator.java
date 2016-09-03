/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim;

import java.util.Random;

import sim.components.Network;
import sim.stats.Stats;
import sim.traffic.Injection;
import sim.traffic.Traffic;

public class Simulator {
	
	public enum SimState {
		WARMING_UP,
		RUNNING,
		DRAINING,
		DONE
	};
	
	private static SimState  m_state;
	private static Random    m_rand;
	private static Network   m_network;
	private static int       m_cycleCount;

	private Simulator() {}
	
	/*
	 * Initialse the simulator by creating all of the network components 
	 * and adding them to a list of Components
	 */
	public static void init() {
		m_state   = SimState.WARMING_UP;
		m_rand    = new Random(Config.seed());
		m_network = new Network();
		Stats.init();
	}

	/*
	 * Perform one simulation step: update states, display it then copy it on
	 */
	public static void step() {
		m_network.step();
		m_cycleCount++;
	}

	public static void reset() {
		m_network.reset();
		Traffic.reset();
		Injection.reset();
		m_cycleCount = 0;
		m_state  = SimState.WARMING_UP;
	}
	
	public String dumpStats() {
		String s = "";
		return s;
	} 
	
	public static boolean  running()                { return m_state.equals(SimState.RUNNING); }
	public static long     clock()                  { return m_cycleCount; }
	public static int      numNodes()               { return m_network.numNodes(); }
	public static int      getRandInt()             { return m_rand.nextInt(); }
	public static int      getRandInt(int n)        { return m_rand.nextInt(n); }
	public static long     getRandLong()            { return m_rand.nextLong(); }
	public static double   randDouble()             { return m_rand.nextDouble(); }
	public static void     setState(SimState state) { m_state = state; }
	public static Network  getNetwork()             { return m_network; }
	public static boolean  warmingUp()              { return m_state.equals(SimState.WARMING_UP); }
	public static SimState getState()               { return m_state; }
	public static boolean  draining()               { return m_state.equals(SimState.DRAINING); }
}
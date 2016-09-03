/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.traffic;

import sim.Config;
import sim.Simulator;

public class Injection {

	private static int[] m_nodeStates;
	
	public static int getPacketLen(int source) {
		switch(Config.injection()) {
		case BERNOULLI: return Injection.bernoulli(source);
		case ONOFF:     return Injection.onOff(source);
		default:        return -1;
		}
	}
	
	/*
	 * Bernoulli process, randomly inject packets according to the rate
	 */
	private static int bernoulli(int source) {
		//return Config.packetSize();
		return (Simulator.randDouble() < (Config.injectionRate() / 
				(double)Config.packetSize())) ? Config.packetSize() : 0;
		//return (Simulator.randDouble() < Config.injectionRate()) ? Config.packetSize() : 0;
	}

	/*
	 * Two-state Modulated Markov Process (MMP): either sending or not
	 */
	private static int onOff(int source) {

		assert((source >= 0) && (source < Simulator.numNodes()));

		if(m_nodeStates == null) {
			m_nodeStates = new int [Simulator.numNodes()];
		    for(int n = 0; n < m_nodeStates.length; n++)
		    	m_nodeStates[n] = 0;
		}

		// advance state: off -> on OR on -> off
		if(m_nodeStates[source] == 0) {
			if(Simulator.randDouble() < Config.burstAlpha()) {
				m_nodeStates[source] = 1;
			}
		} else if (Simulator.randDouble() < Config.burstBeta()) {
			m_nodeStates[source] = 0;
		}

		// generate packet
		if(m_nodeStates[source] == 1) {
			double r1 = (Config.injectionRate() * (1.0 + Config.burstBeta() / Config.burstAlpha())) 
					/ (double)Config.packetSize();
			if (Simulator.randDouble() < r1)
				return Config.packetSize();
		}

		return 0;
	}
	
	public static void reset() {
		if(m_nodeStates != null) {
		    for(int n = 0; n < m_nodeStates.length; n++)
		    	m_nodeStates[n] = 0;
		}
	}
}

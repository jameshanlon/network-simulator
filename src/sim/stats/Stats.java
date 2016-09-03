/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.stats;

import java.util.LinkedList;

import sim.Config;
import sim.Simulator;
import sim.components.Flit;

/*
 * Latency values are recorded in the sample they were generated in
 * Accepted packets and hops are recorded in the sample they arrive
 */
public class Stats {

	// Average for each sample
	private static Average             m_hops;
	private static Average             m_latency;
	private static Average[]           m_accepted;
	
	// Record batch averages to calc stdDev and error
	private static LinkedList<Average> m_batchLatency;
	private static LinkedList<Average> m_batchAccepted;
	
	// Average for each simulation run
	private static LinkedList<Average> m_overallLatency;
	private static LinkedList<Average> m_overallHops;
	private static LinkedList<Average> m_overallAccepted;
	private static LinkedList<Average> m_overallAcceptedMin;
	
	// Hold variables
	private static double    m_prevAccepted;
	private static double    m_currAccepted;
	private static double    m_currLatency;
	private static double    m_changeLatency;
	private static double    m_prevLatency;
	private static double    m_currAcceptedMin;
	private static double    m_changeAccepted;
	
	private static double    m_stdDevLatency;
	private static double    m_stdDevAccepted;
	
	private static int       m_totalGenPackets;
	private static int       m_totalRecPackets;
	
	private Stats() {}
	
	public static void init() {

		m_latency = new Average();
		m_hops = new Average();
		m_accepted = new Average[Simulator.numNodes()];
		
		for(int i=0; i<m_accepted.length; i++)
			m_accepted[i] = new Average();
		
		m_batchLatency       = new LinkedList<Average>();
		m_batchAccepted      = new LinkedList<Average>();
		
		m_overallLatency     = new LinkedList<Average>();
		m_overallHops        = new LinkedList<Average>();
		m_overallAccepted    = new LinkedList<Average>();
		m_overallAcceptedMin = new LinkedList<Average>();
		
		m_currLatency = 0;
		m_currAccepted = 0;
	}
	
	/*
	 * For a new experiment (new routing algorithm) clear the stats 
	 * and return latency and throughput results
	 */
	public static void endExperiment() {
		
		m_latency.clear();
		
		for(int i=0; i<m_accepted.length; i++)
			m_accepted[i].clear();
		
		for(int i=0; i<Config.simRuns(); i++) {
			m_overallLatency.clear();
			m_overallHops.clear();
			m_overallAccepted.clear();
			m_overallAcceptedMin.clear();
		}
	}
	
	/*
	 * Reset fields for a new simulation run
	 */
	public static void newSimRun(int runNum, double x) {
		m_totalGenPackets = 0;
		m_totalRecPackets = 0;
		m_latency.clear();
		
		m_overallLatency.add(new Average(x));
		m_overallHops.add(new Average(x));
		m_overallAccepted.add(new Average(x));
		m_overallAcceptedMin.add(new Average(x));
	}
	
	public static void endSimRun() {
		
		// confidence = 95%, 100 samples/batches
		// 1.984217
		double t = 1.984217;
		
		// Calculate latency std dev
		double sum = 0;
		double batchMean = m_overallLatency.getLast().average();
		for(Average b : m_batchLatency) {
			sum += Math.pow(batchMean - b.average(), 2);
		}
		m_stdDevLatency = 1.0 / (double)(Config.numSamples()-1) * sum;
		double error = (Math.sqrt(m_stdDevLatency) * t) / Math.sqrt(Config.numSamples()); 
		m_overallLatency.getLast().setError(error);
		
		// Calculate throughput std dev
		sum = 0;
		batchMean = m_overallAccepted.getLast().average();
		for(Average b : m_batchAccepted) {
			sum += Math.pow(batchMean - b.average(), 2);
		}
		m_stdDevAccepted = 1.0 / (double)(Config.numSamples()-1) * sum;
		error = (Math.sqrt(m_stdDevAccepted) * t) / Math.sqrt(Config.numSamples()); 
		m_overallAccepted.getLast().setError(error);
	}
	
	/*
	 * Clear the batches
	 */
	public static void newSamplePhase() {		
		m_hops.clear();
		m_latency.clear();
		for(int i=0; i<m_accepted.length; i++)
			m_accepted[i].clear();
	}
	
	public static void calcSampleValues() {
		m_prevLatency = m_currLatency;
		m_currLatency = m_latency.average();
		m_changeLatency = m_currLatency==0 ? 0 : 
			Math.abs((m_currLatency - m_prevLatency)/m_currLatency);
		
		m_prevAccepted = m_currAccepted;
		m_currAccepted = calcAccepted();
		m_currAcceptedMin = calcAcceptedMin();
		m_changeAccepted = m_currAccepted==0 ? 0 : 
			Math.abs((double)(m_currAccepted-m_prevAccepted)/(double)m_currAccepted);
	}
	
	/*
	 * Add batch sample mean to sim run avg and record the previous batch means (for stddev)
	 */
	public static void addSample() {
		m_overallLatency.getLast().addSample(m_currLatency);
		m_overallHops.getLast().addSample(m_hops.average());
		m_overallAccepted.getLast().addSample(m_currAccepted);
		m_overallAcceptedMin.getLast().addSample(m_currAcceptedMin);
		
		m_batchLatency.add(new Average(m_currLatency));
		m_batchAccepted.add(new Average(m_currAccepted));
	}
	
	/*
	 * Called when a packet is retired, or when a packet could have been retired (flit==null)
	 */
	public static void retirePacket(int nodeId, Flit flit) {
		m_totalRecPackets++;
		//if(Simulator.getState().equals(SimState.DRAINING)) {
			//System.out.println("Retired a packet");
		//}
	}
	
	/*
	 * Calculate throughput on a flit level. If flit==null it is spare capacity
	 */
	public static void retireFlit(int nodeId, Flit flit) {
		if(flit != null && flit.isMmt()) {
			m_latency.addSample(flit.getLatency());
			m_hops.addSample(flit.getHops());
		}
		if(!Simulator.draining()) {
			m_accepted[nodeId].addSample(flit != null ? 1 : 0);
		}
	}
	
	private static double calcAccepted() {
		double average = 0;
		for(int i=0; i<m_accepted.length; i++)
			average += m_accepted[i].average();
		return average / (double)m_accepted.length;
	}

	private static double calcAcceptedMin() {
		double min = Double.MAX_VALUE;
		for(int i=0; i<m_accepted.length; i++) {
			if(m_accepted[i].average() < min) {
				min = m_accepted[i].average();
			}
		}
		return min;
	}
	
	public static String dumpInfo() {
		return
			"[STATS]============================================\n"+
			"Packets generated    "+m_totalGenPackets+"\n"+
			"Packets received     "+m_totalRecPackets+"\n"+
			"Overall latency      "+m_overallLatency.getLast().average()+"\n"+
			"Overall hops         "+String.format("%.2f", m_overallHops.getLast().average())+"\n"+
			"Overall accepted     "+String.format("%f", m_overallAccepted.getLast().average())+"\n"+
			"Overall min accepted "+String.format("%.2f", m_overallAcceptedMin.getLast().average())+"\n"+
			"Latency std dev      "+m_stdDevLatency+"\n"+
			"Acceped std dev      "+m_stdDevAccepted+"\n"+
			"---------------------------------------------------";
	}

	public static int     packetsGenerated()  { return m_totalGenPackets; }
	public static int     packetsReceived()   { return m_totalRecPackets; }
	public static double  throughputChange()  { return m_changeAccepted; }
	public static double  latencyChange()     { return m_changeLatency; }
	public static double  currLatency()       { return m_currLatency; }
	public static double  currThroughput()    { return m_currAccepted; }
	public static void    newPacket()         { m_totalGenPackets++; }
}

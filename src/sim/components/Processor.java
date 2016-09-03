/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.components;

import java.util.Iterator;
import java.util.LinkedList;

import sim.Config;
import sim.RunMode;
import sim.Simulator;
import sim.Config.TrafficType;
import sim.stats.Stats;
import sim.traffic.Injection;
import sim.traffic.TraceEvent;
import sim.traffic.Traffic;

public class Processor extends Console 
implements Component {
	
	public  static final int       PROC_VC = 0;
	private static int             m_msgIdCount = 0;
	private int                    m_nodeId;
	private InputPort              m_inputPort;
	private OutputPort             m_outputPort;
	private LinkedList<Flit>       m_pendingFlits;
	private LinkedList<Flit>       m_receivedFlits;
	private LinkedList<TraceEvent> m_pendingEvents;
	
	public Processor(int nodeId, ProcessorLink fromRouter, ProcessorLink toRouter) {
		super();
		m_nodeId        = nodeId;
		m_pendingFlits  = new LinkedList<Flit>();
		m_receivedFlits = new LinkedList<Flit>();
		
		// Connect router
		m_inputPort     = new InputPort(this, m_nodeId, 0, fromRouter, 1);
		m_outputPort    = new OutputPort(this, m_nodeId, 0, toRouter, 1);
		fromRouter.connectTo(m_inputPort);
		toRouter.connectFrom(m_outputPort);
	}
	
	static long total = 0;
	long start, end;
	
	/*
	 * Generate uniform traffic and read received credits
	 */
	public void update() {
		readIncomingCredits();
		readIncomingFlits();
		generateTraffic();
		routeOutgoingFlit();
	}

	/*
	 * Write flits to send to router input
	 */
	public void copy() {
		m_inputPort.writeOuputCredit();
		m_outputPort.writeOutputFlit();
	}

	/*
	 * Read any new credits from output port and update VC credits
	 */
	private void readIncomingCredits() {
		Credit credit = m_outputPort.readInputCredit();
		if(credit != null)
			m_outputPort.incrementCredits(credit.getVC());
	}
	
	/*
	 * Read incoming flits from link, and add them to receieved flits list.
	 * Call incCurrVCCredits() as processor consumes flits
	 * 
	 * NOTE: for now, don't store all received flits
	 * NOTE: for non-minimal algoriths will require a ROB
	 */
	private void readIncomingFlits() {
		m_inputPort.readInputFlit();
		Flit flit = m_inputPort.peekNextFlit();
		
		if(flit != null) {
			flit.setTimeReceived(Simulator.clock());
			m_receivedFlits.add(m_inputPort.takeNextFlit());
			console("Received flit "+flit);
			m_inputPort.incCurrVCCredits();
			
			// Only record stats once per packet, remove the packet from received
			if(flit instanceof TailFlit) {
				
				boolean gotHead = false;
				int bodyCount = 0;
				int bodySize = -1;
				for(Iterator<Flit> it  = m_receivedFlits.iterator(); it.hasNext(); ) {
					Flit f = it.next();
					if(flit.getMsgId() == f.getMsgId()) {
						if(f instanceof HeaderFlit) {
							gotHead = true;
							bodySize = ((HeaderFlit)f).getLength();
						}
						if(f instanceof BodyFlit)
							bodyCount++;
						it.remove();
					}
				}

				if(gotHead && bodyCount == bodySize) {
					Stats.retirePacket(m_nodeId, flit);
				} else {
					System.err.println("Error: incomplete packet");
				}
			}
		}
		Stats.retireFlit(m_nodeId, flit);
	}
	
	/*
	 * Take first pending flit and put on output port if that port is ready 
	 * to accept flits. I.e. it is not waiting for credits.
	 */
	private void routeOutgoingFlit() {	
		if(!m_pendingFlits.isEmpty() && m_outputPort.hasCredits(PROC_VC)) {
			Flit flit = m_pendingFlits.getFirst();
			int outputVC = 0;
			
			if(flit instanceof HeaderFlit) {
				outputVC = m_outputPort.allocVC();
				
				if(outputVC == -1) {
					//console("could not allocate the VC for the processor");
					return;
				} else {
					m_outputPort.setupConnection(PROC_VC, 0, 0);
					console(flit+" opening connection through O[0:0]");
				}
			}
			
			m_outputPort.setCurrVC(outputVC);
			m_outputPort.addFlit(PROC_VC, m_pendingFlits.removeFirst());
		}
	}

	/*
	 * Generate uniform traffic to a random node, don't if in debug mode and 
	 * outside of limit or when draining on a proper run.
	 */
	private void generateTraffic() {
		if((Config.debugMode() && m_msgIdCount == Config.maxMsgs() && Config.maxMsgs() > 0) ||
				(Config.runMode() && Simulator.draining()))
			return;
		
		if(Config.traffic().equals(TrafficType.TRACE)) {
			// Take all of the pending trace events for this clock cycle
			for(Iterator<TraceEvent> it = m_pendingEvents.iterator(); it.hasNext();) {
				TraceEvent e = (TraceEvent) it.next();
				if(e.clock == Simulator.clock()) {
					generatePacket(e.dest, e.burst);
					it.remove();
				} else {
					break;
				}
			}
		} else {
			// Use a traffic pattern generation function
			int node = Traffic.getDest(m_nodeId, Simulator.numNodes());
			int pktLen = Injection.getPacketLen(m_nodeId);
			if(pktLen > 0) {
				generatePacket(node, pktLen);
			}
		}
	}
	
	private void generatePacket(int dest, int length) {
		int messageId = m_msgIdCount++;
		int sample = Simulator.running() ? RunMode.sampleNum() : -1;
		m_pendingFlits.addLast(new HeaderFlit(messageId, sample, m_nodeId, dest, length));
		for(int i=0; i<length; i++)
			m_pendingFlits.addLast(new BodyFlit(messageId, sample, i));
		m_pendingFlits.addLast(new TailFlit(messageId, sample));
		//System.out.println("Node "+m_nodeId+" generated packet "+length+" flits to node "+dest+" at "+Simulator.clock());
		Stats.newPacket();
	}

	public String toString() {
		String s = "";
		
		s += "[I/O PORTS]\n\n";
		s += String.format("%-12s%-12s%-12s%-12s%-12s\n", "Port", "To", "State", "Cdts", "Buffer");
		s += m_inputPort;
		s += m_outputPort;
		
		//s += "\n\n[OUTGOING LINK]\n\n";
		//s += "Link\tFlits\tCdts\n";
		//s += m_outputPort.getLink()+"\n";
		
		s += "\n\n[PENDING FLITS : "+m_pendingFlits.size()+"]\n\n";
		for(Flit f : m_pendingFlits)
			s += f+"\n";
		
		s += "\n\n[RECEIVED FLITS : "+m_receivedFlits.size()+"]\n\n";
		s += String.format("%-16s%-16s%-16%s\n", "Flit", "Rcvd@", "Hops");
		for(Flit f : m_receivedFlits)
			s += String.format("%-16s%-16d%-16d\n", f.toString(), f.getTimeReceived(), f.getNumHops());
		
		return s;
	}

	public void reset() {
		m_pendingFlits.clear();
		m_receivedFlits.clear();
		m_inputPort.reset();
		m_outputPort.reset();
	}

	public int     getNodeId()         { return m_nodeId; }
	public String  getTitle()          { return "Processor "+m_nodeId; }
	public boolean hasFlits()          { return !m_pendingFlits.isEmpty(); }
	public Flit    peekFlit()          { return m_pendingFlits.peek(); }
	public Flit    takeFlit()          { return m_pendingFlits.poll(); }
	public void    addTrace(LinkedList<TraceEvent> trace) { m_pendingEvents = trace; }
}

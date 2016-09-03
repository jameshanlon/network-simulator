/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.components;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import sim.Config;
import sim.routing.RoutingFunction;

public class Router extends Console
implements Component {

	private int              m_nodeId;
	private RoutingFunction  m_routingFunction;
	private InputPort[]      m_inputPorts;
	private OutputPort[]     m_outputPorts;
	private int              m_procInputPort;
	private int              m_procOutputPort;
	
	public Router(int nodeId, ProcessorLink fromProc, ProcessorLink toProc, 
			int numInputs, int numOutputs) {
		super();
		m_nodeId          = nodeId;
		m_inputPorts      = new InputPort[numInputs + 1];
		m_outputPorts     = new OutputPort[numOutputs + 1];
		m_procInputPort   = numInputs;
		m_procOutputPort  = numOutputs;
		
		// Connect processor
		m_inputPorts[m_procInputPort] = new InputPort(this, m_nodeId, m_procInputPort, fromProc, 1);
		m_outputPorts[m_procOutputPort] = new OutputPort(this, m_nodeId, m_procOutputPort, toProc, 1);
		fromProc.connectTo(m_inputPorts[m_procInputPort]);
		toProc.connectFrom(m_outputPorts[m_procOutputPort]);
	}
	
	public void connectTo(Router toRouter, int fromPort, int toPort) {
		//System.out.println("Connecting "+getId()+":"+fromPort+" to "+toRouter.getId()+":"+toPort);
		RouterLink link = new RouterLink(Config.linkDelay());
		m_outputPorts[fromPort] = new OutputPort(this, m_nodeId, fromPort, link, Config.numVCs());
		toRouter.connectFrom(toPort, link);
		link.connectFrom(this, m_outputPorts[fromPort]);
	}
	
	public void connectFrom(int port, RouterLink link) {
		//System.out.println("Connecting "+getId()+":"+port+" from "+link.getFromRouter().getId());
		m_inputPorts[port] = new InputPort(this, m_nodeId, port, link, Config.numVCs());
		link.connectTo(this, m_inputPorts[port]);
	}
	
	/*
	 * read all incoming links for flits, and add them to VC buffers
	 *  and check for credit updates
	 */
	public void update() {
		readInputs();
		readCredits();
		routeInputs();
	}
	
	/*
	 * Copy all of the output flits onto the corresponding link to be transmitted,
	 * close output vc connections with tail flits and update credits at upstream 
	 * nodes
	 */
	public void copy() {
		writeCreditOutputs();
		writeFlitOutputs();
	}

	/*
	 * Read each of the flit inputs and add to VC buffer, should -always- be space
	 */
	public void readInputs() {
		for(InputPort p : m_inputPorts)
			p.readInputFlit();
	}
	
	/*
	 * Read link outputs for credit updates
	 */
	public void readCredits() {
		for(OutputPort p : m_outputPorts) {
			Credit credit = p.readInputCredit();
			if(credit != null) {
				m_outputPorts[p.getPortNum()].incrementCredits(credit.getVC());
				int conInputPort = m_outputPorts[p.getPortNum()].getConnectedInputPort(credit.getVC());
				int conInputVC = m_outputPorts[p.getPortNum()].getConnectedInputVC(credit.getVC());
				m_inputPorts[conInputPort].incrementCredits(conInputVC);
				//console("Recieved credit from output port "+p.getPortNum()+" connected to ["+conInputPort+":"+conInputVC+"]");
			}
		}
	}
	
	/*
	 * Route link input flits
	 */
	private void routeInputs() {
		for(InputPort linkInput : m_inputPorts)
			routeInput(linkInput);
	}
	
	/*
	 * For a given input port, take the next flit from the next VC (round-robin) 
	 * and route it through the switch
	 */
	private void routeInput(InputPort inputPort) {
		Flit flit = inputPort.peekNextFlit();
		if(flit != null) {
			int inputVC = flit.getVC();
			int outputPort = -1;
			int outputVC = -1;
			
			// Work out the output port and vc number
			if(flit instanceof HeaderFlit) {

				int flitSrc = ((HeaderFlit)flit).getSrc();
				int flitDest = ((HeaderFlit)flit).getDest();
				int procId = m_outputPorts[m_procOutputPort].getDownStreamNodeId();
				
				// Select an output port and output virtual channel
				if(flitDest == procId) {
					outputPort = m_procOutputPort;
					outputVC = m_outputPorts[outputPort].allocVC();
				} else {
					outputPort = flitDest == procId ? m_procOutputPort : 
						m_routingFunction.getOutputPort(m_nodeId, inputVC, flitSrc, flitDest);
					int routedVC = m_routingFunction.getOutputVC(m_nodeId, inputVC, flitSrc, flitDest);
					outputVC = routedVC == -1 ? m_outputPorts[outputPort].allocVC() : 
						m_outputPorts[outputPort].allocVC(routedVC);
				}
				
				// If could not allocate VC then replace flit and wait, otherwise open a connection
				if(outputVC == -1) {
					inputPort.setWaitingVC(inputVC);
					console("could not allocate a VC for I["+inputPort.getPortNum()+":"+inputVC+"] on O["+outputPort+":?]");
					return;
				} else {
					inputPort.setupConnection(inputVC, outputPort, outputVC);
					m_outputPorts[outputPort].setupConnection(outputVC, inputPort.getPortNum(), inputVC);
					console(flit+" opening connection I["+inputPort.getPortNum()+":"+inputVC+"] to O["+outputPort+":"+outputVC+"] on dsn VC "+outputVC);
				}
				
			} else if(flit instanceof BodyFlit || flit instanceof TailFlit) {
				outputPort = inputPort.getOutputPort(flit.getVC());
				outputVC = inputPort.getOutputVC(flit.getVC());
			}

			// Only poll the input flit if the output is empty
			//m_outputPorts[outputPort].setCurrVC(outputVC);
			if(m_outputPorts[outputPort].isEmpty(outputVC)) {
				m_outputPorts[outputPort].addFlit(outputVC, inputPort.takeNextFlit());
				//console("Routing link "+linkInput.getPortNum()+" flit "+flit.toShortStr()+" to OP["+outputPort+":"+flit.getVC()+"]");
			}
		}
	}

	/*
	 * Write all pending credit messages to the links
	 */
	private void writeCreditOutputs() {
		for(int i=0; i<m_inputPorts.length; i++)
			m_inputPorts[i].writeOuputCredit();
	}
	
	/*
	 * Write all pending flits to the links
	 */
	private void writeFlitOutputs() {
		for(int i=0; i<m_outputPorts.length; i++)
			m_outputPorts[i].writeOutputFlit();
	}
	
	public String toString() {
		String s = "";
		
		// Ports
		s += "[I/O PORTS]\n\n";
		s += String.format("%-12s%-12s%-12s%-12s%-12s\n", "Port", "To", "State", "Cdts", "Buffer");
		
		for(int i=0; i<m_inputPorts.length-1; i++)
			s += m_inputPorts[i];

		for(int i=0; i<m_outputPorts.length-1; i++)
			s += m_outputPorts[i];
		
		s += "\nProcessor:\n";
		s += m_inputPorts[m_procInputPort];
		s += m_outputPorts[m_procOutputPort];
		
		// Outgoing links
		/*s += "\n\n[OUTGOING LINKS]\n\n";
		s += "Link\tFlits\tCdts\n";

		for(int i=0; i<m_outputPorts.length-1; i++)
			s += m_outputPorts[i].getLink()+"\n";
		
		s += "processor:\n";
		s += m_outputPorts[m_procOutputPort].getLink();*/
		
		s += "\n\n[ROUTING TABLE]\n\n";
		s += m_routingFunction+"";
		
		return s;
	}

	public void reset() {
		for(InputPort p : m_inputPorts)
			p.reset();
		for(OutputPort p : m_outputPorts)
			p.reset();
	}
	
	public Set<RouterLink> linkSet() {
		HashSet<RouterLink> links = new HashSet<RouterLink>();
		for(int i=0; i<m_inputPorts.length - 1; i++)
			links.add((RouterLink)m_inputPorts[i].getLink());
		for(int i=0; i<m_outputPorts.length - 1; i++)
			links.add((RouterLink)m_outputPorts[i].getLink());
		return links;
	}
	
	public Set<RouterLink> incomingLinks() {
		RouterLink[] links = new RouterLink[m_inputPorts.length-1];
		for(int i=0; i<links.length; i++)
			links[i] = (RouterLink) m_inputPorts[i].getLink();
		return new HashSet<RouterLink>(Arrays.asList(links));
	}

	public Set<RouterLink> outgoingLinks() {
		RouterLink[] links = new RouterLink[m_outputPorts.length-1];
		for(int i=0; i<m_outputPorts.length-1; i++)
			links[i] = (RouterLink) m_outputPorts[i].getLink();
		return new HashSet<RouterLink>(Arrays.asList(links));
	}
	
	public int getOutputPort(int downstreamNodeId) {
		for(int i=0; i<m_outputPorts.length; i++)
			if(m_outputPorts[i].getDownStreamNodeId() == downstreamNodeId)
				return i;
		System.err.println("Error: could not get downstream nodeId "+downstreamNodeId);
		return -1;
	}
	
	public RouterLink getLink(Router to) {
		for(RouterLink l : outgoingLinks()) {
			if(l.getTo().equals(to))
				return l;
		}
		System.err.println("Error: could not get router link to "+to.getNodeId());
		return null;
	}
	
	public int       getNodeId()          { return m_nodeId; }
	public String    getTitle()           { return "Router "+m_nodeId; }
	public void      setRoutingFn(RoutingFunction r) { m_routingFunction = r; }
	public int       inDegree()           { return m_inputPorts.length - 1; }
	public int       outDegree()          { return m_outputPorts.length - 1; }
	public boolean   equals(Router r)     { return m_nodeId == r.getNodeId(); }
	public RoutingFunction getRoutingFn() { return m_routingFunction; }
}

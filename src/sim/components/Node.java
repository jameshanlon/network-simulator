/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.components;

import java.util.LinkedList;
import java.util.Set;

import javax.swing.JPanel;

import sim.Config;
import sim.traffic.TraceEvent;

/*
 * A node represents a single processor and router element (including links between) 
 * in a network. Outgoing link components are updated() and copied() here 
 */
public class Node implements Component {

	private int           m_id;
	private Router        m_router;
	private Processor     m_processor;
	private ProcessorLink m_procRouter;
	private ProcessorLink m_routerProc;
	private RouterLink[]  m_outputLinks;
	
	public Node(int id, int numInputs, int numOutputs) {
		m_id          = id;
		m_procRouter  = new ProcessorLink(1);
		m_routerProc  = new ProcessorLink(1);
		m_router      = new Router(id, m_procRouter, m_routerProc, numInputs, numOutputs);
		m_processor   = new Processor(id, m_routerProc, m_procRouter);
		m_outputLinks = null;
	}

	public void update() {
		
		m_processor.update();
		m_router.update();
		
		m_procRouter.update();
		m_routerProc.update();
		
		for(Link l : m_outputLinks)
			l.update();
		
		if(Config.debugMode()) {
			m_processor.updateStateConsole();
			m_router.updateStateConsole();
		}
	}
	
	public void copy() {
		m_processor.copy();
		m_router.copy();
		
		m_procRouter.copy();
		m_routerProc.copy();
		
		for(Link l : m_outputLinks)
			l.copy();
	}

	public void reset() {
		m_processor.reset();
		m_processor.resetConsoles();
		m_router.reset();
		m_router.resetConsoles();
		
		m_procRouter.reset();
		m_routerProc.reset();
		
		for(Link l : m_outputLinks)
			l.reset();
	}
	
	public void connectTo(Node to, int fromPort, int toPort) {
		m_router.connectTo(to.getRouter(), fromPort, toPort);
	}
	
	public void finishConnecting() {
		Set<RouterLink> links = m_router.outgoingLinks();
		m_outputLinks = links.toArray(new RouterLink[m_router.outgoingLinks().size()]);
	} 

	public int       getId()                                { return m_id; }
	public JPanel    getRouterConsole()                     { return m_router.getConsole(); }
	public JPanel    getProcConsole()                       { return m_processor.getConsole(); }
	public Router    getRouter()                            { return m_router; }
	public void      addTrace(LinkedList<TraceEvent> trace) { m_processor.addTrace(trace); }
	public boolean   equals(Node node)                      { return getId() == node.getId(); }
	/*public void      setRouting(RoutingFunction routing)    { m_router.setRoutingFunction(routing); }
	public int       getOutputPort(int downstreamId)        { return m_router.getOutputPort(downstreamId); }
	public int       inDegree()                             { return m_router.inDegree(); }
	public int       outDegree()                            { return m_router.outDegree(); }
	public Set<RouterLink> incomingLinks()                  { return m_router.incomingLinks(); }
	public Set<RouterLink> outgoingLinks()                  { return m_router.outgoingLinks(); }
	public Set<RouterLink> linkSet()                        { return m_router.linkSet(); }*/
}

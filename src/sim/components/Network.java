/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.components;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.ext.DOTExporter;

import sim.Config;
import sim.Utilities;
import sim.routing.DimOrder;
import sim.routing.Minimal;
import sim.routing.UpDown;
import sim.topology.Topology;
import sim.topology.Visualise;

public class Network implements DirectedGraph<Router, RouterLink> {

	private Node[]       m_nodes;
	private Router[]     m_routers;
	private RouterLink[] m_links;
	
	/*
	 * Construct a new network with a topolgy, routing and traffic
	 */
	public Network() {
		
		// Check network parameters
		if(Config.availableVCs() > Config.numVCs()) {
			System.out.println("Error: insufficient virtual channels");
			return;
		}
		
		// Create network objects
		m_nodes = Topology.createTopology();
		m_routers = getNodeRouters();
		m_links = getRouterLinks();
		
		//createTopologyImage(".");
		
		System.out.println("Created network topology of "+m_nodes.length+" nodes\n");
	}
	
	public void initRouting() {
		switch(Config.routing()) {
		
		case MINIMAL:
			Minimal.configNetwork(this);
			break;
		
		case DOR:
			switch(Config.topology()) {
			case MESH:  DimOrder.configNetwork(this, true);  break;
			case TORUS: DimOrder.configNetwork(this, false); break;
			default:    System.err.println("Must use torus or mesh with DOR");
			}
			break;
		
		case UPDOWN:
			UpDown.configNetwork(this);
			break;
		}
		
		System.out.println("Initialised routing tables for "+Config.routing().name()+"\n");
	}
	
	/*private void initTraffic() {
		switch(Config.traffic()) {
		case TRACE:
			LinkedList<TraceEvent>[] m_traces = TraceParser.read(Config.traceFile());
			for(int i=0; i<m_nodes.length; i++)
				m_nodes[i].addTrace(m_traces[i]);
			break;
		}
	}*/

	public void step() {
		for(Node n : m_nodes)
			n.update();
		
		for(Node n : m_nodes)
			n.copy();
	}
	
	public void reset() {
		for(Node n : m_nodes)
			n.reset();
	}
	
	private RouterLink[] getRouterLinks() {
		List<RouterLink> links = new LinkedList<RouterLink>();
		for(int i=0; i<m_nodes.length; i++)
			links.addAll(m_nodes[i].getRouter().linkSet());
		return links.toArray(new RouterLink[links.size()]);
	}
	
	private Router[] getNodeRouters() {
		Router[] routers = new Router[m_nodes.length];
		for(int i=0; i<m_nodes.length; i++)
			routers[i] = m_nodes[i].getRouter();
		return routers;
	}
	
	public int numLinks() {
		int count = 0;
		for(Router r : m_routers)
			count += r.outDegree();
		return count;
	}
	
	public void createTopologyImage(String directory) {
		String file = directory+"/topology_"+Topology.getDescriptor();
		//Visualise.writeDot(this, file+".dot");
		Utilities.writeFile(file+".dot", Visualise.createDotFileString(this));
		Visualise.createDotImage(file+".dot", file+".ps");
	}
	
	public void writeDot(String filename) {
		DOTExporter<Router, RouterLink> export = new DOTExporter<Router, RouterLink>();
		try {
	        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
	        export.export(out, this);
	    } catch (IOException e) {
	    	System.err.println("Error: could not write spanning tree dot file");
	    }
	}
	
	public int getEdgeId(Router routerFrom, Router routerTo) {
		for(RouterLink l : m_links) {
			if(l.getFrom().equals(routerFrom) && l.getTo().equals(routerTo))
				return l.getId();
		}
		return -1;
	}
	

	public RouterLink getLink(int id) {
		for(RouterLink l : m_links) {
			if(l.getId() == id)
				return l;
		}
		return null;
	}
	
	public int      numNodes()   { return m_nodes.length; }
	public Node[]   getNodes()   { return m_nodes; }
	public Router[] getRouters() { return m_routers; }
	
	/*
	 * JGraphT methods
	 */
	public int             inDegreeOf(Router r)        { return r.inDegree(); }
	public int             outDegreeOf(Router r)       { return r.outDegree(); }
	public Set<RouterLink> incomingEdgesOf(Router r)   { return r.incomingLinks(); }
	public Set<RouterLink> outgoingEdgesOf(Router r)   { return r.outgoingLinks(); }
	public Set<RouterLink> edgesOf(Router r)           { return r.linkSet(); }
	public Set<RouterLink> edgeSet()                   { return new HashSet<RouterLink>(Arrays.asList(m_links)); }
	public Set<Router>     vertexSet()                 { return new HashSet<Router>(Arrays.asList(m_routers)); }
	public Router          getEdgeSource(RouterLink l) { return l.getFrom(); }
	public Router          getEdgeTarget(RouterLink l) { return l.getTo(); }
	public double          getEdgeWeight(RouterLink l) { return l.getDelay(); }
	
	public boolean containsEdge(RouterLink link) {
		for(RouterLink l : m_links)
			if(l.equals(link)) return true;
		return false;
	}

	public boolean containsEdge(Router src, Router dest) {
		for(RouterLink l : m_links)
			if(l.connects(src, dest)) return true;
		return false;
	}

	public boolean containsVertex(Router router) {
		for(Router r : m_routers)
			if(router.equals(r)) return true;
		return false;
	}

	public Set<RouterLink> getAllEdges(Router src, Router dest) {
		HashSet<RouterLink> links = new HashSet<RouterLink>();
		for(RouterLink l : m_links)
			if(l.connects(src, dest)) links.add(l);
		return links.size() > 0 ? links : null;
	}

	public RouterLink getEdge(Router src, Router dest) {
		for(RouterLink l : m_links)
			if(l.connects(src, dest)) return l;
		return null;
	}
	
	/*
	 * Prohibited JGraphT methods
	 */
	public RouterLink addEdge(Router n1, Router n2) {
		System.err.println("Warning: cannot edd edge");
		return null;
	}

	public boolean addEdge(Router n1, Router n2, RouterLink l) {
		System.err.println("Warning: cannot edd edge");
		return false;
	}

	public boolean addVertex(Router n) {
		System.err.println("Warning: cannot edd vertex");
		return false;
	}
	
	public boolean removeAllEdges(Collection<? extends RouterLink> arg0) {
		System.err.println("Warning: cannot remove all links");
		return false;
	}

	public Set<RouterLink> removeAllEdges(Router arg0, Router arg1) {
		System.err.println("Warning: cannot remove all links");
		return null;
	}

	public boolean removeAllVertices(Collection<? extends Router> arg0) {
		System.err.println("Warning: cannot remove all links");
		return false;
	}

	public boolean removeEdge(RouterLink link) {
		System.err.println("Warning: cannot remove link");
		return false;
	}

	public RouterLink removeEdge(Router src, Router dest) {
		System.err.println("Warning: cannot remove link");
		return null;
	}

	public boolean removeVertex(Router node) {
		System.err.println("Warning: cannot remove node");
		return false;
	}
	
	public EdgeFactory<Router, RouterLink> getEdgeFactory() {
		System.err.println("Warning: cannot get EdgeFactory");
		return null;
	}
	
}

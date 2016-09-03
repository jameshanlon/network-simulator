/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.graphs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/*
 * Directed graph implementation of a spanning tree
 */
public class Directed extends DefaultDirectedGraph<DNode, DEdge>
implements DirectedGraph<DNode, DEdge> {

	private static final long serialVersionUID = 1L;
	private DNode[]           m_nodes;
	
	public Directed(int numNodes) {
		super(DEdge.class);
		//System.out.println("new directed graph "+numNodes+" num nodes");
		m_nodes = new DNode[numNodes];
		for(int i=0; i<m_nodes.length; i++) {
			m_nodes[i] = new DNode(i);
			addVertex(m_nodes[i]);
		}
	}
	
	public void addEdge(int source, int target) {
		addEdge(m_nodes[source], m_nodes[target], new DEdge(source, target));
	}
	
	public void removeEdge(int source, int target) {
		removeEdge(m_nodes[source], m_nodes[target]);		
	}
	
	public boolean containsCycle() {
		CycleDetector<DNode, DEdge> cd = new CycleDetector<DNode, DEdge>(this);
		return cd.detectCycles();
	}
	
	public boolean containsEdge(int source, int target) {
		return containsEdge(m_nodes[source], m_nodes[target]);
	}
	
	/*
	 * Return the list of routers in the shortest path between src and dest
	 */
	public int[] getPath(int source, int target) {
		List<DEdge> path = BellmanFordShortestPath.findPathBetween(
				this, m_nodes[source], m_nodes[target]);
		int[] p = new int[path.size()+1];
		for(int i=0; i<path.size(); i++)
			p[i] = path.get(i).getSrc();
		p[path.size()] = path.get(path.size()-1).getDst();
		return p;
	}
	
	/*
	 * Return the next node in the shortest path
	 */
	public int getNextNode(int source, int target) {
		//System.out.print("shortest path between "+source+" and "+target+" is ");
		List<DEdge> path = BellmanFordShortestPath.findPathBetween(this, m_nodes[source], m_nodes[target]);
		//List<DEdge> path = DijkstraShortestPath.findPathBetween(this, m_nodes[source], m_nodes[target]);
		//System.out.println(" length "+path.size());
		return path.get(0).getDst();
	}

	public void writeDot(String filename) {
		DOTExporter<DNode, DEdge> export = new DOTExporter<DNode, DEdge>();
		try {
	        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
	        export.export(out, this);
	    } catch (IOException e) {
	    	System.err.println("Error: could not write channel dependency dot file");
	    }
	}
}

/*
 * Channel dependency node
 */
class DNode {

	private int m_id;
	
	public DNode(int id) {
		m_id = id;
	}
	
	public int getId() { return m_id; }
}

/*
 * Channel dependency edge
 */
class DEdge extends DefaultEdge {

	private static final long serialVersionUID = 1L;
	private int m_source;
	private int m_target;
	
	public DEdge(int source, int target) {
		m_source = source;
		m_target = target;
	}
	
	public int getSrc() { return m_source; }
	public int getDst() { return m_target; }
}

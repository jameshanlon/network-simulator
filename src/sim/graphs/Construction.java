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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import sim.components.Node;

/*
 * Directed graph implementation of a spanning tree
 */
public class Construction extends DefaultDirectedGraph<RNode, REdge>
implements DirectedGraph<RNode, REdge> {

	private static final long serialVersionUID = 1L;
	private RNode[] m_nodes;
	
	public Construction(int numNodes) {
		super(REdge.class);
		m_nodes = new RNode[numNodes];
		for(int i=0; i<m_nodes.length; i++) {
			m_nodes[i] = new RNode(i);
			addVertex(m_nodes[i]);
		}
	}
	
	public boolean addEdge(int source, int target) {
		if(addEdge(m_nodes[source], m_nodes[target], new REdge(source, target)) && 
	 			addEdge(m_nodes[target], m_nodes[source], new REdge(target, source))) {
			return true;
		} else {
			return false;
		}
	}
	
	public RNode getNode(int node) {
		return m_nodes[node];
	}
	
	public int outDegreeOf(int node) {
		return this.outDegreeOf(m_nodes[node]);
	}

	public int numConnectedComponents() {
		ConnectivityInspector<RNode, REdge> i = new ConnectivityInspector<RNode, REdge>(this);
		return i.connectedSets().size();
	}
	
	public boolean removeRandomEdge(Random rand) {
		int numAttempts = 100;
		List<REdge> edges = new LinkedList<REdge>(this.edgeSet());
		while(numAttempts > 0) {
			numAttempts--;
			REdge edge = edges.get(rand.nextInt(edges.size()));
			if(removeEdge(m_nodes[edge.getSrc()], m_nodes[edge.getDst()]) != null &&
					removeEdge(m_nodes[edge.getDst()], m_nodes[edge.getSrc()]) != null) {
				if(numConnectedComponents() == 1) {
					//System.out.println("removed edge");
					return true;
				}
				this.addEdge(edge.getSrc(), edge.getDst());
			} else {
				System.out.println("Could not remove edge");
			}
		}
		return false;
	}
	
	public Node[] buildTopology() {
		
		// Build an adjacency list to get port numbers
		int[][] adjList = buildAdjList();
		//System.out.println(dumpAdjList(adjList));
		System.out.print("Building topology from adjacency list...");
		
		// Create all of the nodes
		Node[] nodes = new Node[this.vertexSet().size()];
		for(int node=0; node<nodes.length; node++) {
			int outgoingLinks = this.outDegreeOf(this.getNode(node));
			int incomingLinks = this.inDegreeOf(this.getNode(node));
		    nodes[node] = new Node(node, incomingLinks, outgoingLinks);
		}
		
		// Connect them
		for(int i=0; i<adjList.length; i++) {
			for(int j=0; j<adjList[i].length; j++) {
				Node to = nodes[adjList[i][j]];
				int fromPort = j;
				int toPort = getPort(adjList, adjList[i][j], i);
				nodes[i].connectTo(to, fromPort, toPort);
			}
			nodes[i].finishConnecting();
		}
		
		System.out.println("done.\n");
		return nodes;
	}
	
	private static int getPort(int[][] adjList, int node, int from) {
		for(int i=0; i<adjList[node].length; i++)
			if(adjList[node][i] == from)
				return i;
		System.err.println("Error: could not find input port for node "+from);
		return -1;
	}
	
	private int[][] buildAdjList() {
		int[][] list = new int[m_nodes.length][];
		for(int i=0; i<m_nodes.length; i++) {
			list[i] = new int[this.outDegreeOf(m_nodes[i])];
			int j = 0;
			for(REdge e : this.outgoingEdgesOf(m_nodes[i])) {
				list[i][j] = e.getDst();
				j++;
			}
		}
		return list;
	}
	
	public static String dumpAdjList(int[][] adjList) {
		String s = "";
		for(int i=0; i<adjList.length; i++) {
			s += "["+i+"] ";
			for(int j=0; j<adjList[i].length; j++)
				s += adjList[i][j]+" ";
			s += "\n";
		}
		return s;
	}

	public void writeDot(String filename) {
		DOTExporter<RNode, REdge> export = new DOTExporter<RNode, REdge>();
		try {
	        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
	        export.export(out, this);
	    } catch (IOException e) {
	    	System.err.println("Error: could not write spanning tree dot file");
	    }
	}
}

class RNode {

	private int m_id;
	
	public RNode(int id) {
		m_id = id;
	}
	
	public int getId() { return m_id; }
}

class REdge extends DefaultEdge {

	private static final long serialVersionUID = 1L;
	private int m_source;
	private int m_target;
	
	public REdge(int source, int target) {
		m_source = source;
		m_target = target;
	}
	
	public int getSrc() { return m_source; }
	public int getDst() { return m_target; }
}
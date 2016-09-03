/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.topology;

import sim.Config;
import sim.components.Node;

public class Topology {

	public static Node[] createTopology() {
		
		switch(Config.topology()) {
		
		case MESH:
			return Mesh.create(Config.k(), Config.n());
		
		case TORUS:
			return Torus.create(Config.k(), Config.n());
			
		case DEGENMESH:
			return DegenMesh.create(Config.k(), Config.n(), Config.faults()).buildTopology();
			
		case DEGENTORUS:
			return DegenTorus.create(Config.k(), Config.n(), Config.faults()).buildTopology();
		
		default:
			return null;
		}
	}
	
	public static String getDescriptor() {
		String desc = Config.topology().name()+"(";
		
		switch(Config.topology()) {
		
		case MESH:
			desc +=  Config.k()+","+Config.n();
			break;
		
		case TORUS:
			desc +=  Config.k()+""+Config.n();
			break;
		}
		
		return desc += ")";
	}
		    
	/*
	 * Create the network topology with an adjacency list
	 *
	private static Node[] buildTopology(Construction graph) {
		
		//System.out.println(dumpAdjList(adjList));
		System.out.print("Building topology from adjacency list...");
		
		// Create all of the nodes
		Node[] nodes = new Node[graph.vertexSet().size()];
		for(int node=0; node<nodes.length; node++) {
			int outgoingLinks = graph.outDegreeOf(graph.getNode(node));
			int incomingLinks = graph.inDegreeOf(graph.getNode(node));
		    nodes[node] = new Node(node, incomingLinks, outgoingLinks);
		}

		// Connect them
		for(RNode n : graph.vertexSet())
		
		
		for(int i=0; i<adjList.length; i++) {
			for(int j=0; j<adjList[i].length; j++) {
				Node to = nodes[adjList[i][j]];
				int fromPort = j;
				int toPort = getPort(adjList, adjList[i][j], i);
				nodes[i].connectTo(to, fromPort, toPort);
			}
			nodes[i].finishConnecting();
		}
		
		System.out.println("done.");
		return nodes;
	}

	private static int getPort(int[][] adjList, int node, int from) {
		for(int i=0; i<adjList[node].length; i++)
			if(adjList[node][i] == from)
				return i;
		System.err.println("Error: could not find input port for node "+from);
		return -1;
	}

	private static int countIncomingLinks(int[][] adjList, int node) {
		int count = 0;
		for(int i=0; i<adjList.length; i++) {
			for(int j=0; j<adjList[i].length; j++) {
				if(adjList[i][j] == node)
					count++;
			}
		}
		return count;
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
	}*/
}

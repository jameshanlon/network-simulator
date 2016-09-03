/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.routing;

import java.util.LinkedList;

import sim.Simulator;
import sim.components.Network;
import sim.components.Router;
import sim.components.RouterLink;
import sim.graphs.Directed;

public class UpDown extends TableBased {
	
	public UpDown() {
		super();
	}
	
	public static void configNetwork(Network network) {
		Directed st = BFS(network);
		
		Router[] routers = network.getRouters();
		
		// Fill the routing tables according to shortest paths in the spanning tree
		for(int i=0; i<routers.length; i++) {
			
			UpDown upDown = new UpDown();
			routers[i].setRoutingFn(upDown);
			
			for(int j=0; j<routers.length; j++) {
				if(i != j) {
					int nextNode = st.getNextNode(i, j);
					if(nextNode == -1) {
						System.err.println("Error: no path between nodes "+i+" and "+j);
					} else {
						int outputPort = routers[i].getOutputPort(nextNode);
						upDown.addTableEntry(j, outputPort, -1);
					}
				}
			}
		}
	}
	
	/*
	 * Perform a breadth first search of the graph to construct a spanning tree
	 */
	public static Directed BFS(Network network) {
		
		Router[] routers = network.getRouters();
		
		int[] parent = new int[network.numNodes()];
		for(int i=0; i<parent.length; i++)
			parent[i] = -1;
		
		LinkedList<Integer> next = new LinkedList<Integer>();
		int start = Simulator.getRandInt(network.numNodes());
		next.addLast(start);
		parent[start] = start;
		//System.out.println("Spanning tree root at node "+start);
		   
		while (!next.isEmpty()) {
			int current = next.poll();

		    // Look through neighbors, if unvisited add it to the queue
		    for(RouterLink outLink : network.outgoingEdgesOf(routers[current])) {
		    	int neighbour = outLink.getTo().getNodeId();
		    	//System.out.println(current+" -> "+neighbour);
		        
		    	if (parent[neighbour] == -1) {
		        	parent[neighbour] = current;
		        	next.push(neighbour);
		        }
		    }
		}
		
		//for(int i=0; i<parent.length; i++)
		//	System.out.println(i+" parent "+parent[i]);
		
		Directed st = new Directed(network.numNodes());

		// Add edges except root node's self edge
		for(int i=0; i<parent.length; i++) {
			if(i != parent[i]) {
				st.addEdge(i, parent[i]);
				st.addEdge(parent[i], i);
			}
		}
		
		//st.writeDot("st.dot");
		//Visualise.createDotImage("st.dot", "st.ps");
		
		return st;
	}

}
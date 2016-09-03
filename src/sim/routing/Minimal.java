/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.routing;

import java.util.List;
import java.util.Set;

import org.jgrapht.alg.BellmanFordShortestPath;

import sim.components.Network;
import sim.components.Router;
import sim.components.RouterLink;
import sim.graphs.Directed;

/*
 * This routing function deterministically routes along the shortest 
 * paths, does not avoid deadlock
 */
public class Minimal extends TableBased {
	
	public Minimal() {
		super();
	}

	/*
	 * For each router, calc shortest path to each other node. Write the corresponding 
	 * output port in its routing table
	 */
	public static void configNetwork(Network network) {

		/*Directed cdg = createFullCDG(network);
		cdg.writeDot("cdg.dot");
		Visualise.createDotImage("cdg.dot", "cdg.ps");
		System.out.println("blah");
		System.exit(1);*/
		
		Router[] routers = network.getRouters();
		
		for(int i=0; i<routers.length; i++) {
			
			BellmanFordShortestPath<Router, RouterLink> sp = 
				new BellmanFordShortestPath<Router, RouterLink>(network, routers[i]);
			Minimal min = new Minimal();
			routers[i].setRoutingFn(min);
			
			for(int j=0; j<routers.length; j++) {
				if(i != j) {
					List<RouterLink> path = sp.getPathEdgeList(routers[j]);
					
					if(path == null) {
						System.err.println("Error: no path between nodes "+i+" and "+j);
					} else {
						int outputPort = routers[i].getOutputPort(path.get(0).getTo().getNodeId());
						min.addTableEntry(routers[j].getNodeId(), outputPort, -1);
					}
				}
			}
		}
	}
	
	public static Directed createFullCDG(Network network) {
		Router[] nodes = network.getRouters();
		Directed cdg = new Directed(network.numLinks());
		
		for(int i=0; i<nodes.length; i++) {
			Set<RouterLink> in = nodes[i].incomingLinks();
			Set<RouterLink> out = nodes[i].outgoingLinks();
			
			for(RouterLink inLink : in) {
				for(RouterLink outLink : out) {
					cdg.addEdge(inLink.getId(), outLink.getId());
				}
			}
		}
		
		return cdg;
	}
}

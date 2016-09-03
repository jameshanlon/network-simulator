/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.topology;

import java.util.Random;

import sim.Config;
import sim.graphs.Construction;

public class DegenTorus {

	private static int _k;
	private static int _n;
	
	/*
	 * For each node and each dimension, make the left and right connections,
	 * except on edges where posInDim==0 or k-1
	 * 
	 * Dimension i: (L) <-- (N) --> (R)
	 */
	public static Construction create(int k, int n, double faults) {

		_k = k;
		_n = n;
		
		int numNodes = (int) Math.pow(_k, _n);
		Construction graph = new Construction(numNodes);
 
        for(int node=0; node<numNodes; node++) {
			
			for(int dim = 0; dim < n; dim++) {

				//System.out.println("Dimension "+dim);
				
			    int leftNode  = getLeftNode(node, dim);
			    int rightNode = getRightNode(node, dim);
			  
			    if(leftNode != -1) {
			    	graph.addEdge(node, leftNode);
			    }
			
			    if(rightNode != -1) {
			    	graph.addEdge(node, rightNode);
			    }
		    }

		}
	
        // Remove links as faults
        Random rand = new Random(Config.graphSeed());
        int numLinkFaults = (int)((double)graph.vertexSet().size() * faults);
        int faultCount = 0;
        
        while(faultCount < numLinkFaults) {
        	if(graph.removeRandomEdge(rand)) {
        		faultCount++;
        	} else {
        		System.err.println("Error: cannot remove any links without disconnecting graph");
        	}
        }
        
        //graph.writeDot("degen_mesh.dot");
		//Visualise.createDotImage("degen_mesh.dot", "degen_mesh.ps");
        
		System.out.println("Created degenerated torus topology with "+(100.0*faults)+"% ("+faultCount+") faults");
		return graph;
	}
	
	private static int getLeftNode(int node, int dim) {

	  int kToDim = (int) Math.pow(_k, dim);
	  int posInDim = (node / kToDim) % _k;

	  // if at the left edge of the dimension, wrap-around
	  return posInDim == 0 ? (node + (_k-1)*kToDim) : (node - kToDim);
	}

	private static int getRightNode(int node, int dim) {

	  int kToDim = (int) Math.pow(_k, dim);
	  int posInDim = (node / kToDim) % _k;

	  // if at the right edge of the dimension, wrap-around
	  return posInDim == _k-1 ? (node - (_k-1)*kToDim) : (node + kToDim);
	}
		
}

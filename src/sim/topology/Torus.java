/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.topology;

import sim.components.Node;

public class Torus {

	private static int _k;
	private static int _n;
	
	private Torus() {}
	
	/*
	 * For each node and each dimension, make the left and right connections
	 * 
	 * dimension i: (L) <-- (N) --> (R)
	 */
	public static Node[] create(int k, int n) {

		_k = k;
		_n = n;
		
		int numNodes = (int) Math.pow(_k, n);
		Node[] nodes = new Node[numNodes];
		
		// Create all the nodes
		for(int node=0; node<numNodes; node++)
		    nodes[node] = new Node(node, 2*_n, 2*_n);
		
		// Connect them
		for(int node=0; node<nodes.length; node++) {
			
			for(int dim = 0; dim < _n; dim++) {

		      int leftNode  = getLeftNode(node, dim);
		      int rightNode = getRightNode(node, dim);
		      
		      int leftPort = 2*dim;
		      int rightPort  = 2*dim+1;
		      
		      nodes[node].connectTo(nodes[leftNode], leftPort, rightPort);
		      nodes[node].connectTo(nodes[rightNode], rightPort, leftPort);
		    }

		    nodes[node].finishConnecting();
		}
		return nodes;
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
	
	/*private static void insertRandomFaults() {
		
	}*/
}

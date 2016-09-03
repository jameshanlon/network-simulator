/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.topology;

import sim.components.Node;

public class Mesh {

	private static int _k;
	private static int _n;
	
	/*
	 * For each node and each dimension, make the left and right connections,
	 * except on edges where posInDim==0 or k-1
	 * 
	 * Dimension i: (L) <-- (N) --> (R)
	 */
	public static Node[] create(int k, int n) {

		_k = k;
		_n = n;
		
		int numNodes = (int) Math.pow(_k, _n);
		Node[] nodes = new Node[numNodes];
		
		// Create all the nodes
		for(int node=0; node<numNodes; node++) {
			int connections = getNumConns(node);
		    nodes[node] = new Node(node, connections, connections);
		}
		
		// Connect them
		for(int node=0; node<nodes.length; node++) {
			
			for(int dim = 0; dim < n; dim++) {

				//System.out.println("Dimension "+dim);
				
			    int leftNode  = getLeftNode(node, dim);
			    int rightNode = getRightNode(node, dim);
			  
			    if(leftNode != -1) {
			    	int leftPort = getLeftPort(node, dim);
			  	    int leftNodeRightPort = getRightPort(leftNode, dim);
				    //System.out.println("Connecting node "+node+"["+leftPort+"] to leftNode "+leftNode+"["+leftNodeRightPort+"]");
				    nodes[node].connectTo(nodes[leftNode], leftPort, leftNodeRightPort);
			    }
			
			    if(rightNode != -1) {
			    	int rightPort = getRightPort(node, dim);
				    int rightNodeLeftPort = getLeftPort(rightNode, dim);
				    //System.out.println("Connecting node "+node+"["+rightPort+"] to rightNode "+rightNode+"["+rightNodeLeftPort+"]");
				    nodes[node].connectTo(nodes[rightNode], rightPort, rightNodeLeftPort);
			    }
		    }

		    nodes[node].finishConnecting();
		}
	
		return nodes;
	}

	/*
	 * Return the number of the base (i.e. left) port in dimension dim
	 */
	public static int getLeftPort(int node, int dim) {
		
		int port = -1;
		
		//System.out.print("get left port of node "+node+": ");
		for(int d = 0; d <= dim; d++) {
			int kToD = (int) Math.pow(_k, d);
			int posInDim = (node / kToD) % _k;
			
			//System.out.print("D"+d+" pos="+posInDim);
			// For each available direction in a dimension, add a port
			if(posInDim > 0)
				port++;
			if(posInDim < _k-1 && d < dim)
				port++;
		}
		
		//System.out.println(" PORT = "+port);
		return port;
	}
	
	public static int getRightPort(int node, int dim) {
		
		int port = -1;
		
		//System.out.print("get right port of node "+node+": ");
		for(int d = 0; d <= dim; d++) {
			int kToD = (int) Math.pow(_k, d);
			int posInDim = (node / kToD) % _k;
			
			//System.out.print("D"+d+" pos="+posInDim);
			// For each available direction in a dimension, add a port
			if(posInDim > 0)
				port++;
			if(posInDim < _k-1)
				port++;
		}
		
		//System.out.println(" PORT = "+port);
		return port;
	}
	
	/*
	 * Return the number of connections (input and output) to a node
	 */
	private static int getNumConns(int node) {
		
		int connections = 2*_n;
		
		// For each dimension, take off a connection if at edge
		for(int dim = 0; dim < _n; dim++) {
			int kToDim = (int) Math.pow(_k, dim);
			int posInDim = (node / kToDim) % _k;
			
			if(posInDim == 0 || posInDim == _k-1)
				connections--;
		}
		
		return connections;
	}
	
	private static int getLeftNode(int node, int dim) {

	  int kToDim = (int) Math.pow(_k, dim);
	  int posInDim = (node / kToDim) % _k;

	  // if at the left edge of the dimension, return no node (-1)
	  return posInDim == 0 ? -1 : (node - kToDim);
	}

	private static int getRightNode(int node, int dim) {

	  int kToDim = (int) Math.pow(_k, dim);
	  int posInDim = (node / kToDim) % _k;

	  // if at the right edge of the dimension, return no node (-1)
	  return posInDim == _k-1 ? -1 : (node + kToDim);
	}
	
	/*private static void insertRandomFaults() {
		
	}*/

}

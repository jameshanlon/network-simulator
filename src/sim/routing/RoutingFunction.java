/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.routing;

public interface RoutingFunction {

	public int getOutputPort(int current, int inputVC, int source, int dest);
	public int getOutputVC(int current, int inputVC, int source, int dest);
	
}

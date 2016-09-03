/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.traffic;

public class TraceElement {

	public long clock;
	public int  source;
	public int  dest;
	public int  burst;
	
	public TraceElement(long clock, int source, int dest, int burst) {
		this.clock = clock;
		this.source = source;
		this.dest = dest;
		this.burst = burst;
	}
}

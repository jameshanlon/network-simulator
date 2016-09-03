/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.traffic;

public class TraceEvent {

	public long clock;
	public int  dest;
	public int  burst;
	
	public TraceEvent(long clock, int dest, int burst) {
		this.clock = clock;
		this.dest = dest;
		this.burst = burst;
	}
}

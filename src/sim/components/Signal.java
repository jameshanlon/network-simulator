/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.components;

public class Signal {

	private int m_transitCycles;
	
	public Signal() {
		m_transitCycles = 0;
	}
	
	public void incTransitCycles()  { m_transitCycles++; }
	public int  getTransitCycles()  { return m_transitCycles; }
	public void zeroTransitCycles() { m_transitCycles = 0; }
	
}

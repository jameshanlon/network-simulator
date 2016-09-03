/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.components;

public class RouterLink extends Link {

	private static int m_idCount = 0;
	
	private Router     m_from;
	private Router     m_to;

	public RouterLink(int delay) {
		super(m_idCount++, delay);
	}

	public void connectFrom(Router from, OutputPort fromPort) {
		m_from = from;
		connectFrom(fromPort);
	}

	public void connectTo(Router to, InputPort toPort) {
		m_to = to;
		connectTo(toPort);
	}
	
	public Router  getTo()                           { return m_to; }
	public Router  getFrom()                         { return m_from; }
	public int     getToId()                         { return m_to.getNodeId(); }
	public int     getFromId()                       { return m_from.getNodeId(); }
	public boolean connects(Router src, Router dest) { return m_from.equals(src) && m_to.equals(dest); }
}

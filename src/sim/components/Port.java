/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.components;

public class Port {
	
	private Console m_console;
	private int     m_nodeId;
	private int     m_portNum;
	private int     m_currVC;
	private Link    m_link;
	
	public Port(Console console, int nodeId, int portNum, Link link) {
		m_console = console;
		m_nodeId = nodeId;
		m_portNum = portNum;
		m_link = link;
		m_currVC = 0;
	}
	
	public int  getNodeId()            { return m_nodeId; }
	public int  getPortNum()           { return m_portNum;	}
	public Link getLink()              { return m_link; }
	public int  getCurrVC()            { return m_currVC; }
	public void setCurrVC(int vcIndex) { m_currVC = vcIndex; }
	public void console(String s)      { m_console.console(s); }
}

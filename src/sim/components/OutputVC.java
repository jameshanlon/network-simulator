/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.components;

public class OutputVC extends VirtualChannel {

	private int  m_connectedInputPort;
	private int  m_connectedInputVC;
	private Flit m_flit;
	
	public OutputVC(int portNum, int index, Console console) {
		super(portNum, index, console);
		m_flit = null;
	}
	
	public void addFlit(Flit flit) {
		if(m_flit != null) {
			System.err.println(flit+": output VC O["+getPortNum()+":"+getIndex()+"] not empty: contains "+m_flit);
			console(flit+": output VC O["+getPortNum()+":"+getIndex()+"] not empty: contains "+m_flit);
		}
		m_flit = flit;
		m_flit.setVC(getIndex());
	}
	
	public Flit takeFlit() {
		if(isActive()) {
			Flit flit = m_flit;
			if(flit instanceof TailFlit)
				closeConnection();
			decCredits();
			m_flit = null;
			return flit;
		} else {
			return null;
		}
	}
	
	/*
	 * Register the input port and VC with this output port
	 */
	public void setupConnection(int inputPortNum, int inputVC) {
		m_connectedInputPort = inputPortNum;
		m_connectedInputVC = inputVC;
		setActive();
	}
	
	public void closeConnection() {
		popState();
		console("closing connection O["+getPortNum()+":"+getIndex()+"]");
	}
	
	public String toString(int downstreamNodeId, int downstreamNodePort) {
		String to = (isIdle()?"-":""+downstreamNodeId+":I["+downstreamNodePort+":"+getIndex()+"]");
		String flit = m_flit != null ? m_flit.toString() : "";
		return String.format("%-12s%-12s%-12s", to, getStateStr(), getCredits(), flit);
	}

	public void resetState() {
		m_flit = null;
		resetVCState();
	}

	public int     getConnectedInputPort() { return m_connectedInputPort; }
	public int     getConnectedInputVC()   { return m_connectedInputVC; }
	public boolean hasFlit()               { return m_flit != null; }
}

/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.components;

public class InputVC extends VirtualChannel {

	private int    m_connectedOutputPort;
	private int    m_connectedOutputVC;
	private Buffer m_buffer;
	
	public InputVC(int portNum, int index, Console console, int bufferSize) {
		super(portNum, index, console);
		m_buffer = new Buffer(bufferSize, console);
	}
	
	public boolean addFlit(Flit flit) {
		if(isIdle())
			setActive();
		return m_buffer.offerFlit(flit);
	}
	
	public Flit takeFlit() {
		if(isActive() || isWaitingVC()) {
			Flit flit = m_buffer.takeFlit();
			if(flit instanceof TailFlit)
				closeConnection();
			decCredits();
			return flit;
		} else {
			return null;
		}
	}
	
	public void setupConnection(int outputPort, int outputVC) {
		m_connectedOutputPort = outputPort;
		m_connectedOutputVC = outputVC;
		if(isWaitingVC()) {
			//console("setup connection on IVC in state WAIT_VC, stack = "+getStateStack());
			popState();
		}
	}
	
	public void closeConnection() {
		console("closing connection I["+getPortNum()+":"+getIndex()+"] state: "+getStateStack());
		popState();
	}
	
	public void replaceHeadFlit(Flit flit) {
		m_buffer.replaceHeadFlit(flit);
		incCredits();
	}
	
	public void resetState() {
		m_buffer.clear();
		resetVCState();
	}
	
	public String toString() {
		String to = (isIdle()?"-":"O["+m_connectedOutputPort+":"+m_connectedOutputVC+"]");
		return String.format("%-12s%-12s%-12s%12s", to, getStateStr(), getCredits()+"", m_buffer.toString());
	}

	public Flit     peekFlit()                  { return m_buffer.peekFlit(); }
	public boolean  isEmpty()                   { return !m_buffer.hasFlits(); }
	public int      getOutputPort()             { return m_connectedOutputPort; }
	public int      getOuputVC()                { return m_connectedOutputVC; }
	public int      freeSlots()                 { return m_buffer.freeSlots(); }
	public boolean  hasFlit()                   { return peekFlit() != null; }
}

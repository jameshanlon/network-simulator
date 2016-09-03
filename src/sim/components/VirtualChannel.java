/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.components;

import java.util.LinkedList;

import sim.Config;

public abstract class VirtualChannel {
	
	private enum VCState { IDLE, ACTIVE, WAIT_C, WAIT_VC };
	
	private Console    m_console;
	private int        m_portNum;
	private int        m_index;
	private StateStack m_state;
	private int        m_credits;
	
	private class StateStack {
		private LinkedList<VCState> m_stack;

		public StateStack() {
			m_stack = new LinkedList<VCState>();
			m_stack.push(VCState.IDLE);
		}
		
		public void reset() {
			m_stack.clear();
			m_stack.push(VCState.IDLE);
		}
		
		public String toString() {
			String s = "";
			for(VCState vcs : m_stack)
				s += vcs.name()+" ";
			return s;
		}

	   	public void    push(VCState s) { m_stack.addFirst(s); }  
	    public VCState pop()           { return m_stack.pollFirst(); }
	    public VCState peek()          { return m_stack.peekFirst(); }
	}
	
	public VirtualChannel(int portNum, int index, Console console) {
		m_portNum = portNum;
		m_index = index;
		m_console = console;
		m_state = new StateStack();
		m_credits = Config.bufferSize();
	}

	public int incCredits() {
		if(isWaitingCdt())
			m_state.pop();
		
		// bit of a hack...
		if(hasFlit() && isIdle())
			setActive();

		return m_credits++;
	}
	
	public int decCredits() {
		if(getCredits() == 1)
			m_state.push(VCState.WAIT_C);
		return m_credits--;
	}
	
	public void resetVCState() {
		m_state.reset();
		m_credits = Config.bufferSize();
	}
	
	public abstract boolean hasFlit();
	
	public String  getStateStr()           { return m_state.peek().name(); }
	public void    popState()              { m_state.pop(); }
	public void    setActive()             { if(!isActive()) m_state.push(VCState.ACTIVE); }
	public void    setWaitingVC()          { if(!isWaitingVC()) m_state.push(VCState.WAIT_VC); }
	public boolean isIdle()                { return m_state.peek().equals(VCState.IDLE); }
	public boolean isActive()              { return m_state.peek().equals(VCState.ACTIVE); }
	public boolean isWaitingCdt()          { return m_state.peek().equals(VCState.WAIT_C); }
	public boolean isWaitingVC()           { return m_state.peek().equals(VCState.WAIT_VC); }
	public int     getCredits()            { return m_credits; }
	public boolean hasFullCredits()        { return m_credits == Config.bufferSize(); }
	public int     getPortNum()            { return m_portNum; }
	public int     getIndex()              { return m_index; }
	public void    console(String text)    { m_console.console(text); }
	public String  getStateStack()         { return m_state+""; }
}
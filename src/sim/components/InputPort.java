/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.components;

import sim.Config;

public class InputPort extends Port {

	private Flit      m_inputFlit;
	private InputVC[] m_vcs;
	private boolean   m_sendCredit;
	
	public InputPort(Console console, int nodeId, int portNum, Link link, int numVCs) {
		super(console, nodeId, portNum, link);
		m_vcs = new InputVC[numVCs];
		for(int i=0; i<m_vcs.length; i++)
			m_vcs[i] = new InputVC(portNum, i, console, Config.bufferSize());
		m_sendCredit = false;
		m_inputFlit = null;
	}
	
	/*
	 * Read an input flit and add it to a VC buffer
	 */
	public void readInputFlit() {
		if(m_inputFlit != null) {
			m_vcs[m_inputFlit.getVC()].addFlit(m_inputFlit);
			//console("I["+getPortNum()+":"+m_inputFlit.getVC()+"] received flit "+m_inputFlit.toShortStr());
			m_inputFlit = null;
		}
	}
	
	/*
	 * Pick the next active VC round-robin and return it. If the channel is waiting 
	 * for a VC to be allocated, then take the flit so the router can try to allocate 
	 * it one
	 */
	public Flit peekNextFlit() {
		for(int j=1; j<m_vcs.length+1; j++) {
			int index = (getCurrVC() + j) % m_vcs.length;
			if((m_vcs[index].isActive() || m_vcs[index].isWaitingVC()) /*&& m_vcs[index].hasFlit()*/) {
				if(m_vcs[index].peekFlit() != null) {
					setCurrVC(index);
					//System.out.println("Input set currVC = "+getCurrVC());
					return m_vcs[index].peekFlit();
				}
			}
		}
		return null;
	}
	
	public Flit takeNextFlit() {
		m_sendCredit = true;
		return m_vcs[getCurrVC()].takeFlit();
	}

	/*
	 * Write a credit for the current VC if there is one
	 */
	public void writeOuputCredit() {
		if(m_sendCredit) {
			getLink().setInputCredit(new Credit(getCurrVC()));
			m_sendCredit = false;
			//console("I["+getPortNum()+":"+getCurrVC()+"] sending a credit back");
			//System.out.println("sending a credit back for VC "+getCurrVC());
		}
	}
	
	/*
	 * Add a credit for VCs pointing to outputPort and outputVC
	 */
	public void incrementCredits(int vcIndex) {
		m_vcs[vcIndex].incCredits();
		//console("IP["+getPortNum()+":"+vcIndex+"] got credit");
	}
	
	/*
	 * When a flit has been routed, set the output port and vc index
	 */
	public void setupConnection(int vcIndex, int outputPort, int outputVC) {
		m_vcs[vcIndex].setupConnection(outputPort, outputVC);
	}
	
	public void reset() {
		for(InputVC vc : m_vcs)
			vc.resetState();
		m_sendCredit = false;
		m_inputFlit = null;
		setCurrVC(0);
	}
	
	public String toString() {
		String s = "";
		for(int i=0; i<m_vcs.length; i++) {
			//s += "I["+getPortNum()+":"+i+"]"+(getCurrVC()==i?"*":"")+"\t"+m_vcs[i]+"\n";
			String port = "I["+getPortNum()+":"+i+"]"+(getCurrVC()==i?"*":"");
			s += String.format("%-12s%s\n", port, m_vcs[i].toString());
		}
		return s;
	}
	
	public int  getUpstreamNode()                       { return getLink().getFromPort().getNodeId(); }
	public int  getOutputPort(int vcIndex)              { return m_vcs[vcIndex].getOutputPort(); }
	public int  getOutputVC(int vcIndex)                { return m_vcs[vcIndex].getOuputVC(); }
	public void setFlitInput(Flit flit)                 { m_inputFlit = flit; }
	public void setWaitingVC(int vcIndex)               { m_vcs[vcIndex].setWaitingVC(); }
	public void incCurrVCCredits()                      { m_vcs[getCurrVC()].incCredits(); }
}

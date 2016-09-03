/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.components;

import sim.Config;

public class OutputPort extends Port {

	private Credit     m_inputCredit;
	private OutputVC[] m_vcs;
	
	public OutputPort(Console console, int nodeId, int portNum, Link link, int numVCs) {
		super(console, nodeId, portNum, link);
		m_vcs = new OutputVC[numVCs];
		for(int i=0; i<m_vcs.length; i++)
			m_vcs[i] = new OutputVC(portNum, i, console);
		m_inputCredit = null;
	}

	/*
	 * Read a credit input and return it
	 */
	public Credit readInputCredit() {
		if(m_inputCredit != null) {
			Credit c = m_inputCredit;
			m_inputCredit = null;
			return c;
		}
		return null;
	}
	
	/*
	 * Add a flit to an output VC
	 */
	public void addFlit(int outputVC, Flit flit) {
		m_vcs[outputVC].addFlit(flit);
		//console("O["+getPortNum()+":"+outputVC+"] added flit "+flit.toShortStr());
	}
	
	/*
	 * Pick the next active VC round-robin and send the queued flit. Also, increment the hop count
	 */
	public void writeOutputFlit() {
		
		for(int j=1; j<m_vcs.length+1; j++) {
			int index = (getCurrVC() + j) % m_vcs.length;
			if(m_vcs[index].isActive() && m_vcs[index].hasFlit()) {
				setCurrVC(index);
				Flit flit = m_vcs[getCurrVC()].takeFlit();
				flit.incHops();
				//System.out.println("Output port wrote flit on VC "+getCurrVC());
				getLink().setInputFlit(flit);
				console("O["+getPortNum()+":"+getCurrVC()+"] wrote flit "+flit+" to link "+getLink().getId());
				return;
			}
		}
		
		/*if(m_vcs[getCurrVC()].isActive() && m_vcs[getCurrVC()].hasFlit()) {
			Flit flit = m_vcs[getCurrVC()].takeFlit();
			flit.incHops();
			getLink().setInputFlit(flit);
			console("O["+getPortNum()+":"+getCurrVC()+"] wrote flit "+flit+" to link "+getLink().getId());
		}*/
	}
	
	/*
	 * Allocate a virtual channel: one that is idle and has full credits, this 
	 * removes the possibility of a dependence between the current and waiting VCs
	 * 
	 * The available number of virtual channels is bouned by a configuration parameter 
	 * for comparision of segment with lashtor
	 */
	public int allocVC() {
		for(int i=0; i<(int)Math.min(Config.availableVCs(), m_vcs.length); i++) {
			if(m_vcs[i].isIdle() && m_vcs[i].hasFullCredits()) {
				//System.out.println("OP "+getPortNum()+" allocated VC "+i);
				return i;
			}
		}
		return -1;
	}
	
	/*
	 * Attempt to allocate a specific virtual channel
	 */
	public int allocVC(int vcIndex) {
		if(m_vcs[vcIndex].isIdle() && m_vcs[vcIndex].hasFullCredits()) {
			//System.out.println("OP "+getPortNum()+" allocated VC "+i);
			return vcIndex;
		}
		return -1;
	}
	
	public void reset() {
		for(OutputVC vc : m_vcs)
			vc.resetState();
		m_inputCredit = null;
		setCurrVC(0);
	}
	
	public void incrementCredits(int vcIndex) {
		m_vcs[vcIndex].incCredits();
		//console("Got credit for OP["+getPortNum()+":"+vcIndex+"]");
	}

	public void setupConnection(int vcIndex, int inputPortNum, int inputVC) {
		m_vcs[vcIndex].setupConnection(inputPortNum, inputVC);
	}
	
	public String toString() {
		String s = "";
		for(int i=0; i<m_vcs.length; i++) {
			//s += "O["+getPortNum()+":"+i+"]"+(getCurrVC()==i?"*":"")+"\t"+m_vcs[i]+"\n";
			String port = "O["+getPortNum()+":"+i+"]"+(getCurrVC()==i?"*":"");
			s += String.format("%-12s%s\n", port, m_vcs[i].toString(getDownStreamNodeId(), getDownStreamNodePort()));
		}
		return s;
	}
	
	public int     getDownStreamNodeId()         { return getLink().getToPort().getNodeId(); } // could neaten this
	public int     getDownStreamNodePort()       { return getLink().getToPort().getPortNum(); } // could neaten this
	public int     getConnectedInputPort(int vc) { return m_vcs[vc].getConnectedInputPort(); }
	public int     getConnectedInputVC(int vc)   { return m_vcs[vc].getConnectedInputVC(); }
	public void    setCreditInput(Credit credit) { m_inputCredit = credit; }
	public boolean hasCredits(int vcIndex)       { return !m_vcs[vcIndex].isWaitingCdt(); }
	public boolean isEmpty(int vcIndex)          { return !m_vcs[vcIndex].hasFlit(); }
}

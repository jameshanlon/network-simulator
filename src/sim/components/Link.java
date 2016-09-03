/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.components;

import java.util.LinkedList;

public class Link implements Component {
	
	private int                 m_id;
	
	// Input objects
	private Flit                m_inputFlit;
	private Credit              m_inputCredit;
	
	// State objects
	private int                 m_delay;
	private OutputPort          m_fromPort;
	private InputPort           m_toPort;
	private LinkedList<Flit>    m_transitFlits;
	private LinkedList<Credit>  m_transitCredits;
	
	public Link(int id, int delay) {
		super();
		m_id             = id;
		m_delay          = delay;
		m_inputFlit      = null;
		m_inputCredit    = null;
		m_transitFlits   = new LinkedList<Flit>();
		m_transitCredits = new LinkedList<Credit>();
	}
	
	/*
	 * If the link is empty take the next queued flit and transfer it
	 */
	public void update() {
		//System.out.println("link "+m_id+" numFlits="+m_transitFlits.size()+", numCredits "+m_transitCredits.size());
		readInputFlits();
		readInputCredits();
		transmitSignals();
	}

	/*
	 * When the flit has finished transfering, copy it into the 
	 * destinations node's pre-buffer
	 */
	public void copy() {
		deliverFlit();
		deliverCredit();
	}
	
	/*
	 * Read an input flit
	 */
	private void readInputFlits() {
		if(m_inputFlit != null) {
			m_inputFlit.zeroTransitCycles();
			m_transitFlits.add(m_inputFlit);
			m_inputFlit = null;
		}
	}
	
	/*
	 * Read an input credit
	 */
	private void readInputCredits() {
		if(m_inputCredit != null) {
			m_inputCredit.zeroTransitCycles();
			m_transitCredits.add(m_inputCredit);
			m_inputCredit = null;
		}
	}
	
	private void transmitSignals() {
		for(Flit f : m_transitFlits)
			f.incTransitCycles();
		for(Credit c : m_transitCredits)
			c.incTransitCycles();
	}
	
	private void deliverFlit() {
		if(!m_transitFlits.isEmpty()) {
			if(m_transitFlits.getFirst().getTransitCycles() == m_delay) {
				Flit flit = m_transitFlits.removeFirst();
				m_toPort.setFlitInput(flit);
				//System.out.println("Link "+m_id+" delivered flit "+flit);
			}
		}
	}
	
	private void deliverCredit() {
		if(!m_transitCredits.isEmpty()) {
			if(m_transitCredits.getFirst().getTransitCycles() == m_delay) {
				m_fromPort.setCreditInput(m_transitCredits.removeFirst());
				//System.out.println("Link "+m_id+" delivered credit");
			}
		}
	}
	
	public void reset() {
		m_transitFlits.clear();
		m_transitCredits.clear();
		m_inputFlit = null;
		m_inputCredit = null;
	}
	
	public String toString() {
		String s = m_id+"\t";
		for(Flit f : m_transitFlits)
			s += f+"("+f.getTransitCycles()+") ";
		s += "\t";
		for(Credit c : m_transitCredits)
			s += c+"("+c.getTransitCycles()+") ";
		return s;
	}
	
	public void       connectFrom(OutputPort fromPort)   { m_fromPort = fromPort; }
	public void       connectTo(InputPort toPort)        { m_toPort = toPort; }
	public String     getTitle()                         { return "Link "+m_id; }
	public int        getId()                            { return m_id; }
	public InputPort  getToPort()                        { return m_toPort; }
	public OutputPort getFromPort()                      { return m_fromPort; }
	public int        getDelay()                         { return m_delay; }
	public void       setInputFlit(Flit flit)            { m_inputFlit = flit; }
	public void       setInputCredit(Credit credit)      { m_inputCredit = credit; }
	public int        getWeight()                        { return m_delay; }
	public boolean    equals(Link link)                  { return getId() == link.getId(); }
}
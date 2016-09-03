/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.components;

import java.util.LinkedList;

public class Buffer {

	private Console          m_console;
	private LinkedList<Flit> m_buffer;
	private int              m_capacity;
	
	public Buffer(int capacity, Console console) {
		m_console  = console;
		m_capacity = capacity;
		m_buffer   = new LinkedList<Flit>();
	}

	public boolean offerFlit(Flit flit) {
		if(m_buffer.size() < m_capacity) {
			m_buffer.addLast(flit);
			return true;
		}
		System.err.println("Buffer could not accept flit");
		console("Buffer could not accept flit");
		return false;
	}

	public String toString() {
		String s = "";
		for(Flit f : m_buffer)
			s += f+" ";
		return s;
	}
	
	public boolean hasFlits()                 { return !m_buffer.isEmpty(); }
	public boolean isFull()                   { return m_buffer.size() >= m_capacity; }
	public Flit    peekFlit()                 { return m_buffer.peek(); }
	public Flit    takeFlit()                 { return m_buffer.poll(); }
	public int     freeSlots()                { return m_capacity - m_buffer.size(); }
	public int     usedSlots()                { return m_buffer.size(); }
	public void    clear()                    { m_buffer.clear(); }
	public void    replaceHeadFlit(Flit flit) { m_buffer.add(0, flit); }
	public void    console(String text)       { m_console.console(text); }
}

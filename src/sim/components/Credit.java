/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.components;

/*
 * A class to represent a credit signal, this is transmitted 
 * back up a link in a credit loop to inform of a new fre buffer space
 */
public class Credit extends Signal {

	private int m_vc;
	
	public Credit(int vc) {
		super();
		m_vc = vc;
	}
	
	public String toString()          { return "C["+m_vc+"]"; }
	public void   setVC(int vc)       { m_vc = vc; }
	public int    getVC()             { return m_vc; }
}

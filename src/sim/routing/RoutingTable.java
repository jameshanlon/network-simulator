/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.routing;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RoutingTable {
	
	private Map<Integer, TableEntry> m_lookup;
	
	class TableEntry {
		int port;
		int vc;
		
		public TableEntry(int port, int vc) {
			this.port = port;
			this.vc = vc;
		}
		
		public String toString() {
			return "["+port+":"+vc+"]\n";
		}
	}
	
	public RoutingTable() {
		m_lookup = new HashMap<Integer, TableEntry>();
	}
	
	public int getPort(int dest) {
		TableEntry e = (TableEntry) m_lookup.get(dest);
		if(e == null) {
			System.err.println("(dest) Could not find routing table entry for dest "+dest);
			return -1;
		}
		return e.port;
	}
	
	public int getVC(int dest) {
		TableEntry e = (TableEntry) m_lookup.get(dest);
		if(e == null) {
			System.err.println("(vc) Could not find routing table entry for dest "+dest);
			return -1;
		}
		return e.vc;
	}
	
	public void add(int dest, int port, int vc) {
		if(!m_lookup.containsKey(dest)) {
			m_lookup.put(dest, new TableEntry(port, vc));
		} else {
			TableEntry e = m_lookup.get(dest);
			
			if(e.port != port)
				System.err.println("Error: port mistmatch: "+e.port+" "+port);
			
			if(vc != e.vc)
				System.err.println("Error: table already contains target, e.vc="
						+e.vc+" new vc="+vc);
			e.vc = vc;
			e.port = port;
		}
	}
	
	public String toString() {
		String s = "Dest\tPort\n\n";
		Set<Integer> keys = m_lookup.keySet();
		for(Integer dest : keys) {
			s += dest+"\t"+m_lookup.get(dest);
		}
		return s;
	}
}

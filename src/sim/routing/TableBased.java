/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.routing;

/*
 * Base class for a table-based routing function, provides a simple output port 
 * and vc lookup for each destination node. 
 */
public class TableBased implements RoutingFunction {

	private RoutingTable m_table;
	
	public TableBased() {
		m_table = new RoutingTable();
	}
	
	public int getOutputPort(int current, int inputVC, int source, int dest) {
		//System.out.println("Current node: "+current+" destination "+dest);
		return m_table.getPort(dest);
	}
	
	public int getOutputVC(int current, int inputVC, int source, int dest) {
		return m_table.getVC(dest);
	}
	
	public void addTableEntry(int dest, int port, int vc) {
		m_table.add(dest, port, vc);
		//System.out.println("added table entry for dest "+dest+" on ["+port+":"+vc+"]");
		
		//if(vc > Config.numLayers()-1) 
		//	System.out.println("VC EXEEDS LAYERS!");
	}
	
	public String toString() { return m_table+""; }	
}
/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.traffic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Trace {

	public ArrayList<TraceElement> m_trace;
	
	public Trace(String filename) {
		m_trace = new ArrayList<TraceElement>();
		readTrace(filename);
	}

	private void readTrace(String filename) {

		try {
	        BufferedReader in = new BufferedReader(new FileReader(filename));
	        String str;
	        while ((str = in.readLine()) != null) {
	            if(!(str.trim().charAt(0) == '#')) {
	            	String[] frags = str.split(",");
	            	long clock = Long.parseLong(frags[0]);
	            	int source = Integer.parseInt(frags[1]);
	            	int dest = Integer.parseInt(frags[2]);
	            	int burst = Integer.parseInt(frags[3]);
	            	m_trace.add(new TraceElement(clock, source, dest, burst));
	            }
	        }
	        in.close();
	        System.out.println("Read "+m_trace.size()+" events from traffic trace "+filename);
	    } catch (IOException e) {
	    	System.err.println("Error: reading traffic trace "+filename);
	    }
	}
	
}

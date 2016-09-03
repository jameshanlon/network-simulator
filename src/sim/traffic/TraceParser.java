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
import java.util.LinkedList;

import sim.Simulator;

public class TraceParser {
	
	private TraceParser(String filename) {}

	@SuppressWarnings("unchecked")
	public static LinkedList<TraceEvent>[] read(String filename) {

		LinkedList<TraceEvent>[] traces = new LinkedList[Simulator.numNodes()];

		for(int i=0; i<traces.length; i++)
			traces[i] = new LinkedList<TraceEvent>();
		
		try {
	        BufferedReader in = new BufferedReader(new FileReader(filename));
	        String str;
	        int numEvents = 0;
	        while ((str = in.readLine()) != null) {
	            if(!(str.trim().charAt(0) == '#')) {
	            	numEvents++;
	            	String[] frags = str.split(",");
	            	long clock = Long.parseLong(frags[0]);
	            	int source = Integer.parseInt(frags[1]);
	            	int dest = Integer.parseInt(frags[2]);
	            	int burst = Integer.parseInt(frags[3]);
	            	traces[source].add(new TraceEvent(clock, dest, burst));
	            }
	        }
	        in.close();
	        System.out.println("Read "+numEvents+" events from traffic trace "+filename);
	    } catch (IOException e) {
	    	System.err.println("Error: reading traffic trace "+filename);
	    }
	    
	    return traces;
	}
}

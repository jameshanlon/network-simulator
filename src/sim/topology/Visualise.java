/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.topology;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import sim.Utilities;
import sim.components.Network;
import sim.components.Router;
import sim.components.RouterLink;

public class Visualise {

	private static final String DOT_FILENAME = "network.dot";
	
	private Visualise() {}
	
	public static String createDotFileString(Network network) {
		String s = "";
		s += "digraph NetworkTopology {\n";
		s += "overlap=false; splines=true; size=100; fontsize=8; sep=1.5;\n";
        //s += "\tgraph [fontsize=11]\n";
        //s += "\tnode [shape=circle, height=.1, width=.1, style=filled, fontsize=11];\n";
        //s += "\tedge [weight=1.0];\n";
        
        for(Router r : network.getRouters()) {
        	Set<RouterLink> outgoingLinks = r.outgoingLinks();
        	for(RouterLink l : outgoingLinks)
        		s += "    "+r.getNodeId()+" -> "+l.getTo().getNodeId()+" [label = "+l.getId()+"];\n";
        }
        
        s += "}\n";
        return s;
	}
	
	public static void writeDot(Network network, String filename) {
		try {
	        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
	        out.write(createDotFileString(network));
	        out.close();
	        //System.out.println("Wrote dot file "+DOT_FILENAME);
	    } catch (IOException e) {
	    	System.err.println("Error: writing dot file "+DOT_FILENAME);
	    	e.printStackTrace();
	    }
	}
	
	public static void createDotImage(String imageFilename) {
		createDotImage(DOT_FILENAME, imageFilename);
	}
	
	public static void createDotImage(String dotFilename, String imageFilename) {
		Utilities.exec("fdp -Tps "+dotFilename+" -o "+imageFilename);
		Utilities.exec("rm "+dotFilename);
	    System.out.println("Wrote image file "+imageFilename);
	}
}

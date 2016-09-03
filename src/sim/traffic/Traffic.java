/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.traffic;

import java.util.Random;

import sim.Config;
import sim.Simulator;
import sim.Utilities;

public class Traffic {

	private static int[] m_perm;
	
	public static int getDest(int source, int totalNodes) {
		
		/*switch(source) {
		case 0: return 3;
		case 3: return 0;
		case 2: return 1;
		case 1: return 2;
		default:
			System.err.println("no source!");
			return -1;
		}*/
		
		switch(Config.traffic()) {
		case UNIFORM:    return Traffic.uniform(source);
		case BITCOMP:    return Traffic.bitcomp(source);
		case BITREV:     return Traffic.bitrev(source);
		case TRANSPOSE:  return Traffic.transpose(source);
		case SHUFFLE:    return Traffic.shuffle(source);
		case TORNADO:    return Traffic.tornado(source);
		case NEIGHBOUR:  return Traffic.neighbour(source);
		case RANDPERM:   return Traffic.randperm(source);
		case DIAGONAL:   return Traffic.diagonal(source);
		case ASYMMETRIC: return Traffic.asymmetric(source);
		default:         return -1;
		}
	}
	
	public static void srcDestBin(int source, int dest, int lg) {
		System.out.print("from: ");
		int t = source;
		for(int b = 0; b < lg; b++)
			System.out.print(((t >> (lg - b - 1)) & 0x1));
	  
		System.out.println(" to: ");
		t = dest;
		for(int b = 0; b < lg; b++)
			System.out.print(((t >> (lg - b - 1) ) & 0x1));
		System.out.println();
	}

	private static int uniform( int source) {
		// NOTE: dont want a node to send its self something
		return Simulator.getRandInt(Simulator.numNodes() - 1);
	}

	private static int bitcomp(int source) {
		int lg   = (int) Utilities.log2(Simulator.numNodes());
		int mask = Simulator.numNodes() - 1;

		if(( 1 << lg) != Simulator.numNodes()) {
			System.err.println("Error: The 'bitcomp' traffic pattern requires # nodes to be a power of two!");
			return -1;
		}

		return (~source) & mask;
	}

	private static int transpose(int source) {
		int lg      = (int) Utilities.log2(Simulator.numNodes());
		int mask_lo = (1 << (lg/2)) - 1;
		int mask_hi = mask_lo << (lg/2);

		if(((1 << lg) != Simulator.numNodes()) || ((lg & 0x1) > 0)) {
			System.err.println("Error: The 'transpose' traffic pattern requires # nodes to be an even power of two!");
			return -1;
		}

		return ((source >> (lg/2)) & mask_lo) | ((source << (lg/2)) & mask_hi);
	}

	private static int bitrev(int source) {
		int lg = (int) Utilities.log2(Simulator.numNodes());

		if ((1 << lg) != Simulator.numNodes()) {
		  	System.err.println("Error: The 'bitrev' traffic pattern requires # nodes to be a power of two!");
		  	return -1;
		}

		// If you were fancy you could do this in O(log log total_nodes) instructions, but I'm not
		int dest = 0;
		for (int b = 0; b < lg; b++)
			dest |= ((source >> b) & 0x1) << (lg - b - 1);
		return dest;
	}

	private static int shuffle(int source) {
		int lg = (int) Utilities.log2(Simulator.numNodes());

		if ( ( 1 << lg ) != Simulator.numNodes()) {
			System.err.println("Error: The 'shuffle' traffic pattern requires # nodes to be a power of two!");
			return -1;
		}

		return ((source << 1) & (Simulator.numNodes() - 1)) | ((source >> (lg - 1)) & 0x1);
	}

	private static int tornado(int source) {
		int offset = 1;
		int dest = 0;
	  	int k = Config.k();

	  	for(int n = 0; n < Config.n(); n++) {
	  		dest += offset * (((source / offset) % k + (k/2 - 1)) % k);
	  		offset *= 2;
	  	}

	  	return dest;
	}

	private static int neighbour(int source) {
		int offset = 1;
		int dest = 0;
		int k = Config.k();
	  
		for ( int n = 0; n < Config.n(); ++n ) {
			dest += offset * (((source / offset) % k + 1) % k);
			offset *= k;
		}

		return dest;
	}

	private static void GenerateRandomPerm() {
		Random rand = new Random(Config.permSeed());

		if(m_perm == null)
			m_perm = new int [Simulator.numNodes()];

		for (int i = 0; i < m_perm.length; i++)
			m_perm[i] = -1;

		for(int i = 0; i < m_perm.length; i++) {
			
			int index = rand.nextInt(Simulator.numNodes() - i);
			int j = 0;
			int count = 0;
			
			while((count < index) || ( m_perm[j] != -1)) {
				if (m_perm[j] == -1)
					count++;
				j++;

				if (j >= Simulator.numNodes()) {
		    		System.err.println("ERROR: GenerateRandomPerm( ) internal error");
		    		return;
		    	}
			}
			m_perm[j] = i;
		}
	}

	private static int randperm( int source) {
		if(m_perm == null)
			GenerateRandomPerm();
		return m_perm[source];
	}

	/*
	 * 2/3 of traffic goes from source->source
	 * 1/3 of traffic goes from source->(source+1)%total_nodes
	 */
	private static int diagonal(int source) {
		return Simulator.getRandInt(2)==0 ? ( source + 1 ) % Simulator.numNodes() : source;
	}

	private static int asymmetric(int source) {
		int half = Simulator.numNodes() / 2;
		// This doesn't make sense...
		int d = ( source % half ) + Simulator.getRandInt(1) * half;
		return d;
	}
	
	public static void reset() {
		m_perm = null;
	}
}

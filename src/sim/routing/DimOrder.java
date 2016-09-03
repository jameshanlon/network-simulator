/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.routing;

import sim.Config;
import sim.Simulator;
import sim.components.Network;
import sim.components.Router;
import sim.topology.Mesh;

public class DimOrder implements RoutingFunction {

	private boolean m_mesh;
	private int     _k;
	private int     _n;
	
	public DimOrder(boolean mesh) {
		m_mesh = mesh;
		_k = Config.k();
		_n = Config.n();
	}

	public int getOutputPort(int current, int inputVC, int source, int dest) {
		int D_i = 0;
		int dim = -1;
		
		// Find the first mismatching dimension
		for(dim=0; dim<_n; dim++) {
			
			int destDimPos = getPosInDim(dest, dim);
			int currDimPos = getPosInDim(current, dim);
			
			if(destDimPos != currDimPos) {
				
				if(m_mesh) {
					D_i = destDimPos > currDimPos ? 1 : -1;
					return D_i < 0 ? Mesh.getLeftPort(current, dim) : Mesh.getRightPort(current, dim);
				} else {
					int m_i = (destDimPos - currDimPos) % _k;
					int d_i = m_i - (m_i <= _k/2 ? 0 : _k);
					D_i = d_i == _k/2 ? 0 : (d_i > 0 ? 1 : -1);
					
					// If equal distances, then just randomly choose a direction
					if(D_i == 0)
						D_i = Simulator.randDouble() > 0.5 ? 1 : -1;
					
					// If -1 then route left, otherwise route right
					return D_i < 0 ? 2*dim : 2*dim+1;
				}
			}
		}
		
		//System.out.println("H at "+current+", dest "+dest+" D_Ti="+D_Ti+" (port "+(D_Ti < 0 ? 2*dim : 2*dim+1)+")");
		return -1;
	}
	
	public int getOutputVC(int current, int inputVC, int source, int dest) {
		return -1;
	}
	
	private int getPosInDim(int node, int dim) {
		int kToDim = (int) Math.pow(_k, dim);
		int posInDim = (node / kToDim) % _k;
		return posInDim;
	}
	
	public static void configNetwork(Network network, boolean mesh) {
		for(Router r : network.getRouters())
			r.setRoutingFn(new DimOrder(mesh));		
	}

	public void clear() {}
}

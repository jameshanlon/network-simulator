/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.components;

public class ProcessorLink extends Link {

	private static int m_idCount = 0;
	
	public ProcessorLink(int delay) {
		super(m_idCount++, delay);
	}

}

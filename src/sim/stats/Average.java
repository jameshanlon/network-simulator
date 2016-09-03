/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.stats;

public class Average {

	int    m_numSamples;
	double m_x;
	double m_sum;
	double m_min;
	double m_max;
	double m_error;
	
	public Average() {
		clear();
	}
	
	public Average(double x) {
		clear();
		m_x = x;
	}
	
	/*
	 * Copy constructor
	 *
	public Average(Average a) {
		m_numSamples = a.numSamples();
		m_x = a.x();
		m_sum = a.sum();
		m_min = a.min();
		m_max = a.max();
	}*/

	/*
	 * Create an average of a set of averages
	 *
	public Average(Average[] a) {
		clear();
		for(int i=0; i<a.length; i++) {
			m_sum += a[i].average();
			m_numSamples++;
			if(a[i].max() > m_max)
				m_max = a[i].max();
			if(a[i].min() < m_min)
				m_min = a[i].min();
		}
	}*/
	
	public void addSample(double value) {
		m_numSamples++;
		m_sum += value;
		if(value > m_max)
			m_max = value;
		if(value < m_min)
			m_min = value;
	}
	
	public void clear() {
		m_numSamples = 0;
		m_sum = 0.0;
		m_min = Double.MIN_VALUE;
		m_max = Double.MAX_VALUE;
		m_error = 0;
	}
	
	public String toString() {
		String s = "";
		return s;
	}
	
	public int    numSamples()       { return m_numSamples; }
	public void   setX(double x)     { m_x = x; }
	public double x()                { return m_x; }
	public double average()          { return m_numSamples==0.0 ? 0.0 : m_sum / (double) m_numSamples; }
	public double sum()              { return m_sum; }
	public double min()              { return m_min; }
	public double max()              { return m_max; }
	public void   setError(double e) { m_error = e; }
	public double error()            { return m_error; }
}

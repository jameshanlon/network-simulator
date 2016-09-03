/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Utilities {

    public static int[][] convertToArray(List<Integer>[] list) {
		int[][] array = new int[list.length][];
		for(int i=0; i<list.length; i++) {
			array[i] = new int[list[i].size()];
			for(int j=0; j<list[i].size(); j++) {
				array[i][j] = list[i].get(j).intValue();
			}
		}
		return array;
	}
	
	public static double log2(double num) {
		return (Math.log(num)/Math.log(2));
	} 
	
	/*int log2( int x ) {
	  int r = 0;
	  x >>= 1;
	  while(x>0) {
	    r++; 
	    x >>= 1;
	  }
	  return r;
	}*/
	
	public static boolean mkdir(String directory) {
		if(!(new File(directory)).exists())
			return (new File(directory)).mkdir();
		return true;
	}
	
	public static void exec(String cmd) {
		try {
			Process child = Runtime.getRuntime().exec(cmd);
			child.waitFor();
			
			InputStream in = child.getInputStream();
			int c;
			while ((c = in.read()) != -1) {
				System.out.print(c);
			}

			in.close();
		} catch (IOException e) {
			System.err.println("Error: could execute command: "+cmd);
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.err.println("Error: command interrupted: "+cmd);
			e.printStackTrace();
		}
	}
	
	public static void writeFile(String filename, String contents) {
	   try {
	        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
	        out.write(contents);
	        out.close();
	    } catch (IOException e) {
	    	System.err.println("Error: writing file "+filename);
	    }
	}
}

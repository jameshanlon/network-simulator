/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import sim.Simulator.SimState;
import sim.components.Node;

public class DebugMode {
	
	private static final int   HEIGHT = 800;
	private static final int   WIDTH  = 900;
	private static final int   CYCLE_PAUSE = 10;
	private static JLabel      m_cycles;
	private static JToolBar    m_toolbar;
	private static JButton     m_start;
	private static JTabbedPane m_tabs;
	private static RunThread   m_runThread;
	private static boolean     m_threadRunning;
	private static float       m_cycleTime;
	
	static class RunThread extends Thread {
		
		public RunThread() {
			m_cycleTime = 0;
			m_threadRunning = false;
		}
		
		public void run() {
			Simulator.setState(SimState.RUNNING);
			runSimulation();
			Simulator.setState(SimState.DONE);
		}
	}
	
	public static void init() {
		Simulator.init();
		Simulator.getNetwork().initRouting();
	}
	
	public static void run() {
		SwingUtilities.invokeLater(new Runnable() { 
		    public void run() {	initGUI(); }
		});
		reset();
	}
	
	/*
	 * Run the simulation in debug mode
	 */
	private static void runSimulation() {
		long start=0;
		long end=0;
		
		while(Simulator.clock() < Config.maxCycles() && m_threadRunning) {
			if(Simulator.clock() % 100 == 0) {
				start = System.currentTimeMillis();
			}
			
			step();
			
			if(Simulator.clock() % 100 == 0) {
				end = System.currentTimeMillis();
				m_cycleTime = (end - start) / 1000F;
			}
			
			try {
				Thread.sleep(CYCLE_PAUSE);
			} catch (InterruptedException e) {}
		}
	}
	
	private static void setLAF() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException e) {}
        catch (ClassNotFoundException e) {}
        catch (InstantiationException e) {}
        catch (IllegalAccessException e) {}
    }
	
	private static void initGUI() {
		
		setLAF();
		
	    // Create buttons
	    m_start = new JButton("Start");
	    JButton reset = new JButton("Reset");
	    JButton step = new JButton("Step");
	    m_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startStop();
		}});
	    reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
		}});
	    step.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				step();
		}});
	    
	    // Create a toolbar
	    m_toolbar = new JToolBar();
	    m_toolbar.setFloatable(false);
	    m_toolbar.add(m_start);
	    m_toolbar.add(step);
	    m_toolbar.add(reset);
	    
		// Create a new JFrame and add content
		JFrame frame = new JFrame("Network Simulator");
	    m_tabs = new JTabbedPane();
	    resetPanels();
	    JPanel main = new JPanel(new BorderLayout());
	    m_cycles = new JLabel("0 cycles");
	    main.add(m_toolbar, BorderLayout.NORTH);
	    main.add(m_tabs, BorderLayout.CENTER);
	    main.add(m_cycles, BorderLayout.SOUTH);
	    frame.setContentPane(main);
	    
	    // Show the frame
	    frame.setSize(WIDTH, HEIGHT);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setLocationRelativeTo(null);
	    frame.setVisible(true);
	}
	
	private static void resetPanels() {
		if(m_tabs.getTabCount() == 2)
			m_tabs.removeAll();
		JPanel[] nodePanels = createNodePanels();
		for(int i=0; i<nodePanels.length; i++)
			m_tabs.addTab("Node "+i, nodePanels[i]);
	}
	
	public static JPanel[] createNodePanels() {
		Node[] nodes = Simulator.getNetwork().getNodes();
		JPanel[] panels = new JPanel[nodes.length];
		for(int i=0; i<panels.length; i++) {
			panels[i] = new JPanel(new GridLayout(1,2));
			panels[i].add(nodes[i].getRouterConsole());
			panels[i].add(nodes[i].getProcConsole());
		}
		return panels;
	}
	
	private static void startStop() {
		if(Simulator.running()) {
			stop();
			m_start.setText("Start");
		} else {		
			start();
			m_start.setText("Stop");
		}
	}
	
	public static void start() {
		if(m_runThread.getState().equals(Thread.State.NEW) && !m_threadRunning) {
			m_threadRunning = true;
			m_runThread.start();
		}
	}
	
	public static void stop() {
		if(m_threadRunning)
			m_threadRunning = false;
	}
	
	public static void reset() {
		if(m_threadRunning)
			stop();
		Simulator.reset();
		setCycleCountLabel();
		m_runThread = new RunThread();
	}
	
	private static void step() {
		Simulator.step();
		setCycleCountLabel();
	}
	
	private static void setCycleCountLabel() {
		SwingUtilities.invokeLater(new Runnable() { 
		    public void run() {	m_cycles.setText(Simulator.clock()+" cycles, 100 cycle time "+m_cycleTime+"s"); }
		});
	}
}

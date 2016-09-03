/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import sim.Config;
import sim.Simulator;

public abstract class Console {

	private JTextArea    m_state;
	private JTextArea    m_console;
	private JPanel       m_panel;
	private boolean      m_scroll;
	
	public Console() {
		if(Config.debugMode()) {
			m_scroll = false;
			
			m_state = new JTextArea();
			m_state.setFont(new Font("lucida sans typewriter", Font.PLAIN, 11));
			m_state.setEditable(false);
			m_state.setText("");
			
			m_console = new JTextArea();
			m_console.setFont(new Font("lucida sans typewriter", Font.PLAIN, 11));
			m_console.setEditable(false);
			m_console.setText("");
			
			m_panel = new JPanel(new BorderLayout());
			m_panel.add(new JLabel(getTitle()+":"), BorderLayout.NORTH);
			
			JPanel panel = new JPanel();
			BoxLayout bl = new BoxLayout(panel, BoxLayout.Y_AXIS);
			panel.setLayout(bl);
	
			JScrollPane scrollState = new JScrollPane(m_state);
			scrollState.setPreferredSize(new Dimension(400, 400));
			panel.add(scrollState);
			
			JScrollPane scrollConsole = new JScrollPane(m_console);
			scrollConsole.setPreferredSize(new Dimension(400, 300));
			panel.add(scrollConsole);
			
			m_panel.add(panel, BorderLayout.CENTER);
		}
	}
	
	public void updateStateConsole() {
		if(Config.debugMode()) {
			m_state.setText(toString());
			if(!m_scroll)
				m_state.select(0, 0);
		}
	}
	
	/*
	 * This caused so much hassle, appending to the console is very expensive!
	 */
	public void console(String text) {
		if(Config.debugMode()) {
			m_console.append("["+Simulator.clock()+"]\t"+text+"\n");
			m_console.setCaretPosition(m_console.getText().trim().length());
		}
	}
	
	public void resetConsoles() { 
		if(Config.debugMode()) {
			m_console.setText("");
		}
	}

	public abstract String getTitle();

	public JPanel getConsole()              { return m_panel; }
}

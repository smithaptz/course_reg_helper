package com.view;

import javax.swing.JTabbedPane;

public class MainTabPane extends JTabbedPane {
	private static final long serialVersionUID = 3965740072270404045L;

	public MainTabPane() {
		initComponents();
	}
	
	private void initComponents() {
		setTabPlacement(JTabbedPane.RIGHT);
        setName("null");
	}
	
}

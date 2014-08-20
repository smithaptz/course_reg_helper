package com.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;

import com.controller.CourseRegControllerInterface;




public class CourseRegPane extends JPanel {
	private static final long serialVersionUID = 6648152419014556708L;

	private CourseRegControllerInterface controller;
	
	private DefaultListModel listModel;
	private JButton jButton1;
	private JButton jButton2;
	private JLabel jLabel1;
	private JList jList1;
	private JScrollPane jScrollPane1;
	private JTextField jTextField1;
	
	
    public CourseRegPane(CourseRegControllerInterface controller) {
    	this.controller = controller;
        initComponents();
    }

    private void initComponents() {

        jLabel1 = new JLabel();
        jTextField1 = new JTextField();
        jButton1 = new JButton();
        jButton2 = new JButton();
        jScrollPane1 = new JScrollPane();
        listModel = new DefaultListModel();
        jList1 = new JList(listModel);
        

        setName("Form"); 
        
        jLabel1.setText("課程代碼"); 
        jLabel1.setName("jLabel1"); 

        jTextField1.setText(""); 
        jTextField1.setName("jTextField1"); 

        jButton1.setText("加入排程"); 
        jButton1.setName("jButton1"); 
        jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        
        jButton2.setText("移除"); 
        jButton2.setName("jButton2");
        jButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jScrollPane1.setName("jScrollPane1"); 

        jList1.setName("jList1"); 
        

        jScrollPane1.setViewportView(jList1);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                    .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, 172, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 202, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );
    }
    
    public DefaultListModel getDefaultListModel() {
    	return listModel;
    }

    private void jButton1ActionPerformed(ActionEvent evt) {
    	controller.addWaitCourse(jTextField1.getText());
    	jTextField1.setText(null);
    }
    
    private void jButton2ActionPerformed(ActionEvent evt) {
    	controller.removeWaitCourse(jTextField1.getText());
    	jTextField1.setText(null);
    }
}

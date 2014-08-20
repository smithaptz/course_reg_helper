package com;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import java.text.DateFormat;
import java.util.Date;

import javax.swing.GroupLayout;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.controller.CourseRegController;
import com.controller.CourseRegControllerInterface;
import com.controller.LoginController;
import com.controller.LoginControllerInterface;
import com.controller.LoginControllerObservable;
import com.stu.StuModel;
import com.view.MainTabPane;


public class Main extends JApplet implements LoginControllerObservable {
	private StuModel stuModel;
	private MainTabPane mainView;
	private LoginControllerInterface loginController;
	private CourseRegControllerInterface courseRegController;

	private JTextArea logText;
	private JScrollPane logPane;
	
	private DateFormat mediumFormat;
	

	public Main() {
        mediumFormat = DateFormat.getDateTimeInstance( 
                DateFormat.MEDIUM, DateFormat.MEDIUM);
	}
	
	public static void main(String[] args) {
		Main main = new Main();
		main.windowsInit();
	}
	
	private void windowsInit() {
		init();
		JFrame frame = new JFrame();
		frame.setSize(1024, 768);
		frame.setVisible(true);
		frame.add(mainView);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		System.out.println("Running on Windows Mode");
		System.out.println("Version : 2013.01.12, (NTUST Ver 1.03.03.1010315)");
	}
	
    @Override
    public void init() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {

                public void run() {
                    initComponents();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void initComponents() {
    	stuModel = new StuModel();
    	mainView = new MainTabPane();
    	loginController = new LoginController(this, stuModel);
    	courseRegController = new CourseRegController(this, stuModel);
    	logText = new JTextArea();
    	logPane = new JScrollPane(logText);
    	
    	logText.setEditable(false);
    	
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainView)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainView))
        );
        
        //mainView.addTab("登入", loginController.getComponent());
        //mainView.addTab("日誌", logPane);
        setLoginView();
        loginController.registerObserver((LoginControllerObservable)this);
        setSize(800, 600);
        redirectSystemStreams();
    }

    public void setLoginView() {
    	mainView.removeAll();
        mainView.addTab("登入", loginController.getComponent());
        mainView.addTab("日誌", logPane);
    }

	@Override
	public void setLoginResult(boolean result) {
		if(result) {
			mainView.removeAll();
			mainView.addTab("課程", courseRegController.getComponent());
			mainView.addTab("日誌", logPane);
		}
	}
	
	private void updateTextArea(final String text) { 
		SwingUtilities.invokeLater(new Runnable() { 
			public void run() {
				if("\r\n".equals(text)) {
					logText.append(text);
					return;
				}
				logText.append(mediumFormat.format(System.currentTimeMillis()) + "\t" + text);
			} 
		}); 
	} 
	
	private void redirectSystemStreams() { 
		OutputStream out = new OutputStream() { 
			@Override
			public void write(int b) throws IOException { 
				updateTextArea(String.valueOf((char) b)); 
			} 
			@Override
			public void write(byte[] b, int off, int len) throws IOException { 
				updateTextArea(new String(b, off, len)); 
			} 
			@Override
			public void write(byte[] b) throws IOException { 
				write(b, 0, b.length); 
			} 
		}; 
		System.setOut(new PrintStream(out, true)); 
		System.setErr(new PrintStream(out, true)); 
	}
}

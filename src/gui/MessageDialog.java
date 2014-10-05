package gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class MessageDialog extends JDialog {
	
	private JTextArea text;
	private JLabel image;
	
	private static final ImageIcon ERROR_ICON;
	private static final ImageIcon INFO_ICON;
	private static final String ABOUT_TEXT;
	
	static {
		ERROR_ICON = new ImageIcon(MessageDialog.class.getResource("resources/error.png"));
		INFO_ICON = new ImageIcon(MessageDialog.class.getResource("resources/info.png"));
		ABOUT_TEXT = readTextFile("resources/about.txt");
	}
	
	public MessageDialog(JFrame main) {
		super(main, "Message", true);
				
		image = new JLabel();
		
		text = new JTextArea();
		text.setOpaque(false);
		text.setEditable(false);
		text.setFocusable(false);
		
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
			
		});
		
		JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		p1.setOpaque(false);
		p1.add(image);
		p1.add(text);
		p1.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		
		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		p2.setOpaque(false);
		p2.add(ok);
		p2.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		
		JPanel p3 = new JPanel(new BorderLayout())  {
			
			public void paintComponent(Graphics g) {
			    if (!isOpaque()) {
			        super.paintComponent(g);
			        return;
			    }
			    
			    Graphics2D g2d = (Graphics2D) g;
			    int w = getWidth();
			    int h = getHeight();
			    GradientPaint gp = new GradientPaint(0, 0, Color.WHITE, 0, h, StegoP2PChat.COLOR1);
			    g2d.setPaint(gp);
			    g2d.fillRect(0, 0, w, h);

			    setOpaque(false);
			    super.paintComponent(g);
			    setOpaque(true);
			}
			
		};
        p3.add(p1);
		p3.add(p2, BorderLayout.SOUTH);
		
		setResizable(false);
		add(p3);
	}
	
	public void showInfo(String message) {
		showMessage(message + "!", INFO_ICON);		
	}
	
	public void showError(String message) {
		showMessage(message + "!", ERROR_ICON);		
	}
	
	public void showAbout() {
		showMessage(ABOUT_TEXT, StartFrame.LOGO_ICON);		
	}
	
	private void showMessage(String message, ImageIcon icon) {
		image.setIcon(icon);
		text.setText(message);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);		
	}
	
	public static String readTextFile(String path) {
		String text = "";
		Scanner sc = null;
		try {
			sc = new Scanner(MessageDialog.class.getResourceAsStream(path));
			while (sc.hasNext()){
				text += sc.nextLine();
				if (sc.hasNext()){
					text += "\n";
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (sc != null){
				sc.close();
			}	
		}
		return text;
	}
		
}


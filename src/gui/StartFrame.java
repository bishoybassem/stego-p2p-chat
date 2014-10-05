package gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;

import engine.ChatManager;

@SuppressWarnings("serial")
public class StartFrame extends JFrame {

	public static final ImageIcon LOGO_ICON_S;
	public static final ImageIcon LOGO_ICON;
	
	static {
		LOGO_ICON_S = new ImageIcon(StegoP2PChat.class.getResource("resources/bubble1.png"));
		LOGO_ICON = new ImageIcon(StegoP2PChat.class.getResource("resources/bubble2.png"));
	}
	
	public StartFrame() {
		super("Stego-P2P Chat");
		
		try {
			 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			
		}
		UIManager.put("Label.font", new Font("Arial", Font.PLAIN, 17));
		UIManager.put("Button.font", new Font("Arial", Font.PLAIN, 17));
		UIManager.put("TextField.font", new Font("Arial", Font.PLAIN, 17));
		UIManager.put("TextArea.font", new Font("Arial", Font.PLAIN, 17));
		UIManager.put("List.font", new Font("Arial", Font.PLAIN, 17));
		
		setIconImages(Arrays.asList(LOGO_ICON_S.getImage(), LOGO_ICON.getImage()));
		
		Border b1 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border b2 = BorderFactory.createLineBorder(StegoP2PChat.COLOR1.darker(), 1);
		
		final JTextField text = new JTextField(25);
		text.setBorder(BorderFactory.createCompoundBorder(b2, b1));
		
		final JLabel error = new JLabel();
		error.setForeground(Color.RED);
		
		JButton start = new JButton("Start");
		start.setFocusable(false);
		start.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if (text.getText().length() > 10) {
					error.setText("* Name must be less than 10 characters long!");
					return;
				}
				if (text.getText().matches("(.*)\\s(.*)")) {
					error.setText("* Name must not contain spaces!");
					return;
				}
				if (!text.getText().matches("\\w+")) {
					error.setText("* Name can only contain letters and numbers!");
					return;
				}
				start(text.getText());
			}
			
		});
		
		JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		p1.setOpaque(false);
		p1.add(new JLabel("Enter your name"));
		p1.add(text);
		p1.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		
		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		p2.setOpaque(false);
		p2.add(start);
		
		JPanel p3 = new JPanel(new BorderLayout(0, 0));
		p3.setOpaque(false);
		p3.add(error, BorderLayout.WEST);
		p3.add(p2, BorderLayout.EAST);
		p3.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JPanel p4 = new JPanel(new BorderLayout(0, 0))  {
			
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
        p4.add(p1);
		p4.add(p3, BorderLayout.SOUTH);
		
		setResizable(false);
		add(p4);
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void start(String name) {
		try {
			ChatManager ic = new ChatManager(name);
			StegoP2PChat chat = new StegoP2PChat(ic, name);		
			ic.start();
			setVisible(false);
			chat.setVisible(true);
		} catch (Exception ex) {
			new MessageDialog(this).showError("Could not start the chat session\nCheck that there no other instances running");
		}
	}
	
	public static void main(String[] args) throws Exception {
		new StartFrame().setVisible(true);
	}
		
}


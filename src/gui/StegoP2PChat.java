package gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import engine.ChatManager;
import engine.StegoImage;
import engine.clients.Client;
import engine.clients.Lobby;
import engine.clients.User;
import engine.interfaces.ChatListener;
import engine.interfaces.NetworkListener;

@SuppressWarnings("serial")
public class StegoP2PChat extends JFrame {
	
	private JList<Client> clients;
	private DefaultListModel<Client> listModel;
	private JTextArea chatArea;
	private JTextArea typeArea;
	private JFileChooser imageChooser;
	private MessageDialog messageDialog;
	
	private ChatManager chatManager;
	private BufferedImage img;
	private String format;
	
	public static final Color COLOR1 = new Color(191, 230, 249);
	public static final Color COLOR2 = new Color(245, 251, 254);
	
	public StegoP2PChat(ChatManager manager, String name) throws Exception {
		super("Stego-P2P Chat  [" + name + "]");
		setResizable(false);

		chatManager = manager;
		messageDialog = new MessageDialog(this);
		
		setIconImages(Arrays.asList(StartFrame.LOGO_ICON_S.getImage(), StartFrame.LOGO_ICON.getImage()));
		
		Border b1 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border b2 = BorderFactory.createLineBorder(COLOR1.darker(), 1);
		
		listModel = new DefaultListModel<Client>();
		listModel.addElement(manager.getLobbyChat());
		
		clients = new JList<Client>(listModel);
		clients.setCellRenderer(new DefaultListCellRenderer() {
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c  = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (isSelected) {
					c.setBackground(COLOR1);
					c.setForeground(Color.BLACK);
				}
				return c;
			}
		});
		clients.setSelectedIndex(0);
		clients.setFocusable(false);
		clients.setBackground(COLOR2);
		clients.setLayoutOrientation(JList.VERTICAL);
		clients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		clients.setBorder(b1);
		clients.setPreferredSize(new Dimension(150, 0));
		
		JScrollPane scroll1 = new JScrollPane(clients, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll1.setBorder(b2);
		
		chatArea = new JTextArea(10, 35);
		chatArea.setLineWrap(true);
		chatArea.setForeground(Color.RED);
		chatArea.setBackground(COLOR2);
		chatArea.setBorder(b1);
		chatArea.setEditable(false);
		
		JScrollPane scroll2 = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll2.setBorder(b2);
		
		typeArea = new JTextArea(2, 35);
		typeArea.setBorder(b1);
		
		JScrollPane scroll3 = new JScrollPane(typeArea, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll3.setBorder(b2);
	
		imageChooser = new JFileChooser();
		imageChooser.setAcceptAllFileFilterUsed(false);
		imageChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG files", "png"));
		imageChooser.addChoosableFileFilter(new FileNameExtensionFilter("BMP files", "bmp"));
		imageChooser.addChoosableFileFilter(new FileNameExtensionFilter("GIF files", "gif"));
		
		JButton imageSelect = new JButton("Image");
		imageSelect.setFocusable(false);
		imageSelect.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				selectImage();
			}
			
		});

		JButton send = new JButton("Send");
		send.setFocusable(false);
		send.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				send();
			}
			
		});
		
		JButton about = new JButton("About");
		about.setFocusable(false);
		about.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				messageDialog.showAbout();
			}
			
		});

		JPanel p1 = new JPanel(new BorderLayout(10, 10));
		p1.setOpaque(false);
		p1.add(imageSelect, BorderLayout.EAST);
		p1.add(send, BorderLayout.CENTER);
		
		JPanel p2 = new JPanel(new BorderLayout(10, 10)) {
			
			public void paintComponent(Graphics g) {
				if (!isOpaque()) {
			        super.paintComponent(g);
			        return;
			    }
			    
			    Graphics2D g2d = (Graphics2D) g;
			    int w = getWidth();
			    int h = getHeight();
			    g2d.setPaint(new GradientPaint(0, 0, COLOR1, w / 2, 0, Color.WHITE));
			    g2d.fillRect(0, 0, w / 2, h);
			    
			    g2d.setPaint(new GradientPaint(w / 2, 0, Color.WHITE, w, 0, COLOR1));
			    g2d.fillRect(w / 2, 0, w, h);
			    
			    setOpaque(false);
			    super.paintComponent(g);
			    setOpaque(true);
			}
			
		};
		p2.add(scroll2, BorderLayout.NORTH);
		p2.add(scroll3, BorderLayout.CENTER);
		p2.add(p1, BorderLayout.SOUTH);
		p2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
		
		JPanel p3 = new JPanel(new BorderLayout(10, 10)) {
			
			public void paintComponent(Graphics g) {
				if (!isOpaque()) {
			        super.paintComponent(g);
			        return;
			    }
			    
			    Graphics2D g2d = (Graphics2D) g;
			    int w = getWidth();
			    int h = getHeight();
			    g2d.setPaint(new GradientPaint(0, 0, COLOR1, w / 2, 0, Color.WHITE));
			    g2d.fillRect(0, 0, w / 2, h);
			    
			    g2d.setPaint(new GradientPaint(w / 2, 0, Color.WHITE, w, 0, COLOR1));
			    g2d.fillRect(w / 2, 0, w, h);
			    
			    setOpaque(false);
			    super.paintComponent(g);
			    setOpaque(true);
			}
			
		};
		p3.add(scroll1, BorderLayout.CENTER);
		p3.add(about, BorderLayout.SOUTH);
		p3.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
		
		JPanel p4 = new JPanel(new BorderLayout(0, 0));
		p4.add(p2, BorderLayout.CENTER);
		p4.add(p3, BorderLayout.EAST);
		
		setListeners();
		
		add(p4);
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void setListeners() {
		clients.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					chatArea.setText(chatToString(clients.getSelectedValue().getChat()));
					clients.getSelectedValue().setUnseen(0);
				}
			}
			
        });
		
		chatManager.addChatListener(new ChatListener() {
			
			public void peerChatChanged(User peer) {
				if (peer.equals(clients.getSelectedValue())) {
					chatArea.setText(chatToString(peer.getChat()));
					peer.setUnseen(0);
				}
				clients.repaint();
			}
			
			public void lobbyChatChanged(Lobby lobby) {
				if (lobby == clients.getSelectedValue()) {
					chatArea.setText(chatToString(lobby.getChat()));
					lobby.setUnseen(0);
				}
				clients.repaint();
			}
			
		});
		
		chatManager.getNetwork().addNetworkListener(new NetworkListener() {
			
			public void peerLeft(User peer) {
				if (peer.equals(clients.getSelectedValue()))
					clients.setSelectedIndex(0);
	
				listModel.removeElement(peer);
			}
			
			public void peerJoined(User peer) {
				listModel.addElement(peer);
			}
			
		});

		addWindowListener(new WindowAdapter() {
			
			public void windowClosing(WindowEvent arg0) {
				try {
					chatManager.stop();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		});
	}
	
	private void send() {
		if (img == null) {
			messageDialog.showInfo("Please select an image to be used as a steganographic medium");
			selectImage();
			return;
		}
		if (typeArea.getText().isEmpty())
			return;
		try {
			if (clients.getSelectedValue() instanceof Lobby) {
				chatManager.sendPublicMsg(img, format, typeArea.getText().trim());
			} else {
				chatManager.sendPrivateMsg(img, format, typeArea.getText().trim(), (User) clients.getSelectedValue());
			}
			typeArea.setText("");
		} catch (Exception ex) {
			messageDialog.showError("Could not send the message");
		}
	}
	
	private void selectImage() {
		int returnVal = imageChooser.showDialog(this, "Open");
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	    	try {
				img = ImageIO.read(imageChooser.getSelectedFile());
				format = HidePanel.getImageFormat(imageChooser.getSelectedFile());
				int c = new StegoImage(img, StegoImage.HIDE_MODE).getMaxHideCapacity();
				String msg = "The hide capacity for \"" + imageChooser.getSelectedFile().getName() + "\" is " + c + " bytes";
				messageDialog.showInfo(msg);
			} catch (Exception ex) {
				messageDialog.showError("Could not read the image");
			}
	    }
	}
	
	private static String chatToString(List<String> chat) {
		String s = "";
		for (int i = 0; i < chat.size(); i++) {
			s += chat.get(i) + (i != chat.size() - 1 ? "\n\n" : "");
		}
		return s;
	}
		
}

package engine;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.imageio.ImageIO;

import engine.clients.Lobby;
import engine.clients.User;
import engine.interfaces.ChatListener;

public class ChatManager {

	private static final int PORT = 5556;
	private static final int STEGO_SEED = 9392;
	
	private User user;
	private ServerSocket userSocket;
	private P2PNetwork network;
	private Lobby lobby;
	private ArrayList<ChatListener> chatListeners;
	private Thread thread;
	
	public ChatManager(String userName) throws Exception {
		user = new User(userName, SecureMessage.DSA_PU_KEY, InetAddress.getLocalHost());
		network = new P2PNetwork(user);
		lobby = new Lobby();
		chatListeners = new ArrayList<ChatListener>();
	}

	public void start() throws Exception {
		userSocket = new ServerSocket(PORT);
		network.connect();
        Runnable task = new Runnable() {
        	
            public void run() {
                try {
                    while (!userSocket.isClosed()) {
                        Socket peerSocket = userSocket.accept();
                        DataInputStream in = new DataInputStream(peerSocket.getInputStream());
                        byte[] imageBytes = new byte[in.readInt()];
                        in.readFully(imageBytes);
                        
        				ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
        				receive(ImageIO.read(bais), peerSocket.getInetAddress());
        				
        				bais.close();
        				in.close();
                        peerSocket.close();
                    }
                } catch (Exception e) {
                	
                }
            }
            
        };
        thread = new Thread(task);
        thread.start();
    }
	
	public void stop() throws Exception {
		thread.interrupt();
		network.disconnect();
		userSocket.close();
	}
	
	public synchronized void sendPrivateMsg(BufferedImage img, String imgFormat, String msg, User peer) throws Exception {
		String userMsg = user.getName() + ": " + msg;
		String sentMsg = String.format("%d:%s:%s:%s", 1, peer.getNameAndIp(), user.getNameAndIp(), msg);
		send(img, imgFormat, sentMsg, peer);
		appendToChat(peer.getChat(), userMsg);
		for (int i = 0; i < chatListeners.size(); i++) {
			chatListeners.get(i).peerChatChanged(peer);
		}
	}
	
	public synchronized void sendPublicMsg(BufferedImage img, String imgFormat, String msg) throws Exception {
		String userMsg = user.getName() + ": " + msg;
		for (User peer : getPeers()) {
			String sentMsg = String.format("%d:%s:%s:%s", 0, peer.getNameAndIp(), user.getNameAndIp(), msg);
			send(img, imgFormat, sentMsg, peer);
		}
		
		appendToChat(lobby.getChat(), userMsg);
		for (int i = 0; i < chatListeners.size(); i++) {
			chatListeners.get(i).lobbyChatChanged(lobby);
		}
	}
	
	private synchronized void send(BufferedImage img, String imgFormat, String msg, User peer) throws Exception {
		byte[] msgBytes = SecureMessage.signAndEncrypt(msg.getBytes("UTF-8"));
		StegoImage stegoImage = new StegoImage(img, StegoImage.HIDE_MODE);
		stegoImage.hide(msgBytes, STEGO_SEED);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(stegoImage.getImage(), imgFormat, baos);
		baos.flush();
		byte[] imageBytes = baos.toByteArray();
		
		Socket peerSocket = new Socket(peer.getAddress(), PORT);
		DataOutputStream out = new DataOutputStream(peerSocket.getOutputStream());
		out.writeInt(imageBytes.length);
		out.flush();
		out.write(imageBytes);
		out.flush();
		
		baos.close();
		out.close();
		peerSocket.close();
	}
	
	private synchronized void receive(BufferedImage img, InetAddress senderAddress) throws Exception {
		User sender = null;
		for (User peer : getPeers()) {
			if (peer.getAddress().equals(senderAddress)) {
				sender = peer;
				break;
			}
		}
		StegoImage stegoImage = new StegoImage(img, StegoImage.EXTRACT_MODE);
		String message =  new String(SecureMessage.decryptAndVerify(stegoImage.extract(STEGO_SEED), sender.getPublicKey()), "UTF-8");
		String[] parts = message.split(":");
		if (parts[1].equals(user.getNameAndIp()) && parts[2].equals(sender.getNameAndIp())) {
			if (parts[0].equals("0")) {
				appendToChat(lobby.getChat(), sender.getName() + ": " + parts[3]);
				lobby.setUnseen(lobby.getUnseen() + 1);
				for (int i = 0; i < chatListeners.size(); i++) {
					chatListeners.get(i).lobbyChatChanged(lobby);
				}
			} else if (parts[0].equals("1")) {
				appendToChat(sender.getChat(), sender.getName() + ": " + parts[3]);
				sender.setUnseen(sender.getUnseen() + 1);
				for (int i = 0; i < chatListeners.size(); i++) {
					chatListeners.get(i).peerChatChanged(sender);
				}
			}
		}
	}
	
	private static void appendToChat(List<String> chat, String msg) {
		String timeStamp = new SimpleDateFormat("[hh:mm aa]").format(Calendar.getInstance().getTime());
		chat.add(timeStamp + " " + msg);
	}
	
	public void addChatListener(ChatListener listener) {
		chatListeners.add(listener);
	}
	
	public void removeChatListener(ChatListener listener) {
		chatListeners.remove(listener);
	}
	
	public List<User> getPeers() {
		return network.getPeers();
	}

	public P2PNetwork getNetwork() {
		return network;
	}
	
	public Lobby getLobbyChat() {
		return lobby;
	}
	
}

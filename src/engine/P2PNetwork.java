package engine;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import engine.clients.User;
import engine.interfaces.NetworkListener;

public class P2PNetwork {

	private static final int PORT = 5555;
	private static final String GROUP_IP = "225.4.5.9";
	private static final int BUFFER_SIZE = 1024;
	private static final int INTERVAL = 120;
	private static final int DISCOVER = 0;
	private static final int ADVERTISE = 1;
	private static final int LEAVE = 2;
	
	private User user;
	private List<User> peers;
	private MulticastSocket userSocket;
	private InetAddress groupAddress;
	private Timer timer;
	public ArrayList<NetworkListener> networkListeners;
	private Thread thread;
	
	public P2PNetwork(User user) {
		this.user = user;
		networkListeners = new ArrayList<NetworkListener>();
		peers = new ArrayList<User>();
	}
	
	public void connect() throws Exception {
		groupAddress = InetAddress.getByName(GROUP_IP);
		userSocket = new MulticastSocket(PORT);
		userSocket.joinGroup(InetAddress.getByName(GROUP_IP));
		userSocket.setReceiveBufferSize(BUFFER_SIZE);
		
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			
			public void run() {
				try {
					sendIdentity(DISCOVER);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
		}, 0, INTERVAL * 1000);
		
		Runnable task = new Runnable() {
			
            public void run() {
            	byte[] buffer = new byte[1024];
        		while (!userSocket.isClosed()) {
        			try {
        				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        				userSocket.receive(packet);
        				if (packet.getAddress().equals(user.getAddress()))
        					continue;
        				
        				byte[] identity = SecureMessage.decryptAndVerify(Arrays.copyOf(buffer, packet.getLength()), 1 + User.ADDRESS_LENGTH + User.NAME_MAX_LENGTH);
        				User newUser = new User(identity, packet.getAddress());
        				if (identity[0] == LEAVE) {
        					peers.remove(newUser);
        					for (int i = 0; i < networkListeners.size(); i++) {
        						networkListeners.get(i).peerLeft(newUser);
        					}
        				} else if (!peers.contains(newUser)) {
        					peers.add(newUser);
        					for (int i = 0; i < networkListeners.size(); i++) {
        						networkListeners.get(i).peerJoined(newUser);
        					}
        				}

        				if (identity[0] == DISCOVER) {
        					sendIdentity(ADVERTISE);
        				}
        			} catch (Exception e) {

        			}
        		}
            }
            
        };
        thread = new Thread(task);
        thread.start();
	}
	
	public void disconnect() throws Exception {
		sendIdentity(LEAVE);
		thread.interrupt();
		timer.cancel();
		userSocket.leaveGroup(groupAddress);
		userSocket.close();
	}

	private void sendIdentity(int mode) throws Exception {
		byte[] identity = user.getIdentity();
		identity[0] = (byte) mode;
		byte[] encIdentity = SecureMessage.signAndEncrypt(identity);
		DatagramPacket packet = new DatagramPacket(encIdentity, encIdentity.length, groupAddress, PORT);
		userSocket.send(packet);
	}
	
	public void addNetworkListener(NetworkListener listener) {
		networkListeners.add(listener);
	}
	
	public void removeNetworkListener(NetworkListener listener) {
		networkListeners.remove(listener);
	}
	
	public List<User> getPeers() {
		return peers;
	}
	
}

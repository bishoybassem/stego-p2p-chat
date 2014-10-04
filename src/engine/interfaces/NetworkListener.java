package engine.interfaces;

import engine.clients.User;

public interface NetworkListener {

	void peerJoined(User peer);
	
	void peerLeft(User peer);
	
}

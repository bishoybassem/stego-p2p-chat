package engine.interfaces;
import engine.clients.Lobby;
import engine.clients.User;

public interface ChatListener {

	void lobbyChatChanged(Lobby lobby);
	
	void peerChatChanged(User peer);
	
}

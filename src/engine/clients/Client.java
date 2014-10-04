package engine.clients;

import java.util.ArrayList;

public class Client {

	private ArrayList<String> chat;
	private int unseen;
	
	public Client() {
		chat = new ArrayList<String>();
	}

	public ArrayList<String> getChat() {
		return chat;
	}
	
	public void setUnseen(int unseen) {
		this.unseen = unseen;
	}
	
	public int getUnseen() {
		return unseen;
	}
	
	public String toString() {
		return unseen != 0 ? "(" + unseen + ")" : "";
	}
	
}

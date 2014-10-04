package engine.clients;
import java.net.InetAddress;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class User extends Client {

	public static final int NAME_MAX_LENGTH = 10;
	public static final int ADDRESS_LENGTH = 4;
	
	private String name;
	private InetAddress address;
	private PublicKey publicKey;
	
	public User(String name, PublicKey publicKey, InetAddress address) {
		this.name = name;
		this.publicKey = publicKey;
		this.address = address;
	}
	
	public User(byte[] identity, InetAddress address) throws Exception {
		this.address = address;
		setIdentity(identity);
	}
	
	private void setIdentity(byte[] identity) throws Exception {
		byte[] nameBytes = new byte[NAME_MAX_LENGTH];
		byte[] addressBytes = new byte[ADDRESS_LENGTH];
		byte[] keyBytes = new byte[identity.length - NAME_MAX_LENGTH - ADDRESS_LENGTH - 1];		
		System.arraycopy(identity, 1, addressBytes, 0, ADDRESS_LENGTH);
		System.arraycopy(identity, 1 + ADDRESS_LENGTH, nameBytes, 0, NAME_MAX_LENGTH);
		System.arraycopy(identity, 1 + ADDRESS_LENGTH + NAME_MAX_LENGTH, keyBytes, 0, keyBytes.length);
		int i;
		for (i = 0; i < nameBytes.length; i++) {
			if (nameBytes[i] == -1)
				break;
		}
		name = new String(Arrays.copyOf(nameBytes, i), "UTF-8");
		publicKey = KeyFactory.getInstance("DSA").generatePublic(new X509EncodedKeySpec(keyBytes));
		if (!Arrays.equals(addressBytes, address.getAddress()))
			throw new IllegalArgumentException();
	}

	public byte[] getIdentity() throws Exception {
		byte[] nameBytes = name.getBytes("UTF-8");
		byte[] keyBytes = publicKey.getEncoded();
		byte[] addressBytes = address.getAddress();
		byte[] identity = new byte[1 + ADDRESS_LENGTH + NAME_MAX_LENGTH + keyBytes.length];
		Arrays.fill(identity, (byte) -1);
		System.arraycopy(addressBytes, 0, identity, 1, ADDRESS_LENGTH);
		System.arraycopy(nameBytes, 0, identity, 1 + ADDRESS_LENGTH, nameBytes.length);
		System.arraycopy(keyBytes, 0, identity, 1 + ADDRESS_LENGTH + NAME_MAX_LENGTH, keyBytes.length);
		return identity;
	}
	
	public String getNameAndIp() {
		return address.getHostAddress() + "-" + name;
	}
	
	public boolean equals(Object object) {
		if (object instanceof User) {
			User user = (User) object;
			return name.equals(user.name) && address.equals(user.address);
		}
		return false;
	}
		
	public String getName() {
		return name;
	}
	
	public InetAddress getAddress() {
		return address;
	}
	
	public PublicKey getPublicKey() {
		return publicKey;
	}
	
	public String toString() {
		return name + super.toString();
	}
	
}

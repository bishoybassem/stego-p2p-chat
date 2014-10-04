package engine;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecureMessage {

	private static final byte[] AES_KEY = {-41, 18, -90, -6, -34, 58, 30, -19, -106, -127, 124, -55, 123, -41, -85, -66};
	private static final PrivateKey DSA_PR_KEY;
	public static final PublicKey DSA_PU_KEY;
	
	static {
		KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("DSA");
		} catch (Exception ex) {
			
		}
		keyGen.initialize(512);
	    KeyPair pair = keyGen.generateKeyPair();
	    DSA_PU_KEY = pair.getPublic();
	    DSA_PR_KEY = pair.getPrivate();
	}
	
	public static byte[] signAndEncrypt(byte[] msgBytes) throws Exception {
	    byte[] signature = sign(msgBytes);
	    byte[] msgSignBytes = concatenate(msgBytes, signature);
	    return encrypt(msgSignBytes);
	}
	
	public static byte[] decryptAndVerify(byte[] ivEncMsgSign, PublicKey senderKey) throws Exception {
		byte[] msgSign = decrypt(ivEncMsgSign);
	    byte[][] parts = split(msgSign);
	    verify(parts[0], parts[1], senderKey);
	    return parts[0];
	}
	
	public static byte[] decryptAndVerify(byte[] ivEncMsgSign, int keyStartIndex) throws Exception {
		byte[] msgSign = decrypt(ivEncMsgSign);
	    byte[][] parts = split(msgSign);
		byte[] keyBytes = Arrays.copyOfRange(parts[0], keyStartIndex, parts[0].length);
	    PublicKey senderKey = KeyFactory.getInstance("DSA").generatePublic(new X509EncodedKeySpec(keyBytes));
	    verify(parts[0], parts[1], senderKey);
	    return parts[0];
	}
	
	private static byte[] sign(byte[] msg) throws Exception {
		Signature dsa = Signature.getInstance("SHA1withDSA"); 
		dsa.initSign(DSA_PR_KEY);
		dsa.update(msg);
		return dsa.sign();
	}
	
	private static boolean verify(byte[] msg, byte[] signature, PublicKey senderKey) throws Exception {
		Signature dsa = Signature.getInstance("SHA1withDSA"); 
		dsa.initVerify(senderKey);
		dsa.update(msg);
		return dsa.verify(signature);
	}
	
	private static byte[] concatenate(byte[] msg, byte[] signature) {
		byte[] msgSign = new byte[2 + msg.length + signature.length];
		msgSign[0] = (byte)(msg.length & 0xff);
		msgSign[1] = (byte)((msg.length >> 8) & 0xff);
		System.arraycopy(msg, 0, msgSign, 2, msg.length);
		System.arraycopy(signature, 0, msgSign, 2 + msg.length, signature.length);
		return msgSign;
	}
	
	private static byte[][] split(byte[] msgSign) {
		int msgLength = ((msgSign[1] & 0xFF) << 8) | (msgSign[0] & 0xFF);
		byte[][] parts = new byte[2][];
		parts[0] = Arrays.copyOfRange(msgSign, 2, 2 + msgLength);
		parts[1] = Arrays.copyOfRange(msgSign, 2 + msgLength, msgSign.length);
		return parts;
	}
	
	private static byte[] encrypt(byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        int blockSize = cipher.getBlockSize();
        
        SecretKey key = new SecretKeySpec(AES_KEY, "AES");
        
        byte[] ivData = new byte[blockSize];
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.nextBytes(ivData);
        IvParameterSpec iv = new IvParameterSpec(ivData);
        
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encData = cipher.doFinal(data);

        byte[] ivEncData = new byte[ivData.length + encData.length];
        System.arraycopy(ivData, 0, ivEncData, 0, blockSize);
        System.arraycopy(encData, 0, ivEncData, blockSize, encData.length);
        
        return ivEncData;
    }

	private static byte[] decrypt(byte[] ivEncData) throws Exception {
    	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        int blockSize = cipher.getBlockSize();

        SecretKey key = new SecretKeySpec(AES_KEY, "AES");
        
        byte[] ivData = Arrays.copyOf(ivEncData, blockSize);
        IvParameterSpec iv = new IvParameterSpec(ivData);
        
        byte[] encData = Arrays.copyOfRange(ivEncData, blockSize, ivEncData.length);
  
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(encData);
    }
	    	
}

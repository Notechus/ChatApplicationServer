package com.chatapp.security;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;

import com.chatapp.networking.Packet;

public class CipherSystem
{

	/** Cipher object used to enc/dec */
	private static Cipher cipher;
	/** KeyPair for enc/dec */
	private static KeyPair key;

	/** Type of used cipher */
	private static final String cipher_type = "AES";
	// password for encrypting packets, should be in file or sth (or key)
	/** Password for encryption and decryption */
	private static final String encrypt_passwd = "Somerandompasswd"; // 16

	static
	{
		// RSA
		KeyPairGenerator keyGen;
		try
		{
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048);
			key = keyGen.generateKeyPair();
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		} catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Encrypts packet using cipher specified in <code> cipher_const</code>
	 * 
	 * @param p Packet to be encrypted
	 * @return Encrypted packet as <code>SealedObject</code>
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public static SealedObject encrypt(final Packet p) throws NoSuchAlgorithmException, NoSuchPaddingException
	{
		SecretKeySpec message = new SecretKeySpec(encrypt_passwd.getBytes(), cipher_type);
		Cipher c = Cipher.getInstance(cipher_type);
		SealedObject encrypted_message = null;
		try
		{
			c.init(Cipher.ENCRYPT_MODE, message);
			// cipher.init(Cipher.ENCRYPT_MODE, key.getPublic());
			encrypted_message = new SealedObject(p, c);
			// encrypted_message = new SealedObject(p, cipher);
		} catch (InvalidKeyException e)
		{
			e.printStackTrace();
		} catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return encrypted_message;
	}

	/**
	 * Deciphers packet using cipher specified in <code> cipher_const</code>
	 * 
	 * @param p <code>SealedObject</code> to be deciphered
	 * @return deciphered <code>Packet</code>
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public static Packet decrypt(final SealedObject p) throws NoSuchAlgorithmException, NoSuchPaddingException
	{
		SecretKeySpec message = new SecretKeySpec(encrypt_passwd.getBytes(), cipher_type);
		Cipher c = Cipher.getInstance(cipher_type);
		Packet decrypted_message = null;
		try
		{
			c.init(Cipher.DECRYPT_MODE, message);
			// cipher.init(Cipher.DECRYPT_MODE, key.getPrivate());
			decrypted_message = (Packet) p.getObject(c);
		} catch (InvalidKeyException e)
		{
			e.printStackTrace();
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		} catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
		} catch (BadPaddingException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return decrypted_message;
	}

	protected static byte[] encrypt2(final byte[] p) throws NoSuchAlgorithmException, NoSuchPaddingException
	{
		// SecretKeySpec message = new SecretKeySpec(encrypt_passwd.getBytes(),
		// cipher_const);
		// Cipher c = Cipher.getInstance(cipher_const);
		byte[] encrypted_message = null;
		try
		{
			// c.init(Cipher.ENCRYPT_MODE, message);
			cipher.init(Cipher.ENCRYPT_MODE, key.getPublic());
			// encrypted_message = new SealedObject(p, c);
			encrypted_message = cipher.doFinal(p);
		} catch (InvalidKeyException e)
		{
			e.printStackTrace();
		} catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
		} catch (BadPaddingException e)
		{
			e.printStackTrace();
		}
		return encrypted_message;
	}

	protected static byte[] decrypt2(final byte[] p) throws NoSuchAlgorithmException, NoSuchPaddingException
	{
		// SecretKeySpec message = new SecretKeySpec(encrypt_passwd.getBytes(),
		// cipher_const);
		// Cipher c = Cipher.getInstance(cipher_const);
		byte[] decrypted_message = null;
		try
		{
			// c.init(Cipher.DECRYPT_MODE, message);
			cipher.init(Cipher.DECRYPT_MODE, key.getPrivate());
			decrypted_message = cipher.doFinal(p);
		} catch (InvalidKeyException e)
		{
			e.printStackTrace();
		} catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
		} catch (BadPaddingException e)
		{
			e.printStackTrace();
		}
		return decrypted_message;
	}
}

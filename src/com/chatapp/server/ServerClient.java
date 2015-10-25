package com.chatapp.server;

import java.net.InetAddress;

/**
 * ServerClient is server-side implementation of client. Stores user data
 * 
 * @author notechus
 *
 */
public class ServerClient
{
	/** User name */
	public String name;
	/** Connected user address */
	public InetAddress address;
	/** Connected user port */
	public int port;
	/** User ID given from server, should be final/const */
	private final int ID;
	/** Connection attempts */
	public int attempt = 0;

	/**
	 * Constructs client
	 * 
	 * @param name_ client name
	 * @param address_ client address
	 * @param port_ client port
	 * @param ID_ client ID given from server
	 */
	public ServerClient(String name_, InetAddress address_, int port_, final int ID_)
	{
		this.name = name_;
		this.address = address_;
		this.port = port_;
		this.ID = ID_;
	}

	/**
	 * Getter for client's ID
	 * 
	 * @return client's ID
	 */
	public int getID()
	{
		return ID;
	}
}

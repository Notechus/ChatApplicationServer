package com.chatapp.server;

import java.net.InetAddress;

public class ServerClient
{
	public String name;
	public InetAddress address;
	public int port;
	private final int ID;
	public int attempt = 0;

	public ServerClient(String name_, InetAddress address_, int port_, final int ID_)
	{
		this.name = name_;
		this.address = address_;
		this.port = port_;
		this.ID = ID_;
	}

	public int getID()
	{
		return ID;
	}
}

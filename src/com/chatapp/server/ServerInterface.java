package com.chatapp.server;

public class ServerInterface // runs the server
{
	private int port;
	private Server server;

	public ServerInterface(int port_)
	{
		this.port = port_;
		System.out.println(port_);
		server = new Server(port);
	}

	public static void main(String[] args)
	{
		int port;
		if (args.length != 1)
		{
			System.out.println("Usage: java -jar Server [port]");
			return;
		}
		port = Integer.parseInt(args[0]);
		new ServerInterface(port);
	}
}

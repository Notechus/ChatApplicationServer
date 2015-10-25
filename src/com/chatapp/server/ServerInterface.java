package com.chatapp.server;

/**
 * Sort of UI for server (console)
 * 
 * @author notechus
 *
 */
public class ServerInterface // runs the server
{
	/**	 */
	private int port;
	/**	 */
	@SuppressWarnings("unused")
	private Server server;

	/**
	 * Constructs and runs server
	 * 
	 * @param port_ port for server to run on
	 */
	public ServerInterface(int port_)
	{
		this.port = port_;
		System.out.println(port_);
		server = new Server(port);
	}

	/**
	 * Main function
	 * 
	 * @param args port to run the server
	 */
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

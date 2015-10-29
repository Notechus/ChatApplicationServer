package com.chatapp.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;

import com.chatapp.database.DBConnect;
import com.chatapp.networking.Packet;
import com.chatapp.security.CipherSystem;

/**
 * Chat Server class responsible for handling server side of chat
 * 
 * @author notechus
 */
public class Server implements Runnable
{
	/** List of clients connected to the Server */
	private List<ServerClient> clients = new ArrayList<>();
	/** List of client responses(usable in disconnection handling) */
	private List<Integer> clientResponse = new ArrayList<>();

	/** UDP socket used to send and receive data */
	private DatagramSocket socket;
	/** Socket's running port */
	private int port;
	/** Running flag */
	private boolean running = false;
	/** Server threads: running, database, managing, sending and receiving */
	private Thread run, database, manage, send, receive;
	/** Server's ID should always be 0 */
	private final int ID = 0;
	/** Database connection */
	private DBConnect dbc;

	/**
	 * After <code>MAX_ATTEMPTS</code> lacks of responses user will be
	 * disconnected
	 */
	private final int MAX_ATTEMPTS = 5;
	/** Raw mode flag */
	private boolean raw = false;

	/**
	 * Constructs Server with given parameter and opens Socket
	 * 
	 * @param port_ Port to be run at
	 */
	public Server(int port_)
	{
		this.port = port_;
		try
		{
			socket = new DatagramSocket(port);
		} catch (SocketException ex)
		{
			ex.printStackTrace();
			return;
		}
		dbc = new DBConnect();
		try
		{
			dbc.connect("jdbc:mysql://localhost:3306/ChatDatabase", "notechu", "notechus");
			console("Connected to database");
		} catch (SQLException e)
		{
			e.printStackTrace();
		}

		run = new Thread(this, "Server");
		run.start();
	}

	/**
	 * Runs Server thread
	 * 
	 */
	public void run()
	{
		running = true;
		console("Server started on port " + port);
		manageClients();
		receive();
		Scanner scanner = new Scanner(System.in);
		while (running)
		{
			String com = scanner.nextLine();
			if (!com.startsWith("/"))
			{
				// i think it shouldn't be allowed to write anything but
				// comments
				continue;
			} else
			{
				com = com.substring(1).trim();
				executeCommand(com);
			}
		}
		scanner.close();
	}

	/**
	 * Executes database query
	 * 
	 */
	private void executeQuery()
	{
		database = new Thread("Database")
		{
			public void run()
			{
				System.out.println("db");
			}
		};
		database.start();
	}

	/**
	 * Executes entered command
	 * 
	 * @param com entered command
	 */
	private void executeCommand(String com)
	{

		if (com.equals("raw"))
		{
			// enable raw mode -> print every packet sent/received
			if (raw)
			{
				console("Raw mode off");
			} else
			{
				console("Raw mode on");
			}
			raw = !raw;
		} else if (com.equals("clients"))
		{
			printClients();
		} else if (com.equals("address"))
		{
			System.out.println(socket.getLocalSocketAddress());
		} else if (com.startsWith("kick"))
		{
			// kick [username] or kick [id]
			kick(com.substring(5).trim());
		} else if (com.equals("quit"))
		{
			quit();
		} else if (com.equals("start"))
		{
			// TODO
		} else if (com.equals("help"))
		{
			printHelp();
		} else if (com.equals("history"))
		{
			// TODO
		} else if (com.startsWith("send"))
		{

			sendToAll(new Packet(ID, Packet.Type.MESSAGE, "Server Message: " + com.substring(5).trim()));
		} else
		{
			// in case /blahblah
			console("Unknown command.");
			printHelp();
		}
	}

	/**
	 * Kicks client
	 * 
	 * @param name Client's name
	 */
	private void kick(String name)
	{

		int id = -1;
		boolean number = false;
		try
		{
			id = Integer.parseInt(name);
			number = true;
		} catch (NumberFormatException ex)
		{
			number = false;
		}
		if (number)
		{
			boolean exists = false;
			for (int i = 0; i < clients.size(); i++)
			{
				if (clients.get(i).getID() == id)
				{
					exists = true;
					break;
				}
			}
			if (exists)
			{
				disconnect(id, true);
			} else
			{
				console("Client " + id + " doesn't exist");
			}
		} else // if not number then username
		{
			for (int i = 0; i < clients.size(); i++)
			{
				ServerClient c = clients.get(i);
				if (name.equals(c.name))
				{
					disconnect(c.getID(), true);
					break;
				}
			}
		}
	}

	/**
	 * Prints all connected clients
	 * 
	 */
	private void printClients()
	{
		console("Clients:");
		console("===================================");
		for (int i = 0; i < clients.size(); i++)
		{
			ServerClient c = clients.get(i);
			console(c.name + "(" + c.getID() + ") - " + c.address.toString() + ":" + c.port);
		}
		console("===================================");
	}

	/**
	 * Prints all available commands
	 * 
	 */
	private void printHelp()
	{
		console("Here is a list of available commands:");
		console("=====================================");
		console("/raw - enables raw mode.");
		console("/address - prints address of server.");
		console("/clients - shows all connected clients.");
		console("/kick [users ID or username] - kicks a user.");
		console("/help - shows this help message.");
		console("/send [message] - sends message to all clients.");
		console("/quit - shuts down the server.");
	}

	/**
	 * Manages connected clients
	 */
	private void manageClients()
	{
		manage = new Thread("Manage")
		{
			public void run()
			{
				Packet ping = new Packet(ID, Packet.Type.PING, "server");
				while (running)
				{
					sendToAll(ping);
					try
					{
						Thread.sleep(2000); // sleep to wait for actual
											// response(it might be slow)
					} catch (InterruptedException ex)
					{
						ex.printStackTrace();
					}
					for (int i = 0; i < clients.size(); i++)
					{
						ServerClient c = clients.get(i);
						if (!clientResponse.contains(c.getID()))
						{
							if (c.attempt >= MAX_ATTEMPTS)
							{
								disconnect(c.getID(), false);
							} else
							{
								c.attempt++;
							}
						} else
						{
							clientResponse.remove(new Integer(c.getID()));
							c.attempt = 0;
						}
					}
				}
			}
		};
		manage.start();
	}

	/**
	 * Receives packets from clients and handles them
	 * 
	 */
	private void receive()
	{
		receive = new Thread("Receive")
		{
			public void run()
			{
				while (running)
				{
					// Receiving data
					byte[] data = new byte[65536];
					// byte[] decrypted_packet = null;
					DatagramPacket packet = new DatagramPacket(data, data.length);
					Packet p = null;
					SealedObject d_packet = null;
					try
					{
						socket.receive(packet);
						// decrypted_packet = decrypt2(data);
						ByteArrayInputStream in = new ByteArrayInputStream(data);
						ObjectInputStream is = new ObjectInputStream(in);
						d_packet = (SealedObject) is.readObject();
						p = CipherSystem.decrypt(d_packet);
						// p = (Packet) is.readObject();
					} catch (SocketException ex)
					{

					} catch (IOException ex)
					{
						ex.printStackTrace();
					} catch (ClassNotFoundException ex)
					{
						ex.printStackTrace();
					} catch (NoSuchAlgorithmException e)
					{
						e.printStackTrace();
					} catch (NoSuchPaddingException e)
					{
						e.printStackTrace();
					}
					process(p, packet.getAddress(), packet.getPort());
					if (raw)
						console(p.toString()); // prints messages to syso
				}
			}

		};
		receive.start();
	}

	/**
	 * Sends packet to all clients
	 * 
	 * @param packet <code>Packet</code> to be sent
	 */
	private void sendToAll(Packet packet)
	{
		for (int i = 0; i < clients.size(); i++)
		{
			ServerClient client = clients.get(i);
			send(packet, client.address, client.port);

		}
	}

	/**
	 * Sends packet
	 * 
	 * @param p <code>Packet</code> to be sent
	 * @param address destination address
	 * @param port destination port
	 */
	private void send(Packet p, InetAddress address, int port)
	{
		send = new Thread("Send")
		{
			public void run()
			{
				try
				{
					SealedObject e_packet = CipherSystem.encrypt(p);
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					ObjectOutputStream os = new ObjectOutputStream(outputStream);
					os.writeObject(e_packet);
					byte[] data = outputStream.toByteArray();
					// byte[] encrypted_packet = encrypt2(data);
					DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
					socket.send(packet);
					if (raw)
						console(p.toString());
				} catch (IOException ex)
				{
					ex.printStackTrace();
				} catch (NoSuchAlgorithmException e)
				{
					e.printStackTrace();
				} catch (NoSuchPaddingException e)
				{
					e.printStackTrace();
				}
			}
		};
		send.start();
	}

	/**
	 * Processes all incoming packets
	 * 
	 * @param packet <code>Packet</code> to be processed
	 * @param address destination address, only usable when responding
	 * @param port destination port, as above
	 */
	private void process(Packet packet, InetAddress address, int port)
	{
		Packet.Type type = packet.type;
		if (type == Packet.Type.LOGIN)
		{
			String temp[] = packet.message.split("|");
			// do sth with this
		} else if (type == Packet.Type.CONNECT)
		{
			int id = UniqueIdentifier.getIdentifier();
			clients.add(new ServerClient(packet.message, address, port, id));
			console(packet.message + "(" + id + ") connected.");
			String IDs = "" + id;
			send(new Packet(ID, Packet.Type.CONNECT, IDs), address, port);
			for (int i = 0; i < clients.size(); i++)
			{
				send(new Packet(ID, Packet.Type.USER_ONLINE, clients.get(i).getID() + "." + clients.get(i).name), address, port);
			}
		} else if (type == Packet.Type.MESSAGE)
		{
			packet.message = getName(packet.ID) + ": " + packet.message;
			sendToAll(packet);
		} else if (type == Packet.Type.DISCONNECT)
		{
			disconnect(packet.ID, true);
			sendToAll(new Packet(ID, Packet.Type.USER_OFFLINE, packet.ID + ""));
		} else if (type == Packet.Type.PING)
		{
			clientResponse.add(packet.ID);
		} else if (type == Packet.Type.DIRECT_MESSAGE)
		{
			ServerClient c = null;
			boolean exists = false;
			for (int i = 0; i < clients.size(); ++i)
			{
				if (clients.get(i).getID() == packet.ID)
				{
					c = clients.get(i);
					exists = true;
					break;
				}
			}
			if (exists)
			{
				send(new Packet(packet.ID, packet.type, c.name + ": " + packet.message), c.address, c.port);
			}

		} else
		{
			console(packet.message);
		}
	}

	/**
	 * Disconnects all clients and shutdowns the server
	 */
	private void quit()
	{
		// dc each client
		sendToAll(new Packet(ID, Packet.Type.MESSAGE, "Server has shutdown."));
		for (int i = 0; i < clients.size(); i++)
		{
			ServerClient c = clients.get(i);
			disconnect(c.getID(), true);
		}
		console("Server has shutdown.");
		// close the socket
		running = false; // if you do this you will terminate whole server
		try
		{
			dbc.disconnect();
			manage.join(2000);
			receive.join(2000);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		} catch (NullPointerException e)
		{
			e.printStackTrace();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		socket.close();
	}

	/**
	 * Disconnects client
	 * 
	 * @param id Client's id
	 * @param status disconnection status (time-out or user disconnection)
	 */
	private void disconnect(int id, boolean status)
	{
		ServerClient c = null;
		boolean exists = false;
		for (int i = 0; i < clients.size(); ++i)
		{
			if (clients.get(i).getID() == id)
			{
				c = clients.get(i);
				clients.remove(i);
				exists = true;
				break;
			}
		}
		String message = "";
		if (exists)
		{
			if (status)
			{
				message = "User " + c.name + "(" + c.getID() + ") has disconnected.";
				send(new Packet(ID, Packet.Type.DISCONNECT, ""), c.address, c.port);
			} else
			{
				message = "User " + c.name + "(" + c.getID() + ") has timed out.";
				send(new Packet(ID, Packet.Type.DISCONNECT, ""), c.address, c.port);
			}
		}
		console(message);
	}

	/**
	 * Return name of given client
	 * 
	 * @param id Client's ID
	 * @return Client's name
	 */
	public String getName(int id)
	{
		ServerClient c = null;
		for (int i = 0; i < clients.size(); ++i)
		{
			if (clients.get(i).getID() == id)
			{
				c = clients.get(i);
				break;
			}
		}
		return c.name;
	}

	/**
	 * Closes sockets and application
	 * 
	 */
	public void console(String msg)
	{
		System.out.println(msg);
	}
}

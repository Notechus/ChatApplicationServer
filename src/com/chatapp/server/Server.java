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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;

import com.chatapp.networking.Packet;

public class Server implements Runnable
{
	private List<ServerClient> clients = new ArrayList<>();
	private List<Integer> clientResponse = new ArrayList<>();

	private DatagramSocket socket;
	private int port;
	private boolean running = false;
	private Thread run, manage, send, receive;
	private final int ID = 0; // for packets only i think

	private final int MAX_ATTEMPTS = 5;
	private boolean raw = false;

	// type of currently used cipher
	// private final String cipher_const =
	// "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
	private final String cipher_const = "AES";
	// password for encrypting packets
	private final String encrypt_passwd = "Somerandompasswd"; // 16

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

		run = new Thread(this, "Server");
		run.start();
	}

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
			}
			com = com.substring(1).trim();
			if (com.equals("raw"))
			{
				// enable raw mode -> print every packet sent/received <- TODO
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
				// kick Seba or kick 819212
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
			} else
			{
				// in case /blahblah
				console("Unknown command.");
				printHelp();
			}
		}
		scanner.close();
	}

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

	private void printHelp()
	{
		console("Here is a list of available commands:");
		console("=====================================");
		console("/raw - enables raw mode.");
		console("/address - prints address of server.");
		console("/clients - shows all connected clients.");
		console("/kick [users ID or username] - kicks a user.");
		console("/help - shows this help message.");
		console("/quit - shuts down the server.");
	}

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
					DatagramPacket packet = new DatagramPacket(data, data.length);
					Packet p = null;
					SealedObject d_packet = null;
					try
					{
						socket.receive(packet);
						ByteArrayInputStream in = new ByteArrayInputStream(data);
						ObjectInputStream is = new ObjectInputStream(in);
						d_packet = (SealedObject) is.readObject();
						p = decrypt(d_packet);
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

	private void sendToAll(Packet packet)
	{
		for (int i = 0; i < clients.size(); i++)
		{
			ServerClient client = clients.get(i);
			send(packet, client.address, client.port);

		}
	}

	private void send(Packet p, InetAddress address, int port)
	{
		send = new Thread("Send")
		{
			public void run()
			{

				try
				{
					SealedObject e_packet = encrypt(p);
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					ObjectOutputStream os = new ObjectOutputStream(outputStream);
					os.writeObject(e_packet);
					byte[] data = outputStream.toByteArray();
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

	private void process(Packet packet, InetAddress address, int port)
	{
		Packet.Type type = packet.type;
		if (type == Packet.Type.CONNECT)
		{
			// UUID id = UUID.randomUUID();
			int id = UniqueIdentifier.getIdentifier();
			clients.add(new ServerClient(packet.message, address, port, id));
			console(packet.message + "(" + id + ") connected.");
			String IDs = "" + id;
			console(IDs);
			send(new Packet(ID, Packet.Type.CONNECT, IDs), address, port);
		} else if (type == Packet.Type.MESSAGE)
		{
			packet.message = getName(packet.ID) + ": " + packet.message;
			sendToAll(packet);
		} else if (type == Packet.Type.DISCONNECT)
		{
			disconnect(packet.ID, true);
		} else if (type == Packet.Type.PING)
		{
			clientResponse.add(packet.ID);
		} else
		{
			console(packet.message);
		}
	}

	// TODO only aes works
	protected SealedObject encrypt(final Packet p) throws NoSuchAlgorithmException, NoSuchPaddingException
	{
		SecretKeySpec message = new SecretKeySpec(encrypt_passwd.getBytes(), cipher_const);
		Cipher c = Cipher.getInstance(cipher_const);
		SealedObject encrypted_message = null;
		try
		{
			c.init(Cipher.ENCRYPT_MODE, message);
			encrypted_message = new SealedObject(p, c);
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

	// TODO only aes works
	protected Packet decrypt(final SealedObject p) throws NoSuchAlgorithmException, NoSuchPaddingException
	{
		SecretKeySpec message = new SecretKeySpec(encrypt_passwd.getBytes(), cipher_const);
		Cipher c = Cipher.getInstance(cipher_const);
		Packet decrypted_message = null;
		try
		{
			c.init(Cipher.DECRYPT_MODE, message);
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
			manage.join(2000);
			receive.join(2000);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		socket.close();
	}

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

	public void console(String msg)
	{
		System.out.println(msg);
	}
}

package com.chatapp.database;

import com.chatapp.server.ServerClient;
import com.sun.rowset.JdbcRowSetImpl;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.sql.rowset.JdbcRowSet;

/**
 * Maintains connection with database and executes queries
 * 
 * @author notechus
 */
public class DBConnect
{
	// TODO should identify user here, check id

	/** connects to db */
	private Connection con;
	/** database host */
	private String host;
	/** database username */
	private String uName;
	/** username password */
	private char[] uPass;

	/**
	 * Connects to chosen database using given host, username and password.
	 *
	 * @param host_ host which you will be connected to
	 * @param uName_ your database username
	 * @param uPass_ password for given username
	 * @throws SQLException if a database access error occurs or the url is
	 *             <code>null</code>
	 */
	public void connect(String host_, String uName_, String uPass_) throws SQLException
	{
		this.host = host_;
		this.uName = uName_;
		this.uPass = uPass_.toCharArray(); // we shouldn't really store password
											// here
		this.con = DriverManager.getConnection(host, uName, uPass_);
	}

	/**
	 * Disconnects from chosen database.
	 *
	 * @throws SQLException if a database access error occurs
	 * @throws NullPointerException when there is no connection to disconnect
	 */
	public void disconnect() throws SQLException, NullPointerException
	{
		for (int i = 0; i < uPass.length; i++)
		{
			uPass[i] = 0; // snooping prevention
		}
		con.close();
	}

	/**
	 * Adds client to database
	 * 
	 * @param name client's name
	 * @param l_name client's last name
	 * @param ID client's ID
	 * @throws SQLException
	 */
	public void addClient(String name, String l_name, int ID) throws SQLException
	{
		String order = "INSERT INTO Clients (FirstName,LastName,ID) VALUES(" + name + "," + l_name + "," + ID + ");";
		Statement stmt = con.createStatement();
		try
		{
			stmt.executeQuery(order);
		} finally
		{
			if (stmt != null)
			{
				stmt.close();
			}
		}
	}

	/**
	 * Deletes client from database
	 * 
	 * @param ID client's ID
	 */
	public void deleteClient(int ID) throws SQLException
	{
		String order = "DELETE FROM Clients WHERE ID=" + ID + ");";
		Statement stmt = con.createStatement();
		try
		{
			stmt.executeQuery(order);
		} finally
		{
			if (stmt != null)
			{
				stmt.close();
			}
		}
	}

	/**
	 * Updates client info in database
	 * 
	 */
	public void updateClient()
	{
		// TODO
	}

	/**
	 * Search for specific client in database.
	 *
	 * @param order <code>String</code> query which will be passed to
	 *            <code>Statement</code> and executed there
	 * @return <tt> ArrayList</tt> containing found data
	 * @throws SQLException if a database access error occurs or this method is
	 *             called on a closed connection
	 */
	public ArrayList<String> displayClient(int ID) throws SQLException
	{
		String order = "SELECT * FROM Clients WHERE ID ='" + ID + "';";
		ArrayList<String> array = new ArrayList<>();
		Statement stmt = null;
		try
		{
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(order);

			while (rs.next())
			{
				array.add(rs.getInt(1) + " " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getInt(4));
			}
			return array;
		} finally
		{
			if (stmt != null)
			{
				stmt.close();
			}
		}
	}

	/**
	 * Displays all clients in database.
	 *
	 * @return <code>ArrayList</code> containing all found clients
	 * @throws SQLException if a database access error occurs or this method is
	 *             called on a closed connection
	 */
	public ArrayList<ServerClient> displayAll() throws SQLException
	{
		String SQL_P = "SELECT * FROM Clients";
		ResultSet rs;

		ArrayList<ServerClient> array = new ArrayList<>();
		Statement stmt = con.createStatement();
		try
		{
			rs = stmt.executeQuery(SQL_P);
			while (rs.next())
			{

			}
			return array;
		} finally
		{
			if (stmt != null)
			{
				stmt.close();
			}
		}
	}

	/**
	 * Passes given SQL query to database.
	 *
	 * @param order <code>String</code> query which will be passed to
	 *            <code>Statement</code> and executed there
	 * @throws java.sql.SQLException if a database access error occurs, this
	 *             method is called on a closed Statement, the given SQL
	 *             statement produces a ResultSet object, the method is called
	 *             on a PreparedStatement or CallableStatement
	 */
	public void pass(String order) throws SQLException
	{
		Statement stmt = con.createStatement();
		try
		{
			stmt.executeUpdate(order);
		} finally
		{
			if (stmt != null)
			{
				stmt.close(); // aint giving the solution but good practice
			}
		}
	}

	/**
	 * Insert type of query
	 * 
	 * @param command
	 * @param array
	 * @throws SQLException
	 */
	public void insert(String command, ArrayList<?> array) throws SQLException
	{

		JdbcRowSet jdbcRs = new JdbcRowSetImpl(con);
		jdbcRs.setCommand(command);
		jdbcRs.execute();

		jdbcRs.moveToInsertRow();
		jdbcRs.updateString("FirstName", (String) array.get(0));
		jdbcRs.updateString("LastName", (String) array.get(1));
		jdbcRs.updateString("Email", (String) array.get(2));
		jdbcRs.insertRow();
		jdbcRs.close();
	}

	/**
	 * Returns <code>ResultSet</code> of given SQL query.
	 *
	 * @param order <code>String</code> query which will be passed to
	 *            <code>Statement</code> and executed there
	 * @return <code>ResultSet</code> containing result of <code>order</code>
	 * @throws java.sql.SQLException if a database access error occurs, this
	 *             method is called on a closed <code>Statement</code>, the
	 *             given SQL statement produces anything other than a single
	 *             <code>ResultSet</code> object, the method is called on a
	 *             <code>PreparedStatement</code> or
	 *             <code>CallableStatement</code>
	 */
	public ResultSet getResult(String order) throws SQLException
	{
		Statement stmt = con.createStatement();
		try
		{
			ResultSet rs = stmt.executeQuery(order);
			return rs;
		} finally
		{
			if (stmt != null)
			{
				stmt.close();
			}
		}
	}
}

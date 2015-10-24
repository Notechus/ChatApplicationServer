package com.chatapp.database;

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

	private Connection con;
	private final String host = "jdbc:mysql://localhost:3306/library";
	private String uName = "root";
	private String uPass = "root!@#";

	// check RowSet- jdbcrowset is good for updating and reaching for data
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
		this.con = DriverManager.getConnection(host_, uName_, uPass_);
	}

	/**
	 * Disconnects from chosen database.
	 *
	 * @throws SQLException if a database access error occurs
	 * @throws NullPointerException when there is no connection to disconnect
	 */
	public void disconnect() throws SQLException, NullPointerException
	{
		con.close();
	}

	/**
	 * Search for data in database.
	 *
	 * @param order <code>String</code> query which will be passed to
	 *            <code>Statement</code> and executed there
	 * @param type type of item looked for(@Book, @DVD, @Person, @Firm)
	 * @return <tt> ArrayList</tt> containing found data
	 * @throws SQLException if a database access error occurs or this method is
	 *             called on a closed connection
	 */
	public ArrayList<String> search(String order, String type) throws SQLException
	{
		ArrayList<String> array = new ArrayList<>();
		Statement stmt = null;
		try
		{
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(order);

			while (rs.next())
			{
				if (null != type)
				{
					switch (type)
					{
					case "Person":
						array.add(rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4) + "\n");
					}
				}
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
	 * Search for all contained data in database.
	 *
	 * @param type type of items looked for(@Book, @DVD, @Person, @Firm)
	 * @return <code> ResultSet</code> containing all found data of given @param
	 * @throws SQLException if a database access error occurs or this method is
	 *             called on a closed connection
	 */
	public ArrayList<String> searchAll(String type) throws SQLException
	{
		String SQL_P = "SELECT * FROM Person";
		ResultSet rs;

		ArrayList<String> array = new ArrayList<>();
		Statement stmt = con.createStatement();
		try
		{
			switch (type)
			{
			case "Person":
				rs = stmt.executeQuery(SQL_P);
				while (rs.next())
				{
					array.add(rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4));
				}
				break;
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

	public void insert(String type, String command, ArrayList<?> array) throws SQLException
	{

		JdbcRowSet jdbcRs = new JdbcRowSetImpl(con);
		jdbcRs.setCommand(command);
		jdbcRs.execute();

		switch (type)
		{
		case "Person":
			jdbcRs.moveToInsertRow();
			jdbcRs.updateString("FirstName", (String) array.get(0));
			jdbcRs.updateString("LastName", (String) array.get(1));
			jdbcRs.updateString("Email", (String) array.get(2));
			jdbcRs.insertRow();
			break;
		}
	}

	/**
	 * Returns result of given SQL query.
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

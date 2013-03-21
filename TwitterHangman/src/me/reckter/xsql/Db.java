package me.reckter.xsql;

import me.reckter.misc.Console;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Db {

	/**
	 * 
	 * The Connection object
	 * @var Connection
	 */
	protected Connection conn = null;
	
	/**
	 * 
	 * true if the object is currently able to perform queries
	 * @var bool
	 */
	protected boolean valid;
	
	/**
	 * 
	 * The result of the last query
	 * @var ResultSet
	 */
	protected ResultSet rs = null;
	
	/**
	 * 
	 * The last query
	 * @var Statment
	 */
	protected java.sql.Statement stmt = null;
	
	/**
	 * 
	 * basic constructor. NOTE: A new databse object connects to the database specified in config.xsql.php
	 */
	public Db(String server,String database, String user, String password)
	{
		this.valid = false;

		// Notice, do not import com.mysql.jdbc.*
		// or you will have problems!
		// The newInstance() call is a work around for some
		// broken Java implementations

		try {
            Class.forName("com.mysql.jdbc.Driver");
		    conn = DriverManager.getConnection("jdbc:mysql://" + server + "/" + database, user, password);//"jdbc:mysql://" + server + "/" + database + "?user=" + user + "&password=" + password);
		    // Do something with the Connection
		    // or alternatively, if you don't know ahead of time that
		    // the query will be a SELECT...
		    stmt = conn.createStatement(
		    		 ResultSet.TYPE_SCROLL_INSENSITIVE,
		    		 ResultSet.CONCUR_UPDATABLE);
			this.valid = true;
		}catch (SQLException ex)
		{
		    // handle any errors
			Console.c_log("db","ERROR","SQLException: " + ex.getMessage());
			Console.c_log("db","ERROR","SQLState: " + ex.getSQLState());
			Console.c_log("db","ERROR","VendorError: " + ex.getErrorCode());
		} catch(ClassNotFoundException et)
        {
            Console.c_log("db","ERROR","ClassNotFoundExepction: " + et.toString());
        }
    }
	/**
	 * 
	 * Executes a query
	 * @param string $aq The query
	 * @return bool true if the query was succesfull. Otherwise false. Use getError and getErrno to get more information
	 */
	public boolean query(String aq)
	{
		if(!this.valid)
		{
			return false;
		}
		
		try
		{

		    stmt = conn.createStatement(
		    		 ResultSet.TYPE_SCROLL_INSENSITIVE,
		    		 ResultSet.CONCUR_UPDATABLE);
			if (stmt.execute(aq)) 
			{
                if(stmt == null){
                    Console.c_log("xsql","DEBUG","stmnt == null");
                }
				//rs = stmt.getResultSet();
			}
			this.valid = true;
		}
		catch (SQLException ex) 
		{
			// handle any errors
			Console.c_log("db","ERROR","SQLException: " + ex.getMessage());
			Console.c_log("db","ERROR","SQLState: " + ex.getSQLState());
			Console.c_log("db","ERROR","VendorError: " + ex.getErrorCode());
			this.valid = false;
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * Returns the id of the last inserted row
	 * @return int mysqli->insert_id
	 */
	
	/**
	 * Returns the current result to cache it somewhere.
	 * @return object mysqli_result
	 */
	public ResultSet getResult()
	{
		try {
			return stmt.getResultSet();
		} catch (SQLException ex) {
			// handle any errors
			Console.c_log("db","ERROR","SQLException: " + ex.getMessage());
			Console.c_log("db","ERROR","SQLState: " + ex.getSQLState());
			Console.c_log("db","ERROR","VendorError: " + ex.getErrorCode());
			return null;
		}
	}
	
	
	
	/**
	 * 
	 * Returns the next row of results from the current query
	 * @return bool|array false if something went wrong. Otherwise the row as an associative array
	 */
	
	/**
	 * 
	 * Wrapper for nextRow
	 * @see nextRow
	 */
	
	
	public String getString(String key)
	{
		try {
			return this.rs.getString(key);
		}
		catch (SQLException ex) 
		{
			// handle any errors
			Console.c_log("db","ERROR","SQLException: " + ex.getMessage());
			Console.c_log("db","ERROR","SQLState: " + ex.getSQLState());
			Console.c_log("db","ERROR","VendorError: " + ex.getErrorCode());
			return null;
		}
	}
	
	public int getInt(String key)
	{
		try {
			return this.rs.getInt(key);
		}
		catch (SQLException ex) 
		{
			// handle any errors
			Console.c_log("db","ERROR","SQLException: " + ex.getMessage());
			Console.c_log("db","ERROR","SQLState: " + ex.getSQLState());
			Console.c_log("db","ERROR","VendorError: " + ex.getErrorCode());
			return 0;
		}
	}
	
	public boolean next()
	{
		try {
			return this.rs.next();
		}
		catch (SQLException ex) 
		{
			// handle any errors
			Console.c_log("db","ERROR","SQLException: " + ex.getMessage());
			Console.c_log("db","ERROR","SQLState: " + ex.getSQLState());
			Console.c_log("db","ERROR","VendorError: " + ex.getErrorCode());
			return false;
		}
	}
}

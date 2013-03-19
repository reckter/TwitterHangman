package me.reckter.xsql;

import java.sql.ResultSet;
import java.sql.SQLException;

import me.reckter.misc.Console;

public class Item {
	
	/**
	 * 
	 * The table of the row
	 * @var string
	 */
	protected String table;
	
	/**
	 * 
	 * The value of the primary key of this row
	 * @var string
	 */
	protected String where;
	
	/**
	 * 
	 * The column name of the primary key of the table
	 * @var string
	 */
	protected String primary;
	
	/**
	 * 
	 * An associative array containing the whole row. Note that this gets changed when set() is called but it does not get saved to the db immediately.
	 * @var ResultSet
	 */
	protected ResultSet rs;
	
	/**
	 * 
	 * Wether or not this is a new row
	 * @var bool
	 */
	protected boolean created;
	
	/**
	 * 
	 * Wether this Item is able to peform store()
	 * @var bool
	 */
	protected boolean valid;
	
	/**
	 * 
	 * Db object
	 * @var Db
	 */
	protected Db db;
	
	/**
	 * 
	 * Creates a new Item object. You can create a new row by not setting the $awhere parameter
	 * @param Db The Database object that gets used
	 * @param string The table of the Item
	 * @param string The value of the primaray key of this item
	 * @param string The column name of the primary key (defaults to "id")
	 */
	public Item(Db adb)
	{
		this.db = adb;
	}
	
	
	public Item construct(String atable,String awhere,String aprimary)
	{
		this.created = false;
		this.valid = false;
		this.rs = null;
		
		if(awhere == "")
		{
			if(!this.create(atable))
			{
				Console.c_log("Item", "ERROR","Item creation failed");
			}
		}
		else
		{
			if(!this.load(atable, awhere, aprimary))
			{
				Console.c_log("Item", "ERROR","loading failed");
			}
		}
		return this;
	}
	

	public Item construct(String atable,String awhere)
	{
		return this.construct(atable,awhere,"id");
	}
	
	
	
	public Item construct(String atable)
	{
		return this.construct(atable,"");
	}

	/**
	 * 
	 * Creates a new row for this Item
	 * @param object The Database object
	 * @param string The table of the Item
	 * @return bool Success or no success
	 */
	public boolean create(String atable)
	{
		try {
		this.created = true;
		this.table = atable;
		this.primary = null;

		this.db.query("SHOW COLUMNS FROM `" + this.table + "`");
		this.rs = this.db.getResult();
		
		while(this.rs.next())
		{
			if(rs.getString("Key") == "PRI")
			{
				this.primary = rs.getString("Field");
			}
		}
		this.db.query("SELECT * FROM `" + this.table + "`");
		this.rs = this.db.getResult();
		
		rs.next();
		rs.moveToInsertRow();
		this.valid = true;
		
		}
		catch (SQLException ex)
		{
			// handle any errors
			Console.c_log("db","ERROR","SQLException: " + ex.getMessage());
			Console.c_log("db","ERROR","SQLState: " + ex.getSQLState());
			Console.c_log("db","ERROR","VendorError: " + ex.getErrorCode());
			return false;
		}
		return this.primary != null;
	}
	
	
	public boolean load(String atable, String awhere)
	{
		return load(atable,awhere,"id");
	}
	
	public boolean load(String atable, String awhere, String aprimary)
	{
		this.created = false;
		this.table = atable;
		this.where = awhere;
		this.primary = aprimary;
		
		this.valid = this.db.query("SELECT * FROM `" + this.table + "` WHERE `" + this.primary + "`='" + this.where + "'");
		this.rs = this.db.getResult();
		try {
			this.rs.next();
		}
		catch (SQLException ex)
		{
			// handle any errors
			Console.c_log("db","ERROR","SQLException: " + ex.getMessage());
			Console.c_log("db","ERROR","SQLState: " + ex.getSQLState());
			Console.c_log("db","ERROR","VendorError: " + ex.getErrorCode());
			return false;
		}
		
		return this.rs != null;
	}

	/**
	*
	* Returns wether this Item is able to peform store()
	* @return bool true or false
	*/
	public boolean isValid()
	{
		return this.valid;
	}
	
	/**
	*
	* Returns wether this Item is able to peform store()
	* @return bool true or false
	*/
	public boolean valid()
	{
		return this.isValid();
	}
	
	/**
	 * 
	 * Sets the column $column to the value $content
	 * @param string The name of the column
	 * @param string The value of the column
	 * @return bool false if the column does not exist. Otherwise true
	 */
	public boolean set(String column, String content)
	{
		try {
			this.rs.updateString(column, content);
		} catch (SQLException ex) 
		{
			// handle any errors
			Console.c_log("db","ERROR","SQLException: " + ex.getMessage());
			Console.c_log("db","ERROR","SQLState: " + ex.getSQLState());
			Console.c_log("db","ERROR","VendorError: " + ex.getErrorCode());
			return false;
		}
		
		return true;
	}

	/**
	 * 
	 * Gets the value of the column $column
	 * @param string The name of the column
	 * @return string|null Returns the value or null of the column does not exist
	 */
	public String get(String column)
	{
		try {
			return rs.getString(column);
		} catch (SQLException ex) 
		{
			// handle any errors
			Console.c_log("db","ERROR","SQLException: " + ex.getMessage());
			Console.c_log("db","ERROR","SQLState: " + ex.getSQLState());
			Console.c_log("db","ERROR","VendorError: " + ex.getErrorCode());
			return null;
		}
	}
	
	/**
	 * 
	 * Returns the value of the primary key of this Item
	 * @return string The value
	 */
	public String getPri()
	{
		try {
			return rs.getString(this.primary);
		} catch (SQLException ex) 
		{
			// handle any errors
			Console.c_log("db","ERROR","SQLException: " + ex.getMessage());
			Console.c_log("db","ERROR","SQLState: " + ex.getSQLState());
			Console.c_log("db","ERROR","VendorError: " + ex.getErrorCode());
			return null;
		}
	}
	
	
	private int getColumns()
	{
		int i=1;
		try{
			for(;i<=i+1;i++)
			{
				rs.getString(i);
			}
		}
		catch (SQLException ex)
		{
			if(ex.getSQLState() == "S1009")
			{
				return i-1;
			}
			else
			{
				// handle any errors
				Console.c_log("db","ERROR","SQLException: " + ex.getMessage());
				Console.c_log("db","ERROR","SQLState: " + ex.getSQLState());
				Console.c_log("db","ERROR","VendorError: " + ex.getErrorCode());
				Console.c_log("db","ERROR","StackTrace: " + ex.getStackTrace());
				this.valid = false;
				return -1;
			}
		}
		return -1;
	}
	
	/**
	 * 
	 * Stores the Item with its current status in the Database
	 * @return bool Success or no success
	 */
	public boolean store()
	{
		try {
			if(this.created)
			{
				rs.insertRow();
			}
			else
			{
				rs.updateRow();
			}
			
			return true;
		} catch (SQLException ex) {
			// handle any errors
			Console.c_log("db","ERROR","SQLException: " + ex.getMessage());
			Console.c_log("db","ERROR","SQLState: " + ex.getSQLState());
			Console.c_log("db","ERROR","VendorError: " + ex.getErrorCode());
			return false;
		}
	}
	
	/**
	 * 
	 * Deletes the row from the database. The Item is marked invalid after calling this function.
	 * @return bool Success or no success
	 */
	public boolean delete()
    {
       try {
		rs.deleteRow();
	       return  true;
       } catch (SQLException ex) 
       {
		Console.c_log("db","ERROR","SQLException: " + ex.getMessage());
		Console.c_log("db","ERROR","SQLState: " + ex.getSQLState());
		Console.c_log("db","ERROR","VendorError: " + ex.getErrorCode());
		return false;
       }
    }
}	


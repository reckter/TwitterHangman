package me.reckter.xsql;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Itemcollection {
protected Db db;
	
	/**
	 * 
	 * An array of all items in the collection
	 * @var array
	 */
	protected Item[] items;
	
	/**
	 * 
	 * The part after WHERE of the sql statement
	 * @var string
	 */
	protected String where;
	
	/**
	 * 
	 * The name of the table
	 * @var string
	 */
	protected String table;
	
	/**
	 * 
	 * The name of the primary column of the table
	 * @var string
	 */
	protected String primary;
	
	
	protected int pointer;


	public Itemcollection(Db adb)
	{
		this.db = adb;
	}
	
	public Itemcollection construct(String atable, String awhere, String aprimary)
	{
		this.load(atable, awhere, aprimary);
		return this;
	}
	
	public Itemcollection construct(String atable, String awhere)
	{
		return this.construct(atable, awhere,"id");
	}
	
	public Itemcollection construct(String atable)
	{
		return this.construct(atable, null,"id");
	}
	
	
	/**
	*
	* Loads an ItemCollection.
	*
	* @param object $adb The Database object that will be used for the connection
	* @param string $atable The name of the table
	* @param bool|string $awhere false if every row of the table should be used. Otherwise the WHERE statement
	* @param string $aprimary The name of the primary key. Defaults to "id"
	*
	* @return bool Success or no success
	*/

	public boolean load(String atable, String awhere, String aprimary)
	{
		this.table = atable;
		this.where = awhere;
		this.primary = aprimary;
		
		String sql = "SELECT * FROM `" + this.table + "`";
		String countsql = "SELECT COUNT(*) FROM `" + this.table + "`";
		if(this.where != null)
		{
			sql += " WHERE " + this.where;
			countsql += " WHERE " + this.where;
		}

        try {
            this.db.query(countsql);
            ResultSet countrs = this.db.getResult();
            this.items = new Item[countrs.getInt("COUNT(*)")];
            this.pointer = 0;
            this.db.query(sql);
            ResultSet irs = this.db.getResult();

            for(int i = 1; i <= countrs.getInt("COUNT(*)");i++)
            {
                irs.next();
                this.items[i] = new Item(db).construct(this.table,irs.getString(this.primary),this.primary);
            }
        } catch(SQLException e){

        }
		
		return true;
	}
	
	/**
	 * 
	 * Reloads the ItemCollection from the Database
	 * @return bool Success or no success
	 */
	public boolean refresh()
	{
		return this.load(this.table, this.where, this.primary);
	}
	
	/**
	 * 
	 * Returns the current Item
	 * @return object
	 */
	public Item current()
	{
		return items[pointer];
	}
	

	/**
	 * 
	 * Returns the current Item and moves to the next one
	 * @return object Item
	 */
	public Item next()
	{
		Item ret = current();
		pointer ++;
		if(pointer >= items.length)
		{
			pointer = items.length-1;
		}
		return ret;
	}
	
	/**
	 * 
	 * Sets the pointer to the first Item and returns it
	 * @return object Item
	 */
	public Item first()
	{
		pointer = 0;
		return current();
	}
	
	/**
	 * 
	 * Sets the pointer to the first item
	 */
	public void reset()
	{
		pointer = 0;
	}
	
}


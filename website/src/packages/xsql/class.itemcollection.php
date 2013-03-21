<?php

/**
 * 
 * Represents a collection of items. 
 * @author mischa-holz
 *
 */
class ItemCollection
{
	
	/**
	*
	* The Database object
	* @var object
	*/
	protected $db;
	
	/**
	 * 
	 * An array of all items in the collection
	 * @var array
	 */
	protected $items;
	
	/**
	 * 
	 * The part after WHERE of the sql statement
	 * @var string
	 */
	protected $where;
	
	/**
	 * 
	 * The name of the table
	 * @var string
	 */
	protected $table;
	
	/**
	 * 
	 * The name of the primary column of the table
	 * @var string
	 */
	protected $primary;
	
	/**
	 * 
	 * Creates a new ItemCollection.
	 * 
	 * @param object $adb The Database object that will be used for the connection
	 * @param string $atable The name of the table
	 * @param bool|string $awhere false if every row of the table should be used. Otherwise the WHERE statement
	 * @param string $aprimary The name of the primary key. Defaults to "id"
	 */
	public function __construct($adb, $atable, $awhere = false, $aprimary = "id", $alimit = false, $aorderby = false, $aascend = true)
	{
		$this->load($adb, $atable, $awhere, $aprimary, $alimit, $aorderby, $aascend);
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
	public function load($adb, $atable, $awhere = false, $aprimary = "id", $alimit = false, $aorderby = false, $aascend = true)
	{
		$this->db = $adb;
		$this->table = $atable;
		$this->where = $awhere;
		$this->primary = $aprimary;
		$this->items = array();
		
		$sql = "SELECT * FROM `".$this->table."`";
		if($this->where !== false)
		{
			$sql .= " WHERE ".$this->where;
		}
		if($aorderby !== false) {
			$sql .= " ORDER BY ".$aorderby;
		}
		if($aascend !== true) {
			$sql .= " DESC";
		}
		if($alimit !== false) {
			$sql .= " LIMIT 0,".$alimit;
		}
		
		$this->db->query($sql);
		$res = $this->db->getResult();
		while($row = $res->fetch_assoc())
		{
			$this->items[$row[$this->primary]] = new Item($this->db, $this->table, $row[$this->primary], $this->primary);
		}
		
		return $this->db->getErrno() == 0;
	}
	
	/**
	 * 
	 * Reloads the ItemCollection from the Database
	 * @return bool Success or no success
	 */
	public function refresh()
	{
		return $this->load($this->db, $this->table, $this->where, $this->primary);
	}
	
	/**
	 * 
	 * Returns the current Item
	 * @return object
	 */
	public function current()
	{
		return current($this->items);
	}
	
	/**
	 * 
	 * Returns the current Item and moves to the next one
	 * @return object Item
	 */
	public function next()
	{
		$ret = $this->current();
		next($this->items);
		return $ret;
	}
	
	/**
	 * 
	 * Sets the pointer to the first Item and returns it
	 * @return object Item
	 */
	public function first()
	{
		reset($this->items);
		return current($this->items);
	}
	
	/**
	 * 
	 * Sets the pointer to the first item
	 */
	public function reset()
	{
		reset($this->items);
	}
	
	/**
	 * Returns the item with the primary value $aprimary
	 * @param string $aprimary A primary value
	 */
	public function getRow($aprimary)
	{
		if(!isset($this->items[$aprimary]))
		{
			return false;
		}
		return $this->items[$aprimary];
	}
	
	/**
	 * 
	 * Removes an Item from the collection and from the database
	 * @param string $aprimary The primary value of the Item
	 * @return bool Succes or no success
	 */
	public function remove($aprimary)
	{
		if(!isset($this->items[$aprimary]))
		{
			return false;
		}
		$ret = $this->items[$aprimary]->delete();
		unset($this->items[$aprimary]);
		return $ret;
	}
	
	/**
	 * 
	 * Adds a new Item to the collection. NOTE: This does not add an Item to the database.
	 * @param object $item The new Item
	 */
	public function add($item)
	{
		$this->items[$item->getPri()] = $item;
		return $item->store();
	}
	
	/**
	 * 
	 * Stores every Item in the database by calling $item->store()
	 * @return bool Returns false if one of the Items returend false when calling store()
	 */
	public function storeAll()
	{
		$this->reset();
		while($cur = $this->next())
		{
			if(!$cur->store())
			{
				return false;
			}
		}
		return true;
	}
}

?>
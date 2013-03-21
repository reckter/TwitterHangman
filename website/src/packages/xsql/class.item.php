<?php

/**
 * 
 * Represents a single row in a table
 * @author mischa-holz
 *
 */
class Item
{
	/**
	 * 
	 * The Database object
	 * @var object
	 */
	protected $db;
	
	/**
	 * 
	 * The table of the row
	 * @var string
	 */
	protected $table;
	
	/**
	 * 
	 * The value of the primary key of this row
	 * @var string
	 */
	protected $where;
	
	/**
	 * 
	 * The column name of the primary key of the table
	 * @var string
	 */
	protected $primary;
	
	/**
	 * 
	 * An associative array containing the whole row. Note that this gets changed when set() is called but it does not get saved to the db immediately.
	 * @var array
	 */
	protected $data;
	
	/**
	 * 
	 * Wether or not this is a new row
	 * @var bool
	 */
	protected $created;
	
	/**
	 * 
	 * Wether this Item is able to peform store()
	 * @var bool
	 */
	protected $valid;
	
	/**
	 * 
	 * Creates a new Item object. You can create a new row by not setting the $awhere parameter
	 * @param object The Database object that gets used
	 * @param string The table of the Item
	 * @param string The value of the primaray key of this item
	 * @param string The column name of the primary key (defaults to "id")
	 */
	public function __construct($adb = null, $atable = "", $awhere = "", $aprimary = "id")
	{
		$this->created = false;
		$this->valid = false;
		$this->data = array();
		
		if($atable == "") {
			return;
		} else {
			if($awhere == "") {
				if(!$this->create($adb, $atable)) {
					xError("Item creation failed", false, true);
				}
			} else {
				if(!$this->load($adb, $atable, $awhere, $aprimary)) {
					xError("loading failed");
				}
			}
		}
	}
	
	/**
	 * 
	 * Creates a new row for this Item
	 * @param object The Database object
	 * @param string The table of the Item
	 * @return bool Success or no success
	 */
	public function create($adb, $atable)
	{
		$this->created = true;
		$this->db = $adb;
		$this->table = $atable;
		$this->primary = false;
		
		$this->db->query("SHOW COLUMNS FROM `".$this->table."`");
		
		while($col = $this->db->next())
		{
			if($col['Key'] == "PRI")
			{
				$this->primary = $col['Field'];
			}
			$this->data[$col['Field']] = "";
		}
		
		$this->valid = true;
		
		return $this->primary !== false;
	}
	
	/**
	 * 
	 * Loads a row from the Database to the Item
	 * @param object The Database object that gets used
	 * @param string The table of the Item
	 * @param string The value of the primaray key of this item
	 * @param string The column name of the primary key (defaults to "id")
	 * @return bool Success or no success
	 */
	public function load($adb, $atable, $awhere, $aprimary = "id")
	{
		$this->created = false;
		$this->db = $adb;
		$this->table = $atable;
		$this->where = $awhere;
		$this->primary = $aprimary;
		
		$this->valid = $this->db->query("SELECT * FROM `".$this->table."` WHERE `".$this->primary."`='".$this->where."'");
		$this->data = $this->db->next();
		
		return $this->data !== false;
	}
	
	/**
	 * Returns the db object associated with this item
	 * @return object
	 */
	public function getDB() {
		return $this->db;
	}
	
	/**
	 * 
	 * Returns wether this Item is able to peform store()
	 * @return bool true or false
	 */
	public function isValid()
	{
		return $this->valid;
	}
	
	/**
	*
	* Returns wether this Item is able to peform store()
	* @return bool true or false
	*/
	public function valid()
	{
		return $this->isValid();
	}
	
	/**
	 * 
	 * Sets the column $column to the value $content
	 * @param string The name of the column
	 * @param string The value of the column
	 * @return bool false if the column does not exist. Otherwise true
	 */
	public function set($column, $content)
	{
		if(!isset($this->data[$column]))
		{
			return false;
		}
		
		$this->data[$column] = $content;
		
		return true;
	}
	
	/**
	 * 
	 * Gets the value of the column $column
	 * @param string The name of the column
	 * @return string|null Returns the value or null of the column does not exist
	 */
	public function get($column)
	{
		if(!isset($this->data[$column]))
		{
			return null;
		}
		
		return $this->data[$column];
	}
	
	/**
	 * 
	 * Returns the value of the primary key of this Item
	 * @return string The value
	 */
	public function getPri()
	{
		return $this->data[$this->primary];
	}
	
	/**
	 * 
	 * Stores the Item with its current status in the Database
	 * @return bool Success or no success
	 */
	public function store()
	{
		$sql = "";
		if($this->created)
		{
			$sql = "INSERT INTO `".$this->table."` (";
			
			reset($this->data);
			
			while(($col = current($this->data)) !== false)
			{
				$sql .= "`".key($this->data)."`, ";
				next($this->data);
			}
			
			$sql = substr($sql, 0, strlen($sql) - 2);
			$sql .= ") VALUES (";
			
			reset($this->data);
			
			while(($col = current($this->data)) !== false)
			{
				if($col == "")
				{
					$sql .= "NULL, ";
				}
				else
				{
					$sql .= "'".$col."', ";
				}
				next($this->data);
			}
			
			$sql = substr($sql, 0, strlen($sql) - 2);
			$sql .= ");";
		}
		else
		{
			$sql = "UPDATE `".$this->table."` SET ";
			foreach($this->data as $key => $value)
			{
				if($key == $this->primary)
				{
					continue;
				}
				$sql .= "`".$key."` = '".$value."', ";
			}
			$sql = substr($sql, 0, strlen($sql) - 2);
			$sql .= " WHERE `".$this->primary."` = '".$this->getPri()."';";
		}
		
		$this->db->query($sql);
		
		if($this->created)
		{
			$this->data[$this->primary] = $this->db->getInsertId();
		}
		
		$this->created = false;
		
		return $this->db->getErrno() == 0;
	}
    
	/**
	 * 
	 * Deletes the row from the database. The Item is marked invalid after calling this function.
	 * @return bool Success or no success
	 */
    public function delete()
    {
        $sql = "DELETE FROM `".$this->table."` WHERE (`".$this->primary."`='".$this->getPri()."')";
        
        while($col = current($this->data))
        {
        	if(key($this->data) == $this->primary)
        	{
        		next($this->data);
        		continue;
        	}
        	$sql .= " AND (`".key($this->data)."`='".$col."')";
        	next($this->data);
        }
        
        $sql .= " LIMIT 1";
        
        $this->db->query($sql);
        
        $this->valid = false;
        
        return $this->db->getErrno() == 0;
    }

    static function rowExists($db, $table, $value, $col = "id")
        {
        	$str = "SELECT * FROM `".$table."` WHERE `".$col."`='".$value."'";
        	$result = $db->query($str);
        	$data = $db->next();

        	return $data != false;
        }
}

?>
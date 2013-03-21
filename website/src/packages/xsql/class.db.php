<?php

global $database;

/**
 * Represents a databse connection. The class can do Queries and you can browse through them
 * 
 * @author mischa-holz
 */
class Database
{
	//TODO: add more engines
	/**
	 * 
	 * The MySQLi object
	 * @var object
	 */
	protected $mysqli_db;
	
	/**
	 * 
	 * true if the object is currently able to perform queries
	 * @var bool
	 */
	protected $valid;
	
	/**
	 * 
	 * The result of the last query
	 * @var object
	 */
	protected $current_result;
	
	/**
	 * 
	 * The last query
	 * @var string
	 */
	protected $current_query;
	
	/**
	 * 
	 * basic constructor. NOTE: A new databse object connects to the database specified in config.xsql.php
	 * You have to use Database::getConn() to get a database object. This is to prevent multiple connections to the same db.
	 */
	protected function __construct()
	{
		$this->valid = false;
		
		$this->mysqli_db = new mysqli(SERVER, USERDB, PW, DB);
		if($this->mysqli_db->error)
		{
			return;
		}
		
		$this->valid = true;
	}
	
	public static function getConn() {
		global $database;
		
		if(!isset($database)) {
			$database = new Database();
		}
		
		return $database;
	}
	
	/**
	 * 
	 * Executes a query
	 * @param string $aq The query
	 * @return bool true if the query was succesfull. Otherwise false. Use getError and getErrno to get more information
	 */
	public function query($aq)
	{
		if(!$this->valid)
		{
			return false;
		}
		
		$this->current_query = false;
		$this->current_result = $this->mysqli_db->query($aq);
		
		if($this->mysqli_db->errno != 0)
		{
			$valid = false;
			xError($aq." => ".$this->mysqli_db->error, DEBUG);
			
			return false;
		}
		
		$this->current_query = $aq;
		
		return true;
	}
	
	/**
	 * 
	 * Returns the current error number of the mysqli object
	 * @return int mysqli->errno
	 */
	public function getErrno()
	{
		return $this->mysqli_db->errno;
	}
	
	/**
	 * 
	 * Returns the current error string of the mysqli object
	 * @return string mysqli->error
	 */
	public function getError()
	{
		return $this->mysqli_db->error;
	}
	
	/**
	 * 
	 * Returns the id of the last inserted row
	 * @return int mysqli->insert_id
	 */
	public function getInsertId()
	{
		return $this->mysqli_db->insert_id;
	}
	
	/**
	 * Returns the current result to cache it somewhere.
	 * @return object mysqli_result
	 */
	public function getResult()
	{
		return $this->current_result;
	}
	
	/**
	 * 
	 * Returns the next row of results from the current query
	 * @return bool|array false if something went wrong. Otherwise the row as an associative array
	 */
	public function nextRow()
	{
		if((!$this->valid) || (!$this->current_query))
		{
			return false;
		}
		
		$ret = $this->current_result->fetch_assoc();
		
		if($ret === false)
		{
			$this->current_query = false;
			
			return false;
		}
		
		return $ret;
	}
	
	/**
	 * 
	 * Wrapper for nextRow
	 * @see nextRow
	 */
	public function next()
	{
		return $this->nextRow();
	}
	
	public function escape($str)
	{
		return $this->mysqli_db->real_escape_string($str);
	}
	
	/**
	 * Counts the rows in a table with certain conditions
	 * @param string $table The table
	 * @param string $where The condition to count
	 */
	public function count($table, $where = "") 
	{
		$query = "SELECT COUNT(*) FROM ".$table;
		if($where != "") 
		{
			$query .= " WHERE ".$where;
		}
		$this->query($query);
		$row = $this->next();
		return $row["COUNT(*)"];
	}
}

?>
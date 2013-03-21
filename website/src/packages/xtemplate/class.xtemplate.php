<?php

/**
 * A class to manage and display HTML templates
 * @author mischa-holz
 *
 */
class xTemplate
{
	/**
	 * Contains the HTML file or the replaced values
	 * @var string
	 */
	protected $buffer;
	
	/**
	 * The array with all keys and values that are to be replaced
	 * @var string
	 */
	protected $content;
	
	/**
	 * Creates a new template
	 * @param string $template The name of the HTML file.
	 */
	public function __construct($template)
	{
		$this->content = Array();
		
		$this->buffer = false;
		
		$path_arr = explode(PATH_SEPARATOR, get_include_path());
		foreach($path_arr as $p)
		{
			if(file_exists($p."/templates/".$template.".html"))
			{
				$this->buffer = file_get_contents($p."/templates/".$template.".html");
			}
		}
		
		if($this->buffer === false)
		{
			$this->buffer = file_get_contents(ROOT."/templates/".$template.".html");
		}
		
	}
	
	/**
	 * Sets a key to value. Note that the old value is being replaced with a new one.
	 * @param string $key The key without the brackets
	 * @param string $value The value that will replace the key
	 */
	public function set($key, $value)
	{
		$this->content["{".$key."}"] = $value;
	}
	
	/**
	 * Adds a value to a key. If the key does not exist, it creates a new one.
	 * add("example", "hello ")
	 * add("example", "world!")
	 * After the call of these 2 functions {example} will be replace with "hello world!"
	 * 
	 * @param string $key The key without the brackets.
	 * @param string $value The value which will be added to the key.
	 */
	public function add($key, $value)
	{
		if(isset($this->content["{".$key."}"]))
		{
			$this->content["{".$key."}"] .= $value;
		}
		else
		{
			$this->content["{".$key."}"] = $value;
		}
	}
	
	/**
	 * Replaces every key in the buffer with its value.
	 */
	protected function prepareEcho()
	{
		$keys = array_keys($this->content);
		for($i = 0; $i < count($keys); $i++)
		{
			$key = $keys[$i];
			$value = $this->content[$key];
			$this->buffer = str_replace($key, $value, $this->buffer);
		}
	}
	
	/**
	 * Relaces every key with its value and echos the buffer
	 */
	public function render()
	{
		$this->prepareEcho();
		echo($this->buffer);
	}
	
	/**
	 * Replaces every key with its value and returns the buffer as a string
	 * @return string The buffer
	 */
	public function renderAsString()
	{
		$this->prepareEcho();
		return $this->buffer;
	}
	
	/**
	 * Wrapper for add("SCRIPTS", "&lt;script type='text/javascript' src='".$script."'&gt;&lt;/script&gt;")
	 * @param string $script The path to a javascript
	 */
	public function addScript($script)
	{
		$this->add("SCRIPTS", "<script src='".$script."'></script>");
	}
}

?>
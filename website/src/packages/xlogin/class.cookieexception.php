<?php

class CookieException extends Exception
{
	public function __construct()
	{
		Exception::__construct();
		$this->message = "This user hasnt enabled cookies.";
	}
}

?>
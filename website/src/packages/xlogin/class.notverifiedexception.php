<?php

class NotVerifiedException extends Exception
{
	public function __construct()
	{
		Exception::__construct();
		$this->message = "Your account is not verified.";
	}
}

?>
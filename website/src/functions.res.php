<?php

define("ROOT", dirname(__FILE__));
define("PROOT", ROOT."/packages");
define("DEBUG", true);

function includePackage($apackage) {
	if(!file_exists(PROOT."/".$apackage."/package.".$apackage.".php")) {
		//return false;
	}
	
	set_include_path(get_include_path().PATH_SEPARATOR.PROOT."/".$apackage);
	
	if(file_exists(PROOT."/".$apackage."/config.".$apackage.".php")) {
		include_once("config.".$apackage.".php");
	}
	if(file_exists(PROOT."/".$apackage."/package.".$apackage.".php")) {
		include_once("package.".$apackage.".php");
	} else {
		$arr = glob(PROOT."/".$apackage."/*.php");
		foreach($arr as $file) {
			include_once($file);
		}
	}
	return true;
}

function xPrintr($obj) {
	echo("<pre>");
	print_r($obj);
	echo("</pre>");
}

function xError($msg = "", $show = false, $die = false) {
	if($msg == "") {
		$msg = "The developer forgot to include an error message here";
	}
	$err_str = date("[d-m-Y H:i:s]")." Error: ".$msg."\n";
	
	$e = new Exception();
	$stack = $e->getTrace();
	
	foreach($stack as $fun) {
		if($fun['function'] == "xError") {
			continue;
		}
		
		$err_str .= "at ".$fun['function']." in ".$fun['file']."(".$fun['line'].")\n";
	}
	
	file_put_contents(ROOT."/logs/error.log", $err_str, FILE_APPEND);
	
	if($show) {
		echo(str_replace("\n", "<br>", $err_str));
	}
	
	if($die) {
		die();
	}
}

?>
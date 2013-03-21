x<?php

function login($username, $password)
{	
	$user = xUser::getUserFromName($username);
	
	if($user == null)
	{
		return false;
	}
	
	if($user->getVerified() != 1)
	{
		throw new NotVerifiedException();
		return false;
	}
	
    $pwstring = xUser::encryptPw($password, $user->getSalt());
    
	if($pwstring != $user->getPW())
	{
		return false;
	}
	
	$time = time();
	$addr = $_SERVER['REMOTE_ADDR'];
		
	if(!setcookie("username", $username, $time + 30 * 60 * 60, "/"))
	{
		throw new CookieException();
		return false;
	}
	if(!setcookie("token", $time, $time + 30 * 60 * 60, "/"))
	{
		throw new CookieException();
		return false;
	}
	
	$db = Database::getConn();
	$str = "UPDATE `xlogin` SET `token`='".$time.$addr."' WHERE `id`='".$user->getId()."';";
	$db->query($str);
	
	return true;
}

function getThisUser()
{
	return xUser::getUserFromName($_COOKIE['username']);
}

function isThisUserLoggedIn($xuser)
{
	return $xuser->getToken() == $_COOKIE['token'].$_SERVER['REMOTE_ADDR'];
}

function isLoggedIn()
{
	if(getThisUser() == null)
	{
		return false;
	}
	return isThisUserLoggedIn(getThisUser());
}

function logout()
{
	if(!isLoggedIn())
	{
		return;
	}
	
	$db = new Database();
	$db->query("UPDATE `xlogin` SET `token`='loggedout' WHERE `id`='".getThisUser()->getId()."';");
	
	$_SESSION = array();
	setcookie(session_name(), '', time() - 3600, '/');
	setcookie("token", '', time() - 3600, '/');
	setcookie("username", '', time() - 3600, '/');
}

?>
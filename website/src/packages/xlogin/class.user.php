<?php

/**
 * The cache for all user objects
 * @var array
 */
global $user_cache;

/**
 * 
 * Represents a single user
 * @author mischa-holz
 *
 */
class xUser
{
	/**
	 * 
	 * The id of the user
	 * @var int
	 */
	protected $id;
	
	/**
	 * 
	 * The login name
	 * @var string
	 */
	protected $name;
	
	/**
	 * 
	 * The encrpyted password
	 * @var string
	 */
	protected $pw;
	
	/**
	 * 
	 * The salt that was used to generate $pw
	 * @var string
	 */
	protected $salt;
	
	/**
	 * 
	 * The email adress of the user
	 * @var string
	 */
	protected $mail;
	
	/**
	 * 
	 * The login token of the user. This is "loggedout" if the user is not logged in or empty if the user never logged in.
	 * @var string
	 */
	protected $token;
	
	/**
	 * 
	 * 1 if the user's email adress is verified. 0 otherwise
	 * @var string
	 */
	protected $verified;
	
	/**
	 * 
	 * Enter description here ...
	 * @var unknown_type
	 */
	protected $item;
	
	protected $rights;
	
	protected function __construct()
	{
		$this->id = 0;
		$this->name = "";
		$this->pw = "";
		$this->salt = "";
		$this->mail = "";
		$this->token = "";
		$this->verified = false;
		$this->item = null;
        $this->rights = "";
	}
	
	public static function getUserFromId($id)
	{
		global $user_cache;
		
		if($id == "")
		{
			return null;
		}
		
		if(isset($user_cache["id:".$id]))
		{
			return $user_cache["id:".$id];
		}
		
		$item = new Item(Database::getConn(), "xlogin", $id);
		
		if(!$item->valid())
		{
			return null;
		}
		
		$ret = new xUser();
		$ret->id = $item->get("id");
		$ret->name = $item->get("login");
		$ret->pw = $item->get("pw");
		$ret->salt = $item->get("salt");
		$ret->mail = $item->get("email");
		$ret->token = $item->get("token");
        $ret->verified = $item->get("verified");
        $ret->rights = explode("," , $item->get("rights"));
		$ret->item = $item;
        
		$user_cache["id:".$id] = $ret;
		
		return $ret;
	}
	
	public static function getUserFromName($name)
	{		
		global $user_cache;
		
		if($name == "")
		{
			return null;
		}
		
		if(isset($user_cache["name:".$name]))
		{
			return $user_cache["name:".$name];
		}
		
		$item = new Item(Database::getConn(), "xlogin", $name, "login");
		
		if(!$item->valid())
		{
			return null;
		}
		
		$ret = new xUser();
		
		$ret->id = $item->get("id");
		$ret->name = $item->get("login");
		$ret->pw = $item->get("pw");
		$ret->salt = $item->get("salt");
		$ret->mail = $item->get("email");
		$ret->token = $item->get("token");
		$ret->verified = $item->get("verified");
        $ret->rights = explode("," , $item->get("rights"));
        $ret->item = item;
        
		$user_cache["name:".$name] = $ret;
		
		return $ret;
	}
	
	public function getId()
	{
		return $this->id;
	}
	
	public function getName()
	{
		return $this->name;
	}
	
	public function getPW()
	{
		return $this->pw;
	}
	
	public function getSalt()
	{
		return $this->salt;
	}
	
	public function getMail()
	{
		return $this->mail;
	}
	
	public function getToken()
	{
		return $this->token;
	}
	
	public function getVerified()
	{
		return $this->verified;
	}
    
    public function getRights()
    {
        return rights;
    }
    
    public function setPw($apw)
    {
        $this->pw = xUser::encryptPw($apw,$this->salt);
    }
    
    public function setSalt($asalt)
    {
        $this->salt = $asalt;
    }
    
    public function setName($aname)
    {
        if($this->getUserFromName($aname) == null)
        {
            $this->name = $aname;
            return false;
        }
        return false;
    }
    
    
    /*public function setMail($amail)
    {
        if($amail == explode("@",$amail) && $amail == explode(".",explode("@",$amail)[1]))
        {
            $this->name = $aname;
            return true;
        }
        return false;
    }*/
    
    public function delRight($aright)
    {
        if(!in_array($this->rights , $aright))
        {
            array_push($this->rights , $aright);
        }
    }
    
    public function addRight($aright)
    {
        if(!in_array($this->rights , $aright))
        {
            array_splice(array_search($aright,$this->rights),$this->rights);
        }
    }
    
    public function hasRight($aright)
    {
        return in_array($aright, $this->rights);
    }
    
    public function setVerified($averfified)
    {
        $this->verified = $averified;
    }
    
    public function store()
    {
        $item = $this->item;
        $item->set("login",$this->aname);
        $item->set("email",$this->email);
        $item->set("salt",$this->salt);
        $item->set("pw",$this->pw);
        $item->set("token",$this->token);
        
        $rightscount = 0;
        foreach($this->rights as $right)
        {
            if(rightscount == 0)
            {
                $rights = $right;
            }
            else
            {
                $rights = $rights.",".$right;
            }
            $rightscount++;
        }
        $item->set("rights", $rights);
        
        $item->store();
    }
    
    
    public static function encryptPw($apw , $salt)
    {
        $pwstring = md5($apw.$salt);
        $pwstring = md5($pwstring.$salt);
        return $pwstring;
    }
}


?>
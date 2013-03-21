<?php
/**
*
* benötigt xsql
*
*
* xGroup for xlogin. Represents a single user.
*
* package: xlogin
* author: reckter
* version: 0.1
*/

/**
* 
* Represents a single user
* @author mischa-holz
*
*/
class xGroup
{    /**
      * 
      * The id of the group
      * @var int
      */
    protected $id;
	
    /**
	 * 
	 * The name of the group
	 * @var string
	 */
	protected $name;
	
    /**
	 * 
	 * The xUser Object of the Owner
	 * @var xUser
	 */
	protected $owner; //an xUser object
    
    /**
	 * 
	 * The members of the Group
	 * @var array [int]
	 */
    protected $member;
	
    /**
	 * 
	 * Enter description here ...
	 * @var unknown_type
	 */
	protected $item;
    
    /**
	 * 
	 * Creates an ampty new xGroup object. Use xGroup::getFromId() instead;
	 * 
	 */
    protected function __construct()
	{
		$this->id = 0;
		$this->name = "";
        $this->owner = "";
        $this->member = array();
		$this->item = null;
	}
    
    /**
	 * 
	 * Creates an ampty new xGroup object. Use xGroup::getFromId() instead;
	 * @param id op the group
     * @return the xGroup object
	 */
    public function getFromId($id)
    {
        $item = new Item(new Database(), "xGroup", $id);
		
		if(!$item->valid())
		{
			return null;
		}
        
        $this->item = $item;
        $ret->id = $item->getPri();
        $ret->name = $item->get("name");
        $ret->owner = $xUser->getUserFromId($item->get("owner"));
        
        $items = new Itemcollection(new Database(), "xGroup_member","`id`='".$item->getPri()."'");
        if(!$item->valid())
		{
			return null;
		}
        $member = "";
        while($item = $items->next())
        {
            $member = $member.",".$member;
        }
        $ret->member = $member;
    }
    
    public function getId()
    {
        return $this->id;
    }
    
    public function getName()
    {
        return $this->name;
    }
    
    public function getOwnerName()
    {
        return $this->Owner->getName;
    }
    
    public function getOwnerId()
    {
        return $this->Owner->getId;
    }
    
    public function getOwner()
    {
        return $this->Owner;
    }
    
    public function getMember()
    {
        return $this->member;
    }
    
    
    
    public function setName($aname)
    {
        $this->name = $aname;
    }
    
    public function setOwner($aOwnerId)
    {
        $this->Owner = xUser::getUserFromId($aOwnerId);
    }
    
    public function delMember($aMemberId)
    {
        if(in_array($aMemberId,$this->member))
        {
            $items = new Itemcollection(new Database(), "xGroup_member", "`member` = '".$aMemberId."' AND `id` = '".$this->id."'");
            $item = $items->next;
            $item->delete();
        }
    }
    public function addMember($aMemberId)
    {
        if(!in_array($aMemberId,$this->member))
        {
            $item = new Item(new Database(),"xGroup_member");
            $item->set("id",$this->id);
            $item->set("member",$aMemberId);
            $item->store;
        }
    }
    
    public function store()
    {
        $item = $this->item;
        $item->set("name",$this->name);
        $item->set("owner",$this->Owner->getId());
        $item->store;
    }
}
?>
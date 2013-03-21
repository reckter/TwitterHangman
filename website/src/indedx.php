<?PHP
include_once("functions.res.php");

includePackage("xtemplate");
includePackage("xsql");

$tpl = new xTemplate("index");
$tpl->set("submit_done","");

if(isset($_POST['word'])){
    if(!Item::rowExists(Database::getConn(),"words",$_POST['word'],"word") ){
        $item = new Item(Database::getConn(),"words");
        $item->set("word",$_POST['word']);
        $item->store();
        $tpl->set("submit_done","word submited! Thanks! :)");
    }
    else {
        $tpl->set("submit_done","word allready exists");
    }
}

$items = new Itemcollection(Database::getConn(),"user");
while($item = $items->next())
{
    $tpl_score = new xTemplate("score");
    $tpl_score->set("name","@".$item->get("name"));
    $tpl_score->set("score",$item->get("score"));
    $tpl->add("scoreboard",$tpl_score->renderAsString());
}

$tpl->render();

?>
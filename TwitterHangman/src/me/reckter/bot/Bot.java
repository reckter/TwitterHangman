package me.reckter.bot;

import me.reckter.Twitter.Twitter;
import me.reckter.misc.Console;
import me.reckter.xsql.Db;
import me.reckter.xsql.Item;
import twitter4j.Status;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: reckter
 * Date: 19.03.13
 * Time: 15:04
 * To change this template use File | Settings | File Templates.
 */
public class Bot{

    private long lastMentionId;
    private Twitter twitter;
    private boolean isPlaying;
    List<Character> word;

       Db db;
    public Bot(){
        db = new Db("localhost","hangman","**","**");
        try {
            twitter = new Twitter();
        } catch (IOException e)
        {
            Console.c_log("ERROR", "Twitter", e.toString());
        }
        List<Status> statuses = twitter.getMentions();
        if(statuses == null)
        {
            lastMentionId = -1;
            return;
        }
        lastMentionId = statuses.get(0).getId();
    }


    public void  tick()
    {
        checkMentions();
    }

   private void checkMentions()
   {
       List<Status> statuses = twitter.getMentions();
        if(statuses == null)
        {
            return;
        }
       if(statuses.get(0).getId() == lastMentionId && lastMentionId != -1)
       {
           return;
       }

       int i = 0;
       while(statuses.get(i).getId() != lastMentionId || i == statuses.size())
       {
           i++;
       }

       while(i >= 0)
       {
           //TODO Mentions stuff!
           processMention(statuses.get(i));
           i--;
       }
       lastMentionId = statuses.get(0).getId();
   }

    private void processMention(Status replie){

        Console.c_log("TwitterListener","replie",replie.getText());
        String rawMessage = replie.getText();
        String[] splitMessage = rawMessage.split(" ",2);
        String message = splitMessage[1];

        if(message.startsWith("/")) {
            message = message.substring(1,message.length());

            if(message.startsWith("new)")) {
                if(isPlaying == false)
                {
                    startNewGame();
                    Console.c_log("Bot","NewGame", "@" + replie.getUser().getScreenName() + " has started a new game!");
                }
            }
            else if(message.startsWith("score")) {
                Item item = new Item(db).construct("user","" +  replie.getUser().getId());
                Console.c_log("Bot","Score", "Telling @" + replie.getUser().getScreenName() + " his/her score");
                if(item == null)
                {
                    twitter.replie(" You have no score yet!", replie);
                }
                twitter.replie(" Your score is " + item.get("score"), replie);
            }
        }else {

            //TODO Mentions stuff!
        }
    }

    private void startNewGame(){
        //TODO start new game
    }


}

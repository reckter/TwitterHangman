package me.reckter.bot;

import me.reckter.Twitter.Twitter;
import me.reckter.misc.Console;
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


    public Bot(){
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
           twitter.replie("hello!", statuses.get(i).getId());
           i--;
       }
       lastMentionId = statuses.get(0).getId();
   }


}

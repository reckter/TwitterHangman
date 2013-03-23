package me.reckter.bot;

import me.reckter.Twitter.Twitter;
import me.reckter.misc.Console;
import me.reckter.xsql.Db;
import me.reckter.xsql.Item;
import me.reckter.xsql.Itemcollection;
import sun.security.pkcs11.wrapper.CK_SSL3_RANDOM_DATA;
import twitter4j.Status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: reckter
 * Date: 19.03.13
 * Time: 15:04
 * To change this template use File | Settings | File Templates.
 */
public class Bot {

    private long lastMentionId;
    private Twitter twitter;

    Db db;

    private Hangman hangman;
    private boolean isPlaying;

    private ActiveUserHandler activeUserHandler;

    private boolean isVoting;
    private long voteStarted;
    private long lastVoteStatus;

    private long lastStatusUpdate;
    private long lastMentionCheck;
    private boolean update;

    public Bot(){
        db = new Db("localhost","hangman","**","**;");


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

        hangman = new Hangman(db);
        isPlaying = false;
        lastMentionId = statuses.get(0).getId();
        isVoting = false;
        voteStarted = -1;
        lastVoteStatus = -1;
        lastMentionCheck = System.currentTimeMillis();
        lastStatusUpdate = System.currentTimeMillis() - 90 * 1000;
        activeUserHandler = new ActiveUserHandler();
        update = false;
    }


   private void checkMentions()
   {
       List<Status> statuses = twitter.getMentions();
        if(statuses == null || (statuses.get(0).getId() == lastMentionId && lastMentionId != -1)) {
            return;
        }

       int i = 0;

       while( i < statuses.size() && statuses.get(i).getId() != lastMentionId) {
           i++;
       }

       i--;
       while(i >= 0){

           //splitting the message into 3 parts: "@BOTNAME" + letters / word + rest that may ocour
           Console.c_log("TwitterListener", "reply", "@" + statuses.get(i).getUser().getScreenName() + ": " + statuses.get(i).getText());

           String[] message = statuses.get(i).getText().split(" ",3);
           if(message[1].startsWith("/")) {
               checkCommands(statuses.get(i));
           }
           else {
               Status reply = statuses.get(i);
               if(isPlaying == false)
               {
                   return;
               }

               //handling letter checking
               update = true;

               //set the Player to active
               activeUserHandler.addActivePlayer(reply.getId());


               //checking if the letter are right and if, make them visible
               int score = 0;
               char[] letters = message[1].toUpperCase().toCharArray();

               if(letters.length == 1)
               {
                   if(letters[0] != ' ') {
                       //checking if it was asked before
                       score = hangman.checkLetter(letters[0]);
                   }
               }else {
                   //checking if the asked word is corect
                   if(!hangman.checkWord(letters)) {
                       score = -10;
                   }
               }

               if(hangman.checkGameStatus() == -1){
                   lostGame();
               }

               if(hangman.checkGameStatus() == 2){
                   wonGame(reply);
                   score = 10;
               }
               //saving the core
               Item user = new Item(db).construct("user","" +  reply.getUser().getId());
               if(!user.isValid()) {
                   user = new Item(db).construct("user");
                   user.set("score","" + score);
                   user.set("id","" + reply.getUser().getId());
                   user.set("name","" + reply.getUser().getScreenName());
               }
               else {
                   user.set("score", "" + (Integer.parseInt(user.get("score")) + score));
               }
               user.store();
           }

           i--;
       }
       lastMentionId = statuses.get(0).getId();
   }

    private void checkCommands(Status reply) {
        String[] messageTmp = reply.getText().split("/");
        String[] message = messageTmp[2].split(" ");

        if(message[1].startsWith("new")) {
            if(isPlaying == false)
            {
                startNewGame();
                Console.c_log("Bot", "NewGame", "@" + reply.getUser().getScreenName() + " has started a new game!");
            }
        }
        else if(message[1].startsWith("score")) {
            Item item = new Item(db).construct("user", "" + reply.getUser().getId());
            Console.c_log("Bot","Score", "Telling @" + reply.getUser().getScreenName() + " his/her score");
            if(item.isValid()) {
                twitter.reply("Your score is " + item.get("score"), reply);
            }
            else {
                twitter.reply("You have no score yet!", reply);
            }
        }
        else if(message[1].startsWith("ping")) {
            twitter.reply("Pong!", reply);
        }
        else if(message.equals("update")) {
            update = true;
        }
        else if(message[1].startsWith("next")) {
            if(voteStarted - System.currentTimeMillis() < 10 * 60 * 1000 && isVoting == false)
            {
                return;
            }
            isVoting = true;
            activeUserHandler.vote(reply.getUser().getId(),1);
            if(voteStarted == -1) {
                voteStarted = System.currentTimeMillis();
            }
        }
    }






    private void lostGame() {
        tweetGameStatus("To many mistakes! You lost the game!*trollface*");

        isPlaying = false;

    }

    private void wonGame(Status winStatus) {
       tweetGameStatus("@" + winStatus.getUser().getScreenName() + " solved it!");
        isPlaying = false;

    }


    private void startNewGame(){

        hangman.startNewGame();

        isPlaying = true;
        activeUserHandler.clear();

        lastStatusUpdate = System.currentTimeMillis();

        isVoting = false;
        voteStarted = -1;

        tweetGameStatus("Just started a new Game!");

    }

    private void tweetGameStatus(String additionalMessage){
        char[] wrongLetters = hangman.getWrongLetters();
        String printWrongLetters = "";
        for(char c: wrongLetters) {
            printWrongLetters += c + ", ";
        }
        twitter.tweet(hangman.prepareWord() + " " + hangman.getWrongGuesses()+ "/10 mistakes, wrong letters: " + printWrongLetters + " " + additionalMessage + " #TwitterHangman ");
        update = false;
    }





    public void  tick()
    {
        //checking the Mentions (every 60 seconds)
        if(System.currentTimeMillis() - lastMentionCheck >= 60 * 1000) {
            checkMentions();
            lastMentionCheck = System.currentTimeMillis();
        }

        //handling votes
        if(isVoting == true && lastVoteStatus - System.currentTimeMillis() >= 60 * 1000)
        {
            if(activeUserHandler.getVotes() * 2 >= activeUserHandler.getActiveUsers())
            {
                twitter.tweet("Vote was succesful! (" + activeUserHandler.getVotes() + "/" + activeUserHandler.getActiveUsers() + ") starting new Game now.");
                isVoting = false;
                startNewGame();
            }else
            {
                if(voteStarted - System.currentTimeMillis() >= 4 * 60 * 1000)
                {
                    isVoting = false;
                    twitter.tweet("Vote failed (" + activeUserHandler.getVotes() + "/" + activeUserHandler.getActiveUsers() + ")");
                    activeUserHandler.resetVotes();
                }
                else {
                    twitter.tweet("Voting to skip current word. (" + activeUserHandler.getVotes() + "/" + activeUserHandler.getActiveUsers() + ") type '@" + twitter.getUserName() + " /vote' to say yes.");
                }
            }
            lastVoteStatus = System.currentTimeMillis();
        }

        //displaying the game status if game is active and not done it for 90 seconds and if there is something to update
        if(isPlaying == true && System.currentTimeMillis() - lastStatusUpdate >= 90 * 1000 && update == true) {
            tweetGameStatus("");
            lastStatusUpdate = System.currentTimeMillis();
        }


        //displaying adds (hehe)
        if(System.currentTimeMillis() - lastStatusUpdate >= 3 * 60 * 60 * 1000) {
            if(isPlaying) {
                tweetGameStatus("The game isn't over yet!");
                lastStatusUpdate = System.currentTimeMillis();
            }
            else {
                twitter.tweet("Play with me! To start a game just type '@" + twitter.getUserName() + " /new'!");
                lastStatusUpdate = System.currentTimeMillis();
            }
        }
    }

}

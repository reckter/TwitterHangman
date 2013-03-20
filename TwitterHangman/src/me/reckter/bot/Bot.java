package me.reckter.bot;

import me.reckter.Twitter.Twitter;
import me.reckter.misc.Console;
import me.reckter.xsql.Db;
import me.reckter.xsql.Item;
import me.reckter.xsql.Itemcollection;
import twitter4j.Status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

    Db db;

    private boolean isPlaying;
    private List<Character> word;
    private List<Character> wrongGuesses;
    private List<Character> guesses;
    private long lastStatusUpdate;
    private long lastMentionCheck;
    private boolean update;

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
        isPlaying = false;
        lastMentionCheck = System.currentTimeMillis();
        word = new ArrayList<Character>();
        wrongGuesses = new ArrayList<Character>();
        guesses = new ArrayList<Character>();
        update = false;
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
       while( i < statuses.size() && statuses.get(i).getId() != lastMentionId)
       {
           i++;
       }

       i--;
       while(i >= 0)
       {
           processMention(statuses.get(i));
           i--;
       }
       lastMentionId = statuses.get(0).getId();
   }

    private void processMention(Status reply){

        //splitting the message into 3 parts: "@BOTNAME" + letters / word + rest that may ocour
        Console.c_log("TwitterListener", "reply", "@" + reply.getUser().getScreenName() + ": " + reply.getText());
        String rawMessage = reply.getText();
        String[] splitMessage = rawMessage.split(" ",3);
        String message = splitMessage[1];

        //handling all the commands
        if(message.startsWith("/")) {
            message = message.substring(1,message.length());

            if(message.startsWith("new")) {
                if(isPlaying == false)
                {
                    startNewGame();
                    Console.c_log("Bot","NewGame", "@" + reply.getUser().getScreenName() + " has started a new game!");
                }
            }
            else if(message.startsWith("score")) {
                Item item = new Item(db).construct("user", "" + reply.getUser().getId());
                Console.c_log("Bot","Score", "Telling @" + reply.getUser().getScreenName() + " his/her score");
                if(item == null)
                {
                    twitter.reply("You have no score yet!", reply);
                }
                twitter.reply("Your score is " + item.get("score"), reply);
            }
            else if(message.startsWith("ping")) {
                twitter.reply("Pong! " + (int) (Math.random() * 100) , reply);
            }
            else if(message.equals("update")) {
                update = true;
            }
        }else {
            //handling letter checking
            if(isPlaying == false)
            {
                return;
            }

            //checking if the letter are right and if, make them visible
            int score = 0;
            char[] letters = message.toUpperCase().toCharArray();
            if(letters.length == 1)
            {

                if(letters[0] != ' ') {

                    //checking if it was asked before
                    if(!guesses.contains(new Character(letters[0]))) {
                        guesses.add(new Character(letters[0]));
                        update = true;
                        //checking the letter...
                        boolean isRight = false;
                        for(Character character:word) {
                            if(character.checkCharacter(letters[0])) {
                                score++;
                                isRight = true;
                            }
                        }
                        if(isRight == false) {
                            wrongGuesses.add(new Character(letters[0]));
                            score--;
                        }

                        //checking Game status
                        boolean isWon = true;
                        for(Character character:word) {
                            if(character.isVisible() == false) {
                                isWon = false;
                                break;
                            }
                        }
                        if(isWon == true) {
                            wonGame(reply);
                            score += 10;
                        }else if(wrongGuesses.size() >= 10) {
                            lostGame();
                        }
                    }
                }
            }else {
                //checking if the asked word is corect
                if(letters.length == word.size()) {
                    boolean isWord = true;
                    for(int i = 0;i < letters.length; i ++) {
                        if(word.get(i).getCharacter() != letters[i]){
                            isWord = false;
                            break;
                        }
                    }
                    if(isWord == true) {
                        for(Character character:word) {
                            character.setVisible(true);
                        }
                    }else {
                        wrongGuesses.add(new Character('\n'));
                    }
                    score +=10;
                }
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
    }


    private void lostGame() {
        //TODO lost Game stuff!
        tweetGameStatus("To many misstakes! You lost the game!*trollface* ");

        isPlaying = false;

    }

    private void wonGame(Status winStatus) {
        //TODO won Game stuff!
       tweetGameStatus(winStatus.getUser().getScreenName() + " solved it!");
        isPlaying = false;

    }


    private void startNewGame(){

        Item wordItem = null;
        while(wordItem == null) {
            Itemcollection wordItems = new Itemcollection(db).construct("words");
            int possibleWords = wordItems.size();
            int wordIndex = (int) (Math.random() * possibleWords);

            for(int i = 0; i <= wordIndex; i++) {
                wordItem = wordItems.next();
            }
            word.clear();
            if(wordItem == null) {
                Console.c_log("bot","startGame","wordItem == null");
                continue;
            }
            for(int i = 0; i < wordItem.get("word").length(); i++) {
                word.add(new Character(wordItem.get("word").toUpperCase().toCharArray()[i]));
            }
        }
        isPlaying = true;
        guesses.clear();
        wrongGuesses.clear();

        tweetGameStatus("Just started a new Game!");
        lastStatusUpdate = System.currentTimeMillis();
    }

    private void tweetGameStatus(String additionalMessage){
        String printWrongGuess = "";
        for(Character wrongGues:wrongGuesses) {
            char c = wrongGues.getCharacter();
            if(c == '\n') {
                continue;
            }
            printWrongGuess += c + ",";
        }
         twitter.tweet(prepareWord() + " " + wrongGuesses.size()+ "/10 misstakes, wrong letters: " + printWrongGuess + " " + additionalMessage + " #TwitterHangman " + (int) ( Math.random() * 100));
        update = false;
    }


    private String prepareWord()
    {
        String wordOutput = "";
        for(Character c: word) {
            if(c.isVisible()) {
                wordOutput += c.getCharacter() + " ";
            }
            else {
                wordOutput += "_ ";
            }
        }
        return wordOutput;
    }


    public void  tick()
    {
        if(System.currentTimeMillis() - lastMentionCheck >= 60 * 1000) {
            checkMentions();
            lastMentionCheck = System.currentTimeMillis();
        }

        if(isPlaying == true && System.currentTimeMillis() - lastStatusUpdate >= 90 * 1000 && update == true) {
            tweetGameStatus("");
            lastStatusUpdate = System.currentTimeMillis();
        }
    }

}

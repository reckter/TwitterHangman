package me.reckter.bot;

import me.reckter.Twitter.Twitter;
import me.reckter.misc.Console;
import me.reckter.xsql.Db;
import me.reckter.xsql.Item;
import me.reckter.xsql.Itemcollection;
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

    private boolean isPlaying;
    private List<Character> word;
    private List<Character> wrongGuesses;
    private List<Character> guesses;

    private List<ActiveUser> activeUsers;
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
        lastMentionId = statuses.get(0).getId();

        isPlaying = false;
        isVoting = false;
        voteStarted = -1;
        lastVoteStatus = -1;
        lastMentionCheck = System.currentTimeMillis();
        lastStatusUpdate = System.currentTimeMillis() - 90 * 1000;
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

        //if the mention comes from the bot ignore it
        if(twitter.getUserName() == reply.getUser().getScreenName()){
            return;
        }

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
                if(item.isValid()) {
                    twitter.reply("Your score is " + item.get("score"), reply);
                }
                else {
                    twitter.reply("You have no score yet!", reply);
                }
            }
            else if(message.startsWith("ping")) {
                twitter.reply("Pong!", reply);
            }
            else if(message.equals("update")) {
                update = true;
            }else if(message.startsWith("next")) {
                if(voteStarted - System.currentTimeMillis() > 10 * 60 * 1000 && isVoting == false)
                {
                    return;
                }
                isVoting = true;
                vote(reply.getUser().getId(),1);
                if(voteStarted == -1) {
                    voteStarted = System.currentTimeMillis();
                }
            }
        }else {

            //set the Player to active
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
                update = true;
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
                        wonGame(reply);
                        score +=10;
                    }else {
                        wrongGuesses.add(new Character('\n'));
                    }
                }else {
                    wrongGuesses.add(new Character('\n'));
                }

                //checkinf if game is lost
                if(wrongGuesses.size() >= 10) {
                    lostGame();
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


    private void addActivePlayer(long id)
    {
        boolean isAdded = false;
        for(ActiveUser user:activeUsers) {
            if(user.getId() == id) {
                isAdded = true;
                user.saw();
                break;
            }
        }
        if(isAdded == false){
            activeUsers.add(new ActiveUser(id));
        }
    }

    private int getVotes() {
        int ret = 0;
        for(ActiveUser user:activeUsers) {
            ret += user.getVote();
        }
        return ret;
    }

    private int getActiveUsers() {
        int ret = 0;
        for(ActiveUser user:activeUsers) {
            if(user.isActive())
                ret++;
        }
        return ret;
    }

    private void vote(long id, int vote)
    {
        addActivePlayer(id);
        for(ActiveUser user:activeUsers) {
            if(user.getId() == id) {
                user.vote(vote);
                break;
            }
        }
    }


    private void resetVotes()
    {
        for(ActiveUser user:activeUsers) {
            user.vote(0);
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
        activeUsers.clear();

        lastStatusUpdate = System.currentTimeMillis();

        isVoting = false;
        voteStarted = -1;

        tweetGameStatus("Just started a new Game!");

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
         twitter.tweet(prepareWord() + " " + wrongGuesses.size()+ "/10 mistakes, wrong letters: " + printWrongGuess + " " + additionalMessage + " #TwitterHangman ");
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
        //checking the Mentions (every 60 seconds)
        if(System.currentTimeMillis() - lastMentionCheck >= 60 * 1000) {
            checkMentions();
            lastMentionCheck = System.currentTimeMillis();
        }

        //handling votes
        if(isVoting == true && lastVoteStatus - System.currentTimeMillis() >= 60 * 1000)
        {
            if(getVotes() * 2 >= getActiveUsers())
            {
                twitter.tweet("Vote was succesful! (" + getVotes() + "/" + getActiveUsers() + ") starting new Game now.");
                isVoting = false;
                startNewGame();
            }else
            {
                if(voteStarted - System.currentTimeMillis() >= 4 * 60 * 1000)
                {
                    isVoting = false;
                    twitter.tweet("Vote failed (" + getVotes() + "/" + getActiveUsers() + ")");
                    resetVotes();
                }
                else {
                    twitter.tweet("Voting to skip current word. (\" + getVotes() + \"/\" + getActiveUsers() + \") type '@" + twitter.getUserName() + " /vote' to say yes.");
                }
            }
            lastVoteStatus = System.currentTimeMillis();
        }

        //displaying the game status if game is active and not done it for 90 seconds and if there is somethign to update
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

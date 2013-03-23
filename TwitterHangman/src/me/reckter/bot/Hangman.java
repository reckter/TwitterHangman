package me.reckter.bot;

import me.reckter.misc.Console;
import me.reckter.xsql.Db;
import me.reckter.xsql.Item;
import me.reckter.xsql.Itemcollection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Gast
 * Date: 22.03.13
 * Time: 16:07
 * To change this template use File | Settings | File Templates.
 */
public class Hangman {

    private Db db;
    private List<Character> word;
    private List<Character> wrongGuesses;
    private List<Character> guesses;

    public Hangman(Db db0) {

        this.db =db;
        word = new ArrayList<Character>();
        wrongGuesses = new ArrayList<Character>();
        guesses = new ArrayList<Character>();
    }

    public int getWrongGuesses() {
        return wrongGuesses.size();
    }

    public char[] getWrongLetters() {
        String ret = "";
        for(Character wrongGuess :wrongGuesses) {
            char c = wrongGuess.getCharacter();
            if(c == '\n') {
                continue;
            }
            ret += c;
        }
        return ret.toUpperCase().toCharArray();
    }

    public int checkLetter(char c){
        //checking the letter...
        if(!guesses.contains(new Character(c))) {
            guesses.add(new Character(c));
            for(Character character:word) {
                if(character.checkCharacter(c)) {
                    return 1;
                }
            }
            wrongGuesses.add(new Character(c));
            return -1;
        }
        return 0;
    }

    public boolean checkWord(char[] askedWord) { // checks a word
        if(askedWord.length == word.size()) {
            for(int i = 0; i < askedWord.length; i ++) {
                if(word.get(i).getCharacter() != askedWord[i]){
                    wrongGuesses.add(new Character('\n'));
                    return false;
                }
            }
            for(Character character:word) {
                character.setVisible(true);
            }
            return true;
        }else {
            wrongGuesses.add(new Character('\n'));
            return false;
        }
    }

    public int checkGameStatus() { // returns -1 if lost; 0 if nothing; 1 if won
        if(wrongGuesses.size() >= 10) {
            return -1;
        }
        boolean isWon = true;
        for(Character character:word) {
            if(character.isVisible() == false) {
                return 0;
            }
        }
        return 1;
    }

    public void startNewGame() {
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
                Console.c_log("bot", "startGame", "wordItem == null");
                continue;
            }
            for(int i = 0; i < wordItem.get("word").length(); i++) {
                word.add(new Character(wordItem.get("word").toUpperCase().toCharArray()[i]));
            }
        }
        guesses.clear();
        wrongGuesses.clear();
    }

    public String prepareWord()
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
}

package me.reckter.bot;

/**
 * Created with IntelliJ IDEA.
 * User: reckter
 * Date: 19.03.13
 * Time: 15:08
 * To change this template use File | Settings | File Templates.
 */
public class Character {

    private boolean isVisible;

    private char character;

    public Character(char achar)
    {
        this.character = achar;
    }

    public boolean isVisible()
    {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public char getCharacter() {
        return character;
    }

    public boolean checkCharacter(char c)
    {
        if(this.character == c)
        {
            this.isVisible = true;
            return true;
        }
        return false;
    }
}

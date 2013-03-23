package me.reckter.bot;

/**
 * Created with IntelliJ IDEA.
 * User: Gast
 * Date: 22.03.13
 * Time: 15:33
 * To change this template use File | Settings | File Templates.
 */
public class ActiveUser {
    long id;
    long lastSeen;
    int vote;

    public ActiveUser(long id) {
        this.id = id;
        this.lastSeen = System.currentTimeMillis();
        this.vote = 0;
    }

    public long getId() {
        return this.id;
    }

    public long getVote() {
        return  this.vote;
    }

    public void saw() {
        this.lastSeen = System.currentTimeMillis();
    }

    public void vote(int vote) {
        this.vote = vote;
    }

    public boolean isActive() {
        return System.currentTimeMillis() - lastSeen > 4 * 60 * 1000;
    }


}

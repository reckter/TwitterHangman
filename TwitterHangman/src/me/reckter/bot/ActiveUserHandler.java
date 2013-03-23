package me.reckter.bot;

import org.w3c.dom.stylesheets.LinkStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Gast
 * Date: 22.03.13
 * Time: 15:44
 * To change this template use File | Settings | File Templates.
 */
public class ActiveUserHandler {
    List<ActiveUser> activeUsers;

    public ActiveUserHandler() {
        activeUsers = new ArrayList<ActiveUser>();
    }

    public int getVotes() {
        int ret = 0;
        for(ActiveUser user:activeUsers) {
            ret += user.getVote();
        }
        return ret;
    }

    public int getActiveUsers() {
        int ret = 0;
        for(ActiveUser user:activeUsers) {
            if(user.isActive())
                ret++;
        }
        return ret;
    }

    public void vote(long id, int vote)
    {
        addActivePlayer(id);
        for(ActiveUser user:activeUsers) {
            if(user.getId() == id) {
                user.vote(vote);
                break;
            }
        }
    }

    public void clear(){
        activeUsers.clear();
    }


    public void resetVotes()
    {
        for(ActiveUser user:activeUsers) {
            user.vote(0);
        }
    }

    public void addActivePlayer(long id)
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
}

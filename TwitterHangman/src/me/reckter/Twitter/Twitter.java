package me.reckter.Twitter;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import me.reckter.misc.Console;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import twitter4j.*;
import twitter4j.api.TimelinesResources;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;


public class Twitter 
{

    private twitter4j.Twitter twitter;

	public Twitter() throws IOException
	{	

		me.reckter.misc.Console.c_log("wt","init","started");
try {
            twitter = new TwitterFactory().getInstance();
            try {
                // get request token.
                // this will throw IllegalStateException if access token is already available
                RequestToken requestToken = twitter.getOAuthRequestToken();
                System.out.println("Got request token.");
                System.out.println("Request token: " + requestToken.getToken());
                System.out.println("Request token secret: " + requestToken.getTokenSecret());
                AccessToken accessToken = null;

                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                while (null == accessToken) {
                    System.out.println("Open the following URL and grant access to your account:");
                    System.out.println(requestToken.getAuthorizationURL());
                    System.out.print("Enter the PIN(if available) and hit enter after you granted access.[PIN]:");
                    String pin = br.readLine();
                    try {
                        if (pin.length() > 0) {
                            accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                        } else {
                            accessToken = twitter.getOAuthAccessToken(requestToken);
                        }
                    } catch (TwitterException te) {
                        if (401 == te.getStatusCode()) {
                            System.out.println("Unable to get the access token.");
                        } else {
                            te.printStackTrace();
                        }
                    }
                }
                System.out.println("Got access token.");
                System.out.println("Access token: " + accessToken.getToken());
                System.out.println("Access token secret: " + accessToken.getTokenSecret());
            } catch (IllegalStateException ie) {
                // access token is already available, or consumer key/secret is not set.
                if (!twitter.getAuthorization().isEnabled()) {
                    System.out.println("OAuth consumer key/secret is not set.");
                    System.exit(-1);
                }
            }
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get timeline: " + te.getMessage());
            System.exit(-1);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Failed to read the system input.");
            System.exit(-1);
        }
    }
	
	public void tweet(String text) //sendet einen tweet
	{
        try {
            Status status = twitter.updateStatus(text);
            Console.c_log("Twitter", "tweet", status.getText());
        }
        catch( TwitterException e)
        {
            Console.c_log("Twitter", "tweet", e.toString());
        }
	}

    public void replie(String text, Status replieTo)
    {
        try {
            Status status = twitter.updateStatus(new StatusUpdate("@" + replieTo.getUser().getScreenName() + text).inReplyToStatusId(replieTo.getId()));
            Console.c_log("Twitter", "replie", status.getText());
        }
        catch( TwitterException e)
        {
            Console.c_log("Twitter", "replie", e.toString());
        }

    }

    public List<Status> getMentions()
    {
        List<Status> statuses = null;
        try {
            statuses = twitter.getMentionsTimeline(new Paging(1));
            Iterator<RateLimitStatus> Iter = twitter.getRateLimitStatus().values().iterator();
            RateLimitStatus r;
            while(Iter.hasNext())
            {
                r = Iter.next();
                if(r.getRemaining() != 15 && r.getRemaining() != 180)
                {
                    System.out.println(r.toString());
                }
            }
            Console.c_log("twitter", "getReplie", "firstSeenID: " + statuses.get(0).getId() + " User: " + statuses.get(0).getUser().getScreenName() + "(" + statuses.get(0).getUser().getId() + ") Message:" + statuses.get(0).getText());
        } catch (TwitterException e) {
            if(e.getStatusCode() == 429)
            {
                Console.c_log("twitter","getReplie - wait", "waiting " + e.getRateLimitStatus().getSecondsUntilReset() + " seconds due to the API limit");
                try {
                    Thread.sleep(e.getRateLimitStatus().getSecondsUntilReset() * 1000);
                } catch(InterruptedException e2)
                {
                    Console.c_log("twitter","getReplie - wait","Error:" + e2.toString());
                }
                Console.c_log("twitter","getReplie - wait","continueing...");
                return null;
            }
            Console.c_log("twitter","getReplie","Error:" + e.toString());
        }
        return statuses;
    }
}

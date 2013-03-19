package me.reckter.Twitter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;


public class Twitter 
{
	
	private OAuthService service;
	private Token accessToken;
	public Twitter() throws IOException
	{	

		me.reckter.misc.Console.c_log("wt","init","started");
		//OAuth init.
		service = new ServiceBuilder().provider(TwitterApi.class).apiKey("PAHPUvcJzVH0suwSze1quQ").apiSecret("Gpno9nEghkBWi14Ym6iOyv5WjfEybd0MJT8a8fHNsM").build();
		
		
		//TODO: token soeichern (wie oO??)
	/*	String zeile1 = null; 
		String zeile2 = null;
		try {
			BufferedReader in = new BufferedReader(new FileReader(textFile + ".txt"));
			if((zeile1 = in.readLine()) != null && (zeile2 = in.readLine()) != null)
			{
				
				return;
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	
    	Scanner in = new Scanner(System.in);
    	

		//get the token
		Token requestToken = service.getRequestToken();
    	
		//get the reqURl
		String authUrl = service.getAuthorizationUrl(requestToken);
		
		
		System.out.println("Rufen sie diese URl auf und geben sie den Code, den sie bekommen danach hier ein: " + authUrl);
		System.out.println("Code:");
	    Verifier verifier = new Verifier(in.nextLine());
		//verifing
		accessToken = service.getAccessToken(requestToken, verifier);
		
	    
	    String secret = accessToken.toString().substring(6, accessToken.toString().indexOf(" "));
	    System.out.println("secret(i hope):" + secret + ";");
	    
	    String token = accessToken.toString().substring(accessToken.toString().indexOf(",")+2, accessToken.toString().indexOf("]"));
	    System.out.println("token(i hope):" + token + ";");
	    
	    //save token (auto generatet catch blogs are a mess >.<)
	    FileOutputStream out = null;
		try {
			out = new FileOutputStream("TW_token");
			out.write(secret.getBytes());
			out.write("\r\n".getBytes());
			out.write(token.getBytes());
			out.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
	}
	
	public void tweet(String text) //sendet einen tweet
	{
		/*
		OAuthRequest request = new OAuthRequest(Verb.POST,  "http://api.twitter.com/1/statuses/update.json");
		request.addBodyParameter("status", text);
		service.signRequest(accessToken, request);
		Response response = request.send();
		*/
		me.reckter.misc.Console.c_log("tw","tweeted","\"" + text + "\"");
		
	}

}

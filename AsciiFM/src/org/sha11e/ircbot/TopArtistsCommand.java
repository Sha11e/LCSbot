package org.sha11e.ircbot;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;

import org.jibble.pircbot.PircBot;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TopArtistsCommand implements Command {
   String uname = null;
    @Override
    public String getCommandName() {
	return "TopArtists";
    }

    @Override
    public void handleMessage(PircBot bot, String channel, String hostname,
		    String message, String commandPrefix,
		    HashMap<String, String> authToUser) {
	 uname = authToUser.get(hostname);
	 bot.sendMessage(channel, getArtistAndSong(uname));
    }

    private String getArtistAndSong(String username) {
   	downloadLastfmJson(username);

   	FileReader fr = null;
   	try {
   	    fr = new FileReader("tmp/asciifmtopartists.json");
   	} catch (FileNotFoundException e) {
   	    System.out.println("fok");
   	    e.printStackTrace();
   	}
   	    
   	JSONObject root = null;
   	JSONParser parser = new JSONParser();
   	try {
   	    root = (JSONObject) parser.parse(fr);
   	} catch (IOException | ParseException e) {
   	    System.out.println("Dooblefok");
   	    e.printStackTrace();
   	}
   	
   	String errorMessage = (String)root.get("message");
   	 if (errorMessage != null) {
   	     System.out.println("heh error");
   	     return errorMessage;
   	 }
   	 //else
   	 StringBuilder finalMessage = new StringBuilder();
   	 finalMessage.append(username + "'s top 3 artists played the last 7 days are: ");
   	    
   	 JSONObject topartists = (JSONObject)root.get("topartists");
   	 JSONArray artist = (JSONArray) topartists.get("artist");
   	 
   	 for (int i = 0; i < 3; i++) {
   	     JSONObject artistx = (JSONObject) artist.get(i);
   	     String songartist = (String) artistx.get("name");
   	     System.out.println(songartist);
   	     String playcount = (String) artistx.get("playcount");
   	     
   	     if (i == 2) { finalMessage.append("and "); }
   	     finalMessage.append(songartist + "(x" + playcount + ")");
   	     if(i != 2) {
   		 finalMessage.append(", ");
   	     }
   	 }

   	 return (finalMessage.toString());
       }
       
       private void downloadLastfmJson(String username) {
   	URL website = null;
   	try {
   	    website = new URL("http://ws.audioscrobbler.com/2.0/?method=user.gettopartists&period=7day&limit=3&user=" + URLEncoder.encode(uname, "UTF-8") + "&api_key=823ad6e31d56bdacbbd9957e59957b50&format=json");
   	} catch (MalformedURLException | UnsupportedEncodingException e) {
   	    e.printStackTrace();
   	}
   	ReadableByteChannel rbc = null;
   	do {
   	    try {
   		 rbc = Channels.newChannel(website.openStream());
   	    } catch(IOException ex) {ex.printStackTrace();}
   	} while (rbc == null);
   	
          FileOutputStream fos = null;
          try {
              fos = new FileOutputStream("tmp/asciifmtopartists.json");
          } catch (FileNotFoundException e) {
              e.printStackTrace();
          }
          
          try {
              fos.getChannel().transferFrom(rbc, 0, 9999999);
          } catch (IOException e) {
              e.printStackTrace();
          }
          
          try {
              fos.close();
              rbc.close();
          } catch (IOException e) {
              e.printStackTrace();
          }
       
       }
}

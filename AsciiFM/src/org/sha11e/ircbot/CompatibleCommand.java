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

public class CompatibleCommand implements Command {
    String uname = null;
    
    @Override
    public String getCommandName() {
	return "Cp";
    }

    @Override
    public void handleMessage(PircBot bot, String channel, String hostname,
		    String message, String commandPrefix, HashMap<String, String> authToUser) {
	uname = authToUser.get(hostname);
	if (message.isEmpty() || message.split(" ").length != 1) {
	    bot.sendMessage(channel, "Please enter a username to check you musical compatibility with: !cp <username>");
	} else {
	    bot.sendMessage(channel, getArtistAndSong(message));
	}
    }
    
    private String getArtistAndSong(String username) {
	downloadLastfmJson(username);

	FileReader fr = null;
	try {
	    fr = new FileReader("tmp/asciifmcp.json");
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
	 JSONObject comparison = (JSONObject) root.get("comparison");
	 JSONObject result = (JSONObject) comparison.get("result");
	 String score = (String) result.get("score");
	 JSONObject artists = (JSONObject) result.get("artists");
	 JSONArray artist = (JSONArray) artists.get("artist");
	 
	 StringBuilder finalMessage = new StringBuilder();
	 finalMessage.append("Your musical compatibility with "  + username + " is " + score);
	 finalMessage.append(" - Music you have in common includes ");
	 for (int i = 0; i < artist.size(); i++) {
	     String artistxxx = (String)((JSONObject)artist.get(i)).get("name");
	     if (i == artist.size()-1) { finalMessage.append("and "); }
	     finalMessage.append(artistxxx);
	     if (i != artist.size()-1) { finalMessage.append(", "); }
	 }
	 finalMessage.append(".");
	    
	 return (finalMessage.toString());
    }
    
    private void downloadLastfmJson(String username) {
	URL website = null;
	try {
	    website = new URL("http://ws.audioscrobbler.com/2.0/?method=tasteometer.compare&type1=user&type2=user&value1=" + URLEncoder.encode(uname, "UTF-8") + "&value2=" + URLEncoder.encode(username, "UTF-8") + "&api_key=823ad6e31d56bdacbbd9957e59957b50&format=json");
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
           fos = new FileOutputStream("tmp/asciifmcp.json");
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

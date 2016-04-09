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

public class LastfmCommand implements Command {
    @Override
    public String getCommandName() {
	return "Lastfm";
    }

    @Override
    public void handleMessage(PircBot bot, String channel, String hostname,
		    String message, String commandPrefix, HashMap<String, String> authToUser) {
	if (message.isEmpty()) {
	    String uname = authToUser.get(hostname);
	    
	    if (hostname.toLowerCase().equals("highgate.irccloud.com")) {
	    	bot.sendMessage(channel, "Since all irccloud hostnames look the same, please auth and set usermode +x or type !lastfm <username>");
	    	return;
	    }
	    
	    if (uname == null) {
	        String usage = "Usage: \"" + commandPrefix + "Lastfm <username>\" or associate your hostname with a username: \"" + commandPrefix + "SetLastfm <username>\" so that you can simply do \"" + commandPrefix + "Lastfm\"";
	        bot.sendMessage(channel, usage);
	    } else {
		bot.sendMessage(channel, getArtistAndSong(uname));
	    }
	} else if (message.split(" ").length == 1) {
	    String x = getArtistAndSong(message);
	    if(x.equalsIgnoreCase("")) { System.out.println("IS EMPTY"); }
	    bot.sendMessage(channel, x);
	}
    }
    
    private String getArtistAndSong(String username) {
	downloadLastfmJson(username);

	FileReader fr = null;
	try {
	    fr = new FileReader("tmp/asciifm.json");
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}
	    
	JSONObject root = null;
	JSONParser parser = new JSONParser();
	try {
	    root = (JSONObject) parser.parse(fr);
	} catch (IOException | ParseException e) {
	    e.printStackTrace();
	}
	
	String errorMessage = (String)root.get("message");
	 if (errorMessage != null) {
	     return errorMessage;
	 }
	 //else
	 JSONObject recentTracks = (JSONObject) root.get("recenttracks");
	 if (recentTracks.get("user") != null) {
	     return (recentTracks.get("user") + " has not scrobbled any tracks yet");
	 }
	 JSONArray track = (JSONArray) recentTracks.get("track");
	 JSONObject mostRecent = (JSONObject) track.get(0);
	 JSONObject artistx = (JSONObject) mostRecent.get("artist");
	    
	 String artist = (String) artistx.get("#text");
	 String songName = (String) mostRecent.get("name");
	    
	 return (artist + " - " + songName);
    }
    
    private void downloadLastfmJson(String username) {
	URL website = null;
	try {
	    website = new URL("http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&limit=3&user=" + URLEncoder.encode(username, "UTF-8") + "&api_key=823ad6e31d56bdacbbd9957e59957b50&format=json");
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
           fos = new FileOutputStream("tmp/asciifm.json");
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

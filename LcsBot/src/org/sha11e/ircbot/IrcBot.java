package org.sha11e.ircbot;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;

public class IrcBot extends PircBot {
    private final BotProperties props = new BotProperties();
    private final String commandPrefix = props.getProperty("irc.command.prefix");
    private final String avoidAuth = props.getProperty("irc.avoid.auth");
    
    public IrcBot() {
    	System.out.println(getTime() + " - Setting up the bot");
    	addCommands();
    	createEssentialFolders();
    	setLogin(props.getProperty("irc.login"));
    	setVersion(props.getProperty("irc.version"));
    	setFinger(props.getProperty("irc.finger"));
    	setName(props.getProperty("irc.nickname"));
    	
    	try {
    	    setEncoding(props.getProperty("irc.encoding"));
    	} catch (UnsupportedEncodingException e) {
    	    System.out.println(getTime() + " - Unsuppnorted encoding error: " + e.getMessage());
    	}
    	
    	setAutoNickChange(true);
    	connectToIrc();
    }
    
    private void addCommands() {
    	//TODO: Add commands
    }

    private void connectToIrc() {
    	System.out.println(getTime() + " - Connecting to IRC");
    	String server = props.getProperty("irc.server");
    	int port = Integer.parseInt(props.getProperty("irc.port"));

    	try {
    		connect(server, port);
    	} catch (NickAlreadyInUseException e) {
    		System.out.println(getTime() + " - Nick already in use. Adding numbers to get a unique nick");
    	} catch (IOException e) {
    		System.out.println(getTime() + " - IOException: " + e.getMessage());
    	} catch (IrcException e) {
    		System.out.println(getTime() + " - IrcException: " + e.getMessage());
    	}
    }

    @Override
    protected void onConnect() {
    	System.out.println(getTime() + " - Authenticating, hiding hostmask and joining the channel");
	
        String authBot = props.getProperty("irc.authbot");
        String username = props.getProperty("irc.username");
        String password = props.getProperty("irc.password");
        String hostmask = props.getProperty("irc.hostmask");
        String channel = props.getProperty("irc.channel");
        String channelPassword = props.getProperty("irc.channel.password");
        
        sendRawLine("PRIVMSG " + authBot + " :AUTH "
	          + username + " " + password);
	
        sendRawLine(hostmask.replaceFirst("(?i)botname", getNick()));
	
        try {
        	Thread.sleep(300);
        } catch (InterruptedException e) {
        	e.printStackTrace();
        }
	
        if (channelPassword.isEmpty()) {
        	joinChannel(channel);
        } else {
        	joinChannel(channel, channelPassword);
        }
	
        System.out.println(getTime() + " - The bot has been set up");
    }
    
    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
    	if (hostname.equalsIgnoreCase(avoidAuth)) {
    		System.out.println(getTime() + " - " + avoidAuth + " joined the channel. Setting channelPost to false.");
    		channelPost = false;
    	}
    }
    
    protected void onPart(String channel, String sender, String login, String hostname) {
    	if (hostname.equalsIgnoreCase(avoidAuth)) {
    		System.out.println(getTime() + " - " + avoidAuth + " parted the channel. Setting channelPost to true.");
    		channelPost = true;
    	}
    }
       
    @Override
    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
    	if (sourceHostname.equalsIgnoreCase(avoidAuth)) {
    		System.out.println(getTime() + " - " + avoidAuth + " quit the server. Setting channelPost to true.");
    		channelPost = true;
   	   	}
	
    	if (sourceNick.equalsIgnoreCase(getNick())) {
    		System.out.println("Thefk onquit got called for me");
    	}
    }
    
    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
    	if (hostname.equalsIgnoreCase(avoidAuth) && channelPost) {
    		channelPost = false;
    		System.out.println(hostname + " spoke in the channel. Setting channelPost to false");
    		return;
    	}
	
    	if (!message.startsWith(commandPrefix)) {
    		return;
    	}
	
    	for (Command cmd : commands) {
    		String cmdName = cmd.getCommandName().toLowerCase();
    		if (message.toLowerCase().startsWith(commandPrefix + cmdName)) {
    			String msg = message.replaceFirst("(?i)" + commandPrefix + cmdName, "").trim();
    			cmd.handleMessage(this, channel, hostname, msg, commandPrefix);
    		}
    	}
	
    }
    
    @Override
    protected void onPrivateMessage(String sender, String login,
		                    String hostname, String message) {
	    System.out.println(getTime() + " - " + sender + " PM'd: " + message);

	    if (!hostname.equalsIgnoreCase(props.getProperty("irc.master"))) {
	        return;
	    }
	
	    if (message.equalsIgnoreCase("!help")) {
	    	sendMessage(sender, "Commands: " + commandPrefix + "SetChannelPost [true|false], rawline:<rawlineMessage>");
	    } else if (message.toLowerCase().startsWith(commandPrefix + "setchannelpost")) {
	    	channelPost = (message.toLowerCase().indexOf("true") != -1);
	    	System.out.println("Setting channelPost to true");
	    } else if (message.toLowerCase().startsWith("rawline:")) {
	    	String command = message.replaceFirst("(?i)RAWLINE:", "");
	    	if(command.toLowerCase().startsWith("quit")) {
	    		sendRawLine(command);
                System.out.println("Master sent us a quit message. Closing bot.");
                System.exit(0);
            }
	        sendRawLine(command);
	    } 
    }
    
    @Override
    protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
	if (recipientNick.equalsIgnoreCase(getNick())) {
	    System.out.println(getTime() + " - The bot was kicked by " + kickerNick + ": " + reason);     
	}
    }
    
    @Override
    protected void onDisconnect() {
	System.out.println("The bot was disconnected. Attempting to reconnect...");
	connectToIrc();
    }
    
    private void createEssentialFolders() {
	File dir = new File("tmp");
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (SecurityException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    
    private static String getTime() {
	Date date = new Date();
	DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	df.setTimeZone(TimeZone.getTimeZone("Europe/Copenhagen"));
	
	return df.format(date);
    }
    
    private boolean channelPost = false;
    private List<Command> commands = new ArrayList<Command>();
}
package org.sha11e.ircbot;

import org.jibble.pircbot.PircBot;

public interface Command {
    public String getCommandName();
    
    public void handleMessage(PircBot bot, String channel, String hostname, String message, String commandPrefix);
}
package org.sha11e.ircbot;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class BotProperties {
    private final String PROPERTIES_PATH = "asciifm.properties";
    
    public BotProperties() {
	loadProperties();
    }
    
    private void loadProperties() {
	InputStream input = null;
	try {
	    input = new FileInputStream(PROPERTIES_PATH);
	    props.load(input);
	} catch (FileNotFoundException ex) {
	    createDefaultPropertiesFile();
	    System.exit(0);
	} catch (IOException ex) {
	    ex.printStackTrace();
	} finally {
	    try {
		input.close();
	    } catch (IOException e) {
	    }
	}
    }
    
    private void createDefaultPropertiesFile() {
	System.out.println("Did not find the properties file. Creating a new one.");
	OutputStream output = null;

        try {
            output = new FileOutputStream(PROPERTIES_PATH);
            String fileTxt = String.format("# The user id you want your bot to use(e.g. ~LOGIN@authname.users.quakenet.org * version) for QuakeNet%n"
        		    + "irc.login=login%n%n"
        		    + "# The version you want your bot to use(e.g. ~login@authname.users.quakenet.org * VERSION) for QuakeNet%n"
        		    + "irc.version=Bot Version 1.0%n%n"
        		    + "# What the bot should reply with when someone Fingers it%n"
        		    + "irc.finger=Hey! Stop that...%n%n"
        		    + "# The nickname you want your bot to use%n"
        		    + "irc.nickname=BotNick%n%n" 
        		    + "# The command prefix your command will have, e.g. !command1 !command2%n"
        		    + "irc.command.prefix=!%n%n"
        		    + "# The encoding the bot should use, some encodings can send and read chinese characters, for example. E.g. UTF-8%n"
        		    + "irc.encoding=UTF-8%n%n"
        		    + "# The IRC server and port(usually 6667) you want to connect to(e.g. irc.quakenet.org)%n"
        		    + "irc.server=irc.quakenet.org%n"
        		    + "irc.port=6667%n%n"
        		    + "# The bot you authenticate with(e.g. \"Q@CServe.quakenet.org\" for QuakeNet or \"authserv\" for GameSurge)%n"
        		    + "irc.authbot=Q@CServe.quakenet.org%n%n"
        		    + "# The command to mask your host(e.g. \"MODE botname +x\" for QuakeNet and GameSurge)%n"
        		    + "# Use 'botname' as a placeholder for your bot's nick. The program will replace%n"
        		    + "# the string with the nick your bot ends up using%n"
        		    + "irc.hostmask=MODE botname +x%n%n"
        		    + "# The channel you want to join, e.g. \"#ChannelName\" and the password to the channel%n"
        		    + "# Leave irc.channel.password empty if the channel has no password%n"
        		    + "irc.channel=#SomeChan%n"
        		    + "irc.channel.password=%n%n"
        		    + "# If this user is in the channel, do not respond to commands. That user has the same command(s)%n"
        		    + "irc.avoid.nick=ubot%n"
        		    + "irc.avoid.auth=ubot.users.quakenet.org%n%n"
        		    + "# The bot's administrator. This person will be able to%n"
        		    + "# send irc commands to the bot(e.g. join or part channels)%n"
        		    + "# Type their authname - currentNick!user@AUTHNAME.users.quakenet.org%n"
        		    + "irc.master=TheOwner.users.quakenet.org%n%n"
        		    + "# The username and password to authenticate the bot with%n"
        		    + "irc.username=username%n"
        		    + "irc.password=password");
            output.write(fileTxt.getBytes());
            output.flush();
        } catch(FileNotFoundException e) {
            System.out.println("Unble to create the properties file. "
              + PROPERTIES_PATH + ": " + e.getMessage());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getProperty(String key) {
	return props.getProperty(key);
    }
    
    private Properties props = new Properties();
}

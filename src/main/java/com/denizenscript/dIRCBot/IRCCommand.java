package com.denizenscript.dIRCBot;

import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.CommandExecutionException;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class IRCCommand extends AbstractCommand implements Holdable {

    // <--[command]
    // @Name IRC
    // @Syntax IRC [message <channel> <message>/notice <channel> <message>/raw <server> <message>/join <channel>/leave <channel>/connect <server>/quit <server>]
    // @Required 2
    // @Short Connects to and interacts with an IRC server.
    // @Plugin dIRCBot
    // @Group external

    // @Description
    // Connects to and interacts with an IRC server.
    //
    // the CONNECT and JOIN options function as ~waitable commands. Everytihng else is considered instant.
    // TODO: Document Command Details

    // @Tags
    // TODO: Make tags

    // @Usage
    // Use to connect to an IRC server.
    // - ~irc connect ircserver@irc.esper.net
    // - irc raw ircserver@irc.esper.net "USER dIRCBot mcmonkey.org mcmonkey.org dIRCBot"
    // - irc raw ircserver@irc.esper.net "NICK dIRCBot"
    // @Usage
    // Use to join a channel within an IRC server.
    // - ~irc join ircchannel@irc.esper.net#denizen-dev
    // @Usage
    // Use to greet the channel you've joined.
    //- irc message ircchannel@irc.esper.net#denizen-dev "Hello everyone!"
    // @Usage
    // Use to leave the channel you've joined.
    //- irc leave ircchannel@irc.esper.net#denizen-dev
    // @Usage
    // Use to log into nickserv. (WARNING: Be very careful with passwords in scripts! Recommended: Read from a YAML file, then immediately unload it.)
    // - irc message ircchannel@irc.esper.net#?nickserv "identify password"

    // -->

    public enum IRCCMD { MESSAGE, NOTICE, RAW, JOIN, LEAVE, QUIT, CONNECT }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Interpret arguments
        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(IRCCMD.values()))
                scriptEntry.addObject("type", arg.asElement());

            else if (!scriptEntry.hasObject("channel")
                    && arg.matchesArgumentType(dIRCChannel.class))
                scriptEntry.addObject("channel", arg.asType(dIRCChannel.class));

            else if (!scriptEntry.hasObject("server")
                    && arg.matchesArgumentType(dIRCServer.class))
                scriptEntry.addObject("server", arg.asType(dIRCServer.class));

            else if (!scriptEntry.hasObject("message"))
                scriptEntry.addObject("message", new ElementTag(arg.raw_value));

            else
                arg.reportUnhandled();
        }

        // Check for required information
        if (!scriptEntry.hasObject("type"))
            throw new InvalidArgumentsException("Must have a type!");

    }

    public static List<IRCServerHolder> IRCServers = new ArrayList<IRCServerHolder>();


    final static String icc = String.valueOf((char)0x03);
    final static String iBold = String.valueOf((char)0x02);
    final static String iNormal = String.valueOf((char)0x0F);
    final static String iUnderline = String.valueOf((char)0x1F);
    final static String bcc = String.valueOf(ChatColor.COLOR_CHAR);

    public static String colorBukkitToIRC(String input) {
        return input.replace(bcc + "0", icc + "01")
                .replace(bcc + "1", icc + "02")
                .replace(bcc + "2", icc + "03")
                .replace(bcc + "3", icc + "12")
                .replace(bcc + "4", icc + "04")
                .replace(bcc + "5", icc + "06")
                .replace(bcc + "6", icc + "07")
                .replace(bcc + "7", icc + "15")
                .replace(bcc + "8", icc + "14")
                .replace(bcc + "9", icc + "02")
                .replace(bcc + "a", icc + "09")
                .replace(bcc + "b", icc + "10")
                .replace(bcc + "c", icc + "04")
                .replace(bcc + "d", icc + "06")
                .replace(bcc + "e", icc + "08")
                .replace(bcc + "f", icc + "15")
                .replace(bcc + "k", iBold)
                .replace(bcc + "l", iBold)
                .replace(bcc + "m", iUnderline)
                .replace(bcc + "n", iUnderline)
                .replace(bcc + "o", iBold)
                .replace(bcc + "r", iNormal)
                .replace(bcc + "A", icc + "09")
                .replace(bcc + "B", icc + "10")
                .replace(bcc + "C", icc + "04")
                .replace(bcc + "D", icc + "06")
                .replace(bcc + "E", icc + "08")
                .replace(bcc + "F", icc + "15")
                .replace(bcc + "K", iBold)
                .replace(bcc + "L", iBold)
                .replace(bcc + "M", iUnderline)
                .replace(bcc + "N", iUnderline)
                .replace(bcc + "O", iBold)
                .replace(bcc + "R", iNormal);
    }


    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        // Fetch required objects
        ElementTag type = scriptEntry.getElement("type");
        ElementTag message = scriptEntry.getElement("message");
        dIRCChannel channel = scriptEntry.getObjectTag("channel");
        dIRCServer server = scriptEntry.getObjectTag("server");

        // Debug the execution
        Debug.report(scriptEntry, getName(), type.debug()
                                          + (channel != null ? channel.debug(): "")
                                          + (server != null ? server.debug(): "")
                                          + (message != null ? message.debug(): ""));

        switch (IRCCMD.valueOf(type.asString().toUpperCase())) {
            case CONNECT: {
                if (server == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a server!");
                    scriptEntry.setFinished(true);
                    return;
                }
                for (IRCServerHolder holder : IRCServers) {
                    if (holder.Server.equalsIgnoreCase(server.Server)) {
                        Debug.echoError("Already connected to this server!");
                        return;
                    }
                }
                IRCServerHolder holder = new IRCServerHolder();
                holder.ConnectCallback = new Runnable() {
                    @Override
                    public void run() {
                        scriptEntry.setFinished(true);
                    }
                };
                holder.Server = server.Server;
                IRCServers.add(holder);
                holder.start();
                break;
            }
            case QUIT: {
                scriptEntry.setFinished(true);
                if (server == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a server!");
                    return;
                }
                for (IRCServerHolder holder : IRCServers) {
                    if (holder.Server.equalsIgnoreCase(server.Server)) {
                        holder.disconnect();
                        IRCServers.remove(holder);
                        Debug.echoDebug(scriptEntry, "Disconnected from server.");
                        return;
                    }
                }
                Debug.echoError(scriptEntry.getResidingQueue(), "Not connected to that server!");
                break;
            }
            case JOIN:
                if (channel == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a channel!");
                    scriptEntry.setFinished(true);
                    return;
                }
                for (IRCServerHolder holder : IRCServers) {
                    if (!holder.Server.equalsIgnoreCase(channel.Server)) {
                        continue;
                    }
                    IRCChannel chan = new IRCChannel(channel.Channel);
                    chan.Callback = new Runnable() {
                        @Override
                        public void run() {
                            scriptEntry.setFinished(true);
                        }
                    };
                    holder.Channels.add(chan);
                    if (holder.Server.equalsIgnoreCase(channel.Server)) {
                        holder.sendRaw("JOIN " + channel.Channel);
                        return;
                    }
                }
                Debug.echoError(scriptEntry.getResidingQueue(), "Not connected to that server!");
                break;
            case LEAVE:
                scriptEntry.setFinished(true);
                if (channel == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a channel!");
                    return;
                }
                for (IRCServerHolder holder : IRCServers) {
                    if (holder.Server.equalsIgnoreCase(channel.Server)) {
                        for (IRCChannel chan: holder.Channels) {
                            if (chan.Name.equalsIgnoreCase(channel.Channel)) {
                                holder.Channels.remove(chan);
                                break;
                            }
                        }
                        holder.sendRaw("PART " + channel.Channel);
                        return;
                    }
                }
                Debug.echoError(scriptEntry.getResidingQueue(), "Not connected to that server!");
                break;
            case MESSAGE:
                scriptEntry.setFinished(true);
                if (channel == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a channel!");
                    return;
                }
                if (message == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a message!");
                    return;
                }
                for (IRCServerHolder holder : IRCServers) {
                    if (holder.Server.equalsIgnoreCase(channel.Server)) {
                        holder.sendRaw("PRIVMSG " + channel.Channel + " :" + colorBukkitToIRC(message.asString()));
                        return;
                    }
                }
                Debug.echoError(scriptEntry.getResidingQueue(), "Not connected to that server!");
                break;
            case NOTICE:
                scriptEntry.setFinished(true);
                if (channel == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a channel!");
                    return;
                }
                if (message == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a message!");
                    return;
                }
                for (IRCServerHolder holder : IRCServers) {
                    if (holder.Server.equalsIgnoreCase(channel.Server)) {
                        holder.sendRaw("NOTICE " + channel.Channel + ":" + colorBukkitToIRC(message.asString()));
                        return;
                    }
                }
                Debug.echoError(scriptEntry.getResidingQueue(), "Not connected to that server!");
                break;
            case RAW:
                scriptEntry.setFinished(true);
                if (server == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a server!");
                    return;
                }
                if (message == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a message!");
                    return;
                }
                for (IRCServerHolder holder : IRCServers) {
                    if (holder.Server.equalsIgnoreCase(server.Server)) {
                        holder.sendRaw(colorBukkitToIRC(message.asString()));
                        return;
                    }
                }
                Debug.echoError(scriptEntry.getResidingQueue(), "Not connected to that server!");
                break;
            default:
                Debug.echoError(scriptEntry.getResidingQueue(), "IRC Command: Unimplemented option?!");
                scriptEntry.setFinished(true);
                break;
        }
    }
}

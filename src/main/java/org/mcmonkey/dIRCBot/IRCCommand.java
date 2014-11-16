package org.mcmonkey.dIRCBot;

import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.scripts.commands.Holdable;

import java.util.ArrayList;
import java.util.List;

public class IRCCommand extends AbstractCommand implements Holdable {

    // <--[command]
    // @Name IRC
    // @Syntax IRC [message <channel> <message>/notice <channel> <message>/raw <server> <message>/join <channel>/leave <channel>/connect <server>/quit <channel>]
    // @Required 2
    // @Stable unstable
    // @Short Connects to and interacts with an IRC server.
    // @Author mcmonkey
    // @Plugin dIRCBot
    // @Group external

    // @Description
    // Connects to and interacts with an IRC server.
    // TODO: Document Command Details

    // @Tags
    // TODO: Document Command Details

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
    // Use to log into nickserv.
    // - irc message ircchannel@irc.esper.net#?nickserv "identify password"
    // @Usage
    // TODO: Document Command Details

    // -->

    public enum IRCCMD { MESSAGE, NOTICE, RAW, JOIN, LEAVE, QUIT, CONNECT }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Interpret arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

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
                scriptEntry.addObject("message", new Element(arg.raw_value));

            else
                arg.reportUnhandled();
        }

        // Check for required information
        if (!scriptEntry.hasObject("type"))
            throw new InvalidArgumentsException("Must have a type!");

    }

    public static List<IRCServerHolder> IRCServers = new ArrayList<IRCServerHolder>();


    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        // Fetch required objects
        Element type = scriptEntry.getElement("type");
        Element message = scriptEntry.getElement("message");
        dIRCChannel channel = scriptEntry.getdObject("channel");
        dIRCServer server = scriptEntry.getdObject("server");

        // Debug the execution
        dB.report(scriptEntry, getName(), type.debug()
                                          + (channel != null ? channel.debug(): "")
                                          + (server != null ? server.debug(): "")
                                          + (message != null ? message.debug(): ""));

        switch (IRCCMD.valueOf(type.asString().toUpperCase())) {
            case CONNECT: {
                if (server == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Must specify a server!");
                    scriptEntry.setFinished(true);
                    return;
                }
                for (IRCServerHolder holder : IRCServers) {
                    if (holder.Server.equalsIgnoreCase(server.Server)) {
                        dB.echoError("Already connected to this server!");
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
                    dB.echoError(scriptEntry.getResidingQueue(), "Must specify a server!");
                    return;
                }
                for (IRCServerHolder holder : IRCServers) {
                    if (holder.Server.equalsIgnoreCase(server.Server)) {
                        holder.disconnect();
                        IRCServers.remove(holder);
                        dB.echoDebug(scriptEntry, "Disconnected from server.");
                        return;
                    }
                }
                dB.echoError(scriptEntry.getResidingQueue(), "Not connected to that server!");
                break;
            }
            case JOIN:
                if (channel == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Must specify a channel!");
                    scriptEntry.setFinished(true);
                    return;
                }
                for (IRCServerHolder holder : IRCServers) {
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
                dB.echoError(scriptEntry.getResidingQueue(), "Not connected to that server!");
                break;
            case LEAVE:
                scriptEntry.setFinished(true);
                if (channel == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Must specify a channel!");
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
                dB.echoError(scriptEntry.getResidingQueue(), "Not connected to that server!");
                break;
            case MESSAGE:
                scriptEntry.setFinished(true);
                if (channel == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Must specify a channel!");
                    return;
                }
                if (message == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Must specify a message!");
                    return;
                }
                for (IRCServerHolder holder : IRCServers) {
                    if (holder.Server.equalsIgnoreCase(channel.Server)) {
                        holder.sendRaw("PRIVMSG " + channel.Channel + " :" + message.asString());
                        return;
                    }
                }
                dB.echoError(scriptEntry.getResidingQueue(), "Not connected to that server!");
                break;
            case NOTICE:
                scriptEntry.setFinished(true);
                if (channel == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Must specify a channel!");
                    return;
                }
                if (message == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Must specify a message!");
                    return;
                }
                for (IRCServerHolder holder : IRCServers) {
                    if (holder.Server.equalsIgnoreCase(channel.Server)) {
                        holder.sendRaw("NOTICE " + channel.Channel + ":" + message.asString());
                        return;
                    }
                }
                dB.echoError(scriptEntry.getResidingQueue(), "Not connected to that server!");
                break;
            case RAW:
                scriptEntry.setFinished(true);
                if (server == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Must specify a server!");
                    return;
                }
                if (message == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Must specify a message!");
                    return;
                }
                for (IRCServerHolder holder : IRCServers) {
                    if (holder.Server.equalsIgnoreCase(server.Server)) {
                        holder.sendRaw(message.asString());
                        return;
                    }
                }
                dB.echoError(scriptEntry.getResidingQueue(), "Not connected to that server!");
                break;
            default:
                dB.echoError(scriptEntry.getResidingQueue(), "IRC Command: Unimplemented option?!");
                scriptEntry.setFinished(true);
                break;
        }
    }
}

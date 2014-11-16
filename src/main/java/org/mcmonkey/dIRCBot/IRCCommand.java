package org.mcmonkey.dIRCBot;

import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;

public class IRCCommand extends AbstractCommand {

    // <--[command]
    // @Name IRC
    // @Syntax IRC [message <channel> <message>/notice <channel> <message>/raw <message>/join <channel>/leave <channel>/connect <server>/quit <channel>]
    // @Required 2
    // @Stable unstable
    // @Short Connects to and interacts with an IRC server.
    // @Author mcmonkey
    // @Group external

    // @Description
    // Connects to and interacts with an IRC server.
    // TODO: Document Command Details

    // @Tags
    // TODO: Document Command Details

    // @Usage
    // Use to connect to an IRC server.
    // - irc connect ircserver@irc.esper.net
    // @Usage
    // Use to join a channel within an IRC server.
    // - irc join ircchannel@irc.esper.net#denizen-dev
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


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

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
            case CONNECT:
                break;
            case QUIT:
                break;
            case JOIN:
                break;
            case LEAVE:
                break;
            case MESSAGE:
                break;
            case NOTICE:
                break;
            case RAW:
                break;
            default:
                dB.echoError("IRC Command: Unimplemented option?!");
                break;
        }
    }
}

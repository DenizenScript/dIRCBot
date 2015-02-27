package org.mcmonkey.dIRCBot;

import net.aufdemrand.denizen.scripts.containers.core.BukkitWorldScriptHelper;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;

import java.io.*;
import java.net.Socket;
import java.rmi.server.ExportException;
import java.util.*;

public class IRCServerHolder extends Thread {

    public String Server;

    public List<IRCChannel> Channels = new ArrayList<IRCChannel>();

    public Runnable ConnectCallback;

    public void disconnect() {
        try {
            sock.close();
        }
        catch (Exception ex) {
            dB.echoError(ex);
        }
    }

    public void sendRaw(String str) {
        out.println(str.replace('\n', ' '));
    }

    public Socket sock;
    public PrintWriter out;
    public BufferedReader in;

    @Override
    public void run() {
        dB.log("Connecting to server " + Server);
        try {
            sock = new Socket(Server, 6667);
            out = new PrintWriter(sock.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            ConnectCallback.run();
            while (true)
            {
                try {
                    final String input = in.readLine();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(dIRCBot.Plugin, new Runnable() {
                        @Override
                        public void run() {
                            // <--[event]
                            // @Events
                            // irc raw message
                            // irc raw message from <irc server>
                            //
                            // @Warning This event may fire very rapidly.
                            //
                            // @Plugin dIRCBot
                            // @Group external
                            //
                            // @Triggers when an IRC server sends a message.
                            // @Context
                            // <context.raw_message> returns the full raw message sent by the IRC server.
                            // <context.server> returns what server sent the raw message.
                            //
                            // -->
                            Map<String, dObject> context = new HashMap<String, dObject>();
                            dIRCServer ircServer = new dIRCServer(Server);
                            context.put("raw_message", new Element(input));
                            context.put("server", ircServer);
                            BukkitWorldScriptHelper.doEvents(Arrays.asList("irc raw message", "irc raw message from " + ircServer.identify()),
                                    null, null, context, true);
                        }
                    }, 1);
                    final String[] commands = input.split(" ");
                    String cmd = commands[0].startsWith(":") ? commands[1] : commands[0];
                    if (cmd.equalsIgnoreCase("ping")) {
                        out.println("PONG " + commands[1] + "\n");
                    }
                    else if (cmd.equalsIgnoreCase("privmsg")) {
                        final String channel = commands[2];
                        final StringBuilder message = new StringBuilder();
                        message.append(commands[3].substring(1)).append(' ');
                        for (int i = 4; i < commands.length; i++) {
                            message.append(commands[i]).append(' ');
                        }
                        Bukkit.getScheduler().scheduleSyncDelayedTask(dIRCBot.Plugin, new Runnable() {
                            @Override
                            public void run() {
                                // <--[event]
                                // @Events
                                // irc message
                                // irc message from <irc channel>
                                //
                                // @Plugin dIRCBot
                                // @Group external
                                //
                                // @Triggers when an IRC server sends a message through a channel.
                                // @Context
                                // <context.message> returns the full message sent by the IRC server.
                                // <context.channel> returns what channel sent the raw message.
                                // <context.speaker> returns the username that spoke;
                                //
                                // -->
                                Map<String, dObject> context = new HashMap<String, dObject>();
                                dIRCChannel ircChannel = new dIRCChannel(Server, channel.startsWith("#") ? channel.substring(1): "?" + channel);
                                context.put("message", new Element(message.substring(0, message.length() - 1)));
                                context.put("channel", ircChannel);
                                context.put("speaker", new Element(commands[0].substring(1, commands[0].indexOf('!'))));
                                BukkitWorldScriptHelper.doEvents(Arrays.asList("irc message", "irc message from " + ircChannel.identify()),
                                        null, null, context, true);
                            }
                        }, 1);
                    }
                    else if (cmd.equalsIgnoreCase("376")) {
                        for (IRCChannel channel: Channels) {
                            sendRaw("JOIN " + channel.Name);
                            channel.Callback.run();
                        }
                    }
                }
                catch (Exception ex) {
                    if (ex instanceof IOException) {
                        throw ex;
                    }
                    dB.echoError("IRC Error");
                    dB.echoError(ex);
                }
            }
        }
        catch (Exception ex)
        {
            dB.echoError("IRC Error");
            dB.echoError(ex);
        }
    }
}

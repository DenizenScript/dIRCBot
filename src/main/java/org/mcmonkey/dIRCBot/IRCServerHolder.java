package org.mcmonkey.dIRCBot;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
                            // <context.raw_message> the full raw message sent by the IRC server.
                            // <context.server> what server sent the raw message.
                            //
                            // -->
                            Map<String, dObject> context = new HashMap<String, dObject>();
                            dIRCServer ircServer = new dIRCServer(Server);
                            context.put("raw_message", new Element(input));
                            context.put("server", ircServer);
                            EventManager.doEvents(Arrays.asList("irc raw message", "irc raw message from " + ircServer.identify()),
                                    null, null, context, true);
                        }
                    }, 1);
                    String[] commands = input.split(" ");
                    String cmd = commands[0].startsWith(":") ? commands[1] : commands[0];
                    if (cmd.equalsIgnoreCase("ping")) {
                        out.println("PONG " + commands[1] + "\n");
                    }
                    else if (cmd.equalsIgnoreCase("privmsg")) {
                        final String channel = commands[2];
                    }
                    else if (cmd.equalsIgnoreCase("376")) {
                        for (IRCChannel channel: Channels) {
                            sendRaw("JOIN " + channel.Name);
                            channel.Callback.run();
                        }
                    }
                }
                catch (Exception ex) {
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

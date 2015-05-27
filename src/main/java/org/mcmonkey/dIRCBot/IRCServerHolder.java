package org.mcmonkey.dIRCBot;

import net.aufdemrand.denizen.scripts.containers.core.BukkitWorldScriptHelper;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.*;
import java.net.Socket;
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

    final static String icc = String.valueOf((char)0x03);
    final static String iBold = String.valueOf((char)0x02);
    final static String iNormal = String.valueOf((char)0x0F);
    final static String iUnderline = String.valueOf((char)0x1F);
    final static String bcc = String.valueOf(ChatColor.COLOR_CHAR);

    public static String colorIRCToBukkit(String input) {
        return input.replace(icc + "00", bcc + "7")
                .replace(icc + "01", bcc + "8")
                .replace(icc + "02", bcc + "9")
                .replace(icc + "03", bcc + "2")
                .replace(icc + "04", bcc + "4")
                .replace(icc + "05", bcc + "4")
                .replace(icc + "06", bcc + "5")
                .replace(icc + "07", bcc + "6")
                .replace(icc + "08", bcc + "e")
                .replace(icc + "09", bcc + "a")
                .replace(icc + "10", bcc + "b")
                .replace(icc + "11", bcc + "3")
                .replace(icc + "12", bcc + "3")
                .replace(icc + "13", bcc + "5")
                .replace(icc + "14", bcc + "8")
                .replace(icc + "15", bcc + "7")
                .replace(iBold, bcc + "l")
                .replace(iUnderline, bcc + "n")
                .replace(iNormal, bcc + "r");
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
            while (true) {
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
                            try {
                                Map<String, dObject> context = new HashMap<String, dObject>();
                                dIRCServer ircServer = new dIRCServer(Server);
                                context.put("raw_message", new Element(colorIRCToBukkit(input)));
                                context.put("server", ircServer);
                                BukkitWorldScriptHelper.doEvents(Arrays.asList("irc raw message", "irc raw message from " + ircServer.identify()),
                                        null, null, context, true);
                            }
                            catch (Exception ex) {
                                dB.echoError(ex);
                            }
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
                                try {
                                    Map<String, dObject> context = new HashMap<String, dObject>();
                                    String speaker = commands[0].substring(1, commands[0].indexOf('!'));
                                    dIRCChannel ircChannel = new dIRCChannel(Server, channel.startsWith("#") ? channel.substring(1) : "?" + speaker);
                                    context.put("message", new Element(colorIRCToBukkit(message.substring(0, message.length() - 1))));
                                    context.put("channel", ircChannel);
                                    context.put("speaker", new Element(speaker));
                                    BukkitWorldScriptHelper.doEvents(Arrays.asList("irc message", "irc message from " + ircChannel.identify()),
                                            null, null, context, true);
                                }
                                catch (Exception ex) {
                                    dB.echoError(ex);
                                }
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
                    System.out.println("IRC Error");
                    ex.printStackTrace();
                }
            }
        }
        catch (Exception ex) {
            System.out.println("IRC Error");
            ex.printStackTrace();
        }
    }
}

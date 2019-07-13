package com.denizenscript.dIRCBot;

import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.utilities.debugging.dB;
import org.bukkit.plugin.java.JavaPlugin;

public class dIRCBot extends JavaPlugin {

    public static dIRCBot Plugin;

    @Override
    public void onEnable() {
        dB.log("dIRCBot loaded!");
        DenizenCore.getCommandRegistry()
                .registerCoreMember(IRCCommand.class, "IRC", "IRC [message <channel> <message>" +
                        "/notice <channel> <message>/raw <server> <message>/join <channel>/leave <channel>/" +
                        "connect <server>/quit <server>]", 2);
        ObjectFetcher.registerWithObjectFetcher(dIRCChannel.class);
        ObjectFetcher.registerWithObjectFetcher(dIRCServer.class);
        Plugin = this;
    }
}

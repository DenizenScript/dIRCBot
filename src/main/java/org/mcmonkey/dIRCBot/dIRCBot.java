package org.mcmonkey.dIRCBot;

import net.aufdemrand.denizencore.DenizenCore;
import net.aufdemrand.denizencore.objects.ObjectFetcher;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.plugin.java.JavaPlugin;

public class dIRCBot extends JavaPlugin {

    public static dIRCBot Plugin;

    @Override
    public void onEnable() {
        dB.log("dIRCBot loaded!");
        DenizenCore.getCommandRegistry()
                .registerCoreMember(IRCCommand.class, "IRC", "IRC [message <channel> <message>" +
                        "/notice <channel> <message>/raw <server> <message>/join <channel>/leave <channel>/" +
                        "connect <server>/quit <channel>]", 2);
        ObjectFetcher.registerWithObjectFetcher(dIRCChannel.class);
        ObjectFetcher.registerWithObjectFetcher(dIRCServer.class);
        Plugin = this;
    }
}

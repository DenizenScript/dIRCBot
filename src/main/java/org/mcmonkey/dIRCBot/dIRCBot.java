package org.mcmonkey.dIRCBot;

import net.aufdemrand.denizen.objects.ObjectFetcher;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.plugin.java.JavaPlugin;

public class dIRCBot extends JavaPlugin {

    @Override
    public void onEnable() {
        dB.log("dIRCBot loaded!");
        DenizenAPI.getCurrentInstance().getCommandRegistry()
                .registerCoreMember(IRCCommand.class, "IRC", "IRC [message <channel> <message>" +
                "/notice <channel> <message>/raw <message>/join <channel>/leave <channel>/" +
                "connect <server>/quit <channel>]", 2);
        ObjectFetcher.registerWithObjectFetcher(dIRCChannel.class);
        ObjectFetcher.registerWithObjectFetcher(dIRCServer.class);
    }
}

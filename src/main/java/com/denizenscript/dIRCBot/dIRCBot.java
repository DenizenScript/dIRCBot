package com.denizenscript.dIRCBot;

import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.plugin.java.JavaPlugin;

public class dIRCBot extends JavaPlugin {

    public static dIRCBot Plugin;

    @Override
    public void onEnable() {
        Debug.log("dIRCBot loaded!");
        DenizenCore.getCommandRegistry().registerCommand(IRCCommand.class);
        ObjectFetcher.registerWithObjectFetcher(dIRCChannel.class);
        ObjectFetcher.registerWithObjectFetcher(dIRCServer.class);
        Plugin = this;
    }
}

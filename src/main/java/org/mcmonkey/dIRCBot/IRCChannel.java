package org.mcmonkey.dIRCBot;

import java.util.ArrayList;
import java.util.List;

public class IRCChannel {

    public IRCChannel(String _name) {
        this.Name = _name;
    }

    public String Name;
    public List<IRCUser> Users = new ArrayList<IRCUser>();

    public Runnable Callback;
}

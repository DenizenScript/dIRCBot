package org.mcmonkey.dIRCBot;

import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Fetchable;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import java.util.List;

public class dIRCChannel implements dObject {

    /////////////////////////////////
    // dObject Construction
    /////////////////////////////////

    @Fetchable("ircchannel")
    public static dIRCChannel valueOf(String string, TagContext context) {
        List<String> data = CoreUtilities.split(string.substring("ircchannel@".length()), '#');
        return new dIRCChannel(data.get(0), data.get(1));
    }

    public static boolean matches(String arg) {
        return CoreUtilities.toLowerCase(arg).startsWith("ircchannel@");
    }

    /////////////////////////////////
    // Information
    /////////////////////////////////

    public dIRCChannel(String _server, String _channel) {
        Server = _server;
        if (_channel.startsWith("?"))
            Channel = _channel.substring(1);
        else
            Channel = "#" + _channel;
    }

    public String Server;

    public String Channel;

    /////////////////////////////////
    // Required Nonsense
    /////////////////////////////////
    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String identify() {
        if (Channel.startsWith("#"))
            return "ircchannel@" + Server + Channel;
        else
            return "ircchannel@" + Server + "#?" + Channel;
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public String getObjectType() {
        return "ircchannel";
    }

    String prefix = "ircchannel";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return (prefix + "='<A>" + identify() + "<G>'  ");
    }

    @Override
    public dObject setPrefix(String prefix) {
        if (prefix != null)
            this.prefix = prefix;
        return this;
    }

    /////////////////////////////////
    // Tags
    /////////////////////////////////
    @Override
    public String getAttribute(Attribute attribute) {
        // TODO
        return new Element(identify()).getAttribute(attribute);
    }
}

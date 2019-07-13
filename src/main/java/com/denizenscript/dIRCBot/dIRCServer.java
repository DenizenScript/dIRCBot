package com.denizenscript.dIRCBot;

import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class dIRCServer implements dObject {

    /////////////////////////////////
    // dObject Construction
    /////////////////////////////////

    @Fetchable("ircserver")
    public static dIRCServer valueOf(String string, TagContext context) {
        return new dIRCServer(string.substring("ircserver@".length()));
    }

    public static boolean matches(String arg) {
        return CoreUtilities.toLowerCase(arg).startsWith("ircserver@");
    }

    /////////////////////////////////
    // Information
    /////////////////////////////////

    public dIRCServer(String _server) {
        this.Server = _server;
    }

    public String Server;

    /////////////////////////////////
    // Required Nonsense
    /////////////////////////////////
    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String identify() {
        return "ircserver@" + Server;
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
        return "ircserver";
    }

    String prefix = "ircserver";

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

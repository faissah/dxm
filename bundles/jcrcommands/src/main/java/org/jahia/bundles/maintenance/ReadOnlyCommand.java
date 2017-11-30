package org.jahia.bundles.maintenance;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.jahia.bin.Jahia;

@Command(scope = "dx", name = "read-only")
@Service
public class ReadOnlyCommand implements Action {

    @Argument(description = "on/off")
    @Completion(value=StringsCompleter.class , values = { "ON","OFF" })
    private String enable;

    @Override
    public Object execute() throws Exception {
        if (enable != null) {
            if (!enable.equalsIgnoreCase("on") && !enable.equalsIgnoreCase("off")) {
                System.out.println("Please choose ON or OFF");
            } else {
                Jahia.getSettings().setReadOnlyMode(enable.equalsIgnoreCase("on"));
            }
        }
        System.out.println("Current status : " + (Jahia.getSettings().isReadOnlyMode() ? "ON" : "OFF"));
        return null;
    }
}

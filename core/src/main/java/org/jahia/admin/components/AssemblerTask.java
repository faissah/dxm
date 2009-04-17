package org.jahia.admin.components;

import org.apache.pluto.util.assemble.AssemblerConfig;
import org.apache.pluto.util.assemble.war.WarAssembler;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 15 avr. 2009
 * Time: 12:31:07
 */
public class AssemblerTask {
    private File webapp;
    private File tempDir;

    public AssemblerTask(File webapp) {
        this.webapp = webapp;
    }

    public AssemblerTask(File tempDir, File webapp) {
        this.tempDir = tempDir;
        this.webapp = webapp;
    }

    public File getWebapp() {
        return webapp;
    }

    public void setWebapp(File webapp) {
        this.webapp = webapp;
    }

    public File getTempDir() {
        return tempDir;
    }

    public void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }

    public File execute() throws Exception {
        validateArgs();


        final File tempDir = getTempDir();
        final AssemblerConfig config = new AssemblerConfig();
        config.setSource(getWebapp());
        config.setDestination(tempDir);

        WarAssembler assembler = new WarAssembler();
        assembler.assemble(config);

        return new File(tempDir, getWebapp().getName());


    }

    private void validateArgs() throws Exception {
        if (webapp != null) {
            if (!webapp.exists()) {
                throw new Exception("webapp " + webapp.getAbsolutePath() + " does not exist");
            }
            return;
        }
    }

}



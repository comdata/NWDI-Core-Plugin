/**
 *
 */
package org.arachna.netweaver.hudson.nwdi;

import hudson.FilePath;
import hudson.Util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.arachna.netweaver.dc.types.DevelopmentConfiguration;
import org.arachna.netweaver.hudson.util.FilePathHelper;

/**
 * Create or update the dtr configuration files.
 * 
 * @author Dirk Weigenand
 */
final class DtrConfigCreator {
    /**
     * constant for 'clients.xml' configuration file.
     */
    static final String CLIENTS_XML = "clients.xml";

    /**
     * default encoding to use.
     */
    static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * constant for 'servers.xml' configuration file.
     */
    static final String SERVERS_XML = "servers.xml";

    /**
     * {@link DevelopmentConfiguration} to use for creating the dtr config files.
     */
    private final DevelopmentConfiguration config;

    /**
     * workspace to create dtr configuration in.
     */
    private final FilePath workspace;

    /**
     * Folder where DTR configuration files are to be created/updated.
     */
    private FilePath dtrDirectory;

    /**
     * Folder where the development configurations and its respective development components live.
     */
    private FilePath dtcDirectory;

    /**
     * Create an instance of {@link DtrConfigCreator}.
     * 
     * @param workspace
     *            the workspace where the configuration folders and files should be created/updated.
     * @param config
     *            the {@link DevelopmentConfiguration} that shall be used to create/update the configuration files.
     */
    DtrConfigCreator(final FilePath workspace, final DevelopmentConfiguration config) {
        this.workspace = workspace;
        this.config = config;
    }

    /**
     * Creates/Updates the DTR and development configuration configuration files.
     * 
     * @return the {@link FilePath} created for the DTR configuration directory.
     * @throws IOException
     *             when an error occurred creating the directories and configuration files.
     * @throws InterruptedException
     *             when the user canceled the operation.
     */
    FilePath execute() throws IOException, InterruptedException {
        dtrDirectory = createFolder(NWDIConfigFolder.DTR.getName());
        dtcDirectory = createFolder(NWDIConfigFolder.DTC.getName());

        createOrUpdateServersXml();
        createOrUpdateClientsXml();
        createOrUpdateTrackNameDotSystem();

        return dtrDirectory;
    }

    /**
     * Creates the DTR client configuration file.
     * 
     * @throws IOException
     *             when an error occurred creating the configuration file.
     * @throws InterruptedException
     *             when the user canceled the operation.
     */
    private void createOrUpdateTrackNameDotSystem() throws IOException, InterruptedException {
        final String system = config.getName() + ".system";
        final FilePath child = dtrDirectory.child(system);

        if (child.exists()) {
            child.delete();
        }

        child.write(String.format(getTemplate("template.system"), config.getName(), config.getDtrServerUrl(), config.getBuildServer()),
            DEFAULT_ENCODING);

    }

    /**
     * Creates the given folder in the workspace.
     * 
     * @param folderName
     *            the folder to be created.
     * @return the {@link FilePath} created.
     * @throws IOException
     *             when an error occurred creating the given folder in the workspace.
     * @throws InterruptedException
     *             when the user canceled the operation.
     */
    private FilePath createFolder(final String folderName) throws IOException, InterruptedException {
        final FilePath folder = workspace.child(folderName);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        return folder;
    }

    /**
     * Creates/Updates the 'clients.xml' in the given DTR folder.
     * 
     * @throws IOException
     *             when an error occurred creating the configuration file in the given folder.
     * @throws InterruptedException
     *             when the user canceled the operation.
     */
    private void createOrUpdateClientsXml() throws IOException, InterruptedException {
        final String path = FilePathHelper.makeAbsolute(dtcDirectory);
        final String content = String.format(getTemplate(CLIENTS_XML), config.getName(), path, config.getName());
        dtrDirectory.child(CLIENTS_XML).write(content, DEFAULT_ENCODING);
    }

    /**
     * Creates/Updates the 'servers.xml' in the given DTR configuration folder.
     * 
     * @throws IOException
     *             when an error occurred creating the configuration file in the given folder.
     * @throws InterruptedException
     *             when the user canceled the operation.
     */
    private void createOrUpdateServersXml() throws IOException, InterruptedException {
        dtrDirectory.child(SERVERS_XML).write(String.format(getTemplate(SERVERS_XML), config.getDtrServerUrl(), config.getBuildServer()),
            DEFAULT_ENCODING);
    }

    /**
     * Get the content of the given template.
     * 
     * @param templateName
     *            name of the template to load from class path.
     * 
     * @return content of the given template.
     * @throws IOException
     *             when reading the template fails.
     */
    private String getTemplate(final String templateName) throws IOException {
        final StringWriter content = new StringWriter();
        Util.copyStreamAndClose(new InputStreamReader(this.getClass().getResourceAsStream(templateName), DEFAULT_ENCODING), content);

        return content.toString();
    }
}

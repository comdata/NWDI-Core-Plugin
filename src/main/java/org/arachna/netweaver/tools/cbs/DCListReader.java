/**
 * 
 */
package org.arachna.netweaver.tools.cbs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.arachna.netweaver.dc.types.Compartment;
import org.arachna.netweaver.dc.types.DevelopmentComponentFactory;
import org.arachna.netweaver.dc.types.DevelopmentConfiguration;

/**
 * Parse output of a 'listdcs' command from a SAP NW CE CBS tool.
 * 
 * @author Dirk Weigenand
 */
class DCListReader extends AbstractDCListReader {
    /**
     * regular expression for parsing a line listing a DC and it's enclosing
     * compartment.
     */
    private final Pattern regexp = Pattern.compile("^\\d+\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+.*?$");

    /**
     * Create a new instance of a DCListReader using the given development
     * configuration and development component registry.
     * 
     * @param config
     *            development configuration to add read compartments to.
     * @param dcFactory
     *            registry for read development components.
     */
    DCListReader(final DevelopmentConfiguration config, final DevelopmentComponentFactory dcFactory) {
        super(config, dcFactory);
    }

    /**
     * Read the output of the CBS tool 'listdcs' command and parse the
     * compartments and development components and add them to the development
     * configuration/development component factory.
     * 
     * @param reader
     *            output of the CBS tool 'listdcs' command.
     */
    @Override
    void execute(final Reader reader) {
        final BufferedReader buffer = new BufferedReader(reader);
        String line;

        try {
            while ((line = buffer.readLine()) != null) {
                final Matcher matcher = regexp.matcher(line);

                if (matcher.matches()) {
                    final Compartment compartment = config.getCompartment(matcher.group(1));
                    compartment.add(dcFactory.create(matcher.group(3), matcher.group(2)));
                }
            }
        }
        catch (final IOException e) {
            throw new IllegalStateException(e);

        }
    }
}
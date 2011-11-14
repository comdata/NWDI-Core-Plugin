/**
 *
 */
package org.arachna.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.arachna.netweaver.dc.types.DevelopmentComponent;
import org.arachna.netweaver.dc.types.DevelopmentComponentFactory;
import org.arachna.netweaver.dc.types.PublicPartReference;

/**
 * Helper for building ant projects/tasks.
 * 
 * @author Dirk Weigenand
 */
public class AntHelper {
    /**
     * template for computing the absolute base path of a development component.
     */
    private static final String BASE_PATH_TEMPLATE = "%s/.dtc/DCs/%s/%s/_comp";

    /**
     * template for computing the base path of a development component relative
     * to the workspace.
     */
    private static final String RELATIVE_BASE_PATH_TEMPLATE = ".dtc/DCs/%s/%s/_comp";

    /**
     * registry for development components.
     */
    private final DevelopmentComponentFactory dcFactory;

    /**
     * workspace where build takes place.
     */
    private final transient String pathToWorkspace;

    /**
     * Create an instance of the ant helper.
     * 
     * @param pathToWorkspace
     *            path to workspace the build is taking place on
     * @param dcFactory
     *            registry for development components
     * @param excludesFactory
     *            factory for generating file set excludes from development
     *            component type.
     */
    public AntHelper(final String pathToWorkspace, final DevelopmentComponentFactory dcFactory) {
        this.pathToWorkspace = pathToWorkspace;
        this.dcFactory = dcFactory;
    }

    /**
     * Iterate over the DCs source folders and return those that actually exist
     * in the file system.
     * 
     * @param component
     *            development component to get the existing source folders for.
     * @param filter
     *            determines which source folders should be included in the
     *            resulting collection of source folders
     * @return source folders that exist in the DCs directory structure.
     */
    private Collection<File> getExistingSourceFolders(final DevelopmentComponent component,
        final SourceDirectoryFilter filter) {
        final Collection<File> sourceFolders = new ArrayList<File>();

        for (final String sourceFolder : component.getSourceFolders()) {
            final File folder = new File(sourceFolder);

            if (folder.exists() && filter.accept(folder.getAbsolutePath())) {
                sourceFolders.add(folder);
            }
        }

        return sourceFolders;
    }

    /**
     * Create a set of paths using the public part references of the given
     * development component.
     * 
     * @param component
     *            development component the ant class path shall be created for.
     * 
     * @return set of paths built from the public part references of the given
     *         development component
     */
    public Set<String> createClassPath(final DevelopmentComponent component) {
        Set<String> paths = new HashSet<String>();

        for (final PublicPartReference ppRef : component.getUsedDevelopmentComponents()) {
            DevelopmentComponent referencedDC = dcFactory.get(ppRef.getVendor(), ppRef.getComponentName());

            if (referencedDC != null) {
                final File baseDir = new File(this.getBaseLocation(referencedDC, ppRef.getName()));

                if (!baseDir.exists()) {
                    continue;
                }

                paths.add(baseDir.getAbsolutePath());
            }
        }

        return paths;
    }

    /**
     * @param referencedDC
     * @param name
     * @return
     */
    public String getBaseLocation(final DevelopmentComponent referencedDC, final String name) {
        return getPublicPartLocation(this.getBaseLocation(referencedDC), name);
    }

    /**
     * @param name
     * @param baseLocation
     * @return
     */
    private String getPublicPartLocation(final String baseLocation, final String name) {
        return name != null ? String.format("%s/gen/default/public/%s/lib/java", baseLocation, name) : String.format(
            "%s/gen/default/public/lib/java", baseLocation);
    }

    /**
     * Returns the absolute path to workspace.
     * 
     * @return absolute path to workspace.
     */
    public String getPathToWorkspace() {
        return pathToWorkspace;
    }

    /**
     * Get the location of a development component in workspace.
     * 
     * @param component
     * @return
     */
    public String getBaseLocation(final DevelopmentComponent component) {
        return String.format(BASE_PATH_TEMPLATE, pathToWorkspace, component.getVendor(), component.getName());
    }

    /**
     * Get the location of the given development component relative to the
     * current workspace.
     * 
     * @param component
     *            development component to get the location relative to the
     *            current workspace.
     * @return location of given DC relative to the current workspace.
     */
    public String getBaseLocationRelativeToWorkspace(final DevelopmentComponent component) {
        return String.format(RELATIVE_BASE_PATH_TEMPLATE, component.getVendor(), component.getName());
    }

    /**
     * Get the location of a development components public part in workspace.
     * 
     * @param ppRef
     *            reference to public part
     * @return the location of the files comprising the given public part
     *         reference
     */
    public String getLocation(final PublicPartReference ppRef) {
        return getPublicPartLocation(this.getBaseLocation(ppRef.getVendor(), ppRef.getComponentName()), ppRef.getName());
    }

    /**
     * Calculate the base path using the given vendor and component name.
     * 
     * @param vendor
     *            the vendor of the given component
     * @param name
     *            the name of the component
     * @return base path using the given vendor and component name.
     */
    private String getBaseLocation(final String vendor, final String name) {
        return String.format(BASE_PATH_TEMPLATE, pathToWorkspace, vendor, name);
    }

    /**
     * Creates a collection of source file sets representing the source folders
     * and the sources to in-/exclude.
     * 
     * @param component
     *            development component to create source file sets for.
     * @param excludes
     *            set of standard ant exclude expressions for file sets.
     * @param containsRegexpExcludes
     *            set of regular expressions that determine the files that
     *            should be excluded from the source file sets based on their
     *            content matching one of the given expressions
     */
    public Collection<String> createSourceFileSets(final DevelopmentComponent component,
        final Collection<String> excludes, final Collection<String> containsRegexpExcludes) {
        /**
         * An accept all source directories filter.
         */
        final SourceDirectoryFilter filter = new SourceDirectoryFilter() {
            /**
             * accept all folderNames given.
             * 
             * {@inheritDoc}
             */
            public boolean accept(final String folderName) {
                return true;
            }
        };

        return createSourceFileSets(component, filter);
    }

    /**
     * Creates a collection of source folders.
     * 
     * @param component
     *            development component to create source file sets for.
     * @param filter
     *            a filter that determines which of the source folders should be
     *            added to the result list
     */
    public Collection<String> createSourceFileSets(final DevelopmentComponent component,
        final SourceDirectoryFilter filter) {
        final Collection<String> sources = new HashSet<String>();

        for (final File srcFolder : getExistingSourceFolders(component, filter)) {
            sources.add(srcFolder.getAbsolutePath());
        }

        return sources;
    }
}

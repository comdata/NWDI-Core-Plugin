package org.arachna.ant;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.arachna.netweaver.dc.types.DevelopmentComponent;
import org.arachna.netweaver.dc.types.DevelopmentComponentType;

/**
 * Factory for file set exclude expressions based on development component type.
 * 
 * @author Dirk Weigenand
 */
public class ExcludesFactory {
    /**
     * Exclude regexp for generated web service model classes.
     */
    private static final String WEB_SERVICE_MODEL_CLASS_EXCLUDE = ".*?Web[sS]ervice Model(Class)? implementation.*?";

    /**
     * Exclude regexp for sources generated from a WSDL or Schema definition.
     */
    private static final String GENERATED_WSDL_SCHEMA_EXCLUDE =
        ".*?generated by SAP (WSDL|Schema) to Java generator.*?";

    /**
     * Exclude regexp for sources generated from RFC adapter model.
     */
    private static final String RFC_ADAPTER_MODEL_EXCLUDE = ".*?RFC Adapter Model.*?";

    /**
     * Exclude regexp for sources generated by the WSDL2JAVA generator.
     */
    private static final String GENERATED_WSDL2JAVA_EXCLUDE =
        ".*?extends com.sap.engine.services.webservices.jaxrpc.wsdl2java.(BaseGeneratedStub|ServiceBase).*?";

    /**
     * Mapping from {@link DevelopmentComponentType} to a default set of
     * excludes.
     */
    private final Map<DevelopmentComponentType, Collection<String>> excludesMapping =
        new HashMap<DevelopmentComponentType, Collection<String>>();

    /**
     * Mapping from {@link DevelopmentComponentType} to a default set of
     * excludes via a contains regex.
     */
    private final Map<DevelopmentComponentType, Collection<String>> containsRegexpExcludesMapping =
        new HashMap<DevelopmentComponentType, Collection<String>>();

    /**
     * Create an instance of {@link ExcludesFactory}. Initializes the mapping
     * from development component types to standard ant file set excludes.
     */
    public ExcludesFactory() {
        excludesMapping.put(
            DevelopmentComponentType.WebDynpro,
            Arrays.asList(new String[] { "**/wdp/*.java", "**/*Interface.java", "**/*InterfaceCfg.java",
                "**/*InterfaceView.java" }));
        containsRegexpExcludesMapping.put(
            DevelopmentComponentType.WebDynpro,
            Arrays.asList(new String[] { GENERATED_WSDL2JAVA_EXCLUDE, RFC_ADAPTER_MODEL_EXCLUDE,
                GENERATED_WSDL_SCHEMA_EXCLUDE, WEB_SERVICE_MODEL_CLASS_EXCLUDE,
                ".*?This file has been generated by the Web Dynpro Code Generator.*?",
                ".*?File created by Web Dynpro code generator.*?" }));

        final List<String> containsRegexes =
            Arrays.asList(new String[] { GENERATED_WSDL2JAVA_EXCLUDE, RFC_ADAPTER_MODEL_EXCLUDE,
                GENERATED_WSDL_SCHEMA_EXCLUDE, WEB_SERVICE_MODEL_CLASS_EXCLUDE });

        containsRegexpExcludesMapping.put(DevelopmentComponentType.J2EEWebModule, containsRegexes);
        containsRegexpExcludesMapping.put(DevelopmentComponentType.Java, containsRegexes);
        containsRegexpExcludesMapping.put(DevelopmentComponentType.PortalApplicationModule, containsRegexes);
        containsRegexpExcludesMapping.put(DevelopmentComponentType.PortalApplicationStandalone, containsRegexes);
        containsRegexpExcludesMapping.put(DevelopmentComponentType.WebServicesDeployableProxy, containsRegexes);
    }

    /**
     * Returns the excludes that are default for the type of the given
     * development component joined with the given collection of configured
     * excludes.
     * 
     * @param component
     *            development component to determine the set of excludes for.
     * @param configuredExcludes
     *            configured excludes
     * @return collection of excludes determined by development component type
     *         and configured excludes.
     */
    public String[] create(final DevelopmentComponent component, final Collection<String> configuredExcludes) {
        final Collection<String> defaultExcludes = excludesMapping.get(component.getType());

        return mergeExcludes(configuredExcludes, defaultExcludes);
    }

    /**
     * Returns the contains regexp excludes that are default for the type of the
     * given development component joined with the given collection of
     * configured excludes.
     * 
     * @param component
     *            development component to determine the set of excludes for.
     * @param configuredExcludes
     *            configured excludes
     * @return collection of excludes determined by development component type
     *         and configured excludes.
     */
    public String[] createContainsRegexpExcludes(final DevelopmentComponent component,
        final Collection<String> configuredExcludes) {
        final Collection<String> defaultExcludes = containsRegexpExcludesMapping.get(component.getType());

        return mergeExcludes(configuredExcludes, defaultExcludes);
    }

    /**
     * Merge the given default excludes with the given configured excludes.
     * 
     * @param configuredExcludes
     *            configured excludes per caller (plugin)
     * @param defaultExcludes
     *            default excludes (from this <code>ExcludesFactory</code>).
     * @return the union of the given configured and default excludes.
     */
    private String[] mergeExcludes(final Collection<String> configuredExcludes, final Collection<String> defaultExcludes) {
        final Set<String> excludes = new HashSet<String>();

        if (defaultExcludes != null) {
            excludes.addAll(defaultExcludes);
        }

        if (configuredExcludes != null) {
            excludes.addAll(configuredExcludes);
        }

        return excludes.toArray(new String[excludes.size()]);
    }
}

/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.nanocontainer.script;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * ScriptBuilderResolver handles the task of resolving a file name to a builder
 * name.  Typical default resolution is for Groovy, BeanShell, JavaScript,
 * Jython, and XML script names.  However, you can register/replace your
 * own builder implementations by using the registerBuilder() function.
 * @author Michael Rimov
 */
public class ScriptBuilderResolver {

    public static final String GROOVY = ".groovy";
    public static final String BEANSHELL = ".bsh";
    public static final String JAVASCRIPT = ".js";
    public static final String JYTHON = ".py";
    public static final String XML = ".xml";

    public static final String DEFAULT_GROOVY_BUILDER = "org.nanocontainer.script.groovy.GroovyContainerBuilder";
    public static final String DEFAULT_BEANSHELL_BUILDER = "org.nanocontainer.script.bsh.BeanShellContainerBuilder";
    public static final String DEFAULT_JAVASCRIPT_BUILDER = "org.nanocontainer.script.rhino.JavascriptContainerBuilder";
    public static final String DEFAULT_XML_BUILDER = "org.nanocontainer.script.xml.XMLContainerBuilder";
    public static final String DEFAULT_JYTHON_BUILDER = "org.nanocontainer.script.jython.JythonContainerBuilder";

    private final Map extensionToBuilders = new HashMap();



    public ScriptBuilderResolver() {
        resetBuilders();
    }


    /**
     * Retrieve the classname of the appropriate ScriptedContainerBuilder given the file.
     * @param compositionFile File
     * @return String
     */
    public String getBuilderClassName(File compositionFile) {
        String language = getExtension(compositionFile.getAbsolutePath());
        return getBuilderClassName(language);
    }




    /**
     * Retrieve the classname of the appropriate ScriptedContainerBuilder given the file.
     * @param compositionFile File
     * @return String
     */
    public String getBuilderClassName(URL compositionURL) {
        String language = getExtension(compositionURL.getFile());
        return getBuilderClassName(language);
    }

    /**
     * Retrieve the classname of the builder to use given the provided
     * extension.  Example:
     * <code><pre>
     * ScriptedContainerBuilderFactory factory = new ScriptedContainerBuilderFactory(.....);
     * String groovyBuilderName = factory.getBuilderClassName(&quot;.groovy&quot;);
     * assert &quot;org.nanocontainer.script.groovy.GroovyContainerBuilder&quot;.equals(groovyBuilderName);
     * </pre></code>
     * @param extension String
     * @return String
     */
    public synchronized String getBuilderClassName(final String extension) throws UnsupportedScriptTypeException{
        String resultingBuilderClassName = null;
        resultingBuilderClassName = (String) extensionToBuilders.get(extension);
        if (resultingBuilderClassName == null) {
            throw new UnsupportedScriptTypeException(extension, this.getAllSupportedExtensions());
        }
        return resultingBuilderClassName;
    }

    /**
     * Function to allow the resetting of the builder map to defaults.  Allows
     * testing of the static resource a bit better.
     */
    public synchronized void resetBuilders() {
        extensionToBuilders.clear();

        //This is a bit clunky compared to just registering the items
        //directly into the map, but this way IMO it provides a single access
        //point into the extensionToBuilders map.
        registerBuilder(GROOVY, DEFAULT_GROOVY_BUILDER);
        registerBuilder(BEANSHELL, DEFAULT_BEANSHELL_BUILDER);
        registerBuilder(JAVASCRIPT, DEFAULT_JAVASCRIPT_BUILDER);
        registerBuilder(XML, DEFAULT_XML_BUILDER);
        registerBuilder(JYTHON, DEFAULT_JYTHON_BUILDER);

    }

    /**
     * Registers/replaces a new handler for a given extension.  Allows for customizable
     * behavior in the various builders or the possibility to dynamically add
     * handlers for new file types.  Example:
     * <code><pre>
     * ScriptedContainerBuilderFactory factory = new ScriptedContainerBuilderFactory(...)
     * factory.registerBuilder(&quot;.groovy&quot;, &quot;org.nanocontainer.script.groovy.GroovyContainerBuilder&quot;);
     * ScriptedContainerBuilder builder = factory.getContainerBuilder();
     * assertNotNull(builder);
     * </pre></code>
     * <p>The internal code now requires synchronization of the builder extension map since
     * who knows what is using it when a new builder is registered.</p>
     * @param extension String the extension to register under.
     * @param className String the classname to use for the given extension.
     */
    public synchronized void registerBuilder(final String extension, final String className) {
        extensionToBuilders.put(extension, className);
    }

    /**
     * Retrieve a list of all supported extensions.
     * @return String[] of extensions including the period in the name.
     */
    public synchronized String[] getAllSupportedExtensions() {
         return (String[]) extensionToBuilders.keySet().toArray(new String[extensionToBuilders.size()]);
    }


    /**
     * Retrieve the extension of the file name.
     * @param fileName String
     * @return String
     */
    private static String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }


}

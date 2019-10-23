/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.nanocontainer.script;

import org.nanocontainer.DefaultNanoContainer;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.defaults.DefaultPicoContainer;

import java.io.*;
import java.net.URL;

/**
 * The main class for configuration of PicoContainer with various scripting languages.
 * When using the constructors taking a file, the extensions must be one of the following:
 * <ul>
 * <li>.groovy</li>
 * <li>.bsh</li>
 * <li>.js</li>
 * <li>.py</li>
 * <li>.xml</li>
 * </ul>
 * -And the content of the file likewise. See <a href="http://docs.codehaus.org/display/NANO/NanoContainer">NanoContainer documentation</a>
 * for details.
 *
 * @author Paul Hammant
 * @author Aslak Helles&oslah;y
 * @author Obie Fernandez
 * @author Michael Rimov
 */
public class ScriptedContainerBuilderFactory {

    /**
     * @deprecated Since NanoContainer RC-2. (14-Dec-2005).  Use ScriptBuilderResolver.GROOVY
     * instead.
     */
    public static final String GROOVY = ".groovy";

    /**
     * @deprecated Since NanoContainer RC-2. (14-Dec-2005).  Use ScriptBuilderResolver.BEANSHELL
     * instead.
     */
    public static final String BEANSHELL = ".bsh";

    /**
     * @deprecated Since NanoContainer RC-2. (14-Dec-2005).  Use ScriptBuilderResolver.JAVASCRIPT
     * instead.
     */
    public static final String JAVASCRIPT = ".js";

    /**
     * @deprecated Since NanoContainer RC-2. (14-Dec-2005).  Use ScriptBuilderResolver.JYTHON
     * instead.
     */
    public static final String JYTHON = ".py";

    /**
     * @deprecated Since NanoContainer RC-2. (14-Dec-2005).  Use ScriptBuilderResolver.XML
     * instead.
     */
    public static final String XML = ".xml";


    private ScriptedContainerBuilder containerBuilder;



    public ScriptedContainerBuilderFactory(File compositionFile, ClassLoader classLoader) throws IOException, ClassNotFoundException {
        this(compositionFile, classLoader, new ScriptBuilderResolver());
    }

    /**
     * Added since Nano RC-2.  This allows you to add/modify registered builders to replace
     * script handling or add new extensions by modifying a constructed ScriptBuilderResolver
     * before constructing this object.
     * @param compositionFile File The script file.
     * @param classLoader ClassLoader for class resolution once we resolve what the name of the
     * builder should be..
     * @param builderClassResolver ScriptBuilderResolver the resolver for figuring out
     * file names to container builder class names.
     * @throws IOException upon java.io.File name resolution error.
     * @throws ClassNotFoundException  If there is an error loading
     * the specified builder using the specified classloader.
     * @throws UnsupportedScriptTypeException if the extension of the file
     * does not match that of any known script.
     */
    public ScriptedContainerBuilderFactory(File compositionFile, ClassLoader classLoader, ScriptBuilderResolver builderClassResolver) throws IOException, ClassNotFoundException ,UnsupportedScriptTypeException {
        this(new FileReader(fileExists(compositionFile)), builderClassResolver.getBuilderClassName(compositionFile), classLoader);
    }


    public ScriptedContainerBuilderFactory(File compositionFile) throws IOException, ClassNotFoundException {
        this(compositionFile, Thread.currentThread().getContextClassLoader());
    }

    public ScriptedContainerBuilderFactory(URL compositionURL) throws ClassNotFoundException {
        this(compositionURL, Thread.currentThread().getContextClassLoader(),new ScriptBuilderResolver());
    }

    /**
     *
     * Added since Nano RC-2.  This allows you to add/modify registered builders to replace
     * script handling or add new extensions by modifying a constructed ScriptBuilderResolver
     * before constructing this object.
     * @param compositionURL The script URL.
     * @param builderClassResolver ScriptBuilderResolver the resolver for figuring out
     * file names to container builder class names.
     * @param classLoader ClassLoader for class resolution once we resolve what the name of the
     * builder should be..
     * @throws ClassNotFoundException  If there is an error loading
     * the specified builder using the specified classloader.
     * @throws UnsupportedScriptTypeException if the extension of the file
     * does not match that of any known script.
     */
    public ScriptedContainerBuilderFactory(URL compositionURL, ClassLoader classLoader, ScriptBuilderResolver builderClassResolver) throws ClassNotFoundException ,UnsupportedScriptTypeException {
        this(compositionURL, builderClassResolver.getBuilderClassName(compositionURL), classLoader);
    }


    public ScriptedContainerBuilderFactory(URL compositionURL, String builderClassName, ClassLoader contextClassLoader) throws ClassNotFoundException {
        createContainerBuilder(compositionURL, contextClassLoader, builderClassName);
    }


    public ScriptedContainerBuilderFactory(Reader composition, String builderClass) throws ClassNotFoundException {
        this(composition, builderClass, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Allows you to create a factory that isntantiats the builder class you desire.
     * @param composition Reader the script you wish to create the builder for.
     * @param builderClass String the builder class that instantiate.
     * @param classLoader ClassLoader the classloader to use for instantiation.
     * @throws ClassNotFoundException if the specified class cannot be found.
     */
    public ScriptedContainerBuilderFactory(Reader composition, String builderClass, ClassLoader classLoader) throws ClassNotFoundException {
        createContainerBuilder(composition, classLoader, builderClass);
    }


    /**
     * Performs the actual instantiation of the builder.
     * @param composition Object  Either a URL or a File, it doesn't matter which here.
     * @param classLoader ClassLoader the classloader ot use for classname resolution
     * and loading.
     * @param builderClass String the builder class to load.
     * @throws ClassNotFoundException if the specified builder class cannot be loaded.
     */
    private void createContainerBuilder(Object composition, ClassLoader classLoader, String builderClass) throws ClassNotFoundException {
        DefaultNanoContainer defaultNanoContainer;
        {
            // transient.
            DefaultPicoContainer factory = new DefaultPicoContainer();
            if(composition == null) {
                throw new NullPointerException("composition can't be null");
            }
            factory.registerComponentInstance(composition);

            if(classLoader == null) {
                // on some weird JVMs (like jeode) Thread.currentThread().getContextClassLoader() returns null !?!?
                //Found out on JDK 1.5 javadocs that Thread.currentThread().getContextClassLoader() MAY return null
                //while Class.getClassLoader() should NEVER return null.  -MR
                //
                //
                classLoader = getClass().getClassLoader();
            }
            factory.registerComponentInstance(classLoader);

            //
            //If we don't specify the classloader here, some of the things that make
            //up a nanocontainer may bomb. And we're only talking a reload
            //within a webapp!  -MR
            //
            defaultNanoContainer = new DefaultNanoContainer(classLoader,factory);
        }
        ComponentAdapter componentAdapter = defaultNanoContainer.registerComponentImplementation(builderClass);
        containerBuilder = (ScriptedContainerBuilder) componentAdapter.getComponentInstance(defaultNanoContainer.getPico());
    }

    private static File fileExists(final File file) throws FileNotFoundException {
        if (file.exists()) {
            return file;
        } else {
            //todo a proper exception.
            throw new FileNotFoundException("File " + file.getAbsolutePath() + " does not exist.");
        }
    }

    /**
     * This function does not support custom file type resolving -- for backwards
     * compatibility, it uses a fresh instance of ScriptBuilderResolver for
     * each invocation.  Use ScriptBuilderResolver instead.
     * @param extension String the file extension to res
     * @return String the classname to use for the specified extension.
     * @deprecated Since NanoContainer 1.0 RC-2.  Use the class ScriptBuilderResolver
     * for this functionality.
     */
    public static String getBuilderClassName(final String extension) {
        return new ScriptBuilderResolver().getBuilderClassName(extension);
    }


    /**
     * Retrieve the created container builder instance.
     * @return ScriptedContainerBuilder instance, never null.
     */
    public ScriptedContainerBuilder getContainerBuilder() {
        return containerBuilder;
    }

}

/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.nanocontainer.script;

import org.nanocontainer.integrationkit.LifecycleContainerBuilder;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 * Base abstract class for script-based container builders based.
 *
 * @author Aslak Helles&oslash;y
 * @author Obie Fernandez
 * @author Mauro Talevi
 * @version $Revision$
 */
public abstract class ScriptedContainerBuilder extends LifecycleContainerBuilder {
    private final Reader scriptReader;
    private final URL scriptURL;
    private final ClassLoader classLoader;

    public ScriptedContainerBuilder(Reader script, ClassLoader classLoader) {
        this.scriptReader = script;
        if (script == null) {
            throw new NullPointerException("script");
        }
        this.scriptURL = null;
        this.classLoader = classLoader;
        if ( classLoader == null) {
            throw new NullPointerException("classLoader");
        }
    }

    public ScriptedContainerBuilder(URL script, ClassLoader classLoader) {
        this.scriptReader = null;
        this.scriptURL = script;
        if (script == null) {
            throw new NullPointerException("script");
        }
        this.classLoader = classLoader;
        if ( classLoader == null) {
            throw new NullPointerException("classLoader");
        }
    }

    @Override
    protected final PicoContainer createContainer(PicoContainer parentContainer, Object assemblyScope) {
        try {
            return createContainerFromScript(parentContainer, assemblyScope);
        } finally {
            try {
                Reader reader = getScriptReader();
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                // do nothing. we've given it our best try, now get on with it
            }
        }
    }

    protected final ClassLoader getClassLoader() {
        return classLoader;
    }

    protected final Reader getScriptReader() throws IOException{
        if ( scriptReader != null ){
            return scriptReader;
        }
        return new InputStreamReader(scriptURL.openStream());
    }

    // TODO: This should really return NanoContainer using a nano variable in the script. --Aslak
    protected abstract PicoContainer createContainerFromScript(PicoContainer parentContainer, Object assemblyScope);

    @Override
    protected void composeContainer(MutablePicoContainer container, Object assemblyScope) {
        // do nothing. assume that this is done in createContainer().
    }
}
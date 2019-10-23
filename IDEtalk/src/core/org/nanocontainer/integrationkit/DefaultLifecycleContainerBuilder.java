/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/

package org.nanocontainer.integrationkit;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

public class DefaultLifecycleContainerBuilder extends LifecycleContainerBuilder {
    private final ContainerComposer composer;

    public DefaultLifecycleContainerBuilder(ContainerComposer composer) {
        this.composer = composer;
    }

    @Override
    protected void composeContainer(MutablePicoContainer container, Object assemblyScope) {
        composer.composeContainer(container, assemblyScope);
    }

    @Override
    protected PicoContainer createContainer(PicoContainer parentContainer, Object assemblyScope) {
        return new DefaultPicoContainer(parentContainer);
    }
}
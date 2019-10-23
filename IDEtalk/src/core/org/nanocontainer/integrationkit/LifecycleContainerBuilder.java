/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.nanocontainer.integrationkit;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.ObjectReference;

/**
 * @author <a href="mailto:joe@thoughtworks.net">Joe Walnes</a>
 * @author Aslak Helles&oslash;y
 * @author Paul Hammant
 * @author Mauro Talevi
 * @version $Revision$
 */
public abstract class LifecycleContainerBuilder implements ContainerBuilder {

    @Override
    public final void buildContainer(ObjectReference containerRef, ObjectReference parentContainerRef, Object assemblyScope, boolean addChildToParent) {
        PicoContainer parentContainer = parentContainerRef == null ? null : (PicoContainer) parentContainerRef.get();
        PicoContainer container = createContainer(parentContainer, assemblyScope);

        if (parentContainer instanceof MutablePicoContainer) {
            MutablePicoContainer mutableParentContainer = (MutablePicoContainer) parentContainer;

            if (addChildToParent) {
                // this synchronization is necessary, because several servlet requests may
                // occur at the same time for given session, and this produce race condition
                // especially in framed environments
                synchronized (mutableParentContainer) {
                    // register the child in the parent so that lifecycle can be propagated down the hierarchy
                    mutableParentContainer.addChildContainer(container);
                }
            }
        }

        if (container instanceof MutablePicoContainer) {
            composeContainer((MutablePicoContainer) container, assemblyScope);
        }

        // hold on to it
        containerRef.set(container);
    }

    @Override
    public void killContainer(ObjectReference containerRef) {
        try {
            PicoContainer pico = (PicoContainer) containerRef.get();
            pico.dispose();
            PicoContainer parent = pico.getParent();
            if (parent instanceof MutablePicoContainer) {
                // see comment in buildContainer
                synchronized (parent) {
                    ((MutablePicoContainer) parent).unregisterComponentByInstance(pico);
                }
            }
        } finally {
            containerRef.set(null);
        }
    }

    protected abstract void composeContainer(MutablePicoContainer container, Object assemblyScope);

    protected abstract PicoContainer createContainer(PicoContainer parentContainer, Object assemblyScope);
}

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

/**
 * Records method calls on a {@link MutablePicoContainer}.
 * This allows to replay all invocations on a different container instance.
 *
 * @author Konstantin Pribluda ( konstantin.pribluda(at)infodesire.com )
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
public interface ContainerRecorder {

    /**
     * Creates a new proxy that will forward all method invocations to the container passed to
     * the constructor. All method invocations are recorded so that they can be replayed on a
     * different container.
     *
     * @return a recording container proxy
     * @see #replay
     */
    public MutablePicoContainer getContainerProxy();

    /**
     * Replay recorded invocations on target container
     *
     * @param target container where the invocations should be replayed.
     */
    public void replay(MutablePicoContainer target);
}
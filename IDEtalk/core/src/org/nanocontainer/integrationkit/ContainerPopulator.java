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
 * <p>
 * Interface for populating a container.  The concern here is only on the
 * container to populate and not on the composition or the building.
 * </p>
 * <p>
 * An example of its use can be found in <a href="http://nanocontainer.org/nanowar">NanoWar</a>
 * for container population from multiple web scopes.
 * </p>
 *
 * @author Mauro Talevi
 */
public interface ContainerPopulator {

    /**
     * Populate a container
     * @param container the MutablePicoContainer to populate
     */
    public void populateContainer(MutablePicoContainer container);

}

/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Paul Hammant                                             *
 *****************************************************************************/

package org.nanocontainer;

import org.picocontainer.MutablePicoContainer;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public interface NanoPicoContainer extends MutablePicoContainer, NanoContainer {
  MutablePicoContainer makeChildContainer(String name);
}

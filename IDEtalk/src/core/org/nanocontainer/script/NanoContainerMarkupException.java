/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by James Strachan                                           *
 *****************************************************************************/

package org.nanocontainer.script;

import org.picocontainer.PicoException;

/**
 * Exception thrown due to invalid markup when assembling {@link org.nanocontainer.NanoContainer}s.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Aslak Helles&oslash;y
 * @version $Revision$
 */
public class NanoContainerMarkupException extends PicoException {

    public NanoContainerMarkupException(String message) {
        super(message);
    }

    public NanoContainerMarkupException(String message, Throwable e) {
        super(message, e);
    }

    public NanoContainerMarkupException(Throwable e) {
        super(e);
    }
}

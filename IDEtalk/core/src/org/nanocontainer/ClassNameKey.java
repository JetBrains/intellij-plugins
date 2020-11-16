/*
 *****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************
*/
package org.nanocontainer;

/**
 * ClassNameKey is a simple wrapper for a class name which is used as a key in
 * the registration of components in NanoContainer.
 *
 * @author Paul Hammant
 */
public class ClassNameKey {
    final String className;

    public ClassNameKey(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}

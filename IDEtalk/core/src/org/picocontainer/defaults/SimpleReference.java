/*
 * Copyright (c) PicoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
*/
package org.picocontainer.defaults;

/**
 * @author Aslak Helles&oslash;y
 */
public final class SimpleReference {
  private Object instance;

  public Object get() {
    return instance;
  }

  public void set(Object item) {
    this.instance = item;
  }
}

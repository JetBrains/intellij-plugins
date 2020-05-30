/*****************************************************************************
 * Copyright (C) PicoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.defaults;

import org.picocontainer.Disposable;

/**
 * @author Mauro Talevi
 * @author J&ouml;rg Schaible
 */
public final class DefaultLifecycleStrategy implements LifecycleStrategy {
  @Override
  public void dispose(Object component) {
    if (component instanceof Disposable) {
      ((Disposable)component).dispose();
    }
  }

  @Override
  public boolean hasLifecycle(Class type) {
    return Disposable.class.isAssignableFrom(type);
  }
}

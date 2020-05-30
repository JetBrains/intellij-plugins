/*****************************************************************************
 * Copyright (C) PicoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.defaults;

/**
 * An interface which specifies the lifecyle strategy on the component instance.
 * Lifecycle strategies are used by component adapters to delegate the lifecyle
 * operations on the component instances.
 *
 * @author Paul Hammant
 * @author Peter Royal
 * @author J&ouml;rg Schaible
 * @author Mauro Talevi
 */
public interface LifecycleStrategy {
  /**
   * Invoke the "dispose" method on the component instance if this is disposable.
   * It is up to the implementation of the strategy what "dispose" and "disposable" means.
   *
   * @param component the instance of the component to dispose
   */
  void dispose(Object component);

  /**
   * Test if a component instance has a lifecycle.
   *
   * @param type the component's type
   * @return <code>true</code> if the component has a lifecycle
   */
  boolean hasLifecycle(Class type);
}

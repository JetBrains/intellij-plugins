/*
 * Copyright (c) PicoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaible                                           *
*/
package org.picocontainer.defaults;

public abstract class ThreadLocalCyclicDependencyGuard extends ThreadLocal<Boolean> {
  @Override
  protected Boolean initialValue() {
    return Boolean.FALSE;
  }

  /**
   * Derive from this class and implement this function with the functionality
   * to observe for a dependency cycle.
   *
   * @return a value, if the functionality result in an expression,
   * otherwise just return <code>null</code>
   */
  public abstract Object run();

  /**
   * Call the observing function. The provided guard will hold the {@link Boolean} value.
   * If the guard is already <code>Boolean.TRUE</code> a {@link CyclicDependencyException}
   * will be  thrown.
   *
   * @param stackFrame the current stack frame
   * @return the result of the <code>run</code> method
   */
  public final Object observe(Class stackFrame) {
    if (Boolean.TRUE.equals(get())) {
      throw new CyclicDependencyException(stackFrame);
    }
    Object result;
    try {
      set(Boolean.TRUE);
      result = run();
    }
    catch (final CyclicDependencyException e) {
      e.push(stackFrame);
      throw e;
    }
    finally {
      set(Boolean.FALSE);
    }
    return result;
  }
}

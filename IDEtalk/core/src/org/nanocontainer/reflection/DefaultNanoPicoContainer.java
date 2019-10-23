/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Paul Hammant                                             *
 *****************************************************************************/

package org.nanocontainer.reflection;

import org.nanocontainer.NanoPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DefaultComponentAdapterFactory;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.defaults.LifecycleStrategy;

/**
 * This is a MutablePicoContainer that also supports soft composition. i.e. assembly by class name rather that class
 * reference.
 * <p>
 * In terms of implementation it adopts the behaviour of DefaultPicoContainer and DefaultNanoContainer.</p>
 *
 * @author Paul Hammant
 * @author Mauro Talevi
 * @author Michael Rimov
 * @version $Revision$
 */
public class DefaultNanoPicoContainer extends AbstractNanoPicoContainer implements NanoPicoContainer {

  public DefaultNanoPicoContainer(ClassLoader classLoader, ComponentAdapterFactory caf, PicoContainer parent) {
    super(new DefaultPicoContainer(caf, parent), classLoader);
  }

  public DefaultNanoPicoContainer(ClassLoader classLoader, PicoContainer parent) {
    super(new DefaultPicoContainer(new DefaultComponentAdapterFactory(), parent), classLoader);
  }

  public DefaultNanoPicoContainer(ComponentAdapterFactory caf) {
    super(new DefaultPicoContainer(caf, null), DefaultNanoPicoContainer.class.getClassLoader());
  }

  public DefaultNanoPicoContainer(PicoContainer pc) {
    super(new DefaultPicoContainer(pc), DefaultNanoPicoContainer.class.getClassLoader());
  }

  public DefaultNanoPicoContainer(ClassLoader classLoader) {
    super(new DefaultPicoContainer(), classLoader);
  }

  public DefaultNanoPicoContainer() {
    super(new DefaultPicoContainer(), DefaultNanoPicoContainer.class.getClassLoader());
  }


  /**
   * Constructor that provides the same control over the nanocontainer lifecycle strategies
   * as {@link DefaultPicoContainer(ComponentAdapterFactory, LifecycleStrategy, PicoContainer)}.
   *
   * @param componentAdapterFactory                   ComponentAdapterFactory
   * @param lifecycleStrategyForInstanceRegistrations LifecycleStrategy
   * @param parent                                    PicoContainer may be null if there is no parent.
   * @param cl                                        the Classloader to use.  May be null, in which case DefaultNanoPicoContainer.class.getClassLoader()
   *                                                  will be called instead.
   */
  public DefaultNanoPicoContainer(ComponentAdapterFactory componentAdapterFactory,
                                  LifecycleStrategy lifecycleStrategyForInstanceRegistrations, PicoContainer parent, ClassLoader cl) {

    super(new DefaultPicoContainer(componentAdapterFactory,
                                   lifecycleStrategyForInstanceRegistrations, parent),
          //Use a default classloader if none is specified.
          (cl != null) ? cl : DefaultNanoPicoContainer.class.getClassLoader());
  }

  /**
   * Copy Constructor.  Makes a new DefaultNanoPicoContainer with the same
   * attributes - ClassLoader, child PicoContainer type, ComponentAdapterFactory -
   * as the parent.
   * <p><tt>Note:</tt> This constructor is protected because are existing scripts
   * that call <tt>new DefaultNanoPicoContainer(PicoContainer)</tt>, and they get this
   * constructor instead (which has different behavior).</p>
   *
   * @param parent The object to copy.
   */
  protected DefaultNanoPicoContainer(final DefaultNanoPicoContainer parent) {
    super(parent.getDelegate().makeChildContainer(), parent.getComponentClassLoader());
    MutablePicoContainer parentDelegate = parent.getDelegate();
    parentDelegate.removeChildContainer(getDelegate());
    parentDelegate.addChildContainer(this);
  }


  @Override
  protected AbstractNanoPicoContainer createChildContainer() {
    return new DefaultNanoPicoContainer(this);
  }
}

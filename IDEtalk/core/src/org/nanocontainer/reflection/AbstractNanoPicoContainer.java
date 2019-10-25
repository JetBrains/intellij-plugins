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

import org.nanocontainer.ClassPathElement;
import org.nanocontainer.DefaultNanoContainer;
import org.nanocontainer.NanoContainer;
import org.nanocontainer.NanoPicoContainer;
import org.picocontainer.*;
import org.picocontainer.alternatives.AbstractDelegatingMutablePicoContainer;

import java.io.Serializable;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A base class for NanoPicoContainers. As well as the functionality indicated by the interface it
 * implements, extenders of this class will have named child component capability.
 *
 * @author Paul Hammant
 * @version $Revision$
 */
public abstract class AbstractNanoPicoContainer extends AbstractDelegatingMutablePicoContainer implements NanoPicoContainer, Serializable {

  protected Map namedChildContainers = new HashMap();

  // Serializable cannot be cascaded into DefaultNanoContainer's referenced classes
  // need to implement custom Externalisable regime.
  protected transient NanoContainer container;


  protected AbstractNanoPicoContainer(MutablePicoContainer delegate, ClassLoader classLoader) {
    super(delegate);
    container = new DefaultNanoContainer(classLoader, delegate);
  }

  @Override
  public final Object getComponentInstance(Object componentKey) throws PicoException {

    Object instance = getDelegate().getComponentInstance(componentKey);

    if (instance != null) {
      return instance;
    }

    ComponentAdapter componentAdapter = null;
    if (componentKey.toString().startsWith("*")) {
      String candidateClassName = componentKey.toString().substring(1);
      Collection cas = getComponentAdapters();
      for (Object o : cas) {
        ComponentAdapter ca = (ComponentAdapter)o;
        Object key = ca.getComponentKey();
        if (key instanceof Class && candidateClassName.equals(((Class)key).getName())) {
          componentAdapter = ca;
          break;
        }
      }
    }
    if (componentAdapter != null) {
      return componentAdapter.getComponentInstance(this);
    }
    else {
      return getComponentInstanceFromChildren(componentKey);
    }
  }

  private Object getComponentInstanceFromChildren(Object componentKey) {
    String componentKeyPath = componentKey.toString();
    int ix = componentKeyPath.indexOf('/');
    if (ix != -1) {
      String firstElement = componentKeyPath.substring(0, ix);
      String remainder = componentKeyPath.substring(ix + 1);
      Object o = getNamedContainers().get(firstElement);
      if (o != null) {
        MutablePicoContainer child = (MutablePicoContainer)o;
        return child.getComponentInstance(remainder);
      }
    }
    return null;
  }

  @Override
  public final MutablePicoContainer makeChildContainer() {
    return makeChildContainer("containers" + namedChildContainers.size());
  }

  /**
   * Makes a child container with the same basic characteristics of <tt>this</tt>
   * object (ComponentAdapterFactory, PicoContainer type, LifecycleManager, etc)
   *
   * @param name the name of the child container
   * @return The child MutablePicoContainer
   */
  @Override
  public MutablePicoContainer makeChildContainer(String name) {
    AbstractNanoPicoContainer child = createChildContainer();
    MutablePicoContainer parentDelegate = getDelegate();
    parentDelegate.removeChildContainer(child.getDelegate());
    parentDelegate.addChildContainer(child);
    namedChildContainers.put(name, child);
    return child;
  }

  protected abstract AbstractNanoPicoContainer createChildContainer();

  @Override
  public boolean removeChildContainer(PicoContainer child) {
    boolean result = getDelegate().removeChildContainer(child);
    Iterator children = namedChildContainers.entrySet().iterator();
    while (children.hasNext()) {
      Map.Entry e = (Map.Entry)children.next();
      PicoContainer pc = (PicoContainer)e.getValue();
      if (pc == child) {
        children.remove();
      }
    }
    return result;
  }

  protected final Map getNamedContainers() {
    return namedChildContainers;
  }

  @Override
  public Object getComponentInstanceOfType(String componentType) {
    return container.getComponentInstanceOfType(componentType);
  }

  @Override
  public MutablePicoContainer addDecoratingPicoContainer(Class picoContainerClass) {
    return container.addDecoratingPicoContainer(picoContainerClass);
  }


  @Override
  public ClassPathElement addClassLoaderURL(URL url) {
    return container.addClassLoaderURL(url);
  }

  @Override
  public ComponentAdapter registerComponentImplementation(String componentImplementationClassName)
    throws PicoRegistrationException, ClassNotFoundException, PicoIntrospectionException {
    return container.registerComponentImplementation(componentImplementationClassName);
  }

  @Override
  public ComponentAdapter registerComponentImplementation(Object key, String componentImplementationClassName)
    throws ClassNotFoundException {
    return container.registerComponentImplementation(key, componentImplementationClassName);
  }

  @Override
  public ComponentAdapter registerComponentImplementation(Object key, String componentImplementationClassName, Parameter[] parameters)
    throws ClassNotFoundException {
    return container.registerComponentImplementation(key, componentImplementationClassName, parameters);
  }

  @Override
  public ComponentAdapter registerComponentImplementation(Object key,
                                                          String componentImplementationClassName,
                                                          String[] parameterTypesAsString,
                                                          String[] parameterValuesAsString)
    throws PicoRegistrationException, ClassNotFoundException, PicoIntrospectionException {
    return container
      .registerComponentImplementation(key, componentImplementationClassName, parameterTypesAsString, parameterValuesAsString);
  }

  @Override
  public ComponentAdapter registerComponentImplementation(String componentImplementationClassName,
                                                          String[] parameterTypesAsString,
                                                          String[] parameterValuesAsString)
    throws PicoRegistrationException, ClassNotFoundException, PicoIntrospectionException {
    return container.registerComponentImplementation(componentImplementationClassName, parameterTypesAsString, parameterValuesAsString);
  }


  //TODO Should this method be the NanoContainer interface only?
  @Override
  public MutablePicoContainer getPico() {
    return this;
  }

  @Override
  public ClassLoader getComponentClassLoader() {
    return container.getComponentClassLoader();
  }

  @Override
  public boolean addChildContainer(PicoContainer child) {
    boolean result = getDelegate().addChildContainer(child);


    namedChildContainers.put("containers" + namedChildContainers.size(), child);
    return result;
  }
}

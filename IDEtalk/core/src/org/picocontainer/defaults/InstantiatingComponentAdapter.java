/*
 * Copyright (c) PicoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
*/
package org.picocontainer.defaults;

import org.picocontainer.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * This ComponentAdapter will instantiate a new object for each call to
 * {@link ComponentAdapter#getComponentInstance(PicoContainer)}.
 * That means that when used with a PicoContainer, getComponentInstance will
 * return a new object each time.
 *
 * @author Aslak Helles&oslash;y
 * @author Paul Hammant
 * @author J&ouml;rg Schaible
 * @author Mauro Talevi
 * @version $Revision: 2788 $
 * @since 1.0
 */
public abstract class InstantiatingComponentAdapter extends AbstractComponentAdapter implements LifecycleStrategy {
  /**
   * The parameters to use for initialization.
   */
  protected transient Parameter[] parameters;
  /**
   * Flag indicating instanciation of non-public classes.
   */
  protected boolean allowNonPublicClasses;

  /**
   * The strategy used to control the lifecycle
   */
  protected LifecycleStrategy lifecycleStrategy;

  /**
   * Constructs a new ComponentAdapter for the given key and implementation.
   *
   * @param componentKey            the search key for this implementation
   * @param componentImplementation the concrete implementation
   * @param parameters              the parameters to use for the initialization
   * @param allowNonPublicClasses   flag to allow instantiation of non-public classes
   * @param lifecycleStrategy       the lifecycle strategy used by this ComponentAdapter
   * @throws AssignabilityRegistrationException if the key is a type and the implementation cannot be assigned to
   * @throws NullPointerException               if one of the parameters is <code>null</code>
   */
  protected InstantiatingComponentAdapter(Object componentKey,
                                          Class componentImplementation,
                                          Parameter[] parameters,
                                          boolean allowNonPublicClasses,
                                          LifecycleStrategy lifecycleStrategy) {
    super(componentKey, componentImplementation);
    checkConcrete();
    if (parameters != null) {
      for (int i = 0; i < parameters.length; i++) {
        if (parameters[i] == null) {
          throw new NullPointerException("Parameter " + i + " is null");
        }
      }
    }
    this.parameters = parameters;
    this.allowNonPublicClasses = allowNonPublicClasses;
    this.lifecycleStrategy = lifecycleStrategy;
  }

  /**
   * Constructs a new ComponentAdapter for the given key and implementation.
   *
   * @param componentKey            the search key for this implementation
   * @param componentImplementation the concrete implementation
   * @param parameters              the parameters to use for the initialization
   * @param allowNonPublicClasses   flag to allow instantiation of non-public classes
   * @throws AssignabilityRegistrationException if the key is a type and the implementation cannot be assigned to
   * @throws NullPointerException               if one of the parameters is <code>null</code>
   */
  protected InstantiatingComponentAdapter(Object componentKey, Class componentImplementation,
                                          Parameter[] parameters, boolean allowNonPublicClasses) {
    this(componentKey, componentImplementation, parameters, allowNonPublicClasses, new DefaultLifecycleStrategy());
  }

  private void checkConcrete() {
    // Assert that the component class is concrete.
    Class<?> componentImplementation = getComponentImplementation();
    boolean isAbstract = (componentImplementation.getModifiers() & Modifier.ABSTRACT) == Modifier.ABSTRACT;
    if (componentImplementation.isInterface() || isAbstract) {
      throw new PicoRegistrationException("Bad Access: '" + componentImplementation.getName() + "' is not instantiable");
    }
  }

  /**
   * Create default parameters for the given types.
   *
   * @param parameters the parameter types
   * @return the array with the default parameters.
   */
  protected Parameter[] createDefaultParameters(Class[] parameters) {
    Parameter[] componentParameters = new Parameter[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      componentParameters[i] = ComponentParameter.DEFAULT;
    }
    return componentParameters;
  }

  @Override
  public void dispose(Object component) {
    lifecycleStrategy.dispose(component);
  }

  @Override
  public boolean hasLifecycle(Class type) {
    return lifecycleStrategy.hasLifecycle(type);
  }

  /**
   * Instantiate an object with given parameters and respect the accessible flag.
   *
   * @param constructor the constructor to use
   * @param parameters  the parameters for the constructor
   * @return the new object.
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  protected Object newInstance(Constructor constructor, Object[] parameters)
    throws InstantiationException, IllegalAccessException, InvocationTargetException {
    if (allowNonPublicClasses) {
      constructor.setAccessible(true);
    }
    return constructor.newInstance(parameters);
  }

  /**
   * Find and return the greediest satisfiable constructor.
   *
   * @param container the PicoContainer to resolve dependencies.
   * @return the found constructor.
   * @throws PicoIntrospectionException
   * @throws UnsatisfiableDependenciesException
   * @throws AmbiguousComponentResolutionException
   * @throws AssignabilityRegistrationException
   */
  protected abstract Constructor getGreediestSatisfiableConstructor(PicoContainer container) throws
                                                                                             PicoIntrospectionException,
                                                                                             UnsatisfiableDependenciesException,
                                                                                             AmbiguousComponentResolutionException,
                                                                                             AssignabilityRegistrationException;
}

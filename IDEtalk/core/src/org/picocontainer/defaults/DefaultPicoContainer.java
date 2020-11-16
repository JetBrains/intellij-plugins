/*
 * Copyright (c) PicoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
*/
package org.picocontainer.defaults;

import org.jetbrains.annotations.NotNull;
import org.picocontainer.*;

import java.util.*;

public class DefaultPicoContainer implements MutablePicoContainer, Disposable {
  private final Map componentKeyToAdapterCache = new HashMap();
  private final ComponentAdapterFactory componentAdapterFactory;
  private final DefaultPicoContainer parent;
  private final Set<DefaultPicoContainer> children = new HashSet<>();

  private final List componentAdapters = new ArrayList();
  // Keeps track of instantiation order.
  private final List orderedComponentAdapters = new ArrayList();

  // Keeps track of the container started status
  private boolean started = false;
  // Keeps track of the container disposed status
  private boolean disposed = false;

  private final LifecycleManager lifecycleManager = new LifecycleManager() {
    /**
     * {@inheritDoc}
     * Loops over all component adapters (in inverse order) and invokes
     * dispose(PicoContainer) method on the ones which are LifecycleManagers
     */
    @Override
    public void dispose(PicoContainer node) {
      List adapters = orderedComponentAdapters;
      int i = adapters.size() - 1;
      while (0 <= i) {
        Object adapter = adapters.get(i);
        if (adapter instanceof LifecycleManager) {
          LifecycleManager manager = (LifecycleManager)adapter;
          manager.dispose(node);
        }
        i--;
      }
    }

    @Override
    public boolean hasLifecycle() {
      throw new UnsupportedOperationException("Should not have been called");
    }
  };

  private final LifecycleStrategy lifecycleStrategyForInstanceRegistrations;

  /**
   * Creates a new container with a custom ComponentAdapterFactory, LifecycleStrategy for instance registration,
   * and a parent container.
   *
   * @param componentAdapterFactory                   the factory to use for creation of ComponentAdapters.
   * @param lifecycleStrategyForInstanceRegistrations the lifecylce strategy chosen for regiered
   *                                                  instance (not implementations!)
   * @param parent                                    the parent container (used for component dependency lookups).
   */
  public DefaultPicoContainer(ComponentAdapterFactory componentAdapterFactory,
                              LifecycleStrategy lifecycleStrategyForInstanceRegistrations,
                              PicoContainer parent) {
    if (componentAdapterFactory == null) throw new NullPointerException("componentAdapterFactory");
    if (lifecycleStrategyForInstanceRegistrations == null) throw new NullPointerException("lifecycleStrategyForInstanceRegistrations");
    this.componentAdapterFactory = componentAdapterFactory;
    this.lifecycleStrategyForInstanceRegistrations = lifecycleStrategyForInstanceRegistrations;
    this.parent = (DefaultPicoContainer)parent;
  }

  /**
   * Creates a new container with the DefaultComponentAdapterFactory using a
   * custom ComponentMonitor
   */
  public DefaultPicoContainer() {
    this(new DefaultComponentAdapterFactory(), new DefaultLifecycleStrategy(), null);
  }

  public Collection getComponentAdapters() {
    return Collections.unmodifiableList(componentAdapters);
  }

  @Override
  public final ComponentAdapter getComponentAdapter(@NotNull Object componentKey) {
    ComponentAdapter adapter = (ComponentAdapter)componentKeyToAdapterCache.get(componentKey);
    if (adapter == null && parent != null) {
      adapter = parent.getComponentAdapter(componentKey);
    }
    return adapter;
  }

  public ComponentAdapter getComponentAdapterOfType(@NotNull Class<?> componentType) {
    // See http://jira.codehaus.org/secure/ViewIssue.jspa?key=PICO-115
    ComponentAdapter adapterByKey = getComponentAdapter(componentType);
    if (adapterByKey != null) {
      return adapterByKey;
    }

    List found = getComponentAdaptersOfType(componentType);

    if (found.size() == 1) {
      return ((ComponentAdapter)found.get(0));
    }
    else if (found.size() == 0) {
      if (parent != null) {
        return parent.getComponentAdapterOfType(componentType);
      }
      else {
        return null;
      }
    }
    else {
      Class[] foundClasses = new Class[found.size()];
      for (int i = 0; i < foundClasses.length; i++) {
        foundClasses[i] = ((ComponentAdapter)found.get(i)).getComponentImplementation();
      }

      throw new AmbiguousComponentResolutionException(componentType, foundClasses);
    }
  }

  public List getComponentAdaptersOfType(@NotNull Class<?> componentType) {
    if (componentType == null) {
      return Collections.EMPTY_LIST;
    }
    List found = new ArrayList();
    for (Object o : getComponentAdapters()) {
      ComponentAdapter componentAdapter = (ComponentAdapter)o;

      if (componentType.isAssignableFrom(componentAdapter.getComponentImplementation())) {
        found.add(componentAdapter);
      }
    }
    return found;
  }

  /**
   * {@inheritDoc}
   * This method can be used to override the ComponentAdapter created by the {@link ComponentAdapterFactory}
   * passed to the constructor of this container.
   */
  @Override
  public ComponentAdapter registerComponent(ComponentAdapter componentAdapter) {
    Object componentKey = componentAdapter.getComponentKey();
    if (componentKeyToAdapterCache.containsKey(componentKey)) {
      throw new PicoRegistrationException("Key " + componentKey + " duplicated");
    }
    componentAdapters.add(componentAdapter);
    componentKeyToAdapterCache.put(componentKey, componentAdapter);
    return componentAdapter;
  }

  @Override
  public ComponentAdapter unregisterComponent(Object componentKey) {
    ComponentAdapter adapter = (ComponentAdapter)componentKeyToAdapterCache.remove(componentKey);
    componentAdapters.remove(adapter);
    orderedComponentAdapters.remove(adapter);
    return adapter;
  }

  /**
   * {@inheritDoc}
   * The returned ComponentAdapter will be an {@link InstanceComponentAdapter}.
   */
  @Override
  public ComponentAdapter registerComponentInstance(Object component) {
    return registerComponentInstance(component.getClass(), component);
  }

  /**
   * {@inheritDoc}
   * The returned ComponentAdapter will be an {@link InstanceComponentAdapter}.
   */
  @Override
  public ComponentAdapter registerComponentInstance(Object componentKey, Object componentInstance) {
    ComponentAdapter componentAdapter =
      new InstanceComponentAdapter(componentKey, componentInstance, lifecycleStrategyForInstanceRegistrations);
    return registerComponent(componentAdapter);
  }

  /**
   * {@inheritDoc}
   * The returned ComponentAdapter will be instantiated by the {@link ComponentAdapterFactory}
   * passed to the container's constructor.
   * @param componentImplementation
   */
  @Override
  public ComponentAdapter registerComponentImplementation(@NotNull Class<?> componentImplementation) {
    return registerComponentImplementation(componentImplementation, componentImplementation);
  }

  /**
   * {@inheritDoc}
   * The returned ComponentAdapter will be instantiated by the {@link ComponentAdapterFactory}
   * passed to the container's constructor.
   * @param componentKey
   * @param componentImplementation
   */
  @Override
  public ComponentAdapter registerComponentImplementation(@NotNull Object componentKey, @NotNull Class<?> componentImplementation) {
    return registerComponentImplementation(componentKey, componentImplementation, null);
  }

  /**
   * {@inheritDoc}
   * The returned ComponentAdapter will be instantiated by the {@link ComponentAdapterFactory}
   * passed to the container's constructor.
   */
  public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation, Parameter[] parameters) {
    ComponentAdapter componentAdapter = componentAdapterFactory.createComponentAdapter(componentKey, componentImplementation, parameters);
    return registerComponent(componentAdapter);
  }

  private void addOrderedComponentAdapter(ComponentAdapter componentAdapter) {
    if (!orderedComponentAdapters.contains(componentAdapter)) {
      orderedComponentAdapters.add(componentAdapter);
    }
  }

  public List getComponentInstances() throws PicoException {
    return getComponentInstancesOfType(Object.class);
  }

  public List getComponentInstancesOfType(Class componentType) {
    if (componentType == null) {
      return Collections.EMPTY_LIST;
    }

    Map adapterToInstanceMap = new HashMap();
    for (Object adapter : componentAdapters) {
      ComponentAdapter componentAdapter = (ComponentAdapter)adapter;
      if (componentType.isAssignableFrom(componentAdapter.getComponentImplementation())) {
        Object componentInstance = getInstance(componentAdapter);
        adapterToInstanceMap.put(componentAdapter, componentInstance);

        // This is to ensure all are added. (Indirect dependencies will be added
        // from InstantiatingComponentAdapter).
        addOrderedComponentAdapter(componentAdapter);
      }
    }
    List result = new ArrayList();
    for (Object componentAdapter : orderedComponentAdapters) {
      final Object componentInstance = adapterToInstanceMap.get(componentAdapter);
      if (componentInstance != null) {
        // may be null in the case of the "implicit" adapter
        // representing "this".
        result.add(componentInstance);
      }
    }
    return result;
  }

  @Override
  public Object getComponentInstance(@NotNull Object componentKey) {
    ComponentAdapter componentAdapter = getComponentAdapter(componentKey);
    if (componentAdapter != null) {
      return getInstance(componentAdapter);
    }
    else {
      return null;
    }
  }

  @Override
  public Object getComponentInstanceOfType(@NotNull Class<?> componentType) {
    final ComponentAdapter componentAdapter = getComponentAdapterOfType(componentType);
    return componentAdapter == null ? null : getInstance(componentAdapter);
  }

  private Object getInstance(ComponentAdapter componentAdapter) {
    // check wether this is our adapter
    // we need to check this to ensure up-down dependencies cannot be followed
    final boolean isLocal = componentAdapters.contains(componentAdapter);

    if (isLocal) {
      PicoException firstLevelException = null;
      Object instance = null;
      try {
        instance = componentAdapter.getComponentInstance(this);
      }
      catch (PicoInitializationException | PicoIntrospectionException e) {
        firstLevelException = e;
      }
      if (firstLevelException != null) {
        if (parent != null) {
          instance = parent.getComponentInstance(componentAdapter.getComponentKey());
          if (instance != null) {
            return instance;
          }
        }

        throw firstLevelException;
      }
      addOrderedComponentAdapter(componentAdapter);

      return instance;
    }
    else if (parent != null) {
      return parent.getComponentInstance(componentAdapter.getComponentKey());
    }

    return null;
  }

  public DefaultPicoContainer getParent() {
    return parent;
  }

  /**
   * Start the components of this PicoContainer and all its logical child containers.
   * The starting of the child container is only attempted if the parent
   * container start successfully.  The child container for which start is attempted
   * is tracked so that upon stop, only those need to be stopped.
   * The lifecycle operation is delegated to the component adapter,
   * if it is an instance of {@link LifecycleManager lifecycle manager}.
   * The actual {@link LifecycleStrategy lifecycle strategy} supported
   * depends on the concrete implementation of the adapter.
   *
   * @see LifecycleManager
   * @see LifecycleStrategy
   * @see #addChildContainer(PicoContainer)
   */
  public void start() {
    if (disposed) throw new IllegalStateException("Already disposed");
    if (started) throw new IllegalStateException("Already started");
    started = true;
  }

  /**
   * Stop the components of this PicoContainer and all its logical child containers.
   * The stopping of the child containers is only attempted for those that have been
   * started, possibly not successfully.
   * The lifecycle operation is delegated to the component adapter,
   * if it is an instance of {@link LifecycleManager lifecycle manager}.
   * The actual {@link LifecycleStrategy lifecycle strategy} supported
   * depends on the concrete implementation of the adapter.
   *
   * @see LifecycleManager
   * @see LifecycleStrategy
   * @see #addChildContainer(PicoContainer)
   */
  public void stop() {
    if (disposed) throw new IllegalStateException("Already disposed");
    if (!started) throw new IllegalStateException("Not started");
    started = false;
  }

  /**
   * Dispose the components of this PicoContainer and all its logical child containers.
   * The lifecycle operation is delegated to the component adapter,
   * if it is an instance of {@link LifecycleManager lifecycle manager}.
   * The actual {@link LifecycleStrategy lifecycle strategy} supported
   * depends on the concrete implementation of the adapter.
   *
   * @see LifecycleManager
   * @see LifecycleStrategy
   * @see #addChildContainer(PicoContainer)
   */
  @Override
  public void dispose() {
    if (disposed) throw new IllegalStateException("Already disposed");
    for (DefaultPicoContainer child : children) {
      child.dispose();
    }
    this.lifecycleManager.dispose(this);
    disposed = true;
  }

  public DefaultPicoContainer makeChildContainer() {
    DefaultPicoContainer pc = new DefaultPicoContainer(componentAdapterFactory, lifecycleStrategyForInstanceRegistrations, this);
    addChildContainer(pc);
    return pc;
  }

  public boolean addChildContainer(DefaultPicoContainer child) {
    return children.add(child);
  }
}

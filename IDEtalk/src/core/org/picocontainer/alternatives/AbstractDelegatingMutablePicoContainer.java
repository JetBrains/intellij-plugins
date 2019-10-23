// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.picocontainer.alternatives;

import org.picocontainer.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public abstract class AbstractDelegatingMutablePicoContainer implements MutablePicoContainer, Serializable {
  private final MutablePicoContainer delegate;

  public AbstractDelegatingMutablePicoContainer(MutablePicoContainer delegate) {
    this.delegate = delegate;
  }

  protected MutablePicoContainer getDelegate() {
    return this.delegate;
  }

  @Override
  public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation)
    throws PicoRegistrationException {
    return this.delegate.registerComponentImplementation(componentKey, componentImplementation);
  }

  @Override
  public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation, Parameter[] parameters)
    throws PicoRegistrationException {
    return this.delegate.registerComponentImplementation(componentKey, componentImplementation, parameters);
  }

  @Override
  public ComponentAdapter registerComponentImplementation(Class componentImplementation) throws PicoRegistrationException {
    return this.delegate.registerComponentImplementation(componentImplementation);
  }

  @Override
  public ComponentAdapter registerComponentInstance(Object componentInstance) throws PicoRegistrationException {
    return this.delegate.registerComponentInstance(componentInstance);
  }

  @Override
  public ComponentAdapter registerComponentInstance(Object componentKey, Object componentInstance) throws PicoRegistrationException {
    return this.delegate.registerComponentInstance(componentKey, componentInstance);
  }

  @Override
  public ComponentAdapter registerComponent(ComponentAdapter componentAdapter) throws PicoRegistrationException {
    return this.delegate.registerComponent(componentAdapter);
  }

  @Override
  public ComponentAdapter unregisterComponent(Object componentKey) {
    return this.delegate.unregisterComponent(componentKey);
  }

  @Override
  public ComponentAdapter unregisterComponentByInstance(Object componentInstance) {
    return this.delegate.unregisterComponentByInstance(componentInstance);
  }

  @Override
  public Object getComponentInstance(Object componentKey) {
    return this.delegate.getComponentInstance(componentKey);
  }

  @Override
  public Object getComponentInstanceOfType(Class componentType) {
    return this.delegate.getComponentInstanceOfType(componentType);
  }

  @Override
  public List getComponentInstances() {
    return this.delegate.getComponentInstances();
  }

  @Override
  public PicoContainer getParent() {
    return this.delegate.getParent();
  }

  @Override
  public ComponentAdapter getComponentAdapter(Object componentKey) {
    return this.delegate.getComponentAdapter(componentKey);
  }

  @Override
  public ComponentAdapter getComponentAdapterOfType(Class componentType) {
    return this.delegate.getComponentAdapterOfType(componentType);
  }

  @Override
  public Collection getComponentAdapters() {
    return this.delegate.getComponentAdapters();
  }

  @Override
  public List getComponentAdaptersOfType(Class componentType) {
    return this.delegate.getComponentAdaptersOfType(componentType);
  }

  @Override
  public void dispose() {
    this.delegate.dispose();
  }

  @Override
  public boolean addChildContainer(PicoContainer child) {
    return this.delegate.addChildContainer(child);
  }

  @Override
  public boolean removeChildContainer(PicoContainer child) {
    return this.delegate.removeChildContainer(child);
  }

  @Override
  public List getComponentInstancesOfType(Class type) throws PicoException {
    return this.delegate.getComponentInstancesOfType(type);
  }

  public boolean equals(Object obj) {
    return this.delegate.equals(obj) || this == obj;
  }
}

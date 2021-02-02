/*
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Aslak Hellesoy and Paul Hammant                          *
 */
package org.nanocontainer;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoException;
import org.picocontainer.defaults.ConstantParameter;
import org.picocontainer.defaults.CustomPermissionsURLClassLoader;
import org.picocontainer.defaults.DefaultPicoContainer;

import java.net.URL;
import java.security.AccessController;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 */
public final class DefaultNanoContainer {
  private static final Map<String, String> primitiveNameToBoxedName = new HashMap<>();

  static {
    primitiveNameToBoxedName.put("int", Integer.class.getName());
    primitiveNameToBoxedName.put("byte", Byte.class.getName());
    primitiveNameToBoxedName.put("short", Short.class.getName());
    primitiveNameToBoxedName.put("long", Long.class.getName());
    primitiveNameToBoxedName.put("float", Float.class.getName());
    primitiveNameToBoxedName.put("double", Double.class.getName());
    primitiveNameToBoxedName.put("boolean", Boolean.class.getName());
  }

  private final List<ClassPathElement> classPathElements = new ArrayList<>();
  private DefaultPicoContainer picoContainer;
  private final ClassLoader parentClassLoader;

  private ClassLoader componentClassLoader;
  private boolean componentClassLoaderLocked;

  private static String getClassName(String primitiveOrClass) {
    String fromMap = primitiveNameToBoxedName.get(primitiveOrClass);
    return fromMap != null ? fromMap : primitiveOrClass;
  }

  public DefaultNanoContainer(ClassLoader parentClassLoader, DefaultPicoContainer picoContainer) {
    this.parentClassLoader = parentClassLoader;
    if (picoContainer == null) {
      throw new NullPointerException("picoContainer");
    }
    this.picoContainer = picoContainer;
  }

  public DefaultNanoContainer(ClassLoader parentClassLoader) {
    this(parentClassLoader, new DefaultPicoContainer());
  }

  public void registerComponentImplementation(Object key, String componentImplementationClassName)
    throws ClassNotFoundException {
    Class<?> componentImplementation = loadClass(componentImplementationClassName);
    if (key instanceof ClassNameKey) {
      key = loadClass(((ClassNameKey)key).getClassName());
    }
    picoContainer.registerComponentImplementation(key, componentImplementation);
  }


  private Class<?> loadClass(final String className) throws ClassNotFoundException {
    ClassLoader classLoader = getComponentClassLoader();
    String cn = getClassName(className);
    return classLoader.loadClass(cn);
  }

  public ClassPathElement addClassLoaderURL(URL url) {
    if (componentClassLoaderLocked) throw new IllegalStateException("ClassLoader URLs cannot be added once this instance is locked");

    ClassPathElement classPathElement = new ClassPathElement(url);
    classPathElements.add(classPathElement);
    return classPathElement;
  }

  public ClassLoader getComponentClassLoader() {
    if (componentClassLoader == null) {
      componentClassLoaderLocked = true;
      componentClassLoader = AccessController.doPrivileged(new PrivilegedAction<>() {
        @Override
        public ClassLoader run() {
          return new CustomPermissionsURLClassLoader(getURLs(classPathElements), makePermissions(), parentClassLoader);
        }
      });
    }
    return componentClassLoader;
  }

  public DefaultPicoContainer getPico() {
    return picoContainer;
  }

  private Map<URL, PermissionCollection> makePermissions() {
    Map<URL, PermissionCollection> permissionsMap = new HashMap<>();
    for (ClassPathElement element : classPathElements) {
      PermissionCollection permissionCollection = element.getPermissionCollection();
      permissionsMap.put(element.getUrl(), permissionCollection);
    }
    return permissionsMap;
  }

  private static URL[] getURLs(List<ClassPathElement> classPathElements) {
    final URL[] urls = new URL[classPathElements.size()];
    for (int i = 0; i < urls.length; i++) {
      urls[i] = classPathElements.get(i).getUrl();
    }
    return urls;
  }

  public Object getComponentInstanceOfType(String componentType) {
    try {
      Class<?> compType = getComponentClassLoader().loadClass(componentType);
      return picoContainer.getComponentInstanceOfType(compType);
    }
    catch (ClassNotFoundException e) {
      String message = "Can't resolve class as type '" + componentType + "'";
      throw new PicoException(message);
    }
  }

  public MutablePicoContainer addDecoratingPicoContainer(Class<?> picoContainerClass) {
    DefaultPicoContainer pico = new DefaultPicoContainer();
    pico.registerComponentImplementation(MutablePicoContainer.class, picoContainerClass,
      new Parameter[]{new ConstantParameter(picoContainer)});
    picoContainer = (DefaultPicoContainer)pico.getComponentInstanceOfType(MutablePicoContainer.class);
    return picoContainer;
  }
}

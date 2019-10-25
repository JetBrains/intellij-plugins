/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Aslak Hellesoy and Paul Hammant                          *
 *****************************************************************************/

package org.nanocontainer;

import org.picocontainer.*;
import org.picocontainer.defaults.BeanPropertyComponentAdapter;
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
 * The default implementation of {@link NanoContainer}.
 *
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 */
public final class DefaultNanoContainer implements NanoContainer {
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
  private MutablePicoContainer picoContainer;
  private final ClassLoader parentClassLoader;

  private ClassLoader componentClassLoader;
  private boolean componentClassLoaderLocked;

  private static String getClassName(String primitiveOrClass) {
    String fromMap = primitiveNameToBoxedName.get(primitiveOrClass);
    return fromMap != null ? fromMap : primitiveOrClass;
  }

  public DefaultNanoContainer(ClassLoader parentClassLoader, MutablePicoContainer picoContainer) {
    this.parentClassLoader = parentClassLoader;
    if (picoContainer == null) {
      throw new NullPointerException("picoContainer");
    }
    this.picoContainer = picoContainer;
  }

  public DefaultNanoContainer(ClassLoader parentClassLoader) {
    this(parentClassLoader, new DefaultPicoContainer());
  }

  @Override
  public ComponentAdapter registerComponentImplementation(String componentImplementationClassName)
    throws PicoRegistrationException, ClassNotFoundException, PicoIntrospectionException {
    return picoContainer.registerComponentImplementation(loadClass(componentImplementationClassName));
  }

  @Override
  public ComponentAdapter registerComponentImplementation(Object key, String componentImplementationClassName)
    throws ClassNotFoundException {
    Class componentImplementation = loadClass(componentImplementationClassName);
    if (key instanceof ClassNameKey) {
      key = loadClass(((ClassNameKey)key).getClassName());
    }
    return picoContainer.registerComponentImplementation(key, componentImplementation);
  }


  @Override
  public ComponentAdapter registerComponentImplementation(Object key, String componentImplementationClassName, Parameter[] parameters)
    throws ClassNotFoundException {
    Class componentImplementation = loadClass(componentImplementationClassName);
    if (key instanceof ClassNameKey) {
      key = loadClass(((ClassNameKey)key).getClassName());
    }
    return picoContainer.registerComponentImplementation(key, componentImplementation, parameters);
  }

  @Override
  public ComponentAdapter registerComponentImplementation(Object key,
                                                          String componentImplementationClassName,
                                                          String[] parameterTypesAsString,
                                                          String[] parameterValuesAsString)
    throws PicoRegistrationException, ClassNotFoundException, PicoIntrospectionException {
    Class componentImplementation = getComponentClassLoader().loadClass(componentImplementationClassName);
    if (key instanceof ClassNameKey) {
      key = loadClass(((ClassNameKey)key).getClassName());
    }
    return registerComponentImplementation(parameterTypesAsString, parameterValuesAsString, key, componentImplementation);
  }

  @Override
  public ComponentAdapter registerComponentImplementation(String componentImplementationClassName,
                                                          String[] parameterTypesAsString,
                                                          String[] parameterValuesAsString)
    throws PicoRegistrationException, ClassNotFoundException, PicoIntrospectionException {
    Class componentImplementation = getComponentClassLoader().loadClass(componentImplementationClassName);
    return registerComponentImplementation(parameterTypesAsString, parameterValuesAsString, componentImplementation,
      componentImplementation);
  }

  private ComponentAdapter registerComponentImplementation(String[] parameterTypesAsString,
                                                           String[] parameterValuesAsString,
                                                           Object key,
                                                           Class componentImplementation) throws ClassNotFoundException {
    Parameter[] parameters = new Parameter[parameterTypesAsString.length];
    for (int i = 0; i < parameters.length; i++) {
      Object value = BeanPropertyComponentAdapter.convert(parameterTypesAsString[i], parameterValuesAsString[i], getComponentClassLoader());
      parameters[i] = new ConstantParameter(value);
    }
    return picoContainer.registerComponentImplementation(key, componentImplementation, parameters);
  }

  private Class loadClass(final String className) throws ClassNotFoundException {
    ClassLoader classLoader = getComponentClassLoader();
    String cn = getClassName(className);
    return classLoader.loadClass(cn);
  }

  @Override
  public ClassPathElement addClassLoaderURL(URL url) {
    if (componentClassLoaderLocked) throw new IllegalStateException("ClassLoader URLs cannot be added once this instance is locked");

    ClassPathElement classPathElement = new ClassPathElement(url);
    classPathElements.add(classPathElement);
    return classPathElement;
  }

  @Override
  public ClassLoader getComponentClassLoader() {
    if (componentClassLoader == null) {
      componentClassLoaderLocked = true;
      componentClassLoader = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction() {
        @Override
        public Object run() {
          return new CustomPermissionsURLClassLoader(getURLs(classPathElements), makePermissions(), parentClassLoader);
        }
      });
    }
    return componentClassLoader;
  }

  @Override
  public MutablePicoContainer getPico() {
    return picoContainer;
  }

  private Map makePermissions() {
    Map permissionsMap = new HashMap();
    for (Object element : classPathElements) {
      ClassPathElement cpe = (ClassPathElement)element;
      PermissionCollection permissionCollection = cpe.getPermissionCollection();
      permissionsMap.put(cpe.getUrl(), permissionCollection);
    }
    return permissionsMap;
  }

  private static URL[] getURLs(List classPathElemelements) {
    final URL[] urls = new URL[classPathElemelements.size()];
    for (int i = 0; i < urls.length; i++) {
      urls[i] = ((ClassPathElement)classPathElemelements.get(i)).getUrl();
    }
    return urls;
  }

  @Override
  public Object getComponentInstanceOfType(String componentType) {
    try {
      Class compType = getComponentClassLoader().loadClass(componentType);
      return picoContainer.getComponentInstanceOfType(compType);
    }
    catch (ClassNotFoundException e) {
      String message = "Can't resolve class as type '" + componentType + "'";
      throw new PicoException(message);
    }
  }

  @Override
  public MutablePicoContainer addDecoratingPicoContainer(Class picoContainerClass) {
    DefaultPicoContainer pico = new DefaultPicoContainer();
    pico.registerComponentImplementation(MutablePicoContainer.class, picoContainerClass,
      new Parameter[]{new ConstantParameter(picoContainer)});
    picoContainer = (MutablePicoContainer)pico.getComponentInstanceOfType(MutablePicoContainer.class);
    return picoContainer;
  }
}

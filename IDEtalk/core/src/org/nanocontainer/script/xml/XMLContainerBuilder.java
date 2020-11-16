/*
 ****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Aslak Hellesoy and Paul Hammant                          *
 *****************************************************************************
*/

package org.nanocontainer.script.xml;

import org.nanocontainer.ClassNameKey;
import org.nanocontainer.ClassPathElement;
import org.nanocontainer.DefaultNanoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.defaults.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

/**
 * This class builds up a hierarchy of PicoContainers from an XML configuration file.
 *
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Jeppe Cramon
 * @author Mauro Talevi
 * @version $Revision$
 */
public final class XMLContainerBuilder {
  private final static String DEFAULT_COMPONENT_ADAPTER_FACTORY = DefaultComponentAdapterFactory.class.getName();
  private final static String DEFAULT_COMPONENT_INSTANCE_FACTORY = BeanComponentInstanceFactory.class.getName();

  private final static String CONTAINER = "container";
  private final static String CLASSPATH = "classpath";
  private final static String CLASSLOADER = "classloader";
  private static final String CLASS_NAME_KEY = "class-name-key";
  private final static String COMPONENT = "component";
  private final static String COMPONENT_IMPLEMENTATION = "component-implementation";
  private final static String COMPONENT_INSTANCE = "component-instance";
  private final static String COMPONENT_ADAPTER = "component-adapter";
  private final static String COMPONENT_ADAPTER_FACTORY = "component-adapter-factory";
  private final static String DECORATING_PICOCONTAINER = "decorating-picocontainer";
  private final static String CLASS = "class";
  private final static String FACTORY = "factory";
  private final static String FILE = "file";
  private final static String KEY = "key";
  private final static String EMPTY_COLLECTION = "empty-collection";
  private final static String COMPONENT_VALUE_TYPE = "component-value-type";
  private final static String COMPONENT_KEY_TYPE = "component-key-type";
  private final static String PARAMETER = "parameter";
  private final static String URL = "url";

  private final static String CLASSNAME = "classname";
  private final static String CONTEXT = "context";
  private final static String VALUE = "value";

  private static final String EMPTY = "";
  private final ClassLoader myClassLoader;

  private Element rootElement;

  public XMLContainerBuilder(Reader script, ClassLoader classLoader) {
    myClassLoader = classLoader;
    try {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      parse(documentBuilder, new InputSource(script));
    }
    catch (ParserConfigurationException e) {
      throw new PicoException(e);
    }
  }

  private void parse(DocumentBuilder documentBuilder, InputSource inputSource) {
    try {
      rootElement = documentBuilder.parse(inputSource).getDocumentElement();
    }
    catch (SAXException | IOException e) {
      throw new PicoException(e);
    }
  }

  public void populateContainer(DefaultPicoContainer container) {
    try {
      String parentClass = rootElement.getAttribute("parentclassloader");
      ClassLoader classLoader = myClassLoader;
      if (parentClass != null && !EMPTY.equals(parentClass)) {
        classLoader = classLoader.loadClass(parentClass).getClassLoader();
      }
      DefaultNanoContainer nanoContainer = new DefaultNanoContainer(classLoader, container);
      registerComponentsAndChildContainers(nanoContainer, rootElement, new DefaultNanoContainer(myClassLoader));
    }
    catch (ClassNotFoundException e) {
      String message = "Class not found: " + e.getMessage();
      throw new PicoException(message, e);
    }
    catch (IOException | SAXException e) {
      throw new PicoException(e);
    }
  }

  private void registerComponentsAndChildContainers(DefaultNanoContainer parentContainer,
                                                    Element containerElement,
                                                    DefaultNanoContainer knownComponentAdapterFactories)
    throws ClassNotFoundException, IOException, SAXException {

    DefaultNanoContainer metaContainer = new DefaultNanoContainer(myClassLoader, knownComponentAdapterFactories.getPico());
    NodeList children = containerElement.getChildNodes();
    // register classpath first, regardless of order in the document.
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof Element) {
        Element childElement = (Element)children.item(i);
        String name = childElement.getNodeName();
        if (CLASSPATH.equals(name)) {
          registerClasspath(parentContainer, childElement);
        }
      }
    }
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof Element) {
        Element childElement = (Element)children.item(i);
        String name = childElement.getNodeName();
        if (CONTAINER.equals(name)) {
          DefaultPicoContainer childContainer = parentContainer.getPico().makeChildContainer();
          DefaultNanoContainer childNanoContainer = new DefaultNanoContainer(parentContainer.getComponentClassLoader(), childContainer);
          registerComponentsAndChildContainers(childNanoContainer, childElement, metaContainer);
        }
        else if (COMPONENT_IMPLEMENTATION.equals(name)
                 || COMPONENT.equals(name)) {
          registerComponentImplementation(parentContainer, childElement);
        }
        else if (COMPONENT_INSTANCE.equals(name)) {
          registerComponentInstance(parentContainer, childElement);
        }
        else if (COMPONENT_ADAPTER.equals(name)) {
          registerComponentAdapter(parentContainer, childElement, metaContainer);
        }
        else if (COMPONENT_ADAPTER_FACTORY.equals(name)) {
          addComponentAdapterFactory(childElement, metaContainer);
        }
        else if (CLASSLOADER.equals(name)) {
          registerClassLoader(parentContainer, childElement, metaContainer);
        }
        else if (DECORATING_PICOCONTAINER.equals(name)) {
          addDecoratingPicoContainer(parentContainer, childElement);
        }
        else if (!CLASSPATH.equals(name)) {
          String message = "Unsupported element:" + name;
          throw new PicoException(message);
        }
      }
    }
  }


  private void addComponentAdapterFactory(Element element, DefaultNanoContainer metaContainer)
    throws ClassNotFoundException {
    if (notSet(element.getAttribute(KEY))) {
      String message = "'" + KEY + "' attribute not specified for " + element.getNodeName();
      throw new PicoException(message);
    }
    Element node = (Element)element.cloneNode(false);
    NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof Element) {
        Element childElement = (Element)children.item(i);
        String name = childElement.getNodeName();
        if (COMPONENT_ADAPTER_FACTORY.equals(name)) {
          if (childElement.getAttribute(KEY) != null && !childElement.getAttribute(KEY).isEmpty()) {
            String message = "'" + KEY + "' attribute must not be specified for nested " + element.getNodeName();
            throw new PicoException(message);
          }
          childElement = (Element)childElement.cloneNode(true);
          String key = String.valueOf(System.identityHashCode(childElement));
          childElement.setAttribute(KEY, key);
          addComponentAdapterFactory(childElement, metaContainer);
          // replace nested CAF with a ComponentParameter using an internally generated key
          Element parameter = node.getOwnerDocument().createElement(PARAMETER);
          parameter.setAttribute(KEY, key);
          node.appendChild(parameter);
        }
        else if (PARAMETER.equals(name)) {
          node.appendChild(childElement.cloneNode(true));
        }
      }
    }
    // handle CAF now as standard component in the metaContainer
    registerComponentImplementation(metaContainer, node);
  }

  private void registerClassLoader(DefaultNanoContainer parentContainer, Element childElement, DefaultNanoContainer metaContainer)
    throws IOException, SAXException, ClassNotFoundException {
    String parentClass = childElement.getAttribute("parentclassloader");
    ClassLoader parentClassLoader = parentContainer.getComponentClassLoader();
    if (parentClass != null && !EMPTY.equals(parentClass)) {
      parentClassLoader = parentClassLoader.loadClass(parentClass).getClassLoader();
    }
    DefaultNanoContainer nano = new DefaultNanoContainer(parentClassLoader, parentContainer.getPico());
    registerComponentsAndChildContainers(nano, childElement, metaContainer);
  }

  private static void registerClasspath(DefaultNanoContainer container, Element classpathElement) throws IOException, ClassNotFoundException {
    NodeList children = classpathElement.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof Element) {
        Element childElement = (Element)children.item(i);

        String fileName = childElement.getAttribute(FILE);
        String urlSpec = childElement.getAttribute(URL);
        URL url;
        if (urlSpec != null && !EMPTY.equals(urlSpec)) {
          url = new URL(urlSpec);
        }
        else {
          File file = new File(fileName);
          if (!file.exists()) {
            throw new IOException(file.getAbsolutePath() + " doesn't exist");
          }
          url = file.toURL();
        }
        ClassPathElement cpe = container.addClassLoaderURL(url);
        registerPermissions(cpe, childElement);
      }
    }
  }

  private static void registerPermissions(ClassPathElement classPathElement, Element classPathXmlElement) throws ClassNotFoundException {
    NodeList children = classPathXmlElement.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof Element) {
        Element childElement = (Element)children.item(i);

        String permissionClassName = childElement.getAttribute(CLASSNAME);
        String action = childElement.getAttribute(CONTEXT);
        String value = childElement.getAttribute(VALUE);
        DefaultPicoContainer mpc = new DefaultPicoContainer();
        mpc.registerComponentImplementation(Permission.class, Class.forName(permissionClassName),
                                            new Parameter[]{new ConstantParameter(action), new ConstantParameter(value)});

        Permission permission = (Permission)mpc.getComponentInstanceOfType(Permission.class);
        classPathElement.grantPermission(permission);
      }
    }
  }

  private void registerComponentImplementation(DefaultNanoContainer container, Element element)
    throws ClassNotFoundException {
    String className = element.getAttribute(CLASS);
    if (notSet(className)) {
      String message = "'" + CLASS + "' attribute not specified for " + element.getNodeName();
      throw new PicoException(message);
    }

    Parameter[] parameters = createChildParameters(container, element);
    Class clazz = container.getComponentClassLoader().loadClass(className);
    Object key = element.getAttribute(KEY);
    String classKey = element.getAttribute(CLASS_NAME_KEY);
    if (notSet(key)) {
      if (!notSet(classKey)) {
        key = myClassLoader.loadClass(classKey);
      }
      else {
        key = clazz;
      }
    }
    if (parameters == null) {
      container.getPico().registerComponentImplementation(key, clazz);
    }
    else {
      container.getPico().registerComponentImplementation(key, clazz, parameters);
    }
  }

  private void addDecoratingPicoContainer(DefaultNanoContainer parentContainer, Element childElement) throws ClassNotFoundException {
    String className = childElement.getAttribute("class");

    parentContainer.addDecoratingPicoContainer(myClassLoader.loadClass(className));
  }


  private Parameter[] createChildParameters(DefaultNanoContainer container, Element element) throws ClassNotFoundException {
    List parametersList = new ArrayList();
    NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof Element) {
        Element childElement = (Element)children.item(i);
        if (PARAMETER.equals(childElement.getNodeName())) {
          parametersList.add(createParameter(container.getPico(), childElement));
        }
      }
    }

    Parameter[] parameters = null;
    if (!parametersList.isEmpty()) {
      parameters = (Parameter[])parametersList.toArray(new Parameter[0]);
    }
    return parameters;
  }

  /**
   * Build the org.picocontainer.Parameter from the <code>parameter</code> element. This could
   * create either a ComponentParameter or ConstantParameter instance,
   * depending on the values of the element's attributes. This is somewhat
   * complex because there are five constructors for ComponentParameter and one for
   * ConstantParameter. These are:
   *
   * <a href="http://www.picocontainer.org/picocontainer/latest/picocontainer/apidocs/org/picocontainer/defaults/ComponentParameter.html">ComponentParameter Javadocs</a>:
   *
   * <code>ComponentParameter() - Expect any scalar paramter of the appropriate type or an Array.
   * ComponentParameter(boolean emptyCollection) - Expect any scalar paramter of the appropriate type or an Array.
   * ComponentParameter(Class componentValueType, boolean emptyCollection) - Expect any scalar paramter of the appropriate type or the collecting type Array,Collectionor Map.
   * ComponentParameter(Class componentKeyType, Class componentValueType, boolean emptyCollection) - Expect any scalar paramter of the appropriate type or the collecting type Array,Collectionor Map.
   * ComponentParameter(Object componentKey) - Expect a parameter matching a component of a specific key.</code>
   * <p>
   * and
   *
   * <a href="http://www.picocontainer.org/picocontainer/latest/picocontainer/apidocs/org/picocontainer/defaults/ConstantParameter.html">ConstantParameter Javadocs</a>:
   *
   * <code>ConstantParameter(Object value)</code>
   * <p>
   * The rules for this are, in order:
   * <p>
   * 1) If the <code>key</code> attribute is not null/empty, the fifth constructor will be used.
   * 2) If the <code>componentKeyType</code> attribute is not null/empty, the fourth constructor will be used.
   * In this case, both the <code>componentValueType</code> and <code>emptyCollection</code> attributes must be non-null/empty or an exception will be thrown.
   * 3) If the <code>componentValueType</code> attribute is not null/empty, the third constructor will be used.
   * In this case, the <code>emptyCollection</code> attribute must be non-null/empty.
   * 4) If the <code>emptyCollection</code> attribute is not null/empty, the second constructor will be used.
   * 5) If there is no child element of the parameter, the first constructor will be used.
   * 6) Otherwise, the return value will be a ConstantParameter with the return from the createInstance value.
   */
  private Parameter createParameter(PicoContainer pico, Element element) throws ClassNotFoundException {
    final Parameter parameter;
    String key = element.getAttribute(KEY);
    String emptyCollectionString = element.getAttribute(EMPTY_COLLECTION);
    String componentValueTypeString = element.getAttribute(COMPONENT_VALUE_TYPE);
    String componentKeyTypeString = element.getAttribute(COMPONENT_KEY_TYPE);

    // key not null/empty takes precidence
    if (key != null && !EMPTY.equals(key)) {
      parameter = new ComponentParameter(key);
    }
    else if (componentKeyTypeString != null && !EMPTY.equals(componentKeyTypeString)) {
      if (emptyCollectionString == null || componentValueTypeString == null ||
          EMPTY.equals(emptyCollectionString) || EMPTY.equals(componentValueTypeString)) {

        String message = "The componentKeyType attribute was specified (" +
                         componentKeyTypeString + ") but one or both of the emptyCollection (" +
                         emptyCollectionString + ") or componentValueType (" + componentValueTypeString +
                         ") was empty or null.";
        throw new PicoException(message);
      }

      Class componentKeyType = myClassLoader.loadClass(componentKeyTypeString);
      Class componentValueType = myClassLoader.loadClass(componentValueTypeString);

      boolean emptyCollection = Boolean.parseBoolean(emptyCollectionString);
      parameter = new ComponentParameter(componentKeyType, componentValueType, emptyCollection);
    }
    else if (componentValueTypeString != null && !EMPTY.equals(componentValueTypeString)) {
      if (emptyCollectionString == null || EMPTY.equals(emptyCollectionString)) {

        String message = "The componentValueType attribute was specified (" +
                         componentValueTypeString + ") but the emptyCollection (" +
                         emptyCollectionString + ") was empty or null.";
        throw new PicoException(message);
      }

      Class componentValueType = myClassLoader.loadClass(componentValueTypeString);

      boolean emptyCollection = Boolean.parseBoolean(emptyCollectionString);

      parameter = new ComponentParameter(componentValueType, emptyCollection);
    }
    else if (emptyCollectionString != null && !EMPTY.equals(emptyCollectionString)) {
      boolean emptyCollection = Boolean.parseBoolean(emptyCollectionString);
      parameter = new ComponentParameter(emptyCollection ? ARRAY_ALLOW_EMPTY : CollectionComponentParameter.ARRAY);
    }
    else if (getFirstChildElement(element, false) == null) {
      parameter = new ComponentParameter();
    }
    else {
      Object instance = createInstance(pico, element);
      parameter = new ConstantParameter(instance);
    }
    return parameter;
  }

  /**
   * Use <code>ARRAY_ALLOW_EMPTY</code> as {@link Parameter}for an Array that may have no
   * elements.
   */
  private static final CollectionComponentParameter ARRAY_ALLOW_EMPTY = new CollectionComponentParameter(true);

  private void registerComponentInstance(DefaultNanoContainer container, Element element)
    throws ClassNotFoundException {
    Object instance = createInstance(container.getPico(), element);
    String key = element.getAttribute(KEY);
    String classKey = element.getAttribute(CLASS_NAME_KEY);
    if (notSet(key)) {
      if (!notSet(classKey)) {
        container.getPico().registerComponentInstance(myClassLoader.loadClass(classKey), instance);
      }
      else {
        container.getPico().registerComponentInstance(instance);
      }
    }
    else {
      container.getPico().registerComponentInstance(key, instance);
    }
  }

  private Object createInstance(PicoContainer pico, Element element) throws ClassNotFoundException {
    BeanComponentInstanceFactory factory = createComponentInstanceFactory(element.getAttribute(FACTORY));
    Element instanceElement = getFirstChildElement(element, true);
    return factory.makeInstance(pico, instanceElement, myClassLoader);
  }

  private static Element getFirstChildElement(Element parent, boolean fail) {
    NodeList children = parent.getChildNodes();
    Element child = null;
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof Element) {
        child = (Element)children.item(i);
        break;
      }
    }
    if (child == null && fail) {
      String message = parent.getNodeName() + " needs a child element";
      throw new PicoException(message);
    }
    return child;
  }

  private BeanComponentInstanceFactory createComponentInstanceFactory(String factoryClass) throws ClassNotFoundException {
    if (notSet(factoryClass)) {
      // no factory has been specified for the node
      // return globally defined factory for the container - if there is one
      factoryClass = DEFAULT_COMPONENT_INSTANCE_FACTORY;
    }

    DefaultNanoContainer adapter = new DefaultNanoContainer(myClassLoader);
    adapter.registerComponentImplementation(BeanComponentInstanceFactory.class.getName(), factoryClass);
    return (BeanComponentInstanceFactory)adapter.getPico().getComponentInstances().get(0);
  }

  private void registerComponentAdapter(DefaultNanoContainer container, Element element, DefaultNanoContainer metaContainer)
    throws ClassNotFoundException {
    String className = element.getAttribute(CLASS);
    if (notSet(className)) {
      String message = "'" + CLASS + "' attribute not specified for " + element.getNodeName();
      throw new PicoException(message);
    }
    Class implementationClass = myClassLoader.loadClass(className);
    Object key = element.getAttribute(KEY);
    String classKey = element.getAttribute(CLASS_NAME_KEY);
    if (notSet(key)) {
      if (!notSet(classKey)) {
        key = myClassLoader.loadClass(classKey);
      }
      else {
        key = implementationClass;
      }
    }
    Parameter[] parameters = createChildParameters(container, element);
    ComponentAdapterFactory componentAdapterFactory = createComponentAdapterFactory(element.getAttribute(FACTORY), metaContainer);
    container.getPico().registerComponent(componentAdapterFactory.createComponentAdapter(key, implementationClass, parameters));
  }

  private static ComponentAdapterFactory createComponentAdapterFactory(String factoryName, DefaultNanoContainer metaContainer)
    throws ClassNotFoundException {
    if (notSet(factoryName)) {
      factoryName = DEFAULT_COMPONENT_ADAPTER_FACTORY;
    }
    final Object key;
    if (metaContainer.getPico().getComponentAdapter(factoryName) != null) {
      key = factoryName;
    }
    else {
      metaContainer.registerComponentImplementation(new ClassNameKey(ComponentAdapterFactory.class.getName()), factoryName);
      key = ComponentAdapterFactory.class;
    }
    return (ComponentAdapterFactory)metaContainer.getPico().getComponentInstance(key);
  }

  private static boolean notSet(Object string) {
    return string == null || string.equals(EMPTY);
  }
}

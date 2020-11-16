/*
 *****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************
*/
package org.nanocontainer.script.xml;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.BeanPropertyComponentAdapter;
import org.picocontainer.defaults.DefaultComponentAdapterFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Properties;

/**
 * Implementation of XMLComponentInstanceFactory that uses BeanPropertyComponentAdapter
 * to create instances from DOM elements.
 *
 * @author Paul Hammant
 * @author Marcos Tarruella
 * @author Mauro Talevi
 */
public final class BeanComponentInstanceFactory {
  private static final String NAME_ATTRIBUTE = "name";

  public Object makeInstance(PicoContainer pico, Element element, ClassLoader classLoader)
    throws ClassNotFoundException {
    String className = element.getNodeName();
    Object instance;

    if (element.getChildNodes().getLength() == 1) {
      instance = BeanPropertyComponentAdapter.convert(className, element.getFirstChild().getNodeValue(), classLoader);
    }
    else {
      BeanPropertyComponentAdapter propertyComponentAdapter =
        new BeanPropertyComponentAdapter(createComponentAdapter(className, classLoader));
      Properties properties = createProperties(element.getChildNodes());
      propertyComponentAdapter.setProperties(properties);
      instance = propertyComponentAdapter.getComponentInstance(pico);
    }
    return instance;
  }

  private static ComponentAdapter createComponentAdapter(String className, ClassLoader classLoader) throws ClassNotFoundException {
    return DefaultComponentAdapterFactory.createAdapter(className, classLoader.loadClass(className), new Parameter[]{});
  }

  private static Properties createProperties(NodeList nodes) {
    Properties properties = new Properties();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node n = nodes.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        String name = n.getNodeName();

        //Provide for a new 'name' attribute in properties.
        if (n.hasAttributes()) {
          String mappedName = n.getAttributes().getNamedItem(NAME_ATTRIBUTE).getNodeValue();
          if (mappedName != null) {
            name = mappedName;
          }
        }

        String value = n.getFirstChild().getNodeValue();
        properties.setProperty(name, value);
      }
    }
    return properties;
  }
}

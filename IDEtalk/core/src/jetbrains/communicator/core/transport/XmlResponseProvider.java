// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.transport;

import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.Pico;
import org.jdom.Element;

import java.util.List;

/**
 * @author Kir
 */
public abstract class XmlResponseProvider {

  public static XmlResponseProvider getProvider(Element rootElement, EventBroadcaster eventBroadcaster) {
    //noinspection unchecked
    List<XmlResponseProvider> responseProviders = (List<XmlResponseProvider>)
      Pico.getInstance().getComponentInstancesOfType(XmlResponseProvider.class);
    for (XmlResponseProvider responseProvider : responseProviders) {
      if (responseProvider.getTagName().equals(rootElement.getName()) &&
          responseProvider.getTagNamespace().equals(rootElement.getNamespaceURI())) {
        return responseProvider;
      }
    }

    return new TextMessageEventProvider(eventBroadcaster);
  }

  /**
   * @return tagName, which is processed by this provider
   */
  public abstract String getTagName();

  /**
   * @return namespace for #getTagName
   */
  public abstract String getTagNamespace();

  /**
   * @return true if response should be sent
   */
  public abstract boolean processAndFillResponse(Element response, Element requestRoot, Transport transport, String remoteUser);
}

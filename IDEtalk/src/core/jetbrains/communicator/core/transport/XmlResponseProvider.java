/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  /** @return tagName, which is processed by this provider */
  public abstract String getTagName();

  /** @return namespace for #getTagName */
  public abstract String getTagNamespace();

  /** @return true if response should be sent */
  public abstract boolean processAndFillResponse(Element response, Element requestRoot, Transport transport, String remoteUser);
}

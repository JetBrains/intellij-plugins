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
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

/**
 * @author Kir
 */
public abstract class EventProvider extends XmlResponseProvider {
  private final EventBroadcaster myBroadcaster;

  protected EventProvider(EventBroadcaster broadcaster) {
    myBroadcaster = broadcaster;
  }

  public String getTagNamespace() {
    return Transport.NAMESPACE;
  }

  public boolean processAndFillResponse(Element response, Element requestRoot, Transport transport, String remoteUser) {
    TransportEvent event = createEvent(transport, remoteUser, requestRoot);
    String when = requestRoot.getAttributeValue(XmlMessage.WHEN_ATTR);

    if (event != null) {
      if (when == null) {
        event.setWhen(System.currentTimeMillis());
      } else {
        event.setWhen(Long.parseLong(when));
      }
      myBroadcaster.fireEvent(event);
    }
    return false;
  }

  @Nullable
  protected abstract TransportEvent createEvent(Transport transport, String remoteUser, Element rootElement);
}

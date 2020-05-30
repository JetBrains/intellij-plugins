// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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

  @Override
  public String getTagNamespace() {
    return Transport.NAMESPACE;
  }

  @Override
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

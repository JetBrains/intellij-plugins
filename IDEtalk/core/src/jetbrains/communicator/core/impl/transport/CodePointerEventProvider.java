// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.impl.transport;

import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.transport.CodePointerXmlMessage;
import jetbrains.communicator.core.transport.EventProvider;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.transport.TransportEvent;
import org.jdom.Element;

/**
 * @author Kir
 */
public class CodePointerEventProvider extends EventProvider {

  public CodePointerEventProvider(EventBroadcaster broadcaster) {
    super(broadcaster);
  }

  @Override
  public String getTagName() {
    return CodePointerXmlMessage.TAGNAME;
  }

  @Override
  protected TransportEvent createEvent(Transport transport, String remoteUser, Element rootElement) {
    return CodePointerXmlMessage.createEvent(transport, remoteUser, rootElement);
  }
}

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

  public String getTagName() {
    return CodePointerXmlMessage.TAGNAME;
  }

  protected TransportEvent createEvent(Transport transport, String remoteUser, Element rootElement) {
    return CodePointerXmlMessage.createEvent(transport, remoteUser, rootElement);
  }
}

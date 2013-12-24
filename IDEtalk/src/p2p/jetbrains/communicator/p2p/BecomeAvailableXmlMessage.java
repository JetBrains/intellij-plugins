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
package jetbrains.communicator.p2p;

import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.transport.XmlMessage;
import jetbrains.communicator.core.transport.XmlResponseProvider;
import org.jdom.Element;

/**
 * @author Kir
 */
public class BecomeAvailableXmlMessage extends XmlResponseProvider implements XmlMessage {
  @Override
  public void fillRequest(Element element) {
  }

  @Override
  public String getTagName() {
    return "becomeAvailable";
  }

  @Override
  public String getTagNamespace() {
    return Transport.NAMESPACE;
  }

  @Override
  public boolean processAndFillResponse(Element response, Element requestRoot, Transport transport, String remoteUser) {
    ((P2PTransport)transport).setAvailable(remoteUser);
    return false;
  }

  @Override
  public void processResponse(Element responseElement) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public boolean needsResponse() {
    return false;
  }
}

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
package jetbrains.communicator.p2p.commands;

import jetbrains.communicator.core.transport.XmlMessage;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.util.CommunicatorStrings;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author Kir
 */
class P2PNetworkXmlMessage extends P2PNetworkMessage {
  private static final Logger LOG = Logger.getLogger(P2PNetworkXmlMessage.class);
  private transient final XmlMessage myMessage;

  P2PNetworkXmlMessage(String str, XmlMessage message) {
    super(SendXmlMessageP2PCommand.ID, "incomingMessage", new String[]{
        CommunicatorStrings.toXMLSafeString(str),
    });
    myMessage = message;
  }

  @Override
  public boolean send(User user) {
    if (super.send(user)) {
      processResponse();
      return true;
    }
    return false;
  }

  void processResponse() {
    if (myMessage == null || !myMessage.needsResponse()) return;

    try {
      final String response = getResponse().toString();
      if (!com.intellij.openapi.util.text.StringUtil.isEmptyOrSpaces(response)) {
        Document document = new SAXBuilder().build(new StringReader(response));
        myMessage.processResponse(document.getRootElement());
      }
    } catch (JDOMException e) {
      LOG.error(e.getMessage(), e);
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
    }
  }
}

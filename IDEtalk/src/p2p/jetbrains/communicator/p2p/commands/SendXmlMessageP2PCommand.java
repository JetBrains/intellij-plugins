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

import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.dispatcher.Message;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.transport.XmlMessage;
import jetbrains.communicator.core.transport.XmlResponseProvider;
import jetbrains.communicator.util.StringUtil;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import java.io.StringReader;

/**
 * @author Kir Maximov
 *
 */
public class SendXmlMessageP2PCommand extends BaseP2PCommand {
  private static final Logger LOG = Logger.getLogger(SendXmlMessageP2PCommand.class);
  static final String ID = "SendMessage";

  public SendXmlMessageP2PCommand(EventBroadcaster eventBroadcaster, Transport transport) {
    super(ID, eventBroadcaster, transport);
  }

  public String incomingMessage(String remoteUser, String messageText) {
    String xml = StringUtil.fromXMLSafeString(messageText);
    SAXBuilder builder = new SAXBuilder();
    try {
      Document document = builder.build(new StringReader(xml));
      Element rootElement = document.getRootElement();
      Element response = createResponse(rootElement, StringUtil.fromXMLSafeString(remoteUser));
      if (response == null) return "";

      return new XMLOutputter().outputString(response);
    } catch (Throwable e) {
      LOG.info(e.getMessage(), e);
      return StringUtil.toXML(e);
    }
  }

  private Element createResponse(Element rootElement, String remoteUser) {
    Element response = new Element("response", Transport.NAMESPACE);
    XmlResponseProvider provider = XmlResponseProvider.getProvider(rootElement, myEventBroadcaster);
    if (provider.processAndFillResponse(response, rootElement, myTransport, remoteUser)) {
      return response;
    }
    return null;
  }

  public static Message createNetworkMessage(final XmlMessage message) {
    Element element = new Element(message.getTagName(), message.getTagNamespace());
    message.fillRequest(element);

    XMLOutputter outputter = new XMLOutputter();
    String str = outputter.outputString(element);
    return new P2PNetworkXmlMessage(str, message);
  }

}

// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.p2p.commands;

import com.intellij.openapi.util.JDOMUtil;
import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.dispatcher.Message;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.transport.XmlMessage;
import jetbrains.communicator.core.transport.XmlResponseProvider;
import jetbrains.communicator.util.CommunicatorStrings;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

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
    String xml = CommunicatorStrings.fromXMLSafeString(messageText);
    try {
      Element rootElement = JDOMUtil.load(xml);
      Element response = createResponse(rootElement, CommunicatorStrings.fromXMLSafeString(remoteUser));
      if (response == null) return "";
      return JDOMUtil.write(response);
    } catch (Throwable e) {
      LOG.info(e.getMessage(), e);
      return CommunicatorStrings.toXML(e);
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
    return new P2PNetworkXmlMessage(new XMLOutputter().outputString(element), message);
  }
}
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
package jetbrains.communicator.jabber.impl;

import jetbrains.communicator.core.transport.Transport;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Verifier;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author Kir
 */
public abstract class BaseExtension implements PacketExtension, PacketExtensionProvider {
  private static final Logger LOG = Logger.getLogger(BaseExtension.class);
  static final String NAMESPACE = Transport.NAMESPACE + "/jabber";

  public final String getNamespace() {
    return NAMESPACE;
  }

  public String toXML() {
    Element root = new Element(getElementName());
    setupData(root);
    root.setNamespace(Namespace.getNamespace(getNamespace()));
    XMLOutputter outputter = new XMLOutputter();
    return outputter.outputString(root);
  }

  protected abstract void setupData(Element root);

  protected abstract Object createFrom(Element element);


  public Object createFrom(String xml) {
    try {
      Document document = new SAXBuilder().build(new StringReader(removeNonXmlCharacters(xml)));
      return createFrom(document.getRootElement());

    } catch (Exception e) {
      LOG.error(e.getMessage() + "\n\n" + xml, e);
    }
    return null;
  }

  private static String removeNonXmlCharacters(String xml) {
    StringBuilder sb = null;
    for(int i = 0; i < xml.length(); i ++) {
      char c = xml.charAt(i);
      if (!Verifier.isXMLCharacter(c)) {
        if (sb == null) sb = new StringBuilder(xml);
        sb.setCharAt(i, '?');
      }
    }
    return sb != null ? sb.toString() : xml;
  }

  protected static String getContent(XmlPullParser parser, String tagName) {
    StringBuffer sb = new StringBuffer();
    try {
      int event = parser.getEventType();
      // get the content
      XMLOutputter xmlOutputter = new XMLOutputter();
      while (true) {
        switch (event) {
          case XmlPullParser.TEXT:
            sb.append(xmlOutputter.escapeElementEntities(parser.getText()));
            break;
          case XmlPullParser.START_TAG:
            sb.append('<' + parser.getName());
            int attributeCount = parser.getAttributeCount();
            for (int i = 0; i < attributeCount; i ++) {
              String attributeValue = xmlOutputter.escapeAttributeEntities(parser.getAttributeValue(i));
              sb.append(' ').append(parser.getAttributeName(i)).append('=').append('"')
                  .append(attributeValue).append('"');
            }
            sb.append(" xmlns=\"").append(parser.getNamespace()).append('"');
            sb.append('>');
            break;
          case XmlPullParser.END_TAG:
            sb.append("</" + parser.getName() + '>');
            break;
          default:
        }

        if (event == parser.END_TAG && tagName.equals(parser.getName())) break;

        event = parser.next();
      }
    } catch (XmlPullParserException e) {
      e.printStackTrace(System.err);
    } catch (IOException e) {
      e.printStackTrace(System.err);
    }
    return sb.toString();
  }

}

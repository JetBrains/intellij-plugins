// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.jabber.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.communicator.core.transport.Transport;
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
  private static final Logger LOG = Logger.getInstance(BaseExtension.class);
  static final String NAMESPACE = Transport.NAMESPACE + "/jabber";

  @Override
  public final String getNamespace() {
    return NAMESPACE;
  }

  @Override
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
    StringBuilder sb = new StringBuilder();
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
            sb.append('<').append(parser.getName());
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
            sb.append("</").append(parser.getName()).append('>');
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

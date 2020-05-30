/**
 * $RCSfile$
 * $Revision$
 * $Date$
 *
 * Copyright 2003-2004 Jive Software.
 *
 * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.smackx.provider;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.VCard;
import org.w3c.dom.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * vCard provider.
 *
 * @author Gaston Dombiak
 */
public class VCardProvider implements IQProvider {

    private final static String PREFERRED_ENCODING = "UTF-8";

  public IQ parseIQ(XmlPullParser parser) throws Exception {
      StringBuilder sb = new StringBuilder();
      try {
          int event = parser.getEventType();
          // get the content
          while (true) {
              switch (event) {
                  case XmlPullParser.TEXT:
                      // We must re-escape the xml so that the DOM won't throw an exception
                      sb.append(StringUtils.escapeForXML(parser.getText()));
                      break;
                  case XmlPullParser.START_TAG:
                      sb.append('<').append(parser.getName()).append('>');
                      break;
                  case XmlPullParser.END_TAG:
                      sb.append("</").append(parser.getName()).append('>');
                      break;
                  default:
              }

              if (event == XmlPullParser.END_TAG && "vCard".equals(parser.getName())) break;

              event = parser.next();
          }
      } catch (XmlPullParserException e) {
          e.printStackTrace();
      } catch (IOException e) {
          e.printStackTrace();
      }

      String xmlText = sb.toString();
      return _createVCardFromXml(xmlText);
  }

    public static VCard _createVCardFromXml(String xmlText) {
      VCard vCard = new VCard();
      try {
          DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
          DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
          Document document = documentBuilder.parse(
                  new ByteArrayInputStream(xmlText.getBytes(PREFERRED_ENCODING)));

          new VCardReader(vCard, document).initializeFields();

      } catch (Exception e) {
          e.printStackTrace(System.err);
      }
      return vCard;
  }

    private static class VCardReader {
        private final VCard vCard;
        private final Document document;

        VCardReader(VCard vCard, Document document) {
            this.vCard = vCard;
            this.document = document;
        }

        public void initializeFields() {
            vCard.setFirstName(getTagContents("GIVEN"));
            vCard.setLastName(getTagContents("FAMILY"));
            vCard.setMiddleName(getTagContents("MIDDLE"));
            vCard.setEncodedImage(getTagContents("BINVAL"));

            setupEmails();

            vCard.setOrganization(getTagContents("ORGNAME"));
            vCard.setOrganizationUnit(getTagContents("ORGUNIT"));

            setupSimpleFields();

            setupPhones();
            setupAddresses();
        }

        private void setupEmails() {
            NodeList nodes = document.getElementsByTagName("USERID");
            if (nodes == null) return;
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                if ("HOME".equals(element.getParentNode().getFirstChild().getNodeName())) {
                    vCard.setEmailHome(getTextContent(element));
                } else {
                    vCard.setEmailWork(getTextContent(element));
                }
            }
        }

        private void setupPhones() {
            NodeList allPhones = document.getElementsByTagName("TEL");
            if (allPhones == null) return;
            for (int i = 0; i < allPhones.getLength(); i++) {
                NodeList nodes = allPhones.item(i).getChildNodes();
                String type = null;
                String code = null;
                String value = null;
                for (int j = 0; j < nodes.getLength(); j++) {
                    Node node = nodes.item(j);
                    if (node.getNodeType() != Node.ELEMENT_NODE) continue;
                    String nodeName = node.getNodeName();
                    if ("NUMBER".equals(nodeName)) {
                        value = getTextContent(node);
                    }
                    else if (isWorkHome(nodeName)) {
                        type = nodeName;
                    }
                    else {
                        code = nodeName;
                    }
                }
                if (code == null || value == null) continue;
                if ("HOME".equals(type)) {
                        vCard.setPhoneHome(code, value);
                    }
                else { // By default, setup work phone
                    vCard.setPhoneWork(code, value);
                }
            }
        }

        private boolean isWorkHome(String nodeName) {
            return "HOME".equals(nodeName) || "WORK".equals(nodeName);
        }

        private void setupAddresses() {
            NodeList allAddresses = document.getElementsByTagName("ADR");
            if (allAddresses == null) return;
            for (int i = 0; i < allAddresses.getLength(); i++) {
                Element addressNode = (Element) allAddresses.item(i);

                String type = null;
                List code = new ArrayList();
                List value = new ArrayList();
                NodeList childNodes = addressNode.getChildNodes();
                for(int j = 0; j < childNodes.getLength(); j++) {
                    Node node = childNodes.item(j);
                    if (node.getNodeType() != Node.ELEMENT_NODE) continue;
                    String nodeName = node.getNodeName();
                    if (isWorkHome(nodeName)) {
                        type = nodeName;
                            }
                            else {
                        code.add(nodeName);
                        value.add(getTextContent(node));
                            }
                        }
                for (int j = 0; j < value.size(); j++) {
                    if ("HOME".equals(type)) {
                        vCard.setAddressFieldHome((String) code.get(j), (String) value.get(j));
                    }
                    else { // By default, setup work address
                        vCard.setAddressFieldWork((String) code.get(j), (String) value.get(j));
                    }
                }
            }
        }

        private String getTagContents(String tag) {
            NodeList nodes = document.getElementsByTagName(tag);
            if (nodes != null && nodes.getLength() == 1) {
                return getTextContent(nodes.item(0));
            }
            return null;
        }

        private void setupSimpleFields() {
            NodeList childNodes = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    Element element = (Element) node;

                    String field = element.getNodeName();
                    if (element.getChildNodes().getLength() == 0) {
                        vCard.setField(field, "");
                    } else if (element.getChildNodes().getLength() == 1 &&
                            element.getChildNodes().item(0) instanceof Text) {
                        vCard.setField(field, getTextContent(element));
                    }
                }
            }
        }

        private String getTextContent(Node node) {
            StringBuilder result = new StringBuilder();
            appendText(result, node);
            return result.toString();
        }

        private void appendText(StringBuilder result, Node node) {
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node nd = childNodes.item(i);
                String nodeValue = nd.getNodeValue();
                if (nodeValue != null) {
                    result.append(nodeValue);
                }
                appendText(result, nd);
            }
        }
    }
}

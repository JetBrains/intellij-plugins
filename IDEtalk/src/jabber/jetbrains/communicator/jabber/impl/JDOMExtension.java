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

import org.jdom.Element;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author Kir
 */
public class JDOMExtension extends BaseExtension {

  public static final String ELEMENT_NAME = "IDEtalk-data";

  public static void init() {
    ProviderManager.addExtensionProvider(ELEMENT_NAME, NAMESPACE, new JDOMExtension());
  }

  private Element myElement;

  public JDOMExtension() {
  }

  public JDOMExtension(Element file) {
    myElement = file;
  }

  public Element getElement() {
    return myElement;
  }

  public String getElementName() {
    return ELEMENT_NAME;
  }

  protected void setupData(Element root) {
    myElement.detach();
    root.addContent(myElement);
  }

  public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
    String content = getContent(parser, getElementName());
    return new JDOMExtension((Element) createFrom(content));
  }

  protected Object createFrom(Element element) {
    return element.getChildren().get(0);
  }
}

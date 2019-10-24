// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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

  @Override
  public String getElementName() {
    return ELEMENT_NAME;
  }

  @Override
  protected void setupData(Element root) {
    myElement.detach();
    root.addContent(myElement);
  }

  @Override
  public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
    String content = getContent(parser, getElementName());
    return new JDOMExtension((Element) createFrom(content));
  }

  @Override
  protected Object createFrom(Element element) {
    return element.getChildren().get(0);
  }
}

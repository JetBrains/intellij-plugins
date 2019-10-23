// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.transport;

import org.jdom.Element;

/**
 * @author Kir
 */
public class TextXmlMessage implements XmlMessage {
  public static final String TAG = "message";
  private final String myText;

  public TextXmlMessage(String text) {
    myText = text;
  }

  @Override
  public String getTagName() {
    return TAG;
  }

  @Override
  public String getTagNamespace() {
    return Transport.NAMESPACE;
  }

  @Override
  public boolean needsResponse() {
    return false;
  }

  @Override
  public void fillRequest(Element element) {
    element.setText(myText);
    element.setAttribute(WHEN_ATTR, String.valueOf(System.currentTimeMillis()));
  }

  @Override
  public void processResponse(Element responseElement) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }
}

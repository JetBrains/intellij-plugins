// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.transport;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.communicator.core.EventBroadcaster;
import org.jdom.Element;

/**
 * @author Kir
 */
public class TextMessageEventProvider extends EventProvider {

  public TextMessageEventProvider(EventBroadcaster broadcaster) {
    super(broadcaster);
  }

  @Override
  public String getTagName() {
    return TextXmlMessage.TAG;
  }

  @Override
  protected TransportEvent createEvent(Transport transport, String remoteUser, Element rootElement) {
    if (!StringUtil.isEmptyOrSpaces(rootElement.getText())) {
      return EventFactory.createMessageEvent(transport, remoteUser, rootElement.getText());
    }
    return null;
  }
}

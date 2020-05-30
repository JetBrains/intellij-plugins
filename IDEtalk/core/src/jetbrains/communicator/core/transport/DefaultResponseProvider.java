// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.transport;

import jetbrains.communicator.core.EventBroadcaster;
import org.jdom.Element;

/**
 * @author Kir
 */
public class DefaultResponseProvider extends XmlResponseProvider {
  private final EventBroadcaster myBroadcaster;

  public DefaultResponseProvider(EventBroadcaster broadcaster) {
    assert broadcaster != null;
    myBroadcaster = broadcaster;
  }

  @Override
  public String getTagName() {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public String getTagNamespace() {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public boolean processAndFillResponse(Element response, Element requestRoot, Transport transport, String remoteUser) {
    return false;
  }
}

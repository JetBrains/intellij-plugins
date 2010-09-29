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
package jetbrains.communicator.p2p;

import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.transport.XmlResponseProvider;
import org.jdom.Element;

/**
 * @author Kir
 */
public class MockXmlResponseProvider extends XmlResponseProvider {
  private final String myTag;
  private final String myNamespace;

  public MockXmlResponseProvider(String tag, String namespace, EventBroadcaster eventBroadcaster) {
    myTag = tag;
    myNamespace = namespace;
  }

  @Override
  public String getTagName() {
    return myTag;
  }

  @Override
  public String getTagNamespace() {
    return myNamespace;
  }

  @Override
  public boolean processAndFillResponse(Element response, Element requestRoot, Transport transport, String remoteUser) {
    return true;
  }
}

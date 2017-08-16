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
package jetbrains.communicator.p2p.commands;

import jetbrains.communicator.p2p.MockXmlMessage;
import junit.framework.TestCase;
import org.jdom.Element;

/**
 * @author Kir
 */
public class P2PNetworkXmlMessageTest extends TestCase {
  public void testProcessResponse() {

    final boolean[] processed = new boolean[1];
    MockXmlMessage xmlMessage = new MockXmlMessage(){
      @Override
      public void processResponse(Element responseElement) {
        assertEquals("fee", responseElement.getName());
        processed[0] = true;
      }
    };
    xmlMessage.setShouldWaitForResponse(true);

    P2PNetworkXmlMessage p2PNetworkXmlMessage = new P2PNetworkXmlMessage("str", xmlMessage);

    p2PNetworkXmlMessage.setResponse("<fee/>");
    p2PNetworkXmlMessage.processResponse();

    assertTrue("Xml Response should be processed", processed[0]);

    processed[0] = false;
    xmlMessage.setShouldWaitForResponse(false);
    p2PNetworkXmlMessage.processResponse();

    assertFalse("Xml Response should not be processed", processed[0]);
  }

  public void testNullXmlMessage() {
    P2PNetworkXmlMessage p2PNetworkXmlMessage = new P2PNetworkXmlMessage("str", null);
    p2PNetworkXmlMessage.processResponse();
  }
}

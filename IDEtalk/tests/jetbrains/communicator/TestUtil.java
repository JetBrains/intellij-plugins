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
package jetbrains.communicator;

import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.IDEtalkAdapter;
import jetbrains.communicator.core.IDEtalkEvent;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.transport.CodePointerEvent;
import jetbrains.communicator.core.transport.MessageEvent;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.CodePointer;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.p2p.MockXmlMessage;
import jetbrains.communicator.p2p.MockXmlResponseProvider;
import jetbrains.communicator.util.WaitFor;
import junit.framework.Assert;
import org.jdom.Element;
import org.picocontainer.Disposable;

/**
 * @author Kir
 */
public class TestUtil extends Assert{
  private TestUtil() {
  }

  public static void testSendCodePointer_Functional(BaseTestCase testCase, User self) {
    final String[] log = new String[]{""};
    testCase.getBroadcaster().addListener(new IDEtalkAdapter(){
      @Override
      public void afterChange(IDEtalkEvent event) {
        event.accept(new EventVisitor(){
          @Override public void visitCodePointerEvent(CodePointerEvent event) {
            log[0] += event.getRemoteUser()+ ' ';
            log[0] += event.getComment();
            log[0] += event.getCodePointer().toString();
            log[0] += event.getFile().toString();
          }
        });
      }
    });
    testCase.markLastListenerForCleanup();

    log[0] = "";

    CodePointer pointer = new CodePointer(0, 1);
    VFile file = VFile.create("path");

    self.sendCodeIntervalPointer(file, pointer, "comment���< && 53", testCase.getBroadcaster());

    new WaitFor(2000) {
      @Override
      protected boolean condition() {
        return log[0].length() > 0;
      }
    };

    Assert.assertEquals("Code Pointer expected", self.getName() + " comment���< && 53" + pointer + file, log[0]);
  }

  public static void testSendMessage_Functional(BaseTestCase testCase, User self) {
    final String[] log = new String[]{""};
    final long[] whenSent = new long[1];
    testCase.getBroadcaster().addListener(new IDEtalkAdapter() {
      @Override
      public void afterChange(IDEtalkEvent event) {
        event.accept(new EventVisitor() {
          @Override public void visitMessageEvent(MessageEvent event) {
            super.visitMessageEvent(event);
            log[0] += event.getRemoteUser() + ' ' + event.getMessage();
            if (event.getWhen().getTime() - whenSent[0] < 150) {
              log[0] += " timingOK";
            }
            else {
              log[0] += (event.getWhen().getTime() - whenSent[0]);
            }
          }
        });
      }
    });
    testCase.markLastListenerForCleanup();

    log[0] = "";

    String comment = "SS��� messa&&ge";
    whenSent[0] = System.currentTimeMillis();
    self.sendMessage(comment, testCase.getBroadcaster());

    new WaitFor(500) {
      @Override
      protected boolean condition() {
        return log[0].length() > 0;
      }
    };

    assertEquals("Message expected", self.getName() + ' ' +comment + " timingOK", log[0]);
  }

  public static void testSendXmlMessage_Functional(BaseTestCase testCase, User self, final boolean checkResponse) {
    final String[] log = new String[]{""};
    log[0] = "";


    final String comment = "��� mes&&<>sage";
    MockXmlMessage message = new MockXmlMessage("tagName", "myNamespace") {
      @Override
      public boolean needsResponse() {
        return checkResponse;
      }

      @Override
      public void fillRequest(Element element) {
        element.setText(comment);
      }

      @Override
      public void processResponse(Element responseElement) {
        log[0] += responseElement.getAttributeValue("foo");
      }
    };

    final MockXmlResponseProvider mockXmlResponseProvider = new MockXmlResponseProvider("tagName", "myNamespace", testCase.getBroadcaster()) {
      @Override
      public boolean processAndFillResponse(Element response, Element request, Transport transport, String remoteUser) {
        log[0] += remoteUser + " " + request.getName() + " " + request.getText();
        assertEquals("root element expected", "tagName", request.getName());
        response.setAttribute("foo", "gar");
        return true;
      }
    };
    Pico.getInstance().registerComponentInstance(mockXmlResponseProvider);
    testCase.disposeOnTearDown(new Disposable(){
      @Override
      public void dispose() {
        Pico.getInstance().unregisterComponentByInstance(mockXmlResponseProvider);
      }
    });

    self.sendXmlMessage(message);

    new WaitFor(500) {
      @Override
      protected boolean condition() {
        if (checkResponse) {
          return log[0].endsWith("gar");
        }
        else {
          return log[0].length() > 0;
        }
      }
    };

    String expectedLog = self.getName() + " tagName " + comment;
    expectedLog += checkResponse ? "gar" : "";
    assertEquals("Message expected", expectedLog, log[0]);
  }
}

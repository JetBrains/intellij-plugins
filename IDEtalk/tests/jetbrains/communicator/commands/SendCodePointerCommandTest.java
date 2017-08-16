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
package jetbrains.communicator.commands;

import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.users.UserImpl;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.CodePointer;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.SendCodePointerEvent;
import jetbrains.communicator.mock.MockTransport;
import jetbrains.communicator.mock.MockUser;
import org.jmock.Mock;

/**
 * @author kir
 */
public class SendCodePointerCommandTest extends BaseTestCase {
  private SendCodePointerCommand myCommand;
  private Mock myFacadeMock;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myFacadeMock = mock(IDEFacade.class);
    myCommand = new SendCodePointerCommand((IDEFacade) myFacadeMock.proxy(), getBroadcaster());
  }

  public void testEnabled() {
    assertFalse(myCommand.isEnabled());

    myCommand.setCodePointer(new CodePointer(0,0));
    assertFalse(myCommand.isEnabled());

    myCommand.setVFile(VFile.create("a path"));
    assertFalse(myCommand.isEnabled());

    myCommand.setUser(new MockUser());
    assertTrue("Now codePointer, file, user are selected - ready to send", myCommand.isEnabled());
  }

  public void testExecute_WithMessage() {
    final CodePointer codePointerToSend = new CodePointer(0, 0);
    final VFile fileToSend = VFile.create("a path");

    final boolean [] sent = new boolean[1];
    MockUser user = new MockUser() {
      @Override
      public void sendCodeIntervalPointer(VFile file, CodePointer pointer, String comment, EventBroadcaster eventBroadcaster) {
        sent[0] = true;
        assertSame(fileToSend, file);
        assertSame(codePointerToSend, pointer);
        assertEquals("some message", comment);
      }
    };

    myCommand.setCodePointer(codePointerToSend);
    myCommand.setVFile(fileToSend);
    myCommand.setUser(user);

    myFacadeMock.expects(once()).method("getMessage").will(returnValue("some message"));
    myFacadeMock.expects(once()).method("fillFileContents").with(eq(fileToSend));

    myCommand.execute();
    assertTrue("Should call sendCodeIntervalPointer", sent[0]);
  }

  public void testExecute_LocalMessage() {
    addEventListener();

    final CodePointer codePointerToSend = new CodePointer(0, 0);
    final VFile fileToSend = VFile.create("a path");
    User user = UserImpl.create("user", MockTransport.NAME);

    myCommand.setCodePointer(codePointerToSend);
    myCommand.setVFile(fileToSend);
    myCommand.setUser(user);

    myFacadeMock.expects(once()).method("getMessage").will(returnValue("some message"));
    myFacadeMock.expects(once()).method("fillFileContents").with(eq(fileToSend));

    myCommand.execute();

    verifySendMessageLocalEvent(user, "some message");

    assertSame(fileToSend, ((SendCodePointerEvent) myEvents.get(0)).getFile());
    assertSame(codePointerToSend, ((SendCodePointerEvent) myEvents.get(0)).getCodePointer());
  }

  public void testExecute_Cancel() {

    final boolean [] sent = new boolean[1];
    MockUser user = new MockUser() {
      @Override
      public void sendCodeIntervalPointer(VFile file, CodePointer pointer, String comment, EventBroadcaster eventBroadcaster) {
        sent[0] = true;
      }
    };

    myCommand.setCodePointer(new CodePointer(0, 0));
    myCommand.setVFile(VFile.create("a path"));
    myCommand.setUser(user);

    myFacadeMock.expects(once()).method("getMessage").will(returnValue(null));

    myCommand.execute();
    assertFalse("Should call sendCodeIntervalPointer", sent[0]);
  }

}

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

import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.users.UserModelImpl;
import jetbrains.communicator.jabber.JabberFacade;
import jetbrains.communicator.jabber.JabberUI;
import org.jmock.Mock;

import java.util.Arrays;

/**
 * @author Kir
 */
public class FindByJabberIdCommandTest extends BaseTestCase {
  private FindByJabberIdCommand myCommand;
  private Mock myJabberFacade;
  private Mock myJabberUI;
  private UserModelImpl myUserModel;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myJabberFacade = mock(JabberFacade.class);
    myJabberUI = mock(JabberUI.class);
    myUserModel = new UserModelImpl(getBroadcaster());
    disposeOnTearDown(myUserModel);

    myCommand = new FindByJabberIdCommand((JabberFacade) myJabberFacade.proxy(), (JabberUI)myJabberUI.proxy(),
        myUserModel);
    assertNotNull(myCommand.getName());
    assertTrue(myCommand.isEnabled());
  }

  public void testNotConnected() {
    setupConnection(false);
    myCommand.execute();
  }

  public void testNotEnteredId() {
    //noinspection SSBasedInspection
    myJabberUI.expects(once()).method("getFindByIdData").with(eq(Arrays.asList()))
        .will(returnValue("someGroup:  "));
    setupConnection(true);

    myCommand.execute();
  }

  public void testWithAddress() {
    myUserModel.addGroup("bbb");
    myUserModel.addGroup("aaa");

    myJabberUI.expects(once()).method("getFindByIdData").with(eq(Arrays.asList("aaa", "bbb")))
        .will(returnValue("someGroup:kir@localhost, \nsashka@localhost"));
    setupConnection(true);

    myJabberFacade.expects(once()).method("addUsers").with(eq("someGroup"),eq(
        Arrays.asList("kir@localhost", "sashka@localhost")));

    myCommand.execute();
  }
  
  private void setupConnection(boolean successful) {
    myJabberUI.expects(once()).method("connectAndLogin").will(returnValue(successful));
  }

}

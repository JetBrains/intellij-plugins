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

import jetbrains.communicator.LightTestCase;
import jetbrains.communicator.core.dispatcher.LocalMessage;
import jetbrains.communicator.core.dispatcher.LocalMessageDispatcher;
import jetbrains.communicator.core.impl.users.UserImpl;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.mock.MockMessage;
import jetbrains.communicator.mock.MockTransport;
import jetbrains.communicator.util.CommunicatorStrings;
import org.jmock.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Kir
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class SearchHistoryCommandTest extends LightTestCase {
  private SearchHistoryCommand myCommand;
  private Mock myDispatcherMock;
  private Mock myIdeMock;
  private User myUser;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myDispatcherMock = mock(LocalMessageDispatcher.class);
    myIdeMock = mock(IDEFacade.class);

    myCommand = new SearchHistoryCommand(
        (LocalMessageDispatcher) myDispatcherMock.proxy(),
        (IDEFacade)myIdeMock.proxy());

    myUser = UserImpl.create("user", MockTransport.NAME);
    myCommand.setUser(myUser);
  }

  public void testEnabled() {
    myDispatcherMock.expects(once()).method("isHistoryEmpty").will(returnValue(true));
    assertFalse(myCommand.isEnabled());

    myDispatcherMock.expects(atLeastOnce()).method("isHistoryEmpty").will(returnValue(false));
    assertTrue(myCommand.isEnabled());

    myCommand.setUser(null);
    assertFalse("No user set - should disable", myCommand.isEnabled());
  }

  public void testSearchHistory_Empty() {
    query(null);

    myCommand.execute();
  }

  public void testSearchHistory_NoResults() {
    query("");

    myDispatcherMock.expects(once()).method("getHistory").with(eq(myUser), eq(null)).will(returnValue(new LocalMessage[0]));

    myIdeMock.expects(once()).method("showMessage").with(
        eq(CommunicatorStrings.getMsg("SearchHistoryCommand.search.history", myUser.getDisplayName())),
        eq(CommunicatorStrings.getMsg("SearchHistoryCommand.no.results"))
        );

    myCommand.execute();
  }

  public void testSearchHistory_WithResult() {
    MockMessage m1 = new MockMessage(new Date(), "some text1");
    MockMessage m2 = new MockMessage(new Date(), "some text2");
    MockMessage m3 = new MockMessage(new Date(), "some text2 ataing");

    query("text2");
    myDispatcherMock.expects(once()).method("getHistory").with(eq(myUser), eq(null)).will(
        returnValue(new LocalMessage[]{m1, m2, m3}));

    myIdeMock.expects(once()).method("showSearchHistoryResults").with(
        eq(new ArrayList<LocalMessage>(Arrays.asList(m2, m3))),
        eq(myUser)
    );

    myCommand.execute();
  }

  private void query(String s) {
    myIdeMock.expects(once()).method("getMessage").with(
        eq(CommunicatorStrings.getMsg("SearchHistoryCommand.enter.query.string")),
        eq(CommunicatorStrings.getMsg("SearchHistoryCommand.search.history", myUser.getDisplayName())),
        eq(CommunicatorStrings.getMsg("search"))
    ).will(returnValue(s));
  }

}

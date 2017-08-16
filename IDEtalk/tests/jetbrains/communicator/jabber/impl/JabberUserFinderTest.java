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
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.NullProgressIndicator;
import jetbrains.communicator.mock.MockIDEFacade;
import org.jmock.Mock;

import java.io.IOException;
import java.net.URL;

/**
 * @author Kir
 */
public class JabberUserFinderTest extends BaseTestCase {
  private MockIDEFacade myFacade;
  private JabberUserFinderImpl myUserFinder;
  private UserModelImpl myUserModel;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFacade = new MockIDEFacade(getClass());
    myFacade.setReturnedProjectId("33333");
    myUserModel = new UserModelImpl(getBroadcaster());
    disposeOnTearDown(myUserModel);
    myUserFinder = new JabberUserFinderImpl(myFacade, myUserModel);


  }

  public void testRegisterAndFindUser() throws Throwable {
    URL url = new URL(JabberUserFinderImpl.TEST_URL);
    try {
      url.getContent();

    } catch (IOException e) {
      System.out.println("WARNING: " + JabberUserFinderImpl.TEST_URL + " not available");
      return;
    }

    myUserFinder.registerForProject("kir@localhost");
    Thread.sleep(400);
    User[] users = myUserFinder.findUsers(new NullProgressIndicator());
    assertEquals(1, users.length);
    assertEquals("kir@localhost", users[0].getName());

  }

  public void testAskForRegister_AnswerYes() {
    Mock facadeMock = mock(IDEFacade.class);

    final String[] params = new String[2];
    myUserFinder = new JabberUserFinderImpl((IDEFacade) facadeMock.proxy(), myUserModel) {
      @Override
      protected void doRegister(final String jabberUserId, final String currentProjectId) {
        params[0] = jabberUserId;
        params[1] = currentProjectId;
      }
    };

    set_project(facadeMock, "id1", true);
    myUserFinder.registerForProject("kir@fff");
    assertEquals("kir@fff", params[0]);
    assertEquals("id1", params[1]);

    params[0] = params[1] = null;

    set_project(facadeMock, "id1", false);
    myUserFinder.registerForProject("kir@fff");
    assertNull("No questions expected now - same user and project", params[0]);

    set_project(facadeMock, "id1", true);
    myUserFinder.registerForProject("kir@fff1");
    assertEquals("Another user, expect question", "kir@fff1", params[0]);

    params[0] = params[1] = null;

    set_project(facadeMock, "id2", true);
    myUserFinder.registerForProject("kir@fff1");
    assertEquals("Another project, expect question", "kir@fff1", params[0]);
  }

  public void testAskForRegister_AnswerNo() {
    Mock facadeMock = mock(IDEFacade.class);

    final String[] params = new String[2];
    myUserFinder = new JabberUserFinderImpl((IDEFacade) facadeMock.proxy(), myUserModel) {
      @Override
      protected void doRegister(final String jabberUserId, final String currentProjectId) {
        params[0] = jabberUserId;
        params[1] = currentProjectId;
      }
    };

    facadeMock.expects(atLeastOnce()).method("getCacheDir").will(returnValue(myFacade.getCacheDir()));
    facadeMock.expects(once()).method("getCurrentProjectId").will(returnValue("id1"));
    facadeMock.expects(once()).method("askQuestion").will(returnValue(false));
    myUserFinder.registerForProject("kir@fff");
    assertNull(params[0]);

  }

  private void set_project(Mock facadeMock, String projectId, boolean shouldAskQuestion) {
    facadeMock.expects(atLeastOnce()).method("getCacheDir").will(returnValue(myFacade.getCacheDir()));
    facadeMock.expects(once()).method("getCurrentProjectId").will(returnValue(projectId));
    if (shouldAskQuestion) {
      facadeMock.expects(once()).method("askQuestion").will(returnValue(true));
    }
  }
}

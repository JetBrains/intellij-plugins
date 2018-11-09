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
package jetbrains.communicator.core.impl.transport;

import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.users.UserModelImpl;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.mock.MockTransport;
import org.jdom.Element;
import org.jmock.Mock;

/**
 * @author Kir
 */
public class FileAccessProviderTest extends BaseTestCase {
  private boolean myProcessed;
  private UserModelImpl myUserModel;
  private IDEFacade myIdeFacade;
  private User myUser;
  private MyProvider myProvider;
  private Mock myMock;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myUserModel = new UserModelImpl(getBroadcaster());
    myMock = new Mock(IDEFacade.class);
    disposeOnTearDown(myUserModel);
    myIdeFacade = (IDEFacade) myMock.proxy();

    myUser = myUserModel.createUser("User", MockTransport.NAME);
    myProvider = new MyProvider(myIdeFacade, myUserModel);
  }

  public void testGoodUser() {
    myUserModel.addUser(myUser);
    myUser.setCanAccessMyFiles(true, myUserModel);

    processRequest();

    assertTrue("Good user - should be processed", myProcessed);
  }

  private void processRequest() {
    myProvider.processAndFillResponse(new Element("a"), new Element("a"), new MockTransport(), myUser.getName());
  }

  public void testNotFromModel() {
    processRequest();

    assertFalse("User not from model", myProcessed);
  }

  public void testUserWithoutRights() {
    myUserModel.addUser(myUser);
    myUser.setCanAccessMyFiles(false, myUserModel);
    myMock.expects(once()).method("askQuestion").will(returnValue(false));

    processRequest();
    assertFalse("User not from model", myProcessed);
    assertFalse(myUser.canAccessMyFiles());
  }

  public void testUserWithoutRights_Allow() {
    myUserModel.addUser(myUser);
    myUser.setCanAccessMyFiles(false, myUserModel);
    myMock.expects(once()).method("askQuestion").will(returnValue(true));

    processRequest();
    assertTrue(myProcessed);
    assertTrue(myUser.canAccessMyFiles());
  }
  
  private class MyProvider extends FileAccessProvider {

    MyProvider(IDEFacade ideFacade, UserModel userModel) {
      super(ideFacade, userModel);
    }

    @Override
    protected void doProcess(Element request, Element response) {
      myProcessed = true;
    }

    @Override
    public String getTagName() {
      throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
    }
  }
}

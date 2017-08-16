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

import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.users.BaseUserImpl;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.mock.MockIDEFacade;
import jetbrains.communicator.mock.MockUser;
import org.jmock.Mock;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kir
 */
public class FindUsersCommandTest extends BaseTestCase {
  private FindUsersCommand myCommand;

  private Mock myUserModelMock;
  private Mock myTransportMock;
  private Mock myTransportMock1;
  private MockIDEFacade myIDEFacade;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myUserModelMock = mock(UserModel.class);
    myTransportMock = mock(Transport.class);
    myTransportMock1 = mock(Transport.class);
    myIDEFacade = new MockIDEFacade();

    myUserModelMock.stubs().method("getBroadcaster").will(returnValue(getBroadcaster()));
    myCommand = new FindUsersCommand(
        (UserModel) myUserModelMock.proxy(),
        new Transport[]{(Transport) myTransportMock.proxy(), (Transport) myTransportMock1.proxy()},
        myIDEFacade);


    assertTrue(myCommand.isEnabled());
    assertNotNull(myCommand.getName());
  }

  public void testNoUsersFound() {
    myTransportMock.expects(once()).method("findUsers").will(returnValue(new User[0]));
    myTransportMock1.expects(once()).method("findUsers").will(returnValue(new User[0]));

    myCommand.execute();
    assertTrue(myIDEFacade.getAndClearLog().startsWith("showMessage"));
  }

  public void testOnlySelfFound() {

    final BaseUserImpl self = new MockUser();

    myTransportMock.expects(once()).method("findUsers").will(returnValue(new User[]{self}));
    myTransportMock1.expects(once()).method("findUsers").will(returnValue(new User[]{self}));

    myCommand.execute();

    assertTrue(myIDEFacade.getAndClearLog().startsWith("showMessage"));
  }

  public void testOnlyExistingUserFound() {

    final BaseUserImpl foundUser = setTeddyUserIsFound();

    myUserModelMock.expects(once()).method("hasUser").with(eq(foundUser)).will(returnValue(true));

    myCommand.execute();

    assertTrue(myIDEFacade.getAndClearLog().startsWith("showMessage"));
  }

  public void testUsersFromDifferentTransportsFound() {
    final BaseUserImpl teddy = new MockUser("teddy", "");
    final BaseUserImpl sashka = new MockUser("sashka", "");

    myTransportMock.expects(once()).method("findUsers").will(returnValue(new User[]{teddy}));
    myTransportMock1.expects(once()).method("findUsers").will(returnValue(new User[]{sashka}));

    myUserModelMock.expects(once()).method("getGroups").will(returnValue(null));
    myUserModelMock.expects(once()).method("hasUser").with(eq(teddy)).will(returnValue(false));
    myUserModelMock.expects(once()).method("hasUser").with(eq(sashka)).will(returnValue(false));

    myCommand.execute();
  }

  public void testNewUserFound_AddNoOne() {
    final BaseUserImpl teddy = setTeddyUserIsFound();
    myUserModelMock.expects(once()).method("hasUser").with(eq(teddy)).will(returnValue(false));
    myUserModelMock.expects(once()).method("getGroups").will(returnValue(null));

    myIDEFacade.setReturnedData(new FindUsersCommand.UsersInfo(new User[0], "new group"));
    myCommand.execute();

    assertEquals("chooseUsersToBeAdded", myIDEFacade.getAndClearLog());
  }

  public void testSetGroupBasingOnSelection() {
    final MockUser teddy = setTeddyUserIsFound();

    myUserModelMock.expects(once()).method("hasUser").with(eq(teddy)).will(returnValue(false));
    myUserModelMock.expects(once()).method("getGroups").will(returnValue(null));

    myIDEFacade.setReturnedData(new FindUsersCommand.UsersInfo(new User[]{teddy}, UserModel.AUTO_GROUP));
    teddy.setProjects(new String[]{"projectForTeddy"});

    myUserModelMock.expects(once()).method("findUser").with(eq(teddy.getName()), eq(teddy.getTransportCode()))
        .will(returnValue(teddy));
    myUserModelMock.expects(once()).method("addUser").with(eq(teddy));
    myCommand.execute();

    assertEquals("Group should be set from project name", "projectForTeddy", teddy.getGroup());
  }

  public void testNewUserFound_AddThisUserToModel() {
    final BaseUserImpl teddy = setTeddyUserIsFound();
    myUserModelMock.expects(once()).method("hasUser").with(eq(teddy)).will(returnValue(false));
    myUserModelMock.expects(once()).method("getGroups").will(returnValue(null));

    final List<User> userList = new ArrayList<>();
    userList.add(teddy);

    myIDEFacade.setReturnedData(new FindUsersCommand.UsersInfo(new User[]{teddy}, "new group"));

    myUserModelMock.expects(once()).method("addUser").with(eq(teddy));
    myUserModelMock.expects(once()).method("findUser").with(eq(teddy.getName()), eq(teddy.getTransportCode()))
        .will(returnValue(teddy));

    myCommand.execute();

    assertEquals("User group should be updated", "new group", teddy.getGroup());
  }


  private MockUser setTeddyUserIsFound() {
    final MockUser teddy = new MockUser("teddy", "");

    myTransportMock.expects(once()).method("findUsers").will(returnValue(new User[]{teddy}));
    myTransportMock1.expects(once()).method("findUsers").will(returnValue(new User[0]));
    return teddy;
  }

}

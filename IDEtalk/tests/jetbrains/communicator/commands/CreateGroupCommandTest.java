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

import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.IDEFacade;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * @author Kir
 */
public class CreateGroupCommandTest extends MockObjectTestCase {
  private CreateGroupCommand myCommand;
  private Mock myUserListMock;
  private Mock myIDEFacade;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myUserListMock = mock(UserModel.class);
    myIDEFacade = mock(IDEFacade.class);
    myCommand = new CreateGroupCommand((UserModel)myUserListMock.proxy(), (IDEFacade)myIDEFacade.proxy());
  }

  public void testCreateGroup() {
    myIDEFacade.expects(once()).method("getMessageLine").will(returnValue("group name"));
    myUserListMock.expects(once()).method("addGroup").with(eq("group name"));

    myCommand.execute();
  }
}

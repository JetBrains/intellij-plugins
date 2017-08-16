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
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.ProjectsData;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.mock.MockUser;
import jetbrains.communicator.mock.MockUserListComponent;
import org.jmock.Mock;

/**
 * @author kir
 */
public class ViewFilesCommandTest extends BaseTestCase {
  private MockUserListComponent myMockUserListComponent;
  private Mock myFacadeMock;
  private ViewFilesCommand myCommand;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myMockUserListComponent = new MockUserListComponent();
    myFacadeMock = mock(IDEFacade.class);

    myCommand = new ViewFilesCommand(myMockUserListComponent,
        (IDEFacade) myFacadeMock.proxy());
  }

  public void testIsEnabled() {
    assertFalse(myCommand.isEnabled());

    myMockUserListComponent.setSelectedNodes(new Object[]{"group"});
    assertFalse(myCommand.isEnabled());

    MockUser mockUser = new MockUser("user", null);
    myMockUserListComponent.setSelectedNodes(new User[]{mockUser});
    assertFalse("User is offline", myCommand.isEnabled());
    mockUser.setOnline(true);

    mockUser.setIDEtalkUser(false);
    assertFalse("Single user selected and online but not IDEtalk user- command should be disabled",
        myCommand.isEnabled());

    mockUser.setIDEtalkUser(true);
    assertTrue("Single user selected and online and IDEtalk user- command should be disabled",
        myCommand.isEnabled());

    myMockUserListComponent.setSelectedNodes(new User[]{mockUser, mockUser});
    assertFalse("Two users are selected", myCommand.isEnabled());
  }

  public void testExecute_NoInformation() {
    final ProjectsData projectsData = new ProjectsData();
    MockUser mockUser = new MockUser("user", null) {
      @Override
      public ProjectsData getProjectsData(IDEFacade ideFacade) {
        return projectsData;
      }
    };

    mockUser.setOnline(true);
    myMockUserListComponent.setSelectedNodes(new User[]{mockUser});
    myFacadeMock.expects(once()).method("showMessage");

    myCommand.execute();
  }

  public void testExecute_WithInformation() {
    final ProjectsData projectsData = new ProjectsData();

    projectsData.addNonProjectFile(VFile.create("a path"));

    MockUser mockUser = new MockUser("user", null) {
      @Override
      public ProjectsData getProjectsData(IDEFacade ideFacade) {
        return projectsData;
      }
    };

    mockUser.setOnline(true);
    myMockUserListComponent.setSelectedNodes(new User[]{mockUser});
    myFacadeMock.expects(once()).method("showUserFiles").with(eq(mockUser), eq(projectsData));

    myCommand.execute();
  }

}

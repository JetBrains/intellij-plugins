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
package jetbrains.communicator.idea.viewFiles;

import com.intellij.openapi.actionSystem.AnActionEvent;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.vfs.ProjectsData;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.mock.MockUser;
import org.jmock.Mock;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Kir
 */
public class OpenFileActionTest extends BaseTestCase {
  private ViewFilesPanel myViewFilesPanel;
  private MockUser myUser;
  private OpenFileAction myOpenFileAction;
  private Mock myIdeFacadeMock;
  private VFile myVFile;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myIdeFacadeMock = mock(IDEFacade.class);
    Pico.getInstance().registerComponentInstance(IDEFacade.class, myIdeFacadeMock.proxy());

    myViewFilesPanel = new ViewFilesPanel(null, null, (IDEFacade) myIdeFacadeMock.proxy());
    myUser = new MockUser();

    ProjectsData projectsData = new ProjectsData();
    myVFile = VFile.create("build.xml");
    projectsData.setProjectFiles("ideTalk",
        new VFile[]{
          myVFile,
              VFile.create("log4j.xml"),
        });
    projectsData.addNonProjectFile(VFile.create("non-projectFile"));

    myViewFilesPanel.refreshData(myUser, projectsData);

    myOpenFileAction = new OpenFileAction(myViewFilesPanel.getTree(),
        (IDEFacade) myIdeFacadeMock.proxy());
  }

  public void testEnabled() throws Exception {
    assertEnabled("Nothing selected", false);
  }

  public void testProjectSelected() throws Exception {
    myViewFilesPanel.getTree().setSelectionRow(0);
    assertEnabled("Project node is selected, unable to open", false);
  }

  public void test2FilesSelected() throws Exception {
    myViewFilesPanel.getTree().setSelectionRows(new int[]{1, 2});
    assertEnabled("Several nodes are selected, cannot be opened", false);
  }

  public void testFileSelected_CannotOpen() throws Exception {
    myViewFilesPanel.getTree().setSelectionRow(1);
    myIdeFacadeMock.expects(once()).method("hasFile").with(eq(myVFile)).will(returnValue(false));
    assertEnabled("File node is selected, but cannot be opened", false);
  }

  public void testFileSelected_CanOpen() throws Exception {
    myViewFilesPanel.getTree().setSelectionRow(1);
    myIdeFacadeMock.expects(once()).method("hasFile").with(eq(myVFile)).will(returnValue(true));
    assertEnabled("File node is selected, but cannot be opened", true);
  }

  public void testOpen() throws Exception {
    myViewFilesPanel.getTree().setSelectionRow(1);
    myIdeFacadeMock.expects(once()).method("open").with(eq(myVFile));

    myOpenFileAction.actionPerformed(createActionEvent(myOpenFileAction.getTemplatePresentation()));
  }

  private void assertEnabled(String msg, boolean enabled) throws IllegalAccessException, InvocationTargetException, InstantiationException {
    AnActionEvent e = createActionEvent(myOpenFileAction.getTemplatePresentation());
    myOpenFileAction.update(e);

    assertEquals(msg, enabled, e.getPresentation().isEnabled());
  }


}

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

import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.vfs.ProjectsData;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.mock.MockIDEFacade;
import jetbrains.communicator.mock.MockUser;

import javax.swing.*;

/**
 * @author Kir
 */
public class ViewFilesTest extends BaseTestCase {
  private ViewFilesPanel myViewFilesPanel;
  private MockUser myUser;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    Pico.getInstance().registerComponentInstance(IDEFacade.class, new MockIDEFacade());

    myViewFilesPanel = new ViewFilesPanel(null, null, null);
    myUser = new MockUser();
  }

  public void testTree_OneFileInProject() {
    ProjectsData projectsData = new ProjectsData();
    projectsData.setProjectFiles("ideTalk",
        new VFile[]{
              VFile.create("build.xml")
        });

    myViewFilesPanel.refreshData(myUser, projectsData);
    assertTree(new String[]{
      "ideTalk",
      "build.xml",
    });
  }

  public void testTree_FileNotInProject() {
    ProjectsData projectsData = new ProjectsData();
    projectsData.addNonProjectFile(VFile.create("log4j.xml"));

    myViewFilesPanel.refreshData(myUser, projectsData);
    assertTree(new String[]{
      ViewFilesPanel.NON_PROJECT_NODE,
      "log4j.xml",
    });
  }

  public void testTree_ProjectAndNonProjectFiles() {
    ProjectsData projectsData = new ProjectsData();
    projectsData.addNonProjectFile(VFile.create("log4j.xml"));
    projectsData.setProjectFiles("ideTalk",
        new VFile[]{
              VFile.create("build.xml")
        });


    myViewFilesPanel.refreshData(myUser, projectsData);
    assertTree(new String[]{
      "ideTalk",
      "build.xml",
      ViewFilesPanel.NON_PROJECT_NODE,
      "log4j.xml",
    });
  }

  public void testTree_SortProjects() {
    ProjectsData projectsData = new ProjectsData();
    projectsData.setProjectFiles("aaa", new VFile[0]);
    projectsData.setProjectFiles("ccc", new VFile[0]);
    projectsData.setProjectFiles("bbb", new VFile[0]);

    myViewFilesPanel.refreshData(myUser, projectsData);
    assertTree(new String[]{
      "aaa",
      "bbb",
      "ccc",
    });
  }

  public void testTree_SortFilesInProject() {
    ProjectsData projectsData = new ProjectsData();
    projectsData.setProjectFiles("ideTalk", new VFile[]{
              VFile.create("aaa"),
              VFile.create("ccc"),
              VFile.create("bbb"),
    });

    myViewFilesPanel.refreshData(myUser, projectsData);
    assertTree(new String[]{
      "ideTalk",
      "aaa",
      "bbb",
      "ccc",
    });
  }

  public void testHideReadOnly() {

    myViewFilesPanel.showReadOnly(true);

    ProjectsData projectsData = new ProjectsData();
    projectsData.setProjectFiles("ideTalk", new VFile[]{
              VFile.create("aaa", true),
              VFile.create("bbb", false),
    });

    myViewFilesPanel.refreshData(myUser, projectsData);
    assertTree(new String[]{
      "ideTalk",
      "aaa",
      "bbb",
    });

    myViewFilesPanel.showReadOnly(false);
    assertFalse(myViewFilesPanel.isReadOnlyShown());
    assertEquals(myViewFilesPanel.isReadOnlyShown(), myOptions.isSet(ViewFilesPanel.SHOW_READ_ONLY_KEY, true));

    assertTree(new String[]{
      "ideTalk",
      "aaa",
    });
  }

  public void testHideReadOnly_FromPrefs() {

    boolean roState = myOptions.isSet(ViewFilesPanel.SHOW_READ_ONLY_KEY, true);
    assertEquals(roState, myViewFilesPanel.isReadOnlyShown());

    myOptions.setOption(ViewFilesPanel.SHOW_READ_ONLY_KEY, !roState);
    ViewFilesPanel viewFilesPanel = new ViewFilesPanel(null, null, null);
    assertEquals(!roState, viewFilesPanel.isReadOnlyShown());

    myViewFilesPanel.showReadOnly(roState);

  }

  private JTree getTree() {
    return myViewFilesPanel.getTree();
  }

  private void assertTree(String[] nodes) {
    assertEquals("wrong number of nodes", nodes.length, getTree().getRowCount());
    for (int i = 0; i < nodes.length; i++) {
      assertEquals("wrong node " + i, nodes[i],
          getTree().getPathForRow(i).getLastPathComponent().toString());
    }
  }


}

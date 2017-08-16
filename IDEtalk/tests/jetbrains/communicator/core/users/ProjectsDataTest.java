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
package jetbrains.communicator.core.users;

import jetbrains.communicator.core.vfs.ProjectsData;
import jetbrains.communicator.core.vfs.VFile;
import junit.framework.TestCase;

/**
 * @author kir
 */
public class ProjectsDataTest extends TestCase {
  ProjectsData myProjectsData;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myProjectsData = new ProjectsData();
  }

  public void testProjectFiles() {

    myProjectsData.setProjectFiles("project1", new VFile[] {
              VFile.create("path/to/file", true),
              VFile.create("path/to/file1", false),
    });

    assertEquals(0, myProjectsData.getProjectFiles("fff").length);
    assertEquals(0, myProjectsData.getProjectFiles("project2").length);
    VFile[] projectFiles = myProjectsData.getProjectFiles("project1");
    assertEquals(2, projectFiles.length);

    assertEquals("path/to/file", projectFiles[0].getContentPath());
    assertTrue(projectFiles[0].isWritable());

    assertEquals("path/to/file1", projectFiles[1].getContentPath());
    assertFalse(projectFiles[1].isWritable());


    myProjectsData.setProjectFiles("project1", new VFile[0]);
    assertEquals(0, myProjectsData.getProjectFiles("project1").length);

  }

  public void testSerializeDeserialize() {
    myProjectsData.setProjectFiles("project1", new VFile[]{
              VFile.create("a path")
    });
    myProjectsData.setProjectFiles("project2", new VFile[0]);

    ProjectsData userStatus1 = new ProjectsData(myProjectsData.serialize());

    assertEquals(2, userStatus1.getProjects().length);
    assertEquals(1, userStatus1.getProjectFiles("project1").length);
  }

  public void testNonProjectFile() {
    myProjectsData.addNonProjectFile(VFile.create("a path"));
    myProjectsData.addNonProjectFile(VFile.create("a path2"));

    assertEquals(0, myProjectsData.getProjects().length);
    assertEquals(2, myProjectsData.getNonProjectFiles().length);
    assertEquals("a path", myProjectsData.getNonProjectFiles()[0].getContentPath());
    assertEquals("a path2", myProjectsData.getNonProjectFiles()[1].getContentPath());
  }

  public void testProjectNameInFileInfo() {

    VFile fileInfo = VFile.create("Path");
    myProjectsData.setProjectFiles(VFile.PROJECT_NAME_ATTR, new VFile[]{fileInfo});
    assertEquals(VFile.PROJECT_NAME_ATTR, fileInfo.getProjectName());

    VFile fileInfo1 = VFile.create("Path");
    myProjectsData.addNonProjectFile(fileInfo1);
    assertNull(fileInfo1.getProjectName(), fileInfo1.getProjectName());

    VFile vFile = myProjectsData.getProjectFiles(VFile.PROJECT_NAME_ATTR)[0];
    assertEquals("Should be equal", fileInfo, vFile);
    assertEquals("Project name shoud be kept", VFile.PROJECT_NAME_ATTR, vFile.getProjectName());

    VFile vFile1 = myProjectsData.getNonProjectFiles()[0];
    assertEquals("Should be equal", fileInfo1, vFile1);
    assertNull("Project name shoud be kept", vFile1.getProjectName());
  }
}

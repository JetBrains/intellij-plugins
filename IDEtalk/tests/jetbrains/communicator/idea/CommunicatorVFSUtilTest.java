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
package jetbrains.communicator.idea;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.HeavyPlatformTestCase;
import jetbrains.communicator.core.vfs.VFile;
import junit.framework.AssertionFailedError;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kir
 */
public class CommunicatorVFSUtilTest extends HeavyPlatformTestCase {
  private VirtualFile myContentRoot;
  private VirtualFile mySourceRoot;
  private ContentEntry myContentEntry;
  private final Set<File> myFilesToDelete = new HashSet<>();

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    final File dir = createTempDirectory();
    updateRoots(new Updater() {
      @Override
      public void update(ModifiableRootModel modifiableModel) {
        myContentRoot = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(dir);
        try {
          VirtualFile src = myContentRoot.createChildDirectory(this, "src");
          myContentEntry = modifiableModel.addContentEntry(myContentRoot);
          myContentEntry.addSourceFolder(src, false);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    });

    ProjectRootManager rootManager = ProjectRootManager.getInstance(getProject());
    myContentRoot = rootManager.getContentRoots()[0];
    mySourceRoot = rootManager.getContentSourceRoots()[0];

    VFSUtil._setProject(getProject());
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      for (File file : myFilesToDelete) {
        FileUtil.delete(file);
      }
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  @Override
  protected void runTest() {
    ApplicationManager.getApplication().runWriteAction(() -> {
      try {
        super.runTest();
      }
      catch (AssertionFailedError e) {
        throw e;
      }
      catch (Throwable throwable) {
        throw new RuntimeException(throwable);
      }
    });
  }

  public void testNoContentRoot() throws Exception {
    File dir = createTempDirectory();
    VirtualFile someDir = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(dir);

    VirtualFile file = someDir.createChildData(this, "some.file");

    VFile vFile = VFSUtil.createFileFrom(file, null);

    assertNull("Wrong content path", vFile.getContentPath());
    assertEquals("Wrong full path", file.getPath(), vFile.getFullPath());
    assertNull("Should NOT restore Virtual File NOT FROM CONTENT ROOT: ", VFSUtil.getVirtualFile(vFile));
  }

  public void testInContentRoot() throws Exception {
    VirtualFile file = myContentRoot.createChildData(this, "some.file");

    checkVFile(file, getProject(), "/some.file", file.getPath());
    checkVFile(file, null, "/some.file", file.getPath());
  }

  public void testInSourceRoot() throws Exception {
    VirtualFile file = mySourceRoot.createChildData(this, "some.file");

    VFile vFile = checkVFile(file, getProject(), "/src/some.file", file.getPath());
    assertEquals("Wrong src path", "/some.file", vFile.getSourcePath());

    // Change our source path:
    final VirtualFile newSrc = myContentRoot.createChildDirectory(this, "new_src");
    updateRoots(new Updater() {
      @Override
      public void update(ModifiableRootModel modifiableModel) {
        myContentEntry = modifiableModel.getContentEntries()[0];
        myContentEntry.removeSourceFolder(myContentEntry.getSourceFolders()[0]);
        myContentEntry.addSourceFolder(newSrc, false);
      }
    });
    file.delete(this);
    VirtualFile childData = newSrc.createChildData(this, "some.file");
    VirtualFile virtualFile = VFSUtil.getVirtualFile(vFile);

    assertEquals("Should find source file basing on src path", childData, virtualFile);
  }

  private void updateRoots(final Updater updater) {
    ApplicationManager.getApplication().runWriteAction(() -> {
      try {
        ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(getModule()).getModifiableModel();
        updater.update(modifiableModel);
        modifiableModel.commit();
      }
      catch (Throwable e) {
        throw new RuntimeException(e);
      }
    });
  }

  private interface Updater {
    void update(ModifiableRootModel modifiableModel);
  }

  private VFile checkVFile(VirtualFile file, Project project, String expectedPath, String expectedFullPath) {
    VFile vFile = VFSUtil.createFileFrom(file, project);

    if (project != null) {
      assertEquals("Wrong project", project.getName(), vFile.getProjectName());
    }
    else {
      assertNull("No project expected", vFile.getProjectName());
    }

    assertEquals("Wrong content path", expectedPath, vFile.getContentPath());
    assertEquals("Wrong full path", expectedFullPath, vFile.getFullPath());
    assertEquals("Should restore Virtual File from " + vFile,
        file, VFSUtil.getVirtualFile(vFile));

    return vFile;
  }
}

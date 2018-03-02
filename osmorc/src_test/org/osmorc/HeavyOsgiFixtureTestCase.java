/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package org.osmorc;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.project.ProjectKt;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.JavaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.FileFilter;

import static org.junit.Assert.assertNotNull;

public abstract class HeavyOsgiFixtureTestCase {
  protected TempDirTestFixture myTempDirFixture;
  protected IdeaProjectTestFixture myFixture;

  @Before
  public void setUp() throws Exception {
    myTempDirFixture = IdeaTestFixtureFactory.getFixtureFactory().createTempDirTestFixture();
    myTempDirFixture.setUp();
    myFixture = JavaTestFixtureFactory.createFixtureBuilder("OSGi Tests").getFixture();
    myFixture.setUp();
    Project project = myFixture.getProject();
    // load module components
    ProjectKt.getStateStore(project).setOptimiseTestLoadSpeed(false);
    loadModules(getClass().getSimpleName(), project, myTempDirFixture.getTempDirPath());
  }

  @After
  public void tearDown() throws Exception {
    myFixture.tearDown();
    myTempDirFixture.tearDown();
  }

  private static final FileFilter VISIBLE_DIR_FILTER = pathname -> pathname.isDirectory() && !pathname.getName().startsWith(".");

  private static void loadModules(String projectName, final Project project, String projectDirPath) throws Exception {
    final File projectDir = OsgiTestUtil.extractProject(projectName, projectDirPath);
    WriteAction.runAndWait(() -> {
      VirtualFile virtualDir = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(projectDir);
      assertNotNull(projectDir.getPath(), virtualDir);
      for (File moduleDir : projectDir.listFiles(VISIBLE_DIR_FILTER)) {
        File moduleFile = new File(moduleDir, moduleDir.getName() + ".iml");
        if (moduleFile.exists()) {
          LocalFileSystem.getInstance().refreshAndFindFileByIoFile(moduleFile);
          ModuleManager.getInstance(project).loadModule(moduleFile.getPath());
        }
      }
    });
  }
}

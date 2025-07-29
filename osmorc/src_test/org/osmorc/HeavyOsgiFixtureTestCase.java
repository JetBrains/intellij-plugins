// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.osmorc;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.NioFiles;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.project.ProjectKt;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.JavaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import org.junit.After;
import org.junit.Before;

import java.nio.file.Files;
import java.nio.file.Path;

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
    var project = myFixture.getProject();
    ProjectKt.getStateStore(project).setOptimiseTestLoadSpeed(false);
    loadModules(getClass().getSimpleName(), project, Path.of(myTempDirFixture.getTempDirPath()));
  }

  @After
  public void tearDown() throws Exception {
    myFixture.tearDown();
    myTempDirFixture.tearDown();
  }

  private static void loadModules(String projectName, Project project, Path projectDir) throws Exception {
    OsgiTestUtil.extractProject(projectName, projectDir);
    WriteAction.runAndWait(() -> {
      var virtualDir = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(projectDir);
      assertNotNull(projectDir.toString(), virtualDir);
      for (var moduleDir : NioFiles.list(projectDir)) {
        var moduleFile = moduleDir.resolve(moduleDir.getFileName() + ".iml");
        if (Files.exists(moduleFile)) {
          LocalFileSystem.getInstance().refreshAndFindFileByNioFile(moduleFile);
          ModuleManager.getInstance(project).loadModule(moduleFile);
        }
      }
    });
  }
}

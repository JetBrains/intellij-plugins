/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc;

import com.intellij.facet.FacetManager;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.IdeaTestCase;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.JavaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.util.io.ZipUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;
import org.osmorc.facet.OsmorcFacet;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class TestUtil {
  private static final FileFilter VISIBLE_DIR_FILTER = new FileFilter() {
    public boolean accept(File pathname) {
      return pathname.isDirectory() && !pathname.getName().startsWith(".");
    }
  };

  public static IdeaProjectTestFixture createTestFixture() {
    IdeaTestCase.initPlatformPrefix();
    TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = JavaTestFixtureFactory.createFixtureBuilder("Osmorc Tests");
    return fixtureBuilder.getFixture();
  }

  public static File extractProject(String projectName, String projectDirPath) throws IOException {
    File projectZIP = new File(getTestDataDir(), projectName + ".zip");
    assert projectZIP.exists() : projectZIP.getAbsoluteFile() + " not found";
    assert !projectZIP.isDirectory() : projectZIP.getAbsolutePath() + " is a directory";

    File projectDir = new File(projectDirPath);
    ZipUtil.extract(projectZIP, projectDir, null);
    return projectDir;
  }

  public static void loadModules(String projectName, final Project project, String projectDirPath) throws Exception {
    final File projectDir = extractProject(projectName, projectDirPath);
    new WriteAction() {
      @Override
      protected void run(@NotNull Result result) throws Throwable {
        VirtualFile virtualDir = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(projectDir);
        assertNotNull("Directory not found: " + projectDir, virtualDir);
        virtualDir.refresh(false, true);

        for (File moduleDir : projectDir.listFiles(VISIBLE_DIR_FILTER)) {
          String moduleDirPath = moduleDir.getPath().replace(File.separatorChar, '/') + "/";
          String moduleFileName = moduleDirPath + moduleDir.getName() + ".iml";
          if (new File(moduleFileName).exists()) {
            LocalFileSystem.getInstance().refreshAndFindFileByPath(moduleFileName);
            Module module = ModuleManager.getInstance(project).loadModule(moduleFileName);
            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(moduleDirPath);
            assertNotNull(moduleDirPath, file);
            PsiTestUtil.addContentRoot(module, file);
            PsiTestUtil.addSourceRoot(module, file.findChild("src"));
          }
        }
      }
    }.execute().throwException();
  }

  public static void createOsmorcFacetForAllModules(final Project project) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        final Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
          final ModifiableFacetModel modifiableFacetModel = FacetManager.getInstance(module).createModifiableModel();
          final OsmorcFacet facet = new OsmorcFacet(module);
          facet.getConfiguration().setUseProjectDefaultManifestFileLocation(false);
          facet.getConfiguration().setManifestLocation("META-INF/MANIFEST.MF");
          facet.getConfiguration().setManifestGenerationMode(ManifestGenerationMode.Manually);
          modifiableFacetModel.addFacet(facet);
          modifiableFacetModel.commit();
        }
      }
    });
  }

  public static PsiFile loadPsiFile(Project project, String moduleName, String filePathInSource) {
    final ModuleRootManager rootManager = getModuleRootManager(project, moduleName);
    final VirtualFile root = rootManager.getSourceRoots()[0];
    VirtualFile file = root.findFileByRelativePath(filePathInSource);
    assertNotNull(file);
    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    assertNotNull(psiFile);
    return psiFile;
  }

  public static PsiFile loadPsiFileUnderContent(Project project, String moduleName, String filePathInContent) {
    final ModuleRootManager rootManager = getModuleRootManager(project, moduleName);
    VirtualFile root = rootManager.getContentRoots()[0];
    VirtualFile file = root.findFileByRelativePath(filePathInContent);
    assertNotNull(file);
    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    assertNotNull(psiFile);
    return psiFile;
  }

  public static ModuleRootManager getModuleRootManager(Project project, String moduleName) {
    ModuleManager moduleManager = ModuleManager.getInstance(project);
    Module module = moduleManager.findModuleByName(moduleName);
    assertNotNull(module);
    return ModuleRootManager.getInstance(module);
  }

  private static File getTestDataDir() {
    if (TEST_DATA_DIR == null) {
      TEST_DATA_DIR = new File(TestUtil.class.getResource("/").getFile(), "../../../testdata");
      if (!TEST_DATA_DIR.exists()) {
        TEST_DATA_DIR = new File(TestUtil.class.getResource("").getFile(), "../../../../../testdata");
      }
      if (!TEST_DATA_DIR.exists()) {
        TEST_DATA_DIR = new File(PathManager.getHomePath(), "contrib/osmorc/testdata");
      }
      assert TEST_DATA_DIR.exists();
      assert TEST_DATA_DIR.isDirectory();
    }

    return TEST_DATA_DIR;
  }

  private static File TEST_DATA_DIR;
}

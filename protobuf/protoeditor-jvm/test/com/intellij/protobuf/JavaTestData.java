/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture;

import java.io.File;

import static com.intellij.protobuf.TestUtils.notNull;

/** Utilities to load some testdata for the java integration tests. */
public class JavaTestData {

  // The default java package declared in the testdata's .java files.
  private static final String USER_TEST_PACKAGE = "testdata.java";
  private static final String USER_TEST_PACKAGE_DIR =
      USER_TEST_PACKAGE.replace('.', File.separatorChar) + File.separatorChar;

  static VirtualFile copyJavaProtoUser(JavaCodeInsightTestFixture fixture, File javaFile) {
    String baseName = javaFile.getName();
    return fixture.copyFileToProject(javaFile.getPath(), USER_TEST_PACKAGE_DIR + baseName);
  }

  static void addGenCodeJar(Module module, String root, String jarName, Disposable testDisposable) {
    VfsRootAccess.allowRootAccess(testDisposable, root);
    VirtualFile libFile = notNull(VfsUtil.findFileByIoFile(new File(root, jarName), false));
    VirtualFile jarRoot = notNull(JarFileSystem.getInstance().getRootByLocal(libFile));
    Library library =
        PsiTestUtil.addProjectLibrary(
            module,
            FileUtil.getNameWithoutExtension(jarRoot.getName()),
            ImmutableList.of(jarRoot),
            ImmutableList.of());
    Disposer.register(testDisposable, () -> removeLibrary(module.getProject(), library));
  }

  private static void removeLibrary(Project project, Library library) {
    ApplicationManager.getApplication()
        .runWriteAction(() -> LibraryTablesRegistrar.getInstance().getLibraryTable(project).removeLibrary(library));
  }
}

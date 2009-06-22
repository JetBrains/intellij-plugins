/*
 * Copyright 2007 The authors
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

package com.intellij.struts2.facet.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Detects the Struts2 version from attached struts-2core.jar.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsVersionDetector {

  private StrutsVersionDetector() {
  }

  @Nullable
  public static String detectStrutsVersion(@NotNull final Module module) {
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false);
    final JavaPsiFacade psiManager = JavaPsiFacade.getInstance(module.getProject());
    try {
      final ZipFile zipFile = getStrutsJar(scope, psiManager);
      if (zipFile == null) {
        return null;
      }
      final ZipEntry zipEntry = zipFile.getEntry("META-INF/maven/org.apache.struts/struts2-core/pom.properties");
      if (zipEntry == null) {
        return null;
      }
      final InputStream inputStream = zipFile.getInputStream(zipEntry);
      final Properties properties = new Properties();
      properties.load(inputStream);
      return properties.getProperty("version");
    }
    catch (IOException e) {
      return null;
    }
  }

  @Nullable
  private static VirtualFile getStrutsClass(final GlobalSearchScope scope, final JavaPsiFacade psiManager) {
    final PsiClass psiClass = psiManager.findClass("org.apache.struts2.StrutsConstants", scope);
    if (psiClass == null) {
      return null;
    }
    final PsiFile psiFile = psiClass.getContainingFile();
    if (psiFile == null) {
      return null;
    }
    return psiFile.getVirtualFile();
  }

  @Nullable
  private static ZipFile getStrutsJar(final GlobalSearchScope scope, final JavaPsiFacade psiManager) throws IOException {
    final VirtualFile virtualFile = getStrutsClass(scope, psiManager);
    if (virtualFile == null || !(virtualFile.getFileSystem() instanceof JarFileSystem)) {
      return null;
    }
    return JarFileSystem.getInstance().getJarFile(virtualFile);
  }

}
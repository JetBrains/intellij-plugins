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
package org.osmorc.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.ManifestFileType;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.HeaderValue;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.jetbrains.lang.manifest.psi.Section;
import org.osgi.framework.BundleActivator;
import org.osmorc.facet.OsmorcFacet;

import java.util.List;

import static com.intellij.openapi.util.text.StringUtil.trimTrailing;

public class OsgiPsiUtil {
  private OsgiPsiUtil() { }

  public static boolean isActivator(@Nullable PsiElement element) {
    if (element instanceof PsiClass) {
      PsiClass psiClass = (PsiClass)element;
      if (!psiClass.hasModifierProperty(PsiModifier.ABSTRACT) && OsmorcFacet.getInstance(psiClass) != null) {
        PsiClass activator = getActivatorClass(psiClass.getProject());
        if (activator != null && psiClass.isInheritor(activator, true)) {
          return true;
        }
      }
    }

    return false;
  }

  @Nullable
  public static PsiClass getActivatorClass(@NotNull final Project project) {
    return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
      GlobalSearchScope scope = ProjectScope.getLibrariesScope(project);
      PsiClass aClass = JavaPsiFacade.getInstance(project).findClass(BundleActivator.class.getName(), scope);
      return CachedValueProvider.Result.create(aClass, PsiModificationTracker.MODIFICATION_COUNT);
    });
  }

  public static PsiDirectory @NotNull [] resolvePackage(@NotNull PsiElement element, @NotNull String packageName) {
    Project project = element.getProject();
    Module module = ModuleUtilCore.findModuleForPsiElement(element);
    GlobalSearchScope scope = module != null ? module.getModuleWithDependenciesAndLibrariesScope(false) : ProjectScope.getAllScope(project);
    PsiPackage aPackage = JavaPsiFacade.getInstance(project).findPackage(packageName);
    return aPackage == null ? PsiDirectory.EMPTY_ARRAY : aPackage.getDirectories(scope);
  }

  public static boolean isHeader(@Nullable PsiElement element, @NotNull String headerName) {
    return element instanceof Header && headerName.equals(((Header)element).getName());
  }

  public static void setHeader(@NotNull ManifestFile manifestFile, @NotNull String headerName, @NotNull String headerValue) {
    Header header = manifestFile.getHeader(headerName);
    Header newHeader = createHeader(manifestFile.getProject(), headerName, headerValue);
    if (header != null) {
      header.replace(newHeader);
    }
    else {
      addHeader(manifestFile, newHeader);
    }
  }

  public static void appendToHeader(@NotNull ManifestFile manifestFile, @NotNull String headerName, @NotNull String headerValue) {
    Header header = manifestFile.getHeader(headerName);
    if (header != null) {
      HeaderValue oldValue = header.getHeaderValue();
      if (oldValue != null) {
        String oldText = trimTrailing(header.getText().substring(oldValue.getStartOffsetInParent(), header.getTextLength()));
        if (!oldText.isEmpty()) oldText += ",\n ";
        headerValue = oldText + headerValue;
      }
      header.replace(createHeader(manifestFile.getProject(), headerName, headerValue));
    }
    else {
      addHeader(manifestFile, createHeader(manifestFile.getProject(), headerName, headerValue));
    }
  }

  private static Header createHeader(Project project, String headerName, String valueText) {
    String text = String.format("%s: %s\n", headerName, valueText);
    PsiFile file = PsiFileFactory.getInstance(project).createFileFromText("DUMMY.MF", ManifestFileType.INSTANCE, text);
    Header header = ((ManifestFile)file).getHeader(headerName);
    if (header == null) {
      throw new IncorrectOperationException("Bad header: '" + text + "'");
    }
    return header;
  }

  private static void addHeader(ManifestFile manifestFile, Header newHeader) {
    Section section = manifestFile.getMainSection();
    List<Header> headers = manifestFile.getHeaders();
    if (section == null) {
      manifestFile.add(newHeader.getParent());
    }
    else if (headers.isEmpty()) {
      section.addBefore(newHeader, section.getFirstChild());
    }
    else {
      section.addAfter(newHeader, headers.get(headers.size() - 1));
    }
  }
}

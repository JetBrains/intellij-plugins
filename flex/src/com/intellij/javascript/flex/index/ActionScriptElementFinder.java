// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.index;

import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.index.JSIndexKeys;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.xml.XmlFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class ActionScriptElementFinder {
  /** AS only. Finds static elements which can be imported. */
  public static Collection<JSQualifiedNamedElement> findElementsByName(final String name,
                                                                       final Project project,
                                                                       final GlobalSearchScope scope) {
    return findElementsByName(name, project, scope, true);
  }

  /** AS only. Finds static elements which can be imported. */
  public static Collection<JSQualifiedNamedElement> findElementsByName(final String name,
                                                                       final Project project,
                                                                       final GlobalSearchScope scope,
                                                                       final boolean resultAsNavigationElements) {
    final Set<JSQualifiedNamedElement> result = new HashSet<>();
    Collection<JSQualifiedNamedElement> jsQualifiedNamedElements = StubIndex.getElements(JSIndexKeys.JS_NAME_INDEX_KEY, name, project, scope,
                                                                                         JSQualifiedNamedElement.class);
    ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();

    for (JSQualifiedNamedElement e : jsQualifiedNamedElements) {
      VirtualFile fileOrDir = e.getContainingFile().getVirtualFile();

      if (projectFileIndex.isInLibrarySource(fileOrDir)) {
        if (ApplicationManager.getApplication().isUnitTestMode() &&  // TODO: update this once test flex sdk will be in swc
            !projectFileIndex.isInLibraryClasses(fileOrDir)) {
          continue;
        }
      } else if (!projectFileIndex.isInSource(fileOrDir) &&
                 !projectFileIndex.isInLibraryClasses(fileOrDir) &&
                 e.getContainingFile().getLanguage() == FlexSupportLoader.ECMA_SCRIPT_L4) {
        continue;
      }

      if (resultAsNavigationElements) {
        final PsiElement navElement = e.getNavigationElement();
        if (navElement instanceof JSQualifiedNamedElement) {
          result.add((JSQualifiedNamedElement)navElement);
        }
        else {
          result.add(e);
        }
      }
      else {
        result.add(e);
      }
    }

    Collection<VirtualFile> files =
      new ArrayList<>(FilenameIndex.getVirtualFilesByName(name + FlexSupportLoader.MXML_FILE_EXTENSION_DOT, scope));

    for(final VirtualFile file : files) {
      if (!file.isValid() || !projectFileIndex.isInSource(file)) continue;
      final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
      if (psiFile instanceof XmlFile) {
        result.add(XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)psiFile));
      }
    }
    return result;
  }
}

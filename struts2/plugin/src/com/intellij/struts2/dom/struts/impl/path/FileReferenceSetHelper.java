/*
 * Copyright 2011 The authors
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

package com.intellij.struts2.dom.struts.impl.path;

import com.intellij.javaee.web.WebDirectoryElement;
import com.intellij.javaee.web.WebRoot;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.javaee.web.psi.jsp.WebDirectoryUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Helper methods for {@link StrutsResultContributor}s using {@link FileReferenceSet}.
 *
 * @author Yann C&eacute;bron
 */
public final class FileReferenceSetHelper {

  private FileReferenceSetHelper() {
  }

  /**
   * Creates a new FileReferenceSet allowing references only to (web-)directories and given FileType.
   *
   * @param psiElement      Current element.
   * @param allowedFileType Allowed filetype for resolving.
   * @return Instance.
   */
  public static FileReferenceSet createRestrictedByFileType(final PsiElement psiElement,
                                                            @NotNull FileType allowedFileType) {
    return new FileReferenceSet(psiElement) {

      @Override
      protected boolean isSoft() {
        return true;
      }

      @Override
      protected Condition<PsiFileSystemItem> getReferenceCompletionFilter() {
        return psiFileSystemItem -> {
          if (psiFileSystemItem instanceof PsiDirectory ||
              psiFileSystemItem instanceof WebDirectoryElement) {
            return true;
          }

          final VirtualFile virtualFile = psiFileSystemItem.getVirtualFile();
          return virtualFile != null && FileTypeRegistry.getInstance().isFileOfType(virtualFile, allowedFileType);
        };
      }
    };
  }

  /**
   * Adds all {@link WebDirectoryElement}s as well as web-directory with name of given current namespace
   * (if not "root" namespace) as possible content roots.
   *
   * @param psiElement Current element.
   * @param namespace  Current namespace.
   * @param webFacet   Module.
   * @param set        FRS to patch.
   */
  public static void addWebDirectoryAndCurrentNamespaceAsRoots(final PsiElement psiElement,
                                                               final String namespace,
                                                               final WebFacet webFacet,
                                                               final FileReferenceSet set) {
    final WebDirectoryUtil directoryUtil = WebDirectoryUtil.getWebDirectoryUtil(psiElement.getProject());
    set.addCustomization(
        FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION,
        file -> {
          final List<PsiFileSystemItem> basePathRoots = new ArrayList<>();

          // 1. add all configured web root mappings
          final List<WebRoot> webRoots = webFacet.getWebRoots(true);
          for (final WebRoot webRoot : webRoots) {
            final String webRootPath = webRoot.getRelativePath();
            final WebDirectoryElement webRootBase =
                directoryUtil.findWebDirectoryElementByPath(webRootPath, webFacet);
            ContainerUtil.addIfNotNull(basePathRoots, webRootBase);
          }

          // 2. add parent <package> "namespace" as result prefix directory path if not ROOT
          if (!Objects.equals(namespace, StrutsPackage.DEFAULT_NAMESPACE)) {
            final WebDirectoryElement packageBase =
                directoryUtil.findWebDirectoryElementByPath(namespace, webFacet);
            ContainerUtil.addIfNotNull(basePathRoots, packageBase);
          }

          return basePathRoots;
        });
  }
}
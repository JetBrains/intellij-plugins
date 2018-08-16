/*
 * Copyright 2015 The authors
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

package com.intellij.struts2.dom.struts;

import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.Nullable;

/**
 * Converter for &lt;include&gt; "file"-attribute (struts.xml files).
 *
 * @author Gregory.Shrago
 * @author Yann C&eacute;bron
 * @see Include#getFile()
 */
public abstract class IncludeFileResolvingConverter extends ResolvingConverter<PsiFile> implements CustomReferenceConverter {

  @Override
  public String toString(@Nullable final PsiFile psiFile, final ConvertContext context) {
    if (psiFile == null) {
      return null;
    }

    final VirtualFile file = psiFile.getVirtualFile();
    if (file == null) {
      return null;
    }

    final VirtualFile root = getRootForFile(file, context);
    if (root == null) {
      return null;
    }

    return VfsUtilCore.getRelativePath(file, root, '/');
  }

  @Nullable
  private static VirtualFile getRootForFile(final VirtualFile file, final ConvertContext context) {
    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(context.getPsiManager().getProject()).getFileIndex();
    VirtualFile root = projectFileIndex.getSourceRootForFile(file);

    if (root == null) {
      root = projectFileIndex.getContentRootForFile(file);
    }

    return root;
  }

}
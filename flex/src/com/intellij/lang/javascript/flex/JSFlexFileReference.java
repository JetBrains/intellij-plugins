// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JSFlexFileReference extends FileReference {

  private final ReferenceSupport.RelativeToWhat myRelativeToWhat;

  public JSFlexFileReference(@NotNull final FileReferenceSet fileReferenceSet,
                             TextRange range,
                             int index,
                             String text,
                             final ReferenceSupport.RelativeToWhat relativeToWhat) {
    super(fileReferenceSet, range, index, text);
    myRelativeToWhat = relativeToWhat;
  }


  // - absolute paths remain absolute (i.e. not relative to project root)
  // - relative paths are kept relative to what they were relative to before refactoring
  @Override
  public PsiElement bindToElement(@NotNull final PsiElement element) throws IncorrectOperationException {
    if (!(element instanceof PsiFileSystemItem fileSystemItem)) {
      throw new IncorrectOperationException("Cannot bind to element, should be instanceof PsiFileSystemItem: " + element);
    }

    final VirtualFile destVFile = fileSystemItem.getVirtualFile();
    if (destVFile == null) throw new IncorrectOperationException("Cannot bind to non-physical element:" + element);

    PsiFile currentPsiFile = getElement().getContainingFile();
    final PsiElement contextPsiFile = currentPsiFile.getContext();
    if (contextPsiFile != null) currentPsiFile = contextPsiFile.getContainingFile();

    final VirtualFile currentVFile = currentPsiFile.getVirtualFile();
    if (currentVFile == null) throw new IncorrectOperationException("Cannot bind from non-physical element:" + currentPsiFile);

    final Project project = element.getProject();

    String newName = null;

    switch (myRelativeToWhat) {
      case Absolute:
        newName = destVFile.getPath();
        break;
      case CurrentFile:
        newName = getRelativePath(currentVFile, destVFile, '/');
        break;
      case ProjectRoot:
        final VirtualFile projectRoot = project.getBaseDir();
        newName = projectRoot == null ? null : getRelativePath(projectRoot, destVFile, '/');
        break;
      case SourceRoot:
        // first try to get source root that contains the file
        final VirtualFile sourceRootForFile = ProjectRootManager.getInstance(project).getFileIndex().getSourceRootForFile(destVFile);

        if (sourceRootForFile != null) {
          newName = getRelativePath(sourceRootForFile, destVFile, '/');
        }
        else {
          final Module module = ModuleUtilCore.findModuleForFile(currentVFile, project);
          if (module != null) {
            final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
            for (final VirtualFile sourceRoot : sourceRoots) {
              final String relativePath = getRelativePath(sourceRoot, destVFile, '/');
              if (relativePath != null) {
                newName = relativePath;
                break;
              }
            }
          }
        }
        break;
      case Other:
        break;
    }

    if (newName != null && getFileReferenceSet().getPathString().startsWith("/") && !newName.startsWith("/")) {
      newName = "/" + newName;
    }

    return newName == null ? element : rename(newName);
  }

  // the difference from VfsUtil.getPath() is that if srcFile is a directory then one more "../" may be needed
  @Nullable
  private static String getRelativePath(@NotNull VirtualFile src, @NotNull VirtualFile dst, final char separatorChar) {
    final VirtualFile commonAncestor = VfsUtilCore.getCommonAncestor(src, dst);
    if (commonAncestor != null) {
      StringBuilder buffer = new StringBuilder();
      if (!Comparing.equal(src, commonAncestor)) {
        if (src.isDirectory()) {
          buffer.append("..").append(separatorChar);       // this line is the only difference from VfsUtil.getPath()
        }
        while (!Comparing.equal(src.getParent(), commonAncestor)) {
          buffer.append("..").append(separatorChar);
          src = src.getParent();
        }
      }
      buffer.append(VfsUtilCore.getRelativePath(dst, commonAncestor, separatorChar));
      return buffer.toString();
    }

    return null;
  }
}

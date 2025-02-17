// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.watcher;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.impl.include.FileIncludeInfo;
import com.intellij.psi.impl.include.FileIncludeProvider;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Consumer;
import com.intellij.util.indexing.FileContent;
import com.jetbrains.plugins.jade.JadeToPugTransitionHelper;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;
import com.jetbrains.plugins.jade.psi.JadeFileImpl;
import com.jetbrains.plugins.jade.psi.JadeFileType;
import com.jetbrains.plugins.jade.psi.impl.JadeIncludeStatementImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class JadeFileIncludeProvider extends FileIncludeProvider {

  @Override
  public @NotNull String getId() {
    return "jade";
  }

  @Override
  public boolean acceptFile(@NotNull VirtualFile file) {
    return file.getFileType() instanceof JadeFileType;
  }

  @Override
  public void registerFileTypesUsedForIndexing(@NotNull Consumer<? super FileType> fileTypeSink) {
    fileTypeSink.consume(JadeFileType.INSTANCE);
  }

  @Override
  public FileIncludeInfo @NotNull [] getIncludeInfos(@NotNull FileContent content) {
    PsiFile psiFile = content.getPsiFile();
    if (psiFile instanceof JadeFileImpl) {
      return getIncludeInfos((JadeFileImpl) psiFile);
    }
    return FileIncludeInfo.EMPTY;
  }

  private static FileIncludeInfo[] getIncludeInfos(JadeFileImpl file) {
    final List<FileIncludeInfo> result = new ArrayList<>();
    final String fileExtension = "." + JadeToPugTransitionHelper.getExtension(file);

    file.acceptChildren(new PsiRecursiveElementVisitor() {
      @Override
      public void visitElement(@NotNull PsiElement element) {
        if (element == PsiUtilCore.NULL_PSI_ELEMENT) {
          throw new IllegalStateException("file "+file+" ("+file.getClass()+") must not have "+element+" among its children");
        }
        if (element instanceof JadeIncludeStatementImpl) {
          String path = extractPath(fileExtension, ((JadeIncludeStatementImpl)element));
          if (path != null) {
            result.add(new FileIncludeInfo(path));
          }
        }
        else {
          super.visitElement(element);
        }
      }
    });
    return result.toArray(FileIncludeInfo.EMPTY);
  }

  private static @Nullable String extractPath(@NotNull String fileExtension, @NotNull JadeIncludeStatementImpl includeStatement) {
    PsiElement[] children = includeStatement.getChildren();
    for (PsiElement child : children) {
      if (child instanceof ASTNode node) {
        if (node.getElementType() == JadeElementTypes.FILE_PATH) {
          String path = child.getText();
          if (!path.endsWith(fileExtension)) {
            path += fileExtension;
          }
          return path;
        }
      }
    }
    return null;
  }
}

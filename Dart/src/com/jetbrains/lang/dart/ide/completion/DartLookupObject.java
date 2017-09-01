package com.jetbrains.lang.dart.ide.completion;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartId;
import org.dartlang.analysis.server.protocol.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// this class implements ResolveResult in order to be able to calculate corresponding PsiElement, see com.intellij.codeInsight.lookup.LookupElement#getPsiElement()
public class DartLookupObject implements ResolveResult {
  @NotNull private final Project myProject;
  @Nullable private final Location myLocation;

  public DartLookupObject(@NotNull final Project project, @Nullable Location location) {
    myProject = project;
    myLocation = location;
  }

  @Nullable
  @Override
  public PsiElement getElement() {
    final String filePath = myLocation == null ? null : FileUtil.toSystemIndependentName(myLocation.getFile());
    final VirtualFile vFile = filePath == null ? null : LocalFileSystem.getInstance().findFileByPath(filePath);
    final PsiFile psiFile = vFile == null ? null : PsiManager.getInstance(myProject).findFile(vFile);
    if (psiFile != null) {
      final int offset = DartAnalysisServerService.getInstance(myProject).getConvertedOffset(vFile, myLocation.getOffset());
      final PsiElement elementAtOffset = psiFile.findElementAt(offset);
      if (elementAtOffset != null) {
        final DartComponentName componentName = PsiTreeUtil.getParentOfType(elementAtOffset, DartComponentName.class);
        if (componentName != null) {
          return componentName;
        }
        if (elementAtOffset.getParent() instanceof DartId && elementAtOffset.getTextRange().getStartOffset() == offset) {
          return elementAtOffset; // example in WEB-25478 (https://github.com/flutter/flutter-intellij/issues/385#issuecomment-278826063)
        }
      }
    }
    return null;
  }

  @Override
  public boolean isValidResult() {
    return true;
  }
}

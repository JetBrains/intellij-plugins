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
import org.dartlang.analysis.server.protocol.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// this class implements ResolveResult in order to be able to calculate corresponding PsiElement, see com.intellij.codeInsight.lookup.LookupElement#getPsiElement()
public class DartLookupObject implements ResolveResult {
  @NotNull private final Project myProject;
  @Nullable private final Location myLocation;
  private final int myRelevance;

  public DartLookupObject(@NotNull final Project project, @Nullable Location location, final int relevance) {
    myProject = project;
    myLocation = location;
    myRelevance = relevance;
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
      return PsiTreeUtil.getParentOfType(elementAtOffset, DartComponentName.class);
    }
    return null;
  }

  @Override
  public boolean isValidResult() {
    return true;
  }

  public int getRelevance() {
    return myRelevance;
  }
}

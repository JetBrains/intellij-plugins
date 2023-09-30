// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.jhipster.JdlFileType;
import com.intellij.jhipster.JdlLanguage;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class JdlFile extends PsiFileBase {
  public JdlFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, JdlLanguage.INSTANCE);
  }

  @Override
  public @NotNull FileType getFileType() {
    return JdlFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "JdlFile";
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                     PsiElement lastParent, @NotNull PsiElement place) {
    List<PsiElement> nodes = getDeclarations();
    for (PsiElement node : nodes) {
      if (!processor.execute(node, state)) return false;
    }
    return true;
  }

  public List<PsiElement> getDeclarations() {
    return CachedValuesManager.getCachedValue(this, () ->
      Result.create(findDeclarations(this), PsiModificationTracker.MODIFICATION_COUNT)
    );
  }

  private static List<PsiElement> findDeclarations(@NotNull JdlFile file) {
    ASTNode[] nodes = file.getNode().getChildren(JdlTokenSets.DECLARATIONS);
    return Arrays.stream(nodes)
      .map(ASTNode::getPsi)
      .collect(Collectors.toList());
  }
}

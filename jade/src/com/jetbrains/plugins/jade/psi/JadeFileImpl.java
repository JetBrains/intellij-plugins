// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi;

import com.intellij.lang.html.HtmlCompatibleFile;
import com.intellij.lang.javascript.psi.JSEmbeddedContent;
import com.intellij.lang.javascript.psi.JSExecutionScope;
import com.intellij.lang.javascript.psi.resolve.JSResolveProcessorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.xml.XmlFileImpl;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.jetbrains.plugins.jade.psi.stubs.JadeStubElementTypes;
import org.jetbrains.annotations.NotNull;

public class JadeFileImpl extends XmlFileImpl implements HtmlCompatibleFile, JSExecutionScope {

  public JadeFileImpl(final @NotNull FileViewProvider viewProvider) {
    super(viewProvider, JadeStubElementTypes.JADE_FILE);
  }

  @Override
  public @NotNull FileType getFileType() {
    return JadeFileType.INSTANCE;
  }

  @Override
  public boolean processDeclarations(final @NotNull PsiScopeProcessor processor,
                                     final @NotNull ResolveState state,
                                     final PsiElement lastParent,
                                     final @NotNull PsiElement place) {
    if (!super.processDeclarations(processor, state, lastParent, place)) {
      return false;
    }

    if (!(processor instanceof JSResolveProcessorEx)) {
      return true;
    }

    final PsiElementVisitor visitor = new PsiElementVisitor() {
      @Override
      public void visitElement(@NotNull PsiElement element) {
        ProgressManager.checkCanceled();
        if (element instanceof JSEmbeddedContent) {
          element.processDeclarations(processor, state, lastParent, place);
        }
        else {
          element.acceptChildren(this);
        }
      }
    };
    acceptChildren(visitor);

    return true;
  }
}

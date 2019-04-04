// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.xml;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.TagNameReference;
import com.intellij.xml.HtmlXmlExtension;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import com.jetbrains.lang.dart.resolve.DartResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartXmlExtension extends HtmlXmlExtension {
  @Override
  public boolean isAvailable(PsiFile file) {
    VirtualFile vFile = file.getVirtualFile();
    return super.isAvailable(file) &&
           vFile != null &&
           !DartAnalysisServerService.getInstance(file.getProject()).getNavigation(vFile).isEmpty();
  }

  @Nullable
  @Override
  public TagNameReference createTagNameReference(ASTNode nameElement, boolean startTagFlag) {
    PsiFile file = nameElement.getPsi().getContainingFile();
    TextRange range = nameElement.getTextRange();
    DartServerData.DartNavigationRegion navRegion = DartResolver.findRegion(file, range.getStartOffset(), range.getLength());
    if (navRegion != null) {
      for (DartServerData.DartNavigationTarget target : navRegion.getTargets()) {
        if (!FileUtil.toSystemIndependentName(target.getFile()).endsWith("lib/html/dart2js/html_dart2js.dart")) {
          return new DartTagNameReference(nameElement, startTagFlag, target);
        }
      }
    }

    return super.createTagNameReference(nameElement, startTagFlag);
  }

  private static class DartTagNameReference extends TagNameReference {
    @NotNull private final DartServerData.DartNavigationTarget myTarget;

    DartTagNameReference(@NotNull ASTNode nameElement,
                         boolean startTagFlag,
                         @NotNull DartServerData.DartNavigationTarget target) {
      super(nameElement, startTagFlag);
      myTarget = target;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
      PsiElement resolved = DartResolver.getElementForNavigationTarget(getElement().getProject(), myTarget);
      if (resolved != null) {
        return resolved;
      }

      return super.resolve();
    }
  }
}

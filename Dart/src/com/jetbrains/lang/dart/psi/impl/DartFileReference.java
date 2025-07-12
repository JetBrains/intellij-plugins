// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.psi.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartServerData.DartNavigationRegion;
import com.jetbrains.lang.dart.analyzer.DartServerData.DartNavigationTarget;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.psi.DartImportStatement;
import com.jetbrains.lang.dart.psi.DartUriElement;
import com.jetbrains.lang.dart.resolve.DartResolver;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Reference to a file in an import, export or part directive.
 */
public class DartFileReference implements PsiPolyVariantReference {
  private static final Resolver RESOLVER = new Resolver();

  private final @NotNull DartUriElement myUriElement;
  private final @NotNull String myUri;
  private final @NotNull TextRange myRange;

  public DartFileReference(final @NotNull DartUriElement uriElement, final @NotNull String uri) {
    final int offset = uriElement.getText().indexOf(uri);
    assert offset >= 0 : uriElement.getText() + " doesn't contain " + uri;

    myUriElement = uriElement;
    myUri = uri;
    myRange = TextRange.create(offset, offset + uri.length());
  }

  @Override
  public @NotNull PsiElement getElement() {
    return myUriElement;
  }

  @Override
  public @NotNull TextRange getRangeInElement() {
    return myRange;
  }

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
    return ResolveCache.getInstance(myUriElement.getProject()).resolveWithCaching(this, RESOLVER, true, incompleteCode);
  }

  @Override
  public @Nullable PsiElement resolve() {
    final ResolveResult[] resolveResults = multiResolve(false);
    return resolveResults.length != 1 ||
           !resolveResults[0].isValidResult() ? null : resolveResults[0].getElement();
  }

  @Override
  public @NotNull String getCanonicalText() {
    return myUri;
  }

  @Override
  public PsiElement handleElementRename(final @NotNull String newFileName) throws IncorrectOperationException {
    final int index = Math.max(myUri.lastIndexOf('/'), myUri.lastIndexOf("\\\\"));
    final String newUri = index < 0 ? newFileName : myUri.substring(0, index) + "/" + newFileName;
    return updateUri(newUri);
  }

  @Override
  public PsiElement bindToElement(final @NotNull PsiElement element) throws IncorrectOperationException {
    return myUriElement;
  }

  private PsiElement updateUri(final @NotNull String newUri) {
    final String uriElementText = myUriElement.getText();
    final String startQuote = uriElementText.substring(0, myRange.getStartOffset());
    final String endQuote = uriElementText.substring(myRange.getEndOffset());
    final String text = "import " + startQuote + newUri + endQuote + ";";
    final PsiFile fileFromText = PsiFileFactory.getInstance(myUriElement.getProject()).createFileFromText(DartLanguage.INSTANCE, text);

    final DartImportStatement importStatement = PsiTreeUtil.findChildOfType(fileFromText, DartImportStatement.class);
    assert importStatement != null : fileFromText.getText();

    return myUriElement.replace(importStatement.getUriElement());
  }

  @Override
  public boolean isReferenceTo(final @NotNull PsiElement element) {
    return element instanceof DartFile && element.equals(resolve());
  }

  @Override
  public boolean isSoft() {
    return false;
  }

  private static class Resolver implements ResolveCache.PolyVariantResolver<DartFileReference> {
    @Override
    public ResolveResult @NotNull [] resolve(final @NotNull DartFileReference reference, final boolean incompleteCode) {
      final PsiFile refPsiFile = reference.getElement().getContainingFile();
      final int refOffset = reference.getElement().getTextRange().getStartOffset();
      final int refLength = reference.getElement().getTextRange().getLength();

      DartNavigationRegion region = DartResolver.findRegion(refPsiFile, refOffset, refLength);

      if (region == null) {
        // file might be not open in editor, so we do not have navigation information for it
        final VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(refPsiFile);
        final DartAnalysisServerService das = DartAnalysisServerService.getInstance(refPsiFile.getProject());
        if (virtualFile != null &&
            das.getNavigation(virtualFile).isEmpty() &&
            das.getHighlight(virtualFile).isEmpty()) {
          final PsiElement parent = reference.getElement().getParent();
          final int parentOffset = parent.getTextRange().getStartOffset();
          final int parentLength = parent.getTextRange().getLength();
          final List<DartNavigationRegion> regions = das.analysis_getNavigation(virtualFile, parentOffset, parentLength);
          if (regions != null) {
            region = DartResolver.findRegion(regions, refOffset, refLength);
          }
        }
      }

      if (region != null) {
        final List<DartNavigationTarget> targets = region.getTargets();
        if (!targets.isEmpty()) {
          final DartNavigationTarget target = targets.get(0);
          final VirtualFile targetVirtualFile = target.findFile();
          if (targetVirtualFile != null) {
            final PsiFile targetFile = reference.getElement().getManager().findFile(targetVirtualFile);
            if (targetFile != null) {
              return new ResolveResult[]{new PsiElementResolveResult(targetFile)};
            }
          }
        }
      }

      return ResolveResult.EMPTY_ARRAY;
    }
  }
}

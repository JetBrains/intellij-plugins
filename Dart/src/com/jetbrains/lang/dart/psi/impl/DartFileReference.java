// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.psi.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
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
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Reference to a file in an import, export or part directive.
 */
public class DartFileReference implements PsiPolyVariantReference {
  private static final Resolver RESOLVER = new Resolver();

  @NotNull private final DartUriElement myUriElement;
  @NotNull private final String myUri;
  @NotNull private final TextRange myRange;

  public DartFileReference(@NotNull final DartUriElement uriElement, @NotNull final String uri) {
    final int offset = uriElement.getText().indexOf(uri);
    assert offset >= 0 : uriElement.getText() + " doesn't contain " + uri;

    myUriElement = uriElement;
    myUri = uri;
    myRange = TextRange.create(offset, offset + uri.length());
  }

  @NotNull
  @Override
  public PsiElement getElement() {
    return myUriElement;
  }

  @NotNull
  @Override
  public TextRange getRangeInElement() {
    return myRange;
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    return ResolveCache.getInstance(myUriElement.getProject()).resolveWithCaching(this, RESOLVER, true, incompleteCode);
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    final ResolveResult[] resolveResults = multiResolve(false);
    return resolveResults.length == 0 ||
           resolveResults.length > 1 ||
           !resolveResults[0].isValidResult() ? null : resolveResults[0].getElement();
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return myUri;
  }

  @Override
  public PsiElement handleElementRename(@NotNull final String newFileName) throws IncorrectOperationException {
    final int index = Math.max(myUri.lastIndexOf('/'), myUri.lastIndexOf("\\\\"));
    final String newUri = index < 0 ? newFileName : myUri.substring(0, index) + "/" + newFileName;
    return updateUri(newUri);
  }

  @Override
  public PsiElement bindToElement(@NotNull final PsiElement element) throws IncorrectOperationException {
    if (element instanceof PsiFile) {
      final VirtualFile contextFile = DartResolveUtil.getRealVirtualFile(myUriElement.getContainingFile());
      final VirtualFile targetFile = DartResolveUtil.getRealVirtualFile(((PsiFile)element));
      final Project project = myUriElement.getProject();
      final DartSdk dartSdk = DartSdk.getDartSdk(project);
      if (dartSdk != null && !DartAnalysisServerService.isDartSdkVersionForMoveFileRefactoring(dartSdk)) {
        if (contextFile != null && targetFile != null) {
          final String newUri = DartUrlResolver.getInstance(myUriElement.getProject(), contextFile).getDartUrlForFile(targetFile);
          if (newUri.startsWith(DartUrlResolver.PACKAGE_PREFIX)) {
            return updateUri(newUri);
          }
          else if (newUri.startsWith(DartUrlResolver.FILE_PREFIX)) {
            final String relativePath = FileUtil.getRelativePath(contextFile.getParent().getPath(), targetFile.getPath(), '/');
            if (relativePath != null) {
              return updateUri(relativePath);
            }
          }
        }
      }
    }
    return myUriElement;
  }

  private PsiElement updateUri(@NotNull final String newUri) {
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
  public boolean isReferenceTo(@NotNull final PsiElement element) {
    return element instanceof DartFile && element.equals(resolve());
  }

  @Override
  public boolean isSoft() {
    return false;
  }

  private static class Resolver implements ResolveCache.PolyVariantResolver<DartFileReference> {
    @NotNull
    @Override
    public ResolveResult[] resolve(@NotNull final DartFileReference reference, final boolean incompleteCode) {
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
          final String targetPath = target.getFile();
          final VirtualFile targetVirtualFile = LocalFileSystem.getInstance().findFileByPath(targetPath);
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

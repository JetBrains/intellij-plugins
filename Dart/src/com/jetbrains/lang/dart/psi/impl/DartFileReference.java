/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
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
package com.jetbrains.lang.dart.psi.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.analyzer.DartServerData.PluginNavigationRegion;
import com.jetbrains.lang.dart.analyzer.DartServerData.PluginNavigationTarget;
import com.jetbrains.lang.dart.resolve.DartResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Reference to a file in an import, export or part directive.
 */
public class DartFileReference implements PsiReference {
  @NotNull private final PsiElement myElement;
  @NotNull private final String myUri;
  @NotNull private final TextRange myRange;
  private boolean myResolveDone;
  private PsiFile myTargetPsiFile;

  public DartFileReference(@NotNull final DartUriElementBase uriRefExpr, @NotNull final String uri) {
    final int offset = uriRefExpr.getText().indexOf(uri);
    assert offset >= 0 : uriRefExpr.getText() + " doesn't contain " + uri;

    myElement = uriRefExpr;
    myUri = uri;
    myRange = TextRange.create(offset, offset + uri.length());
  }

  @NotNull
  @Override
  public PsiElement getElement() {
    return myElement;
  }

  @Override
  public TextRange getRangeInElement() {
    return myRange;
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    if (!myResolveDone) {
      final PsiFile refPsiFile = myElement.getContainingFile();
      final int refOffset = myElement.getTextOffset();
      final int refLength = myElement.getTextLength();
      final PluginNavigationRegion region = DartResolver.findRegion(refPsiFile, refOffset, refLength);
      if (region != null) {
        final List<PluginNavigationTarget> targets = region.getTargets();
        if (!targets.isEmpty()) {
          final PluginNavigationTarget target = targets.get(0);
          final String targetPath = target.getFile();
          final VirtualFile targetVirtualFile = LocalFileSystem.getInstance().findFileByPath(targetPath);
          if (targetVirtualFile != null) {
            myTargetPsiFile = myElement.getManager().findFile(targetVirtualFile);
          }
        }
      }

      myResolveDone = true;
    }

    return myTargetPsiFile;
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return myUri;
  }

  @Override
  public PsiElement handleElementRename(final String newElementName) throws IncorrectOperationException {
    return myElement;
  }

  @Override
  public PsiElement bindToElement(@NotNull final PsiElement element) throws IncorrectOperationException {
    return element;
  }

  @Override
  public boolean isReferenceTo(final PsiElement element) {
    return element != null && element.equals(resolve());
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return EMPTY_ARRAY;
  }

  @Override
  public boolean isSoft() {
    return false;
  }
}

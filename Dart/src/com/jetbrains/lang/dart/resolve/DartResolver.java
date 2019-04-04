// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.resolve;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import com.jetbrains.lang.dart.analyzer.DartServerData.DartNavigationRegion;
import com.jetbrains.lang.dart.analyzer.DartServerData.DartNavigationTarget;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartResolver implements ResolveCache.AbstractResolver<DartReference, List<? extends PsiElement>> {
  public static final DartResolver INSTANCE = new DartResolver();

  @Nullable
  @Override
  public List<? extends PsiElement> resolve(@NotNull DartReference reference, boolean incompleteCode) {
    reference = replaceQualifiedReferenceWithLast(reference);
    final PsiFile refPsiFile = reference.getContainingFile();
    int refOffset = reference.getTextRange().getStartOffset();
    int refLength = reference.getTextRange().getLength();
    DartNavigationRegion region = findRegion(refPsiFile, refOffset, refLength);

    if (region == null && reference instanceof DartLibraryId) {
      // DAS returns the whole "part of foo" as a region, but we have only "foo" as a reference
      final PsiElement parent = reference.getParent();
      if (parent instanceof DartPartOfStatement) {
        refOffset = parent.getTextRange().getStartOffset();
        refLength = reference.getTextRange().getEndOffset() - refOffset;
        region = findRegion(refPsiFile, refOffset, refLength);
      }
    }

    if (region == null && (reference instanceof DartSuperExpression || reference instanceof DartReferenceExpression)) {
      // DAS from SDK 1.13- returns 'super.foo' as a single range; SDK 1.14 returns 'super' and 'foo' separately.
      final PsiElement parent = reference.getParent();
      if (parent instanceof DartSuperCallOrFieldInitializer) {
        final List<DartExpression> expressions = ((DartSuperCallOrFieldInitializer)parent).getExpressionList();
        if (expressions.size() == 2 &&
            expressions.get(0) instanceof DartSuperExpression &&
            expressions.get(1) instanceof DartReferenceExpression) {
          refOffset = expressions.get(0).getTextRange().getStartOffset();
          refLength = expressions.get(1).getTextRange().getEndOffset() - refOffset;
          region = findRegion(refPsiFile, refOffset, refLength);
        }
      }
    }

    return region != null ? getTargetElements(reference.getProject(), region) : null;
  }

  @NotNull
  public static List<? extends PsiElement> getTargetElements(@NotNull final Project project, @NotNull final DartNavigationRegion region) {
    final List<PsiElement> result = new SmartList<>();
    for (DartNavigationTarget target : region.getTargets()) {
      final PsiElement targetElement = getElementForNavigationTarget(project, target);
      if (targetElement != null) {
        result.add(targetElement);
      }
    }
    return result;
  }

  /**
   * When parameter information is requested for {@code items.insert(^)},
   * we are given {@code items.insert}, but we cannot resolve it, we need just {@code insert}.
   */
  @NotNull
  private static DartReference replaceQualifiedReferenceWithLast(@NotNull DartReference reference) {
    final PsiElement lastChild = reference.getLastChild();
    if (lastChild instanceof DartReference) {
      reference = (DartReference)lastChild;
    }
    return reference;
  }

  @Nullable
  public static DartNavigationRegion findRegion(final PsiFile refPsiFile, final int refOffset, final int refLength) {
    final VirtualFile refVirtualFile = DartResolveUtil.getRealVirtualFile(refPsiFile);
    if (refVirtualFile != null) {
      final List<DartServerData.DartNavigationRegion> regions =
        DartAnalysisServerService.getInstance(refPsiFile.getProject()).getNavigation(refVirtualFile);
      return findRegion(regions, refOffset, refLength);
    }
    return null;
  }

  @Nullable
  public static PsiElement getElementForNavigationTarget(Project project, DartNavigationTarget target) {
    String targetPath = target.getFile();
    PsiFile file = findPsiFile(project, targetPath);
    if (file != null) {
      int targetOffset = target.getOffset(project, file.getVirtualFile());
      PsiElement elementAtOffset = file.findElementAt(targetOffset);
      PsiNameIdentifierOwner nameOwner =
        PsiTreeUtil.getNonStrictParentOfType(elementAtOffset, DartComponentName.class, DartLibraryNameElement.class);
      return nameOwner != null ? nameOwner : elementAtOffset;
    }

    return null;
  }

  @Nullable
  public static PsiFile findPsiFile(@NotNull Project project, @NotNull String path) {
    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
    if (virtualFile != null) {
      return PsiManager.getInstance(project).findFile(virtualFile);
    }
    return null;
  }

  /**
   * Find the region with the given offset in the given list of sorted regions.
   * Returns the found region or null.
   */
  @Nullable
  public static DartNavigationRegion findRegion(@NotNull final List<? extends DartNavigationRegion> regions,
                                                final int offset,
                                                final int length) {
    int i = findOffsetIndex(regions, offset);
    if (i >= 0) {
      DartNavigationRegion midVal = regions.get(i);
      return midVal.getLength() == length ? midVal : null;
    }
    return null;
  }

  private static int findOffsetIndex(@NotNull List<? extends DartNavigationRegion> regions, int offset) {
    return ObjectUtils.binarySearch(0, regions.size(), mid -> Integer.compare(regions.get(mid).getOffset(), offset));
  }

  public static void processRegionsInRange(@NotNull final List<? extends DartNavigationRegion> regions,
                                           @NotNull final TextRange range,
                                           @NotNull final Consumer<? super DartNavigationRegion> processor) {
    if (regions.isEmpty()) return;

    // first find the first region that has minimal allowed offset

    int i = ObjectUtils.binarySearch(0, regions.size(), mid -> regions.get(mid).getOffset() < range.getStartOffset() ? -1 : 1);
    i = Math.max(0, -i - 2);
    DartNavigationRegion region = regions.get(i);
    if (region.getOffset() < range.getStartOffset()) {
      i++;
      if (i < regions.size()) {
        region = regions.get(i);
      }
      else {
        return;
      }
    }
    while (region.getOffset() + region.getLength() <= range.getEndOffset()) {
      processor.consume(region);
      i++;
      if (i < regions.size()) {
        region = regions.get(i);
      }
      else {
        return;
      }
    }
  }
}

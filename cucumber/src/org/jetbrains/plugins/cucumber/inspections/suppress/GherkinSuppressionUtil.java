// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.inspections.suppress;

import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.codeInspection.SuppressionUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinSuppressionHolder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.codeInspection.SuppressionUtil.COMMON_SUPPRESS_REGEXP;

public final class GherkinSuppressionUtil {
  // the same regexp as for ruby
  private static final Pattern SUPPRESS_IN_LINE_COMMENT_PATTERN = Pattern.compile("#" + COMMON_SUPPRESS_REGEXP);

  private GherkinSuppressionUtil() {
  }

  public static SuppressQuickFix @NotNull [] getDefaultSuppressActions(final @NotNull String toolId) {
    return new SuppressQuickFix[]{
      new GherkinSuppressForStepCommentFix(toolId),
      new GherkinSuppressForScenarioCommentFix(toolId),
      new GherkinSuppressForFeatureCommentFix(toolId),
    };
  }

  public static boolean isSuppressedFor(final @NotNull PsiElement element, final @NotNull String toolId) {
    return ReadAction.compute(() -> getSuppressedIn(element, toolId) != null).booleanValue();
  }

  private static @Nullable PsiComment getSuppressedIn(@NotNull PsiElement place, @NotNull String toolId) {
    // find suppression holder with suppression comment about given inspection tool
    PsiElement suppressionHolder = PsiTreeUtil.getNonStrictParentOfType(place, GherkinSuppressionHolder.class);
    while (suppressionHolder != null) {
      final PsiComment suppressionHolderElement = getSuppressionComment(toolId, suppressionHolder);
      if (suppressionHolderElement != null) {
        return suppressionHolderElement;
      }
      suppressionHolder = PsiTreeUtil.getParentOfType(suppressionHolder, GherkinSuppressionHolder.class);
    }
    return null;
  }

  private static @Nullable PsiComment getSuppressionComment(@NotNull String toolId,
                                                            @NotNull PsiElement element) {
    final PsiElement comment = PsiTreeUtil.skipWhitespacesBackward(element);
    if (comment instanceof PsiComment) {
      String text = comment.getText();
      Matcher matcher = SUPPRESS_IN_LINE_COMMENT_PATTERN.matcher(text);
      if (matcher.matches() && SuppressionUtil.isInspectionToolIdMentioned(matcher.group(1), toolId)) {
        return (PsiComment)comment;
      }
    }
    return null;
  }
}

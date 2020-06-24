// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInsight.daemon.impl.actions.AbstractBatchSuppressByNoInspectionCommentFix;
import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.codeInspection.SuppressionUtilCore;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParserFacade;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression;
import org.angular2.lang.expr.psi.Angular2PipeArgumentsList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import static com.intellij.codeInspection.SuppressionUtil.SUPPRESS_IN_LINE_COMMENT_PATTERN;
import static com.intellij.codeInspection.SuppressionUtil.isInspectionToolIdMentioned;

public class Angular2InspectionSuppressor implements InspectionSuppressor {

  public static final Angular2InspectionSuppressor INSTANCE = new Angular2InspectionSuppressor();

  @NonNls private static final String[] PREFIXES_TO_STRIP = new String[]{"TypeScript", "JS", "Angular"};

  @Override
  public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
    return isSuppressedInStatement(element, stripToolIdPrefix(toolId));
  }

  @Override
  public SuppressQuickFix @NotNull [] getSuppressActions(@Nullable PsiElement element, @NotNull String toolId) {
    return new SuppressQuickFix[]{new Angular2SuppressByCommentFix(stripToolIdPrefix(toolId))};
  }

  private static @Nullable PsiElement getStatementToolSuppressedIn(@NotNull PsiElement place,
                                                                   @NotNull String toolId) {
    PsiElement statement = PsiTreeUtil.getParentOfType(place, Angular2EmbeddedExpression.class);
    if (statement != null) {
      PsiElement candidate = PsiTreeUtil.skipWhitespacesForward(statement);
      //workaround for empty argument list
      if (!(candidate instanceof PsiComment) && statement.getLastChild() instanceof Angular2PipeArgumentsList) {
        candidate = PsiTreeUtil.skipWhitespacesBackward(statement.getLastChild());
      }
      if (candidate instanceof PsiComment) {
        String text = candidate.getText();
        Matcher matcher = SUPPRESS_IN_LINE_COMMENT_PATTERN.matcher(text);
        if (matcher.matches() && isInspectionToolIdMentioned(matcher.group(1), toolId)) {
          return candidate;
        }
      }
    }
    return null;
  }

  private static boolean isSuppressedInStatement(final @NotNull PsiElement place,
                                                 final @NotNull String toolId) {
    return ReadAction.compute(() -> getStatementToolSuppressedIn(place, toolId)) != null;
  }

  private static String stripToolIdPrefix(String toolId) {
    for (String prefix : PREFIXES_TO_STRIP) {
      if (toolId.startsWith(prefix)) {
        return toolId.substring(prefix.length());
      }
    }
    return toolId;
  }

  private static class Angular2SuppressByCommentFix extends AbstractBatchSuppressByNoInspectionCommentFix {

    Angular2SuppressByCommentFix(String key) {
      super(key, false);
    }

    @Override
    protected void createSuppression(@NotNull Project project, @NotNull PsiElement element, @NotNull PsiElement container)
      throws IncorrectOperationException {
      final PsiParserFacade parserFacade = PsiParserFacade.SERVICE.getInstance(project);
      PsiComment comment = parserFacade.createLineOrBlockCommentFromText(Angular2Language.INSTANCE, getSuppressText());
      container.getParent().addAfter(comment, container);
    }

    @Override
    public @Nullable PsiElement getContainer(PsiElement context) {
      return PsiTreeUtil.getParentOfType(context, Angular2EmbeddedExpression.class);
    }

    @Override
    public @NotNull String getText() {
      return Angular2Bundle.message("angular.suppress.for-expression");
    }

    @Override
    protected @Nullable List<? extends PsiElement> getCommentsFor(@NotNull PsiElement container) {
      final PsiElement next = PsiTreeUtil.skipWhitespacesForward(container);
      if (next == null) {
        return null;
      }
      return Collections.singletonList(next);
    }

    protected @NotNull String getSuppressText() {
      return SuppressionUtilCore.SUPPRESS_INSPECTIONS_TAG_NAME + " " + myID;
    }
  }
}

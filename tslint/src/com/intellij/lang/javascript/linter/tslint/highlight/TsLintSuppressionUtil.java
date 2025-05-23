// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.tslint.highlight;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.linter.JSLinterSuppressionUtil;
import com.intellij.lang.javascript.linter.tslint.TsLintBundle;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TsLintSuppressionUtil extends JSLinterSuppressionUtil {
  public static final TsLintSuppressionUtil INSTANCE = new TsLintSuppressionUtil();
  private static final String LINE_COMMENT_PREFIX = "tslint:disable-next-line";
  private static final Pattern LINE_SUPPRESSION_PATTERN = Pattern.compile("(?://|/\\*)\\s*tslint:disable-next-line:\\s*(.*?)(?:\\*/)?");
  private static final Pattern FILE_SUPPRESSION_PATTERN = Pattern.compile("/\\*\\s*tslint:disable:\\s*(.*?)\\s*\\*/");
  private static final String FILE_COMMENT_PREFIX = "tslint:disable";

  private TsLintSuppressionUtil() {
  }

  @Override
  protected @Nullable String getRulesFromFileLevelComment(@NotNull PsiComment comment) {
    if (comment.getNode().getElementType() != JSTokenTypes.C_STYLE_COMMENT) {
      return null;
    }
    String text = comment.getText();
    if (text.contains(FILE_COMMENT_PREFIX)) {
      Matcher matcher = FILE_SUPPRESSION_PATTERN.matcher(text);
      if (matcher.matches()) {
        return matcher.group(1).trim();
      }
    }
    return null;
  }

  @Override
  protected @Nullable String getRulesFromLineSuppressionComment(@NotNull PsiComment comment) {
    String text = comment.getText();
    if (text.contains(LINE_COMMENT_PREFIX)) {
      Matcher matcher = LINE_SUPPRESSION_PATTERN.matcher(text);
      if (matcher.matches()) {
        return matcher.group(1).trim();
      }
    }
    return null;
  }

  @Override
  protected @NotNull String getToolName() {
    return TsLintBundle.message("tslint.framework.title");
  }

  @Override
  protected @NotNull String buildFileCommentText(@Nullable String existing, @Nullable String toAdd) {
    return getCommentText(FILE_COMMENT_PREFIX, existing, toAdd);
  }

  @Override
  protected @NotNull String buildLineCommentText(@Nullable String ruleCode, @Nullable String existingSuppressions) {
    return getCommentText(LINE_COMMENT_PREFIX, existingSuppressions, ruleCode);
  }

  private static @NotNull String getCommentText(String prefix, @Nullable String existing, @Nullable String toAdd) {
    if (toAdd == null) {
      //'suppress all' should overwrite suppression for particular rules
      return prefix;
    }
    String rules = StringUtil.isEmpty(existing) ? toAdd : existing + " " + toAdd;
    return prefix + (StringUtil.isEmpty(rules) ? "" : ":" + rules);
  }
}

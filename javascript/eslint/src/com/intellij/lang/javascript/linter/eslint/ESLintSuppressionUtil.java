package com.intellij.lang.javascript.linter.eslint;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.linter.eslint.EslintBundle;
import com.intellij.lang.javascript.linter.JSLinterSuppressionUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ESLintSuppressionUtil extends JSLinterSuppressionUtil {
  public static final ESLintSuppressionUtil INSTANCE = new ESLintSuppressionUtil();
  private static final String FILE_COMMENT_PREFIX = "eslint-disable";
  public static final String LINE_COMMENT_PREFIX = "eslint-disable-next-line";

  private ESLintSuppressionUtil() {
  }

  private static final Pattern LINE_SUPPRESSION_PATTERN = Pattern.compile("(?://|/\\*)\\s*eslint-disable-next-line\\s*(.*?)(?:\\*/)?");
  private static final Pattern FILE_SUPPRESSION_PATTERN = Pattern.compile("/\\*\\s*eslint-disable\\s*(.*?)\\s*\\*/");

  @Override
  protected @NotNull String getToolName() {
    return EslintBundle.message("settings.javascript.linters.eslint.configurable.name");
  }

  @Override
  protected @Nullable String getRulesFromFileLevelComment(@NotNull PsiComment element) {
    if (element.getNode().getElementType() != JSTokenTypes.C_STYLE_COMMENT) {
      return null;
    }
    String text = element.getText();
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
    String rules = StringUtil.isEmpty(existing) ? toAdd : existing + "," + toAdd;
    return prefix + (StringUtil.isEmpty(rules) ? "" : " " + rules);
  }
}

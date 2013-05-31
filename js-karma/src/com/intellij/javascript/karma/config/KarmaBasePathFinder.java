package com.intellij.javascript.karma.config;

import com.intellij.javascript.testFramework.util.JsPsiUtils;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class KarmaBasePathFinder {

  private static final String BASE_PATH_VAR_NAME = "basePath";
  private static final KarmaBasePathFinder INSTANCE = new KarmaBasePathFinder();

  private final Key<CachedValue<String>> myTestFileStructureRegistryKey = Key.create(
    KarmaBasePathFinder.class.getSimpleName()
  );

  @NotNull
  public static KarmaBasePathFinder getInstance() {
    return INSTANCE;
  }

  public String fetchBasePath(@NotNull final JSFile jsFile) {
    CachedValuesManager cachedValuesManager = CachedValuesManager.getManager(jsFile.getProject());
    return cachedValuesManager.getCachedValue(
      jsFile,
      myTestFileStructureRegistryKey,
      new CachedValueProvider<String>() {
        @Override
        public Result<String> compute() {
          String basePath = buildBasePath(jsFile);
          return Result.create(basePath, jsFile);
        }
      },
      false
    );
  }

  @Nullable
  private static String buildBasePath(@NotNull JSFile jsFile) {
    List<JSStatement> jsStatements = JsPsiUtils.listStatementsInExecutionOrder(jsFile);
    for (JSStatement jsStatement : jsStatements) {
      String basePath = extractBasePath(jsStatement);
      if (basePath != null) {
        return basePath;
      }
    }
    return null;
  }

  @Nullable
  private static String extractBasePath(@NotNull JSStatement statement) {
    JSExpressionStatement expressionStatement = ObjectUtils.tryCast(statement, JSExpressionStatement.class);
    if (expressionStatement != null) {
      JSAssignmentExpression assignmentExpression = ObjectUtils.tryCast(expressionStatement.getExpression(), JSAssignmentExpression.class);
      if (assignmentExpression != null) {
        JSDefinitionExpression lOperand = ObjectUtils.tryCast(assignmentExpression.getLOperand(), JSDefinitionExpression.class);
        if (lOperand != null && BASE_PATH_VAR_NAME.equals(lOperand.getName())) {
          JSLiteralExpression rOperand = ObjectUtils.tryCast(assignmentExpression.getROperand(), JSLiteralExpression.class);
          if (rOperand != null && rOperand.isQuotedLiteral()) {
            return StringUtil.stripQuotesAroundValue(rOperand.getText());
          }
        }
      }
    }
    return null;
  }

  public static boolean isBasePathStringLiteral(@NotNull JSLiteralExpression literalExpression) {
    if (literalExpression.isQuotedLiteral()) {
      JSAssignmentExpression assignmentExpression = ObjectUtils.tryCast(literalExpression.getParent(), JSAssignmentExpression.class);
      if (assignmentExpression != null) {
        JSDefinitionExpression lOperand = ObjectUtils.tryCast(assignmentExpression.getLOperand(), JSDefinitionExpression.class);
        return lOperand != null && BASE_PATH_VAR_NAME.equals(lOperand.getName());
      }
    }
    return false;
  }
}

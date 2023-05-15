package com.intellij.javascript.karma.config;

import com.intellij.javascript.testFramework.util.JsPsiUtils;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
      () -> {
        String basePath = buildBasePath(jsFile);
        return CachedValueProvider.Result.create(basePath, jsFile);
      },
      false
    );
  }

  @Nullable
  private static String buildBasePath(@NotNull JSFile jsFile) {
    final Ref<String> basePathRef = Ref.create(null);
    JSElementVisitor visitor = new JSElementVisitor() {
      @Override
      public void visitJSProperty(@NotNull JSProperty property) {
        String name = JsPsiUtils.getPropertyName(property);
        if (BASE_PATH_VAR_NAME.equals(name)) {
          JSLiteralExpression value = ObjectUtils.tryCast(property.getValue(), JSLiteralExpression.class);
          if (value != null && value.isQuotedLiteral()) {
            basePathRef.set(StringUtil.unquoteString(value.getText()));
          }
        }
      }

      @Override
      public void visitElement(@NotNull PsiElement element) {
        ProgressIndicatorProvider.checkCanceled();
        if (basePathRef.isNull()) {
          element.acceptChildren(this);
        }
      }
    };
    visitor.visitJSFile(jsFile);
    return basePathRef.get();
  }

  public static boolean isBasePathStringLiteral(@NotNull JSLiteralExpression literalExpression) {
    if (literalExpression.isQuotedLiteral()) {
      JSProperty property = ObjectUtils.tryCast(literalExpression.getParent(), JSProperty.class);
      if (property != null) {
        String name = JsPsiUtils.getPropertyName(property);
        return BASE_PATH_VAR_NAME.equals(name);
      }
    }
    return false;
  }

}

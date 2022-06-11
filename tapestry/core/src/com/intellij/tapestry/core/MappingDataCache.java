package com.intellij.tapestry.core;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;

@VisibleForTesting
public final class MappingDataCache {
  private static final String TAPESTRY_MAPPING_FQN = "org.apache.tapestry5.services.LibraryMapping";
  private static final String TAPESTRY_MAPPING_TEST_FQN = "LibraryMapping";

  @NotNull
  public static Map<String, String> getMappingData(@NotNull PsiFile file) {
    return CachedValuesManager.getProjectPsiDependentCache(file, MappingDataCache::computeMappingData);
  }

  private static Map<String, String> computeMappingData(PsiFile file) {
    final Map<String, String> result = new HashMap<>();
    if (file instanceof PsiCompiledElement) {
      PsiElement element = file.getNavigationElement();
      if (element != file && element instanceof PsiFile) {
        file = (PsiFile)element;
      } else {
        PsiElement mirror = ((PsiCompiledElement)file).getMirror();
        if (mirror instanceof PsiFile) file = (PsiFile)mirror;
      }
    }
    file.accept(new JavaRecursiveElementVisitor() {
      @Override
      public void visitNewExpression(@NotNull PsiNewExpression expression) {
        final PsiJavaCodeReferenceElement classReference = expression.getClassReference();

        final String classReferenceQualifiedName = classReference != null ? classReference.getQualifiedName() : null;
        if (TAPESTRY_MAPPING_FQN.equals(classReferenceQualifiedName) ||
            ApplicationManager.getApplication().isUnitTestMode() && TAPESTRY_MAPPING_TEST_FQN.equals(classReferenceQualifiedName)
           ) {
          final PsiExpressionList argumentList = expression.getArgumentList();
          final PsiExpression[] expressions = argumentList == null ? null : argumentList.getExpressions();
          if (expressions != null && expressions.length == 2) {
            String prefix = calculateExprValue(expressions[0]);
            String packageName = calculateExprValue(expressions[1]);

            if (prefix != null && packageName != null) {
              result.put(prefix, packageName);
            }
          }
        }
        super.visitNewExpression(expression);
      }
    });
    return result;
  }

  private static String calculateExprValue(PsiExpression expression) {
    if (expression instanceof PsiJavaReference) {
      final PsiElement resolve = ((PsiJavaReference)expression).resolve();
      if (resolve instanceof PsiField &&
          ((PsiField)resolve).hasModifierProperty(PsiModifier.FINAL) &&
          ((PsiField)resolve).hasInitializer()) {
        final Object constantValue = ((PsiField)resolve).computeConstantValue();
        if (constantValue instanceof String) return (String)constantValue;
      }
    } else if (expression instanceof PsiLiteralExpression) {
      return StringUtil.unquoteString(expression.getText());
    }
    return null;
  }
}

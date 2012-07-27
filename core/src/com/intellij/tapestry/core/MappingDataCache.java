package com.intellij.tapestry.core;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileBasedUserDataCache;
import com.intellij.psi.util.CachedValue;
import com.intellij.tapestry.core.util.ClassUtils;
import gnu.trove.THashMap;

import java.util.Map;

/**
 * @author: Fedor.Korotkov
 */
public class MappingDataCache extends FileBasedUserDataCache<Map<String, String>> {
  private final Key<CachedValue<Map<String, String>>> ourCachedIdsValueKey = Key.create("tapestry.mapping.cached.value");
  private static final String TAPESTRY_MAPPING_FQN = "org.apache.tapestry5.services.LibraryMapping";
  private static final String TAPESTRY_MAPPING_TEST_FQN = "LibraryMapping";

  @Override
  protected Key<CachedValue<Map<String, String>>> getKey() {
    return ourCachedIdsValueKey;
  }

  @Override
  protected Map<String, String> doCompute(PsiFile file) {
    final Map<String, String> result = new THashMap<String, String>();
    file.accept(new JavaRecursiveElementVisitor() {
      @Override
      public void visitNewExpression(PsiNewExpression expression) {
        final PsiJavaCodeReferenceElement classReference = expression.getClassReference();
        final String fqn = ApplicationManager.getApplication().isUnitTestMode() ? TAPESTRY_MAPPING_TEST_FQN : TAPESTRY_MAPPING_FQN;
        if (classReference != null && fqn.equals(classReference.getQualifiedName())) {
          final PsiExpressionList argumentList = expression.getArgumentList();
          final PsiExpression[] expressions = argumentList == null ? null : argumentList.getExpressions();
          if (expressions != null && expressions.length == 2 && ClassUtils.instanceOf(expressions, PsiLiteralExpression.class)) {
            result.put(
              StringUtil.unquoteString(expressions[0].getText()),
              StringUtil.unquoteString(expressions[1].getText())
            );
          }
        }
        super.visitNewExpression(expression);
      }
    });
    return result;
  }
}

package org.angularjs.index;

import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Dennis.Ushakov
 */
public class AngularModuleIndex extends AngularIndexBase {
  public static final StubIndexKey<String, JSImplicitElementProvider> KEY = StubIndexKey.createIndexKey("angularjs.module.index");

  @Override
  public @NotNull StubIndexKey<String, JSImplicitElementProvider> getKey() {
    return KEY;
  }

  // todo have common method for angular module call
  public static List<String> findDependenciesInModuleDeclaration(JSCallExpression call) {
    final JSExpression methodExpression = call.getMethodExpression();
    if (methodExpression instanceof JSReferenceExpression &&
        JSSymbolUtil
          .isAccurateReferenceExpressionName((JSReferenceExpression)methodExpression, "angular", AngularJSIndexingHandler.MODULE)) {
      final JSExpression[] arguments = call.getArguments();
      if (arguments.length > 1 && arguments[0] instanceof JSLiteralExpression
          && ((JSLiteralExpression)arguments[0]).isQuotedLiteral()
          && arguments[1] instanceof JSArrayLiteralExpression array) {
        final JSExpression[] children = array.getExpressions();
        final Set<String> dependencies = new HashSet<>();
        for (JSExpression child : children) {
          if (child instanceof JSLiteralExpression && ((JSLiteralExpression)child).isQuotedLiteral()) {
            dependencies.add(StringUtil.unquoteString(child.getText()));
          }
        }
        if (!dependencies.isEmpty()) {
          return new ArrayList<>(dependencies);
        }
      }
    }
    return null;
  }
}

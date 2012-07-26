package com.google.jstestdriver.idea.js;

import com.intellij.lang.javascript.index.JSFileIndexerFactory;
import com.intellij.lang.javascript.index.JSNamespace;
import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NonNls;

/**
 * User: ksafonov
 */
public class JsTestDriverIndexer extends JSElementVisitor {

  public static class Factory extends JSFileIndexerFactory {

    @Override
    protected int getVersion() {
      return 1;
    }

    @Override
    public JSElementVisitor createVisitor(final JSNamespace topLevelNs,
                                          final JSSymbolUtil.JavaScriptSymbolProcessorEx indexer,
                                          final PsiFile file) {
      return new JsTestDriverIndexer(indexer);
    }
  }

  private final JSSymbolUtil.JavaScriptSymbolProcessorEx myIndexer;

  public JsTestDriverIndexer(final JSSymbolUtil.JavaScriptSymbolProcessorEx indexer) {
    myIndexer = indexer;
  }

  @Override
  public void visitJSExpressionStatement(final JSExpressionStatement node) {
    if (node.getExpression() instanceof JSCallExpression) {
      JSCallExpression callExpr = (JSCallExpression)node.getExpression();

      final JSExpression methodExpression = callExpr.getMethodExpression();

      if (methodExpression instanceof JSReferenceExpression &&
          !(node instanceof JSNewExpression)
        ) {
        final JSReferenceExpression referenceExpression = (JSReferenceExpression)methodExpression;
        final JSExpression qualifier = referenceExpression.getQualifier();
        final @NonNls String calledMethodName = referenceExpression.getReferencedName();

        if ("TestCase".equals(calledMethodName) && qualifier == null) {
          myIndexer.setIsTestFile();
        }
      }
    }
  }
}

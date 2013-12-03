package org.angularjs.index;

import com.intellij.lang.javascript.documentation.JSDocumentationProcessor;
import com.intellij.lang.javascript.index.AngularControllerIndex;
import com.intellij.lang.javascript.index.AngularDirectivesIndex;
import com.intellij.lang.javascript.index.FrameworkIndexingHandler;
import com.intellij.lang.javascript.index.JSSymbolVisitor;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSIndexingHandler extends FrameworkIndexingHandler {
  public static final String DIRECTIVE = "directive";

  @Override
  public void processCallExpression(JSCallExpression callExpression, JSSymbolVisitor visitor) {
    JSReferenceExpression callee = (JSReferenceExpression)callExpression.getMethodExpression();
    JSExpression qualifier = callee.getQualifier();

    if (qualifier == null) return;

    if (DIRECTIVE.equals(callee.getReferencedName())) {
      JSExpression[] arguments = callExpression.getArguments();
      if (arguments.length > 0) {
        JSExpression argument = arguments[0];
        if (argument instanceof JSLiteralExpression && ((JSLiteralExpression)argument).isQuotedLiteral()) {
          visitor.storeAdditionalData(AngularDirectivesIndex.INDEX_ID.toString(),
                                      getAttributeName(argument.getText()), argument.getTextOffset());
        }
      }
    } else if ("controller".equals(callee.getReferencedName())) {
      JSExpression[] arguments = callExpression.getArguments();
      if (arguments.length > 0) {
        JSExpression argument = arguments[0];
        if (argument instanceof JSLiteralExpression && ((JSLiteralExpression)argument).isQuotedLiteral()) {
          visitor.storeAdditionalData(AngularControllerIndex.INDEX_ID.toString(),
                                      StringUtil.unquoteString(argument.getText()), argument.getTextOffset());
        }
      }
    }
  }

  @Override
  public void processCommentMatch(@NotNull final PsiComment comment,
                                  @NotNull JSDocumentationProcessor.MetaDocType type,
                                  @Nullable String matchName,
                                  @Nullable String matchValue,
                                  @Nullable String remainingLineContent,
                                  @NotNull String line,
                                  String patternMatched,
                                  JSSymbolVisitor visitor) {
    if (type == JSDocumentationProcessor.MetaDocType.NAME &&
        matchName != null && matchName.contains(DIRECTIVE) && hasDirectiveName(remainingLineContent)) {
      assert remainingLineContent != null;
      visitor.storeAdditionalData(AngularDirectivesIndex.INDEX_ID.toString(), getAttributeName(remainingLineContent.substring(1)),
                                  comment.getTextOffset() + comment.getText().indexOf(matchName));
    }
  }

  private static boolean hasDirectiveName(String remainingLineContent) {
    return remainingLineContent != null && remainingLineContent.startsWith(":") && !remainingLineContent.contains(".");
  }

  private static String getAttributeName(final String text) {
    final String[] split = StringUtil.unquoteString(text).split("(?=[A-Z])");
    for (int i = 0; i < split.length; i++) {
      split[i] = StringUtil.decapitalize(split[i]);
    }
    return StringUtil.join(split, "-");
  }
}

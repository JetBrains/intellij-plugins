package org.angularjs.index;

import com.intellij.lang.javascript.documentation.JSDocumentationProcessor;
import com.intellij.lang.javascript.index.*;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSIndexingHandler extends FrameworkIndexingHandler {
  public static final String MARKER = "!!!";
  public static Set<String> INTERESTING_METHODS = new HashSet<String>();
  public static final String CONTROLLER = "controller";
  public static final String DIRECTIVE = "directive";

  static {
    Collections.addAll(INTERESTING_METHODS, CONTROLLER, DIRECTIVE, "filter", "service",
                       "factory", "module", "value", "constant", "provider");
  }

  @Override
  public void processCallExpression(JSCallExpression callExpression, JSSymbolVisitor visitor) {
    JSReferenceExpression callee = (JSReferenceExpression)callExpression.getMethodExpression();
    JSExpression qualifier = callee.getQualifier();

    if (qualifier == null) return;

    final String command = callee.getReferencedName();
    if (DIRECTIVE.equals(command)) {
      JSExpression[] arguments = callExpression.getArguments();
      if (arguments.length > 0) {
        JSExpression argument = arguments[0];
        if (argument instanceof JSLiteralExpression && ((JSLiteralExpression)argument).isQuotedLiteral()) {
          final String attributeName = getAttributeName(argument.getText());
          visitor.storeAdditionalData(argument, AngularDirectivesIndex.INDEX_ID.toString(), attributeName, argument.getTextOffset());
          visitor.storeAdditionalData(argument, AngularSymbolIndex.INDEX_ID.toString(), attributeName, argument.getTextOffset());
        }
      }
    } else if (CONTROLLER.equals(command)) {
      JSExpression[] arguments = callExpression.getArguments();
      if (arguments.length > 0) {
        JSExpression argument = arguments[0];
        if (argument instanceof JSLiteralExpression && ((JSLiteralExpression)argument).isQuotedLiteral()) {
          visitor.storeAdditionalData(argument, AngularControllerIndex.INDEX_ID.toString(),
                                      StringUtil.unquoteString(argument.getText()), argument.getTextOffset());
        }
      }
    }
    if (INTERESTING_METHODS.contains(command)) {
      JSExpression[] arguments = callExpression.getArguments();
      if (arguments.length > 0) {
        JSExpression argument = arguments[0];
        if (argument instanceof JSLiteralExpression && ((JSLiteralExpression)argument).isQuotedLiteral()) {
          visitor.storeAdditionalData(argument, AngularSymbolIndex.INDEX_ID.toString(), StringUtil.unquoteString(argument.getText()),
                                      argument.getTextOffset());
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
      final int offset = comment.getTextOffset() + comment.getText().indexOf(matchName);
      final String attributeName = getAttributeName(remainingLineContent.substring(1));
      visitor.storeAdditionalData(comment, AngularDirectivesIndex.INDEX_ID.toString(), attributeName, offset);
      visitor.storeAdditionalData(comment, AngularSymbolIndex.INDEX_ID.toString(), attributeName, offset);
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

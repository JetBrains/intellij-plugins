package org.angularjs.index;

import com.intellij.lang.javascript.documentation.JSDocumentationProcessor;
import com.intellij.lang.javascript.index.*;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.util.Function;
import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSIndexingHandler extends FrameworkIndexingHandler {
  private static final Map<String, ID<String, Void>> INDEXERS = new HashMap<String, ID<String, Void>>();
  private static final Map<String, Function<String, String>> NAME_CONVERTERS = new HashMap<String, Function<String, String>>();

  public static Set<String> INTERESTING_METHODS = new HashSet<String>();
  public static final String CONTROLLER = "controller";
  public static final String DIRECTIVE = "directive";
  public static final String MODULE = "module";

  static {
    Collections.addAll(INTERESTING_METHODS, "filter", "service", "factory", "value", "constant", "provider");

    INDEXERS.put(DIRECTIVE, AngularDirectivesIndex.INDEX_ID);
    NAME_CONVERTERS.put(DIRECTIVE, new Function<String, String>() {
      @Override
      public String fun(String s) {
        return getAttributeName(s);
      }
    });

    INDEXERS.put(CONTROLLER, AngularControllerIndex.INDEX_ID);
    INDEXERS.put(MODULE, AngularModuleIndex.INDEX_ID);
  }

  @Override
  public void processCallExpression(JSCallExpression callExpression, JSSymbolVisitor visitor) {
    JSReferenceExpression callee = (JSReferenceExpression)callExpression.getMethodExpression();
    JSExpression qualifier = callee.getQualifier();

    if (qualifier == null) return;

    final String command = callee.getReferencedName();
    final ID<String, Void> index = INDEXERS.get(command);
    if (index != null) {
      JSExpression[] arguments = callExpression.getArguments();
      if (arguments.length > 0) {
        JSExpression argument = arguments[0];
        if (argument instanceof JSLiteralExpression && ((JSLiteralExpression)argument).isQuotedLiteral()) {
          final Function<String, String> converter = NAME_CONVERTERS.get(command);
          final String defaultName = StringUtil.unquoteString(argument.getText());
          final String name = converter != null ? converter.fun(argument.getText()) : defaultName;
          if (name != null) {
            visitor.storeAdditionalData(argument, index.toString(), name, argument.getTextOffset());
            visitor.storeAdditionalData(argument, AngularSymbolIndex.INDEX_ID.toString(), name, argument.getTextOffset());
            if (!StringUtil.equals(defaultName, name)) {
              visitor.storeAdditionalData(argument, AngularSymbolIndex.INDEX_ID.toString(), defaultName, argument.getTextOffset());
            }
          }
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
      if (attributeName != null) {
        visitor.storeAdditionalData(comment, AngularDirectivesIndex.INDEX_ID.toString(), attributeName, offset);
        visitor.storeAdditionalData(comment, AngularSymbolIndex.INDEX_ID.toString(), attributeName, offset);
        visitor.storeAdditionalData(comment, AngularSymbolIndex.INDEX_ID.toString(), remainingLineContent.substring(1), offset);
      }
    }
  }

  private static boolean hasDirectiveName(String remainingLineContent) {
    return remainingLineContent != null && remainingLineContent.startsWith(":") && !remainingLineContent.contains(".");
  }

  private static String getAttributeName(final String text) {
    if (text.contains("#")) return null;
    final String[] split = StringUtil.unquoteString(text).split("(?=[A-Z])");
    for (int i = 0; i < split.length; i++) {
      split[i] = StringUtil.decapitalize(split[i]);
    }
    return StringUtil.join(split, "-");
  }
}

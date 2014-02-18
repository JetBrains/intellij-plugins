package org.angularjs.index;

import com.intellij.lang.javascript.documentation.JSDocumentationProcessor;
import com.intellij.lang.javascript.index.FrameworkIndexingHandler;
import com.intellij.lang.javascript.index.JSSymbolVisitor;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  public static final String FILTER = "filter";

  static {
    Collections.addAll(INTERESTING_METHODS, "service", "factory", "value", "constant", "provider");

    INDEXERS.put(DIRECTIVE, AngularDirectivesIndex.INDEX_ID);
    NAME_CONVERTERS.put(DIRECTIVE, new Function<String, String>() {
      @Override
      public String fun(String s) {
        return getAttributeName(s);
      }
    });

    INDEXERS.put(CONTROLLER, AngularControllerIndex.INDEX_ID);
    INDEXERS.put(MODULE, AngularModuleIndex.INDEX_ID);
    INDEXERS.put(FILTER, AngularFilterIndex.INDEX_ID);
  }

  private static final String RESTRICT = "@restrict";
  private static final String ELEMENT = "@element";
  private static final String PARAM = "@param";
  private static final Pattern RESTRICT_PATTERN = Pattern.compile(RESTRICT + "\\s*(.*)");
  private static final Pattern ELEMENT_PATTERN = Pattern.compile(ELEMENT + "\\s*(.*)");
  private static final Pattern PARAM_PATTERN = Pattern.compile(PARAM + "\\s*\\{([^}]*)}");

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
          final String argumentText = argument.getText();
          storeAdditionalData(visitor, index, argument, command, argumentText, argument.getTextOffset(), null);
        } else if (argument instanceof JSObjectLiteralExpression) {
          for (JSProperty property : ((JSObjectLiteralExpression)argument).getProperties()) {
            final String argumentText = property.getName();
            storeAdditionalData(visitor, index, property, command, argumentText, property.getTextOffset(), null);
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
                                      argument.getTextOffset(), null);
        }
      }
    }
  }

  private static void storeAdditionalData(final JSSymbolVisitor visitor,
                                          final ID<String, Void> index,
                                          final PsiElement declaration,
                                          final String command,
                                          final String argumentText,
                                          final int offset,
                                          final String value) {
    final Function<String, String> converter = NAME_CONVERTERS.get(command);
    final String defaultName = StringUtil.unquoteString(argumentText);
    final String name = converter != null ? converter.fun(argumentText) : defaultName;
    visitor.storeAdditionalData(declaration, index.toString(), name, offset, value);
    visitor.storeAdditionalData(declaration, AngularSymbolIndex.INDEX_ID.toString(), name, offset, null);
    if (!StringUtil.equals(defaultName, name)) {
      visitor.storeAdditionalData(declaration, AngularSymbolIndex.INDEX_ID.toString(), defaultName, offset, null);
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
    if (type != JSDocumentationProcessor.MetaDocType.NAME || matchName == null || !hasDirectiveName(remainingLineContent)) return;
    assert remainingLineContent != null;

    final int offset = comment.getTextOffset() + comment.getText().indexOf(matchName);
    if (matchName.contains(DIRECTIVE)) {
      final String restrictions = calculateRestrictions(comment);
      storeAdditionalData(visitor, AngularDirectivesDocIndex.INDEX_ID, comment, DIRECTIVE,
                          remainingLineContent.substring(1), offset, restrictions);
    } else if (matchName.contains(FILTER)) {
      storeAdditionalData(visitor, AngularFilterIndex.INDEX_ID, comment, FILTER, remainingLineContent.substring(1), offset, null);
    }
  }

  private static String calculateRestrictions(PsiComment comment) {
    final String commentText = comment.getText();
    String restrict = "A";
    String tag = "";
    String param = "";
    for (String line : StringUtil.splitByLines(commentText)) {
      restrict = getParamValue(restrict, line, RESTRICT_PATTERN, RESTRICT);
      tag = getParamValue(tag, line, ELEMENT_PATTERN, ELEMENT);
      param = getParamValue(param, line, PARAM_PATTERN, PARAM);
    }
    return restrict.trim() + ";" + tag.trim() + ";" + param.trim();
  }

  private static String getParamValue(String previousValue, String line, final Pattern pattern, final String docTag) {
    if (line.contains(docTag)) {
      final Matcher matcher = pattern.matcher(line);
      if (matcher.find()) {
        previousValue = matcher.group(1);
      }
    }
    return previousValue;
  }

  private static boolean hasDirectiveName(String remainingLineContent) {
    return remainingLineContent != null && remainingLineContent.startsWith(":") &&
           !remainingLineContent.contains(".") && !remainingLineContent.contains("#");
  }

  private static String getAttributeName(final String text) {
    final String[] split = StringUtil.unquoteString(text).split("(?=[A-Z])");
    for (int i = 0; i < split.length; i++) {
      split[i] = StringUtil.decapitalize(split[i]);
    }
    return StringUtil.join(split, "-");
  }
}

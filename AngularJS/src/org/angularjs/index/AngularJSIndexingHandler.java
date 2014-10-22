package org.angularjs.index;

import com.intellij.lang.javascript.documentation.JSDocumentationProcessor;
import com.intellij.lang.javascript.documentation.JSDocumentationUtils;
import com.intellij.lang.javascript.index.*;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.indexing.ID;
import org.angularjs.codeInsight.DirectiveUtil;
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
  private static final Map<String, Function<Pair<JSSymbolVisitor, PsiElement>, String>> DATA_CALCULATORS = new HashMap<String, Function<Pair<JSSymbolVisitor, PsiElement>, String>>();

  public static final Set<String> INTERESTING_METHODS = new HashSet<String>();
  public static final Set<String> INJECTABLE_METHODS = new HashSet<String>();
  public static final String CONTROLLER = "controller";
  public static final String DIRECTIVE = "directive";
  public static final String MODULE = "module";
  public static final String FILTER = "filter";
  public static final String DEFAULT_RESTRICTIONS = "D";

  static {
    Collections.addAll(INTERESTING_METHODS, "service", "factory", "value", "constant", "provider");

    INJECTABLE_METHODS.addAll(INTERESTING_METHODS);
    Collections.addAll(INJECTABLE_METHODS, CONTROLLER, DIRECTIVE, MODULE);

    INDEXERS.put(DIRECTIVE, AngularDirectivesIndex.INDEX_ID);
    NAME_CONVERTERS.put(DIRECTIVE, new Function<String, String>() {
      @Override
      public String fun(String s) {
        return DirectiveUtil.getAttributeName(s);
      }
    });
    DATA_CALCULATORS.put(DIRECTIVE, new Function<Pair<JSSymbolVisitor, PsiElement>, String>() {
      @Override
      public String fun(Pair<JSSymbolVisitor, PsiElement> pair) {
        return calculateRestrictions(pair.first, pair.second);
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

  public static boolean isInjectable(PsiElement context) {
    final JSCallExpression call = PsiTreeUtil.getParentOfType(context, JSCallExpression.class, false, JSBlockStatement.class);
    if (call != null) {
      JSReferenceExpression callee = (JSReferenceExpression)call.getMethodExpression();
      JSExpression qualifier = callee.getQualifier();
      return qualifier != null && INJECTABLE_METHODS.contains(callee.getReferencedName());
    }
    return false;
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
          final String argumentText = argument.getText();
          final Function<Pair<JSSymbolVisitor, PsiElement>, String> calculator = DATA_CALCULATORS.get(command);
          final String data = calculator != null ? calculator.fun(Pair.<JSSymbolVisitor, PsiElement>create(visitor, argument)) : null;
          storeAdditionalData(visitor, index, argument, command, argumentText, argument.getTextOffset(), data);
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

    if (INJECTABLE_METHODS.contains(command)) {
      JSExpression[] arguments = callExpression.getArguments();
      if (arguments.length > 0) {
        JSExpression argument = arguments[0];
        if (argument instanceof JSLiteralExpression && ((JSLiteralExpression)argument).isQuotedLiteral()) {
          generateNamespace(visitor, argument);
        }
      }
    }

    if ("$interpolateProvider".equals(qualifier.getText())) {
      if ("startSymbol".equals(command) || "endSymbol".equals(command)) {
        JSExpression[] arguments = callExpression.getArguments();
        if (arguments.length > 0) {
          JSExpression argument = arguments[0];
          if (argument instanceof JSLiteralExpression && ((JSLiteralExpression)argument).isQuotedLiteral()) {
            String interpolation = StringUtil.unquoteString(argument.getText());
            // '//' interpolations are usually dragged from examples folder and not supposed to be used by real users
            if ("//".equals(interpolation)) return;

            visitor.storeAdditionalData(argument, AngularInjectionDelimiterIndex.INDEX_ID.toString(), command,
                                        argument.getTextOffset(), interpolation);
          }
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
    if (type != JSDocumentationProcessor.MetaDocType.NAME || matchName == null) return;
    assert remainingLineContent != null;

    final String commentText = comment.getText();
    if (!commentText.contains("@ngdoc")) return;

    final String[] commentLines = StringUtil.splitByLines(commentText);
    final int offset = comment.getTextOffset() + commentText.indexOf(matchName);
    for (int i = 0; i < Math.min(commentLines.length, 3); i++) {
      String commentLine = commentLines[i];
      if (!commentLine.contains("@ngdoc")) continue;

      final String name = remainingLineContent.isEmpty() ? matchName : remainingLineContent.substring(1);
      if (commentLine.contains(DIRECTIVE)) {
        final String restrictions = calculateRestrictions(commentLines);
        storeAdditionalData(visitor, AngularDirectivesDocIndex.INDEX_ID, comment, DIRECTIVE, name, offset, restrictions);
        return;
      }
      else if (commentLine.contains(FILTER)) {
        storeAdditionalData(visitor, AngularFilterIndex.INDEX_ID, comment, FILTER, name, offset, null);
        return;
      }
    }
  }

  private static String calculateRestrictions(final String[] commentLines) {
    String restrict = DEFAULT_RESTRICTIONS;
    String tag = "";
    String param = "";
    StringBuilder attributes = new StringBuilder();
    for (String line : commentLines) {
      restrict = getParamValue(restrict, line, RESTRICT_PATTERN, RESTRICT);
      tag = getParamValue(tag, line, ELEMENT_PATTERN, ELEMENT);
      final int start = line.indexOf(PARAM);
      if (start >= 0) {
        final JSDocumentationUtils.DocTag docTag = JSDocumentationUtils.getDocTag(line.substring(start));
        if (docTag != null) {
          param = docTag.matchValue != null ? docTag.matchValue : param;
          if (attributes.length() > 0) attributes.append(",");
          attributes.append(docTag.matchName);
        }
      }
    }
    return restrict.trim() + ";" + tag.trim() + ";" + param.trim() + ";" + attributes.toString().trim();
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


  private static void generateNamespace(JSSymbolVisitor visitor, PsiElement second) {
    final String namespace = StringUtil.unquoteString(second.getText());
    visitor.addClassFromQName((JSExpression)second, namespace);
    final JSFunction function = findFunction(visitor, second);
    final JSNamespace ns = visitor.findNsForExpr((JSExpression)second);
    if (function != null && ns != null) {
      visitor.visitWithNamespace(ns, function, false);
    }
  }

  private static String calculateRestrictions(JSSymbolVisitor visitor, PsiElement element) {
    final Ref<String> restrict = Ref.create(DEFAULT_RESTRICTIONS);
    final Ref<String> scope = Ref.create("");
    final JSFunction function = findFunction(visitor, element);
    if (function != null) {
      function.accept(new JSRecursiveElementVisitor() {
        @Override
        public void visitJSProperty(JSProperty node) {
          final String name = node.getName();
          final JSExpression value = node.getValue();
          if ("restrict".equals(name)) {
            if (value instanceof JSLiteralExpression && ((JSLiteralExpression)value).isQuotedLiteral()) {
              restrict.set(StringUtil.unquoteString(value.getText()));
            }
          } else if ("scope".equals(name)) {
            if (value instanceof JSObjectLiteralExpression) {
              scope.set(StringUtil.join(((JSObjectLiteralExpression)value).getProperties(), new Function<JSProperty, String>() {
                @Override
                public String fun(JSProperty property) {
                  return property.getName();
                }
              }, ","));
            }
          }
        }
      });
    }
    return restrict.get().trim() + ";;;" + scope.get();
  }

  private static JSFunction findFunction(JSSymbolVisitor visitor, PsiElement element) {
    JSFunction function = PsiTreeUtil.getNextSiblingOfType(element, JSFunction.class);
    if (function == null) {
      final JSExpression expression = PsiTreeUtil.getNextSiblingOfType(element, JSExpression.class);
      function = findDeclaredFunction(visitor, expression);
    }
    if (function == null) {
      JSArrayLiteralExpression array = PsiTreeUtil.getNextSiblingOfType(element, JSArrayLiteralExpression.class);
      function = PsiTreeUtil.findChildOfType(array, JSFunction.class);
      if (function == null) {
        final JSExpression candidate = array != null ?PsiTreeUtil.getPrevSiblingOfType(array.getLastChild(), JSExpression.class) : null;
        function = findDeclaredFunction(visitor, candidate);
      }
    }
    return function;
  }

  private static JSFunction findDeclaredFunction(JSSymbolVisitor visitor, JSExpression expression) {
    final JSElement candidate = visitor.getOperandFromVarContext(expression);
    return candidate instanceof JSFunction ? (JSFunction)candidate : null;
  }

  public static class Factory extends JSFileIndexerFactory {
    @Override
    protected int getVersion() {
      return AngularIndexUtil.BASE_VERSION;
    }

    @Nullable
    @Override
    public JSElementVisitor createVisitor(JSNamespace topLevelNs,
                                          JSSymbolUtil.JavaScriptSymbolProcessorEx indexer,
                                          PsiFile file) {
      return null;
    }
  }
}

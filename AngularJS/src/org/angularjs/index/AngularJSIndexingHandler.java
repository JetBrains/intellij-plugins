package org.angularjs.index;

import com.intellij.lang.javascript.JSDocTokenTypes;
import com.intellij.lang.javascript.documentation.JSDocumentationUtils;
import com.intellij.lang.javascript.index.*;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment;
import com.intellij.lang.javascript.psi.jsdoc.JSDocTag;
import com.intellij.lang.javascript.psi.jsdoc.JSDocTagValue;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.indexing.ID;
import org.angularjs.codeInsight.DirectiveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSIndexingHandler extends FrameworkIndexingHandler {
  private static final Map<String, ID<String, Void>> INDEXERS = new HashMap<String, ID<String, Void>>();
  private static final Map<String, Function<String, String>> NAME_CONVERTERS = new HashMap<String, Function<String, String>>();
  private static final Map<String, Function<PsiElement, String>> DATA_CALCULATORS = new HashMap<String, Function<PsiElement, String>>();

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
    DATA_CALCULATORS.put(DIRECTIVE, new Function<PsiElement, String>() {
      @Override
      public String fun(PsiElement element) {
        return calculateRestrictions(element);
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
  public void processCallExpression(JSCallExpression callExpression, @NotNull JSIndexContentBuilder builder) {
    final JSExpression methodExpression = callExpression.getMethodExpression();
    if (!(methodExpression instanceof JSReferenceExpression)) return;
    JSReferenceExpression callee = (JSReferenceExpression)methodExpression;
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
          final Function<PsiElement, String> calculator = DATA_CALCULATORS.get(command);
          final String data = calculator != null ? calculator.fun(argument) : null;
          storeAdditionalData(builder, index, argument, command, argumentText, argument.getTextOffset(), data);
        } else if (argument instanceof JSObjectLiteralExpression) {
          for (JSProperty property : ((JSObjectLiteralExpression)argument).getProperties()) {
            final String argumentText = property.getName();
            storeAdditionalData(builder, index, property, command, argumentText, property.getTextOffset(), null);
          }
        }
      }
    }

    if (INTERESTING_METHODS.contains(command)) {
      JSExpression[] arguments = callExpression.getArguments();
      if (arguments.length > 0) {
        JSExpression argument = arguments[0];
        if (argument instanceof JSLiteralExpression && ((JSLiteralExpression)argument).isQuotedLiteral()) {
          builder.storeAdditionalData(AngularSymbolIndex.INDEX_ID.toString(), StringUtil.unquoteString(argument.getText()),
                                      serializeDataValue(false, argument.getTextOffset(), null));
        }
      }
    }

    if (INJECTABLE_METHODS.contains(command)) {
      JSExpression[] arguments = callExpression.getArguments();
      if (arguments.length > 0) {
        JSExpression argument = arguments[0];
        if (argument instanceof JSLiteralExpression && ((JSLiteralExpression)argument).isQuotedLiteral()) {
          generateNamespace(builder, argument);
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

            builder.storeAdditionalData(AngularInjectionDelimiterIndex.INDEX_ID.toString(), command,
                                        serializeDataValue(false, argument.getTextOffset(), interpolation));
          }
        }
      }
    }
  }

  private static void storeAdditionalData(final JSIndexContentBuilder builder,
                                          final ID<String, Void> index,
                                          final PsiElement declaration,
                                          final String command,
                                          final String argumentText,
                                          final int offset,
                                          final String value) {
    final Function<String, String> converter = NAME_CONVERTERS.get(command);
    final String defaultName = StringUtil.unquoteString(argumentText);
    final String name = converter != null ? converter.fun(argumentText) : defaultName;
    final boolean isComment = declaration instanceof PsiComment;
    builder.storeAdditionalData(index.toString(), name, serializeDataValue(isComment, offset, value));
    builder.storeAdditionalData(AngularSymbolIndex.INDEX_ID.toString(), name, serializeDataValue(isComment, offset, null));
    if (!StringUtil.equals(defaultName, name)) {
      builder.storeAdditionalData(AngularSymbolIndex.INDEX_ID.toString(), defaultName, serializeDataValue(isComment, offset, null));
    }
  }

  @NotNull
  private static byte[] serializeDataValue(boolean isComment, int offset, @Nullable String value) {
    final byte[] valueBytes = value != null ? value.getBytes(Charset.forName("UTF-8")) : new byte[0];
    final byte[] result = new byte[5 + valueBytes.length];
    result[0] = (byte)(isComment ? 1 : 0);
    for (int i = 4; i >= 1; i--) {
      result[i] = (byte)(offset & 0xFF);
      offset >>= 8;
    }
    System.arraycopy(valueBytes, 0, result, 5, result.length - 5);
    return result;
  }

  @NotNull
  public static Trinity<Boolean, Integer, String> deserializeDataValue(@NotNull byte[] serializedValue) {
    assert serializedValue[0] == 0 || serializedValue[0] == 1;
    final Boolean isComment = Boolean.valueOf(serializedValue[0] == 1);
    int offset = 0;
    for (int i = 1; i < 5; i++) {
      offset |= serializedValue[i];
      if (i < 4) offset >>= 8;
    }
    final String value = serializedValue.length > 5 ?
                         new String(Arrays.copyOfRange(serializedValue, 5, serializedValue.length), Charset.forName("UTF-8")) :
                         null;
    return Trinity.create(isComment, offset, value);
  }

  @Override
  public void processJSDocComment(final JSDocComment comment, @NotNull JSIndexContentBuilder builder) {
    JSDocTag ngdocTag = null;
    JSDocTag nameTag = null;
    for (JSDocTag tag : comment.getTags()) {
      if ("ngdoc".equals(tag.getName())) ngdocTag = tag;
      else if ("name".equals(tag.getName())) nameTag = tag;
    }
    if (ngdocTag != null && nameTag != null) {
      final JSDocTagValue nameValue = nameTag.getValue();
      String name = nameValue != null ? nameValue.getText() : null;
      if (name != null) name = name.substring(name.indexOf(':') + 1);

      String ngdocValue = null;
      PsiElement nextSibling = ngdocTag.getNextSibling();
      if (nextSibling instanceof PsiWhiteSpace) nextSibling = nextSibling.getNextSibling();
      if (nextSibling != null && nextSibling.getNode().getElementType() == JSDocTokenTypes.DOC_COMMENT_DATA) {
        ngdocValue = nextSibling.getText();
      }
      if (ngdocValue != null && name != null) {
        final String[] commentLines = StringUtil.splitByLines(comment.getText());
        final int offset = nameTag.getTextOffset();

        if (ngdocValue.contains(DIRECTIVE)) {
          final String restrictions = calculateRestrictions(commentLines);
          storeAdditionalData(builder, AngularDirectivesDocIndex.INDEX_ID, comment, DIRECTIVE, name, offset, restrictions);
        }
        else if (ngdocValue.contains(FILTER)) {
          storeAdditionalData(builder, AngularFilterIndex.INDEX_ID, comment, FILTER, name, offset, null);
        }
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


  private static void generateNamespace(JSIndexContentBuilder builder, PsiElement second) {
    final String namespace = StringUtil.unquoteString(second.getText());
    JSQualifiedNameImpl qName = JSQualifiedNameImpl.fromQualifiedName(namespace);
    if (qName == null) return;
    JSImplicitElementImpl.Builder elementBuilder =
      new JSImplicitElementImpl.Builder(qName, second)
        .setType(JSImplicitElement.Type.Class);
    builder.addImplicitElement(qName.getName(), new JSImplicitElementsIndex.JSElementProxy(elementBuilder, second.getTextOffset()));
    // TODO fix
    //final JSFunction function = findFunction(second);
    //final JSNamespace ns = visitor.findNsForExpr((JSExpression)second);
    //if (function != null && ns != null) {
    //  visitor.visitWithNamespace(ns, function, false);
    //}
  }

  private static String calculateRestrictions(PsiElement element) {
    final Ref<String> restrict = Ref.create(DEFAULT_RESTRICTIONS);
    final Ref<String> scope = Ref.create("");
    final JSFunction function = findFunction(element);
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

  private static JSFunction findFunction(PsiElement element) {
    JSFunction function = PsiTreeUtil.getNextSiblingOfType(element, JSFunction.class);
    if (function == null) {
      final JSExpression expression = PsiTreeUtil.getNextSiblingOfType(element, JSExpression.class);
      function = findDeclaredFunction(expression);
    }
    if (function == null) {
      JSArrayLiteralExpression array = PsiTreeUtil.getNextSiblingOfType(element, JSArrayLiteralExpression.class);
      function = PsiTreeUtil.findChildOfType(array, JSFunction.class);
      if (function == null) {
        final JSExpression candidate = array != null ?PsiTreeUtil.getPrevSiblingOfType(array.getLastChild(), JSExpression.class) : null;
        function = findDeclaredFunction(candidate);
      }
    }
    return function;
  }

  private static JSFunction findDeclaredFunction(JSExpression expression) {
    // TODO local resolve
    return null;
    //final JSElement candidate = visitor.getOperandFromVarContext(expression);
    //return candidate instanceof JSFunction ? (JSFunction)candidate : null;
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

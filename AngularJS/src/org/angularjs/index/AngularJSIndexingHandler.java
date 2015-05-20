package org.angularjs.index;

import com.intellij.lang.javascript.JSDocTokenTypes;
import com.intellij.lang.javascript.documentation.JSDocumentationUtils;
import com.intellij.lang.javascript.index.FrameworkIndexingHandler;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment;
import com.intellij.lang.javascript.psi.jsdoc.JSDocTag;
import com.intellij.lang.javascript.psi.jsdoc.JSDocTagValue;
import com.intellij.lang.javascript.psi.literal.JSLiteralImplicitElementProvider;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.lang.javascript.psi.types.JSNamedType;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.BidirectionalMap;
import gnu.trove.THashSet;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.lang.psi.AngularJSAsExpression;
import org.angularjs.lang.psi.AngularJSFilterExpression;
import org.angularjs.lang.psi.AngularJSRepeatExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSIndexingHandler extends FrameworkIndexingHandler {
  private static final Map<String, StubIndexKey<String, JSImplicitElementProvider>> INDEXERS =
    new HashMap<String, StubIndexKey<String, JSImplicitElementProvider>>();
  private static final Map<String, Function<String, String>> NAME_CONVERTERS = new HashMap<String, Function<String, String>>();
  private static final Map<String, Function<PsiElement, String>> DATA_CALCULATORS = new HashMap<String, Function<PsiElement, String>>();

  public static final Set<String> INTERESTING_METHODS = new HashSet<String>();
  public static final Set<String> INJECTABLE_METHODS = new HashSet<String>();
  public static final String CONTROLLER = "controller";
  public static final String DIRECTIVE = "directive";
  public static final String MODULE = "module";
  public static final String FILTER = "filter";
  private static final String START_SYMBOL = "startSymbol";
  private static final String END_SYMBOL = "endSymbol";
  public static final String DEFAULT_RESTRICTIONS = "D";

  private static final String[] ALL_INTERESTING_METHODS;
  private static final BidirectionalMap<String, StubIndexKey<String, JSImplicitElementProvider>> INDEXES;

  static {
    Collections.addAll(INTERESTING_METHODS, "service", "factory", "value", "constant", "provider");

    INJECTABLE_METHODS.addAll(INTERESTING_METHODS);
    Collections.addAll(INJECTABLE_METHODS, CONTROLLER, DIRECTIVE, MODULE);

    INDEXERS.put(DIRECTIVE, AngularDirectivesIndex.KEY);
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

    INDEXERS.put(CONTROLLER, AngularControllerIndex.KEY);
    INDEXERS.put(MODULE, AngularModuleIndex.KEY);
    INDEXERS.put(FILTER, AngularFilterIndex.KEY);

    final THashSet<String> allInterestingMethods = new THashSet<String>(INTERESTING_METHODS);
    allInterestingMethods.addAll(INJECTABLE_METHODS);
    allInterestingMethods.addAll(INDEXERS.keySet());
    allInterestingMethods.add(START_SYMBOL);
    allInterestingMethods.add(END_SYMBOL);
    ALL_INTERESTING_METHODS = ArrayUtil.toStringArray(allInterestingMethods);

    INDEXES = new BidirectionalMap<String, StubIndexKey<String, JSImplicitElementProvider>>();
    INDEXES.put("aci", AngularControllerIndex.KEY);
    INDEXES.put("addi", AngularDirectivesDocIndex.KEY);
    INDEXES.put("adi", AngularDirectivesIndex.KEY);
    INDEXES.put("afi", AngularFilterIndex.KEY);
    INDEXES.put("aidi", AngularInjectionDelimiterIndex.KEY);
    INDEXES.put("ami", AngularModuleIndex.KEY);
    INDEXES.put("asi", AngularSymbolIndex.KEY);

    for (String key : INDEXES.keySet()) {
      JSImplicitElement.UserStringsRegistry.registerUserString(key);
    }
  }

  private static final String RESTRICT = "@restrict";
  private static final String ELEMENT = "@element";
  private static final String PARAM = "@param";
  private static final Pattern RESTRICT_PATTERN = Pattern.compile(RESTRICT + "\\s*(.*)");
  private static final Pattern ELEMENT_PATTERN = Pattern.compile(ELEMENT + "\\s*(.*)");

  public static boolean isInjectable(PsiElement context) {
    final JSCallExpression call = PsiTreeUtil.getParentOfType(context, JSCallExpression.class, false, JSBlockStatement.class);
    if (call != null) {
      final JSExpression methodExpression = call.getMethodExpression();
      JSReferenceExpression callee = ObjectUtils.tryCast(methodExpression, JSReferenceExpression.class);
      JSExpression qualifier = callee != null ? callee.getQualifier() : null;
      return qualifier != null && INJECTABLE_METHODS.contains(callee.getReferenceName());
    }
    return false;
  }


  @NotNull
  @Override
  public String[] interestedMethodNames() {
    return ALL_INTERESTING_METHODS;
  }

  @Override
  public JSLiteralImplicitElementProvider createLiteralImplicitElementProvider(@NotNull final String command) {
    return new JSLiteralImplicitElementProvider() {
      @Override
      public void fillIndexingData(@NotNull JSLiteralExpression argument,
                                   @NotNull JSCallExpression callExpression,
                                   @NotNull JSElementIndexingData outIndexingData) {
        JSExpression[] arguments = callExpression.getArguments();
        if (arguments.length == 0 || arguments[0] != argument) return;
        final JSExpression methodExpression = callExpression.getMethodExpression();
        if (!(methodExpression instanceof JSReferenceExpression)) return;
        JSExpression qualifier = ((JSReferenceExpression)methodExpression).getQualifier();
        if (qualifier == null) return;

        final StubIndexKey<String, JSImplicitElementProvider> index = INDEXERS.get(command);
        if (index != null) {
          if (argument.isQuotedLiteral()) {
            final Function<PsiElement, String> calculator = DATA_CALCULATORS.get(command);
            final String data = calculator != null ? calculator.fun(argument) : null;
            final String argumentText = StringUtil.unquoteString(argument.getText());
            addImplicitElements(argument, command, index, argumentText, data, outIndexingData);
          }
        } else if (INJECTABLE_METHODS.contains(command)) { // INTERESTING_METHODS are contained in INJECTABLE_METHODS
          if (argument.isQuotedLiteral()) {
            generateNamespace(argument, command, outIndexingData);
          }
        }

        if (START_SYMBOL.equals(command) || END_SYMBOL.equals(command)) {
          while (qualifier != null) {
            if (qualifier instanceof JSReferenceExpression) {
              if ("$interpolateProvider".equals(((JSReferenceExpression)qualifier).getReferenceName())) {
                if (argument.isQuotedLiteral()) {
                  String interpolation = StringUtil.unquoteString(argument.getText());
                  // '//' interpolations are usually dragged from examples folder and not supposed to be used by real users
                  if ("//".equals(interpolation)) return;
                  addImplicitElements(argument, null, AngularInjectionDelimiterIndex.KEY, command, interpolation, outIndexingData);
                }
              }
              qualifier = ((JSReferenceExpression)qualifier).getQualifier();
            }
            else {
              qualifier = qualifier instanceof JSCallExpression ? ((JSCallExpression)qualifier).getMethodExpression() : null;
            }
          }
        }
      }
    };
  }

  @Nullable
  @Override
  public JSElementIndexingData processAnyProperty(@NotNull JSProperty property, @Nullable JSElementIndexingData outData) {
    final PsiElement parent = property.getParent();
    if (!(parent instanceof JSObjectLiteralExpression)) return outData;
    final PsiElement grandParent = parent.getParent();
    if (!(grandParent instanceof JSArgumentList)) return outData;
    final PsiElement callExpression = grandParent.getParent();
    if (!(callExpression instanceof JSCallExpression)) return outData;
    final JSExpression methodExpression = ((JSCallExpression)callExpression).getMethodExpression();
    if (!(methodExpression instanceof JSReferenceExpression) || ((JSReferenceExpression)methodExpression).getQualifier() == null) {
      return outData;
    }
    final String command = ((JSReferenceExpression)methodExpression).getReferenceName();
    final StubIndexKey<String, JSImplicitElementProvider> index =
      INDEXERS.get(command);
    if (index == null) return outData;
    if (((JSCallExpression)callExpression).getArguments()[0] != parent) return outData;
    final String name = property.getName();
    if (name == null) return outData;

    if (outData == null) outData = new JSElementIndexingDataImpl();
    addImplicitElements(property, command, index, name, null, outData);
    return outData;
  }

  @Override
  public boolean indexImplicitElement(@NotNull JSImplicitElement element, @Nullable IndexSink sink) {
    final String userID = element.getUserString();
    final StubIndexKey<String, JSImplicitElementProvider> index = userID != null ? INDEXES.get(userID) : null;
    if (index != null) {
      if (sink != null) {
        sink.occurrence(index, element.getName());
        if (index != AngularSymbolIndex.KEY) {
          sink.occurrence(AngularSymbolIndex.KEY, element.getName());
        }
      }
    }
    return false;
  }

  @Override
  public JSElementIndexingData processJSDocComment(@NotNull final JSDocComment comment, @Nullable JSElementIndexingData outData) {
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

        if (ngdocValue.contains(DIRECTIVE)) {
          final String restrictions = calculateRestrictions(commentLines);
          if (outData == null) outData = new JSElementIndexingDataImpl();
          addImplicitElements(comment, DIRECTIVE, AngularDirectivesDocIndex.KEY, name, restrictions, outData);
        }
        else if (ngdocValue.contains(FILTER)) {
          if (outData == null) outData = new JSElementIndexingDataImpl();
          addImplicitElements(comment, FILTER, AngularFilterIndex.KEY, name, null, outData);
        }
      }
    }
    return outData;
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

  public static boolean isAngularRestrictions(@Nullable String restrictions) {
    return restrictions == null || StringUtil.countChars(restrictions, ';') >= 3;
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


  private static void generateNamespace(@NotNull JSLiteralExpression argument,
                                        @NotNull String calledMethodName,
                                        @NotNull JSElementIndexingData outData) {
    final String namespace = StringUtil.unquoteString(argument.getText());
    JSQualifiedNameImpl qName = JSQualifiedNameImpl.fromQualifiedName(namespace);
    JSImplicitElementImpl.Builder elementBuilder =
      new JSImplicitElementImpl.Builder(qName, argument)
        .setType(JSImplicitElement.Type.Class).setUserString("asi");
    final JSImplicitElementImpl implicitElement = elementBuilder.toImplicitElement();
    outData.addImplicitElement(implicitElement);
    // TODO fix
    //final JSFunction function = findFunction(argument);
    //final JSNamespace ns = visitor.findNsForExpr((JSExpression)argument);
    //if (function != null && ns != null) {
    //  visitor.visitWithNamespace(ns, function, false);
    //}
  }

  private static void addImplicitElements(@NotNull final JSImplicitElementProvider elementProvider,
                                          @Nullable final String command,
                                          @NotNull final StubIndexKey<String, JSImplicitElementProvider> index,
                                          @NotNull String defaultName,
                                          @Nullable final String value,
                                          @NotNull final JSElementIndexingData outData) {
    final Function<String, String> converter = command != null ? NAME_CONVERTERS.get(command) : null;
    final String name = converter != null ? converter.fun(defaultName) : defaultName;

    JSQualifiedNameImpl qName = JSQualifiedNameImpl.fromQualifiedName(name);
    JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(qName, elementProvider)
      .setType(elementProvider instanceof JSDocComment ? JSImplicitElement.Type.Tag : JSImplicitElement.Type.Class)
      .setTypeString(value);
    List<String> keys = INDEXES.getKeysByValue(index);
    assert keys != null && keys.size() == 1;
    elementBuilder.setUserString(keys.get(0));
    final JSImplicitElementImpl implicitElement = elementBuilder.toImplicitElement();
    outData.addImplicitElement(implicitElement);
    if (!StringUtil.equals(defaultName, name)) {
      elementBuilder = new JSImplicitElementImpl.Builder(defaultName, elementProvider)
        .setType(elementProvider instanceof JSDocComment ? JSImplicitElement.Type.Tag : JSImplicitElement.Type.Class)
        .setTypeString(value);
      keys = INDEXES.getKeysByValue(AngularSymbolIndex.KEY);
      assert keys != null && keys.size() == 1;
      elementBuilder.setUserString(keys.get(0));
      final JSImplicitElementImpl implicitElement2 = elementBuilder.toImplicitElement();
      outData.addImplicitElement(implicitElement2);
    }
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
    final Ref<JSFunction> result = Ref.create();
    if (expression instanceof JSReferenceExpression) {
      final String name = ((JSReferenceExpression)expression).getReferenceName();
      expression.getContainingFile().accept(new JSRecursiveWalkingElementVisitor() {
        @Override
        public void visitJSFunctionExpression(JSFunctionExpression node) {
          checkFunction(node);
          super.visitJSFunctionExpression(node);
        }

        public void checkFunction(JSFunction node) {
          if (StringUtil.equals(name, node.getName())) {
            result.set(node);
            stopWalking();
          }
        }

        @Override
        public void visitJSFunctionDeclaration(JSFunction node) {
          checkFunction(node);
          super.visitJSFunctionDeclaration(node);
        }
      });
    }
    return result.get();
  }

  @Override
  public String resolveContextFromProperty(JSObjectLiteralExpression objectLiteralExpression) {
    if (!(objectLiteralExpression.getParent() instanceof JSReturnStatement)) return null;

    final JSFunction function = PsiTreeUtil.getParentOfType(objectLiteralExpression, JSFunction.class);
    final JSCallExpression call = PsiTreeUtil.getParentOfType(function, JSCallExpression.class);
    if (call != null) {
      final JSExpression methodExpression = call.getMethodExpression();
      if (!(methodExpression instanceof JSReferenceExpression)) return null;
      JSReferenceExpression callee = (JSReferenceExpression)methodExpression;
      JSExpression qualifier = callee.getQualifier();

      if (qualifier == null) return null;

      final String command = callee.getReferencedName();

      if (INJECTABLE_METHODS.contains(command)) {
        JSExpression[] arguments = call.getArguments();
        if (arguments.length > 0) {
          JSExpression argument = arguments[0];
          if (argument instanceof JSLiteralExpression && ((JSLiteralExpression)argument).isQuotedLiteral()) {
            return StringUtil.unquoteString(argument.getText());
          }
        }
      }
    }
    return null;
  }

  @Override
  public boolean addTypeFromResolveResult(JSTypeEvaluator evaluator,
                                          JSReferenceExpression expression,
                                          PsiElement parent,
                                          PsiElement resolveResult,
                                          boolean hasSomeType) {
    if (!AngularIndexUtil.hasAngularJS(expression.getProject())) return false;

    if (resolveResult instanceof JSDefinitionExpression) {
      final PsiElement resolveParent = resolveResult.getParent();
      if (resolveParent instanceof AngularJSAsExpression) {
        final String name = resolveParent.getFirstChild().getText();
        final JSTypeSource source = JSTypeSourceFactory.createTypeSource(resolveResult);
        final JSType type = JSNamedType.createType(name, source, JSContext.INSTANCE);
        evaluator.addType(type, resolveResult);
        return true;
      }
      else if (resolveParent instanceof AngularJSRepeatExpression) {
        if (calculateRepeatParameterType(evaluator, (AngularJSRepeatExpression)resolveParent)) {
          return true;
        }
      }
    }
    if (resolveResult instanceof JSParameter && isInjectable(resolveResult)) {
      final String name = ((JSParameter)resolveResult).getName();
      final JSTypeSource source = JSTypeSourceFactory.createTypeSource(resolveResult);
      final JSType type = JSNamedType.createType(name, source, JSContext.UNKNOWN);
      evaluator.addType(type, resolveResult);
    }
    return false;
  }

  @Override
  public int getVersion() {
    return AngularIndexUtil.BASE_VERSION;
  }

  private static boolean calculateRepeatParameterType(JSTypeEvaluator evaluator, AngularJSRepeatExpression resolveParent) {
    final PsiElement last = findReferenceExpression(resolveParent);
    JSExpression arrayExpression = null;
    if (last instanceof JSReferenceExpression) {
      PsiElement resolve = ((JSReferenceExpression)last).resolve();
      if (resolve != null) {
        resolve = JSPsiImplUtils.getAssignedExpression(resolve);
        if (resolve != null) {
          arrayExpression = (JSExpression)resolve;
        }
      }
    }
    else if (last instanceof JSExpression) {
      arrayExpression = (JSExpression)last;
    }
    if (last != null && arrayExpression != null) {
      return evaluator.evalComponentTypeFromArrayExpression(resolveParent, arrayExpression) != null;
    }
    return false;
  }

  private static PsiElement findReferenceExpression(AngularJSRepeatExpression parent) {
    JSExpression collection = parent.getCollection();
    while (collection instanceof JSBinaryExpression &&
           ((JSBinaryExpression)collection).getROperand() instanceof AngularJSFilterExpression) {
      collection = ((JSBinaryExpression)collection).getLOperand();
    }
    return collection;
  }
}

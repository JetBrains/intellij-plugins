// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.index;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSDocTokenTypes;
import com.intellij.lang.javascript.documentation.JSDocumentationUtils;
import com.intellij.lang.javascript.index.FrameworkIndexingHandler;
import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.library.JSLibraryUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSCallExpressionImpl;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment;
import com.intellij.lang.javascript.psi.jsdoc.JSDocTag;
import com.intellij.lang.javascript.psi.jsdoc.JSDocTagValue;
import com.intellij.lang.javascript.psi.literal.JSLiteralImplicitElementProvider;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElementStructure;
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.javascript.psi.types.*;
import com.intellij.lang.javascript.psi.util.JSTreeUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.*;
import com.intellij.util.containers.BidirectionalMap;
import org.angular2.index.Angular2IndexingHandler;
import org.angularjs.codeInsight.AngularJSReferenceExpressionResolver;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.codeInsight.router.AngularJSUiRouterConstants;
import org.angularjs.lang.AngularJSLanguage;
import org.angularjs.lang.psi.AngularJSFilterExpression;
import org.angularjs.lang.psi.AngularJSRepeatExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

import static org.angularjs.index.AngularJSDirectivesSupport.getDirectiveIndexKeys;

/**
 * @author Dennis.Ushakov
 */
public final class AngularJSIndexingHandler extends FrameworkIndexingHandler {
  private static final Map<String, StubIndexKey<String, JSImplicitElementProvider>> INDEXERS =
    new HashMap<>();
  private static final Map<String, Function<String, String>> NAME_CONVERTERS = new HashMap<>();
  private static final Map<String, Function<PsiElement, String>> DATA_CALCULATORS = new HashMap<>();
  private final Map<String, PairProcessor<JSProperty, JSElementIndexingData>> CUSTOM_PROPERTY_PROCESSORS = new HashMap<>();
  private final Map<String, PairProcessor<JSProperty, JSElementIndexingData>> CUSTOM_INDIRECT_PROPERTY_PROCESSORS = new HashMap<>();
  private static final Map<String, Function<String, List<String>>> POLY_NAME_CONVERTERS = new HashMap<>();
  private static final Map<String, Processor<JSArgumentList>> ARGUMENT_LIST_CHECKERS = new HashMap<>();

  public static final Set<String> INTERESTING_METHODS = new HashSet<>();
  public static final Set<String> INJECTABLE_METHODS = new HashSet<>();
  public static final String CONTROLLER = "controller";
  public static final String DIRECTIVE = "directive";
  public static final String COMPONENT = "component";
  public static final String BINDINGS = "bindings";
  public static final String SCOPE = "scope";
  public static final String BIND_TO_CONTROLLER = "bindToController";
  public static final String TEMPLATE_URL = "templateUrl";
  public static final String TEMPLATE = "template";
  public static final String CONTROLLER_AS = "controllerAs";
  public static final String MODULE = "module";
  public static final String FILTER = "filter";
  public static final String STATE = "state";
  private static final String START_SYMBOL = "startSymbol";
  private static final String END_SYMBOL = "endSymbol";
  public static final String DEFAULT_RESTRICTIONS = "D";
  public static final String WHEN = "when";
  private static final String RESTRICT_PROP = "restrict";

  private static final String[] ALL_INTERESTING_METHODS;
  private static final BidirectionalMap<String, StubIndexKey<String, JSImplicitElementProvider>> INDEXES;
  public static final String AS_CONNECTOR_WITH_SPACES = " as ";

  public static final String ANGULAR_DIRECTIVES_DOC_INDEX_USER_STRING = "addi";
  public static final String ANGULAR_DIRECTIVES_INDEX_USER_STRING = "adi";
  public static final String ANGULAR_FILTER_INDEX_USER_STRING = "afi";

  public static final String ANGULAR_SYMBOL_INDEX_USER_STRING = "asi";

  public static final String ANGULAR_MODULE_INDEX_USER_STRING = "ami";

  static {
    Collections.addAll(INTERESTING_METHODS, "service", "factory", "value", "constant", "provider");

    INJECTABLE_METHODS.addAll(INTERESTING_METHODS);
    Collections.addAll(INJECTABLE_METHODS, CONTROLLER, DIRECTIVE, COMPONENT, MODULE, "config", "run");

    INDEXERS.put(DIRECTIVE, AngularDirectivesIndex.KEY);
    NAME_CONVERTERS.put(DIRECTIVE, DirectiveUtil::getAttributeName);
    DATA_CALCULATORS.put(DIRECTIVE, element -> calculateRestrictions(element, DEFAULT_RESTRICTIONS));

    INDEXERS.put(COMPONENT, AngularDirectivesIndex.KEY);
    DATA_CALCULATORS.put(COMPONENT, element -> calculateRestrictions(element, "E"));

    INDEXERS.put(CONTROLLER, AngularControllerIndex.KEY);
    INDEXERS.put(MODULE, AngularModuleIndex.KEY);
    INDEXERS.put(FILTER, AngularFilterIndex.KEY);
    INDEXERS.put(STATE, AngularUiRouterStatesIndex.KEY);

    final Set<String> allInterestingMethods = new HashSet<>(INTERESTING_METHODS);
    allInterestingMethods.addAll(INJECTABLE_METHODS);
    allInterestingMethods.addAll(INDEXERS.keySet());
    allInterestingMethods.add(START_SYMBOL);
    allInterestingMethods.add(END_SYMBOL);
    ALL_INTERESTING_METHODS = ArrayUtilRt.toStringArray(allInterestingMethods);

    INDEXES = new BidirectionalMap<>();
    INDEXES.put("aci", AngularControllerIndex.KEY);
    INDEXES.put(ANGULAR_DIRECTIVES_DOC_INDEX_USER_STRING, AngularDirectivesDocIndex.KEY);
    INDEXES.put(ANGULAR_DIRECTIVES_INDEX_USER_STRING, AngularDirectivesIndex.KEY);
    INDEXES.put(ANGULAR_FILTER_INDEX_USER_STRING, AngularFilterIndex.KEY);
    INDEXES.put("aidi", AngularInjectionDelimiterIndex.KEY);
    INDEXES.put(ANGULAR_MODULE_INDEX_USER_STRING, AngularModuleIndex.KEY);
    INDEXES.put(ANGULAR_SYMBOL_INDEX_USER_STRING, AngularSymbolIndex.KEY);
    INDEXES.put("arsi", AngularUiRouterStatesIndex.KEY);
    INDEXES.put("arsgi", AngularUiRouterGenericStatesIndex.KEY);
    INDEXES.put("agmi", AngularGenericModulesIndex.KEY);
    INDEXES.put("ajtui", AngularTemplateUrlIndex.KEY);

    NAME_CONVERTERS.put(BINDINGS, NAME_CONVERTERS.get(DIRECTIVE));
    NAME_CONVERTERS.put(SCOPE, NAME_CONVERTERS.get(DIRECTIVE));
    NAME_CONVERTERS.put(BIND_TO_CONTROLLER, NAME_CONVERTERS.get(DIRECTIVE));

    // example of nested states https://scotch.io/tutorials/angular-routing-using-ui-router
    POLY_NAME_CONVERTERS.put(STATE, (NotNullFunction<String, List<String>>)dom -> {
      final String[] parts = dom.split("\\.");
      final List<String> result = new ArrayList<>();
      result.add(dom);
      String tail = "";
      for (int i = parts.length - 1; i > 0; i--) {
        final String part = "." + parts[i] + tail;
        result.add(part);
        tail = part;
      }
      return result;
    });
    // do NOT split module names by dot
    POLY_NAME_CONVERTERS.put(MODULE, Collections::singletonList);
    ARGUMENT_LIST_CHECKERS.put(MODULE, list -> list.getArguments().length > 1);
  }

  static final String RESTRICT = "@restrict";
  static final String ELEMENT = "@element";
  private static final String PARAM = "@param";

  public AngularJSIndexingHandler() {
    CUSTOM_PROPERTY_PROCESSORS.put(COMPONENT, (property, data) -> processScopedProperty(property, data, BINDINGS, true));
    CUSTOM_INDIRECT_PROPERTY_PROCESSORS.put(DIRECTIVE, (property, data) -> {
      boolean result = processScopedProperty(property, data, SCOPE, false);
      return processScopedProperty(property, data, BIND_TO_CONTROLLER, false) || result;
    });

    final PairProcessor<JSProperty, JSElementIndexingData> processor = createRouterParametersProcessor();
    CUSTOM_PROPERTY_PROCESSORS.put(WHEN, processor);
    CUSTOM_PROPERTY_PROCESSORS.put("otherwise", processor);
    CUSTOM_PROPERTY_PROCESSORS.put("state", processor);
  }

  public static boolean isInjectable(PsiElement context) {
    final JSCallExpression call = PsiTreeUtil.getContextOfType(context, JSCallExpression.class, false, JSBlockStatement.class);
    if (call != null) {
      // TODO stub for method/function references
      final JSExpression methodExpression = AstLoadingFilter.forceAllowTreeLoading(
        call.getContainingFile(), call::getMethodExpression);
      JSReferenceExpression callee = ObjectUtils.tryCast(methodExpression, JSReferenceExpression.class);
      JSExpression qualifier = callee != null ? callee.getQualifier() : null;
      return qualifier != null && INJECTABLE_METHODS.contains(callee.getReferenceName());
    }
    return false;
  }

  @Override
  public String @NotNull [] inheritanceMethodNames() {
    return ALL_INTERESTING_METHODS;
  }

  @Override
  public String @NotNull [] implicitProviderMethodNames() {
    return ALL_INTERESTING_METHODS;
  }

  @Override
  public JSLiteralImplicitElementProvider createLiteralImplicitElementProvider(final @NotNull String command) {
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
            final Processor<JSArgumentList> argumentListProcessor = ARGUMENT_LIST_CHECKERS.get(command);
            if (argumentListProcessor != null && !argumentListProcessor.process(callExpression.getArgumentList())) return;
            final Function<PsiElement, String> calculator = DATA_CALCULATORS.get(command);
            final String data = calculator != null ? calculator.fun(argument) : null;
            final String argumentText = unquote(argument);
            if (data != null && data.startsWith("D;")) {
              addImplicitElements(argument, command, index, argumentText, "A" + data.substring(1), outIndexingData);
            }
            addImplicitElements(argument, command, index, argumentText, data, outIndexingData);
          }
        }
        else if (INJECTABLE_METHODS.contains(command)) { // INTERESTING_METHODS are contained in INJECTABLE_METHODS
          if (argument.isQuotedLiteral()) {
            generateNamespace(argument, outIndexingData);
          }
        }

        if (START_SYMBOL.equals(command) || END_SYMBOL.equals(command)) {
          while (qualifier != null) {
            if (qualifier instanceof JSReferenceExpression) {
              if ("$interpolateProvider".equals(((JSReferenceExpression)qualifier).getReferenceName())) {
                if (argument.isQuotedLiteral()) {
                  String interpolation = unquote(argument);
                  // '//' interpolations are usually dragged from examples folder and not supposed to be used by real users
                  if ("//".equals(interpolation)) return;
                  FileViewProvider provider = qualifier.getContainingFile().getOriginalFile().getViewProvider();
                  VirtualFile virtualFile = provider.getVirtualFile();
                  virtualFile = virtualFile instanceof LightVirtualFile ? ((LightVirtualFile)virtualFile).getOriginalFile() : virtualFile;

                  if (virtualFile != null && JSLibraryUtil.isProbableLibraryFile(virtualFile)) return;
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

  @Override
  public void processCallExpression(JSCallExpression callExpression, @NotNull JSElementIndexingData outData) {
    final JSReferenceExpression reference = ObjectUtils.tryCast(callExpression.getMethodExpression(), JSReferenceExpression.class);
    if (reference == null) return;
    if (JSSymbolUtil.isAccurateReferenceExpressionName(reference, "$stateProvider", STATE)) {
      final JSExpression[] arguments = callExpression.getArguments();
      if (arguments.length == 1 && arguments[0] instanceof JSReferenceExpression) {
        addImplicitElements(callExpression, null, AngularUiRouterGenericStatesIndex.KEY, STATE, null, outData);
      }
    }
    else if (JSSymbolUtil.isAccurateReferenceExpressionName(reference, "angular", MODULE)) {
      final JSExpression[] arguments = callExpression.getArguments();
      if (arguments.length > 1 && arguments[0] instanceof JSReferenceExpression) {
        addImplicitElements(callExpression, null, AngularGenericModulesIndex.KEY, MODULE, null, outData);
      }
    }
  }

  @Override
  public boolean shouldCreateStubForCallExpression(@NotNull ASTNode node) {
    final ASTNode methodExpression = JSCallExpressionImpl.getMethodExpression(node);
    if (methodExpression == null) return false;

    final ASTNode referencedNameElement = methodExpression.getLastChildNode();
    if (referencedNameElement == null) return false;
    final ASTNode qualifierElement = JSReferenceExpressionImpl.getQualifierNode(methodExpression);
    if (qualifierElement == null) return false;
    String referencedName = referencedNameElement.getText();
    String qualifier = qualifierElement.getText();
    return STATE.equals(referencedName) && "$stateProvider".equalsIgnoreCase(qualifier)
           || MODULE.equals(referencedName) && "angular".equalsIgnoreCase(qualifier)
           || INJECTABLE_METHODS.contains(referencedName);
  }

  @Override
  public @Nullable JSElementIndexingData processAnyProperty(@NotNull JSProperty property, @Nullable JSElementIndexingData outData) {
    final String name = property.getName();
    if (name == null) return outData;
    JSElementIndexingData localOutData;
    if (TEMPLATE_URL.equals(name) && processTemplateUrlProperty(
      property, localOutData = (outData == null ? new JSElementIndexingDataImpl() : outData))) {
      return localOutData;
    }
    if (TEMPLATE.equals(name) && processTemplateProperty(
      property, localOutData = (outData == null ? new JSElementIndexingDataImpl() : outData))) {
      return localOutData;
    }
    @Nullable WrappingCall wrappingCall = findWrappingCall(property);
    if (wrappingCall == null) {
      return outData;
    }
    final JSCallExpression callExpression = wrappingCall.call();
    boolean immediate = wrappingCall.immediate();

    final JSExpression methodExpression = callExpression.getMethodExpression();
    if (!(methodExpression instanceof JSReferenceExpression) || ((JSReferenceExpression)methodExpression).getQualifier() == null) {
      return outData;
    }
    final String command = ((JSReferenceExpression)methodExpression).getReferenceName();
    final PairProcessor<JSProperty, JSElementIndexingData> customProcessor =
      immediate ? CUSTOM_PROPERTY_PROCESSORS.get(command) : CUSTOM_INDIRECT_PROPERTY_PROCESSORS.get(command);
    if (customProcessor != null && customProcessor.process(property,
                                                           (localOutData =
                                                              (outData == null ? new JSElementIndexingDataImpl() : outData)))) {
      return localOutData;
    }
    // for 'standard' properties, keep indexing only for properties - immediate children of function calls parameters
    if (wrappingCall.level() > 1 || !immediate) return outData;

    final PsiElement parent = property.getParent();
    final StubIndexKey<String, JSImplicitElementProvider> index = INDEXERS.get(command);
    if (index == null) return outData;
    if (callExpression.getArguments()[0] != parent) return outData;

    if (outData == null) outData = new JSElementIndexingDataImpl();
    addImplicitElements(property, command, index, name, null, outData);
    return outData;
  }

  private record WrappingCall(@NotNull JSCallExpression call, int level, boolean immediate) {}

  private static @Nullable WrappingCall findWrappingCall(@NotNull JSProperty property) {
    PsiElement current = property.getParent();
    int level = 0;
    boolean immediate = true;
    while (current instanceof JSElement) {
      if (current instanceof JSProperty) {
        current = current.getParent();
        continue;
      }
      else if (current instanceof JSObjectLiteralExpression) {
        ++level;
        current = current.getParent();
        continue;
      }
      else if (current instanceof JSReturnStatement) {
        immediate = false;
        JSFunction function = PsiTreeUtil.getParentOfType(current, JSFunction.class);
        if (function instanceof JSFunctionExpression) {
          current = function.getParent();
        }
        else {
          return null;
        }
      }
      if (current instanceof JSArrayLiteralExpression) {
        current = current.getParent();
      }
      if (current instanceof JSArgumentList && current.getParent() instanceof JSCallExpression call) {
        return new WrappingCall(call, level, immediate);
      }
      return null;
    }
    return null;
  }

  @Override
  public boolean indexImplicitElement(@NotNull JSImplicitElementStructure element, @Nullable IndexSink sink) {
    final String userID = element.getUserString();
    final StubIndexKey<String, JSImplicitElementProvider> index = userID != null ? INDEXES.get(userID) : null;
    if (index != null && sink != null) {
      if (index == AngularDirectivesIndex.KEY
          || index == AngularDirectivesDocIndex.KEY) {
        for (String indexKey: getDirectiveIndexKeys(element)) {
          sink.occurrence(index, indexKey);
        }
      }  else {
        sink.occurrence(index, element.getName());
      }
      if (index != AngularSymbolIndex.KEY) {
        sink.occurrence(AngularSymbolIndex.KEY, element.getName());
      }
    }
    return false;
  }

  @Override
  public JSElementIndexingData processJSDocComment(final @NotNull JSDocComment comment, @Nullable JSElementIndexingData outData) {
    JSDocTag ngdocTag = null;
    JSDocTag nameTag = null;
    for (JSDocTag tag : comment.getTags()) {
      if ("ngdoc".equals(tag.getName())) {
        ngdocTag = tag;
      }
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

        final boolean directive = ngdocValue.contains(DIRECTIVE);
        final boolean component = ngdocValue.contains(COMPONENT);
        if (directive || component) {
          final List<Pair<String, String>> restrictions =
            calculateRestrictions(comment, commentLines, name, directive ? DEFAULT_RESTRICTIONS : "E");
          if (outData == null) outData = new JSElementIndexingDataImpl();
          for (Pair<String, String> p : restrictions) {
            addImplicitElements(comment, directive ? DIRECTIVE : COMPONENT, AngularDirectivesDocIndex.KEY, p.first, p.second, outData);
          }
        }
        else if (ngdocValue.contains(FILTER)) {
          if (outData == null) outData = new JSElementIndexingDataImpl();
          addImplicitElements(comment, FILTER, AngularFilterIndex.KEY, name, null, outData);
        }
      }
    }
    return outData;
  }

  private static List<Pair<String, String>> calculateRestrictions(@NotNull PsiElement context, 
                                                                  final String[] commentLines,
                                                                  String directiveName,
                                                                  String defaultRestrictions) {
    String restrict = defaultRestrictions;
    String tag = "";
    List<Pair<String, String>> result = new SmartList<>();
    for (String line : commentLines) {
      restrict = getParamValue(restrict, line, RESTRICT);
      tag = getParamValue(tag, line, ELEMENT);
      final int start = line.indexOf(PARAM);
      if (start >= 0) {
        final JSDocumentationUtils.DocTag docTag = JSDocumentationUtils.getDocTag(context, line.substring(start));
        if (docTag != null && docTag.matchName != null) {
          for (String paramName : StringUtil.split(docTag.matchName, "|")) {
            if (restrict.equals(DEFAULT_RESTRICTIONS) || restrict.contains("A")) {
              result.add(Pair.pair(paramName, "A;" + tag + (directiveName.equals(paramName) ? "" : "=" + directiveName) + ";"
                                              + (docTag.matchValue != null ? docTag.matchValue : "") + ";"));
            }
            if (restrict.contains("E")) {
              result.add(Pair.pair(paramName, "A;" + directiveName + ";"
                                              + (docTag.matchValue != null ? docTag.matchValue : "") + ";"));
            }
          }
        }
      }
    }
    if (restrict.contains("E")) {
      result.add(Pair.pair(directiveName, "E;" + tag + ";;"));
    }
    else if (result.isEmpty()) {
      if (restrict.equals(DEFAULT_RESTRICTIONS)) {
        result.add(Pair.pair(directiveName, "A;" + tag + ";;"));
      }
      result.add(Pair.pair(directiveName, restrict + ";" + tag + ";;"));
    }
    else if (restrict.equals(DEFAULT_RESTRICTIONS)) {
      result.add(Pair.pair(directiveName, "D;" + tag + ";;"));
    }
    return result;
  }

  public static boolean isAngularRestrictions(@Nullable String restrictions) {
    return restrictions == null || StringUtil.countChars(restrictions, ';') >= 3;
  }

  static String getParamValue(String previousValue, String line, final String docTag) {
    final int indexOfTag = line.indexOf(docTag);
    if (indexOfTag >= 0) {
      final int commentAtEndIndex = line.indexOf("//", indexOfTag);
      String newValue = line.substring(indexOfTag + docTag.length(), commentAtEndIndex > 0 ? commentAtEndIndex : line.length());
      newValue = newValue.trim();
      if (!StringUtil.isEmpty(newValue)) return newValue;
    }
    return previousValue;
  }


  private void generateNamespace(@NotNull JSLiteralExpression argument, @NotNull JSElementIndexingData outData) {
    final String namespace = unquote(argument);
    if (namespace == null) return;
    JSQualifiedNameImpl qName = JSQualifiedNameImpl.fromQualifiedName(namespace);
    JSImplicitElementImpl.Builder elementBuilder =
      new JSImplicitElementImpl.Builder(qName, argument)
        .setType(JSImplicitElement.Type.Class).setUserString(this, ANGULAR_SYMBOL_INDEX_USER_STRING);
    final JSImplicitElementImpl implicitElement = elementBuilder.toImplicitElement();
    outData.addImplicitElement(implicitElement);
    // TODO fix
    //final JSFunction function = findFunction(argument);
    //final JSNamespace ns = visitor.findNsForExpr((JSExpression)argument);
    //if (function != null && ns != null) {
    //  visitor.visitWithNamespace(ns, function, false);
    //}
  }

  private void addImplicitElements(final @NotNull JSImplicitElementProvider elementProvider,
                                   final @Nullable String command,
                                   final @NotNull StubIndexKey<String, JSImplicitElementProvider> index,
                                   @Nullable String defaultName,
                                   final @Nullable String value,
                                   final @NotNull JSElementIndexingData outData) {
    if (defaultName == null) return;
    final List<String> keys = INDEXES.getKeysByValue(index);
    assert keys != null && keys.size() == 1;
    final Consumer<JSImplicitElementImpl.Builder> adder = builder -> {
      builder.setType(elementProvider instanceof JSDocComment ? JSImplicitElement.Type.Tag : JSImplicitElement.Type.Class)
        .setUserStringWithData(this, keys.get(0), value);
      final JSImplicitElementImpl implicitElement = builder.toImplicitElement();
      outData.addImplicitElement(implicitElement);
    };

    final Function<String, List<String>> variants = POLY_NAME_CONVERTERS.get(command);

    if (variants != null) {
      final List<String> strings = variants.fun(defaultName);
      for (String string : strings) {
        adder.consume(new JSImplicitElementImpl.Builder(string, elementProvider));
      }
    }
    else {
      adder.consume(new JSImplicitElementImpl.Builder(JSQualifiedNameImpl.fromQualifiedName(defaultName), elementProvider));
    }

    final Function<String, String> converter = command != null ? NAME_CONVERTERS.get(command) : null;
    final String name = converter != null ? converter.fun(defaultName) : defaultName;
    if (!StringUtil.equals(defaultName, name)) {
      JSImplicitElementImpl.Builder symbolElementBuilder = new JSImplicitElementImpl.Builder(name, elementProvider)
        .setType(elementProvider instanceof JSDocComment ? JSImplicitElement.Type.Tag : JSImplicitElement.Type.Class);
      final List<String> symbolKeys = INDEXES.getKeysByValue(AngularSymbolIndex.KEY);
      assert symbolKeys != null && symbolKeys.size() == 1;
      symbolElementBuilder.setUserStringWithData(this, symbolKeys.get(0), value);
      final JSImplicitElementImpl implicitElement2 = symbolElementBuilder.toImplicitElement();
      outData.addImplicitElement(implicitElement2);
    }
  }

  private static String calculateRestrictions(PsiElement element, String defaultRestrictions) {
    final Ref<String> restrict = Ref.create(defaultRestrictions);
    final PsiElement function = findFunction(element);
    if (function != null) {
      function.accept(new JSRecursiveWalkingElementVisitor() {
        @Override
        public void visitJSProperty(@NotNull JSProperty node) {
          final String name = node.getName();
          final JSExpression value = node.getValue();
          if (RESTRICT_PROP.equals(name)) {
            if (value instanceof JSLiteralExpression && ((JSLiteralExpression)value).isQuotedLiteral()) {
              final String unquoted = unquote(value);
              if (unquoted != null) restrict.set(unquoted);
            }
          }
        }
      });
    }
    return restrict.get().trim() + ";;;";
  }

  private static PsiElement findFunction(PsiElement element) {
    PsiElement function = PsiTreeUtil.getNextSiblingOfType(element, JSFunction.class);
    if (function == null) {
      function = PsiTreeUtil.getNextSiblingOfType(element, JSObjectLiteralExpression.class);
    }
    if (function == null) {
      final JSExpression expression = PsiTreeUtil.getNextSiblingOfType(element, JSExpression.class);
      function = findDeclaredFunction(expression);
    }
    if (function == null) {
      JSArrayLiteralExpression array = PsiTreeUtil.getNextSiblingOfType(element, JSArrayLiteralExpression.class);
      function = PsiTreeUtil.findChildOfType(array, JSFunction.class);
      if (function == null) {
        final JSExpression candidate = array != null ? PsiTreeUtil.getPrevSiblingOfType(array.getLastChild(), JSExpression.class) : null;
        function = findDeclaredFunction(candidate);
      }
    }
    return function;
  }

  private static PsiElement findDeclaredFunction(JSExpression expression) {
    final String name = expression instanceof JSReferenceExpression ? ((JSReferenceExpression)expression).getReferenceName() : null;
    if (name != null) {
      ASTNode node = expression.getNode();
      final JSTreeUtil.JSScopeDeclarationsAndAssignments declaration = JSTreeUtil.getDeclarationsAndAssignmentsInScopeAndUp(name, node);
      CompositeElement definition = declaration != null ? declaration.findNearestDefinition(node) : null;
      if (definition != null) {
        return definition.getPsi();
      }
    }
    return null;
  }

  @Override
  public String resolveContextFromProperty(JSObjectLiteralExpression objectLiteralExpression, boolean returnPropertiesNamespace) {
    if (!(objectLiteralExpression.getParent() instanceof JSReturnStatement)) return null;

    final JSFunction function = PsiTreeUtil.getParentOfType(objectLiteralExpression, JSFunction.class);
    final JSCallExpression call = PsiTreeUtil.getParentOfType(function, JSCallExpression.class);
    if (call != null) {
      final JSExpression methodExpression = call.getMethodExpression();
      if (!(methodExpression instanceof JSReferenceExpression callee)) return null;
      JSExpression qualifier = callee.getQualifier();

      if (qualifier == null) return null;

      final String command = callee.getReferenceName();

      if (INJECTABLE_METHODS.contains(command)) {
        JSExpression[] arguments = call.getArguments();
        if (arguments.length > 0) {
          JSExpression argument = arguments[0];
          if (argument instanceof JSLiteralExpression && ((JSLiteralExpression)argument).isQuotedLiteral()) {
            return unquote(argument);
          }
        }
      }
    }
    return null;
  }

  @Override
  public @Nullable JSNamespace findNamespace(@NotNull JSExpression expression, @Nullable Set<PsiElement> visited) {
    PsiElement argumentList = expression.getParent();
    if (!(argumentList instanceof JSArgumentList)) return null;
    PsiElement callExpression = argumentList.getParent();
    if (!(callExpression instanceof JSCallExpression)) return null;
    JSExpression methodExpression = ((JSCallExpression)callExpression).getMethodExpression();
    if (methodExpression instanceof JSReferenceExpression &&
        "controller".equals(((JSReferenceExpression)methodExpression).getReferenceName())) {
      JSExpression[] arguments = ((JSArgumentList)argumentList).getArguments();
      if (arguments.length >= 2 && arguments[1] == expression && arguments[0] instanceof JSLiteralExpression) {
        JSQualifiedName name = JSSymbolUtil.getLiteralValueAsQualifiedName((JSLiteralExpression)arguments[0]);
        if (name != null) {
          return JSNamedTypeFactory.createNamespace(name, JSContext.INSTANCE, expression);
        }
      }
    }
    return null;
  }

  @Override
  public boolean addTypeFromResolveResult(@NotNull JSTypeEvaluator evaluator,
                                          @NotNull JSEvaluateContext context, @NotNull PsiElement resolveResult) {
    if (!AngularIndexUtil.hasAngularJS(resolveResult.getProject())) return false;

    if (resolveResult instanceof JSDefinitionExpression && resolveResult.getLanguage() instanceof AngularJSLanguage) {
      final PsiElement resolveParent = resolveResult.getParent();
      if (AngularJSReferenceExpressionResolver.isAsExpression(resolveParent)) {
        final String name = resolveParent.getFirstChild().getText();
        final JSTypeSource source = JSTypeSourceFactory.createTypeSource(resolveResult);
        final JSType type = JSNamedType.createType(name, source, JSContext.INSTANCE);
        evaluator.addType(type);
        return true;
      }
    }
    if (resolveResult instanceof JSVariable && resolveResult.getLanguage() instanceof AngularJSLanguage) {
      PsiElement resolveParent = resolveResult.getParent().getParent();
      if (resolveParent instanceof AngularJSRepeatExpression) {
        if (calculateRepeatParameterType(evaluator, (AngularJSRepeatExpression)resolveParent)) {
          return true;
        }
      }
    }
    if (resolveResult instanceof JSParameter && context.isFromCurrentFile(resolveResult) && isInjectable(resolveResult)) {
      final String name = ((JSParameter)resolveResult).getName();
      final JSTypeSource source = JSTypeSourceFactory.createTypeSource(resolveResult);
      final JSType type = JSNamedType.createType(name, source, JSContext.UNKNOWN);
      evaluator.addType(type);
    }
    return false;
  }

  private static boolean calculateRepeatParameterType(JSTypeEvaluator evaluator, AngularJSRepeatExpression resolveParent) {
    return JSPsiImplUtils.calculateTypeOfVariableForIteratedExpression(evaluator, findReferenceExpression(resolveParent), resolveParent);
  }

  private static PsiElement findReferenceExpression(AngularJSRepeatExpression parent) {
    JSExpression collection = parent.getCollection();
    while (collection instanceof JSBinaryExpression &&
           ((JSBinaryExpression)collection).getROperand() instanceof AngularJSFilterExpression) {
      collection = ((JSBinaryExpression)collection).getLOperand();
    }
    return collection;
  }

  private PairProcessor<JSProperty, JSElementIndexingData> createRouterParametersProcessor() {
    return new PairProcessor<>() {
      @Override
      public boolean process(JSProperty property, JSElementIndexingData outData) {
        if (!(property.getValue() instanceof JSLiteralExpression value)) return true;
        if (!value.isQuotedLiteral()) return true;
        final String unquotedValue = unquote(value);
        if (AngularJSUiRouterConstants.controllerAs.equals(property.getName())) {
          return recordControllerAs(property, outData, value, unquotedValue);
        }
        else if (CONTROLLER.equals(property.getName())) {
          final int idx = unquotedValue != null ? unquotedValue.indexOf(AS_CONNECTOR_WITH_SPACES) : 0;
          if (idx > 0 && (idx + AS_CONNECTOR_WITH_SPACES.length()) < (unquotedValue.length() - 1)) {
            return recordControllerAs(property, outData, value, unquotedValue.substring(idx + AS_CONNECTOR_WITH_SPACES.length()));
          }
        }
        else if ("name".equals(property.getName())) {
          addImplicitElements(value, STATE, INDEXERS.get(STATE), unquotedValue, null, outData);
          return true;
        }
        return true;
      }

      private boolean recordControllerAs(JSProperty property,
                                         JSElementIndexingData outData,
                                         JSLiteralExpression value,
                                         String unquotedValue) {
        final StubIndexKey<String, JSImplicitElementProvider> index = INDEXERS.get(CONTROLLER);
        assert index != null;
        final JSObjectLiteralExpression object = ObjectUtils.tryCast(property.getParent(), JSObjectLiteralExpression.class);
        if (object == null) return false;
        final JSProperty controllerProperty = object.findProperty(CONTROLLER);
        if (controllerProperty != null) {
          // value (JSFunctionExpression) is not implicit element provider
          addImplicitElements(controllerProperty, null, index, unquotedValue, null, outData);
          return true;
        }
        addImplicitElements(value, null, index, unquotedValue, null, outData);
        return true;
      }
    };
  }

  public static @Nullable String unquote(PsiElement value) {
    return ((JSLiteralExpression)value).getStringValue();
  }

  private static boolean isControllerProperty(@NotNull JSProperty property) {
    PsiElement parent = property.getParent();
    return (parent instanceof JSObjectLiteralExpression) &&
           (((JSObjectLiteralExpression)parent).findProperty(CONTROLLER) != null
            || ((JSObjectLiteralExpression)parent).findProperty(BINDINGS) != null
            || ((JSObjectLiteralExpression)parent).findProperty(SCOPE) != null);
  }

  private boolean processTemplateProperty(@NotNull JSProperty property, @NotNull JSElementIndexingData data) {
    JSExpression expression = property.getValue();
    if ((expression instanceof JSReferenceExpression
         || expression instanceof JSCallExpression)
        && isControllerProperty(property)) {
      return indexComponentTemplateRef(property, Angular2IndexingHandler.getExprReferencedFileUrl(expression), data);
    }
    return false;
  }

  private boolean processTemplateUrlProperty(@NotNull JSProperty property, @NotNull JSElementIndexingData data) {
    JSExpression value;
    if ((value = property.getValue()) instanceof JSLiteralExpression
        && isControllerProperty(property)) {
      return indexComponentTemplateRef(property, unquote(value), data);
    }
    return false;
  }

  private boolean indexComponentTemplateRef(@NotNull JSProperty property,
                                            @Nullable String url,
                                            @NotNull JSElementIndexingData data) {
    if (StringUtil.isEmptyOrSpaces(url)) {
      return false;
    }
    String fileName = new File(url).getName();
    data.addImplicitElement(
      new JSImplicitElementImpl.Builder(fileName, property)
        .setUserStringWithData(this, Objects.requireNonNull(INDEXES.getKeysByValue(AngularTemplateUrlIndex.KEY)).get(0), "TU;;;")
        .toImplicitElement());
    return true;
  }

  private boolean processScopedProperty(JSProperty property, JSElementIndexingData data, String propertyName, boolean isComponent) {
    PsiElement parent = property.getParent();
    if (parent instanceof JSObjectLiteralExpression && parent.getParent() instanceof JSProperty
        && propertyName.equals(((JSProperty)parent.getParent()).getName())
        && property.getName() != null) {
      WrappingCall call = findWrappingCall(property);
      assert call != null;
      JSExpression[] arguments = call.call().getArguments();
      if (arguments.length < 2 ||
          !(arguments[0] instanceof JSLiteralExpression) ||
          !((JSLiteralExpression)arguments[0]).isQuotedLiteral()) {
        return false;
      }
      String name = StringUtil.notNullize(unquote(arguments[0]));
      String restrictions = getRestrictions(parent, isComponent ? "E" : "D");

      String attributeName = DirectiveUtil.getPropertyAlias(property.getName(), property.getValue());

      if (restrictions.contains("E") || restrictions.equals("D")) {
        addImplicitElements(property, attributeName, AngularDirectivesDocIndex.KEY, attributeName,
                            "A;" + name + ";" + getBindingType(property.getValue()) + ";",
                            data);
      }
      if (restrictions.contains("A") || restrictions.equals("D")) {
        addImplicitElements(property, attributeName, AngularDirectivesDocIndex.KEY, attributeName,
                            "A;ANY" + (attributeName.equals(name) ? "" : "=" + name) + ";" + getBindingType(property.getValue()) + ";",
                            data);
      }
      return true;
    }
    return false;
  }

  private static String getRestrictions(PsiElement parent, String defaultRestrictions) {
    JSExpression restrict = ObjectUtils.doIfNotNull(((JSObjectLiteralExpression)parent).findProperty(RESTRICT_PROP), JSProperty::getValue);
    if (restrict instanceof JSLiteralExpression && ((JSLiteralExpression)restrict).isQuotedLiteral()) {
      return StringUtil.notNullize(unquote(restrict), defaultRestrictions);
    }
    return defaultRestrictions;
  }

  private static String getBindingType(@Nullable JSExpression valueExpr) {
    if (valueExpr instanceof JSLiteralExpression) {
      String typeStr = ((JSLiteralExpression)valueExpr).getStringValue();
      if (typeStr != null && typeStr.startsWith("@")) {
        return "constString";
      }
    }
    return "expression";
  }

  @Override
  protected @NotNull Set<@NotNull String> computeJSImplicitElementUserStringKeys() {
    return INDEXES.keySet();
  }
}

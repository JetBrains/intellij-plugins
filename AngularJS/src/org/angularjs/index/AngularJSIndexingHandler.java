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
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.lang.javascript.psi.types.JSNamedType;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory;
import com.intellij.lang.javascript.psi.util.JSTreeUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.*;
import com.intellij.util.containers.BidirectionalMap;
import gnu.trove.THashSet;
import org.angular2.codeInsight.Angular2ReferenceExpressionResolver;
import org.angular2.lang.Angular2LangUtil;
import org.angularjs.codeInsight.AngularJSReferenceExpressionResolver;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.codeInsight.router.AngularJSUiRouterConstants;
import org.angularjs.lang.AngularJSLanguage;
import org.angularjs.lang.psi.AngularJSFilterExpression;
import org.angularjs.lang.psi.AngularJSRepeatExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSIndexingHandler extends FrameworkIndexingHandler {
  private static final Map<String, StubIndexKey<String, JSImplicitElementProvider>> INDEXERS =
    new HashMap<>();
  private static final Map<String, Function<String, String>> NAME_CONVERTERS = new HashMap<>();
  private static final Map<String, Function<PsiElement, String>> DATA_CALCULATORS = new HashMap<>();
  private static final Map<String, PairProcessor<JSProperty, JSElementIndexingData>> CUSTOM_PROPERTY_PROCESSORS = new HashMap<>();
  private final static Map<String, Function<String, List<String>>> POLY_NAME_CONVERTERS = new HashMap<>();
  private final static Map<String, Processor<JSArgumentList>> ARGUMENT_LIST_CHECKERS = new HashMap<>();

  public static final Set<String> INTERESTING_METHODS = new HashSet<>();
  public static final Set<String> INJECTABLE_METHODS = new HashSet<>();
  public static final String CONTROLLER = "controller";
  public static final String DIRECTIVE = "directive";
  public static final String COMPONENT = "component";
  public static final String BINDINGS = "bindings";
  public static final String MODULE = "module";
  public static final String FILTER = "filter";
  public static final String STATE = "state";
  private static final String START_SYMBOL = "startSymbol";
  private static final String END_SYMBOL = "endSymbol";
  public static final String DEFAULT_RESTRICTIONS = "D";
  public static final String WHEN = "when";

  private static final String[] ALL_INTERESTING_METHODS;
  private static final BidirectionalMap<String, StubIndexKey<String, JSImplicitElementProvider>> INDEXES;
  public static final String AS_CONNECTOR_WITH_SPACES = " as ";

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
    NAME_CONVERTERS.put(COMPONENT, NAME_CONVERTERS.get(DIRECTIVE));
    DATA_CALCULATORS.put(COMPONENT, element -> calculateRestrictions(element, "E"));

    INDEXERS.put(CONTROLLER, AngularControllerIndex.KEY);
    INDEXERS.put(MODULE, AngularModuleIndex.KEY);
    INDEXERS.put(FILTER, AngularFilterIndex.KEY);
    INDEXERS.put(STATE, AngularUiRouterStatesIndex.KEY);

    final THashSet<String> allInterestingMethods = new THashSet<>(INTERESTING_METHODS);
    allInterestingMethods.addAll(INJECTABLE_METHODS);
    allInterestingMethods.addAll(INDEXERS.keySet());
    allInterestingMethods.add(START_SYMBOL);
    allInterestingMethods.add(END_SYMBOL);
    ALL_INTERESTING_METHODS = ArrayUtil.toStringArray(allInterestingMethods);

    INDEXES = new BidirectionalMap<>();
    INDEXES.put("aci", AngularControllerIndex.KEY);
    INDEXES.put("addi", AngularDirectivesDocIndex.KEY);
    INDEXES.put(ANGULAR_DIRECTIVES_INDEX_USER_STRING, AngularDirectivesIndex.KEY);
    INDEXES.put(ANGULAR_FILTER_INDEX_USER_STRING, AngularFilterIndex.KEY);
    INDEXES.put("aidi", AngularInjectionDelimiterIndex.KEY);
    INDEXES.put(ANGULAR_MODULE_INDEX_USER_STRING, AngularModuleIndex.KEY);
    INDEXES.put(ANGULAR_SYMBOL_INDEX_USER_STRING, AngularSymbolIndex.KEY);
    INDEXES.put("arsi", AngularUiRouterStatesIndex.KEY);
    INDEXES.put("arsgi", AngularUiRouterGenericStatesIndex.KEY);
    INDEXES.put("agmi", AngularGenericModulesIndex.KEY);

    for (String key : INDEXES.keySet()) {
      JSImplicitElement.ourUserStringsRegistry.registerUserString(key);
    }

    CUSTOM_PROPERTY_PROCESSORS.put(COMPONENT, AngularJSIndexingHandler::bindingsProcessor);
    NAME_CONVERTERS.put(BINDINGS, NAME_CONVERTERS.get(DIRECTIVE));

    final PairProcessor<JSProperty, JSElementIndexingData> processor = createRouterParametersProcessor();
    CUSTOM_PROPERTY_PROCESSORS.put(WHEN, processor);
    CUSTOM_PROPERTY_PROCESSORS.put("otherwise", processor);
    CUSTOM_PROPERTY_PROCESSORS.put("state", processor);
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
  public String[] inheritanceMethodNames() {
    return ALL_INTERESTING_METHODS;
  }

  @NotNull
  @Override
  public String[] implicitProviderMethodNames() {
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
            final Processor<JSArgumentList> argumentListProcessor = ARGUMENT_LIST_CHECKERS.get(command);
            if (argumentListProcessor != null && !argumentListProcessor.process(callExpression.getArgumentList())) return;
            final Function<PsiElement, String> calculator = DATA_CALCULATORS.get(command);
            final String data = calculator != null ? calculator.fun(argument) : null;
            final String argumentText = unquote(argument);
            addImplicitElements(argument, command, index, argumentText, data, outIndexingData);
          }
        } else if (INJECTABLE_METHODS.contains(command)) { // INTERESTING_METHODS are contained in INJECTABLE_METHODS
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
    } else if (JSSymbolUtil.isAccurateReferenceExpressionName(reference, "angular", MODULE)) {
      final JSExpression[] arguments = callExpression.getArguments();
      if (arguments.length > 1 && arguments[0] instanceof JSReferenceExpression) {
        addImplicitElements(callExpression, null, AngularGenericModulesIndex.KEY, MODULE, null, outData);
      }
    }
  }

  @Override
  public boolean shouldCreateStubForCallExpression(ASTNode node) {
    final ASTNode methodExpression = JSCallExpressionImpl.getMethodExpression(node);
    if (methodExpression == null) return false;

    final ASTNode referencedNameElement = methodExpression.getLastChildNode();
    final ASTNode qualifier = JSReferenceExpressionImpl.getQualifierNode(methodExpression);
    if (qualifier == null) return false;
    return STATE.equals(referencedNameElement.getText()) && "$stateProvider".equalsIgnoreCase(qualifier.getText()) ||
           MODULE.equals(referencedNameElement.getText()) && "angular".equalsIgnoreCase(qualifier.getText());
  }

  @Nullable
  @Override
  public JSElementIndexingData processAnyProperty(@NotNull JSProperty property, @Nullable JSElementIndexingData outData) {
    final String name = property.getName();
    if (name == null) return outData;

    final Pair<JSCallExpression, Integer> pair = findImmediatelyWrappingCall(property);
    if (pair == null) return outData;
    final JSCallExpression callExpression = pair.getFirst();
    final int level = pair.getSecond();

    final JSExpression methodExpression = callExpression.getMethodExpression();
    if (!(methodExpression instanceof JSReferenceExpression) || ((JSReferenceExpression)methodExpression).getQualifier() == null) {
      return outData;
    }
    final String command = ((JSReferenceExpression)methodExpression).getReferenceName();
    final PairProcessor<JSProperty, JSElementIndexingData> customProcessor = CUSTOM_PROPERTY_PROCESSORS.get(command);
    JSElementIndexingData localOutData;
    if (customProcessor != null && customProcessor.process(property,
                                                           (localOutData = (outData == null ? new JSElementIndexingDataImpl() : outData)))) {
      return localOutData;
    }
    // for 'standard' properties, keep indexing only for properties - immediate children of function calls parameters
    if (level > 1) return outData;

    final PsiElement parent = property.getParent();
    final StubIndexKey<String, JSImplicitElementProvider> index = INDEXERS.get(command);
    if (index == null) return outData;
    if (callExpression.getArguments()[0] != parent) return outData;

    if (outData == null) outData = new JSElementIndexingDataImpl();
    addImplicitElements(property, command, index, name, null, outData);
    return outData;
  }

  @Nullable
  private static Pair<JSCallExpression, Integer> findImmediatelyWrappingCall(@NotNull JSProperty property) {
    PsiElement current = property.getParent();
    int level = 0;
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
      if (current instanceof JSArgumentList) {
        final PsiElement callExpression = current.getParent();
        if (callExpression instanceof JSCallExpression) return Pair.create((JSCallExpression)callExpression, level);
      }
      return null;
    }
    return null;
  }

  @Override
  public boolean indexImplicitElement(@NotNull JSImplicitElementStructure element, @Nullable IndexSink sink) {
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

        final boolean directive = ngdocValue.contains(DIRECTIVE);
        final boolean component = ngdocValue.contains(COMPONENT);
        if (directive || component) {
          final String restrictions = calculateRestrictions(commentLines, directive ? DEFAULT_RESTRICTIONS : "E");
          if (outData == null) outData = new JSElementIndexingDataImpl();
          addImplicitElements(comment, directive ? DIRECTIVE : COMPONENT, AngularDirectivesDocIndex.KEY, name, restrictions, outData);
        }
        else if (ngdocValue.contains(FILTER)) {
          if (outData == null) outData = new JSElementIndexingDataImpl();
          addImplicitElements(comment, FILTER, AngularFilterIndex.KEY, name, null, outData);
        }
      }
    }
    return outData;
  }

  private static String calculateRestrictions(final String[] commentLines, String defaultRestrictions) {
    String restrict = defaultRestrictions;
    String tag = "";
    String param = "";
    StringBuilder attributes = new StringBuilder();
    for (String line : commentLines) {
      restrict = getParamValue(restrict, line, RESTRICT);
      tag = getParamValue(tag, line, ELEMENT);
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
    return restrict + ";" + tag + ";" + param.trim() + ";" + attributes.toString().trim();
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


  private static void generateNamespace(@NotNull JSLiteralExpression argument,
                                        @NotNull JSElementIndexingData outData) {
    final String namespace = unquote(argument);
    if (namespace == null) return;
    JSQualifiedNameImpl qName = JSQualifiedNameImpl.fromQualifiedName(namespace);
    JSImplicitElementImpl.Builder elementBuilder =
      new JSImplicitElementImpl.Builder(qName, argument)
        .setType(JSImplicitElement.Type.Class).setUserString(ANGULAR_SYMBOL_INDEX_USER_STRING);
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
                                          @Nullable String defaultName,
                                          @Nullable final String value,
                                          @NotNull final JSElementIndexingData outData) {
    if (defaultName == null) return;
    final List<String> keys = INDEXES.getKeysByValue(index);
    assert keys != null && keys.size() == 1;
    final Consumer<JSImplicitElementImpl.Builder> adder = builder -> {
      builder.setType(elementProvider instanceof JSDocComment ? JSImplicitElement.Type.Tag : JSImplicitElement.Type.Class)
        .setTypeString(value);
      builder.setUserString(keys.get(0));
      final JSImplicitElementImpl implicitElement = builder.toImplicitElement();
      outData.addImplicitElement(implicitElement);
    };

    final Function<String, List<String>> variants = POLY_NAME_CONVERTERS.get(command);
    final Function<String, String> converter = command != null ? NAME_CONVERTERS.get(command) : null;
    final String name = converter != null ? converter.fun(defaultName) : defaultName;

    if (variants != null) {
      final List<String> strings = variants.fun(name);
      for (String string : strings) {
        adder.consume(new JSImplicitElementImpl.Builder(string, elementProvider));
      }
    } else {
      adder.consume(new JSImplicitElementImpl.Builder(JSQualifiedNameImpl.fromQualifiedName(name), elementProvider));
    }

    if (!StringUtil.equals(defaultName, name)) {
      JSImplicitElementImpl.Builder symbolElementBuilder = new JSImplicitElementImpl.Builder(defaultName, elementProvider)
        .setType(elementProvider instanceof JSDocComment ? JSImplicitElement.Type.Tag : JSImplicitElement.Type.Class)
        .setTypeString(value);
      final List<String> symbolKeys = INDEXES.getKeysByValue(AngularSymbolIndex.KEY);
      assert symbolKeys != null && symbolKeys.size() == 1;
      symbolElementBuilder.setUserString(symbolKeys.get(0));
      final JSImplicitElementImpl implicitElement2 = symbolElementBuilder.toImplicitElement();
      outData.addImplicitElement(implicitElement2);
    }
  }

  private static String calculateRestrictions(PsiElement element, String defaultRestrictions) {
    final Ref<String> restrict = Ref.create(defaultRestrictions);
    final Ref<String> scope = Ref.create("");
    final PsiElement function = findFunction(element);
    if (function != null) {
      function.accept(new JSRecursiveElementVisitor() {
        @Override
        public void visitJSProperty(JSProperty node) {
          final String name = node.getName();
          final JSExpression value = node.getValue();
          if ("restrict".equals(name)) {
            if (value instanceof JSLiteralExpression && ((JSLiteralExpression)value).isQuotedLiteral()) {
              final String unquoted = unquote(value);
              if (unquoted != null) restrict.set(unquoted);
            }
          } else if ("scope".equals(name)) {
            if (value instanceof JSObjectLiteralExpression) {
              scope.set(StringUtil.join(((JSObjectLiteralExpression)value).getProperties(), PsiNamedElement::getName, ","));
            }
          }
        }
      });
    }
    return restrict.get().trim() + ";;;" + scope.get();
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
            return unquote(argument);
          }
        }
      }
    }
    return null;
  }

  @Override
  public boolean addTypeFromResolveResult(@NotNull JSTypeEvaluator evaluator,
                                          @NotNull JSEvaluateContext context, @NotNull PsiElement resolveResult) {
    if (!AngularIndexUtil.hasAngularJS(resolveResult.getProject()) && !Angular2LangUtil.isAngular2Context(resolveResult)) return false;

    if (resolveResult instanceof JSDefinitionExpression && resolveResult.getLanguage() instanceof AngularJSLanguage) {
      final PsiElement resolveParent = resolveResult.getParent();
      if (AngularJSReferenceExpressionResolver.isAsExpression(resolveParent)
          || Angular2ReferenceExpressionResolver.isAsExpression(resolveParent)) {
        final String name = resolveParent.getFirstChild().getText();
        final JSTypeSource source = JSTypeSourceFactory.createTypeSource(resolveResult);
        final JSType type = JSNamedType.createType(name, source, JSContext.INSTANCE);
        evaluator.addType(type, resolveResult);
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
    if (resolveResult instanceof JSParameter && evaluator.isFromCurrentFile(resolveResult) && isInjectable(resolveResult)) {
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

  private static PairProcessor<JSProperty, JSElementIndexingData> createRouterParametersProcessor() {
    return new PairProcessor<JSProperty, JSElementIndexingData>() {
      @Override
      public boolean process(JSProperty property, JSElementIndexingData outData) {
        if (!(property.getValue() instanceof JSLiteralExpression)) return true;
        final JSLiteralExpression value = (JSLiteralExpression)property.getValue();
        if (!value.isQuotedLiteral()) return true;
        final String unquotedValue = unquote(value);
        if (AngularJSUiRouterConstants.controllerAs.equals(property.getName())) {
          return recordControllerAs(property, outData, value, unquotedValue);
        } else if (CONTROLLER.equals(property.getName())) {
          final int idx = unquotedValue != null ? unquotedValue.indexOf(AS_CONNECTOR_WITH_SPACES) : 0;
          if (idx > 0 && (idx + AS_CONNECTOR_WITH_SPACES.length()) < (unquotedValue.length() - 1)) {
            return recordControllerAs(property, outData, value, unquotedValue.substring(idx + AS_CONNECTOR_WITH_SPACES.length()));
          }
        } else if ("name".equals(property.getName())) {
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

  @Nullable
  public static String unquote(PsiElement value) {
    return ((JSLiteralExpression)value).getStringValue();
  }

  private static boolean bindingsProcessor(JSProperty property, JSElementIndexingData data) {
    PsiElement parent = property.getParent();
    if (parent instanceof JSObjectLiteralExpression && parent.getParent() instanceof JSProperty &&
        BINDINGS.equals(((JSProperty)parent.getParent()).getName())) {
      Pair<JSCallExpression, Integer> call = findImmediatelyWrappingCall(property);
      assert call != null;
      JSExpression[] arguments = call.first.getArguments();
      if (arguments.length < 2 ||
          !(arguments[0] instanceof JSLiteralExpression) ||
          !((JSLiteralExpression)arguments[0]).isQuotedLiteral()) return false;

      final String componentName = unquote(arguments[0]);
      addImplicitElements(property, BINDINGS, AngularDirectivesDocIndex.KEY, DirectiveUtil.getAttributeName(property.getName()),
                          "A;" + (componentName != null ? DirectiveUtil.getAttributeName(componentName) : "") + ";expression;", data);
      return true;
    }
    return false;
  }
}

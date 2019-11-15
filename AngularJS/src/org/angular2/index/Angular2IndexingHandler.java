// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ecmascript6.psi.ES6FromClause;
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration;
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSStubElementTypes;
import com.intellij.lang.javascript.index.FrameworkIndexingHandler;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.stubs.*;
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import one.util.streamex.StreamEx;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularSymbolIndex;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.notNull;
import static java.util.Collections.emptyList;
import static org.angular2.Angular2DecoratorUtil.*;
import static org.angular2.entities.ivy.Angular2IvyUtil.*;

public class Angular2IndexingHandler extends FrameworkIndexingHandler {

  public static final String REQUIRE = "require";

  @NonNls private static final String ANGULAR2_TEMPLATE_URLS_INDEX_USER_STRING = "a2tui";
  @NonNls private static final String ANGULAR2_PIPE_INDEX_USER_STRING = "a2pi";
  @NonNls private static final String ANGULAR2_DIRECTIVE_INDEX_USER_STRING = "a2di";
  @NonNls private static final String ANGULAR2_MODULE_INDEX_USER_STRING = "a2mi";

  @NonNls private static final String PIPE_TYPE = "P;;;";
  @NonNls private static final String DIRECTIVE_TYPE = "D;;;";
  @NonNls private static final String MODULE_TYPE = "M;;;";

  @NonNls public static final String NG_MODULE_INDEX_NAME = "ngModule";

  @NonNls private static final String STYLESHEET_INDEX_PREFIX = "ss/";

  private static final Set<String> STUBBED_PROPERTIES = ContainerUtil.newHashSet(
    TEMPLATE_URL_PROP, STYLE_URLS_PROP, SELECTOR_PROP, INPUTS_PROP, OUTPUTS_PROP);
  private static final Set<String> STUBBED_DECORATORS_STRING_ARGS = ContainerUtil.newHashSet(
    INPUT_DEC, OUTPUT_DEC);

  private final static Map<String, StubIndexKey<String, JSImplicitElementProvider>> INDEX_MAP = new HashMap<>();

  static {
    INDEX_MAP.put(ANGULAR2_TEMPLATE_URLS_INDEX_USER_STRING, Angular2TemplateUrlIndex.KEY);
    INDEX_MAP.put(ANGULAR2_DIRECTIVE_INDEX_USER_STRING, Angular2SourceDirectiveIndex.KEY);
    INDEX_MAP.put(ANGULAR2_PIPE_INDEX_USER_STRING, Angular2SourcePipeIndex.KEY);
    INDEX_MAP.put(ANGULAR2_MODULE_INDEX_USER_STRING, Angular2SourceModuleIndex.KEY);
    for (String key : INDEX_MAP.keySet()) {
      JSImplicitElement.ourUserStringsRegistry.registerUserString(key);
    }
  }

  public static boolean isPipe(@NotNull JSImplicitElement element) {
    return PIPE_TYPE.equals(element.getTypeString());
  }

  public static boolean isDirective(@NotNull JSImplicitElement element) {
    String type = element.getTypeString();
    if (type == null) {
      return false;
    }
    return type.startsWith(DIRECTIVE_TYPE);
  }

  public static boolean isModule(@NotNull JSImplicitElement element) {
    return MODULE_TYPE.equals(element.getTypeString());
  }

  @Override
  public boolean shouldCreateStubForLiteral(ASTNode node) {
    return checkIsInterestingPropertyValue(node.getTreeParent());
  }

  @Override
  public boolean shouldCreateStubForCallExpression(ASTNode node) {
    final ASTNode parent = node.getTreeParent();
    if (parent != null && parent.getElementType() == JSStubElementTypes.ES6_DECORATOR) {
      final ASTNode methodExpression = node.getFirstChildNode();
      if (methodExpression.getElementType() != JSElementTypes.REFERENCE_EXPRESSION) return false;

      final ASTNode referencedNameElement = methodExpression.getFirstChildNode();
      final String decoratorName = referencedNameElement.getText();
      return STUBBED_DECORATORS_STRING_ARGS.contains(decoratorName);
    }
    return false;
  }

  private boolean checkIsInterestingPropertyValue(@Nullable ASTNode parent) {
    if (parent == null) {
      return false;
    }
    if (parent.getElementType() == JSElementTypes.ARGUMENT_LIST) {
      final ASTNode grandParent = parent.getTreeParent();
      return grandParent != null
             && grandParent.getElementType() == JSStubElementTypes.CALL_EXPRESSION
             && shouldCreateStubForCallExpression(grandParent);
    }
    if (parent.getElementType() == JSElementTypes.ARRAY_LITERAL_EXPRESSION) {
      parent = parent.getTreeParent();
    }
    if (parent != null && parent.getElementType() == JSStubElementTypes.PROPERTY) {
      ASTNode identifier = JSPropertyImpl.findNameIdentifier(parent);
      String propName = identifier != null ? JSPsiImplUtils.getNameFromIdentifier(identifier) : null;
      return propName != null && STUBBED_PROPERTIES.contains(propName);
    }
    return false;
  }

  @Override
  public boolean hasSignificantValue(@NotNull JSLiteralExpression expression) {
    return shouldCreateStubForLiteral(expression.getNode());
  }

  @Nullable
  @Override
  public JSElementIndexingDataImpl processDecorator(@NotNull ES6Decorator decorator, @Nullable JSElementIndexingDataImpl data) {
    TypeScriptClass enclosingClass = PsiTreeUtil.getContextOfType(decorator, TypeScriptClass.class);
    if (enclosingClass != null) {
      String decoratorName = decorator.getDecoratorName();
      boolean isComponent = false;
      if (PIPE_DEC.equals(decoratorName)) {
        if (data == null) {
          data = new JSElementIndexingDataImpl();
        }
        addPipe(enclosingClass, data::addImplicitElement, getPropertyValue(decorator, NAME_PROP));
      }
      else if (DIRECTIVE_DEC.equals(decoratorName)
               || (isComponent = COMPONENT_DEC.equals(decoratorName))) {
        if (data == null) {
          data = new JSElementIndexingDataImpl();
        }
        String selector = getPropertyValue(decorator, SELECTOR_PROP);
        addDirective(enclosingClass, data::addImplicitElement, selector);
        if (isComponent) {
          addComponentExternalFilesRefs(decorator, "", data::addImplicitElement,
                                        ContainerUtil.packNullables(getTemplateFileUrl(decorator)));
          addComponentExternalFilesRefs(decorator, STYLESHEET_INDEX_PREFIX, data::addImplicitElement,
                                        getStylesUrls(decorator));
        }
      }
      else if (MODULE_DEC.equals(decoratorName)) {
        if (data == null) {
          data = new JSElementIndexingDataImpl();
        }
        addModule(enclosingClass, data::addImplicitElement);
      }
    }
    return data;
  }

  @Override
  public boolean indexImplicitElement(@NotNull JSImplicitElementStructure element, @Nullable IndexSink sink) {
    if (sink == null) {
      return false;
    }
    final String userID = element.getUserString();
    final StubIndexKey<String, JSImplicitElementProvider> index = userID != null ? INDEX_MAP.get(userID) : null;
    if (index == Angular2SourceDirectiveIndex.KEY) {
      String type = element.toImplicitElement(null).getTypeString();
      if (type != null && type.startsWith(DIRECTIVE_TYPE)) {
        type = type.substring(DIRECTIVE_TYPE.length());
        StringUtil.split(type, "/")
          .forEach(name -> sink.occurrence(index, name));
      }
      return true;
    }
    else if (index != null) {
      sink.occurrence(index, element.getName());
      if (index == Angular2SourcePipeIndex.KEY) {
        sink.occurrence(AngularSymbolIndex.KEY, element.getName());
      }
      else {
        return true;
      }
    }
    return false;
  }

  @Override
  public void indexClassStub(@NotNull JSClassStub<?> jsClassStub, @NotNull IndexSink sink) {
    if (jsClassStub instanceof TypeScriptClassStub) {
      JSAttributeListStub attrs = jsClassStub.findChildStubByType(JSStubElementTypes.ATTRIBUTE_LIST);
      // Do not index abstract classes
      if (attrs == null || attrs.hasModifier(JSAttributeList.ModifierType.ABSTRACT)) {
        return;
      }
      Pair<TypeScriptFieldStub, EntityDefKind> fieldDefPair =
        findEntityDefFieldStubbed((TypeScriptClassStub)jsClassStub);
      if (fieldDefPair != null) {
        EntityDefKind entityDefKind = fieldDefPair.second;
        if (entityDefKind == MODULE_DEF) {
          sink.occurrence(Angular2IvyModuleIndex.KEY, NG_MODULE_INDEX_NAME);
        }
        else if (entityDefKind == PIPE_DEF) {
          String name = PIPE_DEF.getName(fieldDefPair.first);
          if (name != null) {
            sink.occurrence(Angular2IvyPipeIndex.KEY, name);
            sink.occurrence(AngularSymbolIndex.KEY, name);
          }
        }
        else if (entityDefKind == DIRECTIVE_DEF || entityDefKind == COMPONENT_DEF) {
          String selector = ((DirectiveDefKind)entityDefKind).getSelector(fieldDefPair.first);
          if (selector != null) {
            for (String indexName : Angular2EntityUtils.getDirectiveIndexNames(selector.trim())) {
              sink.occurrence(Angular2IvyDirectiveIndex.KEY, indexName);
            }
          }
        }
      }
    }
  }

  private static void addComponentExternalFilesRefs(@NotNull ES6Decorator decorator,
                                                    @NotNull String namePrefix,
                                                    @NotNull Consumer<? super JSImplicitElement> processor,
                                                    @NotNull List<String> fileUrls) {
    for (String fileUrl : fileUrls) {
      int lastSlash = fileUrl.lastIndexOf('/');
      String name = fileUrl.substring(lastSlash + 1);
      //don't index if file name matches TS file name and is in the same directory
      if ((lastSlash <= 0 || (lastSlash == 1 && fileUrl.charAt(0) == '.'))
          && FileUtilRt.getNameWithoutExtension(name)
            .equals(FileUtilRt.getNameWithoutExtension(decorator.getContainingFile().getOriginalFile().getName()))) {
        continue;
      }
      JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(namePrefix + name, decorator)
        .setUserString(ANGULAR2_TEMPLATE_URLS_INDEX_USER_STRING);
      processor.consume(elementBuilder.toImplicitElement());
    }
  }

  private static void addDirective(@NotNull TypeScriptClass directiveClass,
                                   @NotNull Consumer<? super JSImplicitElement> processor,
                                   @NonNls @Nullable String selector) {
    final Set<String> indexNames;
    if (selector == null) {
      selector = "<null>";
      indexNames = Collections.emptySet();
    }
    else {
      indexNames = Angular2EntityUtils.getDirectiveIndexNames(selector.trim());
    }
    JSImplicitElement directive = new JSImplicitElementImpl
      .Builder(notNull(directiveClass.getName(), selector), directiveClass)
      .setType(JSImplicitElement.Type.Class)
      .setTypeString(DIRECTIVE_TYPE + StringUtil.join(indexNames, "/"))
      .setUserString(ANGULAR2_DIRECTIVE_INDEX_USER_STRING)
      .toImplicitElement();
    processor.consume(directive);
  }

  private static void addPipe(@NotNull TypeScriptClass pipeClass,
                              @NotNull Consumer<? super JSImplicitElement> processor,
                              @NonNls @Nullable String pipe) {
    if (pipe == null) {
      pipe = Angular2Bundle.message("angular.description.unnamed");
    }
    JSImplicitElementImpl pipeElement = new JSImplicitElementImpl.Builder(pipe, pipeClass)
      .setUserString(ANGULAR2_PIPE_INDEX_USER_STRING)
      .setTypeString(PIPE_TYPE)
      .setType(JSImplicitElement.Type.Class)
      .toImplicitElement();
    processor.consume(pipeElement);
  }

  private static void addModule(@NotNull TypeScriptClass moduleClass,
                                @NotNull Consumer<JSImplicitElement> processor) {
    JSImplicitElementImpl pipeElement = new JSImplicitElementImpl.Builder(NG_MODULE_INDEX_NAME, moduleClass)
      .setUserString(ANGULAR2_MODULE_INDEX_USER_STRING)
      .setTypeString(MODULE_TYPE)
      .setType(JSImplicitElement.Type.Class)
      .toImplicitElement();
    processor.consume(pipeElement);
  }

  @Nullable
  public static TypeScriptClass findComponentClass(@NotNull PsiElement context) {
    return ContainerUtil.getFirstItem(findComponentClasses(context));
  }

  @NotNull
  public static List<TypeScriptClass> findComponentClasses(@NotNull PsiElement context) {
    final PsiFile file = context.getContainingFile();
    if (file == null
        || !(file.getLanguage().isKindOf(Angular2HtmlLanguage.INSTANCE)
             || file.getLanguage().is(Angular2Language.INSTANCE)
             || isStylesheet(file))) {
      return emptyList();
    }
    PsiFile hostFile = getHostFile(context);
    if (hostFile == null) {
      return emptyList();
    }
    if (!file.getOriginalFile().equals(hostFile) && DialectDetector.isTypeScript(hostFile)) {
      // inline content
      return ContainerUtil.packNullables(PsiTreeUtil.getContextOfType(
        InjectedLanguageManager.getInstance(context.getProject()).getInjectionHost(file.getOriginalFile()),
        TypeScriptClass.class));
    }
    // external content
    List<TypeScriptClass> result = new SmartList<>(
      resolveComponentsFromSimilarFile(hostFile));
    if (!result.isEmpty() && !isStylesheet(file)) {
      return result;
    }
    result.addAll(resolveComponentsFromIndex(hostFile));
    return result;
  }

  @NotNull
  private static List<TypeScriptClass> resolveComponentsFromSimilarFile(@NotNull PsiFile file) {
    final String name = file.getViewProvider().getVirtualFile().getNameWithoutExtension();
    final PsiDirectory dir = file.getParent();
    final PsiFile directiveFile = dir != null ? dir.findFile(name + ".ts") : null;
    if (directiveFile != null) {
      return StreamEx.of(JSStubBasedPsiTreeUtil.getFileOrModuleChildrenStream(directiveFile))
        .select(TypeScriptClass.class)
        .filter(cls -> {
          ES6Decorator dec = findDecorator(cls, COMPONENT_DEC);
          return hasFileReference(dec, file);
        })
        .toList();
    }
    return emptyList();
  }

  @NotNull
  private static List<TypeScriptClass> resolveComponentsFromIndex(@NotNull PsiFile file) {
    final String name = (isStylesheet(file) ? STYLESHEET_INDEX_PREFIX : "") + file.getViewProvider().getVirtualFile().getName();
    final List<TypeScriptClass> result = new SmartList<>();
    AngularIndexUtil.multiResolve(file.getProject(), Angular2TemplateUrlIndex.KEY, name, el -> {
      if (el != null) {
        PsiElement componentDecorator = el.getParent();
        if (componentDecorator instanceof ES6Decorator
            && hasFileReference((ES6Decorator)componentDecorator, file)) {
          ContainerUtil.addIfNotNull(result, PsiTreeUtil.getContextOfType(componentDecorator, TypeScriptClass.class));
        }
      }
      return true;
    });
    return result;
  }

  private static boolean hasFileReference(@Nullable ES6Decorator componentDecorator, @NotNull PsiFile file) {
    Angular2Component component = Angular2EntitiesProvider.getComponent(componentDecorator);
    if (component != null) {
      return isStylesheet(file) ? component.getCssFiles().contains(file) : file.equals(component.getTemplateFile());
    }
    return false;
  }

  private static boolean isStylesheet(@NotNull PsiFile file) {
    return file instanceof StylesheetFile;
  }

  @Nullable
  private static String getTemplateFileUrl(@NotNull ES6Decorator decorator) {
    String templateUrl = getPropertyValue(decorator, TEMPLATE_URL_PROP);
    if (templateUrl != null) {
      return templateUrl;
    }
    JSProperty property = getProperty(decorator, TEMPLATE_PROP);
    if (property != null) {
      return getExprReferencedFileUrl(property.getValue());
    }
    return null;
  }

  @NotNull
  private static List<String> getStylesUrls(@NotNull ES6Decorator decorator) {
    List<String> result = new SmartList<>();

    BiConsumer<String, Function<JSExpression, String>> urlsGetts = (name, func) ->
      StreamEx.of(name)
        .map(prop -> getProperty(decorator, prop))
        .nonNull()
        .map(JSProperty::getValue)
        .select(JSArrayLiteralExpression.class)
        .flatArray(JSArrayLiteralExpression::getExpressions)
        .map(func)
        .nonNull()
        .into(result);

    urlsGetts.accept(STYLE_URLS_PROP, Angular2DecoratorUtil::getExpressionStringValue);
    urlsGetts.accept(STYLES_PROP, Angular2IndexingHandler::getExprReferencedFileUrl);

    return result;
  }

  @Nullable
  public static String getExprReferencedFileUrl(@Nullable JSExpression expression) {
    if (expression instanceof JSReferenceExpression) {
      for (PsiElement resolvedElement : AngularIndexUtil.resolveLocally((JSReferenceExpression)expression)) {
        if (resolvedElement instanceof ES6ImportedBinding) {
          ES6FromClause from = doIfNotNull(((ES6ImportedBinding)resolvedElement).getDeclaration(),
                                           ES6ImportDeclaration::getFromClause);
          if (from != null) {
            return doIfNotNull(from.getReferenceText(), StringUtil::unquoteString);
          }
        }
      }
    }
    else if (expression instanceof JSCallExpression) {
      JSReferenceExpression referenceExpression = ObjectUtils.tryCast(
        ((JSCallExpression)expression).getMethodExpression(), JSReferenceExpression.class);
      JSExpression[] arguments = ((JSCallExpression)expression).getArguments();
      if (arguments.length == 1
          && arguments[0] instanceof JSLiteralExpression
          && (((JSLiteralExpression)arguments[0]).isQuotedLiteral())
          && referenceExpression != null
          && referenceExpression.getQualifier() == null
          && REQUIRE.equals(referenceExpression.getReferenceName())
      ) {
        return ((JSLiteralExpression)arguments[0]).getStringValue();
      }
    }
    return null;
  }

  @Nullable
  private static PsiFile getHostFile(@NotNull PsiElement context) {
    final PsiElement original = CompletionUtil.getOriginalOrSelf(context);
    PsiFile hostFile = FileContextUtil.getContextFile(original != context ? original : context.getContainingFile().getOriginalFile());
    return hostFile != null ? hostFile.getOriginalFile() : null;
  }
}

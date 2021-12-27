// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ecmascript6.psi.ES6FromClause;
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration;
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSStubElementTypes;
import com.intellij.lang.javascript.index.FrameworkIndexingHandler;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.stubs.JSClassStub;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElementStructure;
import com.intellij.lang.javascript.psi.stubs.TypeScriptClassStub;
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.Consumer;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import one.util.streamex.StreamEx;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.entities.ivy.Angular2IvySymbolDef;
import org.angular2.lang.Angular2Bundle;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularSymbolIndex;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.notNull;
import static org.angular2.Angular2DecoratorUtil.*;
import static org.angular2.entities.Angular2ComponentLocator.isStylesheet;

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

  public static final TokenSet TS_CLASS_TOKENS = TokenSet.create(JSStubElementTypes.TYPESCRIPT_CLASS,
                                                                 JSStubElementTypes.TYPESCRIPT_CLASS_EXPRESSION);

  private static final Set<String> STUBBED_PROPERTIES = ContainerUtil.newHashSet(
    TEMPLATE_URL_PROP, STYLE_URLS_PROP, SELECTOR_PROP, INPUTS_PROP, OUTPUTS_PROP);
  private static final Set<String> STUBBED_DECORATORS_STRING_ARGS = ContainerUtil.newHashSet(
    INPUT_DEC, OUTPUT_DEC, ATTRIBUTE_DEC);

  private static final Map<String, StubIndexKey<String, JSImplicitElementProvider>> INDEX_MAP = new HashMap<>();

  static {
    INDEX_MAP.put(ANGULAR2_TEMPLATE_URLS_INDEX_USER_STRING, Angular2TemplateUrlIndex.KEY);
    INDEX_MAP.put(ANGULAR2_DIRECTIVE_INDEX_USER_STRING, Angular2SourceDirectiveIndex.KEY);
    INDEX_MAP.put(ANGULAR2_PIPE_INDEX_USER_STRING, Angular2SourcePipeIndex.KEY);
    INDEX_MAP.put(ANGULAR2_MODULE_INDEX_USER_STRING, Angular2SourceModuleIndex.KEY);
  }

  public static boolean isPipe(@NotNull JSImplicitElement element) {
    return ANGULAR2_PIPE_INDEX_USER_STRING.equals(element.getUserString());
  }

  public static boolean isDirective(@NotNull JSImplicitElement element) {
    return ANGULAR2_DIRECTIVE_INDEX_USER_STRING.equals(element.getUserString());
  }

  public static boolean isModule(@NotNull JSImplicitElement element) {
    return ANGULAR2_MODULE_INDEX_USER_STRING.equals(element.getUserString());
  }

  public static boolean isDecoratorStringArgStubbed(@NotNull ES6Decorator decorator) {
    return STUBBED_DECORATORS_STRING_ARGS.contains(decorator.getDecoratorName());
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
      if (referencedNameElement == null) return false;
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

  @Override
  public @Nullable JSElementIndexingDataImpl processDecorator(@NotNull ES6Decorator decorator, @Nullable JSElementIndexingDataImpl data) {
    TypeScriptClass enclosingClass = getClassForDecoratorElement(decorator);
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
      String type = element.toImplicitElement(null).getUserStringData();
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
      Angular2IvySymbolDef.Entity entityDef = Angular2IvySymbolDef.get((TypeScriptClassStub)jsClassStub, false);
      if (entityDef != null) {
        if (entityDef instanceof Angular2IvySymbolDef.Module) {
          sink.occurrence(Angular2IvyModuleIndex.KEY, NG_MODULE_INDEX_NAME);
        }
        else if (entityDef instanceof Angular2IvySymbolDef.Pipe) {
          String name = ((Angular2IvySymbolDef.Pipe)entityDef).getName();
          if (name != null) {
            sink.occurrence(Angular2IvyPipeIndex.KEY, name);
            sink.occurrence(AngularSymbolIndex.KEY, name);
          }
        }
        else if (entityDef instanceof Angular2IvySymbolDef.Directive) {
          String selector = ((Angular2IvySymbolDef.Directive)entityDef).getSelector();
          if (selector != null) {
            for (String indexName : Angular2EntityUtils.getDirectiveIndexNames(selector.trim())) {
              sink.occurrence(Angular2IvyDirectiveIndex.KEY, indexName);
            }
          }
        }
      }
    }
  }

  private void addComponentExternalFilesRefs(@NotNull ES6Decorator decorator,
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
        .setUserString(this, ANGULAR2_TEMPLATE_URLS_INDEX_USER_STRING);
      processor.consume(elementBuilder.toImplicitElement());
    }
  }

  private void addDirective(@NotNull TypeScriptClass directiveClass,
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
      .setUserStringWithData(this, ANGULAR2_DIRECTIVE_INDEX_USER_STRING, DIRECTIVE_TYPE + StringUtil.join(indexNames, "/"))
      .toImplicitElement();
    processor.consume(directive);
  }

  private void addPipe(@NotNull TypeScriptClass pipeClass,
                       @NotNull Consumer<? super JSImplicitElement> processor,
                       @NonNls @Nullable String pipe) {
    if (pipe == null) {
      pipe = Angular2Bundle.message("angular.description.unnamed");
    }
    JSImplicitElementImpl pipeElement = new JSImplicitElementImpl.Builder(pipe, pipeClass)
      .setUserStringWithData(this, ANGULAR2_PIPE_INDEX_USER_STRING, PIPE_TYPE)
      .setType(JSImplicitElement.Type.Class)
      .toImplicitElement();
    processor.consume(pipeElement);
  }

  private void addModule(@NotNull TypeScriptClass moduleClass, @NotNull Consumer<JSImplicitElement> processor) {
    JSImplicitElementImpl pipeElement = new JSImplicitElementImpl.Builder(NG_MODULE_INDEX_NAME, moduleClass)
      .setUserStringWithData(this, ANGULAR2_MODULE_INDEX_USER_STRING, MODULE_TYPE)
      .setType(JSImplicitElement.Type.Class)
      .toImplicitElement();
    processor.consume(pipeElement);
  }

  @ApiStatus.Internal
  public static @NotNull List<TypeScriptClass> resolveComponentsFromIndex(@NotNull PsiFile file, @NotNull Predicate<ES6Decorator> filter) {
    final String name = (isStylesheet(file) ? STYLESHEET_INDEX_PREFIX : "") + file.getViewProvider().getVirtualFile().getName();
    final List<TypeScriptClass> result = new SmartList<>();
    AngularIndexUtil.multiResolve(file.getProject(), Angular2TemplateUrlIndex.KEY, name, el -> {
      if (el != null) {
        PsiElement componentDecorator = el.getParent();
        if (componentDecorator instanceof ES6Decorator && filter.test((ES6Decorator)componentDecorator)) {
          ContainerUtil.addIfNotNull(result, getClassForDecoratorElement(componentDecorator));
        }
      }
      return true;
    });
    return result;
  }

  private static @Nullable String getTemplateFileUrl(@NotNull ES6Decorator decorator) {
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

  private static @NotNull List<String> getStylesUrls(@NotNull ES6Decorator decorator) {
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

  public static @Nullable String getExprReferencedFileUrl(@Nullable JSExpression expression) {
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

  @Override
  protected @NotNull Set<@NotNull String> computeJSImplicitElementUserStringKeys() {
    return INDEX_MAP.keySet();
  }
}

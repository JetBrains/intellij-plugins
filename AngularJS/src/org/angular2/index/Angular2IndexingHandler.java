// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.json.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSInjectionController;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.frameworks.jquery.JQueryCssLanguage;
import com.intellij.lang.javascript.index.FrameworkIndexingHandler;
import com.intellij.lang.javascript.index.JSImplicitElementsIndex;
import com.intellij.lang.javascript.index.JSIndexContentBuilder;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;
import com.intellij.lang.javascript.psi.resolve.JSTypeInfo;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl;
import com.intellij.lang.javascript.psi.types.JSNamedTypeFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.*;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.Consumer;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.MultiMap;
import org.angular2.codeInsight.attributes.Angular2EventHandlerDescriptor;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angularjs.codeInsight.AngularJSProcessor;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularJSIndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

import static org.angularjs.index.AngularJSIndexingHandler.ANGULAR_DIRECTIVES_INDEX_USER_STRING;
import static org.angularjs.index.AngularJSIndexingHandler.ANGULAR_FILTER_INDEX_USER_STRING;

public class Angular2IndexingHandler extends FrameworkIndexingHandler {
  public static final String TEMPLATE_REF = "TemplateRef";
  public static final String SELECTOR = "selector";
  public static final String TEMPLATE_URL = "templateUrl";
  public static final String NAME = "name";

  public static final String ANGULAR_TEMPLATE_URLS_INDEX_USER_STRING = "atui";

  @Override
  public void processCallExpression(JSCallExpression callExpression, @NotNull JSElementIndexingData outData) {
    final JSExpression expression = callExpression.getMethodExpression();
    if (expression instanceof JSReferenceExpression) {
      final String name = ((JSReferenceExpression)expression).getReferenceName();
      if (isDirective(name)) {
        addDirective(callExpression, outData::addImplicitElement, getPropertyName(callExpression, SELECTOR));
        addDirectiveTemplateRef(callExpression, outData::addImplicitElement, getPropertyName(callExpression, TEMPLATE_URL));
      }
      if (isPipe(name)) {
        addPipe(callExpression, outData::addImplicitElement, getPropertyName(callExpression, NAME));
      }
      if (isModule(name)) {
        addImplicitElementToModules(callExpression, outData::addImplicitElement, determineModuleName(callExpression));
      }
    }
  }

  @Override
  public boolean shouldCreateStubForCallExpression(ASTNode node) {
    ASTNode ref = node.getFirstChildNode();
    if (ref.getElementType() == JSTokenTypes.NEW_KEYWORD) {
      ref = TreeUtil.findSibling(ref, JSElementTypes.REFERENCE_EXPRESSION);
    }
    if (ref != null) {
      final ASTNode name = ref.getLastChildNode();
      if (name != null && name.getElementType() == JSTokenTypes.IDENTIFIER) {
        final String referencedName = name.getText();
        return isDirective(referencedName) || isPipe(referencedName) || isModule(referencedName);
      }
    }
    return false;
  }

  private static String determineModuleName(@NotNull JSCallExpression callExpression) {
    if (!(callExpression.getParent() instanceof ES6Decorator)) return null;
    final ES6Decorator decorator = (ES6Decorator)callExpression.getParent();
    final PsiElement owner = decorator.getOwner();
    if (owner instanceof JSClass) return ((JSClass)owner).getName();
    return null;
  }

  private static void addImplicitElementToModules(@NotNull PsiElement decorator,
                                                  @NotNull Consumer<JSImplicitElement> processor,
                                                  @Nullable String selector) {
    if (selector == null) return;
    JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(selector, decorator)
      .setUserString(AngularJSIndexingHandler.ANGULAR_MODULE_INDEX_USER_STRING);
    processor.consume(elementBuilder.toImplicitElement());
  }

  private static void addDirective(@NotNull PsiElement element,
                                   @NotNull Consumer<JSImplicitElement> processor,
                                   @Nullable String selector) {
    if (selector == null) return;
    selector = selector.replace("\\n", "\n");
    final MultiMap<String, String> attributesToElements = MultiMap.createSet();
    PsiFile cssFile = PsiFileFactory.getInstance(element.getProject()).createFileFromText(JQueryCssLanguage.INSTANCE, selector);
    CssSelectorList selectorList = PsiTreeUtil.findChildOfType(cssFile, CssSelectorList.class);
    if (selectorList == null) return;
    for (CssSelector cssSelector : selectorList.getSelectors()) {
      for (CssSimpleSelector simpleSelector : cssSelector.getSimpleSelectors()) {
        String elementName = simpleSelector.getElementName();
        boolean seenAttribute = false;
        for (CssSelectorSuffix suffix : simpleSelector.getSelectorSuffixes()) {
          if (!(suffix instanceof CssAttribute)) continue;
          String name = ((CssAttribute)suffix).getAttributeName();
          if (!StringUtil.isEmpty(name)) {
            if (seenAttribute) name = "[" + name + "]";
            attributesToElements.putValue(name, elementName);
          }
          seenAttribute = true;
        }
        if (!seenAttribute) attributesToElements.putValue("", elementName);
      }
    }
    Set<String> added = new HashSet<>();
    boolean template = isTemplate(element);
    for (String elementName : attributesToElements.get("")) {
      if (!added.add(elementName)) continue;
      JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(elementName, element)
        .setType(JSImplicitElement.Type.Class);
      if (!attributesToElements.containsKey(elementName)) {
        elementBuilder.setTypeString("E;;;");
      }
      else {
        Collection<String> elements = attributesToElements.get(elementName);
        elementBuilder.setTypeString("AE;" + StringUtil.join(elements, ",") + ";;");
      }
      elementBuilder.setUserString(ANGULAR_DIRECTIVES_INDEX_USER_STRING);
      processor.consume(elementBuilder.toImplicitElement());
    }

    for (Map.Entry<String, Collection<String>> entry : attributesToElements.entrySet()) {
      JSImplicitElementImpl.Builder elementBuilder;
      String attributeName = entry.getKey();
      if (attributeName.isEmpty()) {
        continue;
      }
      if (!added.add(attributeName)) continue;
      String elements = StringUtil.join(entry.getValue(), ",");
      if (template && elements.isEmpty()) {
        elementBuilder = new JSImplicitElementImpl.Builder(attributeName, element)
          .setType(JSImplicitElement.Type.Class).setTypeString("A;template,ng-template;;");
        elementBuilder.setUserString(ANGULAR_DIRECTIVES_INDEX_USER_STRING);
        processor.consume(elementBuilder.toImplicitElement());
      }
      final String prefix = template && !attributeName.startsWith("[") ? "*" : "";
      final String attr = prefix + attributeName;
      elementBuilder = new JSImplicitElementImpl.Builder(attr, element)
        .setType(JSImplicitElement.Type.Class).setTypeString("A;" + elements + ";;");
      elementBuilder.setUserString(ANGULAR_DIRECTIVES_INDEX_USER_STRING);
      processor.consume(elementBuilder.toImplicitElement());
    }
  }

  private static void addDirectiveTemplateRef(@NotNull JSCallExpression decorator,
                                              @NotNull Consumer<JSImplicitElement> processor,
                                              @Nullable String templateUrl) {
    if (templateUrl == null) return;
    String name = new File(templateUrl).getName();
    JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(name, decorator)
      .setUserString(ANGULAR_TEMPLATE_URLS_INDEX_USER_STRING)
      .setTypeString("TU;;;");
    processor.consume(elementBuilder.toImplicitElement());
  }

  private static void addPipe(PsiElement expression, @NotNull Consumer<JSImplicitElement> processor, String pipe) {
    if (pipe == null) return;
    JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(pipe, expression)
      .setUserString(ANGULAR_FILTER_INDEX_USER_STRING)
      .setType(JSImplicitElement.Type.Class)
      .setTypeString("P;;;");
    processor.consume(elementBuilder.toImplicitElement());
  }

  public static boolean isPipeType(String type) {
    return "P;;;".equals(type);
  }

  private static boolean isTemplate(PsiElement decorator) {
    final JSClass clazz = PsiTreeUtil.getParentOfType(decorator, JSClass.class);
    if (clazz != null) {
      final JSFunction constructor = clazz.getConstructor();
      final JSParameterList params = constructor != null ? constructor.getParameterList() : null;
      return params != null && params.getText().contains(TEMPLATE_REF);
    }

    JsonProperty property = PsiTreeUtil.getParentOfType(decorator, JsonProperty.class);
    if (property == null || !"selector".equals(property.getName())) return false;
    property = PsiTreeUtil.getParentOfType(property, JsonProperty.class);
    if (property == null || !"arguments".equals(property.getName())) return false;
    property = PsiTreeUtil.getParentOfType(property, JsonProperty.class);
    if (property == null || !"decorators".equals(property.getName())) return false;
    PsiElement parent = property.getParent();
    if (parent instanceof JsonObject) {
      JsonProperty members = ((JsonObject)parent).findProperty("members");
      return members != null && members.getText().contains(TEMPLATE_REF);
    }
    return false;
  }

  @Nullable
  private static String getPropertyName(PsiElement decorator, String name) {
    final JSProperty selector = getProperty(decorator, name);
    final JSExpression value = selector != null ? selector.getValue() : null;
    if (value instanceof JSBinaryExpression) {
      return JSInjectionController.getConcatenationText(value);
    }
    if (value instanceof JSLiteralExpression && ((JSLiteralExpression)value).isQuotedLiteral()) {
      return AngularJSIndexingHandler.unquote(value);
    }
    return null;
  }

  @Nullable
  public static JSProperty getSelector(PsiElement decorator) {
    return getProperty(decorator instanceof ES6Decorator ? PsiTreeUtil.findChildOfType(decorator, JSCallExpression.class) : decorator,
                       SELECTOR);
  }

  @Nullable
  private static JSProperty getProperty(PsiElement decorator, String name) {
    final JSArgumentList argumentList = PsiTreeUtil.getChildOfType(decorator, JSArgumentList.class);
    JSExpression[] arguments = argumentList != null ? argumentList.getArguments() : null;
    if (arguments == null) {
      final JSArrayLiteralExpression array = PsiTreeUtil.getChildOfType(decorator, JSArrayLiteralExpression.class);
      arguments = array != null ? array.getExpressions() : null;
    }
    final JSObjectLiteralExpression descriptor = ObjectUtils.tryCast(arguments != null && arguments.length > 0 ? arguments[0] : null,
                                                                     JSObjectLiteralExpression.class);
    return descriptor != null ? descriptor.findProperty(name) : null;
  }

  public static boolean isDirective(@Nullable String name) {
    return "Directive".equals(name) || "DirectiveAnnotation".equals(name) ||
           "Component".equals(name) || "ComponentAnnotation".equals(name);
  }

  public static boolean isModule(@Nullable String name) {
    return "NgModule".equals(name);
  }

  private static boolean isPipe(@Nullable String name) {
    return "Pipe".equals(name);
  }

  @Override
  public void addContextType(JSTypeInfo info, PsiElement context) {
    if (context instanceof JSReferenceExpression && ((JSReferenceExpression)context).getQualifier() == null) {
      final JSQualifiedName directiveNamespace = findDirective(context);
      if (directiveNamespace != null) {
        info.addType(JSNamedTypeFactory.createNamespace(directiveNamespace, JSContext.INSTANCE, null, true), true);
      }
    }
  }

  @Override
  public void addContextNames(PsiElement context, List<String> names) {
    if (context instanceof JSReferenceExpression && ((JSReferenceExpression)context).getQualifier() == null) {
      final JSQualifiedName directiveNamespace = findDirective(context);
      if (directiveNamespace != null) {
        names.add(directiveNamespace.getQualifiedName());
      }
    }
  }

  @Nullable
  private static JSQualifiedName findDirective(PsiElement context) {
    JSClass clazz = findDirectiveClass(context);
    return clazz != null ? JSQualifiedNameImpl.buildProvidedNamespace(clazz) : null;
  }

  @Nullable
  public static JSClass findDirectiveClass(PsiElement context) {
    final PsiFile file = context.getContainingFile();
    if (file == null
        || !(file.getLanguage().is(Angular2HtmlLanguage.INSTANCE)
             || file.getLanguage().is(Angular2Language.INSTANCE))) {
      return null;
    }
    PsiFile hostFile = getHostFile(context);
    if (hostFile == null) {
      return null;
    }
    if (!file.getOriginalFile().equals(hostFile)) {// inline template
      return PsiTreeUtil.getParentOfType(InjectedLanguageManager.getInstance(context.getProject()).getInjectionHost(file), JSClass.class);
    }
    // non inline template file
    final String name = hostFile.getViewProvider().getVirtualFile().getName();
    final Ref<JSClass> result = new Ref<>();
    AngularIndexUtil.multiResolve(hostFile.getProject(), Angular2TemplateUrlIndex.KEY, name, el -> {
      if (el != null) {
        PsiElement componentDecorator = el.getParent();
        if (componentDecorator instanceof JSCallExpression) {
          JSProperty templateUrl = getProperty(componentDecorator, "templateUrl");
          if (templateUrl == null || templateUrl.getValue() == null) {
            return true;
          }
          for (PsiReference ref : templateUrl.getValue().getReferences()) {
            PsiElement templateFile = ref.resolve();
            if (hostFile.equals(templateFile)) {
              result.set((JSClass)PsiTreeUtil.findFirstParent(componentDecorator, JSClass.class::isInstance));
              return false;
            }
          }
        }
      }
      return true;
    });
    return result.get();
  }

  private static PsiFile getHostFile(PsiElement context) {
    final PsiElement original = CompletionUtil.getOriginalOrSelf(context);
    PsiFile hostFile = FileContextUtil.getContextFile(original != context ? original : context.getContainingFile().getOriginalFile());
    return hostFile != null ? hostFile.getOriginalFile() : null;
  }

  @Override
  public boolean addTypeFromResolveResult(@NotNull JSTypeEvaluator evaluator,
                                          @NotNull JSEvaluateContext context, @NotNull PsiElement result) {
    if (!(result instanceof JSImplicitElement) || !AngularJSProcessor.$EVENT.equals(((JSImplicitElement)result).getName())) {
      return false;
    }
    XmlAttribute parent = ObjectUtils.tryCast(result.getParent(), XmlAttribute.class);
    Angular2EventHandlerDescriptor descriptor = ObjectUtils.tryCast(parent != null ? parent.getDescriptor() : null,
                                                                    Angular2EventHandlerDescriptor.class);
    PsiElement declaration = descriptor != null ? descriptor.getDeclaration() : null;
    JSType type = null;
    if (declaration instanceof JSField) {
      type = ((JSField)declaration).getType();
    }
    else if (declaration instanceof JSFunction) {
      type = ((JSFunction)declaration).getReturnType();
    }
    type = JSTypeUtils.getValuableType(type);
    if (type instanceof JSGenericTypeImpl) {
      List<JSType> arguments = ((JSGenericTypeImpl)type).getArguments();
      if (arguments.size() == 1) {
        evaluator.addType(arguments.get(0), declaration);
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canProcessCustomElement(@NotNull PsiElement element) {
    return element instanceof JsonProperty && element.getContainingFile().getName().endsWith(".metadata.json");
  }

  @Override
  public boolean processCustomElement(@NotNull PsiElement customElement, @NotNull JSIndexContentBuilder builder) {
    if (customElement instanceof JsonProperty
        && "selector".equals(((JsonProperty)customElement).getName())) {
      JsonValue value = ((JsonProperty)customElement).getValue();
      if (value instanceof JsonStringLiteral) {
        addDirective(value, createIndexContentBuilderProcessor(builder), ((JsonStringLiteral)value).getValue());
      }
    }
    else //noinspection ConstantConditions
      if (customElement instanceof JsonProperty
          && "name".equals(((JsonProperty)customElement).getName())
          && ((JsonProperty)customElement).getValue() instanceof JsonStringLiteral
          && "Pipe".equals(((JsonStringLiteral)((JsonProperty)customElement).getValue()).getValue())) {
        JsonObject object = (JsonObject)PsiTreeUtil.findFirstParent(
          customElement,
          p -> p instanceof JsonObject && ((JsonObject)p).findProperty("arguments") != null);
        if (object != null) {
          JsonProperty args = object.findProperty("arguments");
          assert args != null;
          JsonArray argsArr = ObjectUtils.tryCast(args.getValue(), JsonArray.class);
          if (argsArr != null) {
            for (JsonElement el : argsArr.getValueList()) {
              if (el instanceof JsonObject) {
                JsonProperty nameProp = ((JsonObject)el).findProperty("name");
                if (nameProp != null && nameProp.getValue() instanceof JsonStringLiteral) {
                  final String pipeName = ((JsonStringLiteral)nameProp.getValue()).getValue();
                  addPipe(nameProp.getValue(), createIndexContentBuilderProcessor(builder), pipeName);
                }
              }
            }
          }
        }
      }
    return true;
  }

  @Override
  public int getVersion() {
    return AngularIndexUtil.BASE_VERSION;
  }

  @NotNull
  private static Consumer<JSImplicitElement> createIndexContentBuilderProcessor(@NotNull JSIndexContentBuilder builder) {
    return element -> {
      JSImplicitElementImpl.Builder elementBuilder = ((JSImplicitElementImpl)element).toBuilder().setProvider(null);
      JSImplicitElementsIndex.JSElementProxy proxy =
        new JSImplicitElementsIndex.JSElementProxy(elementBuilder, element.getParent().getTextOffset());
      builder.addImplicitElement(element.getName(), proxy);
    };
  }
}

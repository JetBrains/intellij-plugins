package org.angularjs.index;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
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
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;
import com.intellij.lang.javascript.psi.resolve.JSTypeInfo;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.css.*;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.MultiMap;
import org.angularjs.codeInsight.AngularJSProcessor;
import org.angularjs.codeInsight.attributes.AngularEventHandlerDescriptor;
import org.angularjs.html.Angular2HTMLLanguage;
import org.angularjs.lang.AngularJSLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.angularjs.index.AngularJSIndexingHandler.ANGULAR_DIRECTIVES_INDEX_USER_STRING;
import static org.angularjs.index.AngularJSIndexingHandler.ANGULAR_FILTER_INDEX_USER_STRING;

public class AngularJS2IndexingHandler extends FrameworkIndexingHandler {
  public static final String TEMPLATE_REF = "TemplateRef";
  public static final String SELECTOR = "selector";
  public static final String NAME = "name";

  @Override
  public void processCallExpression(JSCallExpression callExpression, @NotNull JSElementIndexingData outData) {
    final JSExpression expression = callExpression.getMethodExpression();
    if (expression instanceof JSReferenceExpression) {
      final String name = ((JSReferenceExpression)expression).getReferenceName();
      if (isDirective(name)) {
        addImplicitElement(callExpression, (JSElementIndexingDataImpl)outData, getPropertyName(callExpression, SELECTOR));
      }
      if (isPipe(name)) {
        addPipe(callExpression, (JSElementIndexingDataImpl)outData, getPropertyName(callExpression, NAME));
      }
      if (isModule(name)) {
        addImplicitElementToModules(callExpression, (JSElementIndexingDataImpl)outData, determineModuleName(callExpression));
      }
     }
  }

  @Override
  public boolean shouldCreateStubForCallExpression(ASTNode node) {
    ASTNode ref = node.getFirstChildNode();
    if (ref.getElementType() == JSTokenTypes.NEW_KEYWORD) {
      ref = TreeUtil.findSibling(ref, JSElementTypes.REFERENCE_EXPRESSION);
    }
    if (ref != null){
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

  private static void addImplicitElementToModules(PsiElement decorator,
                                                  @NotNull JSElementIndexingDataImpl outData,
                                                  String selector) {
    if (selector == null) return;
    JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(selector, decorator)
      .setUserString(AngularJSIndexingHandler.ANGULAR_MODULE_INDEX_USER_STRING);
    outData.addImplicitElement(elementBuilder.toImplicitElement());
  }

  private static JSElementIndexingDataImpl addImplicitElement(PsiElement element,
                                                              JSElementIndexingDataImpl outData,
                                                              String selector) {
    if (selector == null) return outData;
    selector = selector.replace("\\n", "\n");
    final MultiMap<String, String> attributesToElements = MultiMap.createSet();
    PsiFile cssFile = PsiFileFactory.getInstance(element.getProject()).createFileFromText(JQueryCssLanguage.INSTANCE, selector);
    CssSelectorList selectorList = PsiTreeUtil.findChildOfType(cssFile, CssSelectorList.class);
    if (selectorList == null) return outData;
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
      } else {
        Collection<String> elements = attributesToElements.get(elementName);
        elementBuilder.setTypeString("AE;" + StringUtil.join(elements, ",") + ";;");
      }
      elementBuilder.setUserString(ANGULAR_DIRECTIVES_INDEX_USER_STRING);
      if (outData == null) outData = new JSElementIndexingDataImpl();
      outData.addImplicitElement(elementBuilder.toImplicitElement());
    }

    for (Map.Entry<String, Collection<String>> entry : attributesToElements.entrySet()) {
      JSImplicitElementImpl.Builder elementBuilder;
      String attributeName = entry.getKey();
      if (attributeName.isEmpty()) {
        continue;
      }
      if (!added.add(attributeName)) continue;
      if (outData == null) outData = new JSElementIndexingDataImpl();
      String elements = StringUtil.join(entry.getValue(), ",");
      if (template && elements.isEmpty()) {
        elementBuilder = new JSImplicitElementImpl.Builder(attributeName, element)
          .setType(JSImplicitElement.Type.Class).setTypeString("A;template,ng-template;;");
        elementBuilder.setUserString(ANGULAR_DIRECTIVES_INDEX_USER_STRING);
        outData.addImplicitElement(elementBuilder.toImplicitElement());
      }
      final String prefix = template && !attributeName.startsWith("[") ? "*" : "";
      final String attr = prefix + attributeName;
      elementBuilder = new JSImplicitElementImpl.Builder(attr, element)
        .setType(JSImplicitElement.Type.Class).setTypeString("A;" + elements + ";;");
      elementBuilder.setUserString(ANGULAR_DIRECTIVES_INDEX_USER_STRING);
      outData.addImplicitElement(elementBuilder.toImplicitElement());
    }
    return outData;
  }

  private static void addPipe(PsiElement expression, @NotNull JSElementIndexingDataImpl outData, String pipe) {
    if (pipe == null) return;
    JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(pipe, expression).setUserString(
      ANGULAR_FILTER_INDEX_USER_STRING);
    outData.addImplicitElement(elementBuilder.toImplicitElement());
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
    return getProperty(decorator instanceof ES6Decorator ? PsiTreeUtil.findChildOfType(decorator, JSCallExpression.class) : decorator, SELECTOR);
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
        info.addType(new JSNamespaceImpl(directiveNamespace, JSContext.INSTANCE, true), true);
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
    if (file == null) return null;
    if (file.getLanguage().is(Angular2HTMLLanguage.INSTANCE)) { // inline template
      return PsiTreeUtil.getParentOfType(InjectedLanguageManager.getInstance(context.getProject()).getInjectionHost(file), JSClass.class);
    }
    if (file.getLanguage().is(AngularJSLanguage.INSTANCE)) { // template file with the same name
      final PsiElement original = CompletionUtil.getOriginalOrSelf(context);
      PsiFile hostFile = FileContextUtil.getContextFile(original != context ? original : context.getContainingFile().getOriginalFile());
      final String name = hostFile != null ? hostFile.getVirtualFile().getNameWithoutExtension() : null;
      final PsiDirectory dir = hostFile != null ? hostFile.getParent() : null;
      final PsiFile directiveFile = dir != null ? dir.findFile(name + ".ts") : null;
      if (directiveFile != null) {
        for (PsiElement element : directiveFile.getChildren()) {
          if (element instanceof JSClass) {
            JSClass clazz = (JSClass)element;
            JSAttributeList list = clazz.getAttributeList();
            for (ES6Decorator decorator : PsiTreeUtil.getChildrenOfTypeAsList(list, ES6Decorator.class)) {
              PsiElement[] decoratorChildren = decorator.getChildren();
              if (decoratorChildren.length > 0 && decoratorChildren[0] instanceof JSCallExpression) {
                JSCallExpression call = (JSCallExpression)decoratorChildren[0];
                if (call.getMethodExpression() instanceof JSReferenceExpression &&
                    isDirective(((JSReferenceExpression)call.getMethodExpression()).getReferenceName())) {
                  return clazz;
                }
              }
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public boolean addTypeFromResolveResult(JSTypeEvaluator evaluator, PsiElement result, boolean hasSomeType) {
    if (!(result instanceof JSImplicitElement) || !AngularJSProcessor.$EVENT.equals(((JSImplicitElement)result).getName())) {
      return false;
    }
    XmlAttribute parent = ObjectUtils.tryCast(result.getParent(), XmlAttribute.class);
    AngularEventHandlerDescriptor descriptor = ObjectUtils.tryCast(parent != null ? parent.getDescriptor() : null,
                                                                   AngularEventHandlerDescriptor.class);
    PsiElement declaration = descriptor != null ? descriptor.getDeclaration() : null;
    JSType type = null;
    if (declaration instanceof JSField) {
      type = ((JSField)declaration).getType();
    } else if (declaration instanceof JSFunction) {
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
    if (customElement instanceof JsonProperty && "selector".equals(((JsonProperty)customElement).getName())) {
      JsonValue value = ((JsonProperty)customElement).getValue();
      if (value instanceof JsonStringLiteral) {
        JSElementIndexingDataImpl data = addImplicitElement(value, null, ((JsonStringLiteral)value).getValue());
        if (data != null && data.getImplicitElements() != null) {
          for (JSImplicitElement element : data.getImplicitElements()) {
            JSImplicitElementImpl.Builder elementBuilder = ((JSImplicitElementImpl)element).toBuilder().setProvider(null);
            JSImplicitElementsIndex.JSElementProxy proxy = new JSImplicitElementsIndex.JSElementProxy(elementBuilder, value.getTextRange().getStartOffset());
            builder.addImplicitElement(element.getName(), proxy);
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
}

package org.angularjs.index;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.ASTNode;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.frameworks.jquery.JQueryCssLanguage;
import com.intellij.lang.javascript.index.FrameworkIndexingHandler;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.literal.JSLiteralImplicitElementCustomProvider;
import com.intellij.lang.javascript.psi.resolve.BaseJSSymbolProcessor;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElementStructure;
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.css.*;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.MultiMap;
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
  public static final String DECORATORS = "adei";
  public static final String DECORATE = "__decorate";

  static {
    JSImplicitElement.ourUserStringsRegistry.registerUserString(DECORATORS);
  }

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

  @Nullable
  @Override
  public JSElementIndexingData processAnyProperty(@NotNull JSProperty property, @Nullable JSElementIndexingData outData) {
    final JSObjectLiteralExpression object = (JSObjectLiteralExpression)property.getParent();
    String name = property.getName();
    if ("args".equals(name)) {
      final JSProperty type = object.findProperty("type");
      if (type != null) {
        final JSExpression value = type.getValue();
        if (value instanceof JSReferenceExpression && isDirective(((JSReferenceExpression)value).getReferenceName())) {
          return addImplicitElement(property, (JSElementIndexingDataImpl)outData, getPropertyName(property, SELECTOR));
        }
      }
    }
    if (name != null && object.getParent() instanceof JSAssignmentExpression) {
      JSDefinitionExpression definition = ((JSAssignmentExpression)object.getParent()).getDefinitionExpression();
      JSExpression value = property.getValue();
      if (definition != null && "propDecorators".equals(definition.getName()) && value instanceof JSArrayLiteralExpression) {
        for (JSExpression expression : ((JSArrayLiteralExpression)value).getExpressions()) {
          if (expression instanceof JSObjectLiteralExpression) {
            JSObjectLiteralExpression decoratorProperties = (JSObjectLiteralExpression)expression;
            JSProperty type = decoratorProperties.findProperty("type");
            String decoratorName = type != null && type.getValue() != null ? type.getValue().getText() : null;
            if (isInterestingDecorator(decoratorName)) {
              JSImplicitElementImpl.Builder builder =
                new JSImplicitElementImpl.Builder(getDecoratedName(property.getName(), decoratorProperties), property).setUserString(DECORATORS)
                  .setTypeString(decoratorName + ";Object");
              if (outData == null) outData = new JSElementIndexingDataImpl();
              outData.addImplicitElement(builder.toImplicitElement());
            }
          }
        }
      }
    }
    return outData;
  }

  private static String getDecoratedName(String name, JSObjectLiteralExpression decorator) {
    JSProperty args = decorator.findProperty("args");
    if (args != null) {
      JSExpression argv = args.getValue();
      if (argv instanceof JSArrayLiteralExpression) {
        JSExpression[] expressions = ((JSArrayLiteralExpression)argv).getExpressions();
        if (expressions[0] instanceof JSLiteralExpression) {
          Object value = ((JSLiteralExpression)expressions[0]).getValue();
          if (value instanceof String) return (String)value;
        }
      }
    }
    return name;
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
      final String prefix = isTemplate(element) && !attributeName.startsWith("[") ? "*" : "";
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
    final PsiElement parent = decorator.getParent();
    if (parent instanceof JSArrayLiteralExpression) {
      final JSCallExpression metadata = PsiTreeUtil.getNextSiblingOfType(decorator, JSCallExpression.class);
      return hasTemplateRef(metadata);
    }
    if (parent instanceof JSObjectLiteralExpression) {
      JSQualifiedName namespace = getCompiledDecoratorNamespace(parent);
      if (namespace == null) return false;
      final JSBlockStatement block = PsiTreeUtil.getParentOfType(parent, JSBlockStatement.class);
      final JSFile file = block == null ? PsiTreeUtil.getParentOfType(parent, JSFile.class) : null;
      final JSSourceElement[] statements = block != null ? block.getStatements() :
                                           file != null ? file.getStatements() :
                                           JSStatement.EMPTY;
      for (JSSourceElement statement : statements) {
        if (statement instanceof JSExpressionStatement) {
          final JSExpression expression = ((JSExpressionStatement)statement).getExpression();
          if (expression instanceof JSAssignmentExpression) {
            final JSDefinitionExpression def = ((JSAssignmentExpression)expression).getDefinitionExpression();
            if (def != null && "ctorParameters".equals(def.getName()) && namespace.equals(def.getJSNamespace().getQualifiedName())) {
              return hasTemplateRef(expression) ||
                     PsiTreeUtil.hasErrorElements(expression) && !DialectDetector.isES6(expression) && hasTemplateRef(PsiTreeUtil.getNextSiblingOfType(statement, JSExpressionStatement.class));
            }
          }
        }
      }
    }
    return false;
  }

  private static boolean hasTemplateRef(@Nullable PsiElement expression) {
    return expression != null && expression.getText().contains(TEMPLATE_REF);
  }

  private static JSQualifiedName getCompiledDecoratorNamespace(PsiElement parent) {
    JSAssignmentExpression assignment = PsiTreeUtil.getParentOfType(parent, JSAssignmentExpression.class, true, JSFunction.class, JSFile.class);
    JSDefinitionExpression definition = assignment != null ? assignment.getDefinitionExpression() : null;
    return definition != null ? definition.getJSNamespace().getQualifiedName() : null;
  }

  @Nullable
  private static String getPropertyName(PsiElement decorator, String name) {
    final JSProperty selector = getProperty(decorator, name);
    final JSExpression value = selector != null ? selector.getValue() : null;
    if (value instanceof JSBinaryExpression) {
      return JSInjectionController.getInjectionText(value);
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
  public void addContextType(BaseJSSymbolProcessor.TypeInfo info, PsiElement context) {
    if (context instanceof JSReferenceExpression && ((JSReferenceExpression)context).getQualifier() == null) {
      final JSQualifiedName directiveNamespace = findDirective(context);
      if (directiveNamespace != null) {
        info.addType(new JSNamespaceImpl(directiveNamespace, JSContext.INSTANCE, true), false);
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
            return (JSClass)element;
          }
        }
      }
    }
    return null;
  }

  @Nullable
  @Override
  public JSLiteralImplicitElementCustomProvider createLiteralImplicitElementCustomProvider() {
    return new JSLiteralImplicitElementCustomProvider() {
      @Override
      public boolean checkIfCandidate(@NotNull ASTNode literalExpression) {
        ASTNode parent = TreeUtil.findParent(literalExpression, JSStubElementTypes.CALL_EXPRESSION);
        LeafElement leaf = parent != null ? TreeUtil.findFirstLeaf(parent) : null;
        return leaf != null && leaf.getText().startsWith(DECORATE);
      }

      @Override
      public void fillIndexingDataForCandidate(@NotNull JSLiteralExpression argument, @NotNull JSElementIndexingData outIndexingData) {
        String name = argument.isQuotedLiteral() ? AngularJSIndexingHandler.unquote(argument) : null;
        if (name == null) return;
        JSCallExpression callExpression = PsiTreeUtil.getParentOfType(argument, JSCallExpression.class);
        if (callExpression == null) return;

        JSExpression first = callExpression.getArguments()[0];
        if (!(first instanceof JSArrayLiteralExpression)) return;
        JSExpression[] expressions = ((JSArrayLiteralExpression)first).getExpressions();
        if (expressions.length != 2) return;

        JSExpression decorator = expressions[0];
        String decoratorName = getCallName(decorator);
        if (!isInterestingDecorator(decoratorName)) return;

        JSExpression metadata = expressions[1];
        String metadataName = getCallName(metadata);
        if (metadataName == null || !metadataName.startsWith("__metadata")) return;
        JSExpression[] meta = ((JSCallExpression)metadata).getArguments();
        if (meta.length != 2) return;

        if (!(meta[0] instanceof JSLiteralExpression)) return;
        String type = AngularJSIndexingHandler.unquote(meta[0]);
        if (!"design:type".equals(type)) return;

        JSImplicitElementImpl.Builder builder =
          new JSImplicitElementImpl.Builder(getDecoratedName(name, decorator), argument).setUserString(DECORATORS)
            .setTypeString(decoratorName + ";" + meta[1].getText());
        outIndexingData.addImplicitElement(builder.toImplicitElement());
      }

      private String getDecoratedName(String name, JSExpression decorator) {
        if (decorator instanceof JSCallExpression) {
          final JSExpression expression = ((JSCallExpression)decorator).getMethodExpression();
          if (expression instanceof JSReferenceExpression) {
            JSExpression[] arguments = ((JSCallExpression)decorator).getArguments();
            if (arguments.length > 0 && arguments[0] instanceof JSLiteralExpression) {
              Object value = ((JSLiteralExpression)arguments[0]).getValue();
              if (value instanceof String) return (String)value;
            }
          }
        }
        return name;
      }

      private String getCallName(JSExpression call) {
        if (call instanceof JSCallExpression) {
          JSExpression expression = ((JSCallExpression)call).getMethodExpression();
          if (expression instanceof JSReferenceExpression) {
            return ((JSReferenceExpression)expression).getReferenceName();
          }
        }
        return null;
      }
    };
  }

  protected boolean isInterestingDecorator(String decoratorName) {
    return "Input".equals(decoratorName) || "Output".equals(decoratorName);
  }

  @Override
  public boolean indexImplicitElement(@NotNull JSImplicitElementStructure element, @Nullable IndexSink sink) {
    if (sink != null && DECORATORS.equals(element.getUserString())) {
      sink.occurrence(AngularDecoratorsIndex.KEY, element.getName());
      sink.occurrence(AngularSymbolIndex.KEY, element.getName());
      return true;
    }
    return false;
  }

  @Override
  public int getVersion() {
    return AngularIndexUtil.BASE_VERSION;
  }
}

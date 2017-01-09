package org.angularjs.index;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.ASTNode;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSStubElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.index.FrameworkIndexingHandler;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.BaseJSSymbolProcessor;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.intellij.util.ObjectUtils;
import org.angularjs.html.Angular2HTMLLanguage;
import org.angularjs.lang.AngularJSLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     }
  }

  @Nullable
  @Override
  public JSElementIndexingData processAnyProperty(@NotNull JSProperty property, @Nullable JSElementIndexingData outData) {
    if ("args".equals(property.getName())) {
      final JSObjectLiteralExpression object = (JSObjectLiteralExpression)property.getParent();
      final JSProperty type = object.findProperty("type");
      if (type != null) {
        final JSExpression value = type.getValue();
        if (value instanceof JSReferenceExpression && isDirective(((JSReferenceExpression)value).getReferenceName())) {
          return addImplicitElement(property, (JSElementIndexingDataImpl)outData, getPropertyName(property, SELECTOR));
        }
      }
    }
    return super.processAnyProperty(property, outData);
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
        return isDirective(referencedName) || isPipe(referencedName);
      }
    }
    return false;
  }

  @Override
  public JSElementIndexingDataImpl processDecorator(ES6Decorator decorator, JSElementIndexingDataImpl outData) {
    final String name = getDecoratorName(decorator);
    if (name != null) {
      if (isDirective(name)) {
        outData = addImplicitElement(decorator, outData, getDecoratorPropertyValue(decorator, SELECTOR));
      }
      if (isPipe(name)) {
        outData = addPipe(decorator, outData, getDecoratorPropertyValue(decorator, NAME));
      }
      // module name should be used
      if (isModule(name)) {
        outData = addImplicitElementToModules(decorator, outData, determineModuleName(decorator));
      }
    }
    return outData;
  }

  @Nullable
  private static String getDecoratorName(@NotNull ES6Decorator decorator) {
    final ASTNode callExpressionNode = getDecoratorCallExpression(decorator);
    if (callExpressionNode == null) return null;
    final ASTNode referenceNode = callExpressionNode.getFirstChildNode();
    if (!expect(referenceNode, JSElementTypes.REFERENCE_EXPRESSION)) return null;
    final ASTNode identifierNode = referenceNode.getFirstChildNode();
    if (!expect(identifierNode, JSTokenTypes.IDENTIFIER)) return null;
    return identifierNode.getText();
  }

  @Nullable
  private static ASTNode getDecoratorCallExpression(@NotNull ES6Decorator decorator) {
    final ASTNode decoratorNode = decorator.getNode();
    final ASTNode decoratorNodeFirstChildNode = decoratorNode.getFirstChildNode();
    if (!expect(decoratorNodeFirstChildNode, JSTokenTypes.AT)) return null;
    final ASTNode decoratorNodeLastChildNode = decoratorNode.getLastChildNode();
    if (!expect(decoratorNodeLastChildNode, JSStubElementTypes.CALL_EXPRESSION)) return null;
    return decoratorNodeLastChildNode;
  }

  @Nullable
  private static String getDecoratorPropertyValue(@NotNull ES6Decorator decorator, @NotNull final String name) {
    final ASTNode callExpressionNode = getDecoratorCallExpression(decorator);
    if (callExpressionNode == null) return null;
    final ASTNode argumentListNode = callExpressionNode.getLastChildNode();
    if (!expect(argumentListNode, JSElementTypes.ARGUMENT_LIST)) return null;
    final ASTNode objectArgument = argumentListNode.findChildByType(JSStubElementTypes.OBJECT_LITERAL_EXPRESSION);
    if (objectArgument == null) return null;
    final ASTNode[] properties = objectArgument.getChildren(TokenSet.create(JSStubElementTypes.PROPERTY));
    for (ASTNode property : properties) {
      final ASTNode nameNode = property.getFirstChildNode();
      if (expect(nameNode, JSTokenTypes.IDENTIFIER) && name.equals(nameNode.getText())) {
        ASTNode node = nameNode.getTreeNext();
        while (node != null && (JSTokenTypes.WHITE_SPACE.equals(node.getElementType()) || JSTokenTypes.COLON.equals(node.getElementType()))) {
          node = node.getTreeNext();
        }
        if (expect(node, JSStubElementTypes.LITERAL_EXPRESSION)) {
          if (expect(node.getFirstChildNode(), JSTokenTypes.STRING_LITERAL)) return StringUtil.unquoteString(node.getFirstChildNode().getText());
        } else if (expect(node, JSTokenTypes.STRING_LITERAL)) return StringUtil.unquoteString(node.getText());
      }
    }
    return null;
  }

  private static boolean expect(@Nullable ASTNode node, @NotNull IElementType type) {
    return node != null && type.equals(node.getElementType());
  }

  private static String determineModuleName(@NotNull ES6Decorator decorator) {
    final PsiElement owner = decorator.getOwner();
    if (owner instanceof JSClass) return ((JSClass)owner).getName();
    return null;
  }

  private static void iterateSelectorNames(@Nullable final String selector, @NotNull final Consumer<String> consumer) {
    if (selector == null) return;
    final String[] names = selector.split("\\s*,\\s*");
    for (String selectorName : names) {
      final int not = selectorName.indexOf(":");
      if (not >= 0) {
        selectorName = selectorName.substring(0, not);
      }
      if (!StringUtil.isEmpty(selectorName)) {
        consumer.consume(selectorName);
      }
    }
  }

  private static JSElementIndexingDataImpl addImplicitElementToModules(PsiElement decorator,
                                                                       JSElementIndexingDataImpl outData,
                                                                       String selector) {
    if (selector == null) return outData;
    if (outData == null) outData = new JSElementIndexingDataImpl();
    JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(selector, decorator)
      .setUserString(AngularJSIndexingHandler.ANGULAR_MODULE_INDEX_USER_STRING);
    outData.addImplicitElement(elementBuilder.toImplicitElement());
    return outData;
  }

  private static JSElementIndexingDataImpl addImplicitElement(PsiElement decorator,
                                                              JSElementIndexingDataImpl outData,
                                                              String selector) {
    if (selector == null) return outData;

    final Ref<JSElementIndexingDataImpl> ref = new Ref<>(outData);
    final Set<String> added = new HashSet<>();
    iterateSelectorNames(selector, selectorName -> {
      final int start = selectorName.indexOf('[');
      final int end = selectorName.indexOf(']');
      if (start == 0 && end > 0 || start < 0 && end < 0) {
        if (ref.isNull()) ref.set(new JSElementIndexingDataImpl());
        JSImplicitElementImpl.Builder elementBuilder;
        for (String attr : StringUtil.split(selectorName, "]", false)) {
          if (added.contains(attr)) continue;
          final String restrict = selectorName.startsWith("[") ? "A" : "E";
          elementBuilder = new JSImplicitElementImpl.Builder(attr, decorator)
            .setType(JSImplicitElement.Type.Class).setTypeString(restrict + ";template;;");
          elementBuilder.setUserString(ANGULAR_DIRECTIVES_INDEX_USER_STRING);
          ref.get().addImplicitElement(elementBuilder.toImplicitElement());
          added.add(attr);
        }
        if (end > start) {
          final String attributeName = selectorName.substring(1, end);
          final String prefix = isTemplate(decorator) ? "*" : "";
          final String attr = prefix + attributeName;
          if (added.contains(attr)) return;
          elementBuilder = new JSImplicitElementImpl.Builder(attr, decorator)
            .setType(JSImplicitElement.Type.Class).setTypeString("A;;;");
          elementBuilder.setUserString(ANGULAR_DIRECTIVES_INDEX_USER_STRING);
          ref.get().addImplicitElement(elementBuilder.toImplicitElement());
          added.add(attr);
        }
      }
    });
    return ref.get();
  }

  private static JSElementIndexingDataImpl addPipe(PsiElement expression, JSElementIndexingDataImpl outData, String pipe) {
    if (pipe == null) return outData;
    if (outData == null) outData = new JSElementIndexingDataImpl();
    JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(pipe, expression).setUserString(
      ANGULAR_FILTER_INDEX_USER_STRING);
    outData.addImplicitElement(elementBuilder.toImplicitElement());
    return outData;
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
      return metadata != null && metadata.getText().contains(TEMPLATE_REF);
    }
    if (parent instanceof JSObjectLiteralExpression) {
      final JSBlockStatement block = PsiTreeUtil.getParentOfType(parent, JSBlockStatement.class);
      final JSStatement[] statements = block != null ? block.getStatements() : JSStatement.EMPTY;
      for (JSStatement statement : statements) {
        if (statement instanceof JSExpressionStatement) {
          final JSExpression expression = ((JSExpressionStatement)statement).getExpression();
          if (expression instanceof JSAssignmentExpression) {
            final JSDefinitionExpression def = ((JSAssignmentExpression)expression).getDefinitionExpression();
            if (def != null && "ctorParameters".equals(def.getName())) {
              return expression.getText().contains(TEMPLATE_REF);
            }
          }
        }
      }
    }
    return false;
  }

  @Nullable
  private static String getPropertyName(PsiElement decorator, String name) {
    final JSProperty selector = getProperty(decorator, name);
    final JSExpression value = selector != null ? selector.getValue() : null;
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

  @Override
  public int getVersion() {
    return AngularIndexUtil.BASE_VERSION;
  }
}

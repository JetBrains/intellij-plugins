package org.angularjs.index;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.ASTNode;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.index.FrameworkIndexingHandler;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES7Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.BaseJSSymbolProcessor;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import org.angularjs.html.Angular2HTMLLanguage;
import org.angularjs.lang.AngularJSLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
  public JSElementIndexingDataImpl processDecorator(ES7Decorator decorator, JSElementIndexingDataImpl outData) {
    final String name = decorator.getName();
    if (isDirective(name)) {
      return addImplicitElement(decorator, outData, getPropertyName(decorator, SELECTOR));
    }
    if (isPipe(name)) {
      return addPipe(decorator, outData, getPropertyName(decorator, NAME));
    }
    return outData;
  }

  private static JSElementIndexingDataImpl addImplicitElement(PsiElement decorator,
                                                              JSElementIndexingDataImpl outData,
                                                              String selector) {
    if (selector == null) return outData;

    final Set<String> added = new HashSet<>();
    final String[] names = selector.split(",");
    for (String selectorName : names) {
      final int not = selectorName.indexOf(":");
      if (not >= 0) {
        selectorName = selectorName.substring(0, not);
      }
      if (!StringUtil.isEmpty(selectorName)) {
        final int start = selectorName.indexOf('[');
        final int end = selectorName.indexOf(']');
        if (start == 0 && end > 0 || start < 0 && end < 0) {
          if (outData == null) outData = new JSElementIndexingDataImpl();
          JSImplicitElementImpl.Builder elementBuilder;
          for (String attr : StringUtil.split(selectorName, "]", false)) {
            if (added.contains(attr)) continue;
            final String restrict = selectorName.startsWith("[") ? "A" : "E";
            elementBuilder = new JSImplicitElementImpl.Builder(attr, decorator)
              .setType(JSImplicitElement.Type.Class).setTypeString(restrict + ";template;;");
            elementBuilder.setUserString("adi");
            outData.addImplicitElement(elementBuilder.toImplicitElement());
            added.add(attr);
          }
          if (end > start) {
            final String attributeName = selectorName.substring(1, end);
            final String prefix = isTemplate(decorator) ? "*" : "";
            final String attr = prefix + attributeName;
            if (added.contains(attr)) continue;
            elementBuilder = new JSImplicitElementImpl.Builder(attr, decorator)
              .setType(JSImplicitElement.Type.Class).setTypeString("A;;;");
            elementBuilder.setUserString("adi");
            outData.addImplicitElement(elementBuilder.toImplicitElement());
            added.add(attr);
          }
        }
      }
    }
    return outData;
  }

  private static JSElementIndexingDataImpl addPipe(PsiElement expression, JSElementIndexingDataImpl outData, String pipe) {
    if (pipe == null) return outData;
    JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(pipe, expression).setUserString("afi");
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
    return getProperty(decorator, SELECTOR);
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

  public static boolean isDirective(String name) {
    return "Directive".equals(name) || "DirectiveAnnotation".equals(name) ||
           "Component".equals(name) || "ComponentAnnotation".equals(name);
  }

  private static boolean isPipe(String name) {
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

  private static JSQualifiedName findDirective(PsiElement context) {
    final PsiFile file = context.getContainingFile();
    if (file.getLanguage().is(Angular2HTMLLanguage.INSTANCE)) { // inline template
      final PsiLanguageInjectionHost host = InjectedLanguageManager.getInstance(context.getProject()).getInjectionHost(file);
      final JSClass clazz = PsiTreeUtil.getParentOfType(host, JSClass.class);
      if (clazz != null) {
        return JSQualifiedNameImpl.buildProvidedNamespace(clazz);
      }
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
            return JSQualifiedNameImpl.buildProvidedNamespace((JSClass)element);
          }
        }
      }
    }

    return null;
  }

  @Override
  public boolean canProcessCustomElement(@NotNull PsiElement element) {
    return element instanceof HtmlTag;
  }

  @Override
  public int getVersion() {
    return AngularIndexUtil.BASE_VERSION;
  }
}

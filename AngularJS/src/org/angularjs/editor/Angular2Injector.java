package org.angularjs.editor;

import com.intellij.lang.Language;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.javascript.inject.JSFormattableInjectionUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSLiteralExpressionImpl;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ObjectUtils;
import org.angularjs.codeInsight.attributes.AngularAttributesRegistry;
import org.angularjs.html.Angular2HTMLLanguage;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularJS2IndexingHandler;
import org.angularjs.lang.AngularJSLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class Angular2Injector implements MultiHostInjector {
  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    if (context.getLanguage() == XMLLanguage.INSTANCE) return;
    final Project project = context.getProject();
    if (!AngularIndexUtil.hasAngularJS2(project)) return;

    final PsiElement parent = context.getParent();
    if (context instanceof JSLiteralExpressionImpl && ((JSLiteralExpressionImpl)context).isQuotedLiteral()) {
      if (injectIntoDirectiveProperty(registrar, context, parent, "template", Angular2HTMLLanguage.INSTANCE)) return;
      if (injectIntoEmbeddedLiteral(registrar, context, parent)) return;
      if (parent instanceof JSArrayLiteralExpression) {
        final JSProperty property = ObjectUtils.tryCast(parent.getParent(), JSProperty.class);
        if (injectIntoDirectiveProperty(registrar, context, property, "styles", CSSLanguage.INSTANCE)) return;
      }
      if (parent instanceof JSProperty && parent.getParent() instanceof JSObjectLiteralExpression) {
        String name = ((JSProperty)parent).getName();
        if (name != null && isInjectableAttribute(project, context.getTextLength(), name)) {
          injectIntoDirectiveProperty(registrar, context, parent.getParent().getParent(), "host", AngularJSLanguage.INSTANCE);
        }
      }
      return;
    }
    if (context instanceof XmlAttributeValueImpl && parent instanceof XmlAttribute) {
      final int length = context.getTextLength();
      final String name = ((XmlAttribute)parent).getName();
      if (isInjectableAttribute(project, length, name)) {
        registrar.startInjecting(AngularJSLanguage.INSTANCE).
          addPlace(null, null, (PsiLanguageInjectionHost)context, ElementManipulators.getValueTextRange(context)).
          doneInjecting();
      }
    }
  }

  private boolean injectIntoEmbeddedLiteral(MultiHostRegistrar registrar, PsiElement context, PsiElement parent) {
    if (parent instanceof JSEmbeddedContent) {
      final XmlAttribute attribute = PsiTreeUtil.getParentOfType(parent, XmlAttribute.class);
      if (attribute != null && isInjectableAttribute(context.getProject(), context.getTextLength(), attribute.getName())) {
        final TextRange range = ElementManipulators.getValueTextRange(context);
        registrar.startInjecting(AngularJSLanguage.INSTANCE).addPlace(null, null, (PsiLanguageInjectionHost)context, range).doneInjecting();
        return true;
      }
    }
    return false;
  }

  protected boolean isInjectableAttribute(Project project, int valueLength, String name) {
    return (AngularAttributesRegistry.isEventAttribute(name, project) ||
            AngularAttributesRegistry.isBindingAttribute(name, project) ||
            AngularAttributesRegistry.isTemplateAttribute(name, project)) &&
        valueLength > 0;
  }

  protected boolean injectIntoDirectiveProperty(@NotNull MultiHostRegistrar registrar,
                                                @NotNull PsiElement context,
                                                @Nullable PsiElement parent,
                                                @NotNull String name,
                                                @NotNull Language language) {
    if (parent instanceof JSProperty && name.equals(((JSProperty)parent).getName())) {
      final JSCallExpression callExpression = PsiTreeUtil.getParentOfType(parent, JSCallExpression.class);
      final JSExpression expression = callExpression != null ? callExpression.getMethodExpression() : null;
      if (expression instanceof JSReferenceExpression) {
        final String command = ((JSReferenceExpression)expression).getReferenceName();
        if (!AngularJS2IndexingHandler.isDirective(command) && !"View".equals(command)) return true;

        final TextRange range = ElementManipulators.getValueTextRange(context);
        registrar.startInjecting(language).addPlace(null, null, (PsiLanguageInjectionHost)context, range).doneInjecting();
        JSFormattableInjectionUtil.setReformattableInjection(context, language);
      }
    }
    return false;
  }

  @NotNull
  @Override
  public List<Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(JSLiteralExpression.class, XmlAttributeValue.class);
  }
}

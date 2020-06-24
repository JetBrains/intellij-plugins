// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight.refs;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.angular2.lang.Angular2LangUtil;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularJSIndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.angularjs.codeInsight.DirectiveUtil.normalizeAttributeName;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSReferencesContributor extends PsiReferenceContributor {
  private static final PsiElementPattern.Capture<JSLiteralExpression> TEMPLATE_PATTERN = literalInProperty("templateUrl");
  private static final PsiElementPattern.Capture<JSLiteralExpression> CONTROLLER_PATTERN = literalInProperty("controller");
  public static final PsiElementPattern.Capture<PsiElement> UI_VIEW_PATTERN = uiViewPattern();
  public static final PsiElementPattern.Capture<XmlAttributeValue> UI_VIEW_REF = xmlAttributePattern("uiSref");
  public static final PsiElementPattern.Capture<XmlAttributeValue> NG_APP_REF = xmlAttributePattern("ngApp");
  public static final PsiElementPattern.Capture<JSLiteralExpression> MODULE_PATTERN = modulePattern();
  public static final PsiElementPattern.Capture<JSLiteralExpression> MODULE_DEPENDENCY_PATTERN = moduleDependencyPattern();

  private static final PsiElementPattern.Capture<JSLiteralExpression> NG_INCLUDE_PATTERN =
    PlatformPatterns.psiElement(JSLiteralExpression.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        if (element instanceof JSLiteralExpression) {
          final JSLiteralExpression literal = (JSLiteralExpression)element;
          if (literal.isQuotedLiteral()) {
            final PsiElement original = CompletionUtil.getOriginalOrSelf(literal);
            final PsiLanguageInjectionHost host = InjectedLanguageUtil.findInjectionHost(original);
            if (host instanceof XmlAttributeValue) {
              final PsiElement parent = host.getParent();
              return parent instanceof XmlAttribute
                     && "ngInclude".equals(normalizeAttributeName(((XmlAttribute)parent).getName()));
            }
          }
        }
        return false;
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));


  public static final PsiElementPattern.Capture<JSParameter> DI_PATTERN =
    PlatformPatterns.psiElement(JSParameter.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        return AngularJSIndexingHandler.isInjectable(context);
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));


  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    final AngularJSTemplateReferencesProvider templateProvider = new AngularJSTemplateReferencesProvider();
    registrar.registerReferenceProvider(TEMPLATE_PATTERN, templateProvider);
    registrar.registerReferenceProvider(NG_INCLUDE_PATTERN, templateProvider);
    registrar.registerReferenceProvider(CONTROLLER_PATTERN, new AngularJSControllerReferencesProvider());
    registrar.registerReferenceProvider(DI_PATTERN, new AngularJSDIReferencesProvider());
    registrar.registerReferenceProvider(UI_VIEW_PATTERN, new AngularJSUiRouterViewReferencesProvider());
    registrar.registerReferenceProvider(UI_VIEW_REF, new AngularJSUiRouterStatesReferencesProvider());
    registrar.registerReferenceProvider(NG_APP_REF, new AngularJSNgAppReferencesProvider());
    registrar.registerReferenceProvider(MODULE_PATTERN, new AngularJSModuleReferencesProvider());
  }

  private static PsiElementPattern.Capture<JSLiteralExpression> modulePattern() {
    return PlatformPatterns.psiElement(JSLiteralExpression.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        if (element instanceof JSLiteralExpression) {
          final PsiElement parent = ((PsiElement)element).getParent();
          if (parent instanceof JSArgumentList && parent.getParent() instanceof JSCallExpression
              && ((JSArgumentList)parent).getArguments().length == 1) {
            if (PsiTreeUtil.isAncestor(((JSArgumentList)parent).getArguments()[0], (PsiElement)element, false)) {
              final JSExpression methodExpression = ((JSCallExpression)parent.getParent()).getMethodExpression();
              if (looksLikeAngularModuleReference(methodExpression)) {
                return true;
              }
            }
          }
        }
        return false;
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));
  }

  private static PsiElementPattern.Capture<JSLiteralExpression> moduleDependencyPattern() {
    return PlatformPatterns.psiElement(JSLiteralExpression.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        if (element instanceof JSLiteralExpression) {
          PsiElement parent = ((PsiElement)element).getParent();
          if (!(parent instanceof JSArrayLiteralExpression)) return false;
          parent = parent.getParent();
          if (parent instanceof JSArgumentList && parent.getParent() instanceof JSCallExpression
              && ((JSArgumentList)parent).getArguments().length > 1) {
            if (PsiTreeUtil.isAncestor(((JSArgumentList)parent).getArguments()[1], (PsiElement)element, false) &&
                ((JSArgumentList)parent).getArguments()[1] instanceof JSArrayLiteralExpression) {
              final JSExpression methodExpression = ((JSCallExpression)parent.getParent()).getMethodExpression();
              if (looksLikeAngularModuleReference(methodExpression)) return true;
            }
          }
        }
        return false;
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));
  }

  static boolean looksLikeAngularModuleReference(JSExpression methodExpression) {
    if (methodExpression instanceof JSReferenceExpression && ((JSReferenceExpression)methodExpression).getQualifier() != null &&
        AngularJSIndexingHandler.MODULE.equals(((JSReferenceExpression)methodExpression).getReferenceName())) {
      return true;
    }
    return false;
  }

  private static PsiElementPattern.Capture<JSLiteralExpression> literalInProperty(final String propertyName) {
    return PlatformPatterns.psiElement(JSLiteralExpression.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        if (element instanceof JSLiteralExpression) {
          final JSLiteralExpression literal = (JSLiteralExpression)element;
          if (literal.isQuotedLiteral()) {
            final PsiElement parent = literal.getParent();
            if (parent instanceof JSProperty && propertyName.equals(((JSProperty)parent).getName())) {
              return AngularIndexUtil.hasAngularJS(literal.getProject())
                     || Angular2LangUtil.isAngular2Context(parent);
            }
          }
        }
        return false;
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));
  }

  private static PsiElementPattern.Capture<PsiElement> uiViewPattern() {
    return PlatformPatterns.psiElement(PsiElement.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        if (!(element instanceof PsiElement)) return false;
        if (element instanceof JSLiteralExpression ||
            element instanceof LeafPsiElement && ((LeafPsiElement)element).getNode().getElementType() == JSTokenTypes.STRING_LITERAL) {
          if (!(((PsiElement)element).getParent() instanceof JSProperty)) return false;
          // started typing property, variant
          PsiElement current = moveUpChain((PsiElement)element,
                                           JSLiteralExpression.class,
                                           JSReferenceExpression.class,
                                           JSProperty.class);
          if (!(current instanceof JSProperty) || !acceptablePropertyValue((JSProperty)current)) return false;
          current = current.getParent();
          if (current != null && checkParentViewsObject(current)) return AngularIndexUtil.hasAngularJS(current.getProject());
        }
        return false;
      }

      private boolean acceptablePropertyValue(JSProperty element) {
        return element.getNameIdentifier() != null && StringUtil.isQuotedString(element.getNameIdentifier().getText()) &&
               (element.getValue() instanceof JSObjectLiteralExpression ||
                element.getValue() instanceof JSReferenceExpression || element.getValue() == null);
      }

      private PsiElement moveUpChain(final @Nullable PsiElement element, final Class<? extends PsiElement> @NotNull ... clazz) {
        PsiElement current = element;
        for (Class<? extends PsiElement> aClass : clazz) {
          current = current != null && aClass.isInstance(current.getParent()) ? current.getParent() : current;
        }
        return current;
      }

      private boolean checkParentViewsObject(final PsiElement mustBeObject) {
        if (mustBeObject instanceof JSObjectLiteralExpression) {
          final PsiElement viewsProperty = mustBeObject.getParent();
          if (viewsProperty instanceof JSProperty && "views".equals(((JSProperty)viewsProperty).getName())) {
            // by now will not go further todo other cases
            return true;
          }
        }
        return false;
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));
  }

  private static PsiElementPattern.Capture<XmlAttributeValue> xmlAttributePattern(final @NotNull String directiveName) {
    return PlatformPatterns.psiElement(XmlAttributeValue.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        final XmlAttributeValue attributeValue = (XmlAttributeValue)element;
        final PsiElement parent = attributeValue.getParent();
        if (parent instanceof XmlAttribute && directiveName.equals(normalizeAttributeName(((XmlAttribute)parent).getName()))) {
          return AngularIndexUtil.hasAngularJS(attributeValue.getProject());
        }
        return false;
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));
  }
}

package org.angularjs.codeInsight.refs;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularJSIndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSReferencesContributor extends PsiReferenceContributor {
  private static final PsiElementPattern.Capture<JSLiteralExpression> TEMPLATE_PATTERN = literalInProperty("templateUrl");
  private static final PsiElementPattern.Capture<JSLiteralExpression> CONTROLLER_PATTERN = literalInProperty("controller");
  public static final PsiElementPattern.Capture<PsiElement> UI_VIEW_PATTERN = uiViewPattern();
  private static final PsiElementPattern.Capture<XmlAttributeValue> UI_VIEW_REF = xmlAttributePattern("ui-sref");
  private static final PsiElementPattern.Capture<XmlAttributeValue> NG_APP_REF = xmlAttributePattern("ng-app");

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
              return parent instanceof XmlAttribute && "ng-include".equals(((XmlAttribute)parent).getName());
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
  private static final PsiElementPattern.Capture<JSLiteralExpression> STYLE_PATTERN = PlatformPatterns.psiElement(JSLiteralExpression.class).and(new FilterPattern(new ElementFilter() {
    @Override
    public boolean isAcceptable(Object element, @Nullable PsiElement context) {
      if (element instanceof JSLiteralExpression) {
        final JSLiteralExpression literal = (JSLiteralExpression)element;
        if (literal.isQuotedLiteral()) {
          if ((literal.getParent() instanceof JSArrayLiteralExpression)) {
            final JSProperty property = ObjectUtils.tryCast(literal.getParent().getParent(), JSProperty.class);
            if (property != null && "styleUrls".equals((property).getName())) {
              return AngularIndexUtil.hasAngularJS(literal.getProject());
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

  public static final PsiElementPattern.Capture<JSParameter> DI_PATTERN = PlatformPatterns.psiElement(JSParameter.class).and(new FilterPattern(new ElementFilter() {
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
    registrar.registerReferenceProvider(STYLE_PATTERN, new PsiReferenceProvider() {
      @NotNull
      @Override
      public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        return new AngularJSTemplateReferencesProvider.Angular2SoftFileReferenceSet(element).getAllReferences();
      }
    });
    registrar.registerReferenceProvider(CONTROLLER_PATTERN, new AngularJSControllerReferencesProvider());
    registrar.registerReferenceProvider(DI_PATTERN, new AngularJSDIReferencesProvider());
    registrar.registerReferenceProvider(UI_VIEW_PATTERN, new AngularJSUiRouterViewReferencesProvider());
    registrar.registerReferenceProvider(UI_VIEW_REF, new AngularJSUiRouterStatesReferencesProvider());
    registrar.registerReferenceProvider(NG_APP_REF, new AngularJSNgAppReferencesProvider());
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
              return AngularIndexUtil.hasAngularJS(literal.getProject());
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
        if (element instanceof JSProperty) {
          final JSProperty property = (JSProperty)element;
          if (checkParentViewsObject(property.getParent())) return AngularIndexUtil.hasAngularJS(property.getProject());
        } else if (element instanceof JSLiteralExpression ||
                   element instanceof LeafPsiElement && ((LeafPsiElement)element).getNode().getElementType() == JSTokenTypes.STRING_LITERAL) {
          // started typing property, variant
          final PsiElement current = moveUpChain((PsiElement) element,
                                                 JSLiteralExpression.class,
                                                 JSReferenceExpression.class,
                                                 JSProperty.class,
                                                 JSObjectLiteralExpression.class);
          if (current != null && checkParentViewsObject(current)) return AngularIndexUtil.hasAngularJS(current.getProject());
        }
        return false;
      }

      private PsiElement moveUpChain(@Nullable final PsiElement element, @NotNull final Class<? extends PsiElement>... clazz) {
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

  private static PsiElementPattern.Capture<XmlAttributeValue> xmlAttributePattern(@NotNull final String attributeName) {
    return PlatformPatterns.psiElement(XmlAttributeValue.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        final XmlAttributeValue attributeValue = (XmlAttributeValue)element;
        final PsiElement parent = attributeValue.getParent();
        if (parent instanceof XmlAttribute && attributeName.equals(((XmlAttribute)parent).getName())) {
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

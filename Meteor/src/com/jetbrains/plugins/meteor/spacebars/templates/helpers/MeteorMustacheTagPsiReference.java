package com.jetbrains.plugins.meteor.spacebars.templates.helpers;

import com.dmarcotte.handlebars.psi.impl.HbPsiElementImpl;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.plugins.meteor.settings.MeteorSettings;
import com.jetbrains.plugins.meteor.spacebars.SpacebarsUtils;
import com.jetbrains.plugins.meteor.spacebars.templates.MeteorTemplateIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

import static com.jetbrains.plugins.meteor.spacebars.templates.MeteorHelpersFrameworkIndexingHandler.*;

/**
 * Mustache template: {{helperName}}
 */
public class MeteorMustacheTagPsiReference extends PsiPolyVariantReferenceBase<HbPsiElementImpl> {
  public static final String BODY_DEFAULT_TEMPLATE = "body";

  protected final String myName;

  MeteorMustacheTagPsiReference(HbPsiElementImpl item, String name) {
    super(item, ElementManipulators.getValueTextRange(item), true);
    myName = name;
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
    if (element instanceof JSDefinitionExpression) {
      JSExpression expression = ((JSDefinitionExpression)element).getExpression();
      if (expression instanceof JSReferenceExpression) {
        if (StringUtil.equals(((JSReferenceExpression)expression).getReferenceName(), myName)) {
          PsiElement resolve = resolve();
          if (resolve instanceof JSQualifiedNamedElement) {
            JSExpression qualifier = ((JSReferenceExpression)expression).getQualifier();
            if (qualifier != null) {
              return StringUtil.equals(expression.getText(), ((JSQualifiedNamedElement)resolve).getQualifiedName());
            }
          }
        }
      }
    }
    return super.isReferenceTo(element);
  }

  public static @Nullable String getTemplateName(PsiElement myElement) {
    if (!myElement.isValid()) return null;

    PsiElement parent = getTagForPlace(myElement);
    if (parent == null) return null;

    XmlTag templateTag = (XmlTag)PsiTreeUtil.findFirstParent(parent, element -> element instanceof XmlTag && (
      ((XmlTag)element).getName().equals(MeteorTemplateIndex.TEMPLATE_TAG) ||
      ((XmlTag)element).getName().equals(BODY_DEFAULT_TEMPLATE)));
    if (templateTag == null) return null;

    if (templateTag.getName().equals(BODY_DEFAULT_TEMPLATE)) return BODY_DEFAULT_TEMPLATE;

    XmlAttribute name = templateTag.getAttribute(MeteorTemplateIndex.NAME_ATTRIBUTE);
    if (name == null) return null;

    return name.getValue();
  }

  public static @Nullable PsiElement getTagForPlace(PsiElement myElement) {
    PsiFile file = myElement.getContainingFile();
    if (file == null) return null;
    FileViewProvider provider = file.getViewProvider();
    if (!(provider instanceof TemplateLanguageFileViewProvider templateLanguageFileViewProvider)) {
      return null;
    }
    PsiElement elementAsHtml = templateLanguageFileViewProvider.findElementAt(myElement.getTextOffset(),
                                                                              templateLanguageFileViewProvider.getTemplateDataLanguage());
    if (elementAsHtml == null) return null;

    PsiElement parent = elementAsHtml.getParent();
    if (parent == null) return null;
    return parent;
  }

  /**
   * Template.registerHelper('helper', function() {})
   */
  private static boolean isGlobalHelperQualifier(@Nullable JSPsiElementBase navigationItem) {
    if (navigationItem == null || navigationItem.getQualifiedName() == null) return false;

    return navigationItem.getQualifiedName().startsWith(NAMESPACE_GLOBAL_HELPERS + ".");
  }

  /**
   * Template.hello.helper = ...
   * or
   * Template.hello.helpers({helper:function() {}})
   */
  private static boolean isTemplateQualifier(@Nullable JSPsiElementBase navigationItem, @NotNull String templateName) {
    if (navigationItem == null || navigationItem.getQualifiedName() == null) return false;

    String qualifiedName = navigationItem.getQualifiedName();
    return qualifiedName.startsWith(METEOR_TEMPLATE_JS_TYPE + "." + templateName + ".") ||
           qualifiedName.startsWith(NAMESPACE_TEMPLATE_HELPERS + "." + templateName + ".");
  }


  protected final PsiElement handleElementRename(String newElementName, boolean partial) throws IncorrectOperationException {
    HbPsiElementImpl newElement = SpacebarsUtils.createMustacheTag(myElement.getProject(), newElementName, partial);
    if (null == newElement) {
      throw new IncorrectOperationException("Incorrect name");
    }
    return getElement().replace(newElement);
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    return handleElementRename(newElementName, false);
  }

  @Override
  public final ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
    return multiResolve(incompleteCode, MeteorSettings.getInstance().isWeakSearch());
  }

  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode, boolean includeWeak) {
    if (incompleteCode) return ResolveResult.EMPTY_ARRAY;

    Collection<JSPsiElementBase> allPossibleHelpers =
      JSClassResolver.getInstance().findElementsByNameIncludingImplicit(myName, ProjectScope.getAllScope(getElement().getProject()));



    if (allPossibleHelpers.isEmpty()) return ResolveResult.EMPTY_ARRAY;

    final String templateName = getTemplateName(myElement);
    if (templateName != null) {
      List<JSPsiElementBase> items = ContainerUtil.findAll(allPossibleHelpers, item -> isTemplateQualifier(item, templateName));
      if (!items.isEmpty()) return convertToResolveResult(items);
    }

    List<JSPsiElementBase> items = ContainerUtil.findAll(allPossibleHelpers, item -> isGlobalHelperQualifier(item));

    if (!items.isEmpty()) {
      return convertToResolveResult(items);
    }

    if (includeWeak) {
      return convertToResolveResult(allPossibleHelpers);
    }

    return ResolveResult.EMPTY_ARRAY;
  }

  public ResolveResult @NotNull [] convertToResolveResult(Collection<JSPsiElementBase> items) {
    return ContainerUtil.map2Array(items, ResolveResult.class, item -> new JSResolveResult(item));
  }
}

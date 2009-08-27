package com.intellij.tapestry.intellij.lang.reference;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.XmlNamedElementPattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.java.IJavaMethod;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.model.presentation.Page;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ResolvedValue;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverChain;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.java.IntellijJavaField;
import com.intellij.tapestry.intellij.core.java.IntellijJavaMethod;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.tapestry.psi.TmlFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TapestryReferenceContributor extends PsiReferenceContributor {

  private final PatternCondition<XmlElement> tapestryFileCondition = new PatternCondition<XmlElement>("tapestryFileCondition") {
    public boolean accepts(@NotNull XmlElement element, final ProcessingContext context) {
      return element.getContainingFile() instanceof TmlFile;
    }
  };
  private static final Key<XmlTag> TAG_KEY = Key.create("TAG_KEY");

  public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {
    registerTypeAttrValueReferenceProvider(registrar);
    registerIdAttrValueReferenceProvider(registrar);
    registerAttrValueReferenceProvider(registrar);
  }

  private void registerTypeAttrValueReferenceProvider(PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(
        XmlPatterns.xmlAttribute("type").withNamespace(TapestryConstants.TEMPLATE_NAMESPACE).with(tapestryFileCondition),
        new PsiReferenceProvider() {
          @NotNull
          public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
            if (!(element instanceof XmlAttribute)) return PsiReference.EMPTY_ARRAY;
            XmlAttribute typeAttr = (XmlAttribute)element;
            return getReferenceToComponentClass(typeAttr, getValueTextRange(typeAttr));
          }
        });
  }

  private void registerIdAttrValueReferenceProvider(PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(
        XmlPatterns.xmlAttribute("id").withNamespace(TapestryConstants.TEMPLATE_NAMESPACE).with(tapestryFileCondition),
        new PsiReferenceProvider() {
          @NotNull
          public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
            if (!(element instanceof XmlAttribute)) return PsiReference.EMPTY_ARRAY;

            XmlAttribute idAttr = (XmlAttribute)element;
            return getReferenceToEmbeddedComponent(idAttr, getValueTextRange(idAttr));
          }
        });
  }

  private void registerAttrValueReferenceProvider(PsiReferenceRegistrar registrar) {
    final PatternCondition<XmlTag> tapestryTagCondition = new PatternCondition<XmlTag>("tapestryTagCondition") {
      public boolean accepts(@NotNull XmlTag tag, final ProcessingContext context) {
        return tag.getContainingFile() instanceof TmlFile && TapestryUtils.getTypeOfTag(tag) != null;
      }
    };

    final XmlNamedElementPattern.XmlAttributePattern tapestryAttributePattern =
        XmlPatterns.xmlAttribute().withParent(XmlPatterns.xmlTag().with(tapestryTagCondition).save(TAG_KEY));
    registrar.registerReferenceProvider(tapestryAttributePattern, new PsiReferenceProvider() {
      @NotNull
      public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
        if (!(element instanceof XmlAttribute)) return PsiReference.EMPTY_ARRAY;
        XmlAttribute attr = (XmlAttribute)element;
        XmlTag tag = context.get(TAG_KEY);

        Component component = TapestryUtils.getTypeOfTag(tag);
        if (component == null) return PsiReference.EMPTY_ARRAY;

        final String localName = attr.getLocalName();
        TapestryParameter parameter = component.getParameters().get(localName);
        if (parameter == null) return PsiReference.EMPTY_ARRAY;

        return localName.equals("page")
               ? getReferenceToPage(component, attr)
               : getAttrValueReference(attr, component.getProject(), parameter);
      }
    });
  }

  private PsiReference[] getAttrValueReference(XmlAttribute attr, TapestryProject project, TapestryParameter parameter) {
    final PresentationLibraryElement element = project.findElementByTemplate(attr.getContainingFile());
    if (element == null) return PsiReference.EMPTY_ARRAY;
    IntellijJavaClassType elementClass = (IntellijJavaClassType)element.getElementClass();

    ResolvedValue resolvedValue;
    try {
      resolvedValue = ValueResolverChain.getInstance().resolve(project, elementClass, attr.getValue(), parameter.getDefaultPrefix());
    }
    catch (Exception ex) {
      return PsiReference.EMPTY_ARRAY;
    }

    if (resolvedValue == null) return PsiReference.EMPTY_ARRAY;

    if (resolvedValue.getCodeBind() instanceof IJavaMethod) {
      return new PsiReference[]{
          new PsiAttributeValueReference(attr.getValueElement(), ((IntellijJavaMethod)resolvedValue.getCodeBind()).getPsiMethod())};
    }

    if (resolvedValue.getCodeBind() instanceof IJavaField) {
      return new PsiReference[]{
          new PsiAttributeValueReference(attr.getValueElement(), ((IntellijJavaField)resolvedValue.getCodeBind()).getPsiField())};
    }
    return PsiReference.EMPTY_ARRAY;
  }

  @Nullable
  private TextRange getValueTextRange(@NotNull XmlAttribute attr) {
    XmlAttributeValue attrValue = attr.getValueElement();
    return attrValue == null ? null : attrValue.getValueTextRange().shiftRight(-attr.getTextOffset());
  }

  @NotNull
  private PsiReference[] getReferenceToComponentClass(@NotNull XmlAttribute attr, @Nullable TextRange range) {
    if (range == null) return PsiReference.EMPTY_ARRAY;
    final XmlTag tag = attr.getParent();
    Component component = TapestryUtils.getTypeOfTag(tag);
    final IntellijJavaClassType elementClass = component == null ? null : (IntellijJavaClassType)component.getElementClass();

    return new PsiReference[]{new PsiReferenceBase<PsiElement>(attr, range) {
      @Nullable
      public PsiElement resolve() {
        return elementClass == null ? null : elementClass.getPsiClass();
      }

      @NotNull
      public Object[] getVariants() {
        TapestryProject project = TapestryModuleSupportLoader.getTapestryProject(tag);
        return project == null ? EMPTY_ARRAY : project.getAvailableComponentNames();
      }
    }};
  }

  @NotNull
  private PsiReference[] getReferenceToEmbeddedComponent(@NotNull XmlAttribute attr, TextRange range) {
    if (range == null) return PsiReference.EMPTY_ARRAY;
    final XmlTag tag = attr.getParent();
    final IntellijJavaField field = (IntellijJavaField)TapestryUtils.findIdentifyingField(tag);

    return new PsiReference[]{new PsiReferenceBase<PsiElement>(attr, range) {
      @Nullable
      public PsiElement resolve() {
        return field == null ? null : field.getPsiField();
      }

      @NotNull
      public Object[] getVariants() {
        List<String> fieldsIds = TapestryUtils.getEmbeddedComponentIds(tag);
        return fieldsIds.toArray(new String[fieldsIds.size()]);
      }
    }};
  }

  @NotNull
  private PsiReference[] getReferenceToPage(final Component component, @NotNull XmlAttribute pageAttr) {
    final TextRange range = getValueTextRange(pageAttr);
    if (range == null) return PsiReference.EMPTY_ARRAY;

    final Page page = component.getProject().findPage(pageAttr.getValue());

    return new PsiReference[]{new PsiReferenceBase<PsiElement>(pageAttr, range) {
      @Nullable
      public PsiElement resolve() {
        if (page == null) return null;
        IResource[] templates = page.getTemplate();
        return templates.length == 0 ? null : ((IntellijResource)templates[0]).getPsiFile();
      }

      @NotNull
      public Object[] getVariants() {
        return component.getProject().getAvailablePageNames();
      }
    }};
  }

}

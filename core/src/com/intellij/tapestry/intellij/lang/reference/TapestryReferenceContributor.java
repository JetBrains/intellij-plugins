package com.intellij.tapestry.intellij.lang.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.model.presentation.Page;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.java.IntellijJavaField;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TapestryReferenceContributor extends PsiReferenceContributor {

  public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {
    registerTagNameReferenceProvider(registrar);
  }

  private void registerTagNameReferenceProvider(PsiReferenceRegistrar registrar) {
    PatternCondition<XmlTag> tapestryTagCondition = new PatternCondition<XmlTag>("tapestryTagCondition") {
      public boolean accepts(@NotNull XmlTag tag, final ProcessingContext context) {
        return TapestryUtils.isTapestryTemplate((XmlFile)tag.getContainingFile())
               && TapestryUtils.getComponentIdentifier(tag) != null;
      }
    };
    registrar.registerReferenceProvider(XmlPatterns.xmlTag().with(tapestryTagCondition), new PsiReferenceProvider() {
      @NotNull
      public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
        if (!(element instanceof XmlTag)) return PsiReference.EMPTY_ARRAY;
        final XmlTag tag = (XmlTag)element;
        PsiReference typeRef = null;
        if (tag.getNamespace().equals(TapestryConstants.TEMPLATE_NAMESPACE)) {
          PsiElement nameElem = IdeaUtils.getNameElement(tag);
          TextRange range = TextRange.from(nameElem.getStartOffsetInParent(), nameElem.getTextLength());
          typeRef = getReferenceToComponentClass(tag, range);
        }
        else {
          XmlAttribute typeAttr = TapestryUtils.getTTypeAttribute(tag);
          if (typeAttr != null) {
            typeRef = getReferenceToComponentClass(tag, getValueTextRangeInTag(typeAttr, tag));
          }
          else {
            XmlAttribute idAttr = TapestryUtils.getTIdAttribute(tag);
            if (idAttr != null) {
              typeRef = getReferenceToEmbeddedComponent(tag, getValueTextRangeInTag(idAttr, tag));
            }
          }
        }

        if (typeRef == null) return PsiReference.EMPTY_ARRAY;
        final PsiReference pageRef = getReferenceToPage(tag, TapestryUtils.getTapestryAttribute(tag, "page"));
        return pageRef == null ? new PsiReference[]{typeRef} : new PsiReference[]{typeRef, pageRef};
      }
    });
  }

  @NotNull
  private TextRange getValueTextRangeInTag(@NotNull XmlAttribute typeAttr, XmlTag parent) {
    XmlAttributeValue attrValue = typeAttr.getValueElement();
    int start = attrValue.getTextOffset() - parent.getTextOffset() - 1;
    return TextRange.from(start, attrValue.getTextLength());
  }

  @Nullable
  private PsiReferenceBase<PsiElement> getReferenceToComponentClass(XmlTag tag, TextRange range) {
    final Component component = TapestryUtils.getTypeOfTag(tag);
    if (component == null) return null;
    final IntellijJavaClassType elementClass = (IntellijJavaClassType)component.getElementClass();

    return new PsiReferenceBase<PsiElement>(tag, range) {
      @Nullable
      public PsiElement resolve() {
        return elementClass.getPsiClass();
      }

      @NotNull
      public Object[] getVariants() {
        return EMPTY_ARRAY;
      }
    };
  }

  @Nullable
  private PsiReferenceBase<PsiElement> getReferenceToEmbeddedComponent(XmlTag tag, TextRange range) {
    final IntellijJavaField field = (IntellijJavaField)TapestryUtils.findIdentifyingField(tag);
    if (field == null) return null;

    return new PsiReferenceBase<PsiElement>(tag, range) {
      @Nullable
      public PsiElement resolve() {
        return field.getPsiField();
      }

      @NotNull
      public Object[] getVariants() {
        return EMPTY_ARRAY;
      }
    };
  }

  @Nullable
  private PsiReferenceBase<PsiElement> getReferenceToPage(XmlTag tag, XmlAttribute pageAttr) {
    if (pageAttr == null) return null;

    Component component = TapestryUtils.getTypeOfTag(tag);
    if (component == null) return null;

    TapestryParameter pageParam = component.getParameters().get(pageAttr.getName());
    if (pageParam == null) return null;

    final Page page = component.getProject().findPage(pageAttr.getValue());

    return new PsiReferenceBase<PsiElement>(tag, getValueTextRangeInTag(pageAttr, tag)) {
      @Nullable
      public PsiElement resolve() {
        if (page == null) return null;
        IResource[] templates = page.getTemplate();
        return templates.length == 0 ? null : ((IntellijResource)templates[0]).getPsiFile();
      }

      @NotNull
      public Object[] getVariants() {
        return EMPTY_ARRAY;
      }
    };
  }

}

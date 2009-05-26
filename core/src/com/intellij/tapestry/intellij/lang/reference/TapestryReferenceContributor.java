package com.intellij.tapestry.intellij.lang.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.util.ComponentUtils;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.java.IntellijJavaField;
import com.intellij.tapestry.intellij.core.resource.xml.IntellijXmlTag;
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
        final VirtualFile vFile = tag.getContainingFile().getVirtualFile();
        return (vFile == null || TapestryConstants.TEMPLATE_FILE_EXTENSION.equals(vFile.getExtension()))
               && ComponentUtils.isComponentTag(new IntellijXmlTag(tag));
      }
    };


    registrar.registerReferenceProvider(XmlPatterns.xmlTag().with(tapestryTagCondition), new PsiReferenceProvider() {
      @NotNull
      public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
        if (!(element instanceof XmlTag)) return PsiReference.EMPTY_ARRAY;
        final XmlTag tag = (XmlTag)element;
        PsiReference ref = null;
        if (tag.getNamespace().equals(TapestryConstants.TEMPLATE_NAMESPACE)) {
          PsiElement nameElem = IdeaUtils.getNameElement(tag);
          TextRange range = TextRange.from(nameElem.getStartOffsetInParent(), nameElem.getTextLength());
          ref = getReferenceToComponentClass(tag, range);
        }
        else {
          XmlAttribute typeAttr = TapestryUtils.getTTypeAttribute(tag);
          if (typeAttr != null) {
            ref = getReferenceToComponentClass(tag, getValueTextRangeInTag(typeAttr, tag));
          }
          else {
            XmlAttribute idAttr = TapestryUtils.getTIdAttribute(tag);
            if (idAttr != null) {
              ref = getReferenceToEmbeddedComponent(tag, getValueTextRangeInTag(idAttr, tag));
            }
          }
        }
        return ref == null ? PsiReference.EMPTY_ARRAY : new PsiReference[]{ref};
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
    final Component component = TapestryUtils.getComponentFromTag(tag);
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
}

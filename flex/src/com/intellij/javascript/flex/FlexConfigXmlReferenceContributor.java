package com.intellij.javascript.flex;

import com.intellij.lang.javascript.flex.ReferenceSupport;
import com.intellij.lang.javascript.psi.ecmal4.impl.ActionScriptReferenceSet;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.filters.position.NamespaceFilter;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.XmlPatterns.*;

public class FlexConfigXmlReferenceContributor extends PsiReferenceContributor {
  public static final String CLASS_REFERENCE = "ClassReference";

  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    XmlUtil.registerXmlTagReferenceProvider(
      registrar,
      new String[]{"path-element", "class", "classname", "symbol"},
      new NamespaceFilter(FlexApplicationComponent.HTTP_WWW_ADOBE_COM_2006_FLEX_CONFIG),
      true,
      new PsiReferenceProvider() {
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
          TextRange myRange = ElementManipulators.getValueTextRange(element);
          if (myRange.getStartOffset() == 0) return PsiReference.EMPTY_ARRAY;
          XmlTag tag = (XmlTag)element;
          final String trimmed = tag.getValue().getTrimmedText();
          if (trimmed.indexOf('{') != -1) return PsiReference.EMPTY_ARRAY;

          if ("path-element".equals(tag.getLocalName())) {
            return ReferenceSupport
              .getFileRefs(element, myRange.getStartOffset(), trimmed, ReferenceSupport.LookupOptions.FLEX_COMPILER_CONFIG_PATH_ELEMENT);
          }

          return new FlexConfigXmlReferenceSet(element, trimmed, myRange.getStartOffset()).getReferences();
        }
      });

    registrar.registerReferenceProvider(
      xmlAttributeValue(xmlAttribute("class").withParent(xmlTag().withName("component").withParent(xmlTag().withName("componentPackage")))),
      new PsiReferenceProvider() {
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
          TextRange myRange = ElementManipulators.getValueTextRange(element);
          if (myRange.getStartOffset() == 0) return PsiReference.EMPTY_ARRAY;
          final String attrValue = ((XmlAttributeValue)element).getValue();
          return new FlexConfigXmlReferenceSet(element, attrValue, myRange.getStartOffset()).getReferences();
        }
      });
  }

  static class FlexConfigXmlReferenceSet extends ActionScriptReferenceSet {
    FlexConfigXmlReferenceSet(@NotNull PsiElement element, String text, int offset) {
      super(element, text, offset, false, true);
    }

    @Override
    protected int findSeparatorPosition(String s, int fromIndex) {
      int pos = s.indexOf('.', fromIndex);
      // no more than one ':' and '#' symbol after last '.'
      if (pos == -1 && s.indexOf(":") >= fromIndex) pos = s.indexOf(":", fromIndex);
      if (pos == -1 && s.indexOf("#") >= fromIndex) pos = s.indexOf("#", fromIndex);
      return pos;
    }
  }
}

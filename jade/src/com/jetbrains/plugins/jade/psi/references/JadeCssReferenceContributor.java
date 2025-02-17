package com.jetbrains.plugins.jade.psi.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.css.CssReferenceContributor;
import com.intellij.psi.css.impl.util.CssInHtmlClassOrIdReferenceProvider;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.xml.util.HtmlUtil;
import com.intellij.xml.util.XmlUtil;
import com.jetbrains.plugins.jade.JadeLanguage;
import com.jetbrains.plugins.jade.psi.impl.JadeAttributeValueImpl;
import org.jetbrains.annotations.NotNull;

public final class JadeCssReferenceContributor extends CssReferenceContributor {
  @Override
  public void registerReferenceProviders(final @NotNull PsiReferenceRegistrar registrar) {
    final CssInHtmlClassOrIdReferenceProvider htmlClassOrIdReferenceProvider = new CssInJadeClassOrIdReferenceProvider();
    XmlUtil.registerXmlAttributeValueReferenceProvider(registrar, new String[]{HtmlUtil.CLASS_ATTRIBUTE_NAME, HtmlUtil.ID_ATTRIBUTE_NAME},
                                                       htmlClassOrIdReferenceProvider.getFilter(),
                                                       false, htmlClassOrIdReferenceProvider);
  }


  private static class CssInJadeClassOrIdReferenceProvider extends CssInHtmlClassOrIdReferenceProvider {
    @Override
    public ElementFilter getFilter() {
      return new CssInJadeElementFilter();
    }

    private static class CssInJadeElementFilter implements ElementFilter {
      @Override
      public boolean isAcceptable(Object element, PsiElement context) {
        PsiElement psiElement = (PsiElement)element;
        if (psiElement.getLanguage() != JadeLanguage.INSTANCE) {
          return false;
        }

        final PsiElement parent = psiElement.getParent();

        if (parent instanceof XmlAttribute xmlAttribute) {
          final String attrName = xmlAttribute.getName();

          if (psiElement instanceof JadeAttributeValueImpl &&
              ((JadeAttributeValueImpl)psiElement).isSyntheticClass()) {
            return true;
          }

          if (isSuitableAttribute(attrName) &&
              xmlAttribute.getParent().getNamespacePrefix().isEmpty()) {
            return true;
          }
        }
        return false;
      }

      private static boolean isSuitableAttribute(final String attrName) {
        return HtmlUtil.CLASS_ATTRIBUTE_NAME.equalsIgnoreCase(attrName) || HtmlUtil.ID_ATTRIBUTE_NAME.equalsIgnoreCase(attrName);
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }
  }
}


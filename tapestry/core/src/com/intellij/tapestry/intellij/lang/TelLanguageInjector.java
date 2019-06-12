package com.intellij.tapestry.intellij.lang;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.intellij.lang.descriptor.TapestryAttributeDescriptor;
import com.intellij.tapestry.intellij.lang.descriptor.TapestryXmlExtension;
import com.intellij.tapestry.lang.TelLanguage;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class TelLanguageInjector implements MultiHostInjector {
  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    PsiElement contextParent = context.getParent();
    if (!(contextParent instanceof XmlAttribute)) return;
    XmlAttribute attr = (XmlAttribute)contextParent;
    String name = attr.getLocalName();
    if ("type".equals(name) || "id".equals(name)) return;
    XmlTag parent = attr.getParent();
    if (parent == null) return;

    if (!TapestryXmlExtension.isTapestryTemplateNamespace(attr.getNamespace()) &&
        !TapestryXmlExtension.isTapestryTemplateNamespace(parent.getNamespace())) {
      return;
    }
    if(attr.textContains('\n')) return;
    String value = attr.getValue();
    if (value.indexOf('$') != -1 || value.indexOf('{') != -1 || value.indexOf('}') != -1 || value.indexOf('/') != -1 || value.indexOf('\\') != -1) {
      return;
    }

    final String propPrefix = "prop:";
    boolean explicitProp = value.startsWith(propPrefix);
    TextRange range = attr.getValueTextRange();

    if (!explicitProp) {
      XmlAttributeDescriptor descriptor = attr.getDescriptor();

      if (descriptor instanceof TapestryAttributeDescriptor) {
        TapestryAttributeDescriptor tapestryAttributeDescriptor = (TapestryAttributeDescriptor)descriptor;
        String prefix = tapestryAttributeDescriptor.getDefaultPrefix();

        if (prefix != null && !"prop".equals(prefix)) {
          return;
        }
      } else {
        return;
      }
    } else {
      if (range.getLength() >= propPrefix.length()) {
        range = new TextRange(range.getStartOffset() + propPrefix.length(), range.getEndOffset());
      }
    }

    registrar.startInjecting(TelLanguage.INSTANCE).addPlace("${", "}", (PsiLanguageInjectionHost)context, range).doneInjecting();
  }

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Collections.singletonList(XmlAttributeValue.class);
  }
}

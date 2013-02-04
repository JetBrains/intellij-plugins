package com.intellij.tapestry.intellij.lang;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.intellij.lang.descriptor.TapestryAttributeDescriptor;
import com.intellij.tapestry.lang.TelLanguage;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * User: Maxim.Mossienko
 * Date: 2/4/13
 */
public class TelLanguageInjector implements MultiHostInjector {
  private static final Set<String> namespaces = new THashSet<String>(Arrays.asList(
    TapestryConstants.TEMPLATE_NAMESPACE, TapestryConstants.TEMPLATE_NAMESPACE2, TapestryConstants.TEMPLATE_NAMESPACE3));

  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    PsiElement contextParent = context.getParent();
    if (!(contextParent instanceof XmlAttribute)) return;
    XmlAttribute attr = (XmlAttribute)contextParent;
    String name = attr.getLocalName();
    if ("type".equals(name) || "id".equals(name)) return;
    XmlTag parent = attr.getParent();
    if (parent == null) return;

    if (!namespaces.contains(attr.getNamespace()) && !namespaces.contains(parent.getNamespace())) return;
    if(attr.textContains('\n')) return;
    String value = attr.getValue();
    if (value.indexOf('$') != -1 || value.indexOf('{') != -1 || value.indexOf('}') != -1 || value.indexOf('/') != -1 || value.indexOf('\\') != -1) {
      return;
    }

    XmlAttributeDescriptor descriptor = attr.getDescriptor();
    if (descriptor instanceof AnyXmlAttributeDescriptor) return;
    if (descriptor instanceof TapestryAttributeDescriptor  &&
        "literal".equals(((TapestryAttributeDescriptor)descriptor).getDefaultPrefix())) {
      return;
    }

    registrar.startInjecting(TelLanguage.INSTANCE).addPlace("${", "}", (PsiLanguageInjectionHost)context, attr.getValueTextRange()).doneInjecting();
  }

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(XmlAttributeValue.class);
  }
}

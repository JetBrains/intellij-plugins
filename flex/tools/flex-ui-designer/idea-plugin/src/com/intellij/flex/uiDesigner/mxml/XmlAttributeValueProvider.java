package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InjectionUtil;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class XmlAttributeValueProvider implements XmlElementValueProvider {
  private XmlAttribute attribute;

  public XmlAttributeValueProvider() {
  }

  public XmlAttributeValueProvider(XmlAttribute attribute) {
    this.attribute = attribute;
  }

  public void setAttribute(@Nullable XmlAttribute attribute) {
    this.attribute = attribute;
  }

  @Override
  public String getTrimmed() {
    return attribute.getDisplayValue();
  }

  @Override
  public CharSequence getSubstituted() {
    return attribute.getDisplayValue();
  }

  @Override
  public PsiLanguageInjectionHost getInjectedHost() {
    return (PsiLanguageInjectionHost)attribute.getValueElement();
  }

  @Override
  @Nullable
  public JSClass getJsClass() {
    //noinspection ConstantConditions
    return InjectionUtil.getJsClassFromPackageAndLocalClassNameReferences(attribute.getValueElement());
  }

  @NotNull
  @Override
  public XmlElement getElement() {
    return attribute;
  }

  @Override
  public PsiMetaData getPsiMetaData() {
    return attribute.getDescriptor();
  }
}

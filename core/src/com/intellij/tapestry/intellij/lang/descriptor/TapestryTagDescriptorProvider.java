package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.PsiFile;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.tapestry.psi.TmlFile;

/**
 * @author Alexey Chmutov
 */
public class TapestryTagDescriptorProvider implements XmlElementDescriptorProvider {
  public XmlElementDescriptor getDescriptor(XmlTag tag) {
    PsiFile file = tag.getContainingFile();
    return file instanceof TmlFile ? DescriptorUtil.getTmlOrHtmlTagDescriptor(tag) : null;
  }

}

package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.psi.TmlFile;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class TapestryTagDescriptorProvider implements XmlElementDescriptorProvider {
  @Override
  @Nullable
  public XmlElementDescriptor getDescriptor(XmlTag tag) {
    if (DumbService.isDumb(tag.getProject())) return null;
    PsiFile file = tag.getContainingFile();
    return file instanceof TmlFile ? DescriptorUtil.getTmlOrHtmlTagDescriptor(tag) : null;
  }

}

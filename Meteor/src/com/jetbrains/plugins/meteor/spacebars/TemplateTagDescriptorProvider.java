package com.jetbrains.plugins.meteor.spacebars;

import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import com.jetbrains.plugins.meteor.spacebars.lang.SpacebarsLanguageDialect;
import org.jetbrains.annotations.Nullable;

/**
 * Description provider for "template" tag:
 * <template name="somethingName"> </template>
 */
public class TemplateTagDescriptorProvider implements XmlElementDescriptorProvider {
  private static final String TEMPLATE_TAG = "template";

  @Nullable
  @Override
  public XmlElementDescriptor getDescriptor(XmlTag tag) {
    if (!tag.getName().equals(TEMPLATE_TAG)) {
      return null;
    }

    PsiFile file = tag.getContainingFile();
    if (file == null) return null;

    PsiFile spacebarsFileView = file.getViewProvider().getPsi(SpacebarsLanguageDialect.INSTANCE);
    if (spacebarsFileView == null) return null;

    return new TemplateTagXmlElementDescriptor(tag);
  }
}

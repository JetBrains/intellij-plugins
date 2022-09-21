package com.intellij.javascript.flex.mxml.schema;

import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class FxDefinitionBackedDescriptor extends ClassBackedElementDescriptor {
  private final XmlTag myXmlTag;

  FxDefinitionBackedDescriptor(Module module, XmlTag xmlTag) {
    super(xmlTag.getName(), "", CodeContext.getContext(JavaScriptSupportLoader.MXML_URI3, module), module.getProject());
    myXmlTag = xmlTag;
  }

  @Override
  public PsiElement getDeclaration() {
    return getDeclarationByFxDefinitionTag(myXmlTag);
  }

  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(String attributeName, @Nullable XmlTag context) {
    final ClassBackedElementDescriptor descriptor = getClassBackedDescriptor();
    return descriptor == null ? null : descriptor.getAttributeDescriptor(attributeName, context);
  }

  @Override
  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag _context) {
    final ClassBackedElementDescriptor descriptor = getClassBackedDescriptor();
    return descriptor == null ? XmlAttributeDescriptor.EMPTY : descriptor.getAttributesDescriptors(_context);
  }

  @Nullable
  private ClassBackedElementDescriptor getClassBackedDescriptor() {
    final XmlTag tag = PsiTreeUtil.getParentOfType(getDeclaration(), XmlTag.class);
    final XmlTag[] subTags = tag == null ? XmlTag.EMPTY : tag.getSubTags();
    if (subTags.length == 1) {
      final XmlElementDescriptor descriptor = subTags[0].getDescriptor();
      return descriptor instanceof ClassBackedElementDescriptor ? (ClassBackedElementDescriptor)descriptor : null;
    }
    return null;
  }

  @Nullable
  private static XmlAttributeValue getDeclarationByFxDefinitionTag(final @NotNull XmlTag xmlTag) {
    if (!xmlTag.isValid() || xmlTag.getParent() instanceof XmlDocument) {
      return null;
    }

    XmlTag rootTag = xmlTag;
    XmlTag parent;
    while ((parent = rootTag.getParentTag()) != null) {
      rootTag = parent;
    }

    final XmlTag[] subTags = rootTag.getSubTags();
    final XmlTag libraryTag = subTags.length > 0 &&
                              FlexPredefinedTagNames.LIBRARY.equals(subTags[0].getLocalName()) &&
                              JavaScriptSupportLoader.MXML_URI3.equals(subTags[0].getNamespace()) ? subTags[0] : null;
    final XmlTag[] definitionTags =
      libraryTag == null ? XmlTag.EMPTY : libraryTag.findSubTags(CodeContext.DEFINITION_TAG_NAME, JavaScriptSupportLoader.MXML_URI3);

    final String localName = xmlTag.getLocalName();

    for (final XmlTag definitionTag : definitionTags) {
      final XmlAttribute nameAttribute = definitionTag.getAttribute(MxmlLanguageTagsUtil.NAME_ATTRIBUTE);
      final XmlAttributeValue attributeValue = nameAttribute == null ? null : nameAttribute.getValueElement();
      if (attributeValue != null && localName.equals(attributeValue.getValue())) {
        return attributeValue;
      }
    }

    return null;
  }

  @Nullable
  static XmlElementDescriptor getFxDefinitionBackedDescriptor(final @NotNull Module module, final @NotNull XmlTag xmlTag) {
    final XmlAttributeValue declaration = getDeclarationByFxDefinitionTag(xmlTag);
    if (declaration != null) {
      return new FxDefinitionBackedDescriptor(module, xmlTag);
    }

    return null;
  }
}

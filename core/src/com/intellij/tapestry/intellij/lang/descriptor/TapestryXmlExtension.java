package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.psi.TmlFile;
import com.intellij.xml.DefaultXmlExtension;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 *         Date: Jul 17, 2009
 *         Time: 6:39:24 PM
 */
public class TapestryXmlExtension extends DefaultXmlExtension {

  @Nullable
  @Override
  public String[][] getNamespacesFromDocument(final XmlDocument parent, boolean declarationsExist) {
    String[][] namespaces = {{"", XmlUtil.XHTML_URI}, {"t", TapestryConstants.TEMPLATE_NAMESPACE}};
    XmlTag rootTag = parent.getRootTag();
    if (rootTag == null) return namespaces;
    for (final XmlAttribute attribute : rootTag.getAttributes()) {
      if (!attribute.isNamespaceDeclaration()) continue;
      if (TapestryConstants.TEMPLATE_NAMESPACE.equals(attribute.getValue())) {
        namespaces[1][0] = getNamespacePrefixFromDeclaration(attribute);
      }
      else if (XmlUtil.XHTML_URI.equals(attribute.getValue())) {
        namespaces[0][0] = getNamespacePrefixFromDeclaration(attribute);
      }
    }
    return namespaces;
  }

  private static String getNamespacePrefixFromDeclaration(XmlAttribute attribute) {
    final String localName = attribute.getLocalName();
    return localName.equals(attribute.getName()) ? "" : localName;
  }

  @Override
  public boolean isAvailable(PsiFile file) {
    return file instanceof TmlFile;
  }

  @Override
  @Nullable
  public XmlNSDescriptor getNSDescriptor(final XmlTag element, final String namespace, final boolean strict) {
    PsiFile file = element.getContainingFile();
    if (!(file instanceof TmlFile)) return null;
    if (TapestryConstants.TEMPLATE_NAMESPACE.equals(namespace)) return TapestryNamespaceDescriptor.INSTANCE;
    if (XmlUtil.XHTML_URI.equals(namespace)) return DescriptorUtil.getHtmlNSDescriptor((TmlFile)file);
    return null;
  }

}

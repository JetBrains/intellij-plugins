package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.tapestry.psi.TmlFile;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.impl.schema.XmlNSDescriptorImpl;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Alexey Chmutov
 *         Date: Jun 15, 2009
 *         Time: 12:54:29 PM
 */
class DescriptorUtil {

  public static XmlAttributeDescriptor[] getAttributeDescriptors(@NotNull XmlTag context) {
    Component component = TapestryUtils.getTypeOfTag(context);
    if (component == null) {
      String prefix = context.getPrefixByNamespace(TapestryConstants.TEMPLATE_NAMESPACE);
      if (prefix == null) return XmlAttributeDescriptor.EMPTY;
      String type = prefix.length() > 0 ? prefix + ":type" : "type";
      String id = prefix.length() > 0 ? prefix + ":id" : "id";
      return new XmlAttributeDescriptor[]{new TapestryIdOrTypeAttributeDescriptor(type, context),
          new TapestryIdOrTypeAttributeDescriptor(id, context)};
    }
    return getAttributeDescriptors(component);
  }

  public static XmlAttributeDescriptor[] getAttributeDescriptors(@NotNull Component component) {
    Collection<TapestryParameter> params = component.getParameters().values();
    XmlAttributeDescriptor[] descriptors = new XmlAttributeDescriptor[params.size()];
    int i = 0;
    for (TapestryParameter param : params) {
      descriptors[i++] = new TapestryAttributeDescriptor(param);
    }
    return descriptors;
  }

  public static XmlAttributeDescriptor getAttributeDescriptor(@NotNull String attributeName, @NotNull XmlTag context) {
    XmlAttribute attr = TapestryUtils.getIdentifyingAttribute(context);
    if (attr != null && attr.getName().equals(attributeName)) return new TapestryIdOrTypeAttributeDescriptor(attributeName, context);
    Component component = TapestryUtils.getTypeOfTag(context);
    if (component == null) return null;
    return getAttributeDescriptor(attributeName, component);
  }

  public static XmlAttributeDescriptor getAttributeDescriptor(@NotNull String attributeName, @NotNull Component component) {
    TapestryParameter param = component.getParameters().get(XmlUtil.findLocalNameByQualifiedName(attributeName));
    return param == null ? null : new TapestryAttributeDescriptor(param);
  }

  private static XmlElementDescriptor[] getElementDescriptors(@NotNull Collection<PresentationLibraryElement> elements,
                                                              String namespacePrefix) {
    TapestryTagDescriptor[] descriptors = new TapestryTagDescriptor[elements.size()];
    int i = 0;
    for (PresentationLibraryElement component : elements) {
      descriptors[i++] = new TapestryTagDescriptor(component, namespacePrefix);
    }
    return descriptors;
  }

  public static XmlElementDescriptor[] getTmlSubelementDescriptors(@NotNull XmlTag context) {
    TapestryProject project = TapestryModuleSupportLoader.getTapestryProject(context);
    if (project == null) return XmlElementDescriptor.EMPTY_ARRAY;
    String namespacePrefix = context.getPrefixByNamespace(TapestryConstants.TEMPLATE_NAMESPACE);
    return getElementDescriptors(project.getAvailableElements(), namespacePrefix);
  }

  public static XmlElementDescriptor getTmlOrHtmlTagDescriptor(@NotNull XmlTag tag) {
    TmlFile file = getTmlFile(tag);
    if (file == null) return null;
    XmlElementDescriptor tmlDescriptor = getTmlTagDescriptor(tag);
    if (tmlDescriptor != null) return tmlDescriptor;
    XmlElementDescriptor htmlDescriptor = getHtmlTagDescriptor(tag, file);
    return htmlDescriptor != null ? new TapestryHtmlTagDescriptor(htmlDescriptor, TapestryUtils.getTypeOfTag(tag)) : null;
  }

  private static XmlElementDescriptor getHtmlTagDescriptor(XmlTag tag, TmlFile file) {
    XmlNSDescriptor htmlNSDescriptor = getHtmlNSDescriptor(file);
    return htmlNSDescriptor instanceof XmlNSDescriptorImpl ? ((XmlNSDescriptorImpl)htmlNSDescriptor)
        .getElementDescriptor(tag.getLocalName(), tag.getNamespace()) : htmlNSDescriptor.getElementDescriptor(tag);
  }

  private static TmlFile getTmlFile(XmlTag tag) {
    PsiFile file = tag.getContainingFile();
    if (file instanceof TmlFile) return (TmlFile)file;
    XmlElement parentTag = tag.getUserData(XmlElement.INCLUDING_ELEMENT);
    if (parentTag == null) return null;
    file = parentTag.getContainingFile();
    return file instanceof TmlFile ? (TmlFile)file : null;
  }

  public static XmlNSDescriptor getHtmlNSDescriptor(TmlFile tmlFile) {
    XmlDocument doc = tmlFile.getDocument();
    if (doc == null) return null;
    return doc.getDefaultNSDescriptor(XmlUtil.XHTML_URI, false);
  }

  @Nullable
  public static XmlElementDescriptor getTmlTagDescriptor(XmlTag tag) {
    if (!TapestryConstants.TEMPLATE_NAMESPACE.equals(tag.getNamespace())) return null;
    final Component component = TapestryUtils.getTypeOfTag(tag);
    final String prefix = tag.getNamespacePrefix();
    return component == null ? new TapestryUnknownTagDescriptor(tag.getLocalName(), prefix) : new TapestryTagDescriptor(component, prefix);
  }
}

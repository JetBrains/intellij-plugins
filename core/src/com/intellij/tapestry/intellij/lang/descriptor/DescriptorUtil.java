package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl;
import com.intellij.psi.PsiFile;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.tapestry.intellij.util.PsiElementBasedCachedUserDataCache;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.psi.TmlFile;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Alexey Chmutov
 *         Date: Jun 15, 2009
 *         Time: 12:54:29 PM
 */
class DescriptorUtil {
  private static final PsiElementBasedCachedUserDataCache<XmlNSDescriptor, TmlFile> ourHtmlNSDescriptorCache =
      new PsiElementBasedCachedUserDataCache<XmlNSDescriptor, TmlFile>("ourHtmlNSDescriptorCache") {
        protected XmlNSDescriptor computeValue(TmlFile tmlFile) {
          XmlDocument doc = tmlFile.getDocument();
          if (doc == null) return null;
          return new HtmlNSDescriptorImpl(doc.getDefaultNSDescriptor(XmlUtil.XHTML_URI, false));
        }
      };

  public static XmlAttributeDescriptor[] getAttributeDescriptors(@NotNull XmlTag context) {
    Component component = TapestryUtils.getTypeOfTag(context);
    if (component == null) return XmlAttributeDescriptor.EMPTY;
    return getAttributeDescriptors(component);
  }

  public static XmlAttributeDescriptor[] getAttributeDescriptors(Component component) {
    Collection<TapestryParameter> params = component.getParameters().values();
    XmlAttributeDescriptor[] descriptors = new XmlAttributeDescriptor[params.size()];
    int i = 0;
    for (TapestryParameter param : params) {
      descriptors[i++] = new TapestryAttributeDescriptor(param);
    }
    return descriptors;
  }

  public static XmlAttributeDescriptor getAttributeDescriptor(@NotNull String attributeName, @NotNull XmlTag context) {
    Component component = TapestryUtils.getTypeOfTag(context);
    if (component == null) return null;
    XmlAttribute attr = TapestryUtils.getIdentifyingAttribute(context);
    if (attr != null && attr.getLocalName().equals(attributeName)) return new TapestryIdOrTypeAttributeDescriptor(attributeName);
    return getAttributeDescriptor(attributeName, component);
  }

  public static XmlAttributeDescriptor getAttributeDescriptor(String attributeName, Component component) {
    TapestryParameter param = component.getParameters().get(attributeName);
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
    return getElementDescriptors(project.getAllAvailableElements(), namespacePrefix);
  }

  public static XmlElementDescriptor getTmlOrHtmlTagDescriptor(XmlTag tag) {
    PsiFile file = tag.getContainingFile();
    if (!(file instanceof TmlFile)) return null;
    if (TapestryConstants.TEMPLATE_NAMESPACE.equals(tag.getNamespace())) return getTmlTagDescriptor(tag);
    XmlElementDescriptor htmlDescriptor = ourHtmlNSDescriptorCache.get((TmlFile)file).getElementDescriptor(tag);
    return htmlDescriptor != null ? new TapestryHtmlTagDescriptor(htmlDescriptor, TapestryUtils.getTypeOfTag(tag)) : null;
  }

  public static XmlElementDescriptor getTmlTagDescriptor(XmlTag tag) {
    final Component component = TapestryUtils.getTypeOfTag(tag);
    final String prefix = tag.getNamespacePrefix();
    return component == null ? new TapestryUnknownTagDescriptor(tag.getLocalName(), prefix) : new TapestryTagDescriptor(component, prefix);
  }

  public static XmlElementDescriptor[] getHtmlTagDescriptors(XmlDocument doc) {
    PsiFile file = doc.getContainingFile();
    if (!(file instanceof TmlFile)) return null;
    return ourHtmlNSDescriptorCache.get((TmlFile)file).getRootElementsDescriptors(doc);
  }
}

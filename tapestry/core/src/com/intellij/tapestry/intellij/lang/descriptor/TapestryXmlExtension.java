package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.tapestry.psi.TmlFile;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.DefaultXmlExtension;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.impl.dtd.XmlNSDescriptorImpl;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * @author Alexey Chmutov
 */
public class TapestryXmlExtension extends DefaultXmlExtension {
  private static final Set<String> ourTapestryTemplateNamespaces = ContainerUtil.set(
    TapestryConstants.TEMPLATE_NAMESPACE, TapestryConstants.TEMPLATE_NAMESPACE2, TapestryConstants.TEMPLATE_NAMESPACE3,
    TapestryConstants.TEMPLATE_NAMESPACE4);

  public static boolean isTapestryTemplateNamespace(String namespace) {
    return namespace != null && ourTapestryTemplateNamespaces.contains(namespace);
  }

  @NotNull
  public static String getTapestryNamespace(XmlTag tag) {
    if (tag != null) {
      for(String tapestryTemplateNamespace:ourTapestryTemplateNamespaces) {
        if (tag.getPrefixByNamespace(tapestryTemplateNamespace) != null) return tapestryTemplateNamespace;
      }
    }

    return TapestryConstants.TEMPLATE_NAMESPACE;
  }

  public static @Nullable TapestryNamespaceDescriptor getTapestryTemplateDescriptor(@NotNull XmlTag tag) {
    final XmlNSDescriptor rootTagNSDescriptor = tag.getNSDescriptor(getTapestryNamespace(tag), true);
    return rootTagNSDescriptor instanceof TapestryNamespaceDescriptor ? (TapestryNamespaceDescriptor)rootTagNSDescriptor : null;

  }

  @Override
  public String[] @Nullable [] getNamespacesFromDocument(final XmlDocument parent, boolean declarationsExist) {
    String[][] namespaces = {
      {"", XmlUtil.XHTML_URI},
      {"t", TapestryConstants.TEMPLATE_NAMESPACE},
      {"p", TapestryConstants.PARAMETERS_NAMESPACE}
    };
    XmlTag rootTag = parent.getRootTag();
    if (rootTag == null) return namespaces;
    for (final XmlAttribute attribute : rootTag.getAttributes()) {
      if (!attribute.isNamespaceDeclaration()) continue;
      final String attributeValue = attribute.getValue();

      if (TapestryConstants.PARAMETERS_NAMESPACE.equals(attributeValue)) {
        namespaces[2][0] = getNamespacePrefixFromDeclaration(attribute);
      }
      else if (isTapestryTemplateNamespace(attributeValue)) {
        namespaces[1][0] = getNamespacePrefixFromDeclaration(attribute);
        namespaces[1][1] = attributeValue;
      }
      else if (XmlUtil.XHTML_URI.equals(attributeValue)) {
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
  public boolean isRequiredAttributeImplicitlyPresent(XmlTag tag, String attrName) {
    TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(tag);
    if(tapestryProject == null) return super.isRequiredAttributeImplicitlyPresent(tag, attrName);

    if(tag.getAttribute(attrName, getTapestryNamespace(tag)) != null) {
      return true;
    }

    final PresentationLibraryElement element = tapestryProject.findElementByTemplate(tag.getContainingFile());
    return element != null && TapestryUtils.parameterDefinedInClass(attrName, (IntellijJavaClassType)element.getElementClass(), tag);
  }

  @Override
  @Nullable
  public XmlNSDescriptor getNSDescriptor(final XmlTag element, final String namespace, final boolean strict) {
    PsiFile file = element.getContainingFile();
    if (!(file instanceof TmlFile)) return null;
    return element.getNSDescriptor(namespace, strict);
  }

  @Override
  public XmlNSDescriptor getDescriptorFromDoctype(final XmlFile file, final XmlNSDescriptor descriptor) {
    if (file instanceof TmlFile && descriptor instanceof XmlNSDescriptorImpl) {
      XmlDocument doc = file.getDocument();
      if(doc != null && doc.getProlog().getDoctype() != null) {
        return DescriptorUtil.getHtmlNSDescriptor((TmlFile)file);
      }
    }
    return descriptor;
  }

  public static String[] tapestryTemplateNamespaces() {
    return ArrayUtilRt.toStringArray(ourTapestryTemplateNamespaces);
  }
}

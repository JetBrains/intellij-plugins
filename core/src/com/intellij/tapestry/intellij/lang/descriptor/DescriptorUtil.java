package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.presentation.*;
import com.intellij.tapestry.core.model.presentation.components.DummyTapestryParameter;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.tapestry.psi.TmlFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.XmlNSDescriptorEx;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import com.intellij.xml.impl.schema.XmlNSDescriptorImpl;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Alexey Chmutov
 *         Date: Jun 15, 2009
 *         Time: 12:54:29 PM
 */
class DescriptorUtil {
  private DescriptorUtil() {
  }

  public static XmlAttributeDescriptor[] getAttributeDescriptors(@NotNull XmlTag context) {
    final XmlAttributeDescriptor[] result = getAttributeDescriptorsImpl(context);
    final List<Mixin> mixins = findMixins(context);
    if(mixins.isEmpty()) {
      return result;
    }
    final List<XmlAttributeDescriptor> listResult = new ArrayList<XmlAttributeDescriptor>(result.length);
    Collections.addAll(listResult, result);
    for (Mixin mixin : mixins) {
      ContainerUtil.addAll(listResult, getAttributeDescriptors(mixin, null));
    }
    return listResult.toArray(new XmlAttributeDescriptor[listResult.size()]);
  }

  private static XmlAttributeDescriptor[] getAttributeDescriptorsImpl(XmlTag context) {
    Component component = TapestryUtils.getTypeOfTag(context);
    String id = getTAttributeName(context, "id");
    if (component != null) {
      return getAttributeDescriptors(component, id == null ? null : new TapestryIdOrTypeAttributeDescriptor(id, context));
    }
    if (id == null) return XmlAttributeDescriptor.EMPTY;
    String type = getTAttributeName(context, "type");
    return new XmlAttributeDescriptor[]{new TapestryIdOrTypeAttributeDescriptor(type, context),
      new TapestryIdOrTypeAttributeDescriptor(id, context)};
  }

  @Nullable
  static String getTAttributeName(@NotNull XmlTag context, String attrName) {
    String prefix = context.getPrefixByNamespace(TapestryConstants.TEMPLATE_NAMESPACE);
    if (prefix == null) return null;
    return prefix.length() > 0 ? prefix + ":" + attrName : attrName;
  }

  private static
  @Nullable
  XmlElementDescriptor getImplicitHtmlContainer(@NotNull ParameterReceiverElement component, @NotNull XmlTag context) {
    IJavaClassType aClass = component.getElementClass();

    if (aClass != null && aClass.supportsInformalParameters()) {
      XmlNSDescriptor descriptor = context.getNSDescriptor(XmlUtil.XHTML_URI, false);
      if (descriptor instanceof XmlNSDescriptorEx) {
        return ((XmlNSDescriptorEx)descriptor).getElementDescriptor("div", XmlUtil.XHTML_URI);
      }
    }
    return null;
  }

  public static XmlAttributeDescriptor[] getAttributeDescriptors(@Nullable ParameterReceiverElement component,
                                                                 @Nullable TapestryIdOrTypeAttributeDescriptor idAttrDescriptor) {
    if (component == null) return XmlAttributeDescriptor.EMPTY;

    XmlAttributeDescriptor[] additionalParameters = XmlAttributeDescriptor.EMPTY;
    if (idAttrDescriptor != null) {
      PsiElement declaration = idAttrDescriptor.getDeclaration();
      if (declaration instanceof XmlTag) {
        XmlElementDescriptor div = getImplicitHtmlContainer(component, (XmlTag)declaration);
        if (div != null) {
          additionalParameters = div.getAttributesDescriptors((XmlTag)declaration);
        }
      }
    }

    Collection<TapestryParameter> params = component.getParameters().values();
    XmlAttributeDescriptor[] descriptors =
      new XmlAttributeDescriptor[params.size() + (idAttrDescriptor != null ? 1 : 0) + additionalParameters.length];
    int i = 0;
    for (TapestryParameter param : params) {
      descriptors[i++] = new TapestryAttributeDescriptor(param);
    }
    if (idAttrDescriptor != null) descriptors[i++] = idAttrDescriptor;
    for (XmlAttributeDescriptor attr : additionalParameters) {
      descriptors[i++] = attr;
    }
    return descriptors;
  }

  @Nullable
  public static XmlAttributeDescriptor getAttributeDescriptor(@NotNull String attributeName, @NotNull XmlTag context) {
    XmlAttribute attr = TapestryUtils.getIdentifyingAttribute(context);
    if (attr != null && attr.getName().equals(attributeName)) return new TapestryIdOrTypeAttributeDescriptor(attributeName, context);
    String id = getTAttributeName(context, "id");
    if (attributeName.equals(id)) return new TapestryIdOrTypeAttributeDescriptor(id, context);
    Component component = TapestryUtils.getTypeOfTag(context);
    final List<Mixin> mixins = findMixins(context);
    XmlAttributeDescriptor descriptor = getAttributeDescriptor(attributeName, component, mixins);
    if (descriptor != null) return descriptor;
    if (component != null) {
      XmlElementDescriptor container = getImplicitHtmlContainer(component, context);
      if (container != null) {
        descriptor = container.getAttributeDescriptor(attributeName, context);
        if (descriptor == null && attributeName.indexOf(':') == -1) { // allow any unqualified attribute
          descriptor = new AnyXmlAttributeDescriptor(attributeName);
        }
      }
    }
    return descriptor;
  }

  @Nullable
  public static XmlAttributeDescriptor getAttributeDescriptor(@NotNull String attributeName,
                                                              @Nullable ParameterReceiverElement component,
                                                              List<Mixin> mixins) {
    XmlAttributeDescriptor descriptor = getAttributeDescriptor(attributeName, component);
    if (descriptor != null) {
      return descriptor;
    }
    for (Mixin mixin : mixins) {
      descriptor = getAttributeDescriptor(attributeName, mixin);
      if (descriptor != null) {
        return descriptor;
      }
    }
    return null;
  }

  @Nullable
  public static XmlAttributeDescriptor getAttributeDescriptor(@NotNull String attributeName,
                                                              @Nullable ParameterReceiverElement component) {
    if (component == null) return null;
    TapestryParameter param = component.getParameters().get(XmlUtil.findLocalNameByQualifiedName(attributeName));
    return param == null ? null : new TapestryAttributeDescriptor(param);
  }

  public static XmlElementDescriptor[] getTmlSubelementDescriptors(@NotNull XmlTag context) {
    TapestryProject project = TapestryModuleSupportLoader.getTapestryProject(context);
    if (project == null) return XmlElementDescriptor.EMPTY_ARRAY;
    final String namespacePrefix = context.getPrefixByNamespace(TapestryConstants.TEMPLATE_NAMESPACE);
    final XmlElementDescriptor[] namespaceElements = getElementDescriptors(project.getAvailableElements(), namespacePrefix);
    final String parametersPrefix = context.getPrefixByNamespace(TapestryConstants.PARAMETERS_NAMESPACE);
    final Component component = TapestryUtils.getTypeOfTag(context);
    if (parametersPrefix == null || component == null) {
      return namespaceElements;
    }
    final XmlElementDescriptor[] parameterElements = getParameterDescriptors(component, parametersPrefix, findMixins(context));
    return ArrayUtil.mergeArrays(namespaceElements, parameterElements);
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

  private static XmlElementDescriptor[] getParameterDescriptors(@NotNull final Component component,
                                                                final String namespacePrefix,
                                                                List<Mixin> mixins) {
    final Function<TapestryParameter, XmlElementDescriptor> mapping = new Function<TapestryParameter, XmlElementDescriptor>() {
      @Override
      public XmlElementDescriptor fun(TapestryParameter parameter) {
        return new TapestryParameterDescriptor(component, parameter, namespacePrefix);
      }
    };
    final List<XmlElementDescriptor> result = ContainerUtil.map(component.getParameters().values(), mapping);
    for (Mixin mixin : mixins) {
      result.addAll(ContainerUtil.map(mixin.getParameters().values(), mapping));
    }
    return ArrayUtil.toObjectArray(result, XmlElementDescriptor.class);
  }

  @Nullable
  public static XmlElementDescriptor getTmlOrHtmlTagDescriptor(@NotNull XmlTag tag) {
    TmlFile file = getTmlFile(tag);
    if (file == null) return null;
    XmlElementDescriptor tmlDescriptor = getTmlTagDescriptor(tag);
    if (tmlDescriptor != null) return tmlDescriptor;
    XmlElementDescriptor htmlDescriptor = getHtmlTagDescriptor(tag, file);
    final List<Mixin> mixins = findMixins(tag);
    return htmlDescriptor != null ? new TapestryHtmlTagDescriptor(htmlDescriptor, TapestryUtils.getTypeOfTag(tag), mixins) : null;
  }

  @Nullable
  public static XmlElementDescriptor getHtmlTagDescriptorViaNsDescriptor(XmlTag tag) {
    TmlFile file = getTmlFile(tag);
    return file == null ? null : getHtmlTagDescriptor(tag, file);
  }

  @Nullable
  private static XmlElementDescriptor getHtmlTagDescriptor(XmlTag tag, TmlFile file) {
    XmlNSDescriptor htmlNSDescriptor = getHtmlNSDescriptor(file);
    return htmlNSDescriptor instanceof XmlNSDescriptorImpl ? ((XmlNSDescriptorImpl)htmlNSDescriptor)
      .getElementDescriptor(tag.getLocalName(), tag.getNamespace()) : htmlNSDescriptor.getElementDescriptor(tag);
  }

  @Nullable
  private static TmlFile getTmlFile(XmlTag tag) {
    PsiFile file = tag.getContainingFile();
    if (file instanceof TmlFile) return (TmlFile)file;
    XmlElement parentTag = tag.getUserData(XmlElement.INCLUDING_ELEMENT);
    if (parentTag == null) return null;
    file = parentTag.getContainingFile();
    return file instanceof TmlFile ? (TmlFile)file : null;
  }

  @Nullable
  public static XmlNSDescriptor getHtmlNSDescriptor(TmlFile tmlFile) {
    XmlDocument doc = tmlFile.getDocument();
    if (doc == null) return null;
    return doc.getDefaultNSDescriptor(XmlUtil.XHTML_URI, false);
  }

  @Nullable
  public static XmlElementDescriptor getTmlTagDescriptor(XmlTag tag) {
    final String prefix = tag.getNamespacePrefix();
    if (TapestryConstants.TEMPLATE_NAMESPACE.equals(tag.getNamespace())) {
      final Component component = TapestryUtils.getTypeOfTag(tag);
      final List<Mixin> mixins = findMixins(tag);
      return component == null
             ? new TapestryUnknownTagDescriptor(tag.getLocalName(), prefix)
             : new TapestryTagDescriptor(component, mixins, prefix);
    }
    else if (TapestryConstants.PARAMETERS_NAMESPACE.equals(tag.getNamespace())) {
      final Component component = TapestryUtils.getTypeOfTag(tag.getParentTag());
      final String parameterName = tag.getLocalName();
      final TapestryParameter parameter = component == null ? null : component.getParameters().get(parameterName);
      return parameter == null
             ? new TapestryUnknownTagDescriptor(parameterName, prefix)
             : new TapestryParameterDescriptor(component, parameter, prefix);
    }
    return null;
  }

  @NotNull
  private static List<Mixin> findMixins(@Nullable XmlTag tag) {
    if (tag == null) {
      return Collections.emptyList();
    }
    final TapestryProject tapestryProject = TapestryUtils.getTapestryProject(tag);
    final XmlAttribute mixinsAttribute = tag.getAttribute("mixins", TapestryConstants.TEMPLATE_NAMESPACE);
    if (tapestryProject == null || mixinsAttribute == null) {
      return Collections.emptyList();
    }
    final List<Mixin> result = new ArrayList<Mixin>();
    final String[] components = mixinsAttribute.getValue().split(",");
    for (String mixinName : components) {
      final Mixin mixin = tapestryProject.findMixin(mixinName);
      if (mixin != null) {
        result.add(mixin);
      }
    }
    return result;
  }
}

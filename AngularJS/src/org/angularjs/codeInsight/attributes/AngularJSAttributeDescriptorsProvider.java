package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.html.dtd.HtmlElementDescriptorImpl;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ThreeState;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import com.intellij.xml.XmlElementDescriptor;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.index.AngularDirectivesDocIndex;
import org.angularjs.index.AngularDirectivesIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.angularjs.codeInsight.attributes.AngularAttributesRegistry.createDescriptor;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSAttributeDescriptorsProvider implements XmlAttributeDescriptorsProvider {
  @Override
  public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag xmlTag) {
    if (xmlTag != null) {
      final Map<String, XmlAttributeDescriptor> result = new LinkedHashMap<>();
      final Project project = xmlTag.getProject();
      final XmlElementDescriptor descriptor = xmlTag.getDescriptor();
      final Collection<String> directives = AngularIndexUtil.getAllKeys(AngularDirectivesIndex.KEY, project);
      if (AngularIndexUtil.hasAngularJS2(project)) {
        if (descriptor instanceof HtmlElementDescriptorImpl) {
          final XmlAttributeDescriptor[] descriptors = ((HtmlElementDescriptorImpl)descriptor).getDefaultAttributeDescriptors(xmlTag);
          for (XmlAttributeDescriptor attributeDescriptor : descriptors) {
            final String name = attributeDescriptor.getName();
            if (name.startsWith("on")) {
              addAttributes(project, result, "(" + name.substring(2) + ")");
            }
          }
        }
        for (XmlAttribute attribute : xmlTag.getAttributes()) {
          final String name = attribute.getName();
          if (isAngular2Attribute(name, project) || !directives.contains(name)) continue;
          final PsiElement declaration = AngularIndexUtil.resolve(project, AngularDirectivesIndex.KEY, name);
          if (declaration != null) {
            for (XmlAttributeDescriptor binding : AngularAttributeDescriptor.getFieldBasedDescriptors((JSImplicitElement)declaration)) {
              result.put(binding.getName(), binding);
            }
          }
        }
      }
      final Collection<String> docDirectives = AngularIndexUtil.getAllKeys(AngularDirectivesDocIndex.KEY, project);
      for (String directiveName : docDirectives) {
        if (isApplicable(project, directiveName, xmlTag, AngularDirectivesDocIndex.KEY) == ThreeState.YES) {
          addAttributes(project, result, directiveName);
        }
      }
      for (String directiveName : directives) {
        if (!docDirectives.contains(directiveName) &&
            isApplicable(project, directiveName, xmlTag, AngularDirectivesIndex.KEY) == ThreeState.YES) {
          addAttributes(project, result, directiveName);
        }
      }
      return result.values().toArray(new XmlAttributeDescriptor[result.size()]);
    }
    return XmlAttributeDescriptor.EMPTY;
  }

  protected void addAttributes(Project project, Map<String, XmlAttributeDescriptor> result, String directiveName) {
    result.put(directiveName, createDescriptor(project, directiveName));
    if ("ng-repeat".equals(directiveName)) {
      result.put(directiveName + "-start", createDescriptor(project, directiveName + "-start"));
      result.put(directiveName + "-end", createDescriptor(project, directiveName + "-end"));
    }
  }

  private static ThreeState isApplicable(Project project, String directiveName, XmlTag tag, final StubIndexKey<String, JSImplicitElementProvider> index) {
    final JSImplicitElement directive = AngularIndexUtil.resolve(project, index, directiveName);
    if (directive == null) {
      return ThreeState.UNSURE;
    }

    final String restrictions = directive.getTypeString();
    if (restrictions != null) {
      final String[] split = restrictions.split(";", -1);
      final String restrict = AngularIndexUtil.convertRestrictions(project, split[0]);
      final String requiredTag = split[1];
      if (!StringUtil.isEmpty(restrict) && !StringUtil.containsIgnoreCase(restrict, "A")) {
        return ThreeState.NO;
      }
      if (!tagMatches(tag, requiredTag)) {
        return ThreeState.NO;
      }
    }

    return ThreeState.YES;
  }

  private static boolean tagMatches(XmlTag tag, String requiredTag) {
    if (StringUtil.isEmpty(requiredTag) || StringUtil.equalsIgnoreCase(requiredTag, "ANY")) {
      return true;
    }
    for (String s : requiredTag.split(",")) {
      if (StringUtil.equalsIgnoreCase(tag.getName(), s.trim())) {
        return true;
      }
    }
    if ("input".equalsIgnoreCase(requiredTag)) {
      PsiElement parent = tag;
      while (parent != null) {
        parent = parent.getParent();
        if (parent instanceof XmlTag && isForm((XmlTag)parent)) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean isForm(XmlTag parent) {
    final String name = parent.getName();
    return "form".equalsIgnoreCase(name) || "ng-form".equalsIgnoreCase(name);
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(final String attrName, XmlTag xmlTag) {
    if (xmlTag != null) {
      final Project project = xmlTag.getProject();
      final String attributeName = DirectiveUtil.normalizeAttributeName(attrName);
      ThreeState attributeAvailable = isApplicable(project, attributeName, xmlTag, AngularDirectivesDocIndex.KEY);
      if (attributeAvailable == ThreeState.UNSURE) {
        attributeAvailable = isApplicable(project, attributeName, xmlTag, AngularDirectivesIndex.KEY);
      }
      if (attributeAvailable == ThreeState.YES) {
        return createDescriptor(project, attributeName);
      }
      for (XmlAttribute attribute : xmlTag.getAttributes()) {
        if (isAngular2Attribute(attribute.getName(), project) || attribute.getName().equals(attrName)) continue;
        final PsiElement declaration = AngularIndexUtil.resolve(project, AngularDirectivesIndex.KEY, attribute.getName());
        if (declaration != null) {
          for (XmlAttributeDescriptor binding : AngularAttributeDescriptor.getFieldBasedDescriptors((JSImplicitElement)declaration)) {
            if (binding.getName().equals(attrName)) {
              return binding;
            }
          }
        }
      }

      if (AngularAttributesRegistry.isBindingAttribute(attrName, project)) {
        return new AngularBindingDescriptor(xmlTag, attrName);
      }
      if (AngularAttributesRegistry.isEventAttribute(attrName, project)) {
        return new AngularEventHandlerDescriptor(xmlTag, attrName);
      }
      return getAngular2Descriptor(attrName, project);
    }
    return null;
  }

  @Nullable
  public static AngularAttributeDescriptor getAngular2Descriptor(String attrName, Project project) {
    if (isAngular2Attribute(attrName, project)) {
      return createDescriptor(project, attrName);
    }
    return null;
  }

  protected static boolean isAngular2Attribute(String attrName, Project project) {
    return AngularAttributesRegistry.isEventAttribute(attrName, project) ||
           AngularAttributesRegistry.isBindingAttribute(attrName, project) ||
           AngularAttributesRegistry.isVariableAttribute(attrName, project);
  }
}

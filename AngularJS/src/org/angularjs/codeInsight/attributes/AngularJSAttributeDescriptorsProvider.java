package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.html.dtd.HtmlElementDescriptorImpl;
import com.intellij.psi.stubs.StubIndexKey;
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
      final Map<String, XmlAttributeDescriptor> result = new LinkedHashMap<String, XmlAttributeDescriptor>();
      final Project project = xmlTag.getProject();
      final XmlElementDescriptor descriptor = xmlTag.getDescriptor();
      if (descriptor instanceof HtmlElementDescriptorImpl && AngularIndexUtil.hasAngularJS2(project)) {
        final XmlAttributeDescriptor[] descriptors = ((HtmlElementDescriptorImpl)descriptor).getDefaultAttributeDescriptors(xmlTag);
        for (XmlAttributeDescriptor attributeDescriptor : descriptors) {
          final String name = attributeDescriptor.getName();
          if (name.startsWith("on")) {
            addAttributes(project, result, "(" + name.substring(2) + ")");
          }
        }
      }
      final Collection<String> docDirectives = AngularIndexUtil.getAllKeys(AngularDirectivesDocIndex.KEY, project);
      for (String directiveName : docDirectives) {
        if (isApplicable(project, directiveName, xmlTag, AngularDirectivesDocIndex.KEY) == ThreeState.YES) {
          addAttributes(project, result, directiveName);
        }
      }
      for (String directiveName : AngularIndexUtil.getAllKeys(AngularDirectivesIndex.KEY, project)) {
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
        if (parent instanceof XmlTag && "form".equalsIgnoreCase(((XmlTag)parent).getName())) {
          return true;
        }
      }
    }
    return false;
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(final String attrName, XmlTag xmlTag) {
    if (xmlTag != null) {
      final Project project = xmlTag.getProject();
      if (AngularAttributesRegistry.isEventAttribute(attrName, project) ||
          AngularAttributesRegistry.isVariableAttribute(attrName, project)) {
        return createDescriptor(project, attrName);
      }
      if (AngularAttributesRegistry.isBindingAttribute(attrName, project)) {
        return new AngularBindingDescriptor(project, attrName);
      }

      final String attributeName = DirectiveUtil.normalizeAttributeName(attrName);
      ThreeState attributeAvailable = isApplicable(project, attributeName, xmlTag, AngularDirectivesDocIndex.KEY);
      if (attributeAvailable == ThreeState.UNSURE) {
        attributeAvailable = isApplicable(project, attributeName, xmlTag, AngularDirectivesIndex.KEY);
      }
      return attributeAvailable == ThreeState.YES ? createDescriptor(project, attributeName) : null;
    }
    return null;
  }
}

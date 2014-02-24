package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.ID;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
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
      final Project project = xmlTag.getProject();
      final Map<String, XmlAttributeDescriptor> result = new LinkedHashMap<String, XmlAttributeDescriptor>();
      final Collection<String> docDirectives = AngularIndexUtil.getAllKeys(AngularDirectivesDocIndex.INDEX_ID, project, false);
      for (String directiveName : docDirectives) {
        if (isApplicable(project, directiveName, xmlTag.getName(), AngularDirectivesDocIndex.INDEX_ID)) {
          result.put(directiveName, createDescriptor(project, directiveName));
        }
      }
      for (String directiveName : AngularIndexUtil.getAllKeys(AngularDirectivesIndex.INDEX_ID, project, false)) {
        if (!docDirectives.contains(directiveName) &&
            isApplicable(project, directiveName, xmlTag.getName(), AngularDirectivesIndex.INDEX_ID)) {
          result.put(directiveName, createDescriptor(project, directiveName));
        }
      }
      return result.values().toArray(new XmlAttributeDescriptor[result.size()]);
    }
    return XmlAttributeDescriptor.EMPTY;
  }

  private static boolean isApplicable(Project project, String directiveName, String tagName, final ID<String, Void> index) {
    final JSNamedElementProxy directive = AngularIndexUtil.resolve(project, index, directiveName);
    final String restrictions = directive != null ? directive.getIndexItem().getTypeString() : null;
    if (restrictions != null) {
      final String[] split = restrictions.split(";", -1);
      final String restrict = split[0];
      final String tag = split[1];
      if (!StringUtil.isEmpty(restrict) && !StringUtil.containsIgnoreCase(restrict, "A")) {
        return false;
      }
      if (!tagMatches(tagName, tag)) {
        return false;
      }
    }

    return true;
  }

  private static boolean tagMatches(String tagName, String tag) {
    if (StringUtil.isEmpty(tag) || StringUtil.equalsIgnoreCase(tag, "ANY")) {
      return true;
    }
    for (String s : tag.split(",")) {
      if (StringUtil.equalsIgnoreCase(tagName, s.trim())) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(final String attrName, XmlTag xmlTag) {
    final String attributeName = DirectiveUtil.normalizeAttributeName(attrName);
    if (xmlTag != null) {
      final Project project = xmlTag.getProject();
      boolean attributeAvailable;
      if (AngularIndexUtil.getAllKeys(AngularDirectivesDocIndex.INDEX_ID, project, false).contains(attributeName)) {
        attributeAvailable = isApplicable(project, attributeName, xmlTag.getName(), AngularDirectivesDocIndex.INDEX_ID);
      } else {
        attributeAvailable = AngularIndexUtil.getAllKeys(AngularDirectivesIndex.INDEX_ID, project, false).contains(attributeName);
      }
      return attributeAvailable ? createDescriptor(project, attributeName) : null;
    }
    return null;
  }
}

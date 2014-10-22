package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ThreeState;
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
      final Collection<String> docDirectives = AngularIndexUtil.getAllKeys(AngularDirectivesDocIndex.INDEX_ID, project);
      for (String directiveName : docDirectives) {
        if (isApplicable(project, directiveName, xmlTag.getName(), AngularDirectivesDocIndex.INDEX_ID) == ThreeState.YES) {
          addAttributes(project, result, directiveName);
        }
      }
      for (String directiveName : AngularIndexUtil.getAllKeys(AngularDirectivesIndex.INDEX_ID, project)) {
        if (!docDirectives.contains(directiveName) &&
            isApplicable(project, directiveName, xmlTag.getName(), AngularDirectivesIndex.INDEX_ID) == ThreeState.YES) {
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

  private static ThreeState isApplicable(Project project, String directiveName, String tagName, final ID<String, Void> index) {
    final JSNamedElementProxy directive = AngularIndexUtil.resolve(project, index, directiveName);
    if (directive == null) {
      return ThreeState.UNSURE;
    }

    final String restrictions = directive.getIndexItem().getTypeString();
    if (restrictions != null) {
      final String[] split = restrictions.split(";", -1);
      final String restrict = AngularIndexUtil.convertRestrictions(project, split[0]);
      final String tag = split[1];
      if (!StringUtil.isEmpty(restrict) && !StringUtil.containsIgnoreCase(restrict, "A")) {
        return ThreeState.NO;
      }
      if (!tagMatches(tagName, tag)) {
        return ThreeState.NO;
      }
    }

    return ThreeState.YES;
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
      final String tagName = xmlTag.getName();
      ThreeState attributeAvailable = isApplicable(project, attributeName, tagName, AngularDirectivesDocIndex.INDEX_ID);
      if (attributeAvailable == ThreeState.UNSURE) {
        attributeAvailable = isApplicable(project, attributeName, tagName, AngularDirectivesIndex.INDEX_ID);
      }
      return attributeAvailable == ThreeState.YES ? createDescriptor(project, attributeName) : null;
    }
    return null;
  }
}

package org.angularjs.codeInsight;

import org.angularjs.index.AngularDirectivesIndex;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.angularjs.codeInsight.AngularAttributesRegistry.createDescriptor;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSAttributeDescriptorsProvider implements XmlAttributeDescriptorsProvider {
  @Override
  public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag xmlTag) {
    if (xmlTag != null) {
      final Project project = xmlTag.getProject();
      final Map<String, XmlAttributeDescriptor> result = new LinkedHashMap<String, XmlAttributeDescriptor>();
      for (String directiveName : AngularIndexUtil.getAllKeys(AngularDirectivesIndex.INDEX_ID, project)) {
        result.put(directiveName, createDescriptor(project, directiveName));
      }
      return result.values().toArray(new XmlAttributeDescriptor[result.size()]);
    }
    return XmlAttributeDescriptor.EMPTY;
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(final String attrName, XmlTag xmlTag) {
    final String attributeName = normalizeAttributeName(attrName);
    if (xmlTag != null) {
      final Project project = xmlTag.getProject();
      return AngularIndexUtil.getAllKeys(AngularDirectivesIndex.INDEX_ID, project).contains(attributeName) ?
             createDescriptor(project, attributeName) : null;
    }
    return null;
  }

  private static String normalizeAttributeName(String name) {
    if (name == null) return null;
    if (name.startsWith("data-")) {
      name = name.substring(5);
    } else if (name.startsWith("x-")) {
      name = name.substring(2);
    }
    name = name.replace(':', '-');
    name = name.replace('_', '-');
    return name;
  }
}

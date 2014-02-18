package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlAttribute;
import org.angularjs.index.AngularControllerIndex;
import org.angularjs.index.AngularDirectivesDocIndex;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularModuleIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularAttributesRegistry {
  static AngularAttributeDescriptor createDescriptor(@Nullable final Project project,
                                                     @NotNull String directiveName) {
    if ("ng-controller".equals(directiveName)) {
      return new AngularAttributeDescriptor(project, directiveName, AngularControllerIndex.INDEX_ID);
    }
    if ("ng-app".equals(directiveName)) {
      return new AngularAttributeDescriptor(project, directiveName, AngularModuleIndex.INDEX_ID);
    }
    return new AngularAttributeDescriptor(project, directiveName, null);
  }

  public static boolean isAngularExpressionAttribute(XmlAttribute parent) {
    final String attributeName = AngularJSAttributeDescriptorsProvider.normalizeAttributeName(parent.getName());
    final JSNamedElementProxy directive = AngularIndexUtil.resolve(parent.getProject(), AngularDirectivesDocIndex.INDEX_ID, attributeName);
    if (directive != null) {
      final String restrict = directive.getIndexItem().getTypeString();
      return restrict.split(";", -1)[2].endsWith("expression");
    }
    return false;
  }
}

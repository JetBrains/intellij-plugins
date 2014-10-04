package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlAttribute;
import org.angularjs.codeInsight.DirectiveUtil;
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
    final String attributeName = DirectiveUtil.normalizeAttributeName(parent.getName());
    final JSNamedElementProxy directive = AngularIndexUtil.resolve(parent.getProject(), AngularDirectivesDocIndex.INDEX_ID, attributeName);
    if (directive != null) {
      final String restrict = directive.getIndexItem().getTypeString();
      final String param = restrict.split(";", -1)[2];
      return param.endsWith("expression") || param.startsWith("string");
    }
    return false;
  }

  public static boolean isJSONAttribute(XmlAttribute parent) {
    final String value = parent.getValue();
    if (value == null || !value.startsWith("{")) return false;

    final String attributeName = DirectiveUtil.normalizeAttributeName(parent.getName());
    final JSNamedElementProxy directive = AngularIndexUtil.resolve(parent.getProject(), AngularDirectivesDocIndex.INDEX_ID, attributeName);
    if (directive != null) {
      final String restrict = directive.getIndexItem().getTypeString();
      final String type = restrict.split(";", -1)[2];
      return type.contains("object literal") || type.equals("mixed");
    }
    return false;
  }
}

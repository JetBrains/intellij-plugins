package org.angularjs.codeInsight.attributes;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.xml.XmlAttributeDescriptor;
import org.angularjs.index.AngularControllerIndex;
import org.angularjs.index.AngularModuleIndex;
import com.intellij.openapi.project.Project;
import org.angularjs.codeInsight.attributes.AngularAttributeDescriptor;
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

  public static boolean isAngularAttribute(XmlAttribute parent, final String name) {
    final XmlAttributeDescriptor descriptor = parent.getDescriptor();
    return descriptor instanceof AngularAttributeDescriptor && name.equals(descriptor.getName());
  }
}

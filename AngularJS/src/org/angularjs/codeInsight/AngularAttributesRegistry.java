package org.angularjs.codeInsight;

import com.intellij.openapi.project.Project;
import org.angularjs.codeInsight.attributes.AngularAttributeDescriptor;
import org.angularjs.codeInsight.attributes.ControllerAttributeDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularAttributesRegistry {
  static AngularAttributeDescriptor createDescriptor(@Nullable final Project project,
                                                     @NotNull String directiveName) {
    if ("ng-controller".equals(directiveName)) {
      return new ControllerAttributeDescriptor(project);
    }
    return new AngularAttributeDescriptor(project, directiveName);
  }
}

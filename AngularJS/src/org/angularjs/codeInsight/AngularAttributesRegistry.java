package org.angularjs.codeInsight;

import com.intellij.lang.javascript.index.AngularControllerIndex;
import com.intellij.lang.javascript.index.AngularModuleIndex;
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
}

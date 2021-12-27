// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.xml.XmlAttributeDescriptor;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.index.AngularControllerIndex;
import org.angularjs.index.AngularModuleIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public final class AngularAttributesRegistry {

  public static @NotNull AngularAttributeDescriptor createDescriptor(final @Nullable Project project,
                                                                     @NotNull String attributeName,
                                                                     @Nullable PsiElement declaration) {
    if ("ngController".equals(DirectiveUtil.normalizeAttributeName(attributeName))) {
      return new AngularAttributeDescriptor(project, attributeName, AngularControllerIndex.KEY, declaration);
    }
    if ("ngApp".equals(DirectiveUtil.normalizeAttributeName(attributeName))) {
      return new AngularAttributeDescriptor(project, attributeName, AngularModuleIndex.KEY, declaration);
    }
    return new AngularAttributeDescriptor(project, attributeName, null, declaration);
  }

  public static boolean isAngularExpressionAttribute(XmlAttribute parent) {
    final String type = getType(parent);
    return type.endsWith("expression") || type.startsWith("string");
  }

  public static boolean isJSONAttribute(XmlAttribute parent) {
    final String value = parent.getValue();
    if (value == null || !value.startsWith("{")) return false;

    final String type = getType(parent);
    return type.contains("object literal") || type.equals("mixed");
  }

  private static @NotNull String getType(XmlAttribute parent) {
    XmlAttributeDescriptor descriptor = AngularJSAttributeDescriptorsProvider.getDescriptor(parent.getName(), parent.getParent());
    final PsiElement directive = descriptor != null ? descriptor.getDeclaration() : null;
    if (directive instanceof JSImplicitElement) {
      final String restrict = ((JSImplicitElement)directive).getUserStringData();
      final String[] args = restrict != null ? restrict.split(";", -1) : null;
      return args != null && args.length > 2 ? args[2] : "";
    }
    return "";
  }
}

package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.xml.XmlAttributeDescriptor;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.index.AngularControllerIndex;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularModuleIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularAttributesRegistry {

  private static final List<String> CUSTOM_NG_ATTRS = Collections.singletonList("i18n");

  @NotNull
  public static AngularAttributeDescriptor createDescriptor(@Nullable final Project project,
                                                            @NotNull String directiveName,
                                                            @Nullable PsiElement declaration) {
    return createDescriptor(project, directiveName, declaration != null ? Collections.singletonList(declaration) : Collections.emptyList());
  }

  @NotNull
  public static AngularAttributeDescriptor createDescriptor(@Nullable final Project project,
                                                            @NotNull String directiveName,
                                                            @NotNull List<PsiElement> declarations) {
    if ("ng-controller".equals(directiveName)) {
      return new AngularAttributeDescriptor(project, directiveName, AngularControllerIndex.KEY, declarations);
    }
    if ("ng-app".equals(directiveName)) {
      return new AngularAttributeDescriptor(project, directiveName, AngularModuleIndex.KEY, declarations);
    }
    return new AngularAttributeDescriptor(project, directiveName, null, declarations);
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

  @NotNull
  private static String getType(XmlAttribute parent) {
    final String attributeName = DirectiveUtil.normalizeAttributeName(parent.getName());
    XmlAttributeDescriptor descriptor = AngularJSAttributeDescriptorsProvider.getDescriptor(attributeName, parent.getParent());
    final PsiElement directive = descriptor != null ? descriptor.getDeclaration() : null;
    if (directive instanceof JSImplicitElement) {
      final String restrict = ((JSImplicitElement)directive).getTypeString();
      final String[] args = restrict != null ? restrict.split(";", -1) : null;
      return args != null && args.length > 2 ? args[2] : "";
    }
    return "";
  }

  @NotNull
  public static List<String> getCustomAngularAttributes() {
    return CUSTOM_NG_ATTRS;
  }

  /**
   * @deprecated Kept for compatibility with NativeScript. Use Psi element visitor to determine attribute type.
   */
  public static boolean isEventAttribute(String name, Project project) {
    return name.startsWith("(") && name.endsWith(")") && AngularIndexUtil.hasAngularJS2(project);
  }

  /**
   * @deprecated Kept for compatibility with NativeScript. Use Psi element visitor to determine attribute type.
   */
  public static boolean isTagReferenceAttribute(String name, Project project) {
    return name.startsWith("#") && AngularIndexUtil.hasAngularJS2(project);
  }

  /**
   * @deprecated Kept for compatibility with NativeScript. Use Psi element visitor to determine attribute type.
   */
  public static boolean isVariableAttribute(String name, Project project) {
    return name.startsWith("let-") && AngularIndexUtil.hasAngularJS2(project);
  }

  /**
   * @deprecated Kept for compatibility with NativeScript. Use Psi element visitor to determine attribute type.
   */
  public static boolean isBindingAttribute(String name, Project project) {
    return name.startsWith("[") && name.endsWith("]") && AngularIndexUtil.hasAngularJS2(project);
  }

}

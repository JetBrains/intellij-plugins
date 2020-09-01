// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.util.text.Strings;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.CharSequenceSubSequence;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.angularjs.codeInsight.attributes.AngularAttributesRegistry.createDescriptor;
import static org.angularjs.index.AngularJSDirectivesSupport.findAttributeDirectives;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSAttributeDescriptorsProvider implements XmlAttributeDescriptorsProvider {

  @Override
  public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag xmlTag) {
    if (xmlTag != null) {
      final Project project = xmlTag.getProject();
      if (!AngularIndexUtil.hasAngularJS(project)) return XmlAttributeDescriptor.EMPTY;

      final Map<String, XmlAttributeDescriptor> result = new LinkedHashMap<>();
      for (JSImplicitElement directive : ContainerUtil.concat(findAttributeDirectives(project, null))) {
        String name = directive.getName();
        if (name != null && !result.containsKey(name) && isApplicable(project, xmlTag, directive)) {
          addAttributes(project, result, directive.getName(), directive);
        }
      }
      return result.values().toArray(XmlAttributeDescriptor.EMPTY);
    }
    return XmlAttributeDescriptor.EMPTY;
  }

  protected void addAttributes(@Nullable Project project,
                               @NotNull Map<String, XmlAttributeDescriptor> result,
                               @NotNull String directiveName,
                               @NotNull PsiElement declaration) {
    result.put(directiveName, createDescriptor(project, DirectiveUtil.getAttributeName(directiveName), declaration));
    if ("ngRepeat".equals(directiveName)) {
      result.put("ngRepeatStart", createDescriptor(project, "ng-repeat-start", declaration));
      result.put("ngRepeatEnd", createDescriptor(project, "ng-repeat-end", declaration));
    }
  }

  private static boolean isApplicable(@NotNull Project project, @NotNull XmlTag tag, @NotNull JSImplicitElement directive) {
    final String restrictions = directive.getTypeString();
    if (restrictions != null) {
      int semicolon = restrictions.indexOf(';');
      if (semicolon < 0) {
        return true;
      }
      CharSequence restrict = AngularIndexUtil.convertRestrictions(project, new CharSequenceSubSequence(restrictions, 0, semicolon));
      if (!StringUtil.isEmpty(restrict) && Strings.indexOfIgnoreCase(restrict, "A", 0) < 0) {
        return false;
      }
      int secondSemicolon = restrictions.indexOf(';', semicolon + 1);
      if (secondSemicolon < 0) {
        secondSemicolon = restrictions.length();
      }
      final CharSequence requiredTagAndAttr = new CharSequenceSubSequence(restrictions, semicolon + 1, secondSemicolon);
      if (!tagAndAttrMatches(tag, requiredTagAndAttr)) {
        return false;
      }
    }
    return true;
  }

  private static boolean tagAndAttrMatches(XmlTag tag, CharSequence requiredTagAndDirective) {
    if (requiredTagAndDirective.length() == 0) {
      return true;
    }
    int indexOfEquals = Strings.indexOf(requiredTagAndDirective, '=');
    if (indexOfEquals < 0) {
      return tagMatches(tag, requiredTagAndDirective);
    }
    else if (!tagMatches(tag, new CharSequenceSubSequence(requiredTagAndDirective, 0, indexOfEquals))) {
      return false;
    }
    CharSequence requiredAttr =
      StringUtil.trim(new CharSequenceSubSequence(requiredTagAndDirective, indexOfEquals + 1, requiredTagAndDirective.length()));
    if (requiredAttr.length() == 0) {
      return true;
    }
    for (XmlAttribute attr : tag.getAttributes()) {
      if (StringUtil.equals(requiredAttr, DirectiveUtil.normalizeAttributeName(attr.getName(), true))) {
        return true;
      }
    }
    return false;
  }

  private static boolean tagMatches(XmlTag tag, CharSequence requiredTag) {
    if (StringUtil.isEmpty(requiredTag) || StringUtil.equalsIgnoreCase(requiredTag, "ANY")) {
      return true;
    }
    CharSequence normalizedTag = DirectiveUtil.normalizeAttributeName(tag.getName(), true);
    int last = -1;
    do {
      int nextIndex = Strings.indexOf(requiredTag, ',', last + 1);
      CharSequence requirement =
        StringUtil.trim(new CharSequenceSubSequence(requiredTag, last + 1, nextIndex > 0 ? nextIndex : requiredTag.length()));
      if (StringUtil.equals(normalizedTag, requirement)
          || StringUtil.equalsIgnoreCase(tag.getName(), requirement)) {
        return true;
      }
      last = nextIndex;
    }
    while (last > 0);
    if (StringUtil.equalsIgnoreCase("input", requiredTag)) {
      PsiElement parent = tag;
      while (parent != null && !(parent instanceof PsiFile)) {
        parent = parent.getParent();
        if (parent instanceof XmlTag && isForm((XmlTag)parent)) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean isForm(XmlTag parent) {
    final String name = parent.getName();
    return "form".equalsIgnoreCase(name)
           || DirectiveUtil.normalizeAttributeName(name).equals("ngForm");
  }

  @Override
  public @Nullable XmlAttributeDescriptor getAttributeDescriptor(final String attrName, XmlTag xmlTag) {
    return getDescriptor(attrName, xmlTag);
  }

  static XmlAttributeDescriptor getDescriptor(String attrName, XmlTag xmlTag) {
    if (xmlTag != null) {
      final Project project = xmlTag.getProject();
      if (!AngularIndexUtil.hasAngularJS(xmlTag.getProject())) return null;

      final String directiveName = DirectiveUtil.normalizeAttributeName(attrName);
      for (JSImplicitElement directive : findAttributeDirectives(project, directiveName)) {
        String name = directive.getName();
        if (name != null && directiveName.equals(name) && isApplicable(project, xmlTag, directive)) {
          return createDescriptor(project, attrName, directive);
        }
      }
    }
    return null;
  }
}

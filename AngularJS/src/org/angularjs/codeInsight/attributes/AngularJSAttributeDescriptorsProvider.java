// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.index.AngularDirectivesDocIndex;
import org.angularjs.index.AngularDirectivesIndex;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularJSIndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
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
      if (!AngularIndexUtil.hasAngularJS(xmlTag.getProject())) return XmlAttributeDescriptor.EMPTY;

      final Map<String, XmlAttributeDescriptor> result = new LinkedHashMap<>();
      final Collection<String> directives = AngularIndexUtil.getAllKeys(AngularDirectivesIndex.KEY, project);
      final Collection<String> docDirectives = AngularIndexUtil.getAllKeys(AngularDirectivesDocIndex.KEY, project);
      for (String directiveName : docDirectives) {
        PsiElement declaration = applicableDirective(project, directiveName, xmlTag, AngularDirectivesDocIndex.KEY);
        if (isApplicable(declaration)) {
          addAttributes(project, result, directiveName, declaration);
        }
      }
      for (String directiveName : directives) {
        if (!docDirectives.contains(directiveName)) {
          PsiElement declaration = applicableDirective(project, directiveName, xmlTag, AngularDirectivesIndex.KEY);
          if (isApplicable(declaration)) {
            addAttributes(project, result, directiveName, declaration);
          }
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

  private static PsiElement applicableDirective(@NotNull Project project,
                                                @NotNull String directiveName,
                                                @NotNull XmlTag tag,
                                                final @NotNull StubIndexKey<String, JSImplicitElementProvider> index) {
    Ref<PsiElement> result = Ref.create(PsiUtilCore.NULL_PSI_ELEMENT);
    AngularIndexUtil.multiResolve(project, index, directiveName, (directive) -> {
      // Ensure this is our element
      if (directive == null
          || (!AngularJSIndexingHandler.ANGULAR_DIRECTIVES_INDEX_USER_STRING.equals(directive.getUserString())
              && !AngularJSIndexingHandler.ANGULAR_DIRECTIVES_DOC_INDEX_USER_STRING.equals(directive.getUserString()))) {
        return true;
      }
      if (isApplicable(project, tag, directive)) {
        result.set(directive);
        return false;
      }
      else {
        result.set(null);
      }
      return true;
    });
    return result.get();
  }

  private static boolean isApplicable(@NotNull Project project, @NotNull XmlTag tag, @NotNull JSImplicitElement directive) {

    final String restrictions = directive.getTypeString();
    if (restrictions != null) {
      final String[] split = restrictions.split(";", -1);
      final String restrict = AngularIndexUtil.convertRestrictions(project, split[0]);
      final String requiredTagAndAttr = split[1];
      if (!StringUtil.isEmpty(restrict) && !StringUtil.containsIgnoreCase(restrict, "A")) {
        return false;
      }
      if (!tagAndAttrMatches(tag, requiredTagAndAttr)) {
        return false;
      }
    }
    return true;
  }

  private static boolean tagAndAttrMatches(XmlTag tag, String requiredTagAndDirective) {
    List<String> tagAndDirectiveSplit = StringUtil.split(requiredTagAndDirective, "=");
    if (tagAndDirectiveSplit.isEmpty()) {
      return true;
    }
    if (!tagMatches(tag, tagAndDirectiveSplit.get(0))) {
      return false;
    }
    if (tagAndDirectiveSplit.size() == 1) {
      return true;
    }
    String requiredAttr = tagAndDirectiveSplit.get(1).trim();
    if (requiredAttr.isEmpty()) {
      return true;
    }
    for (XmlAttribute attr : tag.getAttributes()) {
      if (requiredAttr.equals(DirectiveUtil.normalizeAttributeName(attr.getName()))) {
        return true;
      }
    }
    return false;
  }

  private static boolean tagMatches(XmlTag tag, String requiredTag) {
    if (StringUtil.isEmpty(requiredTag) || StringUtil.equalsIgnoreCase(requiredTag, "ANY")) {
      return true;
    }
    String normalizedTag = DirectiveUtil.normalizeAttributeName(tag.getName());
    for (String s : StringUtil.split(requiredTag, ",")) {
      String requirement = s.trim();
      if (StringUtil.equals(normalizedTag, requirement)
          || StringUtil.equalsIgnoreCase(tag.getName(), requirement)) {
        return true;
      }
    }
    if ("input".equalsIgnoreCase(requiredTag)) {
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
      PsiElement declaration = applicableDirective(project, directiveName, xmlTag, AngularDirectivesDocIndex.KEY);
      if (declaration == PsiUtilCore.NULL_PSI_ELEMENT) {
        declaration = applicableDirective(project, directiveName, xmlTag, AngularDirectivesIndex.KEY);
      }
      if (isApplicable(declaration)) {
        return createDescriptor(project, attrName, declaration);
      }
    }
    return null;
  }

  private static boolean isApplicable(PsiElement declaration) {
    return declaration != null && declaration != PsiUtilCore.NULL_PSI_ELEMENT;
  }
}

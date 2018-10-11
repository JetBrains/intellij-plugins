// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ThreeState;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.index.AngularDirectivesDocIndex;
import org.angularjs.index.AngularDirectivesIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
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
      if (DumbService.isDumb(project) || !AngularIndexUtil.hasAngularJS(xmlTag.getProject())) return XmlAttributeDescriptor.EMPTY;

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
    result.put(directiveName, createDescriptor(project, directiveName, declaration));
    if ("ng-repeat".equals(directiveName)) {
      result.put(directiveName + "-start", createDescriptor(project, directiveName + "-start", declaration));
      result.put(directiveName + "-end", createDescriptor(project, directiveName + "-end", declaration));
    }
  }

  private static PsiElement applicableDirective(Project project,
                                                String directiveName,
                                                XmlTag tag,
                                                final StubIndexKey<String, JSImplicitElementProvider> index) {
    Ref<PsiElement> result = Ref.create(PsiUtilCore.NULL_PSI_ELEMENT);
    AngularIndexUtil.multiResolve(project, index, directiveName, (directive) -> {
      ThreeState applicable = isApplicable(project, tag, directive);
      if (applicable == ThreeState.YES) {
        result.set(directive);
      }
      if (applicable == ThreeState.NO && result.get() == PsiUtilCore.NULL_PSI_ELEMENT) {
        result.set(null);
      }
      return !result.isNull();
    });
    return result.get();
  }

  @NotNull
  public static ThreeState isApplicable(Project project, XmlTag tag, JSImplicitElement directive) {
    if (directive == null) {
      return ThreeState.UNSURE;
    }

    final String restrictions = directive.getTypeString();
    if (restrictions != null) {
      final String[] split = restrictions.split(";", -1);
      final String restrict = AngularIndexUtil.convertRestrictions(project, split[0]);
      final String requiredTag = split[1];
      if (!StringUtil.isEmpty(restrict) && !StringUtil.containsIgnoreCase(restrict, "A")) {
        return ThreeState.NO;
      }
      if (!tagMatches(tag, requiredTag)) {
        return ThreeState.NO;
      }
    }

    return ThreeState.YES;
  }

  private static boolean tagMatches(XmlTag tag, String requiredTag) {
    if (StringUtil.isEmpty(requiredTag) || StringUtil.equalsIgnoreCase(requiredTag, "ANY")) {
      return true;
    }
    for (String s : requiredTag.split(",")) {
      if (StringUtil.equalsIgnoreCase(tag.getName(), s.trim())) {
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
    return "form".equalsIgnoreCase(name) || "ng-form".equalsIgnoreCase(name);
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(final String attrName, XmlTag xmlTag) {
    return getDescriptor(attrName, xmlTag);
  }

  static XmlAttributeDescriptor getDescriptor(String attrName, XmlTag xmlTag) {
    if (xmlTag != null) {
      final Project project = xmlTag.getProject();
      if (DumbService.isDumb(project)) return null;
      
      final String attributeName = DirectiveUtil.normalizeAttributeName(attrName);
      PsiElement declaration = applicableDirective(project, attributeName, xmlTag, AngularDirectivesDocIndex.KEY);
      if (declaration == PsiUtilCore.NULL_PSI_ELEMENT) {
        declaration = applicableDirective(project, attributeName, xmlTag, AngularDirectivesIndex.KEY);
      }
      if (isApplicable(declaration)) {
        return createDescriptor(project, attributeName, declaration);
      }
    }
    return null;
  }

  private static boolean isApplicable(PsiElement declaration) {
    return declaration != null && declaration != PsiUtilCore.NULL_PSI_ELEMENT;
  }
}

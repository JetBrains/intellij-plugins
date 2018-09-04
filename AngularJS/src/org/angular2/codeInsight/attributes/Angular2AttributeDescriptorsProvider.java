// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.html.dtd.HtmlElementDescriptorImpl;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ThreeState;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import com.intellij.xml.XmlElementDescriptor;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.html.parser.Angular2HtmlElementTypes;
import org.angular2.lang.html.parser.Angular2HtmlParsing;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.codeInsight.attributes.AngularAttributeDescriptor;
import org.angularjs.codeInsight.attributes.AngularAttributesRegistry;
import org.angularjs.codeInsight.attributes.AngularJSAttributeDescriptorsProvider;
import org.angularjs.index.AngularDirectivesDocIndex;
import org.angularjs.index.AngularDirectivesIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static org.angularjs.codeInsight.attributes.AngularAttributesRegistry.createDescriptor;

public class Angular2AttributeDescriptorsProvider implements XmlAttributeDescriptorsProvider {

  @Override
  public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag xmlTag) {
    if (xmlTag != null) {
      final Project project = xmlTag.getProject();
      if (!Angular2LangUtil.isAngular2Context(xmlTag)) return XmlAttributeDescriptor.EMPTY;

      final Map<String, XmlAttributeDescriptor> result = new LinkedHashMap<>();
      final XmlElementDescriptor descriptor = xmlTag.getDescriptor();
      final Collection<String> directives = AngularIndexUtil.getAllKeys(AngularDirectivesIndex.KEY, project);

      if (descriptor instanceof HtmlElementDescriptorImpl) {
        final XmlAttributeDescriptor[] descriptors = ((HtmlElementDescriptorImpl)descriptor).getDefaultAttributeDescriptors(xmlTag);
        for (XmlAttributeDescriptor attributeDescriptor : descriptors) {
          final String name = attributeDescriptor.getName();
          if (name.startsWith("on")) {
            addAttributes(project, result, "(" + name.substring(2) + ")", attributeDescriptor.getDeclaration());
          }
          else {
            addAttributes(project, result, "[" + name + "]", attributeDescriptor.getDeclaration());
          }
        }
      }
      for (XmlAttribute attribute : xmlTag.getAttributes()) {
        final String name = attribute.getName();
        if (isAngular2Attribute(name) || !directives.contains(name)) continue;
        for (PsiElement declaration : applicableDirectives(project, name, xmlTag, AngularDirectivesIndex.KEY)) {
          if (isApplicable(declaration)) {
            for (XmlAttributeDescriptor binding : Angular2AttributeDescriptor.getFieldBasedDescriptors((JSImplicitElement)declaration)) {
              result.put(binding.getName(), binding);
            }
          }
        }
      }
      AngularAttributesRegistry.getCustomAngularAttributes().forEach(attr -> addAttributes(project, result, attr, null));
      for (String directiveName : directives) {
        for (PsiElement declaration : applicableDirectives(project, directiveName, xmlTag, AngularDirectivesIndex.KEY)) {
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
                               @Nullable PsiElement declaration) {
    result.put(directiveName, createDescriptor(project, directiveName, declaration));
  }

  private static List<PsiElement> applicableDirectives(Project project,
                                                       String directiveName,
                                                       XmlTag tag,
                                                       final StubIndexKey<String, JSImplicitElementProvider> index) {
    List<PsiElement> result = new ArrayList<>();
    AngularIndexUtil.multiResolve(project, index, directiveName, (directive) -> {
      ThreeState applicable = AngularJSAttributeDescriptorsProvider.isApplicable(project, tag, directive);
      if (applicable == ThreeState.YES && directive != PsiUtilCore.NULL_PSI_ELEMENT) {
        result.add(directive);
      }
      return true;
    });
    return result;
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(final String attrName, XmlTag xmlTag) {
    return getDescriptor(attrName, xmlTag);
  }

  static XmlAttributeDescriptor getDescriptor(String attrName, XmlTag xmlTag) {
    if (xmlTag != null) {
      if (!Angular2LangUtil.isAngular2Context(xmlTag)) return null;

      final Project project = xmlTag.getProject();
      final String attributeName = DirectiveUtil.normalizeAttributeName(attrName);
      List<PsiElement> declarations = applicableDirectives(project, attributeName, xmlTag, AngularDirectivesDocIndex.KEY);
      if (declarations.isEmpty()) {
        declarations = applicableDirectives(project, attributeName, xmlTag, AngularDirectivesIndex.KEY);
      }
      declarations = declarations.stream()
        .filter(Angular2AttributeDescriptorsProvider::isApplicable).collect(Collectors.toList());
      if (!declarations.isEmpty()) {
        return createDescriptor(project, attributeName, declarations);
      }

      for (XmlAttribute attribute : xmlTag.getAttributes()) {
        String name = attribute.getName();
        if (isAngular2Attribute(name) || name.equals(attrName)) continue;
        declarations = applicableDirectives(project, name, xmlTag, AngularDirectivesIndex.KEY)
          .stream()
          .filter(Angular2AttributeDescriptorsProvider::isApplicable)
          .collect(Collectors.toList());

        for (PsiElement declaration : declarations) {
          for (XmlAttributeDescriptor binding : Angular2AttributeDescriptor.getFieldBasedDescriptors(
            (JSImplicitElement)declaration)) {
            if (binding.getName().equals(attrName)) {
              return binding;
            }
          }
        }
      }

      XmlElementDescriptor descriptor = xmlTag.getDescriptor();
      if (descriptor instanceof HtmlElementDescriptorImpl) {
        final XmlAttributeDescriptor[] descriptors = ((HtmlElementDescriptorImpl)descriptor).getDefaultAttributeDescriptors(xmlTag);
        for (XmlAttributeDescriptor attributeDescriptor : descriptors) {
          final String name = attributeDescriptor.getName();
          if (name.startsWith("on") && attrName.equals("(" + name.substring(2) + ")")) {
            return new Angular2BindingDescriptor(attributeDescriptor.getDeclaration(), attrName);
          }
          else if (attrName.equals("[" + name + "]")) {
            return new Angular2EventHandlerDescriptor(attributeDescriptor.getDeclaration(), attrName);
          }
        }
      }

      IElementType attrType = Angular2HtmlParsing.parseAttributeName(attrName, false).first;
      if (attrType == Angular2HtmlElementTypes.PROPERTY_BINDING) {
        return new Angular2BindingDescriptor(xmlTag, attrName);
      }
      if (attrType == Angular2HtmlElementTypes.EVENT) {
        return new Angular2EventHandlerDescriptor(xmlTag, attrName);
      }
      return getAngular2Descriptor(attrName, project);
    }
    return null;
  }

  private static boolean isApplicable(PsiElement declaration) {
    return declaration != null && declaration != PsiUtilCore.NULL_PSI_ELEMENT;
  }

  @Nullable
  public static AngularAttributeDescriptor getAngular2Descriptor(String attrName, Project project) {
    if (isAngular2Attribute(attrName)) {
      return createDescriptor(project, attrName, Collections.emptyList());
    }
    return null;
  }

  protected static boolean isAngular2Attribute(String attrName) {
    return Angular2HtmlParsing.parseAttributeName(attrName, false).first != XmlElementType.XML_ATTRIBUTE
           || AngularAttributesRegistry.getCustomAngularAttributes().contains(attrName);
  }
}

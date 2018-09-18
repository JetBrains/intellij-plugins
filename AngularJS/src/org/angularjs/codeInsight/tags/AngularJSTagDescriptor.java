// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.angularjs.codeInsight.tags;

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl;
import com.intellij.psi.impl.source.xml.XmlDescriptorUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlElementsGroup;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptorsProvider;
import org.angular2.lang.Angular2LangUtil;
import org.angularjs.codeInsight.DirectiveUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.intellij.xml.XmlAttributeDescriptor.EMPTY;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSTagDescriptor implements XmlElementDescriptor {
  protected final String myName;
  private final JSImplicitElement myDeclaration;

  public AngularJSTagDescriptor(String name, JSImplicitElement declaration) {
    myName = name;
    myDeclaration = declaration;
  }

  @Override
  public String getQualifiedName() {
    return myName;
  }

  @Override
  public String getDefaultName() {
    return myName;
  }

  @Override
  public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
    return XmlDescriptorUtil.getElementsDescriptors(context);
  }

  @Override
  public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
    return XmlDescriptorUtil.getElementDescriptor(childTag, contextTag);
  }

  @Override
  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    final JSImplicitElement declaration = getDeclaration();
    final String string = Objects.requireNonNull(declaration.getTypeString());
    final String attributes = string.split(";", -1)[3];
    final String[] split = attributes.split(",");
    final List<XmlAttributeDescriptor> result = new ArrayList<>();
    if (context != null && Angular2LangUtil.isAngular2Context(context)) {
      result.addAll(Angular2AttributeDescriptor.getDescriptors(declaration));
      result.addAll(Angular2AttributeDescriptor.getExistingVarsAndRefsDescriptors(context));
    }
    else if (split.length != 1 || !split[0].isEmpty()) {
      for (String aSplit : split) {
        result.add(new AnyXmlAttributeDescriptor(DirectiveUtil.getAttributeName(aSplit)));
      }
    }
    result.addAll(Arrays.asList(HtmlNSDescriptorImpl.getCommonAttributeDescriptors(context)));
    return result.toArray(EMPTY);
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
    return getAttributeDescriptor(attribute.getName(), attribute.getParent());
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(@NonNls final String attributeName, @Nullable XmlTag context) {
    final XmlAttributeDescriptor descriptor = ContainerUtil.find(getAttributesDescriptors(context),
                                                                 descriptor1 -> attributeName.equals(descriptor1.getName()));
    if (descriptor != null) return descriptor;
    return context != null ? Angular2AttributeDescriptorsProvider.getAngular2Descriptor(attributeName, context.getProject()) : null;
  }

  @Override
  public XmlNSDescriptor getNSDescriptor() {
    return null;
  }

  @Nullable
  @Override
  public XmlElementsGroup getTopGroup() {
    return null;
  }

  @Override
  public int getContentType() {
    return CONTENT_TYPE_ANY;
  }

  @Nullable
  @Override
  public String getDefaultValue() {
    return null;
  }

  @Override
  public JSImplicitElement getDeclaration() {
    return myDeclaration;
  }

  @Override
  public String getName(PsiElement context) {
    return getName();
  }

  @Override
  public String getName() {
    return myName;
  }

  @Override
  public void init(PsiElement element) {
  }
}

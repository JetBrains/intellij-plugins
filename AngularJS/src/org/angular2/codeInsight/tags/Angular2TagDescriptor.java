// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.tags;

import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
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
import org.angular2.entities.Angular2Directive;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.angular2.codeInsight.tags.Angular2StandardTagDescriptor.mergeWithAngularDescriptorIfPossible;

public class Angular2TagDescriptor implements XmlElementDescriptor {
  private final String myName;
  private final Angular2XmlElementSourcesResolver myResolver;
  private final boolean myImplied;

  public Angular2TagDescriptor(@NotNull XmlTag tag) {
    this(tag, true, Collections.singleton(createDirective(tag)));
  }

  public Angular2TagDescriptor(@NotNull XmlTag tag,
                               boolean implied,
                               @NotNull Collection<?> sources) {
    myImplied = implied;
    myResolver = new Angular2XmlElementSourcesResolver(tag, sources, x -> Collections.emptyList(), this::getSelectors);
    myName = tag.getLocalName();
  }

  @Override
  public @NotNull String getQualifiedName() {
    return myName;
  }

  @Override
  public @NotNull String getDefaultName() {
    return myName;
  }

  @Override
  public XmlElementDescriptor @NotNull [] getElementsDescriptors(XmlTag context) {
    return XmlDescriptorUtil.getElementsDescriptors(context);
  }

  @Override
  public @Nullable XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
    return XmlDescriptorUtil.getElementDescriptor(childTag, contextTag);
  }

  @Override
  public XmlAttributeDescriptor @NotNull [] getAttributesDescriptors(@Nullable XmlTag context) {
    return HtmlNSDescriptorImpl.getCommonAttributeDescriptors(context);
  }

  @Override
  public @Nullable XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
    return getAttributeDescriptor(attribute.getName(), attribute.getParent());
  }

  @Override
  public @Nullable XmlAttributeDescriptor getAttributeDescriptor(@NonNls final String attributeName, @Nullable XmlTag context) {
    return mergeWithAngularDescriptorIfPossible(HtmlNSDescriptorImpl.getCommonAttributeDescriptor(attributeName, context),
                                                attributeName, context);
  }

  @Override
  public @Nullable XmlNSDescriptor getNSDescriptor() {
    return null;
  }

  @Override
  public @Nullable XmlElementsGroup getTopGroup() {
    return null;
  }

  @Override
  public int getContentType() {
    return CONTENT_TYPE_ANY;
  }

  @Override
  public @Nullable String getDefaultValue() {
    return null;
  }

  @Override
  public PsiElement getDeclaration() {
    return ContainerUtil.getFirstItem(myResolver.getDeclarations());
  }

  public @NotNull List<Angular2Directive> getSourceDirectives() {
    return myResolver.getSourceDirectives();
  }

  private Collection<? extends PsiElement> getSelectors(Angular2Directive directive) {
    return Collections.singleton(directive.getSelector().getPsiElementForElement(myName));
  }

  @Override
  public @NotNull String getName(PsiElement context) {
    return getName();
  }

  @Override
  public @NotNull String getName() {
    return myName;
  }

  @Override
  public void init(PsiElement element) {
  }

  public boolean allowContributions() {
    return true;
  }

  public boolean isImplied() {
    return myImplied;
  }

  private static @NotNull JSImplicitElementImpl createDirective(@NotNull XmlTag xmlTag) {
    //noinspection HardCodedStringLiteral
    return new JSImplicitElementImpl.Builder(xmlTag.getLocalName(), xmlTag)
      .setTypeString("E;;;")
      .toImplicitElement();
  }
}

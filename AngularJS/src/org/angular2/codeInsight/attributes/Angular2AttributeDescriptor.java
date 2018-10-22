// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl;
import com.intellij.lang.javascript.psi.types.JSTypeContext;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations;
import com.intellij.lang.javascript.psi.types.primitives.JSStringType;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.meta.PsiPresentableMetaData;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import com.intellij.xml.impl.XmlAttributeDescriptorEx;
import icons.AngularJSIcons;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static org.angular2.codeInsight.attributes.Angular2AttributeDescriptorsProvider.getCustomNgAttrs;
import static org.angular2.lang.html.parser.Angular2HtmlElementTypes.EVENT;

public class Angular2AttributeDescriptor extends BasicXmlAttributeDescriptor implements XmlAttributeDescriptorEx, PsiPresentableMetaData {

  private static final JSType STRING_TYPE = new JSStringType(true, JSTypeSource.EXPLICITLY_DECLARED, JSTypeContext.INSTANCE);

  @Nullable
  public static Angular2AttributeDescriptor create(@NotNull String attributeName) {
    return create(attributeName, Collections.emptyList());
  }

  @Nullable
  public static Angular2AttributeDescriptor create(@NotNull String attributeName, @NotNull PsiElement element) {
    return create(attributeName, singletonList(element));
  }

  @Nullable
  public static Angular2AttributeDescriptor create(@NotNull String attributeName, @NotNull List<PsiElement> elements) {
    if (getCustomNgAttrs().contains(attributeName)) {
      return new Angular2AttributeDescriptor(attributeName, null, elements);
    }
    Angular2AttributeNameParser.AttributeInfo info = Angular2AttributeNameParser.parse(attributeName, true);
    if (info.elementType == XmlElementType.XML_ATTRIBUTE) {
      return !elements.isEmpty()
             ? new Angular2AttributeDescriptor(attributeName, null, elements)
             : null;
    }
    if (info.elementType == EVENT) {
      return new Angular2EventHandlerDescriptor(attributeName, info.name, elements);
    }
    return new Angular2AttributeDescriptor(attributeName, info.name, elements);
  }

  @NotNull
  public static List<XmlAttributeDescriptor> getDirectiveDescriptors(@NotNull Angular2Directive directive) {
    List<XmlAttributeDescriptor> result = new ArrayList<>();
    addDirectiveDescriptors(directive.getInOuts(), Angular2AttributeDescriptor::createBananaBoxBinding, result);
    addDirectiveDescriptors(directive.getInputs(), Angular2AttributeDescriptor::createBinding, result);
    addDirectiveDescriptors(directive.getOutputs(), Angular2AttributeDescriptor::createEventHandler, result);
    addDirectiveDescriptors(directive.getInputs(), Angular2AttributeDescriptor::createOneTimeBinding, result);
    return result;
  }

  private final PsiElement[] myElements;
  private final String myAttributeName;
  private final String myBindingName;

  protected Angular2AttributeDescriptor(@NotNull String attributeName,
                                        @Nullable String bindingName,
                                        @NotNull List<PsiElement> elements) {
    myAttributeName = attributeName;
    myBindingName = bindingName;
    if (bindingName != null) {
      assert myAttributeName.contains(myBindingName);
    }
    myElements = elements.toArray(PsiElement.EMPTY_ARRAY);
  }

  @Override
  public String getName() {
    return myAttributeName;
  }

  @Override
  public void init(PsiElement element) {}

  @Override
  public boolean isRequired() {
    return false;
  }

  @Override
  public boolean hasIdType() {
    return "id".equals(myAttributeName);
  }

  @Override
  public boolean hasIdRefType() {
    return false;
  }

  @Override
  public boolean isEnumerated() {
    return false;
  }

  @Override
  public boolean isFixed() {
    return false;
  }

  @Override
  public String getDefaultValue() {
    return null;
  }

  @Override
  public String[] getEnumeratedValues() {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  @Override
  protected PsiElement getEnumeratedValueDeclaration(XmlElement xmlElement, String value) {
    return xmlElement;
  }

  @Override
  public PsiElement getDeclaration() {
    return ArrayUtil.getFirstElement(myElements);
  }

  @Nullable
  @Override
  public String handleTargetRename(@NotNull @NonNls String newTargetName) {
    if (myBindingName != null) {
      int start = myAttributeName.indexOf(myBindingName);
      return myAttributeName.substring(0, start)
             + newTargetName
             + myAttributeName.substring(start + myBindingName.length());
    }
    else {
      return newTargetName;
    }
  }

  @Override
  public String getTypeName() {
    return null;
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return AngularJSIcons.Angular2;
  }

  private static <T> void addDirectiveDescriptors(Collection<T> list,
                                                  Function<T, ? extends XmlAttributeDescriptor> factory,
                                                  List<XmlAttributeDescriptor> result) {
    list.forEach(el -> ObjectUtils.doIfNotNull(factory.apply(el), result::add));
  }

  @NotNull
  private static Angular2AttributeDescriptor createBinding(Angular2DirectiveProperty info) {
    return new Angular2AttributeDescriptor("[" + info.getName() + "]", info.getName(), singletonList(info.getNavigableElement()));
  }

  @NotNull
  private static Angular2AttributeDescriptor createBananaBoxBinding(@NotNull Pair<Angular2DirectiveProperty, Angular2DirectiveProperty> info) {
    return new Angular2AttributeDescriptor("[(" + info.first.getName() + ")]", info.first.getName(),
                                           ContainerUtil.newArrayList(info.first.getNavigableElement(),
                                                                      info.second.getNavigableElement()));
  }

  @Nullable
  private static Angular2AttributeDescriptor createOneTimeBinding(Angular2DirectiveProperty info) {
    return info.getType() != null
           && expandStringLiteralTypes(info.getType()).isDirectlyAssignableType(STRING_TYPE, null) ?
           new Angular2AttributeDescriptor(info.getName(), null, singletonList(info.getNavigableElement())) : null;
  }

  @NotNull
  private static Angular2EventHandlerDescriptor createEventHandler(Angular2DirectiveProperty info) {
    return new Angular2EventHandlerDescriptor("(" + info.getName() + ")", info.getName(),
                                              singletonList(info.getNavigableElement()));
  }

  @Contract("null->null")
  private static JSType expandStringLiteralTypes(@Nullable JSType type) {
    if (type == null) return null;
    type = TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(type);
    return type.transformTypeHierarchy(toApply -> toApply instanceof JSStringLiteralTypeImpl ? STRING_TYPE : toApply);
  }
}
